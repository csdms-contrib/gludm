//**Future Cropland Forecasting Model**//
//**Developed July 1st 2013 by Nicholas Haney for Dr. Sagy Cohen**//
//**This is the Main class that creates the GUI used to run the model**//

import java.io.*;
import java.util.Scanner;
import java.util.Calendar;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

public class ComboGUI extends JFrame {		
	private JComboBox yearSelect;
	private JComboBox equation;
	private JButton startButton = new JButton("Compute"); //This button initiates the calculations
	private JPanel panelOne = new JPanel();
	private JPanel panelTwo = new JPanel();
	private JRadioButton cropland, grazing, totalAgriculture, allFields; //These buttons allow the user to select what types of data to predict
	private JCheckBox checkBox; //Checking this box keeps the amount of global grazing land constant according to the 2005 values
	private JCheckBox batch;
	private ButtonGroup group = new ButtonGroup();
	private int popCount = 0;
	private int selection = 0;
	private int numList;
	private int checkOption = 0;
	private int batchOption = 0;
	private long[] popList = new long[140];
	private String[] yearListString = new String[140];
	private String[] equationType = new String[3];
	private Combine combo = new Combine();
	private Scanner infile;
	private String fileName = "popList.txt";     //This is the file that the future population data is read from
	private int newFileName = 0;
	private Border border = BorderFactory.createLineBorder(Color.black);
	private Border hoverBorder = BorderFactory.createLineBorder(Color.red);
	private int regressType;
	private int count;
	
//  This is the constructor that creates the GUI and initializes its variables	
	public ComboGUI() { //Constructor						
		readFile(fileName);   //This calls the function that reads in the future population data
		equations();
		yearSelect = new JComboBox(yearListString);  //This creates a drop down menu allowing the user to select a prediction year
		equation = new JComboBox(equationType);
		yearSelect.setBorder(border);
		startButton.setBorder(border);
		startButton.setPreferredSize (new Dimension (75, 25));
		equation.addActionListener (new EquationListener());
		yearSelect.addActionListener (new ComboListener()); //Adds a listener to the drop down menu
		startButton.addMouseListener (new ButtonListener()); //Adds a listener to the start button
		cropland = new JRadioButton("Cropland", true);
		grazing = new JRadioButton("Grazing");
		totalAgriculture = new JRadioButton("Total Ag");
		allFields = new JRadioButton("Select All");
		checkBox = new JCheckBox("Grazing Constant"); 
		batch = new JCheckBox("Batch Processing");
		checkBox.addItemListener (new CheckListener());
		batch.addItemListener (new BatchListener());
		
		group.add (cropland);
		group.add (grazing);
		group.add(totalAgriculture);
		group.add(allFields);
		
		RadioListener radioListener = new RadioListener();
		cropland.addActionListener(radioListener);
		grazing.addActionListener(radioListener);
		totalAgriculture.addActionListener(radioListener);
		allFields.addActionListener(radioListener);
		
		setLayout(new GridLayout(1, 1));
		add (panelOne);
		panelOne.add(equation);
		panelOne.add (yearSelect);
		panelOne.add (startButton);
		panelOne.add(batch);
		panelOne.setPreferredSize (new Dimension (150, 90));
		setBackground (Color.cyan);
		
		add (panelTwo);
		panelTwo.add (cropland);
		panelTwo.add (grazing);
		panelTwo.add (totalAgriculture);
		panelTwo.add (allFields);
		panelTwo.add (checkBox);
		panelTwo.setPreferredSize (new Dimension (175, 90));
	}
	
	public void readFile(String file) {  //This function loads the population data from a text file
		try {
			infile = new Scanner(new File(fileName));
			count = 0;
			while(infile.hasNext()) {
				yearListString[count]= Integer.toString(infile.nextInt());
				popList[count] = infile.nextLong();
				count++;
			}
		}
		catch(java.io.FileNotFoundException e) {
			System.out.println(e);
		}
	}
	
	private class CheckListener implements ItemListener { //The event listener for the checkbox
		public void itemStateChanged (ItemEvent event) {
			if(checkBox.isSelected()) checkOption = 1;
		}
	}
	
	private class BatchListener implements ItemListener {
		public void itemStateChanged (ItemEvent event) {
			if(batch.isSelected()) batchOption = 1;
		}
	}
	
	private class ComboListener implements ActionListener { //The event listener for the drop down menu
		public void actionPerformed(ActionEvent event) {
			JComboBox cb = (JComboBox)event.getSource();
			popCount = cb.getSelectedIndex();
			newFileName = cb.getSelectedIndex();
		}
	}
	
	private class EquationListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JComboBox cb = (JComboBox)event.getSource();
			regressType = cb.getSelectedIndex();
		}
	}
	
	private class RadioListener implements ActionListener { //The event listener to the radio listner
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			if (source == cropland) selection = 0;
			else if (source == grazing) selection = 1;
			else if (source == totalAgriculture) selection = 2;
			else selection = 3;
		}
	}
		
	private class ButtonListener implements MouseListener { //The mouse listener for the start button
		public void mouseClicked(MouseEvent event) {
			double startTime = System.currentTimeMillis(); //Records the internal clock time at the beginning of calculations, essential to compute calcuation time
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			int currentLoop = 0;
			int numLoops = Integer.parseInt(yearListString[newFileName]) - Integer.parseInt(yearListString[0]);
			if(batchOption == 1) {
				if(numLoops == 0) numLoops = 1;
				double temp = 0;
				while(currentLoop <= numLoops) {
					temp = (double)popList[currentLoop];
					combo.calculateCropland(temp, selection, yearListString[currentLoop], checkOption, regressType);
					currentLoop++;
				}
			}
			else {
				combo.calculateCropland(popList[popCount], selection, yearListString[newFileName], checkOption, regressType);
			}
			double endTime = System.currentTimeMillis();
			double totalTime = (endTime - startTime) / 60000.0; //Computes the total processing time
			//JOptionPane.showMessageDialog(null,"The Calculations Have Finished.\n Time Elapsed: " + totalTime + " minutes");
		}
		public void mousePressed(MouseEvent event){}
		public void mouseReleased(MouseEvent event){
		}
		public void mouseEntered(MouseEvent event){
			JButton button = (JButton)event.getSource();
			button.setBorder(hoverBorder);
		}
		public void mouseExited(MouseEvent event){
			JButton button = (JButton)event.getSource();
			button.setBorder(border);
		}
	}
	
	private void equations() {
		equationType[0] = "Linear Regression";
		equationType[1] = "Power Regression";
		equationType[2] = "Expo Regression";
	}
	
	public static void main(String[] args) { //Main class for the GUI
		ComboGUI window = new ComboGUI();
		window.setTitle("Cropland Forecasting Calculator");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
}