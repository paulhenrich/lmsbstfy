(defproject lmsbstfy "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://lmsbstfy.herokuapp.com"
  :license {:name "FIXME: choose"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.2"]
                 [javax.servlet/servlet-api "2.5"]
                 [http-kit "2.1.16"]
                 [hickory "0.5.4"]
                 [hiccup "1.0.5"]
                 [ring/ring-devel "1.3.2"]
                 [ring/ring-core "1.3.2"]
                 [ring-ratelimit "0.2.2"]
                 [environ "0.5.0"]
                 [com.cemerick/drawbridge "0.0.6"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "lmsbstfy-standalone.jar"
  :profiles {:production {:env {:production true}}})
