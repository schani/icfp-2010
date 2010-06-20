#!/bin/bash

./build-factory.sh $2 | sed '$ d' | scripts/submit_test.pl car $1 - 
