;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.session
  (:import javax.servlet.http.HttpSession))

(def ^:dynamic *http-session*)

(defn set
  ([m] (doseq [[n v] m] (set n v)))
  ([name val] (.setAttribute *http-session* name val)))

(defn get [name] (.getAttribute *http-session* name))

(defn remove [name] (.removeAttribute *http-session* name))

(defn attr? [name] (not (nil? (get name))))

(defn cancel [] (.invalidate *http-session*))
