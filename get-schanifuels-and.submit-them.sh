#!/bin/bash

# this is the source file generated from schani holding data n the form:
# [carid [status [fuelmatrixes]]]
# [38850 [true [((1 1) (1 13)) ((1 0) (0 0)) ((1 0) (4 8)) ((1 3) (2 7)) ((3 2) (0 5)) ((1 6) (4 11))]]]
# or if not successful
# [27235 [false no cars found]]

RUNTIME=`date +"%F-%T"`

SCHANIFILE="submissions/genetic-fuels-7"

SUBMISSIONFILE="submissions/submittable-fuels-$RUNTIME.txt"
SUBLISSIONLOGFILE="submissions/submittable-fuels-$RUNTIME.log"

RUNTIME=`date +"%F-%T"`

cat $SCHANIFILE | grep -v false | awk '{gsub("]|[[]","");printf "%s ", $1; gsub("^[^(]+[(]","(");printf "(%s)\n", $0}' > $SUBMISSIONFILE

cat $SUBMISSIONFILE | while read line
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
   
    ./build-and-submit-fuel.sh $carid $fuelstring >> $SUBLISSIONLOGFILE

done

