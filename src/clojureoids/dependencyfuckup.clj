(ns clojureoids.dependencyfuckup)

(defprotocol IRender
  (render [this render-target])
  (get-shape [this])
  (new-renderer [this shape stats])
  (get-transform [this])
  (get-transformed-bounds [this]))

