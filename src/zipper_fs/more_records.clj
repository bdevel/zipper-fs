(ns zipper-fs.more-records
  (:require [zipper-fs.protocols :as p]))

(defrecord SeqNode [current
                     left-nodes
                     right-nodes
                     up-node
                     down-node])

(extend SeqNode
  p/NodeProtocol
  (-> p/base-implementation
      (assoc :down (fn [this]
                     (if (:down-node this)
                       (p/cursor-down this)
                       (-> this
                         :current
                         :value
                         p/build
                         (assoc :up-node (assoc this :down-node nil))))))))

(defn make-list-node
  ""
  [l]
  (println "Building" l)
  (let [nodes (map #(SeqNode. (p/->Entity %) nil nil nil nil) l)]
    (->SeqNode (:current (first nodes)) (list) (rest nodes) nil nil)))


(extend-protocol p/NodeProtocol
  clojure.lang.PersistentList
  (build [this] (make-list-node this))
  clojure.lang.PersistentVector
  (build [this] (make-list-node this))
  java.lang.String
  (build [this] (make-list-node (seq this)))
  clojure.lang.PersistentHashSet
  (build [this] (make-list-node (seq this))))



(comment
  
  (-> (p/build '(1 2 (3.0 3.1) [4.0, 4.1] "hey you" #{"setv1" "setv2"}))
   
      p/right
      ;;(assoc :left-nodes nil)
      p/right
      p/right
      p/down
      p/right
      p/up
      ;;p/down
      ;;:up-node
      ;;p/up
      ;; p/left
      ;; p/left
      ;; p/down
      ;; p/right
      ;; p/up
      ;;p/down
      (clojure.pprint/pprint)
      ;;p/current
      )
)
