//import java.nio.file.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;
import javax.swing.JFileChooser;
import java.io.*;
import com.pixelmed.dicom.*;
import com.pixelmed.dicom.ClinicalTrialsAttributes.HandleUIDs;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.tree.*; 
import com.pixelmed.display.ConsumerFormatImageMaker;
import com.pixelmed.display.SingleImagePanel;
import com.pixelmed.display.SourceImage;
import com.pixelmed.dicom.DicomFileUtilities;
import com.pixelmed.utils.UUIDBasedOID;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DicomAnonymizer extends JFrame{
	//
	private static final long serialVersionUID = 7526472295622776147L;
	private JFileChooser c = new JFileChooser();
	private JLabel lblCurrentFile = new JLabel(" ");
	private JProgressBar progress = new JProgressBar();
	private String sourcePath;
	private String destPath;
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode("Dicom Files");
	private JTree tree = new JTree(root);
	private JScrollPane scroll = new JScrollPane(tree);	
	private final JButton btnSource = new JButton("Source");	
	private final JButton btnDest = new JButton("Destination");
	private final JButton btnAnonymize = new JButton("Anonymize");
	private final JButton btnSearch = new JButton("Search");
	private final JButton btnExit = new JButton("Exit");
	private final JButton btnCancel = new JButton("Cancel");
	//TO BE REMOVED !!!
	private final JButton btnTEST = new JButton("TEST OID");
	//private ArrayList<String> aNames = new ArrayList<String>();
	//private Boolean bNames = false;
	//private int nameIndex = 0;
	//
	private ArrayList<String> dicomSourceFiles = new ArrayList<String>();
	private JCheckBox chkPatient = new JCheckBox("Remove Patient Information");
	private JCheckBox chkModalities = new JCheckBox("Remove Device Information");
	private JCheckBox chkExtractImage = new JCheckBox("Extract Image to JPEG");	
	private JCheckBox chkMapUIDs = new JCheckBox("Uniquely Map UIDs");
	private FileHandler handler;
	private Logger errorLogger = Logger.getLogger("");	
	//this is the total number of DICOM files found in the 
	//selected folder and subfolders, will be used to update
	//the progress bar during the anonymization process
	private int totalNumFiles = 0;
	private int totalNumFolders = 0;
	private int progressIndicator = 0;
	private Boolean _singleSelection = false;
	private String newPath = "";
	//
    final SwingWorker<String, Void> treeWorker = new SwingWorker<String, Void>() {
		/** Schedule a compute-intensive task in a background thread */
    	@Override
        protected String doInBackground() throws Exception {
    		//while(!isCancelled()){
    			listAllFiles(sourcePath.toString(),root,true);
    		//}
        	//
        	return ".";
     	}
    	//
        @Override
        public void done() {
            //update the label.
            lblCurrentFile.setText("Done. " + totalNumFiles + " DICOM files found in "+ totalNumFolders + " folders");
            progress.setMaximum(0);
            tree.expandRow(0);
            //
            enableGUI();
        }     	
	};
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    final SwingWorker<String, String> anonymizeWorker = new SwingWorker<String, String>() {
  		/** Schedule a compute-intensive task in a background thread */
      	//@Override
          protected String doInBackground() throws InterruptedException{
   			//while(!isCancelled()){
   				copyTree();
   			//}   			
   			//
   			return ".";
       	}
      	//
        //@Override
        public void done() {
        	//Remove the "Loading images" label.
        	lblCurrentFile.setText("Done. " + totalNumFiles + " DICOM files anonymized");
            progress.setMaximum(0);
            //
            enableGUI();
      	}     	
    }; 
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//ctor
    private void disableGUI(){
  		btnDest.setEnabled(false);
  		btnSource.setEnabled(false);
  		btnAnonymize.setEnabled(false);
  		btnExit.setEnabled(false);
  		btnSearch.setEnabled(false);
  		btnCancel.setEnabled(true);
  		//
  		chkExtractImage.setEnabled(false);
  		chkModalities.setEnabled(false);
  		chkPatient.setEnabled(false);
  		chkMapUIDs.setEnabled(false);    	
    }
    //
    private void enableGUI(){
  		btnDest.setEnabled(true);
  		btnSource.setEnabled(true);
  		btnAnonymize.setEnabled(true);
  		btnExit.setEnabled(true);
  		btnSearch.setEnabled(true);
  		//
  		chkExtractImage.setEnabled(true);
  		chkModalities.setEnabled(true);
  		chkPatient.setEnabled(true);
  		chkMapUIDs.setEnabled(true);
  		//
  		btnExit.setEnabled(true);
  		btnCancel.setEnabled(false);    	
    }
    //
	public DicomAnonymizer(){
		buildGUI();
	}
	//
	public static void main(String[] args){
		DicomAnonymizer dcp = new DicomAnonymizer();
	}
	//constructs the interface
	private void buildGUI(){
		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
		//
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//make the window is half the width and half the height of the screen
		this.setSize((w/3)*2,h/2);
		//centre the window on the screen
		this.setLocationRelativeTo(null);			
		//disable the cancel button, no need for it yet
		btnCancel.setEnabled(false);
		//
		addControls();
		//
		setTitle("Dicom Anonymizer");
		//make sure the window can't be resized
		this.setResizable(false);
		//
		this.setVisible(true);		
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private void addControls(){
		//panel to hold checkboxes
		JPanel pnlCheckboxes = new JPanel();		
		pnlCheckboxes.setBorder(BorderFactory.createTitledBorder("Options"));
		//pnlCheckboxes.setLayout(new BoxLayout(pnlCheckboxes,BoxLayout.PAGE_AXIS));
		pnlCheckboxes.setLayout(new GridLayout(2,2));
		pnlCheckboxes.add(chkPatient);
		pnlCheckboxes.add(chkModalities);
		pnlCheckboxes.add(chkExtractImage);
		pnlCheckboxes.add(chkMapUIDs);
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		//panel to hold buttons
		JPanel pnlButtons = new JPanel();
		pnlButtons.setBorder(BorderFactory.createTitledBorder("Functions"));
		pnlButtons.setLayout(new BoxLayout(pnlButtons,BoxLayout.X_AXIS));
		//		
		btnExit.setPreferredSize(new Dimension(100, 30));
		//
		JLabel lblFiller1 = new JLabel("   ");
		JLabel lblFiller2 = new JLabel("   ");
		JLabel lblFiller11 = new JLabel("   ");
		pnlButtons.add(btnSource);
		pnlButtons.add(lblFiller1);
		pnlButtons.add(btnDest);
		pnlButtons.add(lblFiller2);
		pnlButtons.add(btnAnonymize);
		pnlButtons.add(lblFiller11);
		pnlButtons.add(btnSearch);
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		JPanel pnlTop = new JPanel();
		pnlTop.setLayout(new GridLayout(1,2));
		pnlTop.add(pnlCheckboxes);
		pnlTop.add(pnlButtons);
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		JPanel pnlSth = new JPanel();
		JPanel pnlSthTop = new JPanel(new GridLayout(1,10));
		pnlSth.setLayout(new BoxLayout(pnlSth,BoxLayout.Y_AXIS));
		//filler labels to place the "Exit" and "Cancel" buttons properly
		JLabel lblFiller3 = new JLabel("  ");
		JLabel lblFiller4 = new JLabel("          ");
		JLabel lblFiller5 = new JLabel("          ");
		JLabel lblFiller6 = new JLabel("          ");
		JLabel lblFiller7 = new JLabel("          ");
		JLabel lblFiller8 = new JLabel("          ");
		JLabel lblFiller9 = new JLabel("          ");
		JLabel lblFiller10 = new JLabel("          ");
		//
		pnlSthTop.add(btnExit);
		pnlSthTop.add(lblFiller3);
		pnlSthTop.add(btnCancel);
		pnlSthTop.add(lblFiller4);
		pnlSthTop.add(lblFiller5);
		btnTEST.setVisible(false);
		pnlSthTop.add(btnTEST);
		pnlSthTop.add(lblFiller6);
		pnlSthTop.add(lblFiller7);
		pnlSthTop.add(lblFiller8);
		pnlSthTop.add(lblFiller9);
		pnlSthTop.add(lblFiller10);
		pnlSth.add(pnlSthTop);
		//
		JPanel pnlSth1 = new JPanel();
		pnlSth1.setLayout(new GridLayout(1,1));
		pnlSth1.add(lblCurrentFile);
		//
		pnlSth.add(pnlSth1);
		pnlSth.add(progress);
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		//		
		JPanel pnlCenter = new JPanel();
		pnlCenter.setLayout(new GridLayout(1,1));
		pnlCenter.add(scroll);		
		//
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		// source images folder event
		btnSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{					
					//
					File f = new File(".");
					//
			  		c.setCurrentDirectory(f);
			  		c.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			  		//
				  	if(c.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				  		//
				  		sourcePath = c.getSelectedFile().toString();

				  		//need to check here if selection is file or directory
				  		File selFile = new File(sourcePath);
				  		if(selFile.isDirectory()){
				  			disableGUI();
				  			//
				  			treeWorker.execute();
				  		}
				  		else{
				  			if(isFileDicomImage(sourcePath.toString())){
				  				_singleSelection = true;
				  				displaySingleSelection(root, sourcePath.toString());
				  			}
				  			else{
				  				JOptionPane.showMessageDialog(null,"Selected file is not a DICOM image.");
				  			}
				  		}
				  	}
				  	else{
				  		JOptionPane.showMessageDialog(null,"Operation cancelled by user.");
				  	}
			  	}
				catch (Exception ex) {
					errorLogger.log(Level.SEVERE, ex.getMessage());
				}			  	
			}
		});
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		// destination folder(s) for images
		btnDest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{					
					//
					File f = new File(".");
					//
			  		c.setCurrentDirectory(f);
			  		c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			  		//
				  	if(c.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				  		//
				  		destPath = c.getSelectedFile().toString();
				  	}
				  	else{
				  		JOptionPane.showMessageDialog(null,"Operation cancelled by user.");
				  	}
			  	}
				catch (Exception ex) {
					errorLogger.log(Level.SEVERE, ex.getMessage());
				}			  	
			}
		});
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		// work starts here ...		
		btnAnonymize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if(sourcePath == null){
					JOptionPane.showMessageDialog(null,"Please select a SOURCE folder");
				}
				else if(destPath == null){
					JOptionPane.showMessageDialog(null,"Please select a DESTINATION folder");
				}
				else if(chkExtractImage.isSelected()==false && chkModalities.isSelected()==false && chkPatient.isSelected()==false){
					JOptionPane.showMessageDialog(null,"Please select an option/task to perform.");
				}
				else{
					disableGUI();
			  		//
					if(!_singleSelection){
						anonymizeWorker.execute();
					}
					else{
						//reset for next run ...
						_singleSelection = false;
						processSingleImage();
					}
					
				}
			}
		});		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try{
					anonymizeWorker.cancel(true);
			  	}
				catch (Exception ex) {
					errorLogger.log(Level.SEVERE, ex.getMessage());
				}			  	
			}
		});		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		// action listener for the Exit button
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		// action listener for the Search button
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if(sourcePath == null || sourcePath.length() == 0){
					JOptionPane.showMessageDialog(null, "Please select a source folder to search.");
				}
				else{
					//errorLogger.log(Level.INFO, "Search started...");
					//
					DicomSearch ds = new DicomSearch(dicomSourceFiles);
				}
			}
		});			
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//		
		tree.addMouseListener(new MouseAdapter() {
     		public void mouseClicked(MouseEvent e) {
         		if (e.getClickCount() == 2) {
         			//
         			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	         		//
	         		if(node.isLeaf()){	//not a root node so continue ...
	         			TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
	         			//
	         			//JOptionPane.showMessageDialog(null, tp.toString());
						String newPath = tp.toString().replace("[", "");
						newPath = newPath.replace("]", "");
						//
						String[] arr = newPath.split(",");
						String _path = File.separator;
						//
						for(int i=1;i<arr.length;i++){
							_path += arr[i].trim() + File.separator;
						}					
						//
						_path = _path.substring(0,_path.length()-1);
						//
						newPath = sourcePath + _path;	         			
	         			//	         			
	         			if(newPath != null || newPath.length() > 0){	         				
			       			DicomImage di = new DicomImage(newPath,errorLogger);
	         			}	         			
	         		}         			
          		}
     		}
 		});
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		btnTEST.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				//bNames = true;
				//
				//getNewUID();
				//JOptionPane.showMessageDialog(null, "There are " + dicomSourceFiles.size() + " files in the arraylist");
			}
		});			
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
		this.add(pnlTop,BorderLayout.NORTH);
		this.add(pnlSth,BorderLayout.SOUTH);
		this.add(pnlCenter,BorderLayout.CENTER);
	}
	//
	private String getNewUID(String oldID){
		UIDGenerator uidGen = new UIDGenerator(oldID);
		String _newID = "";
		//
		try {
			uidGen.newStamp();
			//
			_newID = uidGen.getNewUID();	
		} catch (DicomException e) {				
			errorLogger.log(Level.INFO,e.getMessage());
		}
		//
		return _newID;
	}
	//
	private void processSingleImage(){
		File f = new File(sourcePath);
		//
		if(isFileDicomImage(sourcePath)){
			if(anonymizeImage(sourcePath, destPath + File.separator + f.getName()) == true){
				lblCurrentFile.setText("Processing " + sourcePath + ", please wait ...");
			}
			//
			if(chkExtractImage.isSelected()){
				extractImage(destPath + File.separator + f.getName());
			}
			//
			progressIndicator++;
			//
			lblCurrentFile.setText("Done. " + totalNumFiles + " DICOM file anonymized");
		}  
		//
		f = null;
		//
		enableGUI();
	}
	//
	private void copyTree(){
		try{
	    	handler = new FileHandler("DicomErrors.log");
	    	handler.setFormatter(new SimpleFormatter());
	    	//
	    	errorLogger.addHandler(handler);
	    	//			
 			File fSource = new File(sourcePath);
 			File fDest = new File(destPath);
			//
			lblCurrentFile.setText("Processing, please wait ...");
			//
        	copyFolder(fSource, fDest);
        	//cancel the thread once the anonymization has finished
        	//anonymizeWorker.cancel(true);
        }
        catch(IOException e){
        	errorLogger.log(Level.SEVERE, e.getMessage());
        }
	}
	//
    private void copyFolder(File src, File dest) throws IOException{
    	String _destFullName = "";
    	String _sourceFullName = "";
    	//
    	File srcFile;
		File destFile;
		//
    	if(src.isDirectory()){
    		//if directory not exists, create it
    		if(!dest.exists()){
    		   dest.mkdir();
    		}
    		//list all the directory contents
    		String files[] = src.list();
    		//
    		for (String file : files) {
    			//construct the src and dest file structure
    			srcFile = new File(src, file);
    			destFile = new File(dest, file);
    			//recursive copy
    			copyFolder(srcFile,destFile);
    			//
    			progress.setMaximum(totalNumFiles);
    			progress.setValue(progressIndicator);
    			progress.setStringPainted(true);  
    			//
    			_sourceFullName = srcFile.getParent() + File.separator + srcFile.getName();
    			_destFullName = destPath + File.separator + file;
    			//_destFullName = destPath + File.separator + srcFile.getParentFile().getName() + File.separator + file;
    			//
    			if(isFileDicomImage(_sourceFullName)){
    				System.out.println("src: " + _sourceFullName);
    				System.out.println("dest: " + _destFullName);
    				
    				if(anonymizeImage(_sourceFullName, _destFullName) == true){
    					lblCurrentFile.setText("Processing " + _sourceFullName + ", please wait ...");
    				}    				
    				//
    				if(chkExtractImage.isSelected()){
    					extractImage(_destFullName);
    				}
    				
    				//
    				progressIndicator++;
    			}    			     			   	
    		}
     	}
    }	
	//
	private boolean isFileDicomImage(String file){
		boolean _isDicom;
		//
		_isDicom = DicomFileUtilities.isDicomOrAcrNemaFile(file);
		//
		if(!_isDicom){
			errorLogger.log(Level.INFO, "Skipping " + file + ", not a DICOM file");
		}
		//
		return _isDicom;
	}
	//display a single file
	private void displaySingleSelection(DefaultMutableTreeNode parent, String filename){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(filename);
		//
		parent.add(node);
		//
		totalNumFiles++;
		totalNumFolders++;
		//
		tree.expandRow(0);
	}
	//display folder and sub-folders
    private void listAllFiles(String directory, DefaultMutableTreeNode parent, Boolean recursive) {
        File [] children = new File(directory).listFiles(); // list all the files in the directory
        //
        lblCurrentFile.setText("Reading files, please wait ...");
        progress.setMaximum(children.length);
        progress.setStringPainted(true);
        //
        for (int i = 0; i < children.length; i++) { // loop through each
        	DefaultMutableTreeNode node = new DefaultMutableTreeNode(children[i].getName());
        	// only display the node if it isn't a folder, and if this is a recursive call
        	if (children[i].isDirectory() && recursive) {
        		parent.add(node); // add as a child node
        		totalNumFolders++;
        		listAllFiles(children[i].getPath(), node, recursive); // call again for the subdirectory
        	} 
        	else if (!children[i].isDirectory()){ // otherwise, if it isn't a directory
        		if(isFileDicomImage(directory + File.separator + children[i].getName()) == true){
        			parent.add(node); // add it as a node and do nothing else
        			progress.setValue(i);
        			progress.setStringPainted(true);
        			//keep a list of source files and paths, thsi will be passed to the search application
        			dicomSourceFiles.add(directory + File.separator + children[i].getName());
        			//
        			totalNumFiles++;
        		}
        	}
        }
        //
        //treeWorker.cancel(true);
    }
    //
	private void extractImage(String file){
		String _outputFile = file.replace(".dcm", ".jpeg");
		//
		try{
			ConsumerFormatImageMaker.convertFileToEightBitImage(file, _outputFile,"jpeg",0);
		}
		catch(Exception ex){
			ex.printStackTrace();
			errorLogger.log(Level.SEVERE, ex.getMessage());
		}
	}
	//
    private boolean anonymizeImage(String srcFile, String destFile){
    	boolean _anonymized = true;	//assume no error
    	DicomInputStream dis = null;
    	//
    	try{
    		dis = new DicomInputStream(new File(srcFile));
    		//get the attribute list from the Dicom Image
    		AttributeList attrList = new AttributeList();
    		//
    		attrList.read(dis);
    		//
    		//IMPORTANT !!!!!!!!!!!!! THIS IS LIVE, REMOVE COMMENT BLOCK
    		Attribute attrib = attrList.get(TagFromName.PatientID);
    		Attribute SOPattrib = attrList.get(TagFromName.SOPInstanceUID);
    		//
    		String _pID = attrib.getDelimitedStringValuesOrNull();
    		String _SOPid = SOPattrib.getDelimitedStringValuesOrNull();
    		String _newID = encryptPatientID(_pID);
    		//
    		//set up the rest of the values needed for the call to removeOrNullIdentifyingAttributes
    		boolean _keepPatientCharacteristics = chkPatient.isSelected();
    		boolean _keepDeviceIdentity =  chkModalities.isSelected();
    		//
    		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(attrList,HandleUIDs.remap,true,true,_keepPatientCharacteristics,_keepDeviceIdentity,true);
    		//
    		Attribute newAttrib = new PersonNameAttribute(TagFromName.PatientID);
    		newAttrib.addValue(_newID);
    		attrList.put(newAttrib);
    		//
    		if(chkMapUIDs.isSelected()){
    			String _SOP = getNewUID(_SOPid);
    			//
    			System.out.println("old SOP: " + _SOPid);
    			System.out.println("new SOP: " + _SOP);
        		//Attribute newSOP = new PersonNameAttribute(TagFromName.SOPInstanceUID);
        		//newAttrib.addValue(_SOP);
        		//attrList.put(newSOP);    			
    		}
    		//
    		attrList.write(destFile);
    		//
    		dis.close();
    	}
    	catch(IOException ioe){
    		_anonymized=false;
    		ioe.printStackTrace();
    		errorLogger.log(Level.SEVERE, "IOException: " + srcFile + ":: " + ioe.getMessage());
    	}
    	catch(DicomException de){
    		de.printStackTrace();
    		_anonymized=false;
    		errorLogger.log(Level.SEVERE, "DicomException: " + srcFile + ":: " + de.getMessage());
    	}
    	finally{
    		try{
    			dis.close();
    		}
    		catch(IOException e){
    			e.printStackTrace();
    			errorLogger.log(Level.SEVERE, e.getMessage());
    		}
    	}
    	//
    	return _anonymized;
    }	
    //
	private String encryptPatientID(String patientID) {
		StringBuffer sb = null;
		byte[] array =null;
		//
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			array = md.digest(patientID.getBytes());
			//
			sb = new StringBuffer();
			//
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
		} 
		catch (NoSuchAlgorithmException e) {
			errorLogger.log(Level.WARNING, e.getMessage());
		}
		//
		return sb.toString();
	}	    
    //
}


