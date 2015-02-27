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
            ;[lmsbstfy.google-analytics :as ga]
            [environ.core :refer [env]]))

(defn fetch-remote-page [url]
  @(http/get (str "http://" url) {:follow-redirects true}))

(defn web-proxy [request]
  (let [url (:* (:params request))
        {:keys [status headers body error] :as response} (fetch-remote-page url)]
    (if (or error
            (not= "text/html" (:content-type headers)))
      "Nerp"
      body)))


(defroutes app
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body (pr-str ["Hello" :from 'Heroku])})
  (GET "/disrupt/*" request
       (web-proxy request)))

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
