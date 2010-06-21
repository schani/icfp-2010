#!/bin/bash

exec java -Dat.ac.tuwien.complang.distributor.worker=true -Dat.ac.tuwien.complang.distributor.vsc-server=l01 -Dpid=$$ -cp `echo lib/*.jar | sed -e 's/ /:/g'`:classes:src/clj clojure.main -i dist/dist.clj
