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

(oages-auto 4 'A 11 'B)

(defn oager-fuel [i n j m]
  (let [car (minimized-car (oages-auto i n j m))
	swapper ((meta car) :mapping)
	invmapping (into {} (map (fn [[x y]] [y x]) swapper))]
    (apply list (map #(matrix-hoch-n 
	   (second (invmapping %)) 
	   (first (invmapping %)))
	 (sort (keys invmapping))))))

