(ns zipper-fs.records
  "Termonology:
  Node: A zipper structure with :left-nodes, :right-nodes, :up-node, :down-node.
  Leaf: A container for a value. It doesn't know what it's neighbors are. Only knows of itself."
  (:require [zipper-fs.protocols :as p]
            [me.raynes.fs :as fs]))

;;(defrecord PropertiesNode [up-node properties])

(comment 
  ;; Basic structure of the zipper.
  ;; This is a Node because it is connected to multiple leafs. It is used for navigating.
  ;; :current is a Leaf which holds the value and any meta information required.
  ;; :left and :right are lists which hold items on the same level as :current. Can be lazy.
  ;; :down If :current is a container (a directory, list, vector, etc), you can step down into it.
  ;;  The [:down :current] is the selected item, and is typically the first item in the container.
  ;; Importantly (if building a custom zipper), Nodes must only have one reference to a Leaf. These are one sided relationships.
  ;; Down-nodes do not need to refer back to up-nodes. Similarly, :right nodes should not refer to their :left node.
  {:current "pictures"
   :left    '("documents" "music")
   :right   '("programs" "videos")
   :down      {:current "beach.jpg"
               :left (list "cheese.jpg")
               :right (list "dog.jpg" "frog.jpg")}})

(defn cursor-right
  "Will conj :current into :left-nodes and (first :right-nodes) becomes :current and :right-nodes is (rest :right-nodes)"
  [c]
  ;; TODO, will keep going right into nil land, stop it?
  (if (not (empty? (:right-nodes c)))
    (-> c
        (update :left-nodes conj (:current c))
        (assoc :current (first (:right-nodes c)))
        (assoc :right-nodes (rest (:right-nodes c))))))

(defn cursor-left
  "Will conj :current into :right-nodes and (first :left-nodes) becomes :current and :left-nodes is (rest :left-nodes)"
  [c]
  ;; TODO, will keep going into nil land, stop it?
  (if (not (empty? (:left-nodes c)))
    (-> c
        (update :right-nodes conj (:current c))
        (assoc :current (first (:left-nodes c)))
        (assoc :left-nodes (rest (:left-nodes c))))))

(defn cursor-up
  ":current becomes :up-node and :down-node becomes the (dissoc c :up-node) because the :down-node doesn't need a reference to the :up-node."
  [c]
  (if (:up-node c)
    (-> (:up-node c)
        ;;(update :right-nodes conj (:current c))
        ;;(assoc :current (first (:left-nodes c)))
        ;;(assoc :left-nodes (rest (:left-nodes c)))
        (assoc :down-node (dissoc c :up-node)) ;; same as c but without up
        )))

(defn cursor-down
  ":down-node becomes :current. :up-node becomes old current without a :down-node"
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
  ""
  [c c2]
  (update c :right-nodes conj c2)
  )

(defn assoc-down
  "Sets the :down-node"
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



(defn dir-leafs
  "Returns a list of fs objects wrapped in a FsLeaf record. Note, the parent or :up-node is not returned."
  [dir-path]
  (println "Getting ls dir for " (str dir-path))
  (let [dir-f (fs/file dir-path)
        leafs (map #(FsLeaf. %)
                   (fs/list-dir dir-f))]
    leafs))

(defn make-fs-node
  "Takes a path, which is a file or directory, and builds the left and rights (by listing the parent directory),
  and sets the current to the path's corresponding FsLeaf."
  [path]
  (let [[left-leafs cur-leaf right-leafs] (loop [lvs   (dir-leafs (fs/parent path))
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
        (if (and p
                 (fs/directory? p))
          (-> (make-fs-node p)
              (assoc :down-node (map->FsNode this)))))))
  (down [this]
    (if (:down-node this)
      (map->FsNode (cursor-down this));; already have it
      (let [f (:value (:current this)) ]
        (if (fs/directory? f)
          (let [leafs  (dir-leafs f)]
            (FsNode. (first leafs)
                     (list);;left
                     (rest leafs);;right
                     (map->FsNode this);; up
                     nil;;down
                     )))))))


(comment
  (->(make-fs-node fs/*cwd*)
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
      (make-fs-node fs/*cwd*);
      p/up
      ;;p/right
      ;;p/down
      ;;p/right
      ;;(clojure.pprint/pprint )
      ;;p/current
    )

  )





;; Original implementation works, but not as good
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
