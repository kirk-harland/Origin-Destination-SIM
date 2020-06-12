# Origin-Destination-SIM
Origin Destination constrained Spatial Interaction Model calibrated using Simulated Annealing 

## Origins of the model
Post Phd studies at the University of Leeds I was asked to contribute to a masters level module on advanced spatial analysis techniques.  The core of the model contained in this repository was produced to facilitate a practical demonstration for that teaching module.  The code contained here has been adapted but essentially remains in the original form produced.  The model is based on the [family of spatial interaction models](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.470.310&rep=rep1&type=pdf) presented by [Alan Wilson](https://en.wikipedia.org/wiki/Alan_Wilson_(academic)) during his time working at the University of Leeds and is a presentation of the production-attraction constrained model outlined in the paper.

The algorithm used to calibrate the model is Simulated Annealing. This algorithm has it roots in the study of the cooling of metals but has been adopted in many other disciplines, one of which is the social sciences where it has been used to produce micor-synthetic populations from macro-level population counts used as model constraints. The algorithm applied here was extracted directly from software developed for this purpose while I worked at the University of Leeds. The guide to the software can be found [here](http://eprints.ncrm.ac.uk/3177/2/microsimulation_model.pdf) and the discussion on how the algorithm works in sections 6.3 and 6.4 pages 27-32 may prove useful.

## Structure of the repository
* Distributable - contains the pre-compiled .jar files required to run the model in its current form.
* Docs - contains a brief guide on how to run the model and what to expect during the execution process.
* Example Data - contains a small idealised example dataset that can be used to quickly test and familiarise yourself with the model operation before moving ahead to tasks where model calibration may take many hours.
* Source Code - contains the Java source code used to produce the model, the measurement metrics and complete optimisation algorithm.

## Licence details
Please see the file LICENCE.txt for details of the distribution licence
