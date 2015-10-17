//**Future Cropland Forecasting Model**//
//**Developed July 1st 2013 by Nicholas Haney for Dr. Sagy Cohen**//
//**This is the power regression class that calculates the distribution and total area of cropland at a future date**//

import java.io.*;
import java.util.Scanner;

public class Regression {
		double pop1, pop2, pop3, pop4, pop5, pop6;
		short numYears = 6;
	
	public Regression() { //Constructor
		try { //Reads in the population data
			Scanner infile = new Scanner(new File("populationData.txt"));
			pop1 = infile.nextDouble();
			pop2 = infile.nextDouble();
			pop3 = infile.nextDouble();
			pop4 = infile.nextDouble();
			pop5 = infile.nextDouble();
			pop6 = infile.nextDouble();
		}
		catch(java.io.FileNotFoundException e) {
			System.out.println(e);
		}
	}
	
	public double regression (double cp1, double cp2, double cp3, double cp4, double cp5, double cp6, double tStep) {
		double sumX = 0;
		double sumY = 0;
		double sumXY = 0;
		double sumX2 = 0;
		double slope = 0;
		double intercept = 0;
		double count = 0;
		double value = 0;
		
		sumX = pop1 + pop2 + pop3 + pop4 + pop5 + pop6;
		sumY = cp1 + cp2 + cp3 + cp4 + cp5 + cp6;
		sumXY = pop1 * cp1 + pop2 * cp2 + pop3 * cp3 + pop4 * cp4 + pop5 * cp5 + pop6 * cp6;
		sumX2 = Math.pow(pop1, 2) + Math.pow(pop2, 2) + Math.pow(pop3, 2) + Math.pow(pop4, 2) + Math.pow(pop5, 2) + Math.pow(pop6, 2);
		slope = ((numYears * sumXY) - (sumX * sumY)) / ((numYears * sumX2) - (sumX * sumX));
		intercept = ((sumX2 * sumY) - (sumX * sumXY)) / ((numYears * sumX2) - (sumX * sumX));
		value = (slope * tStep) + intercept;
		
		return value;
	}
}