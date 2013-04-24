;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.util
  (:import java.math.BigDecimal))

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

(defn- to-number [f y]
  (try (f)
       (catch NumberFormatException ex
         (cond (nil? y) (throw ex) (fn? y) (y) :else y))
       ))

(defn to-int
  ([x] (to-int x nil))
  ([x y] (to-number #(Integer/parseInt x) y))
  )

(defn to-double
  ([x] (to-double x nil))
  ([x y] (to-number #(Double/parseDouble x) y))
  )

(defn to-decimal
  ([x] (to-decimal x nil))
  ([x y] (to-number #(BigDecimal. x) y))
  )

(def ^:dynamic *__jf_debug__* false)

(defn debug [t & args] (if *__jf_debug__* (apply printf t args)))