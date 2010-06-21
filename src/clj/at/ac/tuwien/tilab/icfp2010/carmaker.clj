(ns at.ac.tuwien.tilab.icfp2010.charmaker
  (:use  at.ac.tuwien.tilab.icfp2010.chambaopti)
  (:use  at.ac.tuwien.tilab.icfp2010.fuel)
)

(defn liste-mal-listen [liste1 liste2]
  (apply + (map * liste1 liste2)))



(defn matrix-product [a b]
  (let [tb (apply map list b)]
    (if (empty? a)
      ()
      (cons
       (map liste-mal-listen (repeat (first a)) tb)     
       (matrix-product (rest a) b)))))



(defn matrix-hoch-n [m a]
  (loop [acc m 
	 a a]
    (if (= 1 a)
      acc
      (recur (matrix-product acc m) (dec a)))))



(defn oages-auto [i n j m]
  (vector {:upper (vector (list i n) (list j m))
	   :is-main false
	   :lower (apply vector (concat (take i (repeat (list 1 n))) (take j (repeat (list 1 m)))))
	   }
	  {:upper (vector (list 1 m) (list 1 n) (list 1 n))
	   :is-main true, 
	   :lower (vector (list 1 n) (list 1 m) (list 1 m))}
	  {:upper (apply vector (concat (take i (repeat (list 1 n))) (take j (repeat (list 1 m)))))
	   :is-main false
	   :lower (vector (list i n) (list j m))
	   }))


(defn oager-fuel [i n j m]
  (let [car (minimized-car (oages-auto i n j m))
	swapper ((meta car) :mapping)
	invmapping (into {} (map (fn [[x y]] [y x]) swapper))]
    (apply list (map #(matrix-hoch-n 
	   (second (invmapping %)) 
	   (first (invmapping %)))
	 (sort (keys invmapping))))))

(defn make-fuel [automobil]
  (let [car (minimized-car automobil)
	swapper ((meta car) :mapping)
	invmapping (into {} (map (fn [[x y]] [y x]) swapper))]
    (apply list (map #(matrix-hoch-n 
	   (second (invmapping %)) 
	   (first (invmapping %)))
	 (sort (keys invmapping))))))



(def some-a '((6 2 0 0)
	      (0 2 0 0)
	      (0 0 6 2)
	      (0 0 0 2)))

(def some-b '((1 1 0 0)
	      (0 1 0 0)
	      (0 0 1 1)
	      (0 0 0 1)))

(def links '((1 0 0 0)
	     (0 0 1 0)
	     (0 0 0 1)
	     (0 1 0 0)))

(def rechts '((1 0 0 0)
	      (0 0 0 1)
	      (0 1 0 0)
	      (0 0 1 0)))

(defn perm-matrix-mix [auto]
  (apply vector (apply list (map (fn [cham] 
	 {:upper (apply vector (interleave (cham :upper) (repeat (list 1 links)) (repeat (list 1 rechts)))),
	  :is-main (cham :is-main),
	  :lower (apply vector (interleave (cham :lower) (repeat (list 1 links)) (repeat (list 1 rechts))))
	  })
       auto))))




;; (beta-car (oages-auto 57 some-a 76 some-b))


(def liste '((23 73) (71 73) (41 31) (53 61) (42 7)
;	     (325 142) (99 56) (201 4) (277 53) (18 3)
	     ))

(def liste2 '((43 51) (53 122) (49 17) (85 100) (67 43)
	      (101 103) (111 87) (72 99) (76 98) (50 60)))

(def liste3 '((53 61) (57 22) (79 97) (83 14) (19 77)
	      (6 73) (91 7) (34 69) (75 35) (82 75)))

(defn make-some-cars [l]
     (loop [s "" 
	    l l]
       (if (empty? l)
	 s
	 (let [p (first l)
	       beta-car (oages-auto (first p) some-a (second p) some-b)]
	   (recur (str s "./build-and-submit-car.sh "
		       (thing-to-string (car-schani2biely (minimized-car (perm-matrix-mix beta-car)))) " "
		       (thing-to-string (make-fuel  (perm-matrix-mix beta-car)))
		       "\n\n") 
		  (rest l))))))



