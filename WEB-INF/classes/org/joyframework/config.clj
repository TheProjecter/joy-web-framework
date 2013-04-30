;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.config
  (:require [org.joyframework.validation :as vali]
            [org.joyframework.route :as rt])
  (:import (java.util.regex Pattern Matcher))
  )

(defmacro set* [v val]
  `(alter-var-root (var ~v) (fn [_#] ~val)))

(defn set [& xs]
  (let [conf (apply hash-map xs) ep (:email-pattern conf)]
    (set* rt/__jf_routes__ (:routes conf))
    (set* vali/__jf_date_format__ (:date-format conf))
    (if ep (set* vali/__jf_email_pattern__ (Pattern/compile ep)))
    ))
