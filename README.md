# WebDriver Logic #

The WebDriver Logic library provides a "mirror" of the [clj-webdriver](https://github.com/semperos/clj-webdriver) API using relations (see the [core.logic](https://github.com/clojure/core.logic) library), including a powerful, declarative syntax for authoring your functional web tests and letting webdriver-logic make inferences about the state of your application.

## Usage ##

**Note:** This library is in the earliest stages of development. Feedback is welcome; use at your own risk.

Here's a simple example where we find a single element with a class of `site-logo`:

```clj
(def b (clj-webdriver.core/start {:browser :chrome} "https://github.com"))

(run 1 [q]
  (classo b q "site-logo"))

;=> ({:webelement #<Tag: <a>, Class: site-logo, Href: https://github.com/, Object: [[ChromeDriver: chrome on MAC (6140efaa871769f2b7baa8fa885ebabc)] -> xpath: //*]>})
```

More examples forthcoming (and `classo`, given possible ambiguity, will probably be ditched for something more general like `valueo` or `attro`).

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
