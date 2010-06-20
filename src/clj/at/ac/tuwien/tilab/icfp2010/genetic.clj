(ns at.ac.tuwien.tilab.icfp2010.genetic
  (:use at.ac.tuwien.tilab.icfp2010.cars
	at.ac.tuwien.tilab.icfp2010.ternary
	at.ac.tuwien.tilab.icfp2010.search
	at.ac.tuwien.tilab.icfp2010.superpmap
	at.ac.tuwien.complang.distributor.vsc
	clojure.contrib.math)
  (:require clojure.contrib.string)
  (:import [at.ac.tuwien.tilab.icfp2010 Fuel]))

(defn- choose [random pop]
  (let [index (int (* (.nextGaussian random) (count pop) 1/3))
	index (if (< index 0) (- index) index)]
    (if (>= index (count pop))
      (choose random pop)
      (nth pop index))))

(defn genetic-algorithm [population fitness-func combine-func mutate-func stop-cond-pred & params]
  (let [params (merge {:mutation-rate (if combine-func 200 800)} (apply hash-map params))
	mutation-rate (:mutation-rate params)
	random (java.util.Random.)]
    (loop [generation 0
	   population population]
      (let [fitness-pop (map (fn [i] [i (fitness-func i)]) population)
	    fitness-pop (reverse (sort-by second fitness-pop))]
	(println {:generation generation :best (second (first fitness-pop)) :worst (second (last fitness-pop))})
	(if (stop-cond-pred generation fitness-pop)
	  [generation fitness-pop]
	  (recur (inc generation)
		 (if combine-func
		   (map (fn [_]
			  (let [offspring (combine-func (first (choose random fitness-pop))
							(first (choose random fitness-pop)))]
			    (if (<= (.nextInt random 1001) mutation-rate)
			      (mutate-func offspring)
			      offspring)))
			population)
		   (doall (map (fn [_]
				 (let [offspring (first (choose random fitness-pop))]
				   (if (<= (.nextInt random 1001) mutation-rate)
				     (mutate-func offspring)
				     offspring)))
			       population)))))))))

(def *random* (java.util.Random.))

(defn combine-fuelss [as bs]
  (map (fn [a b]
	 (if (zero? (.nextInt *random* 1)) a b))
       as bs))

(defn genetic-fuels [car num-ingredients pop-size init-max max-mutate max-generations]
  (let [num-tanks (car-tanks car)
	pop (map (fn [_]
		   (map (fn [_]
			  (Fuel/randomFuel *random* num-ingredients init-max))
			(range num-tanks)))
		 (range pop-size))]
    (genetic-algorithm pop
		       #(car-fuels-score car %)
		       combine-fuelss
		       (fn [fuels] (map #(.mutate % *random* max-mutate) fuels))
		       (fn [gen fit-pop] (or (>= gen max-generations)
					     (> (second (first fit-pop)) 0))))))

(defn koeblerify-fuels [fuels]
  (let [fuels (map unjava-fuel fuels)]
    (map #(apply map list %) fuels)))

(vsc-fn genetic-solve-car-from-string 2 [car-string
					 min-ingred max-ingred
					 min-sections max-sections
					 pop-size init-max max-mutate max-generations]
	(try
	  (let [[rest-string car] (parse-car car-string)
		num-sections (car-sections car)]
	    (cond (not (empty? rest-string)) [false "car does not parse"]
		  (or (< num-sections min-sections)
		      (> num-sections max-sections)) [false "number of sections does not match"]
		  (< (car-tanks car) 2) [false "car does not have at least 2 tanks"]
		  :else
		  (loop [num-ingred min-ingred]
		    (if (> num-ingred max-ingred)
		      [false "no cars found"]
		      (let [[gen fit-pop] (genetic-fuels car num-ingred pop-size init-max max-mutate max-generations)
			    [best best-score] (first fit-pop)]
			(if (> best-score 0)
			  [true (str (apply vector (koeblerify-fuels best)))]
			  (recur (inc num-ingred))))))))
	  (catch Exception exc
	    [false (str exc)])))

(defn genetic-solve-cars [cars
			  min-ingred max-ingred
			  min-sections max-sections
			  pop-size init-max max-mutate max-generations]
  (superpmap 1000 (fn [[id string]]
		    [id (genetic-solve-car-from-string-vsc string min-ingred max-ingred min-sections max-sections pop-size init-max max-mutate max-generations)])
	     cars))

(defn read-cars-from-file [filename]
  (map #(clojure.contrib.string/split #"\s+" %) (clojure.contrib.string/split-lines (slurp filename))))

(defn genetic-car [num-fuelss num-ingredients fuel-max num-tanks max-sections pop-size max-generations]
  (let [fuelss (map (fn [_]
		      (map (fn [_]
			     (Fuel/randomFuel *random* num-ingredients fuel-max))
			   (range num-tanks)))
		    (range num-fuelss))
	pop (map (fn [_]
		   (random-chamber *random* num-tanks max-sections))
		 (range pop-size))]
    (genetic-algorithm pop
		       (fn [chamber]
			 (let [car [chamber]
			       scores (map #(car-fuels-score car %) fuelss)
			       [poss negs] (partition-with-pred #(>= % 0) scores)
			       pos-count (count poss)
			       max-neg (if (zero? (count negs)) 0 (apply max negs))]
			   (if (zero? pos-count)
			     max-neg
			     (let [upper-freqs (frequencies (:upper chamber))
				   lower-freqs (frequencies (:lower chamber))
				   diff-score (apply * (map (fn [t]
							      (/ 1 (inc (abs (- (get upper-freqs t 0) (get lower-freqs t 0))))))
							    (range num-tanks)))
				   tanks-score (* (count upper-freqs) (count lower-freqs))]
			       (* (- (/ max-neg pos-count))
				  ;diff-score
				  tanks-score)))))
		       nil
		       (fn [chamber] (mutate-chamber *random* chamber num-tanks))
		       (fn [gen fit-pop] (>= gen max-generations)))))
