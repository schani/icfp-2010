(ns at.ac.tuwien.tilab.icfp2010.perms)

(defn- heap [res a n]
  (if (= n 1)
    (conj res (into [] a))
    (loop [i 0
	   res res]
      (if (= i n)
	res
	(let [res (heap res a (dec n))]
	  (if (= (mod n 2) 1)
	    (let [x (aget a (dec n))
		  y (aget a 0)]
	      (aset a 0 x)
	      (aset a (dec n) y))
	    (let [x (aget a (dec n))
		  y (aget a i)]
	      (aset a i x)
	      (aset a (dec n) y)))
	  (recur (inc i) res))))))

	  )))

(defn all-permutations [n]
  (heap [] (into-array (range n)) n))
