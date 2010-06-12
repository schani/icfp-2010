(ns at.ac.tuwien.tilab.icfp2010.core
  (:use at.ac.tuwien.complang.distributor.vsc))

(vsc-fn fib 1 [n]
  (if (< n 2)
    n
    (+ (fib (- n 1)) (fib (- n 2)))))
