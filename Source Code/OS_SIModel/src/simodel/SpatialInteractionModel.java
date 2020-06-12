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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import uk.ac.leeds.mass.optimisation.simulatedannealing.SimulatedAnnealing;


public class SpatialInteractionModel extends JFrame implements ActionListener, Runnable, KeyListener, FocusListener{
    
    private static Random random = new Random();
    
    private static String startingFilePath = "";
    
    //create some instance level variables for our text areas
    private JTextArea reportingArea = new JTextArea(20, 30);
        
    
    private JTextField originFile = new JTextField();
    private JTextField destinationFile = new JTextField();
    private JTextField distanceFile = new JTextField();
    
    private JButton originFileSelector = new JButton("...");
    private JButton destinationFileSelector = new JButton("...");
    private JButton distanceFileSelector = new JButton("...");

    
    private final JFileChooser chooser = new JFileChooser();
    
    private static SpatialInteractionModel sim = null;
    
    private SimulatedAnnealing sa = new SimulatedAnnealing();
    private JFormattedTextField maximumDistance = new JFormattedTextField(NumberFormat.getNumberInstance());
    private JButton run = new JButton("run optimisation");
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //create a new instance of this class
        SpatialInteractionModel.getCurrent();
    }

    public static SpatialInteractionModel getCurrent(){
        if (sim == null){
            sim = new SpatialInteractionModel();
        }
        return sim;
    }
    
    private SpatialInteractionModel(){
        //set the window title displayed at the top of the window
        this.setTitle("Spatial Interaction Model");
        
        //make the application exit when the window is closed
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Add contents to the window
        this.setMinimumSize(new Dimension(300, 300));

        this.add(setUpFrame());
        //Display the window. Pack makes sure the window can fit all 
        //of the components on it
        this.pack();
        this.setVisible(true);
    }
    
   
    private JPanel setUpFrame(){
        //create a new JPanel to hold all of the components
        JPanel panel = new JPanel(new GridBagLayout());
        //set up a layout constraint object
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        //add the panel with our text boxes and buttons to the main panel        
        panel.add(setupInputPanel(),c);
        //set up the area for reporting not to be typed into
        reportingArea.setEditable(false);
        //add it to a scroll panel in case it grows too big
        JScrollPane scrollPane = new JScrollPane(reportingArea);
        //adjust the settings for our layout constraints
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        //add the scroll pane to the main panel with the constraints
        panel.add(scrollPane, c);
        //return the main panel
        return panel;
    }

    private void addInputFileSelector(JPanel p, GridBagConstraints c, JTextField tf, JButton button, String name, String cmd){
        p.add(new JLabel(name),c);
        c.gridx += 1;
        p.add(tf,c);
        button.setActionCommand(cmd);
        button.removeActionListener(this);
        button.addActionListener(this);
        c.gridx += 1;
        c.gridwidth = 2;
        p.add(button,c);
        c.gridwidth = 1;
        c.gridx=0;
        c.gridy++;
    }
    
    private JPanel setupInputPanel(){
        // create the master panel to hang all of the controls on
        JPanel controlPanel = new JPanel(new GridBagLayout());
        //create the layout object for the control panel
        GridBagConstraints controlConstraints = new GridBagConstraints();
        
        //create the new sub pane to hold the input boxes
        JPanel p = new JPanel(new GridBagLayout());
        //set up a constraints object to position the controls
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        //position the controls
        c.gridx = 0;
        c.gridy = 0;
        
        originFile.setEditable(false);
        destinationFile.setEditable(false);
        distanceFile.setEditable(false);
        
        refreshTextBoxes();
        
        addInputFileSelector(p,c,originFile,originFileSelector,"origins", "origin");
        addInputFileSelector(p,c,destinationFile,destinationFileSelector,"destinations", "destination");
        addInputFileSelector(p,c,distanceFile,distanceFileSelector,"distances", "distance");

        controlConstraints.fill = GridBagConstraints.BOTH;
        controlConstraints.gridx = 0;
        controlConstraints.gridy = 0;

        controlPanel.add(p,controlConstraints);
        controlConstraints.gridy++;
        
        //create a button to run the optimisation process and add this as the listener
        run.setEnabled(checkRunStatus());
        run.removeActionListener(this);
        run.addActionListener(this);

        maximumDistance.removeFocusListener(this);
        maximumDistance.addFocusListener(this);
        maximumDistance.removeKeyListener(this);
        maximumDistance.addKeyListener(this);

        controlPanel.add(new JLabel("Enter total system distance (in metres)"),controlConstraints);
        controlConstraints.gridy++;
        controlPanel.add(maximumDistance,controlConstraints);
        controlConstraints.gridy++;
        controlPanel.add(new JLabel("Setup the optimisation parameters"),controlConstraints);
        controlConstraints.gridy++;
        controlPanel.add(sa,controlConstraints);
        controlConstraints.gridy++;
        controlPanel.add(run,controlConstraints);
        
        return controlPanel;
    }

    
    private boolean checkRunStatus(){
        boolean number = false;
        try{
            Double.parseDouble(maximumDistance.getText());
            number = true;
        }catch(Exception e){
            number = false;
        }
        return (DataHandler.getDataHandler().isOriginLoaded() &&
                DataHandler.getDataHandler().isDestinationLoaded() &&
                number);
    }
    
    
    private void refreshTextBoxes(){
        
        originFile.setText(DataHandler.getDataHandler().getOriginFile());
        if(DataHandler.getDataHandler().isOriginLoaded()){
            originFile.setForeground(Color.GREEN);
        }else{
            originFile.setForeground(Color.RED);
        }
        
        destinationFile.setText(DataHandler.getDataHandler().getDestinationFile());
        if(DataHandler.getDataHandler().isDestinationLoaded()){
            destinationFile.setForeground(Color.GREEN);
        }else{
            destinationFile.setForeground(Color.RED);
        }
        
        distanceFile.setText(DataHandler.getDataHandler().getDistanceFile());
        if(DataHandler.getDataHandler().isDistanceLoaded()){
            distanceFile.setForeground(Color.GREEN);
        }else{
            distanceFile.setForeground(Color.RED);
        }
        
    }
    
    //provide a way to report messages to the screen
    public void report(String message){
        //append the message passed into the method to the reporting area 
        //followed by a line separator
        reportingArea.append(message + System.getProperty("line.separator"));
        //make sure the last entered text is visible
        reportingArea.setCaretPosition(reportingArea.getText().length());
        reportingArea.repaint();
    }

    private void chooseFile(ActionEvent e){
        
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setCurrentDirectory(new File(startingFilePath));
        int ret = chooser.showOpenDialog(this);
        File file = chooser.getSelectedFile();
        DataHandler data = DataHandler.getDataHandler();
        if (ret == JFileChooser.APPROVE_OPTION){
            if ( e.getActionCommand().equals("origin") ){
                data.loadOrigins(file.getAbsolutePath());
            }else if ( e.getActionCommand().equals("destination") ){
                data.loadDestinations(file.getAbsolutePath());
            }else if ( e.getActionCommand().equals("distance")){
                data.loadDistances(file.getAbsolutePath());
            }
        
            refreshTextBoxes();
            
        }
        
    }
    
  
    //detect an action when the button is pressed
    @Override
    public void actionPerformed(ActionEvent e) {        

        if ( ((JButton)e.getSource()).getText().equals("...")){
            //Launch the file selector if it is any of the file selector buttons pressed
            chooseFile(e);
        }else{
            //launch new thread to run the model if it is the run model button pressed
            (new Thread(this)).start();
        }    
    }
    

    @Override
    public void run() {
        
        Random r = new Random();
        
        double[] beta = new double[1];

        DoubleModel dm = new DoubleModel(r);
        
//************************************************************************************/
//* CHANGE THE INITIALISATION OF THE BETA PARAMETERS HERE IF YOU NEED TO.            */
//************************************************************************************/
        //initialise the beta values
        for (int i = 0; i < beta.length; i++) {beta[i] = -0.01;}
        
        //make an initial run of the model
        dm.runModel(beta);
        
        //print out the initial runs to the screen
        SpatialInteractionModel.getCurrent().report("Distance " + Double.toString(dm.getDistance()));
        SpatialInteractionModel.getCurrent().report("Entropy " + Double.toString(dm.getEntropy()));
        
        DoubleModelCalibrate calib = new DoubleModelCalibrate(Double.parseDouble(maximumDistance.getValue().toString()), 
                sa.getStepsSlider().getModel().getValue(), 
                sa.getImprovementAttemptSlider().getModel().getValue(), 
                sa.getImprovementSlider().getModel().getValue(), 
                sa.getFactorSlider().getModel().getValue(), 
                r);
        
        calib.setSa(dm);
        calib.optimise();
        
        SpatialInteractionModel.getCurrent().report("Calibration finished. Saving outputs");
        
        DataHandler.getDataHandler().saveOutputs();

        SpatialInteractionModel.getCurrent().report("Done.");
    }
      
    public static Random random(){
        return random;
    }

    @Override
    public void focusLost(FocusEvent e) {
        run.setEnabled(checkRunStatus());
    }

    @Override
    public void focusGained(FocusEvent e) {
        run.setEnabled(checkRunStatus());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        run.setEnabled(checkRunStatus());
    }

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
