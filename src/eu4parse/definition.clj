(ns eu4parse.definition
  (:import org.apache.commons.lang3.StringEscapeUtils)
  (:require [clojure.string]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zx]
            [clojure.data.xml]
            [clojure-csv.core :refer [parse-csv]])) 

(defn get-defs-csv []
  (parse-csv
   (slurp (clojure.java.io/resource "definition.csv") :encoding "ISO-8859-1")
   :delimiter \;)
  )

(def map-svg
  (-> "provinces.svg"
      (clojure.java.io/resource)
      (clojure.java.io/as-file)
      (xml/parse)) 
  )

(defn dec-to-hex-str
  "Convert decimal RGB triples to hex."
  [ls]
  (str "#" (clojure.string/lower-case (reduce #(str % (format "%02X" (int %2))) "" ls))))

(def zipped (zip/xml-zip map-svg))

(defn defs->map [defs]
  (into {}
        (for [[province-id r g b name] defs]
          [(dec-to-hex-str
            (map read-string [r g b]))
           {:content (StringEscapeUtils/escapeXml name)
            :province-id (StringEscapeUtils/escapeXml province-id)}]
          )))

(def definitions
  (defs->map
    ;; skip header
    (rest (get-defs-csv))))

(defn match-path? [loc]
  (let [tag (:tag (zip/node loc))]
    ;; true if tag is of type <path>
    (= :path tag)))


(defn edit-path [node]
  (let [style-def (get-in node [:attrs :style])
        props (clojure.string/split style-def #";")
        fill (second (clojure.string/split (first props) #":"))
        lookup-data (get definitions fill)]
    (->
     node
     (assoc-in [:attrs :description] (:content lookup-data))
     (assoc-in [:attrs :id] (:province-id lookup-data)))))

(defn tree-edit
  "Take a zipper, a function that matches a pattern in the tree,
   and a function that edits the current location in the tree.  Examine the tree
   nodes in depth-first order, determine whether the matcher matches, and if so
   apply the editor. On first match, return modified tree."
  [zipper matcher editor]
  (loop [loc zipper]
    (if (zip/end? loc)
      (zip/root loc)
      (if-let [matcher-result (matcher loc)]
        (let [new-loc (zip/edit loc editor)]
          (if (not (= (zip/node new-loc) (zip/node loc)))
            ;(zip/root new-loc)
            (recur (zip/next new-loc))))
        (recur (zip/next loc))))))

(def edited (tree-edit zipped match-path? edit-path))

(def new-svg
  (with-out-str
    (xml/emit edited)))

(defn save-svg [svg-as-string]
  (spit
   (clojure.java.io/file
    (clojure.java.io/resource "new.svg")) svg-as-string))
