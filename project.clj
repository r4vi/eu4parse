(defproject eu4parse "0.1.0-SNAPSHOT"
  :description "Silly app to parse the positions.txt file from Europa Universalis 4"
  :url "http://github.com/r4vi/eu4parse"
  :license {:name "WTFPL"
            :url "http://www.wtfpl.net/about/"}
  :jvm-opts ["-XX:+UseConcMarkSweepGC"
             "-Xms4g"
             "-Xmx4g"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [instaparse "1.2.13"]
                 [org.apache.commons/commons-lang3 "3.2.1"]
                 [org.clojure/data.xml "0.0.7"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [org.clojure/data.zip "0.1.1"]])
