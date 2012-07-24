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

```
(run 3 [q]
  (fresh [an-element a-value]
    (attributeo b an-element :id a-value)
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

More and larger examples forthcoming.

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
