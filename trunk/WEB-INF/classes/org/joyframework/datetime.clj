;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.datetime
  (:require [clj-time.format :as cf])
  (:import (org.joda.time.format DateTimeFormat))
  )

(defn parse
  ([#^String s] (cf/parse s))
  ([#^String fmt #^String s] (cf/parse fmt s))
  )
