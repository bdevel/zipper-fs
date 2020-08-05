(ns zipper-fs.records
  (:require [zipper-fs.protocols :as p]
            [me.raynes.fs :as fs]))

(defrecord DirectoryNode [path offset])

(extend-protocol p/NodeProtocol 
  DirectoryNode
  (inspect [this] this)

  (value [this] (nth (fs/list-dir (:path this)) (:offset this)))
  (right [this] (try (nth (fs/list-dir (:path this)) (inc (:offset this)))
                     (DirectoryNode. (:path this) 
                                     (inc (:offset this)))
                     (catch IndexOutOfBoundsException e nil)))

  (left [this] (if (> (:offset this) 0)
                 (DirectoryNode. (:path this) 
                                 (dec (:offset this)))))

  (up [this] (DirectoryNode. 
               (fs/parent (:path this))
               (.indexOf (fs/list-dir (fs/parent (:path this))) (:path this))))

  (down [this] (if (fs/directory? (p/value this))
                 (DirectoryNode. (p/value this) 0)
                 nil)))

;; [1 [2.1 2.2] 3]
(comment
  (fs/base-name (fs/file fs/*cwd*))
  (.indexOf (fs/list-dir (fs/parent fs/*cwd*)) fs/*cwd*)
  (nth (fs/list-dir fs/*cwd*) 200)
  (->> (DirectoryNode. fs/*cwd* 0)
       p/right
       p/right
       p/right
       p/down
       p/up
       p/inspect
       )
)
