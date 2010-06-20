#!/bin/bash

#this is the source file generated from schani holding data n the form:
#carid fuelmatrixes
#2723 (((2 7 0 1) (0 7 0 0) (0 5 0 7) (14 1 9 0)) ((1 4 0 0) (0 0 0 0) (4 2 0 2) (0 7 0 0)))




SCHANIFILE="submissions/genetic-fuels"
RUNTIME=`date +"%F-%T"`


cat $SCHANIFILE | while read line
do
    #get the car id of the line
    carid=`echo ${line%% *}`;
    #get the fuelmatrix of the line
    fuelmatrix=`echo ${line#* }`;

    #call thing-to-string magic code
    fuelstring=`./thing-to-string.sh "$fuelmatrix"`

    #testoutput
    #echo $carid $fuelstring

    #generate fuel factory and submit it to the contest page (and log the output)
    ./build-and-submit-fuel.sh $carid $fuelstring >> submit-$RUNTIME.log

done

