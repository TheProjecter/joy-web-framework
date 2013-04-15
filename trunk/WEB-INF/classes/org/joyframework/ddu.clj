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

(defn- get-logs-created-in [year month]
  (rs/tiles "logs"
            {"logs" (db/select ds ["select * from logs where year = ? and month = ?"
                                   year month]) "year" year "month" month}))

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

(defn- select-log
  ([id] (let [log (first (db/select ds ["select * from logs where id =?" id]))
              tags (db/select ds ["select id, tag from tags, log_tags 
                                   where tags.id = log_tags.tag_id 
                                   and log_tags.log_id = ?" id])]
          (assoc log "tags" tags)))
  ([id target] (rs/tiles target {"log" (select-log id) "id" id}))
  )

(defn GET-log [id]
  (try
    (if (<= (Integer/parseInt id) 0)
      (rs/tiles "log-edit" {"id" 0 "tags" (select-tags)})
      (select-log id "log"))
    (catch Exception ex (.printStackTrace ex))
    )
  )

(defn- next-id []
  (first (vals (first (db/select ds ["call next value for seq"])))))

(defn- log-from-request [id]
  (let [insert? (<= (Integer/parseInt id) 0)
        tid (if insert? (next-id) id)
        tags (servlet/param "tag")
        checked-tags (if (string? tags) [tags] tags)]
    [insert?
     {:title (servlet/param "title") :content (servlet/param "content") :id tid}
     (map #(vector tid %) checked-tags)]))

(defn POST-log [id]
  (let [[insert? log tags] (log-from-request id)]
    (sql/with-connection {:datasource ds}
      (if insert?
        (let [[y m d] (today)]
          (sql/insert-record "logs" (assoc log :year y :month m :date d)))
        (let [id (:id log)]
          (sql/update-values "logs" ["id=?" id] log)
          (sql/delete-rows "log_tags" ["log_id=?" id])))
      (if (< 0 (count tags))
        (apply sql/insert-rows "log_tags" tags)))
    )
  (select-log id "log"))

(defn- select-tags []
  (db/select ds ["select * from tags"]))

(defn edit [_ id]
  (let [log (select-log id)
        tags (map #(reduce (fn [x y] (if (x "checked") x
                                         (if (= (x "id") (y "id"))
                                           (assoc x "checked" true) x))) %
                                           (log "tags")) (select-tags))]
    (rs/tiles "log-edit" {"log" log "tags" tags "id" id})))

(defn GET-tags []
  (rs/tiles "tags" {"tags" (select-tags)}))

(defn GET-tag [id]
  (let [t (first (db/select ds ["select * from tags where id=?" id]))]
    (rs/tiles "tag" {"tag" t}))
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
  (let [{year "year" month "month"} (select-log id)]
    (sql/with-connection {:datasource ds}
      (sql/delete-rows "logs" ["id=?" id]))
    (GET-logs year month)
    )
  )
