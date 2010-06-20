(ns at.ac.tuwien.tilab.icfp2010.cars
  (:import [at.ac.tuwien.tilab.icfp2010 Fuel]))

(defn long-arr [a]
  (into-array (. Long TYPE) a))

(def long-array-type (type (long-arr [1 2 3])))

(defn make-java-fuel [matrix]
  (Fuel. (count matrix)
	 (into-array long-array-type (map long-arr matrix))))

(defn unjava-fuel [fuel]
  (map seq (map seq (.contents fuel))))

(def sample-car [{:upper [0 0 0] :is-main true :lower [0 1 0]}])
(def sample-fuels (map make-java-fuel [[[1 2]
					[1 1]]
				       [[1 0]
					[1 1]]]))
(def test-car [{:upper [0 1 0 1 0 0], :is-main true, :lower [1 0 0 1 0 0 1]}])

(defn car-tanks [car]
  (if (empty? car)
    0
    (apply max (map (fn [chamber]
		      (let [upper (:upper chamber)
			    lower (:lower chamber)]
			(max (if (empty? upper) 0 (inc (apply max upper)))
			     (if (empty? lower) 0 (inc (apply max lower))))))
		    car))))

(defn car-sections [car]
  (if (empty? car)
    0
    (apply max (map (fn [chamber]
		      (max (count (:upper chamber)) (count (:lower chamber))))
		    car))))

(defn fuels-ingredients [fuels]
  (.numIngredients (first fuels)))

(defn run-pipe [pipe fuels]
  (if (empty? pipe)
    (Fuel/defaultFuel (fuels-ingredients fuels))
    (loop [fuel (nth fuels (first pipe))
	   pipe (rest pipe)]
      (if (empty? pipe)
	fuel
	(recur (.multiply (nth fuels (first pipe)) fuel)
	       (rest pipe))))))

(defn run-chamber [chamber fuels]
  (let [upper (run-pipe (:upper chamber) fuels)
	lower (run-pipe (:lower chamber) fuels)
	diff (.subtract upper lower)]
    diff))

(defn car-fuels-score [car fuels]
  (let [chamber-scores (map (fn [chamber]
			      (.simpleScore (run-chamber chamber fuels)
					    (:is-main chamber)))
			    car)]
    (apply min chamber-scores)))
