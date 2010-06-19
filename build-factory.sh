#!/bin/bash

java -cp icfp-2010-standalone.jar clojure.main -e "(use 'at.ac.tuwien.tilab.icfp2010.builder) (use 'at.ac.tuwien.tilab.icfp2010.circuit) (let [s \"$1\" c (build-fuel-factory-from-string s)] (println (circuit-string c)) (println) (println (simulate-circuit c (concat default-input (repeat (count s) 0)))))"
