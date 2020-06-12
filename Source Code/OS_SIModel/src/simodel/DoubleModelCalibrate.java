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

import java.util.Random;
import uk.ac.leeds.mass.optimisation.simulatedannealing.SimulatedAnnealingProcess;


public class DoubleModelCalibrate extends SimulatedAnnealingProcess{
    
    private double maximumDistance;
    
    
    public DoubleModelCalibrate(double maximumDistance, int steps, int attempts, int success, int factor, Random random){
        //set up the values for the variables from the sliders
        super(steps, attempts, success, factor, random);
        this.maximumDistance = maximumDistance;
        DataHandler.getDataHandler().setObservedDistance(maximumDistance);
    }
    
    
}

