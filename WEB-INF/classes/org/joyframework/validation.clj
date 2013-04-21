;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.validation2
  (:require [org.joyframework.result :as rs]
            [org.joyframework.request :as req]
            [org.joyframework.resources :as res]))

(def __jf_page_err__ "__jf_page_err__")

(def ^:dynamic *field-spec*)

(defn tiles [t]
  (rs/tiles t req/*http-params*)
  )

(defn- required* [{:keys [field-name field-id field-label field-label-key key]
                   :or {key "vali.required"}}]
  (if (empty? (req/param field-name))
    (let [k (or field-id __jf_page_err__)
          msg (res/get-message key (or field-label field-name))]
      {k msg}
      )) 
  )

(defn required [])

(defn minlength [l])

(defn maxlength [l])

(defmacro rule [spec & vs])

(defmacro with-validation [f & rules])



