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
      (doseq [rec res]
        (println rec)
        )
      )
    )
  (rs/tiles "logs" {"logs" [{"id" 1 "title" "title1"}
                            {"id" 2 "title" "title2"}]})
  )

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