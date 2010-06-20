(ns at.ac.tuwien.tilab.icfp2010.fuel
  (:use [at.ac.tuwien.tilab.icfp2010.ternary]
	[clojure.contrib.io :only (writer)])
  (:import (java.io BufferedReader FileReader
		    BufferedWriter FileWriter)))

 

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

(defn map-to-fuel-desc [f]
  (apply list (map #(f %) (sort (keys f)))))

(defn is-simple-car [c]
  (let [pred (fn [chamber]
	       (let [{:keys [upper lower]} chamber]
		 (or (= (:upper chamber) (:lower chamber))
		     (let [fu (first upper)
			   fl (first lower)]
		       (and 
			(every? #(= % fu) upper)
			(every? #(= % fl) lower))))))]
    (every? pred c)))

   

(defn fuel-for-simple-car
  "simple cars are cars which have something like a>b^221 and b>=a^222"
  [c]
  (if-not (is-simple-car c)
    nil 
    (let [in-eq (filter (fn [chamber] (not (= (:upper chamber) (:lower chamber)))) c)
	  ;;	  main (filter #(:is-main %) in-eq)
	  aux  (filter #(not (:is-main %)) in-eq)]
      (loop [aux (seq aux) fuel {}]
	(if-not aux 
	  (do 
	    ;	    (println fuel)
	    (map-to-fuel-desc fuel))
	  (let [{:keys [upper lower]} (first aux)
		big-number (int-pow 2 (count upper))]
	    (recur (next aux) 
		   (assoc fuel 
		     (first upper) '((2)) 
		     (first lower) `((~big-number))))))))))

(defn parse-car-carefully [car-id carstring]
  (try 
   (second (parse-car carstring))
   (catch Exception e 
     (println (str "parsing car #" car-id " failed: " carstring))
       nil)))

(defn solve-all-simple-cars [file]
  (let [pattern (re-pattern "id=(\\d+) car=(\\d+)")]
    (with-open [file (BufferedReader. (FileReader. file))]
      (with-open [out (BufferedWriter. (FileWriter. "skriptl"))]
	(loop [lines (line-seq file) solutions []]
	  (if-not lines
	    solutions
	    (let [line (first lines)
		  [_ car-id car-code] (first (re-seq pattern line))
		  parsed-car (parse-car-carefully car-id car-code)
		  solution (fuel-for-simple-car parsed-car)]
	      (when solution
					;	      (println (str "submission" car-id " "  solution))
		(.write out (str "./build-and-submit-fuel.sh " car-id " " (thing-to-string solution) "\n"))
		)
	      (recur (next lines)
		     (if solution 
		       (conj solutions [car-id solution])
		       solutions)))))))))
