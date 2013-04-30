;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.config
  (:require [org.joyframework.validation :as vali]
            [org.joyframework.route :as rt])
  (:import (java.util.regex Pattern Matcher))
  )

(defmacro set* [v val]
  (if val `(alter-var-root (var ~v) (fn [_#] ~val)))
  )

(defn set [& xs]
  (let [conf (apply hash-map xs)
        ep (:email-pattern conf)
        email-pattern (if ep (Pattern/compile ep))
        ]
    (set* rt/__jf_routes__ (:routes conf))
    (set* vali/__jf_date_format__ (:date-format conf))
    (set* vali/__jf_email_pattern__ email-pattern)
    ))
