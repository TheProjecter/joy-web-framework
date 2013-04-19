; Copyright (c) Pengyu Yang. All rights reserved

(ns org.joyframework.validation
  (:require [clojure.string :as str]
            [org.joyframework.request :as req])
  (:use [org.joyframework servlet resources]))

(def ^:dynamic *field-value*)

(def ^:dynamic *field-name*)

(def ^:dynamic *field-label*)

(def ^:dynamic *field-label-key*)

(def ^:dynamic *field-id*)

(defn validate* [field-info valis]
  (let [{:keys [field label key id short-circuit]
         :or {short-circuit true}} (if (map? field-info)
                                     field-info {:field field-info}) 
         field-value (req/*http-params* field)
         r (reduce
            (fn [rs el] ;; el is an element of valis
              (binding [*field-name* field
                        *field-value* field-value
                        *field-label* (or label
                                          (and key (get-message key))
                                          field)
                        *field-id* id]
                (if (and short-circuit (not (empty? rs))) rs
                    (if-let [r (if (vector? el)
                                 (apply (first el) (rest el))
                                 (if el (el)))]
                      (conj rs r) rs))))
            [] valis)
         ]
    (if (not (empty? r)) {(or id "form") r})
    ))

(defn length [& {:keys [min max key key-min key-max]
                 :or {key "vali.length"
                      key-min "vali.length.min"
                      key-max "vali.length.max"}}]
  (let [len (.length *field-value*)]
    (cond
     (and (nil? min) (not (nil? max)) (<= max len))
     (get-message key-max *field-label* max) 
    
     (and (nil? max) (not (nil? min)) (< len min))
     (get-message key-min *field-label* min)
    
     (and (not (nil? min)) (not (nil? max))
          (or (<= max len) (< len min)))
     (get-message key *field-label* min max)
     )
    )
  )

(defn required [& {key :key :or {key "vali.required"}}]
  (if (empty? *field-value*) (get-message key *field-label*)))

(defn integer [& {:keys [min max key]
                :or {min nil max nil key "vali.int"}}]
  ["integer" min max key]
  )

(defn email [& {key :key :or {key "vali.email"}}]
  ["email" key]
  )


;;(defn vali-field
;;  "Validate a single field with the given validation functions.
;;   The field to be validated is indicated by the field name in the 
;;   HTML form; info is a map providing extra information about 
;;   the field; vfs is a vector of validation functions."
;;  [field-name info & vfs]
;;  (binding [*field-value* (*http-params* field-name)
;;            *field-name* field-name]
;;    (loop [[vf args & re] vfs rs []]
;;      (if vf
;;        (recur re (conj rs (vf (into (or args {}) info)))) rs)
;;      )))
;;
;;
;;(defn vali-fields
;;  ""
;;  [short-circuit? & fields]
;;  (loop [[field & fs] fields rs []] 
;;    (if field
;;      (recur fs (into rs (apply vali-field field)))
;;      (filter #(not (nil? %)) rs)
;;      ))
;;  )

;;(defn required [{id :id label :label key :key     
;;                 :or {key "vali.required"
;;                      label *field-name*}
;;                 }]
;;  (if (empty? *field-value*) (get-message key label))
;;  )
;;
;;(defn str-len [])
;;
;;(defn integer [{id :id label :label key :key
;;                key-min :key-min key-max :key-max
;;                key-between :key-between
;;                min :min max :max min-inc :min-inc max-inc :max-inc
;;                :or {min nil min-inc true
;;                     max nil max-inc false
;;                     key "vali.int" key-min "vali.min" key-max "vali.max"
;;                     key-between "vali.between" label *field-name*}}]
;;  (try
;;    (let [n (Integer/parseInt *field-value*)
;;          min-inc-text (if min-inc "Inclusive" "Exclusive")
;;          max-inc-text (if max-inc "Inclusive" "Exclusive")
;;          m
;;          (cond
;;           (and min max) [key-between label min min-inc-text max max-inc-text]
;;           min [key-min label min min-inc-text]
;;           max [key-max label max max-inc-text]
;;           :else [key label]
;;           )
;;          ]
;;      ;;(println m)
;;      (if (not (and
;;                (or (not min) (if min-inc (<= min n) (< min n)))
;;                (or (not max) (if max-inc (<= n max) (< n max))))) 
;;        (apply get-message m))) 
;;    (catch NumberFormatException e (get-message key label))
;;    )
;;  )
;;
;;(defn alpha-only [{id :id label :label key :key
;;                   :or {key "vali.alpha-only"}}]
;;  
;;  )
;;
;;(defn length [{id :id label :label
;;               key :key key-min :key-min key-max :key-max
;;               min :min max :max
;;               :or {min -1 max -1
;;                    key "vali.length"
;;                    key-min "vali.length.min"
;;                    key-max "vali.length.max"
;;                    label *field-name*}}]
;;  ;;(println "str-length invoked")
;;  (let [len (.length *field-value*)]
;;    (if (and (== -1 min) (>= len max))
;;      (get-message key-max label max)
;;      (if (and (== -1 max) (> min len))
;;        (get-message key-min label min)
;;        (if (or (> min len) (>= len max))
;;          (get-message key label min max))))
;;    ))
;;
;;(defn email [{id :id label :label key :key}]
;;  (println "email invoked")
;;  ("email")
;;  )




