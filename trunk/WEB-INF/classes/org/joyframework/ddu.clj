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

(defn- select-tags []
  (db/select ds ["select * from tags"]))

(defn- today []
  (let [d (dt/today)]
    [(dt/year d) (dt/month d) (dt/day d)]))

(defn- pages [page total per-page]
  (let [last-page (+ (if (= 0 (mod total per-page)) 0 1) (quot total per-page))]
    (if (and (>= page 1) (<= page last-page))
      (let [page-groups (partition 5 5 nil (range 1 (+ 1 last-page)))
            pages (some #(if (<= page (last %)) % false) page-groups)
            more? (< page (first (last page-groups)))
            prev? (> page (last (first page-groups)))]
        {:pages pages :page page :last-page last-page
         :more (if more? (+ 1 (last pages)) 0)
         :prev (if prev? (- (first pages) 1) 0)
         :start (* per-page (- page 1))}
        ))
    ))

(defn- get-logs-created-in [year month]
  (let [per-page 4]
    (if-let [{:keys [pages more prev page start]}
             (pages (Integer/parseInt (or (servlet/param "page") "1"))
                    (first (vals
                            (first
                             (db/select ds ["select count(*) from logs where 
                                             year=? and month=?" year month]))))
                    per-page)]
      (let [sql (str "select limit " start " " per-page
                     " * from logs where year=? and month=?")]
        (rs/tiles "logs" {"logs" (db/select ds [sql year month])
                          "pages" pages "more" more "prev" prev "page" page}))
      (rs/not-found))
    ))

(defn- get-logs-created-between [{starty :year startm :month}
                                 {endy :year endm :month}])

(defn GET-logs "url: /joy/logs/2013/4?page=1"
  ([] (let [[year month] (today)]
        (get-logs-created-in year month)))
  ([arg]
     (if (= "search" arg)
       (rs/tiles "logs-search" {"tags" (select-tags)})
       (get-logs-created-in arg (dt/month (dt/today))))
     )
  ([year month]
     (get-logs-created-in year month))
  )

(defn POST-logs
  ([] (get-logs-created-in (servlet/param "year") (servlet/param "month")))
  ([arg]
     (if (= "search" arg)
       (let [year (servlet/param "year" nil) month (servlet/param "month" nil)
             title (servlet/param "title" nil) tag (servlet/param "tag" nil)
             sql (str "select * from logs "
                      (if (or year month title tag) "where ")
                      (if year "year=? ")
                      (if month (str (if year "and ") "month=? "))
                      (if title (str (if (or year month) "and ") "title like ? "))
                      (if tag (str (if (or year month title) "and ")
                                   "id in (select distinct lts.log_id from 
                                             log_tags lts, tags ts where 
                                             lts.tag_id = ts.id and ts.id in (?))")))
             tags (if (string? tag) tag (if tag (reduce #(str % "," %2) tag)))
             q (vec (filter #(not (nil? %))
                            [sql (if year year) (if month month)
                             (if title (str title "%")) (if tags tags)]))
             ]
         (rs/tiles "logs-search-done" {"logs" (db/select ds q)
                                       "tags" (select-tags)} ))
       (rs/not-found))
     )
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
