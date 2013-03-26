(ns accord.util
  (:require [clj-http.util :refer [url-encode]]))


(def entity-methods [:post :put :patch])


(defn encode-params
  [params]
  (let [encoded (for [[k v] params]
                  (str (url-encode (name k)) "=" (url-encode v)))]
    (apply str (interpose "&" encoded))))


(defn parse-qs
  [uri] 
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" uri)]
    [(keyword k) v])))


(defn in?
  [e s]
  (if (nil? (some #(= e %) s))
    false
    true))
