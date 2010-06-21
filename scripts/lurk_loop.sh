#!/bin/bash

while true; do 
    echo "new lurk/generate loop round started"
    echo "lurking!"
    ./lurk_submit_cars.pl
    echo "badcars!"  
    ./generate_badcars.sh
    echo "sleeping"
    sleep 60 
done


