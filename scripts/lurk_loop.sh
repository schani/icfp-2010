#!/bin/bash

while true; do 
    echo "new lurk/generate loop round started"
    echo
    ./lurk_submit_cars.pl  
    ./generate_badcars.sh
    sleep 60 
done


