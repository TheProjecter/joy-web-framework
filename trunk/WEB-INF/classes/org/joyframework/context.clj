;; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.context)

(def ^:dynamic *servlet-context*)

(defn path [] (.getContextPath *servlet-context*))

(defn real-path [p] (.getRealPath *servlet-context* p))
