;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.request
  (:require [clojure.string :as str]
            [org.joyframework.util :as util])
  (:import org.apache.commons.fileupload.servlet.ServletFileUpload))

(def ^:dynamic *http-request*)
(def ^:dynamic *http-params*)

(defn multipart? [req] (ServletFileUpload/isMultipartContent req))

(defn params "Gets the HTTP request parameters map."
  [req]
  (let [params (into {} (.getParameterMap req)) 
        parts (if (multipart? req) (into {} (.getPartsMap req)))]
    (into {} (for [[k v] (merge params parts)]
               [k (if (== 1 (alength v)) (aget v 0) v)]))
    ))

(defn set
  ([m] (doseq [[n v] m] (set n v)))
  ([name val] (.setAttribute *http-request* name val) val))

(defn get [name] (.getAttribute *http-request* name))

(defn remove [name] (.removeAttribute *http-request* name))

(defn attr? [name] (not (nil? (get name))))

(defn param
  ([name] (*http-params* name))
  ([name val] (let [p (param name)] (if p p val)))
  ([f name & xs] (map (if f (comp f param) param) (cons name xs)))
  )

(defn path []
  (let [path-info (or (.getPathInfo *http-request*) "/")
        servlet-path (.getServletPath *http-request*)]
    [servlet-path
     (util/trim
      (if path-info path-info
          (let [i (.lastIndexOf servlet-path ".")]
            (if (== -1 i) servlet-path
                (.substring servlet-path 0 i)))) "/")
     (.getQueryString *http-request*)]
    ))

(defn header [name] (.getHeader *http-request* name))

(defn method [] (.getMethod *http-request*))

(defn reader [] (.getReader *http-request*))

(defn GET? [] (= "GET" (method)))

(defn POST? [] (= "POST" (method)))

