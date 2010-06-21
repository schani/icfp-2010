#!/bin/bash

git pull
./submit.pl badcars > data/badcars.txt
git commit data/badcars.txt -m autoupdate_badcars
git push
