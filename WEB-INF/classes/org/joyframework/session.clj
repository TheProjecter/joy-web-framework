;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.session
  (:require [org.joyframework.request :as req])
  (:import javax.servlet.http.HttpSession
           org.joyframework.TokenException))

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

(defn token
  ([] (let [tn (get "__jf_tk_name__") tv (get "__jf_tk_value__")]
        (remove "__jf_tk_name__" "__jf_tk_value__")
        (if-not (and tn tv (= (req/param tn) tv)) (throw (TokenException.)))
        ))
  ([h] (try (token) (catch TokenException _ (h))))
  )