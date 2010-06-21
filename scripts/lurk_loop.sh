#!/bin/bash

while true; do 
    echo "new lurk/generate loop round started"
    echo "lurking!"
    ./lurk_submit_cars.pl
    git commit scripts/data/allcars.txt -m autoupdate
    git commit scripts/data/known_cars.txt -m autoupdate

    echo "badcars!"  
    ./generate_badcars.sh
    echo "sleeping"
    sleep 60 
done


