# WebDriver Logic #

The WebDriver Logic library provides a "mirror" of the [clj-webdriver](https://github.com/semperos/clj-webdriver) API using relations (see the [core.logic](https://github.com/clojure/core.logic) library), including a powerful, declarative syntax for authoring your functional web tests and letting webdriver-logic make inferences about the state of your application.

## Usage ##

**Note:** This library is in the earliest stages of development. Feedback is welcome; use at your own risk.

Check out the ClojureConj talk : [Web Testing with Logic Programming ](http://www.youtube.com/watch?v=09zlcS49zL0)

### Exploration ###

Here's a simple example where we find the first element with a class of `footer_nav`:

```clj
(use 'webdriver-logic.core)
(require '[clj-webdriver.core :as wd])
(set-driver! {:browser :chrome} "https://github.com")

(run 1 [q]
  (attributeo q :class "footer_nav"))
;=> ({:webelement #<Tag: <ul>, Text: GitHub  About  Blog  Features  Contact & Support  Training  ..., Class: footer_nav, Object: [[ChromeDriver: chrome on MAC (26459fb4e495c6bf086ea92acbaa7715)] -> xpath: //*]>})
```

Or how about all the footer navigation sections?

```clj
(run* [q]
  (attributeo q :class "footer_nav"))
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
    (attributeo an-element :id a-value)
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

And if we limit the search domain to a sub-set of elements on the page (in this case, only `div` elements):

```clj
(binding [*search-domain* {:xpath "//div"}]
  (run 3 [q]
       (fresh [an-element a-value]
              (attributeo an-element :id a-value)
              (!= a-value nil)
              (!= a-value "")
              (== q [a-value an-element]))))
;=>
;; (["wrapper"
;;   {:webelement
;;    #<Tag: <div>, Text: Signup and Pricing  Explore GitHub  Features  Blog  Sign in ..., Id: wrapper, Object: [[ChromeDriver: chrome on MAC (1fc632cc0ded7fc2c7fa1db418329876)] -> xpath: //div]>}]
;;  ["header"
;;   {:webelement
;;    #<Tag: <div>, Text: Signup and Pricing  Explore GitHub  Features  Blog  Sign in, Id: header, Class: true clearfix, Object: [[ChromeDriver: chrome on MAC (1fc632cc0ded7fc2c7fa1db418329876)] -> xpath: //div]>}]
;;  ["footer-push"
;;   {:webelement
;;    #<Tag: <div>, Id: footer-push, Object: [[ChromeDriver: chrome on MAC (1fc632cc0ded7fc2c7fa1db418329876)] -> xpath: //div]>}])
```

Pretty simple - you could do that with regular CSS or XPath queries. One could argue, however, that even at this simple point the declarative nature of `run*` is easier to follow and reason about than a series of explicit `find-element`, `filter` or `remove` calls.

Let's make the inference work harder for us. Are there two links included in both the header and footer that have the same `href` value?

```clj
(binding [*search-domain* {:xpath "//a"}
          *child-search-domain* {:xpath ".//a"}]
  (run 2 [q]
     (fresh [header-el footer-el the-href-value]
            (attributeo header-el :href the-href-value)
            (attributeo footer-el :href the-href-value)
            (!= the-href-value nil)
            (!= the-href-value "")
            (childo header-el (wd/find-element driver {:id "header"}))
            (childo footer-el (wd/find-element driver {:id "footer"}))
            (== q [the-href-value header-el footer-el]))))
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

You'll notice again the binding of `*search-domain*` and `*child-search-domain*` to a subset of all anchor elements on the page. Though this is not necessary for the program to run, it drastically improves performance. Relations like `attributeo` have to traverse all the elements on the page to find an answer, which for Selenium-WebDriver means creating lots of objects.

You can `flash` these elements to convince yourself that the above works:

```clj
(require '[clj-webdriver.element :as el])

(doseq [[_ h f] (binding [*search-domain* {:xpath "//a"}
                          *child-search-domain* {:xpath ".//a"}]
                  (run 2 [q]
                    (fresh [header-el footer-el the-href-value]
                      (attributeo header-el :href the-href-value)
                      (attributeo footer-el :href the-href-value)
                      (!= the-href-value nil)
                      (!= the-href-value "")
                      (childo header-el (wd/find-element driver {:id "header"}))
                      (childo footer-el (wd/find-element driver {:id "footer"}))
                      (== q [the-href-value header-el footer-el]))))]
  (wd/flash h)
  (wd/flash f))
```

Larger and more meaningful examples forthcoming.

### Writing Your Tests ###

Given that core.logic returns values that can be consumed by "regular" functions, it's not hard to imagine how to compose tests against those values. But in the spirit of [Prolog unit tests](http://www.swi-prolog.org/pldoc/package/plunit.html), webdriver-logic provides a handful of macros that wrap clojure.test's `is` macro for common Logic Programming cases:

 * `s` - Succeeds if a single value is returned from the relation (deterministic behavior)
 * `s+` - Succeeds if more than one value is returned from the relation (non-deterministic behavior)
 * `s-as` - Succeeds if its first parameter `=` the value returned from the relation
 * `s-includes` - Succeeds if its first parameter contains values that are included in the value returned from the relation
 * `s?` - Succeeds if its first parameter, a predicate, returns true when passed the **seq of values** returned from the relation
 * `u` - Succeeds if the relation fails
 
Remember that core.logic `run*` and friends always return a seq of zero or more values.

For examples of these in action, please see this library's test suite.

### Contributing ###

This library is currently in the earliest stages of development. Pull requests against master are welcome, preferably authored in a feature branch.

Run the tests with Leiningen:

```
lein test
```

*Note:* If you just want to run the example app that webdriver-logic uses for its testing purposes, do the following:

 * Open a terminal and run `lein repl` or at the root of this project
 * Evaluate `(use 'webdriver-logic.test.example-app.core 'ring.adapter.jetty)`
 * Evaluate `(defonce my-server (run-jetty #'routes {:port 5744, :join? false}))` (make sure your port selection doesn't conflict with actual test runs)

### Logic Programming Materials ###

#### Clojure/Lisp ####
 
 * [The Reasoned Schemer](http://mitpress.mit.edu/catalog/item/default.asp?ttype=2&tid=10663) (also [available in Kindle format](http://www.amazon.com/The-Reasoned-Schemer-ebook/dp/B004GEBQS6/ref=kinw_dp_ke?ie=UTF8&m=AG56TWVU5XWC2))
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
