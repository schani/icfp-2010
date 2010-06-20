(ns at.ac.tuwien.tilab.icfp2010.genetic
  (:use at.ac.tuwien.tilab.icfp2010.cars)
  (:import [at.ac.tuwien.tilab.icfp2010 Fuel]))

(defn- choose [random pop]
  (let [index (int (* (.nextGaussian random) (count pop) 1/3))
	index (if (< index 0) (- index) index)]
    (if (>= index (count pop))
      (choose random pop)
      (nth pop index))))

(defn genetic-algorithm [population fitness-func combine-func mutate-func stop-cond-pred & params]
  (let [params (merge {:mutation-rate 5} (apply hash-map params))
	mutation-rate (:mutation-rate params)
	random (java.util.Random.)]
    (loop [generation 0
	   population population]
      (let [fitness-pop (reverse (sort-by second (map (fn [i] [i (fitness-func i)]) population)))]
	(println {:generation generation :best (first fitness-pop) :worst (last fitness-pop)})
	(if (stop-cond-pred generation fitness-pop)
	  [generation fitness-pop]
	  (recur (inc generation)
		 (map (fn [_]
			(let [offspring (combine-func (first (choose random fitness-pop))
						      (first (choose random fitness-pop)))]
			  (if (zero? (.nextInt random mutation-rate))
			    (mutate-func offspring)
			    offspring)))
		      population)))))))

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
