(ns parle.net
    (:require [cljs.nodejs :as nodejs]))

(def net (nodejs/require "net"))

(defn connect [port callback-fn]
  (let [client (.connect net #js {:port port})]
    (.on client "error" #(callback-fn (-> (aget % "code") keyword)))
    (.on client "connect" #(callback-fn client))))
