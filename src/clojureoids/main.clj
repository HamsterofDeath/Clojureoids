(ns clojureoids.main
  (:use clojureoids.renderer clojureoids.logic clojureoids.factory clojureoids.math)
  (:import [clojureoids.javainterop MainFrame AdvanceCallback UIAccess]))

(defn advance-world-and-render [world user-input-atom uiaccess]
  (let [most-recent-world (atom world)]
    (reify AdvanceCallback
      (onTick [this target-image]
        (swap! user-input-atom (fn [discard] (.getMostRecentUserInput uiaccess)))
        (render-world @most-recent-world target-image)
        (let [old-world @most-recent-world
              old-game-elements (:game-elements old-world)
              advanced-game-elements
              (flatten
                (for [game-element old-game-elements]
                  (let [coll-or-single ((:advance-function game-element) game-element old-world)]
                    (if (coll? coll-or-single) coll-or-single [coll-or-single]))))]
          (swap! most-recent-world #(assoc % :game-elements advanced-game-elements)))))))

(let [user-input-atom (atom [])
      world (gen-demo-world 1 user-input-atom)
      uiaccess (MainFrame/createFrame field-width field-height)]
  (.initAdvanceCallback uiaccess (advance-world-and-render world user-input-atom uiaccess)))
