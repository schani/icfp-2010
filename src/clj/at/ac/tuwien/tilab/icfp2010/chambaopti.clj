(ns at.ac.tuwien.tilab.icfp2010.chambaopti
  (:use  at.ac.tuwien.tilab.icfp2010.perms))

(defn perm-car [car]
  (let [mixit (fn [indexs]
		(map #(car %) indexs))]
    (map mixit (all-permutations (count car)))))

; transforms contents of ar using the mapping, new indizes used starting from nextindex
(defn transform-pipe [ar mapping nextindex]
  (loop [result ()
	 todo ar
	 mapping mapping
	 nextindex nextindex]
    (if (empty? todo)
      [(reverse result) mapping nextindex]
      (if (contains? mapping (first todo))
	(recur (cons (get mapping (first todo)) result) (rest todo) mapping nextindex)
	(recur (cons nextindex result) (rest todo) (assoc mapping (first todo) nextindex) (inc nextindex))))))

(defn transform-chamber [chamber mapping nextindex]
  (let [[upper mapping nextindex] (transform-pipe (:upper chamber) mapping nextindex)
	[lower mapping nextindex] (transform-pipe (:lower chamber) mapping nextindex)]
    [{:upper upper, :is-main (:is-main chamber), :lower lower} mapping nextindex]))
  

(defn transform-car [car]
  (loop [result ()
	 todo car
	 mapping {}
	 nextindex 0]
    (if (empty? todo)
      [(reverse result) mapping]
      (let [[car2 mapping nextindex] (transform-chamber (first todo) mapping nextindex)]
	(recur (cons car2 result) (rest todo) mapping nextindex)))))

(defn compare-pipes [pipe1 pipe2]
  (loop [p1 pipe1
	 p2 pipe2]
    (if (empty? p1)
      (if (empty? p2)
	'=
	'<)
      (if (empty? p2)
	'>
	(if (= (first p1) (first p2))
	  (recur (rest p1) (rest p2))
	  (if (< (first p1) (first p2))
	    '<
	    '>))))))

(defn compare-chambers [chamber1 chamber2]
  (let [upper (compare-pipes (:upper chamber1) (:upper chamber2))]
    (if (= upper '=)
      (if (= (:is-main chamber1) (:is-main chamber2))
	(compare-pipes (:upper chamber1) (:upper chamber2))
	(if (< (:is-main chamber1) (:is-main chamber2))
	  '<
	  '>))
      upper)))

(defn compare-cars [car1 car2]
  (loop [car1 car1
	 car2 car2]
    (if (empty? car1)
      (if (empty? car2)
	'=
	'<)
      (if (empty? car2)
	'>
	(let [rc (compare-chambers (first car1) (first car2))]
	  (if (= rc '=)
	    (recur (rest car1) (rest car2))
	    rc))))))

(defn car-smaller [car1 car2]
  (if (= '< (compare-cars car1 car2))
    true
    false))

(def some-car [{:upper ['a 'b 'c 'b], :is-main true, :lower ['b 'c 'a]}
	       {:upper ['a 'b 'c], :is-main true, :lower ['b 'c 'a 'd]}
	       {:upper ['f 'a], :is-main true, :lower ['b 'c]}])

(defn car-biely2schani [car]
  (map #({:upper (first %) :is-main (if (= (second %) 0) false true) :lower (second (rest %))}) car))

; (compare-cars some-car some-car)
; (perm-car some-car)

; (sort car-smaller (map transform-car (perm-car some-car)))

; (transform-pipe ['a 'b 'a 'a 'c 'b 'a] {} 0)

; (transform-chamber {:upper ['a 'b 'c 'b], :is-main true, :lower ['b 'c 'a]} {} 0)

; (transform-car some-car)

; (compare-pipes [1 1 1] [1 1])

(defn saubua [x]
  {:upper (x 0) :is-main (if (= (x 1) 0) true false) :lower (x 2)})

(defn car-biely2schani [car]
  (map saubua car))

(defn bielyfizierer [chamber]
  [(:upper chamber) (if (= (:is-main chamber) true) 0 1) (:lower chamber)])

(defn car-schani2biely [car]
  (apply list (map bielyfizierer car)))
