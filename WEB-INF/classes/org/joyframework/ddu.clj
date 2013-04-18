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

(defn- next-id [] (first (vals (first (db/select ds ["call next value for seq"])))))

(defn- today [] (let [d (dt/today)] [(dt/year d) (dt/month d) (dt/day d)]))

(defn- select-tags [] (db/select ds ["select * from tags"]))

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

(defn select-logs* [{:keys [wh args page]}]
  (let [per-page 3
        sql-count (str "select count(*) from logs " wh)
        total (first (vals (first (db/select ds (into [sql-count] args)))))
        page-info (pages page total per-page)
        sql-logs (str "select "
                      (if (>= page 0)
                        (str "limit " (:start page-info) " " per-page " "))
                      "* from logs " wh)]
    (assoc page-info :logs (db/select ds (into [sql-logs] args)))
    ))

;; GET /ddu/joy/logs?search
;; POST /ddu/joy/logs p: year/month/date/title/tags

;; GET /ddu/joy/logs
;; GET /ddu/joy/logs?page=2

;; GET /ddu/joy/logs/2013
;; GET /ddu/joy/logs/2013/4

;; GET /ddu/joy/logs/2013?page=2
;; GET /ddu/joy/logs/2013/4?page=2
(defn select-logs
  ([] (let [wh (servlet/session-get "wh") args (servlet/session-get "args")
            page (servlet/param "page")]
        (if (and wh args page)
          (select-logs wh args page) (select-logs nil))
        ))
  ([y] (select-logs y nil))
  ([y m] (select-logs y m nil nil nil (servlet/param "page" "1")))
  ([y m d t tags page]
     (let [wh (if (or y m d t tags)
                (str "where "
                     (if y "year=? ") (if m (str (if y "and ") "month=? "))
                     (if d (str (if (or y m) "and ") "date=? "))
                     (if t (str (if (or y m d) "and ") "title like ? "))
                     (if tags (str (if (or y m d t) "and ")
                                   "id in (select distinct lts.log_id 
                                    from log_tags lts, tags ts where 
                                    lts.tag_id = ts.id and ts.id in (" tags "))"))))
           args (vec (filter #(not (nil? %)) [y m d t]))]
       (servlet/session-set {"wh" wh "args" args})
       (select-logs wh args page)
       ))
  ([wh args page]
     (let [{:keys [logs pages more prev page]}
           (select-logs* {:wh wh :args args :page (Integer/parseInt page)})]
       (rs/tiles
        "logs" {"logs" logs "pages" pages "more" more "prev" prev "page" page})
       ))
  )

(defn GET-logs "url: /joy/logs/2013/4?page=1"
  ([] (select-logs))
  ([arg]
     (condp = arg 
       "search" (rs/tiles "logs-search" {"tags" (select-tags)})
       ))
  ([year month]
     (select-logs year month))
  )

(defn POST-logs []
  (let [tag (servlet/param "tag")]
    (select-logs (servlet/param "year" nil) (servlet/param "month" nil)
                 (servlet/param "date" nil) (servlet/param "title" nil)
                 (if (string? tag) tag (if tag (reduce #(str % "," %2) tag)))
                 "1")))

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
    ))

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

(defn GET-tags [] (rs/tiles "tags" {"tags" (select-tags)}))

(defn GET-tag [id]
  (let [t (first (db/select ds ["select * from tags where id=?" id]))]
    (rs/tiles "tag" {"tag" t})
    ))

(defn POST-tag [id]
  (let [insert? (<= (Integer/parseInt id) 0)
        tid (if insert? (next-id) id)
        tag (servlet/param "tag")]
    (sql/with-connection {:datasource ds}
      (if insert?
        (sql/insert-record "tags" {:id tid :tag tag})
        (sql/update-values "tags" ["id=?" tid] {:tag tag})))
    (GET-tags)
    ))

(defmulti delete (fn [target _] target))

(defmethod delete "tag" [_ id]
  (sql/with-connection {:datasource ds}
    (sql/delete-rows "tags" ["id=?" id]))
  (GET-tags))

(defmethod delete "log" [_ id]
  (let [{year "year" month "month"} (select-log id)]
    (sql/with-connection {:datasource ds}
      (sql/delete-rows "logs" ["id=?" id]))
    (GET-logs year month)
    ))
