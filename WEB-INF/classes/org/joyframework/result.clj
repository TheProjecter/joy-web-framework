;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.result
  (:require [org.joyframework.session :as sess]
            [org.joyframework.request :as req]
            [org.joyframework.response :as resp]
            [org.joyframework.context :as ctxt]
            [org.joyframework.flash :as flash]
            [clojure.data.json :as json])
  (:import org.apache.tiles.servlet.context.ServletUtil))

(defn ok
  ([msg] (ok msg "text/html"))
  ([msg content-type] (ok msg content-type {} 200))
  ([msg content-type headers sc]
       (doto resp/*http-response*
         (.setStatus sc)
         (.setContentType content-type)
         (.setContentLength (.length msg)))
       (resp/header headers)
       (doto (resp/writer)
         (.println msg)
         (.flush)
         (.close)))
  )

(defn json [data] (ok (json/write-str data) "application/json"))

(defn error 
  ([sc] (error sc ""))
  ([sc msg] (error sc msg {}))
  ([sc msg headers]
     (resp/header headers)
     (resp/error sc msg))
  )

(defn to-do
  ([] (to-do "To be implemented."))
  ([msg] (error 500 msg))
  )

(defn not-found
  ([] (not-found "Not found!!!"))
  ([msg] (error 404 msg)))

(defn redirect
  ([url] (redirect url nil))
  ([url args]
     ;;(println "args ==>" args)
     (flash/set args)
     (resp/redirect url))
  )

(defn forward
  ([path] (forward path {}))
  ([path http-attrs & x] (forward path (apply merge http-attrs x)))
  ([path http-attrs]
     (req/set http-attrs)
     (.forward (.getRequestDispatcher req/*http-request* path)
               req/*http-request* resp/*http-response*)) 
  )

(defn tiles
  ([id] (tiles id {}))
  ([id http-attrs & x] (tiles id (apply merge http-attrs x)))
  ([id http-attrs]
     (let [args (object-array 2)]
       (aset args 0 req/*http-request*)
       (aset args 1 resp/*http-response*)
       (req/set http-attrs)
       (.render (ServletUtil/getContainer ctxt/*servlet-context*) id args)))
  )

