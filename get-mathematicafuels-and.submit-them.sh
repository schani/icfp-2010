#!/bin/bash



#this is the source file generated from mathematica holding data n the form:
#carid fuelmatrixes
#2723 (((2 7 0 1) (0 7 0 0) (0 5 0 7) (14 1 9 0)) ((1 4 0 0) (0 0 0 0) (4 2 0 2) (0 7 0 0)))





RUNTIME=`date +"%F-%T"`

SCHANIFILE="raw00sol.txt"

SUBMISSIONFILE="submissions/submittable-fuels-$RUNTIME.txt"
SUBLISSIONLOGFILE="submissions/submittable-fuels-$RUNTIME.log"

RUNTIME=`date +"%F-%T"`

#cat $SCHANIFILE | grep -v false | awk '{gsub("]|[[]","");printf "%s ", $1; gsub("^[^(]+[(]","(");printf "(%s)\n", $0}' > $SUBMISSIONFILE

cat $SCHANIFILE > $SUBMISSIONFILE

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

