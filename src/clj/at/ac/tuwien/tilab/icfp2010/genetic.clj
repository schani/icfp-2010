(ns at.ac.tuwien.tilab.icfp2010.genetic
  (:use at.ac.tuwien.tilab.icfp2010.cars
	at.ac.tuwien.tilab.icfp2010.ternary
	at.ac.tuwien.tilab.icfp2010.search
	at.ac.tuwien.tilab.icfp2010.superpmap
	at.ac.tuwien.tilab.icfp2010.fuel
	at.ac.tuwien.tilab.icfp2010.chambaopti
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

(defn transpose [l]
  (apply map list l))

(def fuels-blacklist
     (let [lines (filter #(and (> (count %) 0) (not (.startsWith % "#")))
			 (clojure.contrib.string/split-lines (slurp "submissions/check_fuels.list")))
	   fuelss (map #(second (parse-fuel %)) lines)
	   fuelss (map transpose fuelss)]
       (apply vector (map (fn [fuels]
			    (map make-java-fuel fuels))
			  fuelss))))

(defn genetic-car [num-fuelss num-ingredients fuel-max num-tanks max-sections pop-size max-generations]
  (let [fuelss (map (fn [_]
		      (map (fn [_]
			     (Fuel/randomFuel *random* num-ingredients fuel-max))
			   (range num-tanks)))
		    (range num-fuelss))
	blacklist (filter #(>= (count %) num-tanks) fuels-blacklist)
	pop (map (fn [_]
		   (random-chamber *random* num-tanks max-sections))
		 (range pop-size))
	score-fn (fn [chamber]
		   (let [car [chamber]
			 scores (map #(car-fuels-score car %) fuelss)
			 [poss negs] (partition-with-pred #(>= % 0) scores)
			 pos-count (count poss)
			 max-neg (if (zero? (count negs)) 0 (apply max negs))]
		     (if (zero? pos-count)
		       [max-neg]
		       (let [upper-freqs (frequencies (:upper chamber))
			     lower-freqs (frequencies (:lower chamber))
			     upper-count (count (:upper chamber))
			     lower-count (count (:lower chamber))
			     tanks-score (* (count upper-freqs) (count lower-freqs))
			     short-upper-score (if (< upper-count lower-count) 2 1)
			     blacklist-match (some #(> (car-fuels-score car %) 0) blacklist)]
			 [(expt (- (/ max-neg pos-count))
				(/ 1 (max upper-count lower-count)))
			  tanks-score
			  short-upper-score
			  (if blacklist-match 1/10 1)]))))
	[num-gens fit-pop] (genetic-algorithm pop
					      (fn [chamber] (apply * (score-fn chamber)))
					      nil
					      (fn [chamber] (mutate-chamber *random* chamber num-tanks max-sections))
					      (fn [gen fit-pop] (>= gen max-generations))
					      :mutation-rate 950)
	[best best-score] (first fit-pop)]
    (if (< best-score 0)
      nil
      (let [car [best]
	    list-fuelss (map #(map unjava-fuel %)
			     (filter #(> (car-fuels-score car %) 0) fuelss))
	    car (minimized-car car)
	    mapping (into {} (map (fn [[k v]] [v k]) (:mapping (meta car))))
	    fuels (first list-fuelss)
	    fuels (map #(nth fuels (mapping %))
		       (range (count fuels)))
	    transposed-fuels (apply list (map #(apply list (apply map list %)) fuels))]
	(println (thing-to-string (car-schani2biely car)))
	(println (thing-to-string transposed-fuels))
	[best-score (count list-fuelss) car fuels]))))

(vsc-fn genetic-produce-car 1 [num-fuelss num-ingredients fuel-max num-tanks max-sections pop-size max-generations]
	(let [[score fuelss-count car fuels] (genetic-car num-fuelss num-ingredients fuel-max num-tanks
							  max-sections pop-size max-generations)]
	  (if score
	    (let [transposed-fuels (apply list (map #(apply list (apply map list %)) fuels))]
	      [score fuelss-count car (thing-to-string (car-schani2biely car)) (thing-to-string transposed-fuels)])
	    nil)))
