;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.util
  (:require [clojure.string :as str])
  (:import java.math.BigDecimal
           (java.text SimpleDateFormat ParseException) 
           ))

(defn trim [s tr]
  (if (or (nil? s) (nil? tr) (= s tr)) s
      (let [start-with (.startsWith s tr) end-with (.endsWith s tr)]
        (if (or start-with end-with)
          (let [strl (.length s) trl (.length tr)]
            (.substring s (if start-with trl 0) (if end-with (- strl trl) strl)))
          s))
      ))

(defn split
  ([s re] (split s re 0))
  ([s re limit] (if s (str/split s re limit)))
  )

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

