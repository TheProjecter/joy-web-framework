;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.util)

(defn trim-slashes "Trims slashes from both ends of the given string argument."
  [s]
  (when s
    (if (= "/" s) s
        (let [b1 (.startsWith s "/") b2 (.endsWith s "/")]
          (if (or b1 b2)
            (let [l (.length s)]
              (.substring s (if b1 1 0) (if b2 (- l 1) l))) s)))
    ))

(defn array? [obj] (and obj (.isArray (class obj))))