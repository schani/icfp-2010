#!/bin/bash

while true; do 
    echo "new lurk/generate loop round started"
    echo "lurking!"
    ./lurk_submit_cars.pl
    #git commit data/allcars.txt -m autoupdate
    #git commit data/known_cars.txt -m autoupdate

    echo "badcars!"  
    ./generate_badcars.sh

    echo "log2known"
    ./log2known.pl

    echo "sleeping"
    sleep 60 
done


