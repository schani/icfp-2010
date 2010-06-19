(ns at.ac.tuwien.tilab.icfp2010.ternary
  (:use clojure.contrib.generic.math-functions)
  )

(def prefixes
     ["", "1", "220", "2210", "2211", "2212", 
      "2222000", "2222001", "2222002", "2222010", "2222011", "2222012", "2222020", "2222021", "02222022",
      "222210000", "222210001", "222210002", "222210010", "222210011", "222210012", "222210020", "222210021", "222210022",
      "222210100", "222210101", "222210102", "222210110", "222210111", "222210120", "222210121", "222210122", "222210200",
      "222210201", "222210202", "222210210", "222210211", "222210212", "222210220", "222210221", "222210222"])


(defn number-of-real-digits [n]
  (. 
   (new Double 
	(floor (/ (log (+ (* 2 n) 1)) (log 3)))) 
   intValue)
  )

(defn ternary-prefix [n]
  (nth prefixes (number-of-real-digits n)))

(defn pad-leading-zeros [string zeros]
  (str (apply str (repeat (- zeros (count string)) "0")) string))


(defn ternary-offset [n]
  (.
   (new Double
	(/ 
	 (dec (pow 3  (number-of-real-digits n))) 
	 2))
   intValue)
  )

(defn positive-integer-to-string-with-radix [n radix]
  (if (= n 0)
    "0"
    (loop [n n, string ""]
      (if (= n 0)
	string
	(let [digit (mod n radix)]
	  (recur 
	   (/ (- n digit) radix)
	   (str digit string)))))))


(defn number-to-ternary [n]
  (str (ternary-prefix n) (pad-leading-zeros (positive-integer-to-string-with-radix (- n (ternary-offset n)) 3) (number-of-real-digits n) )))

(defn ternary-to-number [string]
  (if (= string "0")
    0
    (let [prefix (some (fn [pre] (if (. string startsWith pre) pre false)) (drop 1 prefixes)), remain (. string substring (count prefix))
	  prefix-index (first (some (fn [x] (if (= (second x) prefix) x nil)) (map list (range) prefixes))) ]
      (. (new Double
	  (+ (Integer/valueOf remain 3)
	     (/ (dec (pow 3 prefix-index)) 2)))
	  intValue)
	 )))



