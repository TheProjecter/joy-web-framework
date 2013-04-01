; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.resources
  (:use [org.joyframework servlet])
  (:require [clojure.string :as str])
  (:import [java.util ResourceBundle MissingResourceException]
           [javax.servlet.jsp.jstl.fmt LocalizationContext]
           [java.text MessageFormat])
  )


(def RES "Global resources map." (hash-map))

(defn load-resource
  "Loads the given resource bundle. The base-name argument should
   be fully quailfied class name, like 'x.y.z'.
   The loaded ResourceBundle instance is returned."
  [base-name]
  (try
    (ResourceBundle/getBundle (str base-name))
    (catch MissingResourceException ex
      (ResourceBundle/getBundle (str base-name ".resources"))
      ))
  )

(defn- bundle-2-map [bundle]
  (loop [e (.getKeys bundle) rs {} ]
    (if (.hasMoreElements e)
      (let [k (.nextElement e) v (.getObject bundle k)]
        (recur e (assoc rs k v (str k "$format") (MessageFormat. v))))
      (assoc rs "$bundle" bundle "$lc" (LocalizationContext. bundle)))
    )
  )

(defn load-resources
  "Even number of args are required. The arguments with even indexes
   are key for the resource in the resource map; the odd arguments are
   the base names for the resource properties file.

   Examples: (load-resources res a.b.c labels a.b.c.labels).
   will change RES to be {:res {<map of a.b.c>} 
                          :labels {<map of a.b.c.labels>}}"
  [& args]
  (let [[k base-name & r] args
        bundle (load-resource base-name)
        m (bundle-2-map bundle)]
    (alter-var-root #'RES conj {k m}
                    (if r (apply load-resources r)))
    ;;(println "__RES__:" RES)
    )
  )

;; loads the framework resources under the name joy
(load-resources :joy 'org.joyframework)


(defn get-message [k & args]
  (let [[m fmt] (first (filter #(not (nil? %))
                         (map (fn [[_ v]]
                                (if-let [m (v k)]
                                  [m (v (str k "$format"))])) RES)
                         ))]
    ;;(println "fmt:" fmt ", m:" m)
    (if fmt (.format fmt (to-array args)) m)
    ))

(defn get-message-from [rk k & args]
  (if-let [m (RES rk)]
    (if-let [fmt (m (str k "$format"))]
      (.format fmt (to-array args))
      (m k))
    ))

(defn set-resources-into-request
  ""
  []
  (doseq [[k v] RES]
    ;;(println "k:" (str k) ", v:" v)
    (.setAttribute *http-request* (name k) v ;;(:map v)
                   ))
  )

