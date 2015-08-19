(ns parle.config
    (:require [cljs.nodejs :as nodejs]))

(def fs (nodejs/require "fs"))

(defn- str->int
  [s]
  (when (re-matches #"^\d+$" s)
    (js/parseInt s)))

(defn read-file [file-name callback-fn]
  (.readFile fs file-name (fn [err data] (-> data .toString str->int callback-fn))))
