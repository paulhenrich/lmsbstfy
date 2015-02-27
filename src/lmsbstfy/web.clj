(ns lmsbstfy.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [org.httpkit.server :refer [run-server]]
            [org.httpkit.client :as http]
            [hickory.core :as hk]
            ;[lmsbstfy.google-analytics :as ga]
            [environ.core :refer [env]])
  (:use [ring.middleware ratelimit]))

(use 'hiccup.core 'hiccup.page)
;; todo:
;;   - semantic injection if necessary
;;   - GA

(defn fetch-remote-page [url]
  @(http/get url {:follow-redirects true
                  :max-redirects 3}))

(def style-tag (html [:style {:type "text/css"} (slurp "resources/lmsbstfy.css")]))
(def banner-tag (html [:div {:class "lmsbstfy-banner"} (slurp "resources/banner.html")]))
(defn base-tag [url] (html [:base {:href url}]))

(defn sans-bullshit-sans-ify [url body]
  (str
   style-tag
   banner-tag
   (base-tag url)
   body))

(defn urlify [url-ish]
  "Take a guess at the intended URL"
  (if (re-find #"^http" url-ish)
    url-ish
    (str "http://" url-ish)))

(defn web-proxy [request]
  (let [url (urlify (:* (:params request)))
        {:keys [status headers body error] :as response} (fetch-remote-page url)]
    (if (or error
            (not (boolean (re-find #"text/html" (:content-type headers)))))
      "^ put the url of the page you want to see after /disrupt/ e.g. /disrupt/example.com ^"
      (sans-bullshit-sans-ify url body))))


(defroutes app
  (GET "/" [] (io/resource "public/index.html"))
  (GET "/disrupt/*" request
       (web-proxy request))
  (route/resources "/"))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(defn wrap-app [app]
  ;; TODO: heroku config:add SESSION_SECRET=$RANDOM_16_CHARS
  (let [store (cookie/cookie-store {:key (env :session-secret)})]
    (-> app
        (wrap-ratelimit {:limits [(ip-limit 100)]})
        ((if (env :production)
           wrap-error-page
           trace/wrap-stacktrace))
        (site {:session {:store store}}))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (run-server (wrap-app #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
