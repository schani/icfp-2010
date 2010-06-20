(ns at.ac.tuwien.tilab.icfp2010.fuel
  (:use clojure.contrib.generic.math-functions)
  (:use at.ac.tuwien.tilab.icfp2010.ternary)
  )

;; warning only up to 41 is supported

(defn thing-to-string [thing]
  (if (list? thing)
    (apply str 
	   (prefixes (count thing)) 
	   (map thing-to-string thing))  
    (if (vector? thing)
      (apply str (map thing-to-string thing))
      (encode-number thing))))


(defn output-fuel "(((1))) is a fuel" 
  [fuel]
  (print (thing-to-string fuel)))
