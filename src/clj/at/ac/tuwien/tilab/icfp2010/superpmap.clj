(ns at.ac.tuwien.tilab.icfp2010.superpmap)

(defn- partition-with-pred [pred coll]
  (let [res (group-by pred coll)]
    [(res true) (res false)]))

(defn superpmap [n f coll]
  (let [futures (map #(future (f %)) coll)]
    (loop [futures futures
	   results []]
      (if (empty? futures)
	results
	(let [[finished working] (partition-with-pred future-done? futures)]
	  (doseq [f finished]
	    (println @f))
	  (Thread/sleep 500)
	  (recur working (concat (map deref finished) results)))))))
