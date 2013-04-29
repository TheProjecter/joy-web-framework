;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.config
  (:require [org.joyframework.validation :as vali])
  (:import (java.util.regex Pattern Matcher))
  )

(defn config* [v val]
  (alter-var-root v (fn [x]
                      (cond (fn? val) (val)
                            :else val)
                      )))

(defn config [& xs]
  (let [conf (apply hash-map xs)
        email-pattern (:email-pattern conf)
        date-format (:date-format conf)]
    (cond email-pattern (config* #'vali/__jf_email_pattern__
                                 #(Pattern/compile email-pattern))
          date-format (config* #'vali/__jf_date_format__ date-format)
          )
    ))
