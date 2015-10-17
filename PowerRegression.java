//**Future Cropland Forecasting Model**//
//**Developed July 1st 2013 by Nicholas Haney for Dr. Sagy Cohen**//
//**This is the power regression class that calculates the distribution and total area of cropland at a future date**//

import java.io.*;
import java.util.Scanner;

public class PowerRegression {
	double pop60, pop70, pop80, pop90, pop00, pop05;
	double crop60, crop70, crop80, crop90, crop00, crop05;
	short numYears = 6;
	short count;
	
	public PowerRegression() { //Constructor
		try { //Reads in the population data
			Scanner infile = new Scanner(new File("populationData.txt"));
			pop60 = Math.log(infile.nextDouble());
			pop70 = Math.log(infile.nextDouble());
			pop80 = Math.log(infile.nextDouble());
			pop90 = Math.log(infile.nextDouble());
			pop00 = Math.log(infile.nextDouble());
			pop05 = Math.log(infile.nextDouble());
		}
		catch(java.io.FileNotFoundException e) {
			System.out.println(e);
		}
	}
	
	public double powerRegression (double cp60, double cp70, double cp80, double cp90, double cp00, double cp05, double tStep) {
		crop60 = 0;
		crop70 = 0;
		crop80 = 0;
		crop90 = 0;
		crop00 = 0;
		crop05 = 0;
		count = 0;
		if(cp60 == 0) {crop60 = 0; count++;} //This sequence of if statements checks to see if one of the inputs is equal to zero as this will result in an output of NaN
		else crop60 = Math.log(cp60);
		if(cp70 == 0) {cp70 = 5; count++;}
		else crop70 = Math.log(cp70);
		if(cp80 == 0) {cp80 = 5; count++;}
		else crop80 = Math.log(cp80);
		if(cp90 == 0) {cp90 = 5; count++;}
		else crop90 = Math.log(cp90);
		if(cp00 == 0) {cp00 = 5; count++;}
		else crop00 = Math.log(cp00);
		if(cp05 == 0) {cp05 = 5; count++;}
		else crop05 = Math.log(cp05);
		if(count == 6) return 0;
		
		double timeStep = tStep;
		double value;
		double sumXY = 0;
		double sumX = 0;
		double sumY = 0;
		double sumX2 = 0;
		double slope = 0; 
		double intercept = 0;
		double part1 = 0;
		double part2 = 0;
		
		sumXY += pop60 * crop60 + pop70*crop70 + pop80*crop80 + pop90*crop90 + pop00*crop00+ pop05*crop05;
		sumX += pop60 + pop70 + pop80 + pop90 + pop00 + pop05;
		sumY += crop60 + crop70 + crop80 + crop90 + crop00 + crop05;
		sumX2 += Math.pow(pop60, 2) + Math.pow(pop70, 2) + Math.pow(pop80, 2) + Math.pow(pop90, 2) + Math.pow(pop00, 2) + Math.pow(pop05, 2);
		slope = ((numYears * sumXY) - (sumX * sumY))/((numYears * sumX2) - Math.pow(sumX, 2));
		intercept = Math.exp((sumY - (slope * sumX)) / numYears);
		value = intercept * Math.pow(timeStep, slope);
			
		return value;
	}
}
