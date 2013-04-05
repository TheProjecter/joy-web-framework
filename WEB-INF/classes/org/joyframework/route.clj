; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.route
  (:use [org.joyframework servlet resources validation] )
  (:require [clojure.string :as str]
            [org.joyframework.result :as r])
  (:import java.io.FileNotFoundException))

(def ^:dynamic *bootstraps* "Mappings in the bootstrap namespace.")

(declare get-request-path get-route get-handler get-handler-from-path
         checkbox validate valid-http-method? valid-token? invoke trim-slashes)

(defn service
  ""
  [bss request response]
  (binding [*bootstraps* bss
            *http-request* request
            *http-method* (.getMethod request)
            *http-response* response
            *http-params* (params request)
            *http-session* (.getSession request)
            *servlet-context* (.. request getSession getServletContext)]
    (let [[handler args ns] (-> (get-request-path) get-route get-handler)]
      (reinstate-flash)
      (try
        (if handler
          (doto handler
            valid-http-method? valid-token? checkbox
            (validate ns)
            (apply args))
          (r/not-found))
        (catch Exception ex
          (if-let [h ('exception-handler *bootstraps*)] (h ex) (throw ex))
          ))
      )))

(defn- valid-http-method? ""
  [handler]
  (let [m (meta handler)]
    (if-not (cond (:GET m) (GET?) (:POST m) (POST?) :else true)
      (throw (RuntimeException. "invalid.http.method")))
    ))

(defn- valid-token?
  "Tests if the handler is callable. Returns true if the handler
   can be invoked; otherwise false returned. "
  [handler]
  (if (:token (meta handler))
       (let [tn (session-get "__jf_tk_name__")
          tv (session-get "__jf_tk_value__")]
      (session-set "__jf_tk_name__" nil)
      (if-not (= (param tn) tv)
        (throw (RuntimeException. "invalid.token")))
      )
    )
  )

(defn- checkbox "" [handler]
  (let [chks
        (reduce (fn [m [k v]]
                  (let [kn (name k) n (.substring kn 5)]
                    (if (nil? (param n)) (assoc m n v) m))) {}
                (filter (fn [[k v]]
                          (.startsWith (name k) "_chk_")) (meta handler)))]
    (set! *http-params* (into *http-params* chks) ))
  )

(defn- validate [handler ns]
  (let [m (meta handler)
        vali (or (:vali m) ((ns-publics ns)
                            (symbol (str (:name m) "-validate"))))]
    (if vali (vali))))

(defn- invoke
  "Invokes the validations configured on the handler. If validation
   succeeds, the handler is invoked to process the user's request."
  [handler args]
  (let [{:keys [input redirect forward tiles
                short-circuit form] :or {short-circuit true} :as vali}
        (:vali (meta handler))
        ;;_ (println "form:" form)
        rules (filter #(let [[k v] %]
                         (not (or (= :input k) (= :redirect k)
                                  (= :tiles k) (= :forward k)
                                  (= :form k)
                                  (= :short-circuit k)))) vali)
        ;;_ (println "rules:" rules)
        e (loop [[[f [s & rule]] & xs] rules rs {}]
            ;;(println "f===>" f)
            (cond
             (and short-circuit (not (empty? rs))) rs
             (nil? f) (if-let [fs (and form (form))]
                        (merge-with concat rs {"form" fs}) rs)
             :else (recur
                    xs (merge-with concat rs
                                   (validate* (assoc s :field (name f))
                                              rule)))))
        ;;(println "e:" e)
        ]
    (if (empty? e)
      (apply handler args)
      (if input (input {"errors" e})
          (let [p (conj *http-params* {"errors" e})]
            (cond redirect (r/redirect redirect p)
                  tiles (r/tiles tiles p)
                  forward (r/forward forward p))
            ))
      )))

(defn- get-request-path
  ""
  []
  (let [path-info (or (.getPathInfo *http-request*) "/") 
        ;;_ (println "path-info:" path-info)
        servlet-path (.getServletPath *http-request*)
        ;;_ (println "servlet-path:" servlet-path)
        ]
    (str/split
     (trim-slashes
      (if path-info path-info
          (let [i (.lastIndexOf servlet-path ".")]
            (if (== -1 i) servlet-path
                (.substring servlet-path 0 i))))
      ) #"/")
    ))

(defn- get-route
  ""
  [request-path]
  (loop [[r & rs :as path] request-path m (var-get ('rt *bootstraps*))]
    (if-let [sub-rt (m r)]
      (recur rs sub-rt) (assoc m :path path)))
  )

(defn- get-handler
  "Finds the proper function to handle the request."
  [{ns :ns path :path}]
  (let [ns-pubs (ns-publics ns)]
    (if-let [h0 (first (for [[pk pv] ns-pubs [pa _] *http-params*
                             :when (= (str pk) pa)] pv))]
      [h0 path ns]
      (if-let [h1 (get-handler-from-path path ns-pubs)]
        [h1 (rest path) ns]
        [(ns-pubs (or (:default (meta ns)) INDEX)) path ns])
      )))

(defn- get-handler-from-path [path ns-pubs]
  (if-let [fst (first path)]
    (or (ns-pubs (symbol (str *http-method* "-" fst)))
        (ns-pubs (symbol fst)))))

(defmacro defroutes
  "Usage: (defroutes [a.b.c d e] x.y.z)"  
  [name & nss]
  `(def ~name (defroutes* '~@nss))
  )

(defn- build-routes-map
  ""
  [m [h & t]]
  (if-let [subm (m h)]
    (let [f (first t)]
      (if (map? f)
        (assoc m h (conj subm f))
        (conj m {h (build-routes-map subm t)})))
    (if (map? h) (merge m h)
        (assoc m h (reduce #(hash-map %2 %1) (reverse t))))
    ))

(defn- load-ns
  "Finds the namespace indicated by the given symbol. If the 
   given namespace is not found, tries loading the namespace and
   returns it."
  [s]
  (try (or (find-ns s)
           (do (require s) (find-ns s)))
       (catch FileNotFoundException e
         (throw (IllegalArgumentException.
                 (str "Unable to find namespace " s))))))

(defn defroutes*
  "Usage: (defroutes* '[a.b.c d e] 'x.y.z)"
  [& nss]
  ;;(println "nss:" nss)
  (let [redu (fn [v] (reduce #(hash-map %2 %) (reverse v)))]

    (->> (map #(cond (symbol? %) [%]
                     (vector? %)
                     (let [[pn & names] %]
                       (map (fn [nm] (symbol (str pn "." nm))) names))) nss)
         ;; '[a.b.c d e] 'x.y.z ==> ('a.b.c.d 'a.b.c.e 'x.y.z)
         (apply concat)
         ;;(println "after apply:")

         ;; find the defined namespaces
         ;;(map find-ns)
         (map load-ns)
         ;;(println "after find-ns:") 

         ;; remove invalid namespaces
         (filter #(not (nil? %)))

         ;; build a vector out of each namespace for later use
         ;; i.e. a.b.c ==> ["a" "b" "c" {:ns a.b.c}]
         (map #(let [md (meta %)
                     path-md (:path md)
                     ;;_ (println "path-md:" path-md)
                     path (or (and path-md
                                   (str/split (trim-slashes path-md) #"/"))
                              (str/split (str (ns-name %)) #"\."))
                     ;;_ (println "ns:" % ", path:" path) 
                     ]
                 (conj path {:ns %})))

         ;;build a route table tree out of the vectors
         ;;from the last step
         (reduce build-routes-map {})
         )))

(defn- trim-slashes
  "Trims slashes from both ends of the given string argument."
  [s]
  ;;(println "s:" s)
  (when s
    (if (= "/" s) s
        (let [b1 (.startsWith s "/") b2 (.endsWith s "/")]
          (if (or b1 b2)
            (let [l (.length s)]
              (.substring s (if b1 1 0) (if b2 (- l 1) l))) s))
        )
    ))