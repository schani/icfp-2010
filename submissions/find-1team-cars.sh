#!/bin/bash

cat | awk '/: 1 teams/ {carid=$2} /size/ {if (carid > 0) { printf "carid %i size %i\n", carid, $2; carid=0}}' | sort -nr -k 4
# egrep "$(echo $(./find-1team-cars.sh < cars | awk '{print $2}' ) | sed 's# # |#g')" ../scripts/data/badcars.txt > hipricars.txt
