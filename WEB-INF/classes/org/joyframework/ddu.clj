; Copyright (c) Pengyu Yang. All rights reserved

(ns ^{:path "/"} org.joyframework.ddu
  (:require [org.joyframework.route :reload true :as route]
            [org.joyframework.result :reload true :as rs]
            [org.joyframework.resources :reload true :as res]
            [org.joyframework.servlet :reload true :as servlet]))

(route/defroutes rt org.joyframework.ddu)

(res/load-resources :res 'org.joyframework.ddu)

(defn index [] (rs/tiles "index"))

(defn logs
  ([] (rs/tiles "logs"))
  ([year month]
     (println "year:" year ", month:" month)
     )
  ([year]
     (println "year:" year)
     )
  )

(defn get-logs
  ([year])
  ([year month]))

(defn post-logs []

  )

(defn get-log [id])

(defn post-log [])

(defn get-logs []
  (let [year (servlet/param "year")
        month (servlet/param "month")]
    (println "year:" year ", month:" month)
    ) 
  )