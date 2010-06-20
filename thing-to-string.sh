#!/bin/bash



java -cp icfp-2010-standalone.jar clojure.main -e "(use 'at.ac.tuwien.tilab.icfp2010.ternary) (use 'at.ac.tuwien.tilab.icfp2010.fuel) (let [thing '$1] (print (thing-to-string thing)))"


