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
    (let [prefix (some (fn [pre] (if (. string startsWith pre) pre false)) (drop 1 prefixes))
	  remain (. string substring (count prefix))
	  prefix-index (first (some (fn [x] (if (= (second x) prefix) x nil)) (map list (range) prefixes)))]
      (. (new Double
	  (+ (Integer/valueOf remain 3)
	     (/ (dec (pow 3 prefix-index)) 2)))
	  intValue)
	 )))

(defn- eat [input num]
  (if (< (count input) num)
    (throw (Exception. (str "input " (apply str input) " should be at least " num " long")))
    (drop num input)))

(defn consume [input expected]
  (if (.startsWith (apply str input) expected)
    (drop (count expected) input)
    (throw (Exception. (str "expected " expected " but got " (apply str input))))))

(defn int-pow [x y]
  (apply * (repeat y x)))

(defn len-offset [m]
  (/ (dec (int-pow 3 m)) 2))

(defn invert-len-offset [x]
  (loop [i 1]
    (if (< x (len-offset i))
      (dec i)
      (recur (inc i)))))

(defn trinarify-number [num length]
  (loop [digits ()
	 num num
	 length length]
    (if (zero? length)
      (apply str digits)
      (recur (cons (mod num 3) digits)
	     (/ (- num (mod num 3)) 3)
	     (dec length)))))

(defn encode-number [input]
  (case input
	0 "0"
	1 "10"
	(str "22"
	     (encode-number (- (invert-len-offset input) 2))
	     (trinarify-number (- input (len-offset (invert-len-offset input)))
			       (invert-len-offset input)))))

(defn parse-number [input]
  (case (first input)
	\0 [(rest input) 0]
	\1 (case (second input)
		 \0 [(eat input 2) 1]
		 \1 [(eat input 2) 2]
		 \2 [(eat input 2) 3])
	\2 (let [input (consume input "22")
		 [input len] (parse-number input)
		 len (+ len 2)
		 str (apply str (take len input))
		 input (eat input len)]
	     [input (+ (len-offset len) (java.math.BigInteger. str 3))])
	(throw (Exception. (str "illegal input " (apply str input))))))

(defn parse-list [input parse-elem]
  (case (first input)
	\0 [(rest input) []]
	\1 (let [[input elem] (parse-elem (rest input))]
	     [input [elem]])
	\2 (let [input (consume input "22")
		 [input len] (parse-number input)
		 len (+ len 2)]
	     (loop [input input
		    elems []
		    i 0]
	       (if (= i len)
		 [input elems]
		 (let [[input elem] (parse-elem input)]
		   (recur input
			  (conj elems elem)
			  (inc i))))))
	(throw (Exception. (str "illegal input " (apply str input))))))

(defn parse-chamber [input]
  (let [[input upper] (parse-list input parse-number)
	[input is-aux] (parse-number input)
	[input lower] (parse-list input parse-number)]
    [input {:upper upper :is-main (zero? is-aux) :lower lower}]))

(defn parse-car [input]
  (parse-list input parse-chamber))
