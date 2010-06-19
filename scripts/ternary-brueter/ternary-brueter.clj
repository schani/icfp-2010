; (println 3r120)

(defn fetch-url[address]
   (with-open [stream (.openStream (java.net.URL. address))]
     (let  [buf (java.io.BufferedReader. 
                 (java.io.InputStreamReader. stream))]
       (apply str (line-seq buf)))))

; (fetch-url "http://google.com")

(defn ternary-str [i]
  (. Integer toString i 3))

(def fwriter (java.io.FileWriter. "brueter-out.txt"))
(map #(do (.write fwriter %) (.write fwriter "\n")) (take 100 (map ternary-str (iterate inc 0))))
(.close fwriter)

(defn process-range [from to]
  (let [fwriter (java.io.FileWriter. "brueter-out.txt")]
    (dorun 
     (map #(do (.write fwriter %) (.write fwriter "\n")) 
	  (take (- to from) (map ternary-str (iterate inc from)))))
    (.close fwriter)))

; (process-range 0 100000000)

  