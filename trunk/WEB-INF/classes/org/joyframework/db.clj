; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.db
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as sql])
  (:import com.jolbox.bonecp.BoneCPDataSource))


(defmacro defds [name spec]
  "Defines a var after the given name pointing to a datasource
   required by the spec."
  `(def ~name (ds ~spec)))


(defn ds [spec]
  "Returns a datasource by the spec. The argument is a map, 
   which may contain following keys:
   :driver      --> the JDBC driver class
   :subprotocol
   :subname
   :user        --> the name of user accessing the database, sa by default
   :password    --> the password of the user, empty by default
   :jndi        --> the JNDI pointing to a defined by the container,
                    it has priority over other options. "   
  (let [{:keys [driver subprotocol subname user password jndi]
         :or {user "sa" password ""}} spec]
    (if jndi
      (do (println "----> jndi: " jndi))
      (do
        (println "----> Create a BoneCP datasource.")
        (Class/forName driver)
        (doto (BoneCPDataSource.)
          (.setJdbcUrl
           (str "jdbc:" subprotocol ":" subname))
          (.setUsername user)
          (.setPassword password))
        ))
    )
  )

(defn foo [m]
  (into {} (map (fn [[k v]] {(name k)
                             (cond (map? v) (foo v)
                                   (vector? v) (into [] (map foo v))
                                   :else v)}) m)))

(defn select [ds query]
  (sql/with-connection {:datasource ds}
    (sql/with-query-results res query
      (vec (map foo res))
      )))
