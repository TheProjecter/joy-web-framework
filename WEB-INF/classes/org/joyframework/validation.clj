;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.validation
  (:require [org.joyframework.result :as rs]
            [org.joyframework.request :as req]
            [org.joyframework.resources :as res]
            [clojure.string :as str]))

(def __jf_page_err__ "__jf_page_err__")

(def ^:dynamic *field-spec*)

(def ^:dynamic *field-label*)

(def ^:dynamic *field-value*)

(defn tiles [t]
  (fn [] (rs/tiles t req/*http-params*)) 
  )

(defn required
  ([] (required true))
  ([tr]
     (let [{:keys [key] :or {key "vali.required"}} *field-spec*]
       (if (empty? (if tr (str/trim *field-value*) *field-value*))
         (res/get-message key *field-label*))
       ))
  )

(defn- between [l {:keys [min max]}])

(defn length "min <= length < max"
  ([m] (length true m)) 
  ([tr {:keys [min max]}]
     (let [{k :key :or {k "vali.length"}} *field-spec*
           l (count (if tr (str/trim *field-value*) *field-value*))]
       
       )
     )
  )

(defn minlength [len] (length {:min len}))

(defn maxlength [len] (length {:max len}))

(defn integer [{:keys [min max]}]

  )

(defn double [{:keys [min max]}]

  )

(defn date [{:keys [format before after]}])

(defn email [])

(defn regex [patt])

(defn rule [spec & vs]
  (let [fname (spec :field-name)]
    (fn []
      (binding [*field-spec* spec
                *field-label* (or (spec :field-label)
                                  (res/get-message
                                   (or (spec :field-label-key) fname)) fname)
                *field-value* (req/param fname)]
        (reduce (fn [m f]
                  (if (empty? m)
                    (if-let [msg (f)]
                      [(or (:field-id spec) __jf_page_err__) msg])
                    m)) nil vs)
        ))
    )
  )

(defn put [m k val]
  (assoc m k (if-let [v (m k)]
               (conj (if (vector? v) v [v]) val) val)))

(defn with-rules [params & rules]
  (let [errs (reduce (fn [m rule] (if-let [[k msg] (rule)] (put m k msg) m)) {} rules)]
    (if (seq errs)
      (into errs params))
    ))