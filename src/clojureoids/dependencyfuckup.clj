(ns clojureoids.dependencyfuckup)

(defprotocol IRender
  (render [this render-target])
  (get-shape [this])
  (new-renderer [this shape stats])
  (get-transform [this])
  (get-reverse-transform [this])
  (get-raw-shape [this])
  (get-transformed-bounds [this]))

