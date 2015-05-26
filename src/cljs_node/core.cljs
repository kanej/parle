(ns cljs-node.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs.nodejs :as nodejs]
              [cljs.core.async :refer [put! chan <!]]))

(nodejs/enable-util-print!)

(def readline (nodejs/require "readline"))
(def blessed (nodejs/require "blessed"))

(defn read-user-input [rl read-ch]
  (.question rl "> " #(put! read-ch %)))

(defn setup-repl []
  (let [read-ch (chan)
        ev identity
        rl (.createInterface readline #js {:input (.-stdin js/process) :output (.-stdout js/process)})]
    (go
      (loop [read-ch read-ch rl rl]
        (let [expr (<! read-ch)]
          (if (not= expr "exit")
            (do
              (println (ev expr))
              (read-user-input rl read-ch)
              (recur read-ch rl))
            (.exit js/process)))))
    (read-user-input rl read-ch)))

(defn -main [& args]
  (setup-repl))

(set! *main-cli-fn* -main)
