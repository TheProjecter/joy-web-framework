;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.session
  (:import javax.servlet.http.HttpSession))

(def ^:dynamic *http-session*)

(defn set
  ([m] (doseq [[n v] m] (set n v)))
  ([name val] (.setAttribute *http-session* name val) val)
  )

(defn get
  ([name] (.getAttribute *http-session* name))
  ([name val] (or (get name) val))
  )

(defn remove [name & x]
  (doseq [n (cons name x)] (.removeAttribute *http-session* n)))

(defn attr? [name] (not (nil? (get name))))

(defn cancel [] (.invalidate *http-session*))
