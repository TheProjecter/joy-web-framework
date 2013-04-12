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

(defn- today []
  (let [d (dt/today)]
    [(dt/year d) (dt/month d) (dt/day d)]))

(defn- rs-to-map [rs]
  (reduce (fn [r [k v]] (conj r {(name k) v})) {} rs))

(defn- get-logs-created-in [year month]
  (db/select ds ["select * from logs where year = ? and month = ?" year month]
             #(rs/tiles "logs" {"logs" % "year" year "month" month})))

(defn- get-logs-created-between [{starty :year startm :month}
                                 {endy :year endm :month}])

(defn GET-logs
  ([] (let [[year month] (today)]
        (get-logs-created-in year month)))
  ([year]
     (get-logs-created-in year (dt/month (dt/today))))
  ([year month]
     (get-logs-created-in year month))
  )

(defn POST-logs []
  (get-logs-created-in (servlet/param "year") (servlet/param "month"))
  )

(defn- get-log* [id]
  (sql/with-connection {:datasource ds}
    [(sql/with-query-results [log] ["select * from logs where id = ?" id]
         ;;(if (= 0 (count res)))
       log)
     (sql/with-query-results [tags]
       ["select t.* from tags t, log_tags lt where lt.log_id = ? and lt.tag_id = t.id" id])
     ] 
    )
  )

(defn- get-log [id target]
  (rs/tiles target {"log" (rs-to-map (get-log* id)) "id" id})
  )

(defn- select-log [id target]
  (db/select ds ["select * from logs where id =?" id]
             #(rs/tiles target {"log" (first %) "id" id})
             ))

(defn GET-log [id]
  (try
    (if (<= (Integer/parseInt id) 0)
      (rs/tiles "log-edit" {"id" 0}) (select-log id "log"))
    (catch Exception ex (.printStackTrace ex))
    )
  )

(defn- next-id []
  (sql/with-connection {:datasource ds}
    (sql/with-query-results [m] ["call next value for seq"]
      (first (vals m))
      )))

(defn POST-log [id]
  (let [title (servlet/param "title") content (servlet/param "content")
        insert? (<= (Integer/parseInt id) 0) tid (if insert? (next-id) id)]
    (sql/with-connection {:datasource ds}
      (if insert?
        (let [[year month date] (today)]
          (sql/insert-record "logs" {:id tid :title title :content content
                                     :year year :month month :date date}))
        (sql/update-values "logs" ["id=?" tid] {:title title :content content}))
      )
    (get-log tid "log"))
  )

(defn edit [_ id] (get-log id "log-edit"))

(defn GET-tags []
  (sql/with-connection {:datasource ds}
    (sql/with-query-results tags ["select * from tags"]
      (rs/tiles "tags" {"tags" (map #(rs-to-map %) tags)})
      )
    )
  )

(defn GET-tag [id]
  (sql/with-connection {:datasource ds}
    (sql/with-query-results [t] ["select * from tags where id = ?" id]
      (rs/tiles "tag" {"tag" (rs-to-map t) "id" id})      
      )
    )
  )

(defn POST-tag [id]
  (let [insert? (<= (Integer/parseInt id) 0)
        tid (if insert? (next-id) id)
        tag (servlet/param "tag")]
    (sql/with-connection {:datasource ds}
      (if insert?
        (sql/insert-record "tags" {:id tid :tag tag})
        (sql/update-values "tags" ["id=?" tid] {:tag tag}))
      )
    (GET-tags)
    )
  )

(defmulti delete (fn [target _] target))

(defmethod delete "tag" [_ id]
  ;;(println "delete tag:" id)
  (sql/with-connection {:datasource ds}
    (sql/delete-rows "tags" ["id=?" id]))
  (GET-tags))

(defmethod delete "log" [_ id]
  ;;(println "delete log:" id)
  (let [{year :year month :month} (get-log* id)]
    (sql/with-connection {:datasource ds}
      (sql/delete-rows "logs" ["id=?" id]))
    (GET-logs year month)
    )
  )

(defn select []
;  (sql/with-connection {:datasource ds}
;;    (sql/with-query-results res ["select * from logs"]
;;      (println (map db/foo res))
;;      )
;;    )
;;  (println (db/select {:datasource ds} ["select * from logs"]))
  (let [rs (db/select ds ["select * from logs"])]
    (println "rs==>" rs)
    ) 
  )