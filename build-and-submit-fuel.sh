#!/bin/bash

./build-factory.sh $2 | sed '$ d' | scripts/submit.pl fuel $1 - 
