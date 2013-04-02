(ns accord.util
  (:require [clj-http.util :refer [url-encode]]
            [clojure.string :refer [join]]))


(def entity-methods [:post :put :patch])


(defn a->str
  [a]
  (if (instance? clojure.lang.Named a)
    (name a)
    (str a)))


(defn rfc-3986-url-encode
  [unencoded]
  (-> (url-encode (a->str unencoded))
      (.replace "+" "%20")
      (.replace "*" "%2A")
      (.replace "%7E" "~")))


(defn url-form-encode
  [params]
  (join "&" (map (fn [[k v]]
                   (str (rfc-3986-url-encode k)
                        "="
                        (rfc-3986-url-encode v))) params)))


(defn parse-qs
  [uri]
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" uri)]
    [(keyword k) v])))


(defn in?
  [e s]
  (if (nil? (some #(= e %) s))
    false
    true))
