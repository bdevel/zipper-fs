(ns zipper-fs.protocols-test
  (:require [clojure.test :refer :all]
            [zipper-fs.core :refer :all]
            [zipper-fs.protocols :as p]))

(def empty-tree {:current "a"})

(def tree {:current     "pictures"
           :left-nodes  '("documents" "music")
           :right-nodes '("programs" "videos")
           :down-node   {:current     "beach.jpg"
                         :left-nodes  (list "cheese.jpg")
                         :right-nodes (list "dog.jpg" "frog.jpg")}
           :up-node     {:current     "my folders"
                         :left-nodes  (list "my files")
                         :right-nddes (list "my workspace")}})

(deftest cursor-test
  (testing "Cursor navigation"
    (is (=    (-> tree
                  p/cursor-left
                  p/cursor-right
                  p/cursor-up
                  p/cursor-down
                  )
              {:current     "pictures",
               :left-nodes  ("documents" "music"),
               :right-nodes ("programs" "videos"),
               :down-node
               {:current     "beach.jpg",
                :left-nodes  ("cheese.jpg"),
                :right-nodes ("dog.jpg" "frog.jpg")}}))))

(deftest insert-nodes
  (testing "Insert Nodes"
    (is (= {:current "a"
            :right-nodes '("b")} 
          (-> empty-tree
              (p/insert-right "b"))))))
