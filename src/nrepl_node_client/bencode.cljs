(ns nrepl-node-client.bencode
    (:require [cljs.nodejs :as nodejs]))

(def bencode (nodejs/require "bencode"))

(defn encode [expr]
  (.encode bencode #js {:op "eval" :code expr}))

(defn decode [data]
  (.decode bencode data "utf8"))
