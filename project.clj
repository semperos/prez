(defproject prez "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.3"]
                 [markdown-clj "0.9.20"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler prez.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}}
  :main prez.core)
