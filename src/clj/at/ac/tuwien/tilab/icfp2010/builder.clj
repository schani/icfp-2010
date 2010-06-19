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
  (map adders (calculate-adders ys)))

(defn reduce-right [f s]
  (cond (empty? s) (f)
	(empty? (rest s)) (first s)
	:else (f (first s) (reduce-right f (rest s)))))

(defn build-fuel-factory [ys]
  (let [adders (build-adders ys)]
    (reduce-right concat-circuits-reversed adders)))

(defn build-fuel-factory-from-string [s]
  (build-fuel-factory (concat the-key (map #(Integer/parseInt (str %)) s))))
