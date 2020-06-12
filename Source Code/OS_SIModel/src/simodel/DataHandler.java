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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import uk.ac.leeds.filereader.CSVReader;
import uk.ac.leeds.filereader.CSVWriter;


public class DataHandler {
    
    public static boolean CALIBRATE_BETA = false;
    public static boolean CALIBRATE_ALPHA = true;
    
    private String path = "";
    
    private String originFile = "not specified";
    private String destinationFile = "not specified";
    private String distanceFile = "not specified";
    
    private boolean originLoaded = false;
    private boolean destinationLoaded = false;
    private boolean distanceLoaded = false;
    
    //array holding the weights for the origins
    private double[] origin = null;
    private String[] originID = null;
    //array holding the weights for the destinations
    private double[] destination = null;
    private String[] destinationID = null;
    
    //array holding the distances between the origin and 
    //destinations
    private double[][] distance = null;
    
    private double[][] results = null;
    
    private double[][] bestFit = null;    
    private double bestFitDisance = 0.0;
    private double bestFitEntroy = 0.0;
    private double bestFitStat = 0.0;
    private double[][] finalRun = null;
    private double finalRunDisance = 0.0;
    private double finalRunEntroy = 0.0;
    
    private double observedDistance = 0.0;
    
    //create a private static variable to hold the singleton instance 
    private static DataHandler data = null;
    
    private BufferedReader br = null;
    
    /**
     * Make the constructor private so it can only be accessed from 
     * within the class
     */
    private DataHandler(){}
    
    /**
     * Turns his object into a singleton. only one of these
     * can exist and it can be accessed from anywhere.
     * 
     */
    public static DataHandler getDataHandler(){
        if (data==null){
            data = new DataHandler();
        }
        return data;
    }
    

    /**
     * Accessor method for returning the loaded origin weights
     * 
     * @return a double array containing the origin weights or
     * null if data has not been loaded
     */
    public double[] getOrigin() {
        return origin;
    }

    /**
     * Accessor method for returning the loaded destination weights
     * 
     * @return a double array containing the destination weights or
     * null if data has not been loaded
     */
    public double[] getDestination() {
        return destination;
    }

    /**
     * Accessor method to get the distance between the origin and 
     * destination identified by the index values.
     * 
     * @param originIndex a valid integer value between 0 and the length 
     * of the origin array.
     * @param destinationIndex a valid integer value between 0 and the length 
     * of the destination array.
     * @return a double value representing the distance between the specified 
     * origin and destination.  If an invalid index is entered -1.0 is 
     * returned.
     */
    public double getDistance(int originIndex, int destinationIndex) {
        
        //check the origin index is valid
        if (originIndex >= 0 && originIndex < distance.length){
            //check that the destination index is valid
            if (destinationIndex >= 0 && destinationIndex < 
                    distance[originIndex].length){
                //if everything is correct return the value from the array
                return distance[originIndex][destinationIndex];
            }
        }
        
        //if the index values are not correct we will fall through to 
        //here and return -1.0
        return - 1.0;
        
    }
 
    private double[][] load2DimensionDoubles(String filePath){
        
        File file = new File(filePath);
        
        String currentOrig = "";
        String currentDest = "";
        int currentOrigIndex = -1;
        int currentDestIndex = -1;
        
        double[][] d = new double[origin.length][destination.length];
        
        //set all of the values to -1 so that we can detect if there is not a 
        //connection between the origin and destination
        for(int i=0; i<d.length;i++){
            for(int j=0; j<d[i].length;j++){
                d[i][j]=-1.0;
            }
        }
        
        //create a string to hold the line we read
        String line;

        try{
            br = new BufferedReader(new FileReader(file));

            //cycle through the file until the line returned is null
            while ( (line = br.readLine()) != null ){
                //call parseLine to parse the fields from the line
                String[] s = parseLine(line);
                
                if ( !s[0].equals(currentOrig) ){
                    currentOrigIndex = -1;
                    currentOrig = s[0];
                    for (int j = 0; j < originID.length; j++) {
                        if ( currentOrig.equals(getOriginID(j)) ){
                            currentOrigIndex = j;
                            break;
                        }
                    } 
                }

                if ( !s[1].equals(currentDest) && currentOrigIndex > -1 ){
                    currentDestIndex = -1;
                    currentDest = s[1];
                    for (int j = 0; j < destinationID.length; j++) {
                        if ( currentDest.equals(getDestinationID(j)) ){
                            currentDestIndex = j;
                            break;
                        }
                    } 
                }
                
                if (currentOrigIndex>-1 && currentDestIndex>-1)
                    d[currentOrigIndex][currentDestIndex] = Double.parseDouble(s[2]);

            }

            //close the BufferedReader object when we have finished with it
            br.close(); 

        } catch (IOException ex) {
            SpatialInteractionModel.getCurrent().report(ex.getMessage());
            for (StackTraceElement ste: ex.getStackTrace()) {
                SpatialInteractionModel.getCurrent().report(ste.toString());
            }
            
            return null;
            
        }
        
        return d;
        
    }
    
    public boolean loadOrigins(String filePath){
        
        try{
            originFile = filePath;
            String[][] data = loadFile(filePath);
            int indexOffset = 0;
            
            try{
                double d = Double.parseDouble(data[0][1]);
                indexOffset = 0;
            }catch(Exception e){
                indexOffset = 1;
            }
            
            
            
            origin = new double [data.length - indexOffset];
            originID = new String[data.length - indexOffset];

            for (int i = 0 + indexOffset; i < data.length; i++) {
                originID[i - indexOffset] = data[i][0];
                origin[i - indexOffset] = Double.parseDouble(data[i][1]);              
            }
            
            originLoaded = true;

        }catch(Exception e){
            originLoaded = false;
        }
        
        return originLoaded;
    }
    
    public boolean loadDestinations(String filePath){
        
        try{
            destinationFile = filePath;
            String[][] data = loadFile(filePath);
            int indexOffset = 0;
            
            try{
                double d = Double.parseDouble(data[0][1]);
                indexOffset = 0;
            }catch(Exception e){
                indexOffset = 1;
            }
            
            destination = new double [data.length - indexOffset];
            destinationID = new String[data.length - indexOffset];
            

            for (int i = 0 + indexOffset; i < data.length; i++) {
                destinationID[i - indexOffset] = data[i][0];
                destination[i - indexOffset] = Double.parseDouble(data[i][1]);
            }            

            destinationLoaded = true;
                      
        }catch(Exception e){
            destinationLoaded = false;
        }
        
        return destinationLoaded;
    }
    
    public boolean loadDistances(String filePath){
        
        try{
            distanceFile = filePath;
            distance = load2DimensionDoubles(filePath);
            distanceLoaded = true;
        }catch (Exception e){
            distanceLoaded =false;
        }

        return distanceLoaded;
        
    }
    
    
    
    private String[][] loadFile(String filePath){
        
        File f = new File(filePath);
        
        CSVReader csv = new CSVReader();
        
        try {
            csv.loadData(f);
        } catch (IOException ex) {
            SpatialInteractionModel.getCurrent().report(ex.getMessage());
            for (StackTraceElement ste: ex.getStackTrace()) {
                SpatialInteractionModel.getCurrent().report(ste.toString());
            }
            
            return null;
            
        }
        String[][] s  = csv.getData();
        String[][] ret = new String[s.length][s[0].length];
        
        for (int i = 0; i < ret.length; i++) {
            
            for (int j = 0; j < ret[i].length; j++) {
                
                ret[i][j] = stripQuotes(s[i][j]);
            }
            
        }
        
        return ret;
        
    }
    
    private String stripQuotes(String original){
        
        int start = 0;
        int end = original.length();
        if ( original.startsWith("\"") ){
            start = 1;
        }
        if (original.endsWith("\"")){
            end = original.length()-1;
        }

        return original.substring(start, end);
    } 
    
    
    public boolean saveData(File file, String[][] data){
        if ( file != null && data != null ){
            
            CSVWriter csv = new CSVWriter();
            
            if ( file.exists() ){file.delete();}
            try {
                
                csv.saveData(file.getAbsolutePath(), data);
                return true;
                
            } catch (Exception ex) {
                SpatialInteractionModel.getCurrent().report(ex.getMessage());
                for (StackTraceElement ste: ex.getStackTrace()) {
                    SpatialInteractionModel.getCurrent().report(ste.toString());
                }
                
                return false;
            }
            
        }else{
            return false;
        }
    }
    
    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the originFile
     */
    public String getOriginFile() {
        return originFile;
    }

    /**
     * @param originFile the originFile to set
     */
    public void setOriginFile(String originFile) {
        this.originFile = originFile;
    }

    /**
     * @return the destinationFile
     */
    public String getDestinationFile() {
        return destinationFile;
    }

    /**
     * @param destinationFile the destinationFile to set
     */
    public void setDestinationFile(String destinationFile) {
        this.destinationFile = destinationFile;
    }


    /**
     * @return the originLoaded
     */
    public boolean isOriginLoaded() {
        return originLoaded;
    }

    /**
     * @return the destinationLoaded
     */
    public boolean isDestinationLoaded() {
        return destinationLoaded;
    }


    /**
     * @return the results
     */
    public double[][] getResults() {
        return results;
    }

    /**
     * @param results the results to set
     */
    public void setResults(double[][] results) {
        this.results = results;
    }
    
    public void setResults(double result, int orig, int dest){
        if (results==null){
            results = new double[this.origin.length][this.destination.length];
        }
        results[orig][dest] = result;
    }

    
    public String[] parseLine(String line){
        //return the split values to a local array
        String s[] = line.split(",",-1);
        //cycle each value in the local array
        for (int i = 0; i < s.length; i++) {
            //trim the values of any white space and assign them back
            //into the array
            s[i] =  stripQuotes(s[i].trim());
        }
        //return the array to the calling method
        return s;
    }
    
    public String getDistanceFile(){
        return distanceFile;
    }
    
    public boolean isDistanceLoaded(){
        return distanceLoaded;
    }

    /**
     * @return the observedDistance
     */
    public double getObservedDistance() {
        return observedDistance;
    }

    /**
     * @param observedDistance the observedDistance to set
     */
    public void setObservedDistance(double observedDistance) {
        this.observedDistance = observedDistance;
    }

    
    public double[][] cloneResults(){
        double [][] d = new double[results.length][results[0].length];
        
        //copy the array
        for(int i=0; i<results.length;i++){
            for(int j=0; j<results[i].length;j++){
                d[i][j]=results[i][j];
            }
        }
        
        return d;
    }
    
    public void storeBestFitResult(double[][] flowMatrix, double entropy, double distance, double fitness){
        if (fitness > bestFitStat){
            bestFit = new double[flowMatrix.length][flowMatrix[0].length];

            double distRes = 0.0;
            double dist = 0.0;
            double d = 0.0;
            //copy the array
            for(int i=0; i<flowMatrix.length;i++){
                for(int j=0; j<flowMatrix[i].length;j++){
                    bestFit[i][j]=flowMatrix[i][j];
                    d = getDistance(i,j) == -1.0 ? 0.0 : getDistance(i,j);
                    distRes += ( results[i][j] * d );
                    dist += ( flowMatrix[i][j] * d );
                }
            }

            bestFitDisance = distance;
            bestFitEntroy = entropy;
            bestFitStat = fitness;
        }
    }
    
    public void storeFinalRunResult(double[][] flowMatrix, double entropy, double distance){
        finalRun = new double[flowMatrix.length][flowMatrix[0].length];

        double dist = 0.0;
        double distRes = 0.0;
        double d = 0.0;
        
        //copy the array
        for(int i=0; i<flowMatrix.length;i++){
            for(int j=0; j<flowMatrix[i].length;j++){
                finalRun[i][j]=flowMatrix[i][j];
                d = getDistance(i,j) == -1.0 ? 0.0 : getDistance(i,j);
                distRes += ( results[i][j] * d );
                dist += ( flowMatrix[i][j] * d );
            }
        }
        
        finalRunDisance = distance;
        finalRunEntroy = entropy;
    }
    
    public void saveOutputs(){
        
        File dist = new File(destinationFile);
        File outputFolder = new File(dist.getParent() + System.getProperty("file.separator") + "outputs");
        if (outputFolder.mkdir()){

            double[][] bfProb = new double[bestFit.length][bestFit[0].length];
            double[][] frProb = new double[finalRun.length][finalRun[0].length];
            
            calculateProbabilities (bfProb, bestFit);
            calculateProbabilities (frProb, finalRun);
            
            File bfMatrixFile = new File(dist.getParent() + System.getProperty("file.separator") + "outputs" + System.getProperty("file.separator") + "Best Fit Matrix.csv");
            File bfStatsFile = new File(dist.getParent() + System.getProperty("file.separator") + "outputs" + System.getProperty("file.separator") + "Best Fit Stats.csv");
            File frMatrixFile = new File(dist.getParent() + System.getProperty("file.separator") + "outputs" + System.getProperty("file.separator") + "Final Run Matrix.csv");
            File frStatsFile = new File(dist.getParent() + System.getProperty("file.separator") + "outputs" + System.getProperty("file.separator") + "Final Run Stats.csv");
            File bfFlowMatrixFile = new File(dist.getParent() + System.getProperty("file.separator") + "outputs" + System.getProperty("file.separator") + "Best Fit Flow Matrix.csv");
            File frFlowMatrixFile = new File(dist.getParent() + System.getProperty("file.separator") + "outputs" + System.getProperty("file.separator") + "Final Run Flow Matrix.csv");
            File bfProbabilitiesFile = new File(dist.getParent() + System.getProperty("file.separator") + "outputs" + System.getProperty("file.separator") + "Best Fit Flow Probabilities.csv");
            File frProbabilitiesFile = new File(dist.getParent() + System.getProperty("file.separator") + "outputs" + System.getProperty("file.separator") + "Final Run Flow Probabilities.csv");

            if (bfMatrixFile.exists()) bfMatrixFile.delete();
            if (bfStatsFile.exists()) bfMatrixFile.delete();
            if (frMatrixFile.exists()) bfMatrixFile.delete();
            if (frStatsFile.exists()) bfMatrixFile.delete();
            if (bfFlowMatrixFile.exists()) bfFlowMatrixFile.delete();
            if (frFlowMatrixFile.exists()) frFlowMatrixFile.delete();
            if (bfProbabilitiesFile.exists()) bfProbabilitiesFile.delete();
            if (frProbabilitiesFile.exists()) frProbabilitiesFile.delete();

            String bfMatrixString[][]=new String[bestFit.length * bestFit[0].length][3];
            String bfProbabilityString[][]=new String[bestFit.length * bestFit[0].length][3];
            String bfStatsString[][]=new String[2][2];
            String bfFlowMatrixString[][]=new String[bestFit.length][bestFit[0].length];

            bfStatsString[0][0] = "Distance";
            bfStatsString[0][1] = Double.toString(bestFitDisance);
            bfStatsString[1][0] = "Entropy";
            bfStatsString[1][1] = Double.toString(bestFitEntroy);


            int c = 0;
            for(int i=0; i<bestFit.length;i++){
                for(int j=0; j<bestFit[i].length;j++){
                    bfMatrixString[c][0] = getOriginID(i);
                    bfMatrixString[c][1] = getDestinationID(j);
                    bfMatrixString[c][2] = Double.toString(bestFit[i][j]);
                    bfProbabilityString[c][0] = getOriginID(i);
                    bfProbabilityString[c][1] = getDestinationID(j);
                    bfProbabilityString[c][2] = Double.toString(bfProb[i][j]);
                    bfFlowMatrixString[i][j] = Double.toString(bestFit[i][j]);
                    c++;
                }
            }

            



            String frMatrixString[][]=new String[finalRun.length * finalRun[0].length][3];
            String frProbabilityString[][]=new String[bestFit.length * bestFit[0].length][3];
            String frStatsString[][]=new String[2][2];
            String frFlowMatrixString[][]=new String[bestFit.length][bestFit[0].length];

            frStatsString[0][0] = "Distance";
            frStatsString[0][1] = Double.toString(finalRunDisance);
            frStatsString[1][0] = "Entropy";
            frStatsString[1][1] = Double.toString(finalRunEntroy);

            c = 0;
            for(int i=0; i<finalRun.length;i++){
                for(int j=0; j<finalRun[i].length;j++){
                    frMatrixString[c][0] = getOriginID(i);
                    frMatrixString[c][1] = getDestinationID(j);
                    frMatrixString[c][2] = Double.toString(finalRun[i][j]);
                    frFlowMatrixString[i][j] = Double.toString(finalRun[i][j]);
                    frProbabilityString[c][0] = getOriginID(i);
                    frProbabilityString[c][1] = getDestinationID(j);
                    frProbabilityString[c][2] = Double.toString(frProb[i][j]);
                    c++;
                }
            }


            
            saveData(bfStatsFile,bfStatsString);
            saveData(bfMatrixFile,bfMatrixString);
            saveData(frStatsFile,frStatsString);
            saveData(frMatrixFile,frMatrixString);
            saveData(bfFlowMatrixFile,bfFlowMatrixString);
            saveData(frFlowMatrixFile,frFlowMatrixString);
            saveData(bfProbabilitiesFile,bfProbabilityString);
            saveData(frProbabilitiesFile,frProbabilityString);
            
        }
        
    }
    
    private void calculateProbabilities(double[][] probabilityMatrix, double[][] flowMatrix){
        double iTot;
        for(int i=0;i<flowMatrix.length;i++){
            //reset iTot
            iTot = 0.0;
            //get the total for the i dimension
            for(int j=0;j<flowMatrix[i].length;j++){
                iTot += flowMatrix[i][j];
            }
            
            //work out the probability for each cell if the iTot > 0
            for(int j=0;j<flowMatrix[i].length;j++){
                if (iTot > 0.0)
                    probabilityMatrix[i][j] = flowMatrix[i][j]/iTot;
            }
        }
    }
    

    /**
     * @return the originID
     */
    public String getOriginID(int i) {
        return originID[i];
    }

    /**
     * @return the destinationID
     */
    public String getDestinationID(int j) {
        return destinationID[j];
    }
    
}



