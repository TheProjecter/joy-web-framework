;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.datetime
  (:require [clj-time.format :as ctf]
            [clj-time.core :as ctc])
  (:import (org.joda.time.format DateTimeFormat DateTimeFormatter ISODateTimeFormat)
           (org.joda.time DateTime))
  )

(def formatters
  (into {"yyyyMMdd" (ISODateTimeFormat/basicDate) 
         "yyyy/MM/dd" (DateTimeFormat/forPattern "yyyy/MM/dd")
         "yyyy-MM-dd" (ISODateTimeFormat/date)}
        ctf/formatters)
  )

(defn parse* [h #^DateTimeFormatter fmtr #^String s]
  (try (.parseDateTime fmtr s) (catch Exception e
                         (cond (= :throw h) (throw e)
                               (fn? h) (h e)
                               :else h)))
  )

(defn parse-ex
  ([h #^String s]
     (first (for [fmtr (vals formatters)
                  :let [d (parse* h fmtr s)]
                  :while d] d)))
  ([h #^String s #^String k] (parse* h (formatters k) s))
  )

(defn parse
  ([#^String s] (parse-ex nil s))
  ([#^String s #^String k]
     (if (vector? k)
       (first (for [ky k :let [d (parse-ex nil s ky)] :when d] d))
       (parse-ex nil s k))))

(defn today []
  (let [d (ctc/today)] [(ctc/year d) (ctc/month d) (ctc/day d)]))

(defn before? [#^DateTime this #^DateTime that]
  (println "before? this:" this ", that:" that)
  (ctc/before? this that))

(defn after? [#^DateTime this #^DateTime that]
  (println "after? this:" this ", that:" that)
  (ctc/after? this that))