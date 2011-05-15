(defproject clj-pipes "0.1.0"
  :description "Wrapper for the TinkerPop Pipes API for traversing Graph DBMSs. It supports version 0.5 of the Pipes API."
  :url "https://github.com/eduardoejp/clj-pipes"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [com.tinkerpop/pipes "0.5"]
                 [clj-blueprints "0.1.0"]]
  :dev-dependencies [[org.clojars.rayne/autodoc "0.8.0-SNAPSHOT"]]
  :repositories {"tinkerpop" "http://tinkerpop.com/maven2"}
  :autodoc {:name "clj-pipes"
            :description "Wrapper for the TinkerPop Pipes API for traversing Graph DBMSs. It supports version 0.5 of the Pipes API."
            :copyright "Copyright 2011 Eduardo Julian"
            :web-src-dir "http://github.com/eduardoejp/clj-pipes/blob/"
            :web-home "http://eduardoejp.github.com/clj-pipes/"
            :output-path "autodoc"}
  :aot [AccessibleAbstractPipe]
	)
