# Kotlin Simple Server  <!-- omit in toc -->


# Table of Contents  <!-- omit in toc -->

- [TODO: WORK IN PROGRESS](#todo-work-in-progress)
- [Introduction](#introduction)
- [Kotlin vs Java vs Clojure](#kotlin-vs-java-vs-clojure)
- [Configuration](#configuration)
- [Using JDK11](#using-jdk11)


# TODO: WORK IN PROGRESS

TODO: I remove this chapter when this exercise is done.


# Introduction

This Kotlin version of my exercise server "Simple Server" is now the sixth language that I have used to implement the same server as an exercise (previous implementations: Clojure, Javascript, Java, Python and Go). In this blog post I document the implementation details and also some observations regarding Kotlin as a backend language and especially compare it to Java.

# Kotlin vs Java vs Clojure

I actually started to learn Kotlin already some four years ago. But after a short introduction I thought that "Kotlin is just Java done right" and decided to deep dive into Clojure (which actually was a good decision - Clojure is an excellent language and very different when compared to Java and Kotlin). But this autumn I spent with an interesting customer which had a team of extremely competent developers who were also quite language enthusiastic. They had implemented many microservices using different languages, one of the languages being Kotlin. I also had a chance to convert one old Java application into Kotlin. Therefore I had a good reason to learn Kotlin and I studied Kotlin in the evenings this autumn. 

After doing some diving into Kotlin I realized that Kotlin actually is much more than just "Java done right". Kotlin is very well designed language and you can use Kotlin quite naturally either using object-oriented paradigm or functional paradigm. I decided to implement this Kotlin version of Simple Server using functional paradigm to compare this side also to Clojure. 

My main impressions are:
-  Kotlin is really easy. If you have used Java you have absolutely no issues to learn Kotlin. Kotlin has actually simplified creating backend systems using a JVM language quite a bit compared to Java.
- Kotlin is pretty concise. Example: ```data class User(val email: String, val firstName: String, val LastName: String, val password: String)```, i.e. you have a valid data class ready to be used without any getter/setter boilerplate. Since Kotlin is both easy and concise new developers will be productive in a short period of time and they most probably will be more productive using Kotlin than Java since Kotlin is easier to use and concise.
- I really liked Kotlin's idea that all the stuff that are statements in Java (if-else, try-catch...) are expressions in Kotlin (i.e. you return values from those constructs) - this idea supports a lot using Kotlin as a functional language.
- You can mix Java and Kotlin files without any extra boilerplate in your project. This makes migration from Java to Kotlin extremely easy: just convert the files one at a time as a need basis. I also tried IntelliJ IDEA's Java-to-Kotlin migration tool and it was astonishingly good - there were only a few cases where the tool couldn't figure out how to do the conversion. 
-  Kotlin provides language constructs to avoid most of the null pointer exception type errors.
- Lesser need for exceptions. Kotlin provides nice constructs that you can use to pass information e.g. whether you have found results for some query or not using ```sealed class```. Example: 

```kotlin
sealed class ProductsResult
data class ProductsFound(val data: List<Product>) : ProductsResult()
object ProductsNotFound : ProductsResult()
```
Then you can use these sealed classes quite nicely to process various happy-day scenarios and exceptional scenarios, example:

```kotlin
    val ret = when (val products = getProducts(pgId)) {
        is ProductsNotFound -> ProductNotFound
        is ProductsFound -> {
            when (val p = products.data.firstOrNull { it.pId == pId }) {
                null -> ProductNotFound
                p -> ProductFound(p)
                else -> ProductNotFound
            }
        }
    }
```
This makes code pretty readable: "If you didn't find any products you cannot find an individual product. If you found products you can try to find an individual product." Because ```when``` is an expression you finally return some value to variable ```ret```.

When comparing Kotlin to Clojure my first impression is: "Kotlin is pretty good but compared to Clojure Kotlin is just Java done right". With this statement I mean that Clojure being a Lisp has all the power of Lisp including a real REPL and a real functional language with immutability by default, and excellent data oriented language structures and standard library to process data. If I can start a new project with very competent developers who already know Clojure or are willing to learn it my choice of language would definitely be Clojure since with competent developers Clojure is like a secret weapon when considering developer productivity. But if Clojure is not an option and I had to choose either Java or Kotlin I would choose Kotlin hands down. So, considering JVM development I'm starting to think that there is no going back to Java days. Sorry Java, you had your glory days but now it is time for new guys to continue JVM journey.  




# Configuration

Configuration was a bit of a hassle. First I wanted to use Nat Pryce's excellent [konfig](https://github.com/npryce/konfig) configuration library. But then I realized that Ktor uses its own configuration mechanism. I didn't want to use two different configuration files and therefore I had two options. #1. Use konfig but provide Ktor a mechanism to use custom configuration. I experiemented with this but it didn't turn out to be very straight forward. #2. Use just [Ktor's own configuration mechanism](https://ktor.io/servers/configuration.html). I decided to go this way and provided in [utils.kt](https://github.com/karimarttila/kotlin/blob/master/webstore-demo/simple-server/src/main/kotlin/simpleserver/util/utils.kt) an auxiliary function to read the property values from Ktor's HoconApplicationConfig. Well, at least this way I just need one configuration file. But honestly I wasn't quite as satisfied with this solution as in the Java version of Simple Server in which I could pass the configuration using SS_ENV environment variable. But I didn't want to spend too much time with this issue - this is just an exercise to learn Kotlin anyway. 

# Using JDK11

TODO: See ../../setenv.sh

