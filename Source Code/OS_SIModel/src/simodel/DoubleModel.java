/*
 *   The Flexible Modelling Framework is a Social Science application for 
 *   synthesising individual level populations
 *   Copyright (C) 2013  Kirk Harland
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Contact email: k.harland@leeds.ac.uk
 */

package simodel;

import java.util.Arrays;
import java.util.Random;
import uk.ac.leeds.mass.optimisation.simulatedannealing.ISimulatedAnnealing;


public class DoubleModel implements ISimulatedAnnealing{ //, Runnable{
    
    private boolean optimised = false;
    
    private double[] beta;
    private int betaIndex = -1;
    private double originalBeta = 0.0;
    
    private double[] ai = null;
    private double[] bj = null;
    private double[] ai_original = null;
    private double[] bj_original = null;
    private double[][] originalResults = null;
    
    private double distance = 0.0;
    private double entropy = 0.0;
    private double originalDistance = 0.0;
    private double originalEntropy = 0.0;
    private double originalFitness = 0.0;
    
    private int origin = 0;
    private int destination = 0;
    
    private DataHandler data = DataHandler.getDataHandler();
    
    private Random random;
    
    private static boolean firstChange = true;
    private double changeValue = 0.01;
    
    
    public DoubleModel(Random random){
        this.random = random;
    }

    
    /**
     * Calculate the main spatial interaction model equation.
     * Cycles through the origins and destinations applying the 
     * spatial interaction model equation to each pair.  
     * 
     * @param beta a double value representing the parameter applied to the 
     * distance between the origin and destination this is an origin based parameter
     * representing the distance decay between origin and destination.
     */
    public boolean runModel(double[] beta){
        this.beta = beta;
        return runModel();
    }
    
    
    private boolean runModel(){
        
        ai = new double[data.getOrigin().length];
        for (int i = 0; i < ai.length; i++) {ai[i] = 1.0;}
        bj = new double[data.getDestination().length];
        for (int j = 0; j < bj.length; j++) {bj[j] = 1.0;}
        
        double dist = 0.0;
        
        if ( calculateAiBj() ){
            distance = 0.0;
            double[][] res = data.getResults();
            
            //calculate the distance for the system
            for (int i = 0; i < res.length; i++) {
                for (int j = 0; j < res[i].length; j++) {
                    dist = data.getDistance(i,j) == -1.0 ? 0.0 : data.getDistance(i,j);
                    distance += ( res[i][j] * dist );
                }
            }
            
            //calculate the entropy value
            entropy = (new uk.ac.leeds.mass.statistics.gof.Entropy()).test(res, res);
            
            return true;
        }else{
            return false;
        }
        
    }
    
    
    /*******************************************************************************************/
    /* Balance the Ai and Bj weights to make sure the model can converge on a solution that    */
    /* adds up correctly having the correct origin and destination totals                      */
    /*******************************************************************************************/
    private boolean calculateAiBj(){
        
        int maximumIterations = 5000;
        double threshold = 1.0;
        boolean convergence = false;
        double[] destTotals = new double[data.getDestination().length];
        double[] origTotals = new double[data.getOrigin().length];

        int mainCounter = 0;
        
        while ( !convergence && mainCounter < maximumIterations ){
            
        
            convergence = true;
            
            for (int i = 0; i < data.getOrigin().length; i++) {

                calculateAi(i);

            }
            
            for (int j = 0; j < data.getDestination().length; j++) {

                calculateBj(j);

            }

            mainCounter++;
            
            calculate();
            
            double[][] res = data.getResults();

            for (int i=0;i<origTotals.length; i++){origTotals[i] = 0.0;}
            for (int j=0;j<destTotals.length; j++){destTotals[j] = 0.0;}
            
            for (int i = 0; i < origTotals.length; i++) {
                for (int j = 0; j < destTotals.length; j++) {
                    destTotals[j]+=res[i][j];
                    origTotals[i]+=res[i][j];
                }
            }
            for (int i = 0; i < origTotals.length; i++) {
                if (Math.abs(origTotals[i] - data.getOrigin()[i]) > threshold){
                    convergence = false;
                    break;
                }
            }
            if (convergence){
                for (int i = 0; i < destTotals.length; i++) {
                    if (Math.abs(destTotals[i] - data.getDestination()[i]) > threshold){
                        convergence = false;
                        break;
                    }
                }
            }
            
            if (mainCounter%100 == 0){
                int a = 0;
                a++;
            }

        }

        if( convergence ){
            SpatialInteractionModel.getCurrent().report("Balancing complete iteration " + mainCounter);
            return true;
            
        }
        
        return false;
        
    }
    

    /*******************************************************************************************/
    /* Calculate the actual model equation once the Ai and Bj terms have been balanced.        */
    /*******************************************************************************************/
    private void calculate(){
        
        origin = 0;
        destination = 0;
        
        //cycle the origins using instance counter
        for(; origin < data.getOrigin().length; origin++){
                        
            //cycle the destinations  using instance counter
            for(; destination < data.getDestination().length; destination++){
            
                //get the distance value for this origin,destination pair
                double distance = data.getDistance(origin, destination);
                
                //test and make sure our distances are valid
                if (distance > -1.0){
                    
                    //calculate flow
                    data.setResults(
                            ai[origin]
                            * bj[destination]
                            * data.getOrigin()[origin]
                            * data.getDestination()[destination]
                            * Math.exp(distance * this.beta[0])
                            ,origin
                            ,destination
                            );
                    
                }else{
                    //tell the user about the discrepancy
                    data.setResults(0.0,origin,destination);
                }
                
            //end destination cycle
            }
            
            //reset destination
            destination = 0;
            
        //end origin cycle
        }
        
    }
    
    
    /*******************************************************************************************/
    /* Calculate the actual Ai term for an origin.                                             */
    /*******************************************************************************************/
    
    private void calculateAi(int orig){
            
        //create a variable to hold the sum of denominator values
        double denominator = 0.0;
        
        //cycle the destinations   
        for(int j = 0; j < data.getDestination().length; j++){

            double den = 0.0;
            
            if (data.getDistance(orig, j) != -1.0){
                //Sum all of the destination calculations for 
                //this origin into the denominator variable
                den += (
                        bj[j]
                        * data.getDestination()[j]
                        * Math.exp(data.getDistance(orig, j) * beta[0])
                        );
            }
            
            denominator += den;

        //end destination cycle
        }
        
        //finally calculate the Ai value
        ai[orig] = 0.0;
        
        //check to make sure denominator > 0 so no div by 0 error
        if ( denominator != 0.0 ){ai[orig] = 1/denominator;}
        
    }
    
    /*******************************************************************************************/
    /* Calculate the actual Bj term for a destination.                                         */
    /*******************************************************************************************/
    private void calculateBj(int dest){
        //create a variable to hold the sum of denominator values
        double denominator = 0.0;      

        //cycle the origins
        for(int i = 0; i < data.getOrigin().length; i++){


            double den = 0.0;
            
            if (data.getDistance(i, dest) != -1.0){
                //Sum all of the origin calculations for 
                //this destination into the denominator variable
                den += (
                        ai[i]
                        * data.getOrigin()[i]
                        * Math.exp(data.getDistance(i, dest) * beta[0])
                        );
            }
            
            denominator += den;
            
        //end origin cycle
        }
        
        //finally calculate the bj value
        bj[dest] = 0.0;
        //check to make sure denominator > 0 so no div by 0 error
        if ( denominator != 0.0 ){bj[dest] = 1/denominator;}
          
    }
    
    public double[] getAi(){
        return ai;
    }
    
    public double[] getBj(){
        return bj;
    }
    
    public double[] getBeta(){
        return beta;
    }
    
    public double getDistance(){
        return distance;
    }
    public double getOriginalDistance(){
        return originalDistance;
    }
    public double getDistanceDifferance(){
        return distance - originalDistance;
    }
    public double getEntropy(){
        return entropy;
    }

    @Override
    public int getSampleSize() {
        return beta.length;
    }

    @Override
    public double getCurrentFittness() {
        if (Math.abs(getDistance()-data.getObservedDistance()) < data.getObservedDistance())
            return (1-(Math.abs(getDistance()-data.getObservedDistance()) / data.getObservedDistance())) * getEntropy();
        else
            return data.getObservedDistance() - Math.abs(getDistance()-data.getObservedDistance());
    }

    @Override
    public double testChange(double fittnessToTest) {
        return originalFitness - fittnessToTest;
    }

    @Override
    public double suggestChange() {
        if (firstChange){
//            betaIndex = random.nextInt(beta.length);
            betaIndex = 0;

            originalBeta = beta[betaIndex];

            beta[betaIndex] = generateRandomBeta();
        }else{
            originalBeta = beta[betaIndex];
            beta[betaIndex] *= 0.99;
        }

        originalFitness = getCurrentFittness();
        originalDistance = distance;
        originalEntropy = entropy;
        originalResults = data.cloneResults();
        
        ai_original = Arrays.copyOf(ai, ai.length);
        bj_original = Arrays.copyOf(bj, bj.length);
        
        if (!runModel()){
            rejectChange();
            firstChange = true;
        }
        
        return getCurrentFittness();
        
    }

    public double generateRandomBeta(){
        return -1 * (random.nextDouble() / (random.nextInt(1000) + 1));
    }
    
    @Override
    public void makeChange() {
        firstChange=true;
    }

    @Override
    public void rejectChange() {
        beta[betaIndex] = originalBeta;
        distance = originalDistance;
        entropy = originalEntropy;
        data.setResults(originalResults);
        
        ai = ai_original;
        bj = bj_original;
        
        firstChange = !firstChange;
        
    }

    @Override
    public String getMessage() {
        return "Calibrating doubly constrained model";
    }

    @Override
    public void printFittness(boolean minorIteration) {
        if (minorIteration){
            //System.out.println(entropy);
            SpatialInteractionModel.getCurrent().report("minor - Entropy = " + entropy + " Distance = " + distance + " Fitness = " + getCurrentFittness());
            data.storeBestFitResult(data.getResults(), entropy, distance, getCurrentFittness());
        }else{
            SpatialInteractionModel.getCurrent().report("MAJOR - Entropy = " + entropy + " Distance = " + distance + " Fitness = " + getCurrentFittness());
            data.storeFinalRunResult(data.getResults(), entropy, distance);
        }
    }

    
    
    @Override
    public boolean isPerfect() {
        return false;
    }

    @Override
    public void setOptimised(boolean optimised) {
        this.optimised = optimised;
    }
    
}

