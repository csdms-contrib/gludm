//**Future Cropland Forecasting Model**//
//**Developed July 1st 2013 by Nicholas Haney for Dr. Sagy Cohen**//
//**This is the class that reads in the ascii values and calculates pixel values**//

import java.io.*;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Combine{
		private double file1, file2, file3, file4, file5, file6, file7, file8, file9, file10, file11, file12, combo;
		private int numIts, numCols, numRows, colNum, numFiles, increase, currentRow, currentCol, count, allocate, elevation, maxElevation, rowCount, checkOption, noData;
		private int cropNorth, cropSouth, grasNorth, grasSouth;
		private double difference, xCorner, yCorner, cellSize, maximum, maximumValue;
		private double squareKms = 111.12;
		private double[][] cropArray;		//Holds the pixel values for the crop file
		private double[][] grasArray;		//Holds the pixel values for the pasture file
		private double[][] totalLanduse;    //Holds the pixel values for the total land use file
		private double[][] elevationArray;  //Holds elevation values
		private boolean fixed, flagged;
		private double cropTotal;
		private double pastureTotal;
		private double totalAgriculture;
		private PowerRegression power = new PowerRegression();
		private Regression regress = new Regression();
		private ExpoRegres expo = new ExpoRegres();
		PrintWriter outfile;
		PrintWriter outfile2;
		PrintWriter outfile3;
		PrintWriter outfile4;
		String fileYear, userHomeFolder;
	
	public Combine() { //Constructor
		userHomeFolder = System.getProperty("user.home"); //Enables data to be written to the desktop, Windows specific command
		userHomeFolder = userHomeFolder + "/Desktop/Forecasts";
		boolean success = new File(userHomeFolder).mkdirs(); //Creates a file on the desktop that the program writes subsequent files to
		zeroValues();
	}
	
	public void calculateCropland (double population, int selection, String newFileName, int chk, int regressType) {
		zeroValues();
		fileYear = newFileName;
		checkOption = chk;
		Scanner infile1, infile2, infile3, infile4, infile5, infile6, infile7; 
		try {
			infile1 = new Scanner(new File("crop1960.asc"));
			infile2 = new Scanner(new File("crop1970.asc"));
			infile3 = new Scanner(new File("crop1980.asc"));
			infile4 = new Scanner(new File("crop1990.asc"));
			infile5 = new Scanner(new File("crop2000.asc"));
			infile6 = new Scanner(new File("crop2005.asc"));
			infile7 = new Scanner(new File("elevationUrban.asc"));
			infile1.next(); numCols = infile1.nextInt(); infile2.nextLine(); infile3.nextLine(); infile4.nextLine(); infile5.nextLine(); infile6.nextLine(); infile7.nextLine();
			infile1.next(); numRows = infile1.nextInt(); infile2.nextLine(); infile3.nextLine(); infile4.nextLine(); infile5.nextLine(); infile6.nextLine(); infile7.nextLine();
			infile1.next(); xCorner = infile1.nextDouble(); infile2.nextLine(); infile3.nextLine(); infile4.nextLine(); infile5.nextLine(); infile6.nextLine(); infile7.nextLine();
			infile1.next(); yCorner = infile1.nextDouble(); infile2.nextLine(); infile3.nextLine(); infile4.nextLine(); infile5.nextLine(); infile6.nextLine(); infile7.nextLine();
			infile1.next(); cellSize = infile1.nextDouble(); infile2.nextLine(); infile3.nextLine(); infile4.nextLine(); infile5.nextLine(); infile6.nextLine(); infile7.nextLine();
			infile1.next(); noData = infile1.nextInt(); infile2.nextLine(); infile3.nextLine(); infile4.nextLine(); infile5.nextLine(); infile6.nextLine(); infile7.nextLine();
			maximumValue = Math.pow((squareKms * cellSize),2);
			double temp = numRows * 0.1273148;
			cropNorth = (int) temp;
			temp = numRows * 0.8726851;
			cropSouth = (int) temp;
			for(int i = 0; i < numRows; i++) { //This routine reads in the values and calls the power regression class
				for(int j = 0; j < numCols; j++) {
					file1 = infile1.nextDouble();
					file2 = infile2.nextDouble();
					file3 = infile3.nextDouble();
					file4 = infile4.nextDouble();
					file5 = infile5.nextDouble();
					file6 = infile6.nextDouble();
					elevationArray[i][j] = infile7.nextDouble();
					if(file1 == noData) {   //-9999 represents water in the input files, if this is found no further processing is required
						cropArray[i][j] = noData;
					}
					else {
						if(regressType == 0) cropArray[i][j] = regress.regression(file1, file2, file3, file4, file5, file6, population); //calls the regression class
						else if(regressType == 1) cropArray[i][j] = power.powerRegression(file1, file2, file3, file4, file5, file6, population); //calls the power regression class
						else cropArray[i][j] = expo.expoRegression(file1, file2, file3, file4, file5, file6, population); //calls the exponential regression class
						cropTotal += cropArray[i][j]; //increments the total amount of cropland
					}
				}
			}
			flagged = false; //this means the current row has not exceeded the total number of rows
			for(int i = 0; i < numRows; i++) { //this routine checks each pixel value to ensure it is valid
				for(int j = 0; j < numCols; j++) {
					maximum = maximumValue * 0.95;
					if(elevationArray[i][j] >= maxElevation || elevationArray[i][j] == noData) { //checks to see if the pixel is at an elevation that is to high for crops or in a built up area
						maximum = 0; //setting the maximum value to zero triggers the cleanup routine
					}
					else if(i <= (cropNorth) || i >= (cropSouth)) { //Checks to see if the pixel is located in a latitude appropriate for agriculture 66.5 N and S
						maximum = 0;
					}	
					if(cropArray[i][j] > maximum) { //Checks to see if the regression equation has created a pixel value larger than the maximum
						cleanup(i, j);	//calls the cleanup function
					}
					if(cropArray[i][j] < 0 && cropArray[i][j] > noData) { //Checks to see if the regression equation has created a pixel value smaller than zero
						checkZeros(i, j);
					}
					totalLanduse[i][j] += cropArray[i][j];
				}
			}
		}
		catch(java.io.FileNotFoundException e) {
			System.out.println(e);
		}
		if (selection == 0) { //Checks to see which files were selected in the GUI
			printCropland();
			printTotals(selection);
		}
		else if (selection == 3) {
			printCropland();
			calculatePasture(population, selection, regressType);
		}
		else if (selection == 1 || selection == 2) {
			calculatePasture(population, selection, regressType);
		}
	}
	
	private void printCropland () { //This function prints the cropland data to a file
		String stringOutput;
		File textFile = new File(userHomeFolder, fileYear + "Cropland.asc"); //Creates the file name allowing the file to be written to the desktop
		try {
			outfile = new PrintWriter(textFile);
			outfile.println("ncols" + "\t" + numCols);
			outfile.println("nrows"+ "\t" + numRows);
			outfile.println("xllcorner" + "\t" + xCorner);
			outfile.println("yllcorner" + "\t" + yCorner);
			outfile.println("cellsize" + "\t" + cellSize);
			outfile.println("nodata_value" + "\t" + noData + "\n");
			for(int i = 0; i < numRows; i++) { //This routine prints the contents of the crop array to the desktop
				for(int j = 0; j < numCols; j++) {
					if(cropArray[i][j] == noData) {
						stringOutput = Integer.toString(noData);
						//stringOutput = stringOutput.substring(0,5);
						//stringOutput = "-9999";
					}
					else {
						stringOutput = Double.toString((cropArray[i][j]/maximumValue) * 100); //Turns the crop array value into a percentage
						//stringOutput = Double.toString(cropArray[i][j]);
					}
					outfile.print(stringOutput + "\t");
				}
				outfile.println("");
			}
			outfile.close();
		}
		catch(java.io.FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,"Cannot locate file:  " + textFile);
		}
	}
		
	private void calculatePasture (double population, int selection, int regressType) { //This function calculates the amount of grazing land
		try {
			Scanner infile7 = new Scanner(new File("gras1960.asc"));
			Scanner infile8 = new Scanner(new File("gras1970.asc"));
			Scanner infile9 = new Scanner(new File("gras1980.asc"));
			Scanner infile10 = new Scanner(new File("gras1990.asc"));
			Scanner infile11 = new Scanner(new File("gras2000.asc"));
			Scanner infile12 = new Scanner(new File("gras2005.asc"));
			infile7.next(); numCols = infile7.nextInt(); infile8.nextLine(); infile9.nextLine(); infile10.nextLine(); infile11.nextLine(); infile12.nextLine();
			infile7.next(); numRows = infile7.nextInt(); infile8.nextLine(); infile9.nextLine(); infile10.nextLine(); infile11.nextLine(); infile12.nextLine();
			infile7.next(); xCorner = infile7.nextDouble(); infile8.nextLine(); infile9.nextLine(); infile10.nextLine(); infile11.nextLine(); infile12.nextLine();
			infile7.next(); yCorner = infile7.nextDouble(); infile8.nextLine(); infile9.nextLine(); infile10.nextLine(); infile11.nextLine(); infile12.nextLine();
			infile7.next(); cellSize = infile7.nextDouble(); infile8.nextLine(); infile9.nextLine(); infile10.nextLine(); infile11.nextLine(); infile12.nextLine();
			infile7.next(); noData = infile7.nextInt(); infile8.nextLine(); infile9.nextLine(); infile10.nextLine(); infile11.nextLine(); infile12.nextLine();
			maximumValue = Math.pow((squareKms * cellSize), 2);
			double temp = numRows * 0.1157407;
			grasNorth = (int) temp;
			temp = numRows * 0.8842592;
			grasSouth = (int) temp;
			for(int i = 0; i < numRows; i++) {
				for(int j = 0; j < numCols; j++) {
				file7 = infile7.nextDouble();
				file8 = infile8.nextDouble();
				file9 = infile9.nextDouble();
				file10 = infile10.nextDouble();
				file11 = infile11.nextDouble();
				file12 = infile12.nextDouble();
					if(file7 == noData) {
						grasArray[i][j] = noData;
					}
					else {
						if(checkOption == 0) {
							if(regressType == 0) grasArray[i][j] = regress.regression(file7, file8, file9, file10, file11, file12, population); //calls the regression class
							else if(regressType == 1) grasArray[i][j] = power.powerRegression(file7, file8, file9, file10, file11, file12, population); //calls the power regression class
							else grasArray[i][j] = expo.expoRegression(file7, file8, file9, file10, file11, file12, population); //calls the exponential regression class
						}
						else {
							grasArray[i][j] = file11;
						}
						pastureTotal += grasArray[i][j];
					}
				}
			}
			for(int i = 0; i < numRows; i++) {
				for(int j = 0; j < numCols; j++) {
					maximum = maximumValue;
					if(elevationArray[i][j] >= 5800 || elevationArray[i][j] == noData) {
						maximum = 0;
					}
					else if(i <= grasNorth || i >= numRows * grasSouth) {
						maximum = 0;
					}
					if(grasArray[i][j] + cropArray[i][j] > maximum) {
						cleanupGrassland(i, j);
					}
						
					if(grasArray[i][j] < 0 && grasArray[i][j] > noData) {
						checkZerosGrassland(i, j);
					}
					totalLanduse[i][j] += grasArray[i][j];
				}
			}
			if (selection == 1) {
				printPasture();
				printTotals(selection);
			}
			else if (selection == 3) {
				printPasture();
				printTotal();
				printTotals(selection);
			}
			else {
				printTotal();
				printTotals(selection);
			}
		}
		catch(java.io.FileNotFoundException e) {
			System.out.println(e);
		}
	}
	private void printPasture () {
		maximum = maximumValue;
		String stringOutput;
		File textFile = new File(userHomeFolder, fileYear + "Grassland.asc");
		try {
			outfile2 = new PrintWriter(textFile);
			outfile2.println("ncols" + "\t" + numCols);
			outfile2.println("nrows"+ "\t" + numRows);
			outfile2.println("xllcorner" + "\t" + xCorner);
			outfile2.println("yllcorner" + "\t" + yCorner);
			outfile2.println("cellsize" + "\t" + cellSize);
			outfile2.println("nodata_value" + "\t" + noData + "\n");
				for(int i = 0; i < numRows; i++) {
					for(int j = 0; j < numCols; j++) {
						if(grasArray[i][j] == noData) {
							stringOutput = Integer.toString(noData);
						}
						else {
							stringOutput = Double.toString(grasArray[i][j]/maximum * 100);
						}
						outfile2.print(stringOutput + "\t");
					}
					outfile2.println("");
				}
				outfile2.close();
		}
		catch(java.io.FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,"Cannot locate file:  " + textFile);
		}
	}
	
	private void printTotal() {
		maximum = maximumValue;
		String stringOutput;
		File textFile = new File(userHomeFolder, fileYear + "TotalAgriculture.asc");
		try {
			outfile3 = new PrintWriter(textFile);
			outfile3.println("ncols" + "\t" + numCols);
			outfile3.println("nrows"+ "\t" + numRows);
			outfile3.println("xllcorner" + "\t" + xCorner);
			outfile3.println("yllcorner" + "\t" + yCorner);
			outfile3.println("cellsize" + "\t" + cellSize);
			outfile3.println("nodata_value" + "\t" + noData + "\n");
			for(int i = 0; i < numRows; i++) {
				for(int j = 0; j < numCols; j++) {
					if(grasArray[i][j] == noData) {
						stringOutput = Integer.toString(noData);
					}
					else {
						totalAgriculture += totalLanduse[i][j];
						stringOutput = Double.toString(totalLanduse[i][j]/maximum * 100);
					}
					outfile3.print(stringOutput + "\t");
				}
			outfile3.println("");
			}
		outfile3.close();
		}
		catch(java.io.FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,"Cannot location file:  " + textFile);
		}
	}
	
	public void printTotals (int selection) { //Writes the totals to a seperate file
		try {
			File textFile = new File(userHomeFolder, fileYear + "landUseTotals.txt");
			outfile4 = new PrintWriter(textFile);
			if(selection == 0 || selection == 3) {
				outfile4.println("Total Cropland (km^2):    " + cropTotal);
				outfile4.println("");
			}
			if(selection == 1 || selection == 3) {
				outfile4.println("Total Pasture (km^2):     " + pastureTotal);
				outfile4.println("");
			}
			if(selection == 2 || selection == 3) {
				outfile4.println("Total Agriculture (km^2): " + (cropTotal + pastureTotal));
			}
			outfile4.close();
		}
			catch(java.io.FileNotFoundException e) {
			System.out.println(e);
		}
	}
	
	public void cleanup(int r, int c) { //This routine reallocates overflow pixel values
		difference = (cropArray[r][c] - maximum) / 15; //Calculates the amount to be reallocated and divides them among 15 cells
		cropArray[r][c] = maximum;
		fixed = false;	//When the control variable for the while loop
		currentRow = r;
		currentCol = c+1;
		count = 0;
		maximum = (maximumValue * 0.95);
		while(fixed == false) {
			for(int x = 0; x < 3; x++) {
				for(int y = 0; y < 5; y++) {
					if(currentCol < numCols && currentCol >= 0) { //Checks to see if the current column  is out of bounds
						if(currentRow < cropSouth && currentRow >= cropNorth) { //Checks to see if the current row is between latitudes suitable for agriculture
							if(cropArray[currentRow][currentCol] != noData) { //Checks to see if the current pixel cannot contain agriculture
								if(flagged == false && elevationArray[currentRow][currentCol] != noData) {	//Checks to see that the current row exceeds the total number of rows
									cropArray[currentRow][currentCol] += difference;
									count++;
								}
								else {
									if((cropArray[currentRow][currentCol] + difference) <= (maximum) && elevationArray[currentRow][currentCol] <= maxElevation
									&& (cropArray[currentRow][currentCol] + difference) > 0 && elevationArray[currentRow][currentCol] != noData) {
										cropArray[currentRow][currentCol] += difference;
									}
									count++;
								}
								if(count == 15) { //If the difference amount has been distributed among 15 cells, then the problem has been "fixed"
									fixed = true;
								}
							}
						}
						else { //If the current row is out of bounds, then the current row is moved back up and is flagged
							double temp = numRows * 0.2;
							currentRow = (int) temp;
							flagged = true;
						}
					}
					else { //If the current column is out of bounds, then the current row is set to zero
						currentCol = 0;
						currentRow++;
					}
					currentCol++;
				}
				currentCol -= 5;
				currentRow++;
			}
		currentRow -= 2;
		currentCol += 5;
		}
	}
	
	public void cleanupGrassland (int r, int c) {
		difference = ((grasArray[r][c] + cropArray[r][c]) - maximum) / 15;
		grasArray[r][c] = maximum - cropArray[r][c];
		fixed = false;
		currentRow = r;
		currentCol = c+1;
		count = 0;
		maximum = maximumValue;
		while(fixed == false) {
			for(int x = 0; x < 3; x++) {
				for(int y = 0; y < 5; y++) {
					if(currentCol < numCols && currentCol >= 0) {
						if(currentRow < grasSouth && currentRow >= grasNorth) {
							if(grasArray[currentRow][currentCol] != noData) {
								if(flagged == false) {
									grasArray[currentRow][currentCol] += difference;
									count++;
								}
								else {
									if(((grasArray[currentRow][currentCol] + difference) + cropArray[currentRow][currentCol]) <= maximumValue 
									&& elevationArray[currentRow][currentCol] <= maxElevation && (grasArray[currentRow][currentCol] + difference) > 0) {
										grasArray[currentRow][currentCol] += difference;
									}
									count++;
								}
								if(count == 15) {
									fixed = true;
								}
							}
						}
						else {
							double temp = numRows * 0.2;
							currentRow = (int) temp;
							flagged = true;
						}
					}
					else {
						currentCol = 0;
						currentRow++;
					}
					currentCol++;
				}
				currentCol -= 5;
				currentRow++;
			}
			currentRow -= 2;
			currentCol += 5;
		}
	}
	
	public void checkZeros(int r, int c) { //This function checks the cropland array to make sure none of the values are less than zero
		currentCol = c;
		currentRow = r;
		difference = Math.abs(cropArray[r][c]); //Calculates the excess to be reallocated
		cropArray[r][c] = 0;
		while(fixed == false) {
			currentCol++;
			if(currentCol < numCols) {
				if(currentRow < numRows) {
					if(cropArray[currentRow][currentCol] >= difference && elevationArray[currentRow][currentCol] != noData) {
						cropArray[currentRow][currentCol] -= difference;
						fixed = true;
					}
				}
				else {
					double temp = (numRows * 0.2);
					currentRow = (int)temp;
				}
			}
			else {
				currentRow++;
				currentCol = 0;
			}
		}
	}
	public void checkZerosGrassland(int r, int c) {
		currentCol = c;
		currentRow = r;
		difference = Math.abs(grasArray[r][c]);
		grasArray[r][c] = 0;
		while(fixed == false) {
			currentCol++;
			if(currentCol < numCols) {
				if(currentRow < numRows) {
					if(grasArray[currentRow][currentCol] > 0) {
						grasArray[currentRow][currentCol] -= difference;
						fixed = true;
					}
				}
				else fixed = true;
			}
			else {
				currentRow++;
				currentCol = 0;
			}
		}
	}
	public void zeroValues() {
		numIts = 0;
		numCols = 4320;	
		numRows = 2160;
		colNum = 0;
		numFiles = 6;
		maximum = 0;
		increase = 1;
		currentRow = 0;
		currentCol = 0;
		count = 0;
		rowCount = 0;
		allocate = 0;
		cropTotal = 0;
		pastureTotal = 0;
		totalAgriculture = 0;
		maxElevation = 4000;
		cropArray = new double[numRows][numCols];
		grasArray = new double[numRows][numCols];
		totalLanduse = new double[numRows][numCols];
		elevationArray = new double[numRows][numCols];
		fixed = false;
			for(int i = 0; i < numRows; i++) {       //The following routine zeros out the arrays
			for(int j = 0; j < numCols; j++) {
				cropArray[i][j] = 0;
				grasArray[i][j] = 0;
				totalLanduse[i][j] = 0;
				elevationArray[i][j] = 0;
			}	
		}
	}
}
