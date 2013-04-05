; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.result
  (:use [org.joyframework servlet] )
  (:import org.apache.tiles.servlet.context.ServletUtil))

(defn ok
  ([msg] (ok msg "text/html"))

  ([msg content-type] (ok msg content-type {} 200))

  ([msg content-type headers sc]
     (let [writer (.getWriter *http-response*)]
       (doto *http-response*
         (.setStatus sc)
         (.setContentType content-type)
         (.setContentLength (.length msg)))
       (header-set headers)
       (doto writer
         (.println msg)
         (.flush)
         (.close))
       ))
  )

(defn error 
  ([sc] (error sc ""))

  ([sc msg] (error sc msg {}))

  ([sc msg headers]
     (header-set headers)
     (.sendError *http-response* sc msg))
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
     (doseq [[n v] args] (flash-set n v))
     (.sendRedirect *http-response* (.encodeURL *http-response* url)))
  )

(defn forward
  ([path] (forward path {}))

  ([path http-attrs]
     (doseq [[nm val] http-attrs] (.setAttribute *http-request* nm val))
     (.. *http-request*
         (getRequestDispatcher path)
         (forward *http-request* *http-response*))) 
  )

(defn tiles
  ([id] (tiles id {}))

  ([id http-attrs]
     (let [args (object-array 2)]
       (aset args 0 *http-request*)
       (aset args 1 *http-response*)
       (doseq [[nm val] http-attrs]
         (.setAttribute *http-request* nm val))
       (.render (ServletUtil/getContainer *servlet-context*) id args)))
  )