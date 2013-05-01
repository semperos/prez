(ns prez.core
  (:require [prez.gershwin :refer [code-gwn]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-js include-css]]
            [markdown.core :refer [md-to-html-string]]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def ^{:doc "When true, the presentation will be 'compiled' to a single, standalone index.html file that can be viewed offline."
       :dynamic true}
  *compile-prez* false)

(defn css-resource
  "Slurp a resource off the classpath under public/css"
  [^String resource-name]
  (when-let [res (io/resource (str "public/css/" resource-name))]
    (slurp res)))

(defn js-resource
  "Slurp a resource off the classpath under public/js"
  [^String resource-name]
  (when-let [res (io/resource (str "public/js/" resource-name))]
    (slurp res)))

(defn inc-css*
  "Build a raw <style> tag with inline css for the given CSS resource"
  [r]
  (let [content (css-resource r)]
    [:style content]))

(defn inc-css
  "Depending on value of *compile-prez*, either include links to CSS files or include them inline."
  [& resources]
  (if *compile-prez*
    (html (map inc-css* resources))
    (apply include-css
           (map #(str "css/" %) resources))))

(defn inc-print-css
  "Include a CSS stylesheet for print media. Depending on value of *compile-prez*, either includes a link to the CSS file or inlines it."
  [r]
  (if *compile-prez*
    (html [:style {:media "print"} (css-resource r)])
    (html [:link {:type "text/css"
                  :src (str "css/" r)
                  :media "print"}])))

(defn inc-js*
  "Build a raw <script> tag with inline JS for the given JS resource"
  [r]
  (let [content (js-resource r)]
    [:script {:type "text/javascript"} content]))

(defn inc-js
  "Depending on the value of *compile-prez*, either include links to the JS files or include them inline."
  [& resources]
  (if *compile-prez*
    (html (map inc-js* resources))
    (apply include-js
           (map #(str "js/" %) resources))))

(defn reveal
  "Entry-point for creating a Reveal.js presentation. Provides the HTML skeleton required to run a Reveal.js presentation. Expects an initial object of options, supportig keys :title, :description. The rest args `content` should be Hiccup-compatible content that includes the actual slides of the presentation."
  [spec & content]
  (let [title (:title spec)]
    (html5
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "author" :content "Daniel Gregoire"}]
      [:meta {:name "description" :content (get spec :description "Technical presentation")}]
      [:meta {:name "apple-mobile-web-app-capable" :content "yes"}]
      [:meta {:name "apple-mobile-web-app-status-bar-style" :content "black-translucent"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"}]
      [:title title]
      (inc-css "reveal.min.css"
               ;; Reveal themes: beige, simple, solarized, moon, night, default, serif, sky
               "theme/serif.css"
               ;; Highlight themes: arta ascetic brown_paper brown_papersq.png dark default docco far foundation github googlecode idea ir_black magula mono-blue monokai monokai_sublime obsidian pojoaque pojoaque.jpg railscasts rainbow school_book school_book.png solarized_dark solarized_light sunburst tomorrow tomorrow-night-blue tomorrow-night-bright tomorrow-night tomorrow-night-eighties vs xcode zenburn
               "lib/highlight/github.css"
               "app.css")
      (inc-js "lib/jquery-2.0.0.min.js"
              "lib/plugin/highlight/highlight.js")
      ;; If compiled, it's already like having a PDF, so just provide
      ;; basic "paper" print stylesheet.
      (if *compile-prez*
        (inc-print-css "print/paper.css")
        [:script {:type "text/javascript"}
         "document.write( '<link rel=\"stylesheet\" href=\"css/print/' + ( window.location.search.match( /print-pdf/gi ) ? 'pdf' : 'paper' ) + '.css\" type=\"text/css\" media=\"print\">' );"])
      (str "<!--[if lt IE 9]>"
           (inc-js "lib/html5shiv.js")
           "<![endif]-->")]
     [:body
      [:div.reveal
       [:div.slides
        content]]
      (inc-js "lib/head.min.js"
              "reveal-2.4.0.js"
              "app.js")])))

(defn section
  "Build a single slide (<section>) element. Accepts an optional initial map of options that can be used to customize the output of the section tag."
  [& args]
  (let [attrs (if (map? (first args))
                (first args)
                {})
        content (if (map? (first args))
                  (rest args)
                  args)]
    (html [:section attrs content])))

(defn merge-args
  "Utility function for creating custom section-foo functions that call section with custom options."
  [base-args extra]
  (if (map? (first base-args))
    (cons (merge (first base-args) extra)
          (rest base-args))
    (cons extra base-args)))

(defn md
  "Shortcut for compiling a Markdown string to HTML."
  [s]
  (md-to-html-string s))

(defn code
  "Generic HTML code block"
  [& args]
  (when (seq (remove nil? args))
    (html [:pre [:code args]])))

(defn code-clj
  "HTML code block marked as 'clojure' source code"
  [& args]
  (when (seq (remove nil? args))
    (html [:pre [:code.clojure args]])))

(defn code-resource
  "Slurp a resource off the classpath under the code/ directory"
  [^String resource-name]
  (when-let [res (io/resource (str "code/" resource-name))]
    (slurp res)))

(defn parse-prez
  "Read prez.md and build slides. Produces simple horizontal presentation, splits slides on lines with '::section'."
  ([] (parse-prez "prez.md"))
  ([f]
     (let [rdr (io/reader f)
           lines (line-seq rdr)
           attrs (read-string (first lines))
           material (str/join "\n" (rest lines))
           sections (str/split material #"::section")]
       (prn sections)
       (reveal attrs
               (for [sec sections]
                 (section (md-to-html-string sec)))))))

(defn elc-proposal-2013
  "Presentation proposal for Emerging Language Camp, 2013"
  []
  (reveal {:title "Gershwin" :description "Concatenative, stack-based language built on Clojure"}
          (section [:h1 "Gershwin"]
                   [:aside "Concatenative, stack-based language built on Clojure"])
          (section [:h2 "Why?"]
                   [:ul
                    [:li.fragment "Want all of Clojure..."]
                    [:li.fragment "...plus a concatenative language"]])
          (section
           (section [:h2 "Concatenative Languages"])
           (section [:h3 "Function Application"])
           (section [:h3 "Factoring"]))
          (section [:h2 "Header 2"]
                   (code-gwn (code-resource "simple-gershwin.gwn")))
          (section [:h3 "Header 3"]
                   (code-clj "(defn foo [] \"wowza sauce\")"))
          (section [:h1 "Thank You"])))

(defn build-prez
  "Point to function for current presentation."
  []
  (elc-proposal-2013))

(defn -main
  "Build the default presentation and spit to target/classes/public/index.html"
  [& args]
  (let [prez (if (= (first args) "compile")
                 (binding [*compile-prez* true]
                   (build-prez))
                 (build-prez))
        dirs (java.io.File. "target/classes/public")
        f (java.io.File. (str dirs "/index.html"))]
    (if (not (.exists dirs))
      (when (.mkdirs dirs)
        (spit f prez))
      (spit f prez))))