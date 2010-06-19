(ns at.ac.tuwien.tilab.icfp2010.cars)

(def sample-car [{:upper [0 0 0] :is-main true :lower [0 1 0]}])
(def sample-fuels [[[1 2]
		    [0 1]]
		   [[1 0]
		    [0 0]]])

(defn fuels-ingredients [fuels]
  (count (first fuels)))

(defn transform-air [air fuel]
  (map (fn [k]
	 (reduce + (map * (nth fuel k) air)))
       (range (count air))))

(defn process-pipe [air pipe fuels]
  (loop [air air
	 pipe pipe]
    (if (empty? pipe)
      air
      (recur (transform-air air (nth fuels (first pipe)))
	     (rest pipe)))))

(defn run-car [in-air car fuels]
  (map (fn [chamber]
	 (map - (process-pipe in-air (:upper chamber) fuels) (process-pipe in-air (:lower chamber) fuels)))
       car))

(defn run-successful? [car run]
  (reduce (fn [a b] (and a b))
	  (map (fn [chamber diff]
		 (and (if (:is-main chamber)
			(> (first diff) 0)
			(>= (first diff) 0))
		      (every? #(>= % 0) (rest diff))))
	       car run)))

(defn standard-air [fuels]
  (repeat (fuels-ingredients fuels) 1))

(defn minimum-air [fuels]
  (concat [1] (repeat (dec (fuels-ingredients fuels)) 0)))
