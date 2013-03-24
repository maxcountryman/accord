# Accord

A simple OAuth 1.0/a, 2.0 consumer client for Clojure built on top of 
[clj-http](https://github.com/dakrone/clj-http).

## Warning

This is a work-in-progress as well as my first foray into Clojure...use at your
own risk.

## Usage

Using Accord is easy and straight forward:

```clojure
;; We'll be using a popular OAuth 2.0 provider for this example
(require '[accord.oauth2 :as oauth2])

;; You don't have to use Cheshire, but we'll parse the JSON with it in this
;; example
(require '[cheshire.core :refer [parse-string]])

;; Get a real client id and secret from:
;;
;;  https://developers.facebook.com/apps
;;
(def client-id "440483442642551")
(def client-secret "cd54f1ace848fa2a7ac89a31ed9c1b61")

(def authorize-url "https://graph.facebook.com/oauth/authorize")
(def access-token-url "https://graph.facebook.com/oauth/access_token")
(def redirect-uri "https://www.facebook.com/connect/login_success.html")
```

Now we have some basic constants we can use to seed our client with.

```clojure
(def fb (oauth2/service client-id
                        client-secret
                        authorize-url
                        access-token-url
                        :base-url "https://graph.facebook.com/"))
```

Great! At this point we should construct an authorization URL and ask our user
to visit it.

```clojure
(oauth2/make-authorize-url fb {:scope "read_stream"
                               :response_type "code"
                               :redirect_uri redirect-uri})
; => "https://graph.facebook.com/oauth/authorize?client_id=440483442642551&scope=read_stream&response_type=code&redirect_uri=https%3A%2F%2Fwww.facebook.com%2Fconnect%2Flogin_success.html"
```

Typically the redirect URI would direct the user back to your application. On
this redirect we would find a code parameter, assuming the user authorized our
application. Once we obtain that code parameter's value we can get an access
token.

```clojure
(oauth2/get-access-token fb {:query-params {:code code
                                            :redirect_uri redirect-uri}})
```

Because the `fb` var is a ref, calling this function updates the proper
access-token field associated with the ref. From this point we can make
authenticated requests with the provider. In other words, we have an accord
with the provider.

```clojure
(require '[accord.client :as client])  ;; require our HTTP client wrapper
(def user (parse-string (:body (client/get fb "me")) true))
(println "currently logged in as: " (:name user))
; => "currently logged in as: Foo Bar"
```

## License

Copyright © 2013 Max Countryman

Distributed under the BSD License.
