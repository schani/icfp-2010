(ns at.ac.tuwien.tilab.icfp2010.car2graphviz
  (:use  at.ac.tuwien.tilab.icfp2010.ternary))

(defn graphviz-from-chamber [index chamber]
  (let [fuel-colors ["magenta", "orange", "cyan", "red", "green", "blue"]
	output-section 
	(fn [sindex pipe section]
	  (str 
	   (if (> sindex 0)
	     (str "  subgraph cluster_Pipe" index " { style=filled; color=lightgrey; subgraph cluster_" pipe "_Pipe" index " { section_" pipe index "_" (dec sindex) " -- section_" pipe index "_" sindex " }}\n")
	     "")
	   "  fuel_" section " -- section_" pipe index "_" sindex " [color=\"" (nth fuel-colors section) "\"]\n"
	   "  fuel_" section " [style=filled,color=\"" (nth fuel-colors section) "\"]\n"))]
    (str
;     "  air -- upper_pipe_" index "\n"
;     "  air -- lower_pipe_" index "\n"
     "  subgraph cluster_Pipe" index " { subgraph cluster_U_Pipe" index " { upper_pipe_" index " -- section_U" index "_0 }}\n"
     "  subgraph cluster_Pipe" index " { subgraph cluster_L_Pipe" index " { lower_pipe_" index " -- section_L" index "_0 }}\n"
     (apply str (map-indexed #(output-section %1 "L" %2) (:lower chamber)))
     (apply str (map-indexed #(output-section %1 "U" %2) (:upper chamber))))))

(defn graphviz-from-car [car]
  (str
   "graph gates {\n"
   (apply str (map-indexed graphviz-from-chamber (first (rest car))))
   "}\n"))

(defn graphviz-file-from-car [filename car]
  (let [fwriter (java.io.FileWriter. filename)]
    (.write fwriter (graphviz-from-car car))
    (.close fwriter)))
	

;; (graphviz-file-from-car "/tmp/car.dot" (parse-car "221022000022010112201010022001122011110220010"))
;; (graphviz-file-from-car "/tmp/car.dot" (parse-car "22121010220101122012111022010102202200022000102210010102210101010102202200022000221022000220002200002210121112"))
;; (graphviz-file-from-car "/tmp/car.dot"
;; 			(parse-car "2202200001102201010010"))
