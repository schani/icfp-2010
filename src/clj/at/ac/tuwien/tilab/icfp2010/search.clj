(ns at.ac.tuwien.tilab.icfp2010.search
  (:use at.ac.tuwien.tilab.icfp2010.circuit
	at.ac.tuwien.complang.distributor.vsc)
  (:import [at.ac.tuwien.tilab.icfp2010 Permuter IPermutationConsumer Simulator
	    SimulationPermutationConsumer SearchSimulationConsumer ISimulationConsumer PrefixPermutationConsumer]
	   [java.util Random]))

(def integer-array-type (type (into-array (. Integer TYPE) [1 2 3])))

(defn- int-arr [a]
  (into-array (. Integer TYPE) a))

(defn java-simulate [n circuit input-index input-stream]
  (doall (seq (Simulator/simulate n circuit input-index (int-arr input-stream)))))

(defn all-subpermutations [n coll]
  (if (zero? n)
    [[]]
    (mapcat (fn [x]
	      (map #(conj % x)
		   (all-subpermutations (dec n) (remove #(= % x) coll))))
	    coll)))

(defn search-prefixes [num-gates len]
  (all-subpermutations len (range (inc (* num-gates 2)))))

(defn make-simulation-permutation-consumer [n search-streams pred results-atom]
  (let [print-simulation-consumer (reify
				   ISimulationConsumer
				   (consumeSimulation [this n circuit input-index input-stream output-stream]
						      (let [output (apply vector output-stream)]
							(when (pred n circuit input-index output)
							  (swap! results-atom conj {:circuit (apply vector circuit)
										    :input-index input-index
										    :output output})))))
	search-simulation-consumer (SearchSimulationConsumer. (into-array integer-array-type (map int-arr search-streams))
							      print-simulation-consumer)]
    (SimulationPermutationConsumer. n (int-arr default-input) search-simulation-consumer)))

(defn search-circuits-with-prefix [n search-streams prefix pred]
  (let [rest-seq (remove (fn [x] (some #(= % x) prefix)) (range (inc (* n 2))))
	results (atom [])
	simulation-permutation-consumer (make-simulation-permutation-consumer n search-streams pred results)
	prefix-permutation-consumer (PrefixPermutationConsumer. (int-arr prefix) simulation-permutation-consumer)]
    (Permuter/permuteArray (int-arr rest-seq) prefix-permutation-consumer)
    @results))

(defn search-circuits [n search-streams pred]
  (let [results (atom [])
	simulation-permutation-consumer (make-simulation-permutation-consumer n search-streams pred results)]
    (Permuter/permuteRange (inc (* n 2)) simulation-permutation-consumer)
    @results))

(defn all-inputs [n]
  (if (zero? n)
    [[]]
    (let [all-sub (all-inputs (dec n))]
      (mapcat (fn [i]
		(map #(conj % i)
		     all-sub))
	      [0 1 2]))))

(defn random-inputs [n len]
  (let [rand (Random.)]
    (map (fn [x]
	   (map (fn [i]
		  (.nextInt rand 3))
		(range len)))
	 (range n))))

(def all-inputs-6 (all-inputs 6))
(def some-random-inputs (random-inputs 50 1000))

(defn check-solution [pred]
  (fn [n circuit input-index output]
    (and (pred default-input output)
	 (every? (fn [input]
		   (let [result (java-simulate n circuit input-index input)]
		     (pred input result)))
		 all-inputs-6)
	 (every? (fn [input]
		   (let [result (java-simulate n circuit input-index input)]
		     (pred input result)))
		 some-random-inputs))))

(defn simple-gen? [n circuit input-index output]
  (and (not (= (first output) 0))
       (= (second output) 0)
       (apply = (rest output))
       (= (java-simulate n circuit input-index the-key) output)
       (= (java-simulate n circuit input-index [2 1 2 0 2 0 1 2 2 0 1 1 0 1 1 1 0]) output)
       (every? (fn [input]
		 (let [result (java-simulate n circuit input-index input)]
		   ;;(println result)
		   (= result (take (count input) output))))
	       all-inputs-6)
       ;;(println "beidel")
       (every? (fn [input]
		 (let [result (java-simulate n circuit input-index input)]
		   (and (= (take (count output) result) output)
			(apply = (rest result)))))
	       some-random-inputs)))

(defn is-key? [n circuit input-index output]
  (= output the-key))

(defn increment-stream [s n]
  (map #(mod (+ % n) 3) s))

(defn is-incrementer? [x]
  (check-solution (fn [input result]
		    (= result (increment-stream input x)))))

(def is-identity? (check-solution =))

(defn is-constant-x? [x]
  (let [proper? (fn [s]
		  (and (= (first s) x)
		       (apply = s)))]
    (check-solution (fn [input output] (proper? output)))))

(defn delay-add-stream [s n]
  (map #(mod (+ % n) 3)
       (take (count s) (concat [0] s))))

(defn is-delay-adder? [n]
  (check-solution (fn [input result]
		    (= result (delay-add-stream input n)))))

(defn delay-double-add-stream [s a b]
  (take (count s)
	(map (fn [x y z] (mod (+ x y z) 3))
	     (concat [0] s)
	     (repeat a)
	     (concat [0] (repeat b)))))

(defn is-delay-double-adder? [a b]
  (check-solution (fn [input result]
		    (= result (delay-double-add-stream input a b)))))

(defn delay-triple-add-stream [s a b c]
  (take (count s)
	(map (fn [w x y z] (mod (+ w x y z) 3))
	     (concat [0 0] s)
	     (repeat a)
	     (concat [0] (repeat b))
	     (concat [0 0] (repeat c)))))

(defn is-delay-triple-adder? [a b c]
  (check-solution (fn [input result]
		    (= result (delay-triple-add-stream input a b c)))))

(defn decode-wish [name arg]
  (case name
	'incrementer [(is-incrementer? arg) [(increment-stream default-input arg)]]
	'constant [(is-constant-x? arg) []]
	'delay-add [(is-delay-adder? arg) [(delay-add-stream default-input arg)]]
	'delay-double-add [(is-delay-double-adder? (first arg) (second arg)) [(delay-double-add-stream default-input (first arg) (second arg))]]
	'delay-triple-add [(apply is-delay-triple-adder? arg) [(apply delay-triple-add-stream default-input arg)]]))

(vsc-fn search-wish-with-prefix 3 [n prefix name arg]
	(let [[pred outputs] (decode-wish name arg)]
	  (search-circuits-with-prefix n outputs prefix pred)))

(defn vsc-pmap
  ([n f coll]
     (let [rets (map #(future (f %)) coll)
	   step (fn step [[x & xs :as vs] fs]
		  (lazy-seq
		   (if-let [s (seq fs)]
		     (cons (deref x) (step xs (rest s)))
		     (map deref vs))))]
       (step rets (drop n rets))))
  ([n f coll & colls]
     (let [step (fn step [cs]
		  (lazy-seq
		   (let [ss (map seq cs)]
		     (when (every? identity ss)
		       (cons (map first ss) (step (map rest ss)))))))]
       (vsc-pmap n #(apply f %) (step (cons coll colls))))))

(defn vsc-search [n prefix-len name arg]
  (let [prefixes (search-prefixes n prefix-len)]
    (apply concat (vsc-pmap (min 600 (count prefixes)) #(search-wish-with-prefix-vsc n % name arg) prefixes))))

(defn- search-delay-triple-adders-with-length [len combinations]
  (loop [combinations combinations
	 no-solutions []]
    (if (empty? combinations)
      no-solutions
      (let [combination (first combinations)
	    solutions (take 20 (vsc-search len 3 'delay-triple-add combination))]
	(println {:len len :combination combination :solutions solutions})
	(if (empty? solutions)
	  (recur (rest combinations) (conj no-solutions combination))
	  (recur (rest combinations) no-solutions))))))

(defn search-delay-triple-adders [min max]
  (loop [n min
	 combinations (all-inputs 3)]
    (when (<= n max)
      (let [no-solutions (search-delay-triple-adders-with-length n combinations)]
	(recur (inc n) no-solutions)))))
