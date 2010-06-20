#!/bin/bash

export SUBMIT_DATA_PATH=scripts/data
./build-factory.sh $2 | sed '$ d' | scripts/submit.pl fuel $1 - 
