;; Copyright (c) Pengyu Yang. All rights reserved

(ns ^{:path "/"} org.joyframework.ddu
    (:require [org.joyframework.route :reload true :as route]
              [org.joyframework.result :reload true :as rs]
              [org.joyframework.resources :reload true :as res]
              [org.joyframework.db :reload true :as db]
              [org.joyframework.session :reload true :as sess]
              [org.joyframework.request :reload true :as req]
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
  (println "page:" page ", total:" total)
  (let [last-page (+ (if (= 0 (mod total per-page)) 0 1) (quot total per-page))]
    (println "last-page:" last-page)
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
  (println "wh ==>" wh ", args ==>" args)
  (let [per-page 3
        sql-count (str "select count(*) from logs " wh)
        total (first (vals (first (db/select ds (into [sql-count] args)))))]
    (if-let [page-info (pages page total per-page)]
      (assoc page-info
        :logs (db/select ds (into [(str "select limit "
                                        (:start page-info) " " per-page " "
                                        "* from logs " wh)] args)))
      {:logs []})
    ))

(defn select-logs
  ([y m d t tags page]
     (let [wh (if (or y m d t tags)
                (str "where "
                     (if y "year=? ") (if m (str (if y "and ") "month=? "))
                     (if d (str (if (or y m) "and ") "date=? "))
                     (if t (str (if (or y m d) "and ") "title like ? "))
                     (if tags (str (if (or y m d t) "and ")
                                   "id in (select distinct lts.log_id from log_tags lts, 
                                    tags ts where lts.tag_id = ts.id and ("
                                   (reduce #(str % " or " %2) (for [_ tags] (str "ts.id=?")))
                                   "))"))))
           args (into (vec (filter #(not (nil? %)) [y m d t])) tags)]
       (sess/set {"wh" wh "args" args})
       (select-logs wh args page)
       ))
  ([wh args page]
     (let [{:keys [logs pages more prev page]}
           (select-logs* {:wh wh :args args :page (Integer/parseInt page)})]
       (rs/tiles
        "logs" {"logs" logs "pages" pages "more" more
                "prev" prev "page" page
                "all" (if (req/param "all") "all")})
       ))
  )

;; GET /ddu/joy/logs?all
;; GET /ddu/joy/logs?search
;; POST /ddu/joy/logs p: year/month/date/title/tags

;; GET /ddu/joy/logs
;; GET /ddu/joy/logs?page=2

;; GET /ddu/joy/logs/2013
;; GET /ddu/joy/logs/2013/4

;; GET /ddu/joy/logs/2013?page=2
;; GET /ddu/joy/logs/2013/4?page=2

(defn GET-logs "url: /joy/logs/2013/4?page=1"
  ([] (cond
       (req/param "all") (GET-logs nil)
       (req/param "search") (rs/tiles "logs-search" {"tags" (select-tags)})
       :else (let [wh (sess/get "wh") args (sess/get "args")]
               (if (and wh args)
                 (select-logs wh args (req/param "page" "1"))
                 (rs/tiles "logs")))
       ))
  ([year] (GET-logs year nil))
  ([year month] (select-logs year month nil nil nil (req/param "page" "1")))
  )

(defn POST-logs []
  (let [[year month date title tag]
        (req/param #(if (== 0 (count %)) nil %) "year" "month" "date" "title" "tag")]
    ;;(println "year:" year ", month: " month ", date:" date ", title:" title)
    (select-logs year month date title
                 (if-not (nil? tag) (if (string? tag) [tag] (vec tag)))
                 "1")))

(defn- select-log
  ([id] (let [log (first (db/select ds ["select * from logs where id =?" id]))
              tags (db/select ds ["select id, tag from tags, log_tags 
                                   where tags.id = log_tags.tag_id 
                                   and log_tags.log_id = ?" id])]
          (assoc log "tags" tags)))
  ([id target] (rs/tiles target {"log" (select-log id) "id" id
                                 "page" (req/param "page")
                                 "all" (if (req/param "all") "all")}))
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
        tags (req/param "tag")
        checked-tags (if (string? tags) [tags] tags)]
    [insert?
     {:title (req/param "title") :content (req/param "content") :id tid}
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
    (select-log (:id log) "log")
    ))

(defn edit [_ id]
  (let [log (select-log id)
        tags (map #(reduce (fn [x y] (if (x "checked") x
                                         (if (= (x "id") (y "id"))
                                           (assoc x "checked" true) x))) %
                                           (log "tags")) (select-tags))]
    (rs/tiles "log-edit" {"log" log "tags" tags "id" id
                          "all" (if (req/param "all") "all")})))

(defn GET-tags [] (rs/tiles "tags" {"tags" (select-tags)}))

(defn GET-tag [id]
  (let [t (first (db/select ds ["select * from tags where id=?" id]))]
    (rs/tiles "tag" {"tag" t})
    ))

(defn POST-tag [id]
  (let [insert? (<= (Integer/parseInt id) 0)
        tid (if insert? (next-id) id)
        tag (req/param "tag")]
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
