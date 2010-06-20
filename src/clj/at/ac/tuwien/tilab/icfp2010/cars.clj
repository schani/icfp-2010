(ns at.ac.tuwien.tilab.icfp2010.cars)

(def sample-car [{:upper [0 0 0] :is-main true :lower [0 1 0]}])
(def sample-fuels [[[1 2]
		    [1 1]]
		   [[1 0]
		    [1 1]]])
(def test-car [{:upper [0 1 0 1 0 0], :is-main true, :lower [1 0 0 1 0 0 1]}])

(defn fuels-ingredients [fuels]
  (count (first fuels)))

(def unit-vectors
     (memoize (fn [n]
		(map (fn [i]
		       (concat (repeat i 0)
			       [1]
			       (repeat (- n i 1) 0)))
		     (range n)))))

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

(defn car-fuels-match [car fuels]
  (let [differences (map (fn [air]
			   (run-car air car fuels))
			 (unit-vectors (fuels-ingredients fuels)))
	is-match (every? (fn [air-diffs]
			   (every? (fn [chamber-diffs]
				     (and (> (first chamber-diffs) 0)
					  (every? #(>= % 0) (rest chamber-diffs))))
				   air-diffs))
			 differences)]
    [is-match differences]))

;;(defn run-successful? [car run]
;;  (reduce (fn [a b] (and a b))
;;	  (map (fn [chamber diff]
;;		 (and (if (:is-main chamber)
;;			(> (first diff) 0)
;;			(>= (first diff) 0))
;;		      (every? #(>= % 0) (rest diff))))
;;	       car run)))

(defn standard-air [fuels]
  (repeat (fuels-ingredients fuels) 1))

(defn minimum-air [fuels]
  (concat [1] (repeat (dec (fuels-ingredients fuels)) 0)))
