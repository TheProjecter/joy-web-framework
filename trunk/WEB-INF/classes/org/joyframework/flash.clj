;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.flash
  (:require [org.joyframework.request :as req]
            [org.joyframework.session :as sess]))

(def ^:private FLASH_SCOPE_KEY "__flash_ky__")

(defn reinstate
  "Move flash scoped parames into the request and clear the session."
  []
  (req/set (sess/get FLASH_SCOPE_KEY))
  (sess/remove FLASH_SCOPE_KEY))

(defn set ""
  ([attrs]
     (doseq [[n v] attrs] (set n v)))
  ([name val]
     (let [fs (or (sess/get FLASH_SCOPE_KEY) {})]
       (sess/set FLASH_SCOPE_KEY (assoc fs name val))))
  )



