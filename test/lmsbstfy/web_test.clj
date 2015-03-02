(ns lmsbstfy.web-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [lmsbstfy.web :refer :all]))

(defn body-of [response]
  (let [body (:body response)]
    (if (= java.io.File (type body))
      (slurp body)
      body)))

(deftest index-route
  (let [index (app (mock/request :get "/"))]
    (is 200 (:status index))
    (is (.contains (body-of index) "bullshit"))))

(deftest test-urlify
  (is "http://example.com" (urlify "http://example.com"))
  (is "http://example.com" (urlify "example.com")))
