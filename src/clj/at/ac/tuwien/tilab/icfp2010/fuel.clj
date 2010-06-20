(ns at.ac.tuwien.tilab.icfp2010.fuel
  (:use [at.ac.tuwien.tilab.icfp2010.ternary]
	[clojure.contrib.io :only (writer)])
  (:import (java.io BufferedReader FileReader
		    BufferedWriter FileWriter)))

 

(defn thing-to-string [thing]
  (if (list? thing)
    (apply str 
	   (encode-list-length (count thing)) 
	   (map thing-to-string thing))  
    (if (vector? thing)
      (apply str (map thing-to-string thing))
      (if (seq? thing)
	(do 
	  (println "warning. got sequential which is neither a list not a vector. assuming list.")
	  (recur (apply list thing)))
	(encode-number thing)))))


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

(def mathematica-header "#$ -N mathematica\n#$ -pe mpich 32\n#$ -M mark.probst@gmail.com\n#$ -l h_rt=06:00:00\n#######$ -ar 2046\n#$ -cwd\n#$ -V\n\nMATHDIR=/opt/mathematica/tuwien/70/bin\n\n$MATHDIR/math <<EOF\n")
(def mathematica-footer "\nEOF")

(defn car-to-mathematica [car]
  (loop [chamber-list car, string "", num-of-tanks 0, num-of-chambs 0]
    (if (empty? chamber-list)
      (str
       string
       "FindInstance[ "
       (apply str (map (fn [x] (str "Z" x " >= 0 && ")) (range  num-of-chambs)))
       (reduce (fn [x y] (str x " && " y)) (map (fn [x] (str "A" x " >= 0")) (range  (inc num-of-tanks))))
       ", {"
       (reduce (fn [x y] (str x ", " y)) (map (fn [x] (str "A" x)) (range  (inc num-of-tanks))))
       "}, Integers]"
       )
      (let [chamber (first chamber-list), max-num (apply max (cons num-of-tanks (concat (chamber :upper) (chamber :lower))))]
	(recur (rest chamber-list)
	       (str  "Z" num-of-chambs " = "
		     (reduce (fn [x y] (str x "*" y)) (map (fn [x] (str "A" x)) (chamber :upper )))
		     " - "
		     (reduce (fn [x y] (str x "*" y)) (map (fn [x] (str "A" x)) (chamber :lower )))
		     (if (chamber :is-main)
		       " - 1"
		       ""
		       )
		     ";\n")
	       max-num
	       (inc num-of-chambs)
	       )
	))))

(defn prepare-cars-mathematica [file]
  (let [pattern (re-pattern "id=(\\d+) car=(\\d+)")]
    (with-open [file (BufferedReader. (FileReader. file))]
	(loop [lines (line-seq file)]
	  (if-not lines
	    nil
	    (let [line (first lines)
		  [_ car-id car-code] (first (re-seq pattern line))
		  parsed-car (second (parse-car car-code))
		  math-string (car-to-mathematica parsed-car)]
	      (do
		(with-open [out (BufferedWriter. (FileWriter. (str "mathematica_car" car-id ".sh")))]
		(.write out (str mathematica-header math-string mathematica-footer))
		))
	      (recur (next lines))
	      ))))))
