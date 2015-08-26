(ns parle.config
    (:require [cljs.nodejs :as nodejs]))

(def fs (nodejs/require "fs"))

(defn- str->int
  [s]
  (when (re-matches #"^\d+$" s)
    (js/parseInt s)))

(defn read-file [file-name callback-fn]
  (.readFile fs file-name (fn [err data] (-> data .toString str->int callback-fn))))


(def port-mapping [:port #(-> % name js/parseInt)])
(def host-mapping [:host #(-> % name)])

(def param-mappings {"-p" port-mapping
                     "--port" port-mapping
                     "-h" host-mapping
                     "--host" host-mapping})

(defn- merge-param [options-map [param value]]
  (if-let [[k convertor] (get param-mappings param)]
    (assoc options-map k (convertor value))
    options-map))

(defn args->map [args]
  (let [pairs (partition 2 args)]
    (reduce merge-param {} pairs)))
