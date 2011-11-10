# WebDriver Logic #

The WebDriver Logic library provides a "mirror" of the [clj-webdriver](https://github.com/semperos/clj-webdriver) API, replacing its functions with ones that return goals, as understood by Clojure's [core.logic](https://github.com/clojure/core.logic) library. This allows tests to be written using logic programming (LP) constructs.

## Usage ##

**Note:** This library is in the earliest stages of development. Feedback is welcome; use at your own risk.

Instead of encapsulating web testing logic within complex conditional forms, you can more easily define a set of relations for individual components of your web application, and compose them as needed with LP-specific mechanisms that are arguably more intuitive for testing purposes.

Here's a simple example:

```clj
(def b (clj-webdriver.core/start :firefox "https://github.com"))
(def a-link (clj-webdriver.core/find-it b :a {:text "Login"}))

(run* [q]
  (texto a-link "Login"))
  
;=> (_.0)
```

The `(_.0)` indicates that all relations succeeded. In this case, I used the `:text` attribute to find the element, so this isn't particularly impressive. I can use other relations to discover new information about my element:

```clj
(run* [q]
  (fresh [a]
    (attributeo a-link :href a)
    (== q a)))
    
;=> ("https://github.com/login")
```

In the above example, we bind a fresh variable `a` to what the `attributeo` relation can discover about the element based on what is passed in. This also isn't particularly impressive, since it relies directly on clj-webdriver's API to extract that information, something you could easily do just by using that API.

It is in the manner of composing higher levels of abstraction in your web tests that webdriver-logic's functionality is particularly useful.

### Modeling your App in Tests ###

Let's take the state of being logged in or logged out as an example. On Github, if you are logged out, you see two links: "Signup and Pricing" and "Login". Let's capture this as a relation:

```clj
(defn logged-outo
  [driver]
  (let [price-link (wd/find-it driver :a {:text "Signup and Pricing"})
        log-link (wd/find-it driver :a {:text "Login"}) ]
     (visibleo price-link)
     (visibleo log-link)))
```

The `logged-outo` relation can now be used anywhere, succeeding only when both the `price-link` and `log-link` relations themselves succeed.

Again, how does this differ from "normal functions?" If you were to stop here, there wouldn't be much benefit to couching everything in logical terms. The real power comes in using things like `conde` and `matche` to express complex logical states and series of successes and/or failures based on your applications workflows. An example leveraging these will be forthcoming, but in the meantime, you can get an idea of the expressivity of these constructs by reading [The Reasoned Schemer](http://mitpress.mit.edu/catalog/item/default.asp?ttype=2&tid=10663) (also [available in Kindle format](http://www.amazon.com/The-Reasoned-Schemer-ebook/dp/B004GEBQS6/ref=kinw_dp_ke?ie=UTF8&m=AG56TWVU5XWC2) or Ambrose Bonnaire-Sergeant's excellent [Logic Starter tutorial](https://github.com/frenchy64/Logic-Starter/wiki).

## License ##

Copyright (C) 2011 Daniel L. Gregoire (semperos)

Distributed under the [Eclipse Public License](http://opensource.org/licenses/eclipse-1.0.php), the same as Clojure.
