(ns accord.oauth1
  (:require [clj-http.client :as [client]]
            [accord.signature :as sig]))


(declare request)


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


(defn- format-url
  [serv req]
  ,,,)


(defn- msecs->secs
  [msec]
  (int (/ msecs 1000)))


(defn- nonce
  []
  (apply str (repeatedly 64 #(char (rand-nth (range 128))))))


(defn- oauth-params
  [serv req meth]
  (let [params {:oauth_consumer_key (:client-key @serv)
                :oauth_nonce (nonce)
                :oauth_signature_method meth
                :oauth_timestamp (msecs->secs (System/currentTimeMillis))
                :oauth_version "1.0"}]
      (if (:access-token @serv)  ;; conditionally use token
        (assoc params :oauth_token (:access-token @serv))
        params)))


(defn- sign
  [serv meth req]
  (sig/sign serv meth req))


(defn- auth-header
  [req]
  ,,,)


(defn- attach-signature
  [serv header-auth req]
  (if header-auth
    (merge req {:headers {"Authorization" (auth-header req)}})
    (if (in? (:method req) entity-methods)
      ,,, ;; entity method branch
      (update-in req [:query-params] {:oauth_signature (:oauth_params req)}))))


(defn request
  [serv req & {:keys [sign-meth header-auth]
               :or {sign-meth :hmac-sha1 header-auth false}}]

    (let [req (->>
                (format-url serv req)
                (oauth-params serv)
                (sign serv sign-meth)
                (attach-signature header-auth))]

      (client/check-url! (:url req))
      (client/request req)))
