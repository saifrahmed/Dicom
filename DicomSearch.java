import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import com.pixelmed.dicom.*;

@SuppressWarnings("serial")
public class DicomSearch extends JFrame{
	private JCheckBox chkPatient = new JCheckBox("Patient Name");
	private JTextField txtFname = new JTextField(15);
	private JTextField txtLname = new JTextField(15);
	private JCheckBox chkBodyPart = new JCheckBox("Body Part Examined");
	private JTextField txtBodyPart = new JTextField(15);
	private JCheckBox chkAcqDate = new JCheckBox("Acquisition Date (dd/mm/yyyy)");
	private JTextField txtAcqDateFrom = new JTextField(15);		
	private JTextField txtAcqDateTo = new JTextField(15);
	private JButton btnSearch = new JButton("Search");
	private JButton btnClose = new JButton("Close");
	private JButton btnHelp = new JButton("Help");
	private JTable tblResults;
	private ArrayList<String> arrDicomFiles = new ArrayList<String>();
	//
	public DicomSearch(ArrayList<String> dicomFiles){
		arrDicomFiles = dicomFiles;
		//
		buildGUI();
		//
		assignActions();
	}
	//
	private void buildGUI(){
		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
		//
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//
		this.setSize((w/8)*4,h/2);
		//centre the window on the screen
		this.setLocationRelativeTo(null);			
		//
		addControls();
		//
		disableSearch();
		//
		this.setTitle("Dicom Search");
		//make sure the window can't be resized
		this.setResizable(false);
		//		
		this.setVisible(true);			
	}
	//
	private void disableSearch(){
		txtFname.setEnabled(false);
		txtLname.setEnabled(false);
		txtBodyPart.setEnabled(false);
		txtAcqDateFrom.setEnabled(false);
		txtAcqDateTo.setEnabled(false);
	}
	//
	private void assignActions(){
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				//close this window, keeping the main window open
				dispose();
			}
		});	
		//
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				String _msg = checkSearchFields();
				//
				if(!_msg.equals("ok")){
					JOptionPane.showMessageDialog(null,_msg);
				}
				else{
					getSearchCriteria();
				}				
			}
		});			
		//
		chkPatient.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if(chkPatient.isSelected()){
					txtFname.setEnabled(true);
					txtLname.setEnabled(true);
				}
				else{
					txtFname.setText("");
					txtLname.setText("");
					txtFname.setEnabled(false);
					txtLname.setEnabled(false);
				}
			}
		});
		//
		chkBodyPart.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if(chkBodyPart.isSelected()){
					txtBodyPart.setEnabled(true);
				}
				else{
					txtBodyPart.setText("");
					txtBodyPart.setEnabled(false);
				}
			}
		});		
		//
		chkAcqDate.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if(chkAcqDate.isSelected()){
					txtAcqDateFrom.setEnabled(true);
					txtAcqDateTo.setEnabled(true);
				}
				else{
					txtAcqDateFrom.setText("");
					txtAcqDateFrom.setEnabled(false);
					//
					txtAcqDateTo.setText("");
					txtAcqDateTo.setEnabled(false);					
				}
			}
		});		
		//
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				displayHelp();
				//JOptionPane.showMessageDialog(null, "There are " + arrDicomFiles.size() + " files in the arraylist");
			}
		});
	}
	//
	private void getSearchCriteria(){
		boolean _searchByName = false;
		boolean _searchByDate = false;
		String _fname = "", _lname = "";
		String _dateFrom = "", _dateTo = "";
		//
		if(chkPatient.isSelected()){
			_searchByName = true;
			_fname = txtFname.getText();
			_lname = txtLname.getText();
		}
		//
		if(chkAcqDate.isSelected()){
			_searchByDate = true;
			_dateFrom = txtAcqDateFrom.getText();
			_dateTo = txtAcqDateTo.getText();
		}
		//
		startSearch(_searchByName, _searchByDate, _fname, _lname, _dateFrom, _dateTo);
	}
	//
	private void startSearch(boolean byName, boolean byDate, String fname,String lname,String dateFrom, String dateTo){
		//StringBuilder sbName = new StringBuilder();
		DicomInputStream dis = null;
		String sName = "";
		String arrName[];
		//
		for(int i=0;i<arrDicomFiles.size();i++){
			if(byName == true){
		    	try{
		    		dis = new DicomInputStream(new File(arrDicomFiles.get(i)));
		    		//
		    		AttributeList attrList = new AttributeList();
		    		//
		    		attrList.read(dis);		    		
		    		//
		    		Attribute attrib = attrList.get(TagFromName.PatientName);
		    		//
		    		sName = attrib.getDelimitedStringValuesOrNull();
		    		arrName = sName.split("^");
		    		//
		    		
		    		//
		    		dis.close();
		    	}
		    	catch(DicomException de){
		    		de.printStackTrace();
		    	}		    	
		    	catch(IOException ioe){
		    		ioe.printStackTrace();
		    	}	
		    	finally{
		    		try{
		    			dis.close();
		    		}
		    		catch(IOException e){
		    			e.printStackTrace();
		    		}
		    	}		    	
			}
		}
	}
	//
	private void displayHelp(){
		//searchHelp
		URL index = ClassLoader.getSystemResource("searchHelp.html");
		//
		searchHelp sh = new searchHelp("DICOM", index);
	}
	//
	private String checkSearchFields(){
		String _msg = "ok";
		//
		if(chkPatient.isSelected() && txtFname.getText().length() ==0 && txtLname.getText().length() ==0){
			_msg = "Please enter search critera for Patient name\n";
		}
		else if(chkBodyPart.isSelected() && txtBodyPart.getText().length() ==0){
			_msg = "Please enter search critera for Body Part\n";
		}
		else if(chkAcqDate.isSelected() && txtAcqDateFrom.getText().length() ==0 && txtAcqDateTo.getText().length() ==0){
			_msg = "Please enter search critera for Acquisition Date\n";
		}
		else if(txtFname.getText().length()==0 && txtLname.getText().length() ==0 && 
				txtBodyPart.getText().length()==0 && txtAcqDateFrom.getText().length()==0 && txtAcqDateTo.getText().length() ==0){
			_msg = "No search criteria entered";
		}
		else if(chkAcqDate.isSelected() && (!isLegalDate(txtAcqDateFrom.getText()) && !isLegalDate(txtAcqDateTo.getText()))){ 
			_msg = "Acquisition Date(s) are in wrong format or not valid date(s)"; 
		}
		else if(chkAcqDate.isSelected() && !isLegalDate(txtAcqDateFrom.getText())){ 
			_msg = "Acquisition FROM Date is in wrong format or not a valid date"; 
		}
		else if(chkAcqDate.isSelected() && !isLegalDate(txtAcqDateTo.getText())){ 
			_msg = "Acquisition TO Date is in wrong format or not a valid date"; 
		}		
		//	
		return _msg;
	}
	//
	private void addControls(){
		JPanel pnlTop = new JPanel();
		pnlTop.setBorder(BorderFactory.createTitledBorder("Search Options"));
		pnlTop.setLayout(new BoxLayout(pnlTop,BoxLayout.PAGE_AXIS));
		//
		JPanel ep1 = new JPanel();
		JLabel el1 = new JLabel("   ");
		ep1.add(el1);
		//
		JPanel ep2 = new JPanel();
		JLabel el2 = new JLabel("   ");
		ep2.add(el2);
		//
		JPanel ep3 = new JPanel();
		JLabel el3 = new JLabel("   ");
		ep3.add(el3);				
		//patient name panel
		JPanel p1 = new JPanel();		
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		p1.add(chkPatient);
		JLabel lblPatientFiller = new JLabel("          ");
		p1.add(lblPatientFiller);		
		JLabel lblFname = new JLabel("First Name");
		p1.add(lblFname);
		p1.add(txtFname);
		JLabel lblLname = new JLabel("Last Name");
		p1.add(lblLname);
		p1.add(txtLname);
		//bodypart panel
		JPanel p2 = new JPanel();		
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(chkBodyPart);
		JLabel lblFiller1 = new JLabel("  ");
		p2.add(lblFiller1);
		p2.add(txtBodyPart);
		//acquisition date panel
		JPanel p3 = new JPanel();		
		p3.setLayout(new FlowLayout(FlowLayout.LEFT));
		p3.add(chkAcqDate);
		JLabel lblDateFrom = new JLabel("From");
		p3.add(lblDateFrom);
		p3.add(txtAcqDateFrom);
		JLabel lblDateTo = new JLabel("To");
		p3.add(lblDateTo);		
		p3.add(txtAcqDateTo);
		//panel for buttons
		JPanel p4 = new JPanel();		
		p4.setLayout(new FlowLayout(FlowLayout.LEFT));
		p4.add(btnSearch);
		JLabel lblFiller3 = new JLabel("  ");
		p4.add(lblFiller3);
		p4.add(btnClose);
		JLabel lblFiller4 = new JLabel("  ");
		p4.add(lblFiller4);
		p4.add(btnHelp);		
		//
		pnlTop.add(p1);
		pnlTop.add(ep1);
		pnlTop.add(p2);
		pnlTop.add(ep2);
		pnlTop.add(p3);
		pnlTop.add(ep3);
		pnlTop.add(p4);
		//
		this.add(pnlTop, BorderLayout.NORTH);
		//
		//setupResultsTable();
	}
	//
	/*
	private void setupResultsTable(){
		//
		String[] titles = new String[] {"Patient Name","Body Part Examined","Date"};
		String data[][] = new String [][]
		{
			{ "12", "234", "68887" },
			{ "-123", "43", "853" },
			{ "93", "89.2", "109" },
			{ "279", "9033", "3092" }
		};		
		//
		JPanel pnlTable = new JPanel();
		pnlTable.setBorder(BorderFactory.createTitledBorder("Search Results"));
		pnlTable.setLayout(new GridLayout(1,1));
		//	
		DefaultTableModel model = new DefaultTableModel(data,titles){
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		//
		tblResults = new JTable();
		tblResults.setModel(model);		
		//
		JTableHeader header = tblResults.getTableHeader();
		header.setBackground(Color.yellow);
		//
		JScrollPane pane = new JScrollPane(tblResults);
	  	//		
		pnlTable.add(pane);
		//
		this.add(pnlTable, BorderLayout.CENTER);
	}
	*/
	//
	private boolean isLegalDate(String s) {
		boolean _isValid = true;	//assume correct date format
	    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	    //
	    Date testDate = null;
	 	//
	    //try to parse the string into date form
	    try
	    {
	      testDate = sdf.parse(s);
	      //
	      if (!sdf.format(testDate).equals(s))
		  {
	    	  _isValid = false;
		  }	      
	    }
	 	//
	    catch (Exception e)
	    {
	    	_isValid = false;
	    }
		//
	    return _isValid;
	}	
}
