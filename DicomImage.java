import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pixelmed.dicom.*;
import com.pixelmed.display.SingleImagePanel;
import com.pixelmed.display.SourceImage;

public class DicomImage {
	private JFrame frame = new JFrame();
	private JButton btnClose = new JButton("Close");
	private JLabel imgLabel = new JLabel();
	private JPanel centerPanel = new JPanel();
	private JTree attrTree = new JTree();
	//
	// default constructor
	public DicomImage(){
	}
	//
	public DicomImage(String img, Logger erl){
		viewImage(img, erl);
	}
	//
	/**
	 * Creates a new JFrame to display the selected DICOM image
	 * using the pixelmed library.  
	 * Also displays the AttributeList in a JTree
	 * 
	 * @param img	The filename of the DICOM image to display
	 * @param erl	Logger object to log any errors thrown during this operation
	 */
	private void viewImage(String img, Logger erl){
		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
		//
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		//make the window is half the width and half the height of the screen
		frame.setSize((w/3)*2,h/2);
		//centre the window on the screen
		frame.setLocationRelativeTo(null);			
		//
		frame.setTitle("View DICOM Image Details");
		//make sure the window can't be resized
		frame.setResizable(false);
		//
		addControls(img);
		//
		displayImage(img, erl);
		//
		frame.setVisible(true);
	}
	//
	private void displayImage(String filename, Logger erl){
    	DicomInputStream dis = null;
    	//
    	try{
    		dis = new DicomInputStream(new File(filename));
    		//
    		AttributeList attrList = new AttributeList();
    		//get the attribute list from the Dicom Image
    		attrList.read(dis);
    		//no longer need this so can close it
    		dis.close();
    		//
    		//create an AttributeTree from the list
    		//this will be used to populate the JTree
    		//
    		TreeModel tm = new AttributeTree(attrList);
    		attrTree.setModel(tm);
    		JScrollPane pane = new JScrollPane(attrTree);		    	
			//use the pixelmed library to get the image from the file and display it in a panel
			SingleImagePanel ip = new SingleImagePanel(new SourceImage(filename));
			ip.removeMouseListener(ip);
			ip.removeMouseMotionListener(ip);
			//
			centerPanel.add(ip);
			centerPanel.add(pane);
			//
			imgLabel.repaint();
			//
			frame.update(frame.getGraphics());
    	}
    	catch(Exception e){
    		erl.log(Level.INFO, e.getMessage());
    	}
	}
	//
	private void addControls(String img){
		JPanel topPanel = new JPanel();		
		JPanel imgPanel = new JPanel();
		//
		JLabel sourcePath = new JLabel(img);
		topPanel.add(sourcePath);
		//
		imgPanel.add(imgLabel);
		//
		centerPanel.setLayout(new GridLayout(1,2));
		//
		JPanel pnlSouth = new JPanel();
		pnlSouth.setLayout(new FlowLayout(FlowLayout.LEFT));
		pnlSouth.add(btnClose);
		//		
		frame.add(topPanel,BorderLayout.NORTH);
		frame.add(centerPanel,BorderLayout.CENTER);
		frame.add(pnlSouth,BorderLayout.SOUTH);
		//
		assignActions();
	}
	//
	private void assignActions(){
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				//close this window, keeping the main window open
				frame.dispose();
			}
		});			
	}
}
