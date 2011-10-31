(ns clojureoids.random
  (:import java.util.Random))

(def my-random-gen (new Random))
(defn random-double [] (.nextDouble my-random-gen))
(defn random-double-smaller-than [max-value] (* max-value (random-double)))
(defn random-boolean [chance] (<= (num (random-double)) chance))
(defn random-int-in-range [minInclusive maxExclusive]
  (let [diff (- maxExclusive minInclusive)
        randomInt (.nextInt my-random-gen diff)]
    (+ randomInt minInclusive)))
