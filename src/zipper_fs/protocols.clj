(ns zipper-fs.protocols)


(comment 
  ;; Basic structure of the zipper.
  ;; This is a Node because it is connected to multiple leafs. It is used for navigating.
  ;; :current is a Leaf which holds the value and any meta information required.
  ;; :left and :right are lists which hold items on the same level as :current. Can be lazy.
  ;; :down If :current is a container (a directory, list, vector, etc), you can step down into it.
  ;;  The [:down :current] is the selected item, and is typically the first item in the container.
  ;; Importantly (if building a custom zipper), Nodes must only have one reference to a Leaf. These are one sided relationships.
  ;; Down-nodes do not need to refer back to up-nodes. Similarly, :right nodes should not refer to their :left node.
  ;; In the Files directory
  {:up-node   {:current "Files" ,,, }
   :current   "pictures"
   :down-node {:current     "beach.jpg"
               :left-nodes  (list "cheese.jpg")
               :right-nodes (list "dog.jpg" "frog.jpg")}

   :left-nodes  '()
   :right-nodes '({:current "music" :down-node (list "jazz.mp3"
                                                     "rock.mp3")}
                  {:current "programs" :down-node (list "start.exe"
                                                        "worker.exe")})
   }

  )




(defn cursor-right
  "Will conj :current into :left-nodes and (first :right-nodes) becomes :current and :right-nodes is (rest :right-nodes)"
  [c]
  ;; TODO, will keep going right into nil land, stop it?
  (if (not (empty? (:right-nodes c)))
    (-> (first (:right-nodes c))
        (assoc :up-node (:up-node c))
        (assoc :left-nodes (conj (:left-nodes c) (-> c
                                                     (dissoc :right-nodes
                                                             :left-nodes
                                                             :up-node))))
        (assoc :right-nodes (rest (:right-nodes c))))))

(defn cursor-left
  "Will conj :current into :right-nodes and (last :left-nodes) becomes :current and :left-nodes is (rest :left-nodes)"
  [c]
  ;; TODO, will keep going into nil land, stop it?
  (if (not (empty? (:left-nodes c)))
    (-> (first (:left-nodes c))
        (assoc :up-node (:up-node c))
        (assoc :right-nodes (conj (:right-nodes c) (-> c
                                                     (dissoc :right-nodes
                                                             :left-nodes
                                                             :up-node))))
        (assoc :left-nodes (rest (:left-nodes c))))
    #_(-> c
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
  (-> {:up-node      {:current "Files" ,,, }
       :current "pictures"
       :down-node    {:current "beach.jpg"
                      :left-nodes    (list "cheese.jpg")
                      :right-nodes   (list "dog.jpg" "frog.jpg")}

       :left-nodes  '()
       :right-nodes '({:current "music" :down-node {:current "jazz.mp3"
                                                    :right-nodes '("rock.mp3")}}
                {:current "programs" :down-node (list "start.exe"
                                                      "worker.exe")})
       }
      
      ;;cursor-right
      ;;cursor-right
      cursor-right
      cursor-down
      cursor-up
      cursor-left
      cursor-down
      ;;cursor-left
      ;;cursor-left
      ;;cursor-left
      ;;cursor-up
      ;;cursor-down
      ;;cursor-left
      ;;cursor-right
      clojure.pprint/pprint
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
  (-> {:current    "c"
       :left-nodes '()}
      (insert-left "b")
      (insert-right "d")
      )
  
  )


;;--------------------------------------------------------------------------------

(defprotocol NodeProtocol 
  ""
  (up [this])
  (down [this])
  (left [this])
  (right [this])
  (value [this])
  (current [this])
  (inspect [this])
  )

(defrecord Leaf [value
                 properties])

(defrecord Node [current
                 left-nodes right-nodes
                 up-node down-node])


(def base-implementation
  {
   :inspect (fn [this] this)
   :current (fn [this] (:current this))
   :value   (fn [this] (:value (:current this)))
   :right   (fn [this] (cursor-right this))
   :left    (fn [this] (cursor-left this))
   :up      (fn [this] (cursor-up this))
   :down    (fn [this] (cursor-down this))
   })

(extend Node
  NodeProtocol
  base-implementation
  )

;; Same as
#_(extend-protocol NodeProtocol
  Node
  (inspect [this] this)
  (current [this] (:current this))
  (value   [this] (:value (:current this)))
  (right   [this] (cursor-right this))
  (left    [this] (cursor-left this))
  (up      [this]  (cursor-up this))
  (down    [this] (cursor-down this)))


