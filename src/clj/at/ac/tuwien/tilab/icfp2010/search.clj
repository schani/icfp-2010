(ns at.ac.tuwien.tilab.icfp2010.search
  (:use at.ac.tuwien.tilab.icfp2010.circuit)
  (:import [at.ac.tuwien.tilab.icfp2010 Permuter IPermutationConsumer Simulator
	    SimulationPermutationConsumer SearchSimulationConsumer ISimulationConsumer]))

(defn- int-arr [a]
  (into-array (. Integer TYPE) a))

(defn search-circuits [n]
  (let [print-simulation-consumer (reify
				   ISimulationConsumer
				   (consumeSimulation [this n circuit input-index input-stream output-stream]
						      (println {:circuit (doall (seq circuit)) :output (doall (seq output-stream))})))
	search-simulation-consumer (SearchSimulationConsumer. (into-array [(int-arr the-key)]) print-simulation-consumer)
	simulation-permutation-consumer (SimulationPermutationConsumer. n (int-arr default-input) search-simulation-consumer)]
    (Permuter/permuteRange (inc (* n 2)) simulation-permutation-consumer)))
