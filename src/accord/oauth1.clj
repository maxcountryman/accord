(ns accord.oauth1
  (:require [accord.util :refer [a->str
                                 entity-methods
                                 in?
                                 rfc-3986-url-encode
                                 url-form-encode]]
            [clojure.string :refer [join upper-case]]
            [clojurewerkz.urly.core :refer [relative?]]
            [clj-http.client :as client])
  (:import (javax.crypto Mac)
           (javax.crypto.spec SecretKeySpec)))


(declare request)


;; ## Service Wrapper


(defn service
  [consumer-key
   consumer-secret
   access-token-url
   request-token-url
   & {:keys [access-token access-token-secret base-url]}]

  (ref {:consumer-key consumer-key
        :consumer-secret consumer-secret
        :access-token-url access-token-url
        :access-token access-token
        :access-token-secret access-token-secret
        :base-url base-url
        :request request}))


;; ## Resource Retrieval


(defn- format-url
  [serv req]
  (let [base-url (:base-url @serv)
        url (:url req)]
    (merge req {:url (if (and base-url (relative? url))
                       (str base-url url)
                       url)})))


(defn- msecs->secs
  [msecs]
  (int (/ msecs 1000)))


(defn- nonce
  [& seed]
  (let [sha1 (java.security.MessageDigest/getInstance "sha1")
        seed (or seed (str (rand)))]
    (->>
      (.digest sha1 (.getBytes seed))
      (map #(+ (bit-and % 0xff) 0x100))
      (map #(.substring (format "%x" %) 1))
      (apply str))))


(defn- oauth-params
  [serv sig-meth req]
  (let [params {:oauth_callback "oob"  ;; TODO
                :oauth_consumer_key (:consumer-key @serv)
                :oauth_nonce (nonce)
                :oauth_signature_method sig-meth
                :oauth_timestamp (msecs->secs (System/currentTimeMillis))
                :oauth_version "1.0"}]
      (if (:access-token @serv)  ;; conditionally use token
        (->>
          (assoc params :oauth_token (:access-token @serv))
          (assoc req :oauth-params))
        (assoc req :oauth-params params))))


(defn- base-string
  [http-method url params]
  (join "&" [http-method
             (rfc-3986-url-encode url)
             (rfc-3986-url-encode (url-form-encode (sort params)))]))


(defn- hmac-sha1-digest
  [^String k ^String s]
  (let [hmac-sha1 "HmacSHA1"
        signing-key (SecretKeySpec. (.getBytes k) hmac-sha1)
        mac (doto (Mac/getInstance hmac-sha1) (.init signing-key))]
    (String. (org.apache.commons.codec.binary.Base64/encodeBase64 
               (.doFinal mac (.getBytes s)))
             "UTF-8")))


(defmulti sign
  (fn [serv sig-meth req] sig-meth))


(defmethod sign :hmac-sha1
  [serv sig-meth req]
  (let [secret (:consumer-secret @serv)
        http-method (upper-case (name (:method req)))
        unsigned (base-string http-method (:url req) (:oauth-params req))
        signature (hmac-sha1-digest secret unsigned)]
    (assoc-in req [:oauth-params :oauth_signature] signature)))


(defn- auth-header
  ([params]
   (let [encoded (map (fn [[k v]] (str (rfc-3986-url-encode (a->str k))
                                       "=\""
                                       (rfc-3986-url-encode (a->str v))
                                       "\""))
                     params)]
     (str "OAuth " (join ", " encoded))))
  ([params realm]
     (auth-header (assoc params :realm realm))))


(defn- entity-method
  [req]
  (update-in req [:body-params] merge (:oauth-params req)))


(defn- attach-creds
  [serv header-auth req]
  (cond
    ;; attach OAuth params to the header
    (true? header-auth) (auth-header (:oauth-params req))
    ;; attach OAuth params to the entity body
    (in? (:method req) entity-methods) (entity-method req)
    ;; attach OAuth params to the query string
    :else (update-in req [:query-params] merge (:oauth-params req))))


(defn request
  [serv req & {:keys [sig-meth header-auth]
               :or {sig-meth :hmac-sha1 header-auth false}}]

    (let [req (->>
                (format-url serv req)
                (oauth-params serv sig-meth)
                (sign serv sig-meth)
                (attach-creds header-auth))]

      (client/check-url! (:url req))
      (client/request req)))
