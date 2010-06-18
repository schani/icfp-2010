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

(defn input-index [input]
  (if (= input :x-in)
    0
    (let [[gate wire] input]
      (inc (+ (* gate 2) (if (= wire :l) 0 1))))))

(defn preprocess-circuit [circuit]
  (loop [outputs (sort-by key (:outputs circuit))
	 code []]
    (if (empty? outputs)
      code
      (let [gate (key (first outputs))
	    gate-outputs (val (first outputs))]
	(recur (rest outputs)
	       (conj code [(input-index [gate :l]) (input-index [gate :r])
			   (input-index (:l gate-outputs)) (input-index (:r gate-outputs))]))))))

(defn preprocessed-step [code inputs]
  (loop [code code
	 inputs inputs]
    (if (empty? code)
      inputs
      (let [[li ri lo ro] (first code)
	    [l r] (gate-function (nth inputs li) (nth inputs ri))]
	(recur (rest code) (assoc inputs lo l ro r))))))

(defn simulate-circuit [circuit input-stream]
  (loop [input-stream input-stream
	 last-inputs (undef-inputs circuit)
	 output-stream []]
    (if (empty? input-stream)
      output-stream
      (let [[inputs delayed changed] (circuit-step circuit (assoc last-inputs (:input circuit) (first input-stream)))]
	;;(println {:inputs inputs :delayed delayed :changed changed})
	(recur (rest input-stream)
	       delayed
	       (conj output-stream (:x-in inputs)))))))

(defn simulate-preprocessed [circuit input-stream]
  (let [code (preprocess-circuit circuit)
	in-index (input-index (:input circuit))
	inputs (apply vector (repeat (inc (* (count (:outputs circuit)) 2)) 0))]
    (loop [input-stream input-stream
	   output-stream []
	   inputs inputs]
      (if (empty? input-stream)
	output-stream
	(let [new-inputs (preprocessed-step code (assoc inputs in-index (first input-stream)))]
	  (recur (rest input-stream) (conj output-stream (nth new-inputs 0)) new-inputs))))))

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
