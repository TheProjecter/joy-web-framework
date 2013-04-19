; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.servlet
  (:require [clojure.string :as str]
            ))

;;(def ^:dynamic *http-method*)
;;(def ^:dynamic *http-request*)
;;(def ^:dynamic *http-response*)
;;(def ^:dynamic *http-params*)
;;(def ^:dynamic *http-suffix*)
;;(def ^:dynamic *http-session*)

;;(defn params
;;  "Gets the HTTP request parameters map."
;;  [request]
;;  (let [params (.getParameterMap request)]
;;    (into {} (for [[k v] params]
;;               [k (if (== 1 (alength v)) (aget v 0) v)]))
;;    ))

;;(defn servlet-context-attr
;;  (
;;   [name]
;;     "Gets the value of the named attribute from ServletContext."
;;     (.getAttribute *servlet-context* name))
;;
;;  (
;;   [name value]
;;     "Sets the value of the named attribute in ServletContext."
;;     (.setAttribute *servlet-context* name value))
;;  )

;;(defn session-attr
;;  ([name] ""
;;     (.getAttribute *http-session* name))
;;
;;  ([name value] ""
;;     (.setAttribute *http-session* name value)))
;;
;;(defn header
;;  ([name] "" (.getHeader *http-request*))
;;
;;  ([name val] ""
;;     (condp instance? val
;;       Long (.setDateHeader *http-response* val)
;;       Integer (.setIntHeader *http-response* val)
;;       (.setHeader *http-response* val))))

;;(defn param
;;  "Retrieves the value HTTP request parameter by the given name." 
;;  ([name] (*http-params* name))
;;  ([name val]
;;     (let [p (*http-params* name)]
;;       (if (< 0 (count p)) p val)
;;       )))

;;(defn GET? [] (= "GET" *http-method*))

;;(defn POST? [] (= "POST" *http-method*))

;;(defn session-set
;;  "Sets the value into the session scope under the given name."
;;  ([attrs] (doseq [[n v] attrs] (session-set n v)))
;;  ([name val] (.setAttribute *http-session* name val))
;;  )
;;
;;(defn session-get
;;  "Gets the value from the session scope under the given name."
;;  [name] (.getAttribute *http-session* name))
;;
;;(defn session-invalidate []
;;  (.invalidate *http-session*))
;;
;;(defn request-set "Sets the value into the HTTP request attributes."
;;  ([attrs] (doseq [[n v] attrs] (if v (request-set n v))))
;;  ([name val] (.setAttribute *http-request* name val))
;;  )
;;
;;(defn request-get "Gets the value from the HTTP request scope."
;;  [name] (.getAttribute *http-request* name))


;;(defn flash-get "" [name])

;;(defn header-get ""
;;  [name] (.getHeader *http-request* name))
;;
;;(defn header-set
;;  ""
;;  ([headers] (doseq [[n v] headers] (header-set n v)))
;;  ;;(map #(header-set (key %) (val %)) headers)
;;  ([name val]
;;     (condp instance? val
;;       Long (.setDateHeader *http-response* name val)
;;       Integer (.setIntHeader *http-response* name val)
;;       (.setHeader *http-response* name val))) 
;;  )

