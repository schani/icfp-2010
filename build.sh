#!/bin/bash

cd clj-simple-dist
lein deps
lein compile
lein compile-java
lein install
cd ..

lein deps
lein compile-java
lein uberjar
