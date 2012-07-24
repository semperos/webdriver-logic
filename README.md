# WebDriver Logic #

The WebDriver Logic library provides a "mirror" of the [clj-webdriver](https://github.com/semperos/clj-webdriver) API using relations (see the [core.logic](https://github.com/clojure/core.logic) library), including a powerful, declarative syntax for authoring your functional web tests and letting webdriver-logic make inferences about the state of your application.

## Usage ##

**Note:** This library is in the earliest stages of development. Feedback is welcome; use at your own risk.

Here's a simple example where we find the first element with a class of `site-logo`:

```clj
(def driver (clj-webdriver.core/start {:browser :chrome} "https://github.com"))

(run 1 [q]
  (attributeo driver q :class "site-logo"))
;=> ({:webelement #<Tag: <a>, Class: site-logo, Href: https://github.com/, Object: [[ChromeDriver: chrome on MAC (6140efaa871769f2b7baa8fa885ebabc)] -> xpath: //*]>})
```

Or how about all the footer navigation sections?

```clj
(run* [q]
  (attributeo driver q :class "footer_nav"))
;=>
;; ({:webelement
;;   #<Tag: <ul>, Text: GitHub  About  Blog  Features  Contact & Support  Training  ..., Class: footer_nav, Object: [[ChromeDriver: chrome on MAC (6140efaa871769f2b7baa8fa885ebabc)] -> xpath: //*]>}
;;  {:webelement
;;   #<Tag: <ul>, Text: Clients  GitHub for Mac  GitHub for Windows  GitHub for Ecli..., Class: footer_nav, Object: [[ChromeDriver: chrome on MAC (6140efaa871769f2b7baa8fa885ebabc)] -> xpath: //*]>}
;;  {:webelement
;;   #<Tag: <ul>, Text: Tools  Gauges: Web analytics  Speaker Deck: Presentations  G..., Class: footer_nav, Object: [[ChromeDriver: chrome on MAC (6140efaa871769f2b7baa8fa885ebabc)] -> xpath: //*]>}
;;  {:webelement
;;   #<Tag: <ul>, Text: Documentation  GitHub Help  Developer API  GitHub Flavored M..., Class: footer_nav, Object: [[ChromeDriver: chrome on MAC (6140efaa871769f2b7baa8fa885ebabc)] -> xpath: //*]>})
```

How about the first three elements on the page that have a legitimate `id` attribute?

```clj
(run 3 [q]
  (fresh [an-element a-value]
    (attributeo driver an-element :id a-value)
    (!= a-value nil)
    (!= a-value "")
    (== q [a-value an-element])))
;=>
;; (["gauges-tracker"
;;   {:webelement
;;    #<Tag: <script>, Id: gauges-tracker, Source: https://secure.gaug.es/track.js, Object: [[ChromeDriver: chrome on MAC (1fc632cc0ded7fc2c7fa1db418329876)] -> xpath: //*]>}]
;;  ["wrapper"
;;   {:webelement
;;    #<Tag: <div>, Text: Signup and Pricing  Explore GitHub  Features  Blog  Sign in ..., Id: wrapper, Object: [[ChromeDriver: chrome on MAC (1fc632cc0ded7fc2c7fa1db418329876)] -> xpath: //*]>}]
;;  ["header"
;;   {:webelement
;;    #<Tag: <div>, Text: Signup and Pricing  Explore GitHub  Features  Blog  Sign in, Id: header, Class: true clearfix, Object: [[ChromeDriver: chrome on MAC (1fc632cc0ded7fc2c7fa1db418329876)] -> xpath: //*]>}])
```

Pretty simple - you could do that with regular CSS or XPath queries. One could argue, however, that even at this simple point the declarative nature of `run*` is easier to follow and reason about than a series of explicit `find-element`, `filter` or `remove` calls.

Let's make the inference work harder for us. Are there two links included in both the header and footer that have the same `href` value?

```clj
(binding [*search-domain* {:xpath "//a"}
          *child-search-domain* {:xpath ".//a"}]
  (run 2 [q]
     (fresh [header-el footer-el the-href-value]
            (attributeo b header-el :href the-href-value)
            (attributeo b footer-el :href the-href-value)
            (!= the-href-value nil)
            (!= the-href-value "")
            (childo b header-el (wd/find-element b {:id "header"}))
            (childo b footer-el (wd/find-element b {:id "footer"}))
            (== q [the-href-value header-el footer-el]))))
```

You'll notice the binding of `*search-domain*` to a subset of all anchor elements on the page. This drastically improves performance as relations like `attributeo` have to traverse all the elements on the page to find an answer. (Note: Performance issues of this kind will be improved once clj-webdriver's caching facilities are improved. Currently clj-webdriver limits caching to calls to `find-element`, which doesn't help with webdriver-logic).

You can flash these elements to convince yourself that the above works:

```clj
(require '[clj-webdriver.element :as el])

(doseq [res (binding [*search-domain* {:xpath "//a"}
                      *sub-search-domain* {:xpath ".//a"}]
              (run 2 [q]
                   (fresh [header-el footer-el the-href-value]
                          (attributeo b header-el :href the-href-value)
                          (attributeo b footer-el :href the-href-value)
                          (!= the-href-value nil)
                          (!= the-href-value "")
                          (childo b header-el (wd/find-element b {:id "header"}))
                          (childo b footer-el (wd/find-element b {:id "footer"}))
                          (== q [the-href-value header-el footer-el]))))]
  (wd/flash (el/map->Element (nth res 1)))
  (wd/flash (el/map->Element (nth res 2))))
;=>
;; (["https://github.com/features"
;;   {:webelement
;;    #<Tag: <a>, Text: Features, Href: https://github.com/features, Object: [[ChromeDriver: chrome on MAC (1fc632cc0ded7fc2c7fa1db418329876)] -> xpath: //a]>}
;;   {:webelement
;;    #<Tag: <a>, Text: Features, Href: https://github.com/features, Object: [[ChromeDriver: chrome on MAC (1fc632cc0ded7fc2c7fa1db418329876)] -> xpath: //a]>}]
;;  ["https://github.com/blog"
;;   {:webelement
;;    #<Tag: <a>, Text: Blog, Href: https://github.com/blog, Object: [[ChromeDriver: chrome on MAC (1fc632cc0ded7fc2c7fa1db418329876)] -> xpath: //a]>}
;;   {:webelement
;;    #<Tag: <a>, Text: Blog, Href: https://github.com/blog, Object: [[ChromeDriver: chrome on MAC (1fc632cc0ded7fc2c7fa1db418329876)] -> xpath: //a]>}])
```

Note: What is bound to `q` comes back as simple `clojure.lang.PersistentArrayMap` instances. As a neophyte in the realm of core.logic, I'm not entirely sure why the `Element` records returned by `find-element` are "downgraded" to maps, but the above works.

Larger and more meaningful examples forthcoming.

### Logic Programming Materials ###

#### Clojure/Lisp ####

 * [The Reasoned Schemer](http://mitpress.mit.edu/catalog/item/default.asp?ttype=2&tid=10663) (also [available in Kindle format](http://www.amazon.com/The-Reasoned-Schemer-ebook/dp/B004GEBQS6/ref=kinw_dp_ke?ie=UTF8&m=AG56TWVU5XWC2)
 * [README for core.logic](https://github.com/clojure/core.logic#readme)
 * Ambrose Bonnaire-Sergeant's [Logic Starter tutorial](https://github.com/frenchy64/Logic-Starter/wiki)
 
#### Prolog ####

 * [Learn Prolog Now](http://www.learnprolognow.org/)
 * [Artifical Intelligence through Prolog](http://faculty.nps.edu/ncrowe/book/book.html)
 * [The Art of Prolog, 2nd Edition](http://www.amazon.com/The-Art-Prolog-Second-Edition/dp/0262193388)
 * [Prolog Programming for Artificial Intelligence](http://www.amazon.com/Programming-Artificial-Intelligence-International-Computer/dp/0321417461)

## License ##

Copyright (C) 2011 Daniel L. Gregoire (semperos)

Distributed under the [Eclipse Public License](http://opensource.org/licenses/eclipse-1.0.php), the same as Clojure.
