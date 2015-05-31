(ns nrepl-node-client.net
    (:require [cljs.nodejs :as nodejs]))

(def net (nodejs/require "net"))

(defn connect [port]
  (.connect net #js {:port port}))
