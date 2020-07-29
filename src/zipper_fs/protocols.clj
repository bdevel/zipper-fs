(ns zipper-fs.protocols)

;; [1 2 [3.0 3.1] 4 [5.0 [5.1 5.2 5.3]]]???
;; {:folder "" :contents [{:file_name ""} 
;;{:file_name ""} 
;; {:folder "" :contents [{:file_name ""} ]}
;;

(defprotocol NodeProtocol 
  ""
  (up [this])
  (down [this])
  (left [this])
  (right [this])
  (value [this])
  (inspect [this])
  (get-nth [this n]))
;;(take-while [1 [2.1 2.2] 3])

(declare right-fn)
(defrecord Node 
    [branch-items
     offset]

  NodeProtocol
  (inspect [this] [(:value-node this)
                   (:value-node (:up-node this))
                   (:value-node (:down-node this))
                   (:value-node (:left-node this))
                   (:value-node (:right-node this))])
  (value [this] (:value-node this))
  (right [this] (right-fn this))
  (left [this] (->Node (:value-node (:left-node this)) 
                       (:up-node (:left-node this))
                       (:down-node (:left-node this))
                       (:left-node (:left-node this))
                       this))
  (up [this] (->Node (:value-node (:up-node this)) 
                     (:up-node (:up-node this))          
                     (loop [l (:left-node this)
                            last-l nil]
                       (if l
                         (recur (:left-node l) l)
                         last-l))
                     (:left-node (:up-node this))
                     (:right-node (:up-node this))))
  (down [this] (->Node (:value-node (:down-node this)) 
                       this
                       (:down-node (:down-node this))
                       (:left-node (:down-node this))
                       (:right-node (:down-node this)))))

(defn right-fn [this]
  (->Node (:value-node (:right-node this)) 
          (:up-node (:right-node this))
          (:down-node (:right-node this))
          this
          (:right-node (:right-node this))))


;; [1, [2.1,2,2], 3]

(defn build-branch
  ""
  [original-items]
  (loop [items original-items
         previous-node nil
         nodes []]
    (if (empty? items)
      (first (map-indexed (fn [i, n] (assoc n :right-node (nth nodes (inc i) nil))) nodes))
      (let [i (first items)
            n (Node. i nil nil previous-node nil)]
        (recur (next items) n (conj nodes n))))))
(defn build-lazy-branch
  ""
  [original-items]
  (let [xf (map #(Node. % nil nil nil))
        f (fn [acc n])
        #_(loop [items         original-items
                       previous-node nil
                       nodes         []]
                  (if (empty? items)
                    (first (map-indexed (fn [i, n] (assoc n :right-node (nth nodes (inc i) nil))) nodes))
                    (let [i (first items)
                          n (Node. i nil nil previous-node nil)]
                      (recur (next items) n (conj nodes n)))))])


  )

(comment
  ;; [1 2 3 4]
  (->> [1 2 3 4]
       (build-branch)
       (clojure.pprint/pprint))
  (def right-sample-1 (Node. 1 nil nil nil
                           (Node. 2 nil nil 1
                                  (Node. 3 nil nil 2
                                         (Node. 4 nil nil 3 nil)))))
  (def right-sample-1 (let [n1  (Node. 1 nil nil nil nil)
                            n2  (Node. 2 nil nil n1 nil)
                            n3  (Node. 3 nil nil n2 nil)
                            n4  (Node. 4 nil nil n3 nil)
                            _ (println (:right-node n4) n4)
                            nn3 (assoc n3 :right-node n4)
                            _ (clojure.pprint/pprint nn3)
                            nn2 (assoc n2 :right-node nn3)
                            nn1 (assoc n1 :right-node nn2)]
                        nn1))
  
  (-> right-sample-1
      right
      left
      left
      down
      up
      value)
  
  (clojure.pprint/pprint (right right-sample-2))

  ;;(take-while [1 [2.1 2.2] 3])
  (last (take-while (comp not nil?) [1 2 3 nil]))

  (def sample-2 (let [n1 (Node. 1 nil nil nil nil)
                      
                      n2 (Node. [] nil nil n1 nil)
                      n2d (Node. 2.1 n2 nil nil nil)
                      n2dr (Node. 2.2 n2 nil n2d nil)
                      nn2d (assoc n2d :right-node n2dr)
                      _ (println "nn2d" (inspect nn2d))
                      n3  (Node. 3 nil nil n2 nil)
                      nn2 (assoc n2 :right-node n3 :down-node nn2d)
                      _ (println "nn2" (inspect nn2))
                      nn1 (assoc n1 :right-node nn2)]
                  nn1))
  (clojure.pprint/pprint (-> sample-2
                             right
                             down
                             right
                             up
                             ;;:down-node
                             inspect
                             ))
)


