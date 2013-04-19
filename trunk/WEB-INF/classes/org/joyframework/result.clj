;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.result
  ;;(:use [org.joyframework servlet] )
  (:require [org.joyframework.session :as sess]
            [org.joyframework.request :as req]
            [org.joyframework.response :as resp]
            [org.joyframework.context :as ctxt]
            [org.joyframework.flash :as flash])
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
     (doseq [[n v] args] (flash/set n v))
     (resp/redirect url))
  )

(defn forward
  ([path] (forward path {}))
  ([path http-attrs]
     (req/set http-attrs)
     (.. req/*http-request*
         (getRequestDispatcher path)
         (forward req/*http-request* resp/*http-response*))) 
  )

(defn tiles
  ([id] (tiles id {}))
  ([id http-attrs]
     (let [args (object-array 2)]
       (aset args 0 req/*http-request*)
       (aset args 1 resp/*http-response*)
       (req/set http-attrs)
       (.render (ServletUtil/getContainer ctxt/*servlet-context*) id args)))
  )