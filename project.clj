(defproject parle "0.3.0-SNAPSHOT"
  :description "Nodejs nrepl client"
  :url "http://github.com/nrepl-node-client"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :node-dependencies [[source-map-support "*"]
                      [bencode "0.7.0"]]
  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-npm "0.4.0"]]
  :cljsbuild {
    :builds [{
        :source-paths ["src"]
        :compiler {
          :output-to "parle.js"
          :optimizations :simple
          :pretty-print true
          :target :nodejs}}]})
