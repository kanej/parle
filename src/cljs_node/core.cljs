(ns cljs-node.core
    (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(def readline (nodejs/require "readline"))
(def blessed (nodejs/require "blessed"))

;;(def screen (.screen blessed #js {:autoPadding true :smartCSR true}))

(defn rep []
  (let [rl (.createInterface readline #js {:input (.-stdin js/process) :output (.-stdout js/process)})
        ev identity]
    (.question rl "> " #(do (println (ev %) (.close rl))))))

(defn -main [& args]
  (rep))


(set! *main-cli-fn* -main)
