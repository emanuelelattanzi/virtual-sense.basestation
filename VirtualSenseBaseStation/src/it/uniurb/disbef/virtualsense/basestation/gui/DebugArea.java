package it.uniurb.disbef.virtualsense.basestation.gui;

import javax.swing.JTextArea;

public class DebugArea extends JTextArea{
	public void println(String s){
		this.append(s+"\n");
		this.setCaretPosition(this.getText().length()-1);
	}
}
