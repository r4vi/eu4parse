(ns eu4parse.core
  (:require [instaparse.core :as insta]))


(def sample "
#Stockholm
	1=
	{
		position=
		{
			3085.000 1723.000 3086.000 1730.000 3084.500 1729.000 3095.000 1724.500 3082.000 1730.000 3080.000 1736.000 
		}
		rotation=
		{
			0.000 0.000 0.087 -0.698 0.000 0.000 
		}
		height=
		{
			0.000 0.000 1.000 0.000 0.000 0.000 
		}
	}
#Östergötland
	2=
	{
		position=
		{
			3067.000 1692.500 3057.500 1696.000 3053.000 1700.000 3067.000 1691.000 3053.000 1700.000 3052.500 1702.000 
		}
		rotation=
		{
			-1.134 0.000 0.000 -1.047 0.000 0.000 
		}
		height=
		{
			0.000 0.000 1.000 0.000 0.000 0.000 
		}
	}")

(def grammar
  "
PROVINCES = PROVINCE_TAG*
PROVINCE_TAG = PROVINCE_NAME <WHITESPACE?> #'\\d+' <'=' WHITESPACE? '{' WHITESPACE?> POSITION_TAG <ROTATION_TAG HEIGHT_TAG WHITESPACE? '}' WHITESPACE?> 
PROVINCE_NAME = <WHITESPACE? '#'> #'.+' <#'\\r?\\n'>
POSITION_TAG = <WHITESPACE? 'position=' WHITESPACE?> POLYGON <WHITESPACE?>
HEIGHT_TAG = <WHITESPACE? 'height=' WHITESPACE?> POLYGON <WHITESPACE?>
ROTATION_TAG = <WHITESPACE? 'rotation=' WHITESPACE> POLYGON <WHITESPACE?>
POLYGON = <'{' WHITESPACE?> PAIR_COORD* <WHITESPACE? '}'>
PAIR_COORD = FLOAT_NUM <' '> FLOAT_NUM <WHITESPACE?>
FLOAT_NUM = #'[-]?[0-9]+\\.?[0-9]+'
WHITESPACE = <#'\\s+'>
  ")

(def parse
  (insta/parser grammar))

(defn get-points [pos-tag]
  (let [polygon (second pos-tag)
        pairs (map rest (rest (rest polygon)))
        maxx 5632
        maxy 2048
        xys (map (fn [[x y]]
                   (str
                    (- (read-string (second x)) maxx)
                    ","
                    (- (read-string (second y)) maxy))
               ) pairs)]
    (clojure.string/join " " xys)))

(defn to-kvp [name value]
  (str name "=\"" value "\" "))

(defn province-tree-to-tag [province-tree]
  (let [province-name (-> province-tree second second)
        province-id (nth province-tree 2)
        points (get-points (nth province-tree 3))
        fill "#E44D26;stroke-width=10"]
    (str "<polygon "
         ;(to-kvp "name" province-name)
         (to-kvp "id" province-id)
         (to-kvp "points" points)
         (to-kvp "fill" fill)
         "/>")))

 (defn make-svg [svg-inner]
  (str
   "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"2048\" width=\"5632\">\n"
       (clojure.string/join "\n" svg-inner)
       "\n</svg>\n"))

(comment
  (def toparse (slurp (clojure.java.io/resource "positions_tiny.txt") :encoding "ISO-8859-1"))
  (def toparse (slurp (clojure.java.io/resource "positions_mini.txt") :encoding "ISO-8859-1"))
  (def toparse (slurp (clojure.java.io/resource "positions.txt") :encoding "ISO-8859-1"))

  (def parsed (parse toparse :optimize :memory))

  (def provinces (rest parsed))
  (def polygon-svg-tags (map province-tree-to-tag provinces))
  (def out-svg (make-svg polygon-svg-tags))
  (spit "/tmp/weird2.svg" out-svg :encoding "UTF-8") 
 )


















