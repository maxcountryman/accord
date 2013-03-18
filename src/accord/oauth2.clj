(ns accord.oauth2
  "OAuth 2.0 consumer client handlers."
  (:require [accord.util :refer [encode-params
                                 entity-methods
                                 in?
                                 parse-qs]]
            [clj-http.client :as client])
  (:refer-clojure :exclude (get)))


;; default timeout in milliseconds
(def timeout 3000)


(defn service
  "
  A wrapper for OAuth 2.0 consumer handling.

  Expected positional arguments:
    * id (client ID)
    * secret (client secret)
    * authorize-url ()
    * token-url (access token url)
  "
  [id
   secret
   authorize-url
   token-url
   & {:keys [access-token base-url]}]

  (ref {:client-id id
        :client-secret secret
        :authorize-url authorize-url
        :access-token-url token-url
        :access-token access-token
        :base-url base-url}))


(defn make-authorize-url
  "
  Handles constructing authorization URLs.

  Expected positional arguments:
    * serv

  Optional positional arguments:
    * query-params
  "
  [serv & [query-params]]

  (let [url (:authorize-url @serv)
        id (:client-id @serv)
        query-params (or query-params {})
        params (assoc query-params :client_id id)]
    (str url "?" (encode-params params))))


(defn get-access-token
  "
  Retrieves an access token and attaches it to the serv ref.

  This assumes a provider will return a form-encoded response. In the future
  this function should be modified to accept an arbitrary decoder as an
  argument, thus supporting whatever format a provider presents.

  Expected positional arguments:
    * serv

  Optional positional arguments:
    * req
  "
  [serv & [req]]

  (let [client-creds {:client_id (:client-id @serv)
                      :client_secret (:client-secret @serv)}
        url (:access-token-url @serv)
        method (or (:method req) :post)
        is-entity-method (in? method entity-methods)
        req (if is-entity-method  ;; conditionally merge credentials
              (update-in req [:form-params] merge client-creds)
              (update-in req [:query-params] merge client-creds))]

      ;; perform same check as clj-http
      (client/check-url! url)

    (dosync
      (let [resp (client/request (merge req {:method method :url url}))
            data (parse-qs (:body resp))]  ;; TODO: arbitrary decoders
        (alter serv assoc-in [:access-token] (:access_token data))))))


(defn request
  "
  Loosely wraps clj-http.client/request. Expects the addition of a serv ref.
  Otherwise accepts the same args. Attaches access token and uses base-url if
  defined. Returns the result of calling clj-http.client/request.

  Expected positional arguments:
    * serv
    * req
  "
  [serv req]

  (let [req (update-in req [:query-params] merge {:access_token
                                                  (:access-token @serv)})
        has-base? (not (nil? (:base-url @serv)))
        method (:method req)
        url (if has-base?
              (str (:base-url @serv) (:url req))
              (:url req))]

    ;; Perform same check as clj-http
    (client/check-url! url)

    (client/request (merge req {:method method :url url}))))


(defn get
  "Like #'request, but sets the :method and :url as appropriate."
  [serv uri & [req]]
  (request serv (merge req {:method :get :url uri})))


(defn head
  "Like #'request, but sets the :method and :url as appropriate."
  [serv uri & [req]]
  (request serv (merge req {:method :head :url uri})))


(defn post
  "Like #'request, but sets the :method and :url as appropriate."
  [serv uri & [req]]
  (request serv (merge req {:method :post :url uri})))


(defn put
  "Like #'request, but sets the :method and :url as appropriate."
  [serv uri & [req]]
  (request serv (merge req {:method :put :url uri})))


(defn delete
  "Like #'request, but sets the :method and :url as appropriate."
  [serv uri & [req]]
  (request serv (merge req {:method :delete :url uri})))


(defn options
  "Like #'request, but sets the :method and :url as appropriate."
  [serv uri & [req]]
  (request serv (merge req {:method :options :url uri})))


(defn copy
  "Like #'request, but sets the :method and :url as appropriate."
  [serv uri & [req]]
  (request serv (merge req {:method :copy :url uri})))


(defn move
  "Like #'request, but sets the :method and :url as appropriate."
  [serv uri & [req]]
  (request serv (merge req {:method :move :url uri})))


(defn patch
  "Like #'request, but sets the :method and :url as appropriate."
  [serv uri & [req]]
  (request serv (merge req {:method :patch :url uri})))
