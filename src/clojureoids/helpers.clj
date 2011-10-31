(ns clojureoids.helpers)

(defmacro times [times & code]
  `(let [countdown# ~times]
     (loop [remaining# countdown#]
       (when (< 0 remaining#)
         ~@code
         (recur (dec remaining#))))))


