//**Future Cropland Forecasting Model**//
//**Developed July 1st 2013 by Nicholas Haney for Dr. Sagy Cohen**//
//**This is the exponential regression class that calculates the distribution and total area of grassland at a future date**//

import java.io.*;
import java.util.Scanner;

public class ExpoRegres {
	double pop60, pop70, pop80, pop90, pop00, pop05;
	short numYears = 6;
	
	public ExpoRegres() { //Constructor
		try { //Reads in the population data
			Scanner infile = new Scanner(new File("populationData.txt"));
			pop60 = infile.nextDouble();
			pop70 = infile.nextDouble();
			pop80 = infile.nextDouble();
			pop90 = infile.nextDouble();
			pop00 = infile.nextDouble();
			pop05 = infile.nextDouble();
		}
		catch(java.io.FileNotFoundException e) {
			System.out.println(e);
		}
	}
	
	public double expoRegression (double gras60, double gras70, double gras80, double gras90, double gras00, double gras05, double timeStep) {
		short count = 0;
		short numYears = 6;
		if(gras60 == 0) {gras60 = 1; count++;} //This sequence of if statements checks to see if one of the inputs is equal to zero as this will result in an output of NaN
		if(gras70 == 0) {gras70 = 1; count++;}
		if(gras80 == 0) {gras80 = 1; count++;}
		if(gras90 == 0) {gras90 = 1; count++;}
		if(gras00 == 0) {gras00 = 1; count++;}
		if(gras05 == 0) {gras05 = 1; count++;}
		if(count == 6) return 0;
		else {
			double value = 0;
			double sumY = 0;
			double sumXY = 0;
			double sumX2Y = 0;
			double sumYLnY = 0;
			double sumXYLnY = 0;
			double intercept = 0;
			double slope = 0;
		
			sumY = gras60 + gras70 + gras80 + gras90 + gras00 + gras05;
			sumXY = pop60 * gras60 + pop70 * gras70 + pop80 * gras80 + gras90 * pop90 + gras00 * pop00 + gras05 * pop05;
			sumX2Y = Math.pow(pop60,2 )* gras60 + Math.pow(pop70,2) * gras70 + Math.pow(pop80,2) * gras80 + Math.pow(pop90,2) * gras90 + Math.pow(pop00,2) * gras00 + Math.pow(pop05,2) * gras05;
			sumYLnY = gras60 * Math.log(gras60) + gras70 * Math.log(gras70) + gras80 * Math.log(gras80) + gras90 * Math.log(gras90) + gras00 * Math.log(gras00) + gras05 * Math.log(gras05);
			sumXYLnY = pop60*gras60*Math.log(gras60) + pop70*gras70*Math.log(gras70) + pop80*gras80*Math.log(gras80) + pop90*gras90*Math.log(gras90) + pop00*gras00*Math.log(gras00) + pop05*gras05*Math.log(gras05);
		
			intercept = Math.exp(((sumX2Y * sumYLnY) - (sumXY * sumXYLnY)) / ((sumY * sumX2Y) - Math.pow(sumXY, 2)));
			slope = ((sumY * sumXYLnY) - (sumXY * sumYLnY))/((sumY * sumX2Y) - Math.pow(sumXY, 2));
			value = intercept * Math.exp(slope*timeStep);
		
			return value;
		}
	}
}