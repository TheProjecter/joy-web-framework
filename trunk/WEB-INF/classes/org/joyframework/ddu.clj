;; Copyright (c) Pengyu Yang. All rights reserved

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

(defn- rs-to-map [rs]
  (reduce (fn [r [k v]] (conj r {(name k) v})) {} rs))

(defn- get-logs-created-in [year month]
  (sql/with-connection {:datasource ds}
    (sql/with-query-results res
      ["select * from logs where year = ? and month = ?" year month]
      (rs/tiles "logs" {"logs" (map #(rs-to-map %) res)
                        "year" year "month" month})
      ))
  )

(defn- get-logs-created-between [{starty :year startm :month}
                                 {endy :year endm :month}])

(defn GET-logs
  ([] (let [today (dt/today)]
        (get-logs-created-in (dt/year today) (dt/month today))))
  ([year]
     (get-logs-created-in year (dt/month (dt/today))))
  ([year month]
     (get-logs-created-in year month))
  )

(defn POST-logs []
  (get-logs-created-in (servlet/param "year") (servlet/param "month"))
  )

(defn- get-log [id target]
  (sql/with-connection {:datasource ds}
    (sql/with-query-results res ["select * from logs where id = ?" id]
      ;;(if (= 0 (count res)))
      (rs/tiles target {"log" (rs-to-map (first res)) "id" id})
      )))

(defn GET-log [id]
  (try
    (if (<= (Integer/parseInt id) 0)
      (rs/tiles "log-edit" {"id" 0}) (get-log id "log"))
    (catch Exception ex)
    )
  )

(defn- next-id []
  (sql/with-connection {:datasource ds}
    (sql/with-query-results [m] ["call next value for seq"]
      (first (vals m))
      )))

(defn POST-log [id]
  (let [title (servlet/param "title") content (servlet/param "content")]
    (try
      (sql/with-connection {:datasource ds}
        (if (<= (Integer/parseInt id) 0)
          (let [nid (next-id) today (dt/today) year (dt/year today)
                month (dt/month today) date (dt/day today)]
            (sql/insert-record "logs" {:id nid :title title :content content
                                       :year year :month month :date date})
            (GET-log nid))
          (do
            (sql/update-values "log" ["id" id] {:title title :content content})
            (GET-log id))))
      (catch Exception ex))
    )
  )

(defn edit [_ id] (get-log id "log-edit"))

