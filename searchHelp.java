import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

public class searchHelp extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	//
	private final int WIDTH = 600;
	private final int HEIGHT = 400;
	private JEditorPane editorpane;
	private URL helpURL;
	//
	public searchHelp(String title, URL hlpURL) {
		super(title);
		helpURL = hlpURL; 
		editorpane = new JEditorPane();
		editorpane.setEditable(false);
		editorpane.setBackground(Color.LIGHT_GRAY);
		//
		try {
			editorpane.setPage(helpURL);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//anonymous inner listener
		editorpane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent ev) {
				try {
					if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						editorpane.setPage(ev.getURL());
					}
				} catch (IOException ex) {
					//	put message in window
					ex.printStackTrace();
				}
			}
		});
		getContentPane().add(new JScrollPane(editorpane));
		addButtons();
		// no need for listener just dispose
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// dynamically set location
		calculateLocation();
		this.setBackground(Color.LIGHT_GRAY);
		setVisible(true);
		// end constructor
	}
	//
	public void actionPerformed(ActionEvent e) {
		String strAction = e.getActionCommand();
		//
		if (strAction == "Close") {
			// more portable if delegated
			processWindowEvent(new WindowEvent(this,WindowEvent.WINDOW_CLOSING));
		}
	}
	//
	private void addButtons() {
		//JButton btncontents = new JButton("Contents");
		//btncontents.addActionListener(this);
		JButton btnclose = new JButton("Close");
		btnclose.addActionListener(this);
		//put into JPanel
		JPanel panebuttons = new JPanel();
		//panebuttons.add(btncontents);
		panebuttons.add(btnclose);
		panebuttons.setBackground(Color.LIGHT_GRAY);
		//	add panel south
		getContentPane().add(panebuttons, BorderLayout.SOUTH);
	}
	//
	private void calculateLocation() {
		Dimension screendim = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(new Dimension(WIDTH, HEIGHT));
		int locationx = (screendim.width - WIDTH) / 2;
		int locationy = (screendim.height - HEIGHT) / 2;
		setLocation(locationx, locationy);
	}
	//
	/*
	public static void main(String [] args){  
		URL index = ClassLoader.getSystemResource("index.html");
		new searchHelp("Test", index);
	}
	*/
}

