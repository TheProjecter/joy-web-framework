                                        ; Copyright (c) Pengyu Yang. All rights reserved

(ns ^{:path "/"} org.joyframework.ddu
    (:require [org.joyframework.route :reload true :as route]
              [org.joyframework.result :reload true :as rs]
              [org.joyframework.resources :reload true :as res]
              [org.joyframework.servlet :reload true :as servlet]
              [org.joyframework.db :reload true :as db]
              [clj-time.core :as dt]
              [clojure.java.jdbc :as sql]))

(route/defroutes rt org.joyframework.ddu)

(res/load-resources :res 'org.joyframework.ddu)

(db/defds ds {:driver "org.hsqldb.jdbc.JDBCDriver"
              :subprotocol "hsqldb"
              :subname "hsql://localhost/ddu"
              :user "SA"})

(defn index [] (rs/tiles "index"))

(defn- get-logs [{year :year month :month}]
  (println (.getConnection ds))
  )

(defn- get-logs-created-in [year month]
  (sql/with-connection {:datasource ds}
    (sql/with-query-results res
      ["select * from logs where year = ? and month = ?" year month]
;;      (println res)
      (rs/tiles "logs" {"logs"
                        
                        (map #(reduce (fn [[k1 v1] [k2 v2]]
                                        ;; (conj {(name k1) v1} {(name k2) v2})
                                        (println k1 "==>" v1 ","
                                                 k2 "==>" v2)
                                        ) %) res)

                        })
      )
    ))

(defn- get-logs-created-between [{starty :year startm :month}
                                 {endy :year endm :month}]
  )

(defn GET-logs
  ([] (println "GET-logs") (rs/tiles "logs"))
  ([year]
     (get-logs-created-in year (dt/month (dt/today))))
  ([year month]
     (get-logs-created-in year month))
  )

(defn POST-logs []
  (println "yes, this is POST-logs.")
  )

(defn GET-log [id]
  (rs/tiles "log" {"log" {"id" id "title" "title1"}})
  )

(defn POST-log []

  )