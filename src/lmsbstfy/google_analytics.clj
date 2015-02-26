;; via https://gist.github.com/anonymous/4383179

(ns lmsbstfy.google-analytics
  (:require [ring.util.codec :refer [url-encode]]))

(defn- make-query-string [m]
  (->> (for [[k v] m]
         (str (url-encode k) "=" (url-encode (str v))))
       (interpose "&")
       (apply str)))

(defn make-ga-url [utmp ip]
  (let [gif-url "http://www.google-analytics.com/__utm.gif?"
        url-map (atom {})
          add-key (fn [k v]
                      (swap! url-map assoc (name k) v))]
      (add-key :utmac "MO-XXXXXX") ;; use MO not UA
      (add-key :utmn (rand-int 0x7fffffff))
      (add-key :utmr "-")
      (add-key :utmp utmp)
      (add-key :utmdebug 1)
      (add-key :guid "ON")
      (add-key :utmwv "4.4sh")
      (add-key :utmcc "__utma=999.999.999.999.999.1")
      (add-key :utmvid "0xa5a576be59cbd6e4") ;; I dont know whats is this...
      (add-key :utmip ip)
      (str gif-url (make-query-string @url-map))))

(defn wrap-ga
  [handler]
  (fn [request]
   (let [url (make-ga-url (str (:uri request) "?" (:query-string request)) (:remote-addr request))]
     (prn url)
     (slurp url)
     (handler request))))
