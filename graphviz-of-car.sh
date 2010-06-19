#!/bin/bash

function usage() {
    cat <<EOF
$0: usage: $0 <str> <ofi>
  where <str> .. car describing ternary string
    and <ofi> .. name of output postscript file (e.g. car.ps)
EOF
    exit 1
}

[ -z $1 ] && usage
[ -z $2 ] && usage
[ -z $3 ] || usage

java -cp icfp-2010-standalone.jar clojure.main -e "(use 'at.ac.tuwien.tilab.icfp2010.ternary) (use 'at.ac.tuwien.tilab.icfp2010.car2graphviz) (let [car \"$1\"] (print (graphviz-from-car (parse-car car))))" | dot -Grankdir=LR -Tps -o $2

