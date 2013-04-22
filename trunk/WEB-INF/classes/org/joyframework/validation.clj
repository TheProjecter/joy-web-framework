;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.validation
  (:require [org.joyframework.result :as rs]
            [org.joyframework.request :as req]
            [org.joyframework.resources :as res]))

(def __jf_page_err__ "__jf_page_err__")

(def ^:dynamic *field-spec*)

(defn tiles [t]
  (fn [] (rs/tiles t req/*http-params*)) 
  )

(defn required []
  (let [{:keys [field-name field-label field-label-key key]
         :or {key "vali.required"}} *field-spec*
         label (or field-label
                   (res/get-message (or field-label-key field-name))
                   field-name)]
    (println "field-value :" (req/param field-name)
             "empty? :" (empty? (req/param field-name)))
    (if (empty? (req/param field-name))
      (res/get-message key label))
    ))

(defn length "min <= length < max" [{:keys [min max]}]

  )

(defn minlength [len] (length {:min len}))

(defn maxlength [len] (length {:max len}))

(defn rule [spec & vs]
  (fn [] (binding [*field-spec* spec]
           (reduce (fn [m f]
                     (if (empty? m)
                       (if-let [msg (f)]
                         [(or (:field-id spec) __jf_page_err__) msg])
                       m)) nil vs)
           )))

(defn put [m k val]
  (assoc m k (if-let [v (m k)]
               (conj (if (vector? v) v [v]) val) val)))

(defn with-rules [& rules]
  (reduce (fn [m rule] (if-let [[k msg] (rule)] (put m k msg) m)) {} rules)
  )