(ns zipper-fs.records
  "Termonology:
  Node: A zipper structure with :left-nodes, :right-nodes, :up-node, :down-node.
  Leaf: A container for a value. It doesn't know what it's neighbors are. Only knows of itself."
  (:require [zipper-fs.protocols :as p]
            [me.raynes.fs :as fs]))


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
  
  (if-let [parent-f (fs/parent path)]
    (let [[cur-leaf left-leafs right-leafs] (loop [lvs   (dir-leafs parent-f)
                                                   lefts (list)]
                                              (if (or (nil? lvs)
                                                      (= path
                                                         (:value (first lvs))))
                                                [(first lvs) lefts (rest lvs)]
                                                (recur (next lvs) (conj lefts (first lvs)))))]
           (FsNode. cur-leaf
                    left-leafs
                    right-leafs
                    nil
                    nil))
    ;; no parent-f, so must be root /
    (FsNode. (FsLeaf. (fs/file path))
             (list);;left
             (list) ;;right
             nil;; up
             nil)))

(extend FsNode
  p/NodeProtocol
  (-> p/base-implementation
      (assoc :up (fn [this]
                   (if (:up-node this)
                     (map->FsNode (p/cursor-up this));; already exists, use that.
                     ;; need to load into memory
                     (let [f (:value (:current this))
                           p (fs/parent f)]
                       (if (and p
                                (fs/directory? p))
                         (-> (make-fs-node p)
                             (assoc :down-node (map->FsNode this))))))))
      (assoc :down (fn [this]
                     (if (:down-node this)
                       (map->FsNode (p/cursor-down this));; already have it
                       (let [f (:value (:current this)) ]
                         (if (fs/directory? f)
                           (let [leafs (dir-leafs f)]
                             (FsNode. (first leafs)
                                      (list);;left
                                      (rest leafs);;right
                                      (map->FsNode this);; up
                                      nil;;down
                                      )))))))))



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

  (-> (make-fs-node fs/*cwd*);
      p/up
      p/up
      p/up
      p/up
      p/up
      ;;p/right
      ;;p/down
      ;;p/right
      ;;(clojure.pprint/pprint )
      p/value
      )

  (-> (make-fs-node "/")
      p/down
      p/right
      p/right
      p/right
      p/right
    p/current
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
