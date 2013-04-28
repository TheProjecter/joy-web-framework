;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.validation
  (:require [org.joyframework.result :as rs]
            [org.joyframework.request :as req]
            [org.joyframework.resources :as res]
            [org.joyframework.util :as u]
            [org.joyframework.datetime :as dt]
            [clojure.string :as str])
  (:import (org.joda.time DateTime)
           (java.util Date Calendar)
           (java.util.regex Matcher Pattern))
  )

(def __jf_page_err__ "__jf_page_err__")

(def ^:dynamic *field-spec*)

(def ^:dynamic *field-label*)

(def ^:dynamic *value*)
(def ^:dynamic *trimmed-value*)

(defn tiles [t]
  (fn [] (rs/tiles t req/*http-params*)) 
  )

(defn required
  ([] (required true))
  ([tr?]
     (let [{:keys [key] :or {key "vali.required"}} *field-spec*]
       (if (empty? (if tr? *trimmed-value* *value*))
         (res/get-message key *field-label*))
       ))
  )

(defn- within [val min max k kmin kmax]
  ;;(println "val ==>" val)
  (cond
   (and min max) (if (or (< val min) (>= val max))
                   (res/get-message k *field-label* min max))
   min (if (< val min) (res/get-message kmin *field-label* min))
   max (if (>= val max) (res/get-message kmax *field-label* max))
   ))

(defn length "min <= length < max"
  ([] (length true))
  ([tr?]
     (let [{:keys [k kmin kmax max min]
            :or {k "vali.length" kmin "vali.length.min"
                 kmax "vali.length.max"}} *field-spec*]
       (within (count (if tr? *trimmed-value* *value*)) min max k kmin kmax)))
  )

(defn- check-number [f ky]
  (if-not (empty? *trimmed-value*)
    (let [n (f *trimmed-value* (res/get-message ky *field-label*))]
      (if (not (number? n)) n
          (let [{:keys [k kmin kmax min max]
                 :or {k "vali.between" kmin "vali.min"
                      kmax "vali.kmax"}} *field-spec*]
            (within n min max k kmin kmax))
          ))
    ))

(defn integer [] (check-number u/to-int "vali.int"))

(defn double [] (check-number u/to-double "vali.decimal"))

(defn decimal [] (check-number u/to-decimal "vali.decimal"))

(defn option [])

(defn options [])

(def __jf_date_format__ ["yyyy-MM-dd" "yyyy/MM/dd" "yyyyMMdd"])

(defn- datetime [x]
  (cond (fn? x) (x) (= :now x) (DateTime.) :else (DateTime. x)))

(defn date []
  (let [{:keys [key formats after before key-before key-after]
         :or {key "vali.date" formats __jf_date_format__
              key-before "vali.date.before"
              key-after "vali.date.after"}} *field-spec*]
    (if-let [d (dt/parse *trimmed-value* formats)]
      (cond
       (and before (dt/after? d (datetime before)))
         (res/get-message key-before *field-label*
                          (if (= :now before) "now" before))
       (and after (dt/before? d (datetime after)))
         (res/get-message key-after *field-label*
                          (if (= :now after) "now" after)))
      (res/get-message key *field-label* formats)) 
    ))


(def __jf_email_pattern__
  (Pattern/compile (str "^['_a-z0-9-\\+](\\.['_a-z0-9-\\+])*@"
                        "[a-z0-9-](\\.[a-z0-9-])*\\."
                        "([a-z]{2}|aero|arpa|asia|biz|com|coop|edu|gov|"
                        "info|int|jobs|mil|mobi|museum|name|nato|net|"
                        "org|pro|tel|travel|xxx)$")))

(defn email
  ([] (email true))
  ([tr?]
     (let [{:keys [key] :or {key "vali.email"}} *field-spec*
           val (if tr? *trimmed-value* *value*)]
       (if-not (.matches (.matcher __jf_email_pattern__ val))
         (res/get-message key *field-label*))))
  )

(defn regex [patt])

(defn rule [spec & vs]
  (let [fname (spec :field-name) val (req/param fname)]
    ;;(println "fname ==>" fname ", val ==>" val)
    (fn []
      (binding [*field-spec* spec
                *field-label* (or (spec :field-label)
                                  (res/get-message
                                   (or (spec :field-label-key) fname)) fname)
                *value* val *trimmed-value* (if val (str/trim val))]
        (reduce (fn [m f]
                  (if (empty? m)
                    (if-let [msg (f)]
                      [(or (:field-id spec) __jf_page_err__) msg])
                    m)) nil vs)
        ))
    ))

(defn- put [m k val]
  (assoc m k (if-let [v (m k)]
               (conj (if (vector? v) v [v]) val) val)))

(defn with-rules [& rules]
  (let [err (reduce (fn [m rule]
                      (if-let [[k msg] (rule)] (put m k msg) m))
                    {} (filter #(fn? %) rules))]
    (if (seq err)
      (apply merge err (filter #(map? %) rules)))
    ))