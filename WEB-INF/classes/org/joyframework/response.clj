;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.response)

(def ^:dynamic *http-response*)

(defn writer [] (.getWriter *http-response*))

(defn error [sc msg] (.sendError *http-response* sc msg))

(defn redirect [url]
  (println "url ==>" url)
  (.sendRedirect *http-response* (.encodeURL *http-response* url)))

(defn header
  ([hs] (doseq [[n v] hs] (header n v)))
  ([name val]
     (condp instance? val
       Long (.setDateHeader *http-response* name val)
       Integer (.setIntHeader *http-response* name val)
       (.setHeader *http-response* name val))
     ))