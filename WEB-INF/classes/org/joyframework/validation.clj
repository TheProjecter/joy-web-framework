;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.validation
  (:require [org.joyframework.result :as rs]
            [org.joyframework.request :as req]
            [org.joyframework.resources :as res]
            [clojure.string :as str]))

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
  ([tr]
     (let [{:keys [key] :or {key "vali.required"}} *field-spec*]
       (if (empty? (if tr *trimmed-value* *value*))
         (res/get-message key *field-label*))
       ))
  )

(defn- within [val min max k kmin kmax]
  (cond
   (and min max) (if (or (< val min) (>= val max))
                   (res/get-message k *field-label* min max))
   min (if (< val min) (res/get-message kmin *field-label* min))
   max (if (>= val max) (res/get-message kmax *field-label* max))
   ))

(defn length "min <= length < max"
  ([m] (length true m)) 
  ([tr {:keys [min max]}]
     (let [{:keys [k kmin kmax] :or {k "vali.length" kmin "vali.length.min"
                                     kmax "vali.length.max"}} *field-spec*]
       (within (count (if tr *trimmed-value* *value*)) min max k kmin kmax)))
  )

(defn minlength [len] (length {:min len}))

(defn maxlength [len] (length {:max len}))

(defn integer
  ([] (integer nil))
  ([{:keys [min max]}]
     (if-not (empty? *trimmed-value*)
       (let [{:keys [k kmin kmax]
              :or {k "vali.int" kmin "vali.min" kmax "vali.kmax"}} *field-spec*]
         (try (within (Integer/parseInt *trimmed-value*) min max k kmin kmax) 
              (catch NumberFormatException ex (res/get-message k *field-label*)))
         )))
  )

(defn double [{:keys [min max]}]

  )

(defn date [{:keys [format before after]}])

(defn email [])

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

(defn with-rules [params & rules]
  (let [errs (reduce (fn [m rule]
                       (if-let [[k msg] (rule)] (put m k msg) m)) {} rules)]
    (if (seq errs) (into errs params))))