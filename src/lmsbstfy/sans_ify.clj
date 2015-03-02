(ns lmsbstfy.sans-ify
  (:require [hiccup.core :refer [html]]))

(def ^:private style-tag (html [:style {:type "text/css"} (slurp "resources/lmsbstfy.css")]))

(def ^:private banner-tag (html [:div {:class "lmsbstfy-banner"} (slurp "resources/banner.html")]))

(defn- base-tag [url] (html [:base {:href url}]))

(defn sans-bullshit-sans-ify [url body]
  (str
   style-tag
   banner-tag
   (base-tag url)
   body))

