(ns facebook
  "A simple Facebook OAuth consumer example."
  (:require [accord.oauth2 :as oauth2]
            [cheshire.core :refer [parse-string]]))


;; Get a real client id and secret from:
;;
;;  https://developers.facebook.com/apps
;;
(def client-id "440483442642551")
(def client-secret "cd54f1ace848fa2a7ac89a31ed9c1b61")


(def authorize-url "https://graph.facebook.com/oauth/authorize")
(def access-token-url "https://graph.facebook.com/oauth/access_token")
(def redirect-uri "https://www.facebook.com/connect/login_success.html")


(def fb (oauth2/service client-id
                        client-secret
                        authorize-url
                        access-token-url
                        :base-url "https://graph.facebook.com/"))


(def auth-url (oauth2/make-authorize-url fb {:scope "read_stream"
                                             :response_type "code"
                                             :redirect_uri redirect-uri}))


(println "Visit this URL in your browser: " auth-url)
(println "Enter code from browser: ")
(def code (read-line))


(oauth2/get-access-token fb {:query-params {:code code
                                            :redirect_uri redirect-uri}
                             :throw-entire-message? true})


(def user (parse-string (:body (oauth2/get fb "me")) true))
(println "currently logged in as: " (:name user))
