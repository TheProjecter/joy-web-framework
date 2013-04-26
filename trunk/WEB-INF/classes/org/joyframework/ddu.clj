;; Copyright (c) Pengyu Yang. All rights reserved

(ns ^{:path "/" :res-key res} org.joyframework.ddu
    (:require [org.joyframework.route :reload true :as route]
              [org.joyframework.result :reload true :as rs]
              [org.joyframework.resources :reload true :as res]
              [org.joyframework.db :reload true :as db]
              [org.joyframework.session :reload true :as sess]
              [org.joyframework.request :reload true :as req]
              [org.joyframework.validation :reload true :as vali]
              [org.joyframework.util :reload true :as u]
              [org.joyframework.datetime :reload true :as dt]
              [clojure.java.jdbc :as sql]))

(route/defroutes __jf_rt__ org.joyframework.ddu)

(alter-var-root #'vali/__jf_date_format__ (fn [x] "yyyy-MM-dd"))

(db/defds ds {:driver "org.hsqldb.jdbc.JDBCDriver"
              :subprotocol "hsqldb"
              :subname "hsql://localhost/ddu"
              :user "SA"})

(defn index [] (rs/tiles "index"))

(defn- page []
  (if-let [p (req/param "page")] (sess/set "page" p) (sess/get "page" "1")))

(defn- next-id [] (first (vals (first (db/select ds ["call next value for seq"])))))

(defn- select-tags
  ([] (db/select ds ["select * from tags"]))
  ([checked]
     (let [tags (select-tags)]
       (if checked
         (map (fn [tag] (assoc tag "checked"
                               (some #(== (tag "id")
                                          (if (string? %) (Integer/parseInt %) %))
                                     (if (or (coll? checked)
                                             (u/array? checked)) checked [checked]))))
              tags)
         tags)
       ))
  )

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

(defn- select-logs* [{:keys [wh args page]}]
  ;;(println "wh ==>" wh ", args ==>" args)
  (let [per-page 15
        sql-count (str "select count(*) from logs " wh)
        total (first (vals (first (db/select ds (into [sql-count] args)))))]
    (if-let [page-info (pages page total per-page)]
      (assoc page-info
        :logs (db/select ds (into [(str "select limit "
                                        (:start page-info) " " per-page " "
                                        "* from logs " wh)] args)))
      {:logs []})
    ))

(defn- select-logs
  ([page] (select-logs nil nil nil nil nil page))
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
        "logs" {"logs" logs "pages" pages "more" more "prev" prev "page" page})
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
  ([]
     (cond
      (req/param "search") (rs/tiles "logs-search" {"tags" (select-tags)})
      (or (req/param "all")
          (sess/attr? "all")) (do (sess/set "all" true) (select-logs (page)))
      :else (let [wh (sess/get "wh") args (sess/get "args")]
              (if (or wh args)
                (select-logs wh args (page))
                (rs/tiles "logs")))
      ))
  ([year] (GET-logs year nil))
  ([year month]
     (sess/remove "all" "page")
     (select-logs year month nil nil nil (page)))
  )

(defn POST-logs-validate []
  (vali/with-rules
    (vali/rule {:field-name "year"} vali/integer)
    (vali/rule {:field-name "month"} #(vali/integer {:min 0 :max 13})))
  )

(defn POST-logs []
  (let [[year month date title tag]
        (req/param #(if (== 0 (count %)) nil %) "year" "month" "date" "title" "tag")]
    (sess/remove "all" "page")
    (select-logs year month date title
                 (if-not (nil? tag) (if (string? tag) [tag] (vec tag))) "1")))

(defn- select-log
  ([id] (let [log (first (db/select ds ["select * from logs where id =?" id]))
              tags (db/select ds ["select id, tag from tags, log_tags 
                                   where tags.id = log_tags.tag_id 
                                   and log_tags.log_id = ?" id])]
          (assoc log "tags" tags)))
  ([id target] (rs/tiles target {"log" (select-log id) "id" id}))
  )

(defn GET-log [id]
  (try (if (<= (Integer/parseInt id) 0)
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


(defn ^{;; :forward "" :redirect "" :input (fn []) :tiles ""
        :input #(rs/tiles "log-edit" req/*http-params* %
                          {"tags" (select-tags (req/param "tag"))})}
  POST-log-validate [id]
  (vali/with-rules {"id" id}
    (vali/rule {:field-name "title"} vali/required #(vali/maxlength 50))
    (vali/rule {:field-name "content"} vali/required #(vali/maxlength 2000))
    (vali/rule {:field-name "tag"} vali/required))
  )

(defn POST-log [id]
  (let [[insert? log tags] (log-from-request id)]
    (sql/with-connection {:datasource ds}
      (if insert?
        (let [[y m d] (dt/today)]
          (sql/insert-record "logs" (assoc log :year y :month m :date d)))
        (let [id (:id log)]
          (sql/update-values "logs" ["id=?" id] log)
          (sql/delete-rows "log_tags" ["log_id=?" id])))
      (if (< 0 (count tags))
        (apply sql/insert-rows "log_tags" tags)))
    (select-log (:id log) "log")
    ))

(defn edit [_ id]
  (let [log (select-log id)]
    (rs/tiles "log-edit" { "id" id "title" (log "title") "content" (log "content")
                           "tags" (select-tags (map #(% "id") (log "tags")))})))

(defn GET-tags [] (rs/tiles "tags" {"tags" (select-tags)}))

(defn GET-tag [id]
  (rs/tiles
   "tag" (conj {"id" id}
               (if (< 0 (Integer/parseInt id))
                 (let [t (first (db/select ds ["select * from tags where id=?" id]))]
                   {"tag" (t "tag")})
                 )) 
   ))

(defn ^{:tiles "tag"} POST-tag-validate [id]
  (vali/with-rules {"id" id}
    (vali/rule {:field-name "tag"} vali/required #(vali/length {:min 3 :max 20})))
  )

(defn ^:token POST-tag [id]
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

(defn- delete-log-tags [{:keys [log tag]}]
  (if-let [wh (cond log ["log_id=?" log]
                    tag ["tag_id=?" tag])]
    (sql/delete-rows "log_tags" wh)))

(defmethod delete "tag" [_ id]
  (sql/with-connection {:datasource ds}
    (sql/delete-rows "tags" ["id=?" id])
    (delete-log-tags {:tag id}))
  (GET-tags))

(defmethod delete "log" [_ id]
  (let [{year "year" month "month"} (select-log id)]
    (sql/with-connection {:datasource ds}
      (sql/delete-rows "logs" ["id=?" id])
      (delete-log-tags {:log id}))
    (GET-logs)
    ))

(defn GET-validations []
  (rs/tiles "validations"))

(defmulti POST-validations (fn [x] x))

(defmethod POST-validations "date" [_]
  (rs/tiles "validations" {"date" (req/param "date") "validDate" true})
  )

(defmethod POST-validations "date-before" [_]
  (println "date-before")
  )

(defmethod POST-validations "date-after" [_]
  (println "date-after")
  )

(defmulti ^{:tiles "validations"} POST-validations-validate (fn [x] x))

(defmethod POST-validations-validate "date" [_]
  (vali/with-rules
    (vali/rule {:field-name "date" :field-label "Input"
                :after "2010-5-1" :before :now} vali/required vali/date))
  )

(defmethod POST-validations-validate "date-before" [_]
  (println "validate-date-before")
  )

(defmethod POST-validations-validate "date-after" [_]
  (println "validate-date-after")
  )
