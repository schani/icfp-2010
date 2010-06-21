#!/bin/bash

# this is the source file generated from schani holding data n the form:
# car_trinary fuel_trinary





RUNTIME=`date +"%F-%T"`

SCHANIFILE="submissions/bertlliste_winner_go.txt"

SUBMISSIONFILE="submissions/submittable-cars-$RUNTIME.txt"
SUBLISSIONLOGFILE="submissions/submittable-cars-$RUNTIME.log"

RUNTIME=`date +"%F-%T"`

cp $SCHANIFILE $SUBMISSIONFILE


cat $SUBMISSIONFILE | while read line
do
    #get the carstring of the line
    carstring=`echo ${line%% *}`;
    #get the fuelstring of the line
    fuelstring=`echo ${line#* }`;

    #testoutput
    echo "Trying: $carid $fuelstring" >> $SUBLISSIONLOGFILE

    #generate fuel factory and submit car to the contest page (and log the output)
    ./build-and-submit-car.sh $carstring $fuelstring >> $SUBLISSIONLOGFILE

done

