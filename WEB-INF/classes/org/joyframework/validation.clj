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

(defn- required* [{:keys [field-name field-id field-label field-label-key key]
                   :or {key "vali.required"}}]
  (if (empty? (req/param field-name))
    (let [k (or field-id __jf_page_err__)
          msg (res/get-message key (or field-label field-name))]
      {k msg}
      )) 
  )

(defn required [] "required")

(defn minlength [len] "minlength")

(defn maxlength [len] "maxlength")

(defn rule [spec & vs]
  (fn []
    (binding [*field-spec* spec]
      (reduce (fn [m f]
                (if (empty? m)
                  (if-let [msg (f)]
                    (assoc m (or (:field-id spec) __jf_page_err__) msg))
                  m)) {} vs)
      )
    )
  )

(defn put [m k val]
  (assoc m k (if-let [v (m k)]
               (conj (if (vector? v) v [v]) val) val)))

;;(defmacro with-rules [f & rules] `(reduce (fn [m# rule#] (let [[k# msg#] (rule#)] (put m# k# msg#))) {} [~@rules]))

(defn with-rules [f & rules]
  (reduce (fn [m rule] (let [[k msg] (rule)] (put m k msg))) {} rules)
  )






