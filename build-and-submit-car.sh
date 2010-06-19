#!/bin/bash

./build-factory.sh $2 | sed '$ d' | scripts/submit.pl car $1 - 
