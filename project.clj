(defproject parle "0.3.0-SNAPSHOT"
  :description "Nodejs nrepl client"
  :url "http://github.com/kanej/parle"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89"]
                 [org.clojure/core.async "0.2.385"]]
  :npm {:dependencies [[source-map-support "*"]
                      [bencode "0.10.0"]]}
  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-npm "0.6.2"]]
  :cljsbuild {
    :builds [{
        :source-paths ["src"]
        :compiler {
          :output-to "parle.js"
          :optimizations :simple
          :pretty-print true
          :target :nodejs}}]})
