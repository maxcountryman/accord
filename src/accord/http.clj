(ns accord.http
  (:refer-clojure :exclude (get)))


(defn get
  [serv uri & [req]]
  ((:request @serv) serv (merge req {:method :get :url uri})))


(defn head
  [serv uri & [req]]
  ((:request @serv) serv (merge req {:method :head :url uri})))


(defn post
  [serv uri & [req]]
  ((:request @serv) serv (merge req {:method :post :url uri})))


(defn put
  [serv uri & [req]]
  ((:request @serv) serv (merge req {:method :put :url uri})))


(defn delete
  [serv uri & [req]]
  ((:request @serv) serv (merge req {:method :delete :url uri})))


(defn options
  [serv uri & [req]]
  ((:request @serv) serv (merge req {:method :options :url uri})))


(defn copy
  [serv uri & [req]]
  ((:request @serv) serv (merge req {:method :copy :url uri})))


(defn move
  [serv uri & [req]]
  ((:request @serv) serv (merge req {:method :move :url uri})))


(defn patch
  [serv uri & [req]]
  ((:request @serv) serv (merge req {:method :patch :url uri})))
