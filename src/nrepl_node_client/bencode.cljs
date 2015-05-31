(ns nrepl-node-client.bencode
    (:require [cljs.nodejs :as nodejs]))

(def bencode (nodejs/require "bencode"))

(defn encode [data]
  (.encode bencode (clj->js data)))

(defn decode [data]
  (.decode bencode data "utf8"))
