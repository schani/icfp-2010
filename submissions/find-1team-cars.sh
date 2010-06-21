#!/bin/bash

cat | awk '/: 1 teams/ {carid=$2} /size/ {if (carid > 0) { printf "carid %i size %i\n", carid, $2; carid=0}}' | sort -nr -k 4
