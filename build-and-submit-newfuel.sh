#!/bin/bash

./build-factory.sh $1 | sed '$ d' > scripts/data/fuel_$2.txt
cd scripts
./submit_new_fuel_against_all.pl data/fuel_$2.txt  
cd ..
