(ns at.ac.tuwien.tilab.icfp2010.circuit
  )

(def sample-circuit
  {:input [0 :l]
   :outputs {0 {:r :x-in
		:l [0 :r]}}})

(defn grid-gen [n]
  {:input [0 :r]
   :outputs (assoc (into {} (map (fn [i]
				   [i {:l [i :l]
				       :r [(inc i) :r]}])
				 (range n)))
	      n {:l [n :l] :r :x-in})})

; more or less the output of 
; $_=lc $_;s/.*\#(\d+)([rl])(\d+)([rl]).*/{:r [$1 :$2] :l [$3 :$4]}/;
(defn key-gen []
  {:input [19 :l]
   :outputs {
	     0
	     {:r [1 :r] :l [12 :r]}
	     1
	     {:r [4 :r] :l [9 :l]}
	     2
	     {:r [3 :l] :l [8 :l]}
	     3
	     {:r [5 :l] :l [9 :r]}
	     4
	     {:r [10 :r] :l [13 :r]}
	     5
	     {:r [6 :l] :l [15 :l]}
	     6
	     {:r [13 :l] :l [12 :l]}
	     7
	     {:r [11 :r] :l [8 :r]}
	     8
	     {:r [11 :l] :l [10 :l]}
	     9
	     {:r [18 :l] :l [2 :l]}
	     10
	     {:r [16 :l] :l [2 :r]}
	     11
	     {:r [15 :r] :l [6 :r]}
	     12
	     {:r [14 :l] :l [0 :l]}
	      13
	     {:r [14 :r] :l [0 :r]}
	     14
	     {:r [17 :l] :l [1 :l]}
	     15
	     {:r [16 :r] :l [4 :l]}
	     16
	     {:r [17 :r] :l [7 :r]}
	     17
	     {:r [18 :r] :l [3 :r]}
	     18
	     {:r [19 :r] :l [5 :r]}
	     19
	     {:r :x-in :l [7 :l]}}})
  


(defn assign-input [inputs output input]
  (if (= :x-in input)
    (assoc inputs :x-in output)
    (let [input-gate (first input)]
      (assoc inputs input-gate (assoc (inputs input-gate) (second input) output)))))

(defn gate-inputs [circuit]
  (let [input (:input circuit)]
    (loop [inputs {(first input) {(second input) :x-out}}
	   outputs (:outputs circuit)]
      (if (empty? outputs)
	inputs
	(let [output (first outputs)
	      gate (key output)
	      wires (val output)]
	  (recur (-> inputs
		     (assign-input [gate :l] (:l wires))
		     (assign-input [gate :r] (:r wires)))
		 (rest outputs)))))))

(def gate-table {[0 0] [0 2]
		 [0 1] [2 2]
		 [0 2] [1 2]
		 [1 0] [1 2]
		 [1 1] [0 0]
		 [1 2] [2 1]
		 [2 0] [2 2]
		 [2 1] [1 1]
		 [2 2] [0 0]})

(defn gate-function [l r]
  (let [ln (if (= :undef l) 0 l)
	rn (if (= :undef r) 0 r)]
    (gate-table [ln rn])))

(defn wire-delayed? [circuit gate wire]
  (let [output (((:outputs circuit) gate) wire)]
    (if (= :x-in output)
      false
      (<= (first output) gate))))

(defn undef-inputs [circuit]
  (into {} (mapcat (fn [[gate outputs]]
		     (mapcat (fn [[wire input]]
			       (if (or (= input :x-in) (not (wire-delayed? circuit gate wire)))
				 []
				 [[input :undef]]))
			     outputs))
		   (:outputs circuit))))

(defn circuit-step [circuit inputs]
    (loop [outputs (sort-by key (:outputs circuit))
	   inputs inputs
	   delayed {}
	   changed false]
      (if (empty? outputs)
	[inputs delayed changed]
	(let [gate (key (first outputs))
	      gate-outputs (val (first outputs))
	      l-input (inputs [gate :l])
	      r-input (inputs [gate :r])]
	  (if (and l-input r-input)
	    (let [[l-output r-output] (gate-function l-input r-input)
		  l-delayed (wire-delayed? circuit gate :l)
		  r-delayed (wire-delayed? circuit gate :r)]
	      (let [delayed* (if l-delayed
			       (assoc delayed (:l gate-outputs) l-output)
			       delayed)
		    delayed** (if r-delayed
				(assoc delayed* (:r gate-outputs) r-output)
				delayed*)
		    inputs* (if l-delayed
			      inputs
			      (assoc inputs (:l gate-outputs) l-output))
		    inputs** (if r-delayed
			       inputs*
			       (assoc inputs* (:r gate-outputs) r-output))]
		(recur (rest outputs) inputs** delayed** true)))
	    (recur (rest outputs) inputs delayed changed))))))

(defn simulate-circuit [circuit input-stream]
  (loop [input-stream input-stream
	 last-inputs (undef-inputs circuit)
	 output-stream []]
    (if (empty? input-stream)
      output-stream
      (let [[inputs delayed changed] (circuit-step circuit (assoc last-inputs (:input circuit) (first input-stream)))]
	(println {:inputs inputs :delayed delayed :changed changed})
	(recur (rest input-stream)
	       delayed
	       (conj output-stream (:x-in inputs)))))))

(defn input-output-string [io]
  (if (contains? #{:x-in :x-out} io)
    "X"
    (let [[gate wire] io]
      (str gate ({:l "L" :r "R"} wire)))))

(defn circuit-string [circuit]
  (let [inputs (gate-inputs circuit)]
    (str (input-output-string (:input circuit)) ":\n"
	 (apply str
		(interpose ",\n"
			   (map (fn [[gate output]]
				  (let [gate-inputs (inputs gate)
					[li ri lo ro] (map input-output-string [(:l gate-inputs) (:r gate-inputs)
										(:l output) (:r output)])]
				    (str li ri "0#" lo ro)))
				(sort-by key (:outputs circuit))))) ":\n"
	 (input-output-string (:x-in inputs)) "\n")))

(def default-input [0 1 2 0 2 1 0 1 2 1 0 2 0 1 2 0 2])
