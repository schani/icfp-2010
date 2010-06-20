(ns at.ac.tuwien.tilab.icfp2010.builder
  (:use at.ac.tuwien.tilab.icfp2010.circuit))

(def one-zero-circuit (un-preprocess-java-circuit 9 [5 6 7 10 0 8 2 1 3 4 9]))
(def constant-two-circuit (un-preprocess-java-circuit 10 [3 7 8 0 4 9 6 5 2 1 10]))
(def identity-circuit (un-preprocess-java-circuit 1 [0 5 2 3 4 6 1]))

(def increment-circuit {:input [5 :l],
			:outputs {0 {:l [1 :l], :r [3 :l]},
				  1 {:l [3 :r], :r [5 :r]},
				  2 {:l [1 :r], :r [4 :l]},
				  3 {:l [2 :r], :r [2 :l]},
				  4 {:l [0 :r], :r [0 :l]},
				  5 {:l :x-in, :r [4 :r]}}})

(def decrement-circuit {:input [6 :l],
			:outputs {0 {:l [1 :l], :r [1 :r]}
				  1 {:l [2 :l], :r [3 :r]},
				  2 {:l [6 :r], :r [0 :l]},
				  3 {:l [0 :r], :r [2 :r]},
				  4 {:l [3 :l], :r [5 :r]},
				  5 {:l [4 :l], :r [4 :r]},
				  6 {:l :x-in :r [5 :l]}}})

(def better-decrement-circuit {:input [1 :r],
			       :outputs {5 {:l [0 :r], :r [0 :l]},
					 4 {:l :x-in, :r [5 :r]},
					 3 {:l [4 :r], :r [5 :l]},
					 2 {:l [3 :r], :r [3 :l]},
					 1 {:l [2 :l], :r [4 :l]},
					 0 {:l [2 :r], :r [1 :l]}}})

(def delay-double-adders {[0 0] (un-preprocess-java-circuit 3 [0 5 1 6 2 4 3])
			  [0 1] (un-preprocess-java-circuit 12 [4 5 6 10 8 7 9 0 11 2 1 3 12])
			  [0 2] (un-preprocess-java-circuit 9 [4 5 0 6 7 8 11 10 12 3 2 1 9])
			  [1 0] (un-preprocess-java-circuit 6 [5 4 0 7 9 10 1 2 11 12 13 14 8 3 6])
			  [1 1] (un-preprocess-java-circuit 9 [6 5 1 10 8 11 0 12 2 7 3 4 9])
			  [1 2] (un-preprocess-java-circuit 5 [6 4 0 1 3 2 5])
			  [2 0] (un-preprocess-java-circuit 7 [5 3 0 11 12 8 9 10 14 13 2 1 6 4 7])
			  [2 1] (un-preprocess-java-circuit 6 [5 3 0 1 4 2 6])
			  [2 2] (un-preprocess-java-circuit 5 [4 3 0 14 1 2 6 10 13 11 7 8 9 12 5])})

(def delay-triple-adders {[2 1 0] (un-preprocess-java-circuit 12 [5 3 0 10 8 1 4 9 7 11 6 2 12])
			  [1 0 0] (un-preprocess-java-circuit 8 [7 4 0 6 11 12 5 9 1 2 13 14 10 3 8])
			  [2 2 1] (un-preprocess-java-circuit 14 [5 3 0 6 7 13 9 10 2 12 4 1 8 11 14])
			  [1 1 1] (un-preprocess-java-circuit 12 [5 4 0 1 11 2 6 9 3 7 10 8 12])
			  [0 0 1] (un-preprocess-java-circuit 9 [3 5 0 10 13 11 4 14 7 8 1 2 6 12 9])
			  [1 2 2] (un-preprocess-java-circuit 14 [5 4 0 6 2 8 11 10 13 12 1 3 9 7 14])
			  [0 1 2] (un-preprocess-java-circuit 9 [3 1 0 11 10 7 4 5 8 6 12 2 9])
			  [2 1 1] (un-preprocess-java-circuit 11 [5 3 0 6 12 14 10 4 1 2 9 13 8 7 11])
			  [1 0 1] (un-preprocess-java-circuit 9 [5 4 0 10 12 11 1 2 7 8 13 14 3 6 9])
			  [2 2 2] (un-preprocess-java-circuit 12 [5 3 0 2 10 1 11 9 4 7 6 8 12])
			  [1 1 2] (un-preprocess-java-circuit 14 [5 4 0 6 13 8 9 10 2 12 3 1 7 11 14])
			  [0 0 2] (un-preprocess-java-circuit 14 [3 5 0 7 9 13 1 2 11 12 8 4 6 10 14])
			  [2 0 0] (un-preprocess-java-circuit 7 [8 3 0 6 11 12 5 9 1 2 13 14 10 4 7])
			  [2 1 2] (un-preprocess-java-circuit 14 [5 3 0 6 13 8 12 9 1 11 2 4 7 10 14])
			  [1 0 2] (un-preprocess-java-circuit 12 [5 4 0 1 9 2 11 10 3 7 6 8 12])
			  [2 0 1] (un-preprocess-java-circuit 12 [5 3 0 1 11 2 6 10 4 7 9 8 12])
			  [2 0 2] (un-preprocess-java-circuit 14 [5 3 0 6 7 1 10 9 11 4 13 2 8 12 14])
			  [0 2 0] (un-preprocess-java-circuit 6 [1 5 0 12 11 13 4 10 2 3 9 14 7 8 6])
			  [1 2 0] (un-preprocess-java-circuit 12 [5 4 0 1 3 2 11 10 9 7 6 8 12])
			  [0 1 0] (un-preprocess-java-circuit 11 [6 5 0 12 4 13 14 2 3 8 10 9 7 1 11])
			  [0 2 1] (un-preprocess-java-circuit 12 [3 1 0 5 6 2 11 10 4 7 9 8 12])
			  [2 2 0] (un-preprocess-java-circuit 11 [5 3 0 6 2 12 13 9 8 14 10 7 1 4 11])
			  [1 1 0] (un-preprocess-java-circuit 14 [5 4 0 6 13 1 12 9 8 11 2 3 10 7 14])
			  [0 0 0] (un-preprocess-java-circuit 7 [3 1 0 9 4 12 6 11 10 2 5 8 7])
			  [1 2 1] (un-preprocess-java-circuit 14 [5 4 0 6 7 1 12 9 13 11 2 3 8 10 14])
			  [0 1 1] (un-preprocess-java-circuit 11 [9 1 0 12 7 8 10 4 13 14 5 6 2 3 11])
			  [0 2 2] (un-preprocess-java-circuit 14 [6 5 0 8 9 10 13 2 12 4 7 3 11 1 14])})

(defn shift-input [input n]
  (if (= input :x-in)
    input
    [(+ (first input) n) (second input)]))

(defn transform-circuit [circuit gate-trans input-trans]
  {:input (input-trans (:input circuit))
   :outputs (into {} (map (fn [[gate inputs]]
			    [(gate-trans gate)
			     (into {} (map (fn [[wire input]]
					     [wire (input-trans input)])
					   inputs))])
			  (:outputs circuit)))})

(defn shift-circuit [circuit n]
  (transform-circuit circuit
		     #(+ % n)
		     #(shift-input % n)))

(defn rewire-output [circuit new-input]
  (transform-circuit circuit
		     (fn [x] x)
		     (fn [input]
		       (if (= input :x-in)
			 new-input
			 input))))

(defn concat-circuits [c1 c2]
  (let [new-c2 (shift-circuit c2 (count (:outputs c1)))
	new-c1 (rewire-output c1 (:input new-c2))]
    {:input (:input new-c1)
     :outputs (merge (:outputs new-c1) (:outputs new-c2))}))

(defn concat-circuits-reversed [c1 c2]
  (let [new-c2 (rewire-output (shift-circuit c2 (count (:outputs c1))) (:input c1))]
    {:input (:input new-c2)
     :outputs (merge (:outputs c1) (:outputs new-c2))}))

(def adders [identity-circuit
	     increment-circuit
	     better-decrement-circuit])

(defn calculate-adders [ys]
  (loop [as []
	 sum 0
	 ys ys]
    (if (empty? ys)
      as
      (let [z (mod (- (first ys) sum) 3)]
	(recur (conj as z)
	       (+ sum z)
	       (rest ys))))))

(defn build-adders [ys]
  (loop [zs (calculate-adders ys)
	 res []]
    (cond (empty? zs) res
	  (= (count zs) 1) (conj res (adders (first zs)))
	  (= (count zs) 2) (conj res (delay-double-adders (take 2 zs)))
	  :else (recur (drop 3 zs)
		       (conj res (delay-triple-adders (take 3 zs)))))))

(defn reduce-right [f s]
  (cond (empty? s) (f)
	(empty? (rest s)) (first s)
	:else (f (first s) (reduce-right f (rest s)))))

(defn build-fuel-factory [ys]
  (let [adders (build-adders ys)]
    (reduce-right concat-circuits-reversed adders)))

(defn build-fuel-factory-from-string [s]
  (build-fuel-factory (concat the-key (map #(Integer/parseInt (str %)) s))))
