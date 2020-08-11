(ns zipper-fs.records
  "Termonology:
  Node: A zipper structure with :left-nodes, :right-nodes, :up-node, :down-node.
  Leaf: A container for a value. It doesn't know what it's neighbors are. Only knows of itself."
  (:require [zipper-fs.protocols :as p]
            [me.raynes.fs :as fs]))

;; (defrecord DirectoryNode [path offset])

;; (extend-protocol p/NodeProtocol 
;;   DirectoryNode
;;   (inspect [this] this)

;;   (value [this] (nth (fs/list-dir (:path this)) (:offset this)))
;;   (right [this] (try (nth (fs/list-dir (:path this)) (inc (:offset this)))
;;                      (DirectoryNode. (:path this) 
;;                                      (inc (:offset this)))
;;                      (catch IndexOutOfBoundsException e nil)))

;;   (left [this] (if (> (:offset this) 0)
;;                  (DirectoryNode. (:path this) 
;;                                  (dec (:offset this)))))

;;   (up [this] (DirectoryNode. 
;;                (fs/parent (:path this))
;;                (.indexOf (fs/list-dir (fs/parent (:path this))) (:path this))))

;;   (down [this] (if (fs/directory? (p/value this))
;;                  (DirectoryNode. (p/value this) 0)
;;                  nil)))

;; ;; [1 [2.1 2.2] 3]
;; (comment
;;   (fs/base-name (fs/file fs/*cwd*))
;;   (.indexOf (fs/list-dir (fs/parent fs/*cwd*)) fs/*cwd*)
;;   (nth (fs/list-dir fs/*cwd*) 200)
;;   (->> (DirectoryNode. fs/*cwd* 0)
;;        p/right
;;        p/right
;;        p/right
;;        p/down
;;        p/up
;;        p/inspect
;;        )
;; )





;;(defrecord PropertiesNode [up-node properties])

(comment 
  ;;Given [a b c d e]
  ;; Cursor on "c"
  {:v "c"
   :r {:v "d"
       :r {:v "e"
           :d {:v "e2"}}}
   :l {:v :b
       :l {:v "a"}}}
  
  ;; Move Right to "d"
  {:v "d"
   :d {:v "d2"}
   :r {:v "e"
       :d {:v "e2"}}
   :l {:v "c"
       :l {:v :b
           :l {:v "a"}}}}

  
  ;; Using a list
  {:current "c"
   :left    '("b" "a")
   :right   '("d" "e")
   :up      nil}

  )

(defn cursor-right
  ""
  [c]
  ;; TODO, will keep going right into nil land, stop it?
  (if (not (empty? (:right-nodes c)))
    (-> c
        (update :left-nodes conj (:current c))
        (assoc :current (first (:right-nodes c)))
        (assoc :right-nodes (rest (:right-nodes c))))))

(defn cursor-left
  ""
  [c]
  ;; TODO, will keep going into nil land, stop it?
  (if (not (empty? (:left-nodes c)))
    (-> c
        (update :right-nodes conj (:current c))
        (assoc :current (first (:left-nodes c)))
        (assoc :left-nodes (rest (:left-nodes c))))))

(defn cursor-up
  ""
  [c]
  (if (:up-node c)
    (-> (:up-node c)
        ;;(update :right-nodes conj (:current c))
        ;;(assoc :current (first (:left-nodes c)))
        ;;(assoc :left-nodes (rest (:left-nodes c)))
        (assoc :down-node (dissoc c :up-node)) ;; same as c but without up
        )))

(defn cursor-down
  ""
  [c]
  (if (:down-node c)
    (-> (:down-node c)
        ;;(update :right-nodes conj (:current c))
        ;;(assoc :current (first (:left-nodes c)))
        ;;(assoc :left-nodes (rest (:left-nodes c)))
        (assoc :up-node (dissoc c :down-node))
        )))

(comment
  (-> {:current "c"
       :left-nodes    '("b" "a")
       :right-nodes   '("d" "e")
       :up-node      {:current 3
                 :left-nodes    '(2 1 0)
                 :right-nodes   '(4 5)
                 :up-node      nil}
       :down-node nil}
      
      ;;cursor-right
      ;;cursor-right
      ;;cursor-right
      ;;cursor-left
      ;;cursor-left
      cursor-left
      cursor-up
      cursor-left
      cursor-right
      cursor-down
      )
  
  
  )


(defn insert-left
  "Inserts item."
  [c c2]
  (update c :left-nodes conj c2))

(defn insert-right
  "Inserts item, and moves cursor to the item just inserted."
  [c c2]
  (update c :right-nodes conj c2)
  )

(defn assoc-down
  "Sets the  :down-node"
  [c c2]
  (assoc c :down-node c2))


(comment
  (-> {:current "c"
       :left-nodes '()}
      (insert-left "b")
      (insert-right "d")
      )
  
  )



#_(def branch-node-implementation
  {:right (fn [this] (BranchNode. ()
                                  properties))
   :left  (fn [this] )
   :up  (fn [this] )
   :down  (fn [this] )
   :inspect  (fn [this] this)
   :value  (fn [this] )
   })


(defrecord Leaf [value
                 properties])

(defrecord Node [current
                 left-nodes right-nodes
                 up-node down-node])


(extend-protocol p/NodeProtocol
  Node
  (inspect [this] this)
  (current [this] (:current this))
  (value [this] (:value (:current this)))
  (right [this] (cursor-right this))
  (left [this] (cursor-left this))
  (up [this]  (cursor-up this))
  (down [this] (cursor-down this)))


(comment

  (def n (Node. (Leaf. "a" {})
                   (list )
                   (list)
                   nil
                   nil))

  (-> n
      ;;p/current
      (insert-right (Leaf. "b" {}))
      ;;p/right
      ;;(insert-right (Node. (Leaf. "(,,,)" {}) (list) (list) nil (Leaf. "c1" {}) ))
      (insert-right (Leaf. "(,,,)" {})) ;;(Node.  (list) (list) nil (Leaf. "c1" {}) ))
      p/right
      (assoc-down (Node. (Leaf. "c1" {})
                         (list ) (list)
                         nil nil ))
      p/down
      p/current
      ;;p/down
      clojure.pprint/pprint
      )

  

  {:current #_StringNode {:value "c"
                          :properties #_PropertyNode {:last-modified 100
                                                      :user "Tyler"
                                                      ;; :length 1  defined on the StringNode protocol
                                                      }}
   :left    '("b" "a")
   :right   '("d" "e")
   :up      nil
   :down    {:current #_StringNode {:value      "c1"
                                    :properties #_PropertyNode {:last-modified 100
                                                                :user          "Tyler"
                                                                ;; :length 1  defined on the StringNode protocol
                                                                }}
             :left    '()
             :right   '("c2"
                        {:current "c3" :down {,,,}}
                        ,,,)
             }}
  
  


  ;; Down goes in to the map
  ;; Left and right over the keys
  ;; down into the (get :offset) value
  ;; might be either a value or a
  (defrecord HashmapNode [map offset])
  (extend-protocol p/NodeProtocol 
    HashmapNode
    (inspect [this] this)

    (value [this] (:dmap this))
    (right [this] (try (nth (fs/list-dir (:path this)) (inc (:offset this)))
                       (DirectoryNode. (:path this) 
                                       (inc (:offset this)))
                       (catch IndexOutOfBoundsException e nil)))

    (left [this] (if (> (:offset this) 0)
                   (DirectoryNode. (:path this) 
                                   (dec (:offset this)))))

    (up [this] (:up-node this))

    (down [this] (get (:dmap this)
                      (nth (keys (:dmap this)) (:offset this)))))


  )






;;================================================================================


(defrecord FsLeaf [value])

(defrecord FsNode [current
                   left-nodes right-nodes
                   up-node down-node])


(declare make-dir-leafs)
(declare make-node-leaf)

(extend-protocol p/NodeProtocol
  FsNode
  (inspect [this] this)
  (current [this] (:current this))
  (value [this] (:value (:current this)))
  (right [this] (cursor-right this))
  (left [this] (cursor-left this))
  (up [this]
    (if (:up-node this)
      (map->FsNode (cursor-up this));; already exists, use that.
      ;; need to load into memory
      (let [f (:value (:current this))
            p (fs/parent f)]
        (println "Parent " p)
        (if (and p
                 (fs/directory? p))
          ;; todo, need to add the leafs, and insert current
          (-> (make-node-leaf p)
              (assoc :down-node (map->FsNode this))
              ;;cursor-up
              ;;map->FsNode
              )
          #_(let [p (fs/parent f)
                leafs (make-dir-leafs )]
            
            (FsNode. (first leafs)
                     (list);;left
                     (rest leafs);;right
                     (map->FsNode this);; up
                     nil;;down
                     ))


          )))
    
    
    ;;(cursor-up this)
    ;;(make-fs-node (fs/parent (fs/parent (:value (:current this)))))
    ;;(fs/parent (:value (:current this)))
    )
  (down [this]
    (if (:down-node this)
      (map->FsNode (cursor-down this));; already have it
      (let [f (:value (:current this)) ]
        (if (fs/directory? f)
          (let [leafs  (make-dir-leafs f)]
            (FsNode. (first leafs)
                     (list);;left
                     (rest leafs);;right
                     (map->FsNode this);; up
                     nil;;down
                     ))


            )))))


(defn make-dir-leafs
  ""
  [dir-path]
  (println "Getting ls dir for " (str dir-path))
  (let [dir-f  (fs/file dir-path)
        leafs  (map #(FsLeaf. %)
                    (fs/list-dir dir-f))]
    leafs
   ))

(defn make-node-leaf
  "Takes a path, which is a file or directory, and builds the left and rights,
  and sets the current to the path's corresponding FsLeaf"
  [path]
  (let [dir-leafs                         (make-dir-leafs (fs/parent path))
        [left-leafs cur-leaf right-leafs] (loop [lvs   dir-leafs
                                                 lefts (list)]
                                            (if (or (nil? lvs)
                                                    (= path
                                                       (:value (first lvs))))
                                              [lefts (first lvs) (rest lvs)]
                                              (recur (next lvs) (conj lefts (first lvs)))))]
    (FsNode. cur-leaf
             left-leafs
             right-leafs
             nil
             nil)))




(comment
  ;; (fs/file fs/*cwd*)
  (-> ;;(make-dir-node fs/*cwd*)
    ;;(FsNode. (FsLeaf. (fs/file fs/*cwd*) ) (list) (list) nil nil)
    (make-node-leaf fs/*cwd*)
    p/down
    p/right
    p/right
    p/down
    p/down
    p/right
    

    ;; can go up and wont list director again!
    p/up
    p/up
    p/down
    p/down
    )

  (-> ;;(FsNode. (FsLeaf. (fs/file fs/*cwd*) ) (list) (list) nil nil)
      (make-node-leaf fs/*cwd*);
      ;;p/up
      ;;p/right
      ;;p/down
      ;;p/right
      ;;(clojure.pprint/pprint )
      p/current
    )

  )





