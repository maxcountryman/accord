(ns accord.util
  (:require [clj-http.util :refer [url-encode]]))

;; Constants
(def entity-methods '("POST" "PUT" "PATCH"))


(defn encode-params
  "Returns a query string."
  [params]
  (let [encoded (for [[k v] params]
                  (str (url-encode (name k)) "=" (url-encode v)))]
    (apply str (interpose "&" encoded))))


(defn parse-qs
  "Returns a parsed query string as a map."
  [uri] 
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" uri)]
    [(keyword k) v])))


(defn in?
  "Returns true if s contains e, otherwise false."
  [e s]
  (if (nil? (some #(= e %) s))
    false
    true))
