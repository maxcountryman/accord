;; ## OAuth 2.0 Handlers
;;
;; This is a set of functions related to the OAuth 2.0 authorization flow.
;; Functions are provided for generating authorization URLs, retrieving access
;; tokens, and making requests against a protected resource.
;;
;; A service wrapper factory is provided as well, which should be used to seed
;; all other functions, including HTTP functions from `accord.client`.


(ns accord.oauth2
  (:require [accord.util :refer [encode-params
                                 entity-methods
                                 in?
                                 parse-qs]]
            [clj-http.client :as client]
            [clojurewerkz.urly.core :refer [relative?]]))


(declare request)


;; ## Service Wrapper


(defn service
  "
  Defines a service ref to be used for making authorized HTTP calls against an
  OAuth 2.0 provider.

  For example, we could define a ref bound to the `fb` var like this:

    (def auth-url \"https://graph.facebook.com/oauth/authorize\")
    (def token-url \"https://graph.facebook.com/oauth/access_token\")

    (def fb (service 123 456 auth-url token-url))

  Returns a ref.
  "
  [client-id
   client-secret
   authorize-url
   access-token-url
   & {:keys [access-token base-url]}]

  (ref {:client-id client-id
         :client-secret client-secret
         :authorize-url authorize-url
         :access-token-url access-token-url
         :access-token access-token
         :base-url base-url
         :request request}))


;; ## Authorization Helpers


(defn make-authorize-url
  "
  Given a `serv` ref, creates an authorize URL that should be presented to the
  user. Additional query string parameters map be passed in as a map. Returns
  the authorize URL.

  If we were constructing an authorize URL for Facebook, we could do this:

    (make-authorize-url fb {:scope \"read_stream\"
                            :response_type \"code\"
                            :redirect_uri redirect-uri})

  Assume that you have a var `redirect-uri` as appropriate for your program.
  "
  [serv & [query-params]]

  (let [url (:authorize-url @serv)
        id (:client-id @serv)]
    (->>
      (assoc query-params :client_id id)
      encode-params
      (str url "?"))))


(defn get-access-token
  "
  Given a `serv` ref, retrieves an access token and attaches it to the ref.
  Additional modifications to the request's structure may be passed in as a
  `req` map. Returns the ref.

  This assumes a provider will return a form-encoded response. In the future
  this function should be modified to accept an arbitrary decoder as an
  argument, thus supporting whatever format a provider presents.

  Carrying on with the Facebook example, you could get an access token like so:

    (get-access-token fb {:query-params
                          {:code code
                           :redirect_uri redirect-uri})
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

      (client/check-url! url)

    (dosync
      (let [resp (request serv (merge req {:method method :url url}))
            data (parse-qs (:body resp))]  ;; TODO: arbitrary decoders
        (alter serv assoc-in [:access-token] (:access_token data))))))


;; ## Resource Retrieval


(defn request
  "
  Given a `serv` ref and a `req` map, makes an HTTP request against a service
  providers resource. This function handles attaching the necessary
  authentication parameters to the request.

  Generally this function need not be invoked directly.
  Instead the corresponding HTTP verb functions may be used in its place.

  However directly calling it would work like this:

    (request fb {:url \"https://graph.facebook.com/me\"
                 :method :get})
  "
  [serv req & {:keys [bearer_auth] :or [bearer_auth true]}]

    (let [req (update-in req [:query-params] merge {:access_token
                                                    (:access-token @serv)})
          has-base? (not (nil? (:base-url @serv)))
          req-url (:url req)
          url (if (and has-base? (relative? req-url))
                (str (:base-url @serv) req-url)
                req-url)]

      (client/check-url! url)

      (client/request (merge req {:url url}))))
