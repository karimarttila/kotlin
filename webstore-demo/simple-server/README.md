# Kotlin Simple Server  <!-- omit in toc -->


# Table of Contents  <!-- omit in toc -->

- [Introduction](#introduction)
- [Kotlin vs. Java vs. Clojure](#kotlin-vs-java-vs-clojure)
- [Tools and Versions](#tools-and-versions)
- [Gradle](#gradle)
- [IDE](#ide)
- [Configuration](#configuration)
- [Resource Hassle](#resource-hassle)
- [Ktor](#ktor)
- [Tests](#tests)
- [Fat Jar](#fat-jar)
- [Summary](#summary)



# Introduction

This Kotlin version of my exercise server "Simple Server" is now the sixth language that I have used to implement the same server as an exercise (previous implementations: Clojure, Javascript, Java, Python and Go, see: my Medium blog post regarding these exercises: [Five Languages — Five Stories](https://medium.com/@kari.marttila/five-languages-five-stories-1afd7b0b583f)). I have written a new blog post regarding this Kotlin exercise: [Kotlin — Much More Than Just a Better Java](https://medium.com/@kari.marttila/kotlin-much-more-than-just-a-better-java-883b1b625220).

In this README.md document (and in the above mentioned Medium blog post) I document the implementation details of this Kotlin exercise and also some observations regarding Kotlin as a backend language.

# Kotlin vs. Java vs. Clojure

I actually started to learn Kotlin already some four years ago. But after a short introduction I thought that "Kotlin is just Java done right" and decided to deep dive into Clojure instead (which actually was a good decision - Clojure is an excellent language and very different when compared to Java and Kotlin). I worked this autumn with an interesting customer which had a team of extremely competent developers who were also a bunch of real language enthusiastics. They had implemented many microservices using different languages, one of the languages being Kotlin. In that assignment I had a chance to convert one old Java application to Kotlin. Therefore I had a good reason to learn Kotlin and I also decided to implement my exercise server using Kotlin.

After doing some diving into Kotlin I realized that Kotlin actually is much more than just "Java done right". Kotlin is a very well designed language and you can use Kotlin quite naturally either using object-oriented paradigm or functional paradigm. I decided to implement this Kotlin version of Simple Server using functional paradigm to compare this side also to Clojure.

My main impressions regarding Kotlin as a language are:
- **Kotlin is really easy**. If you have used Java you have absolutely no issues to learn Kotlin. Kotlin has actually simplified creating backend systems using a JVM language quite a bit compared to Java.
- **Kotlin is pretty concise**. Example: ```data class User(val email: String, val firstName: String, val LastName: String, val password: String)```, i.e. you have a valid data class ready to be used without any Java getter/setter boilerplate. Since Kotlin is both easy and concise new developers will be productive in a short period of time.
- **Expressions.** I really liked Kotlin's idea that most of the things that are statements in Java (if-else, try-catch...) are expressions in Kotlin (i.e. you return values from those constructs) - this idea supports a lot using Kotlin as a functional language.
- **Mixing Java and Kotlin.** You can mix Java and Kotlin files without any extra boilerplate in your project. This makes migration from Java to Kotlin extremely easy: just convert the files one at a time as a need basis. I also tried IntelliJ IDEA's Java-to-Kotlin migration tool and it was astonishingly good - there were only a few cases where the tool couldn't figure out how to do the conversion. See: [Mixing Java and Kotlin in one project](https://kotlinlang.org/docs/tutorials/mixing-java-kotlin-intellij.html) Kotlin documentation.
- **Null pointer safety.** Kotlin provides language constructs to avoid most of the null pointer exception errors. See: [Null Safety](https://kotlinlang.org/docs/reference/null-safety.html) Kotlin documentation.
- **IDE tooling.** IntelliJ IDEA is a great Kotlin IDE, of course since the same company - JetBrains - is behind the language and the IDE. See: [Getting Started with IntelliJ IDEA](https://kotlinlang.org/docs/tutorials/getting-started.html) Kotlin documentation.
- **Gradle and Kotlin.** You can use Kotlin as a Gradle domain specific language (DSL). See: [Using Gradle](https://kotlinlang.org/docs/reference/using-gradle.html) Kotlin documentation.
- **Lesser need for exceptions**. Kotlin provides nice constructs that you can use to pass information e.g. whether you have found results for some query or not using ```sealed class```. See [Sealed Classes](https://kotlinlang.org/docs/reference/sealed-classes.html) in Kotlin documentation for more information. Example:

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
This makes code pretty readable: "If you didn't find any products you cannot find an individual product. If you found products you can try to find an individual product by pId field." Because ```when``` is an expression you finally return some value to variable ```ret```. 

When comparing Kotlin to Clojure my first impression is: "Kotlin is pretty good but compared to Clojure Kotlin is just Java done right". With this statement I mean that Clojure being a Lisp has all the power of Lisp including a real REPL and a real functional language with immutability by default, and excellent data oriented language structures and standard library to process data. If I started a new project with very competent developers who already know Clojure or are willing to learn it my choice of language would definitely be Clojure since with competent developers Clojure is like a secret weapon when considering developer productivity. But if Clojure is not an option and I had to choose either Java or Kotlin I would choose Kotlin hands down. So, considering JVM development I'm starting to think that there is no going back to Java. Sorry Java, you had your glory days but now it is time for new guys to continue JVM journey.


# Tools and Versions

I used the following tools and versions when implementing this Kotlin exercise:

- [OpenJdk 11](http://openjdk.java.net/): openjdk 11.0.4 2019-07-16
- [Gradle](https://gradle.org/): 5.6.3 / Kotlin: 1.3.41 / Groovy: 2.5.4
- [Kotlin](https://kotlinlang.org/): 1.3.50
- [Ktor](https://ktor.io/): 1.2.6



# Gradle

You can create the gradle wrapper using command:

```bash
gradle wrapper
```

Gradle wrapper is also provided in this Git repo in [gradle](gradle) directory.


# IDE

[IntelliJ IDEA](https://www.jetbrains.com/idea/) is my favorite Java IDE (and also my favourite Clojure and Python IDE and also from now on my favourite Kotlin IDE). IntelliJ IDEA works with Kotlin like a charm, of course, since Kotlin is created by JetBrains which is the company behind IntelliJ IDE products as well (if I understood correctly JetBrains created Kotlin specifically because they wanted to have a better language to build their IDEs).


# Configuration

Configuration was a bit of a hassle. First I wanted to use Nat Pryce's excellent [konfig](https://github.com/npryce/konfig) configuration library. But then I realized that Ktor uses its own configuration mechanism. I didn't want to use two different configuration files and therefore I had two options. #1. Use konfig but provide Ktor a mechanism to use konfig as a custom configuration. I experiemented this choice a bit but couldn't find a good solution fast enough. #2. Use just [Ktor's own configuration mechanism](https://ktor.io/servers/configuration.html). I decided to go this way and provided in [utils.kt](https://github.com/karimarttila/kotlin/blob/master/webstore-demo/simple-server/src/main/kotlin/simpleserver/util/utils.kt) a couple auxiliary functions to read the property values from Ktor's HoconApplicationConfig. Well, at least this way I just needed one configuration file. But honestly I'm not quite as satisfied with this solution as in the Java version of Simple Server in which I could pass the configuration environment to the application using SS_ENV environment variable. Anyway, I didn't want to spend too much time with the configuration mechanism since this was just an exercise to learn Kotlin.


# Resource Hassle

When I thought I was done with this exercise I finally tested the application also running it using the fat jar (and not from IntelliJ IDEA) - of course the application couldn't find the csv files inside its jar. Therefore I had to implement two mechanisms for reading the csv files: either using the classloader or manually from given configuration path [utils.kt](https://github.com/karimarttila/kotlin/blob/master/webstore-demo/simple-server/src/main/kotlin/simpleserver/util/utils.kt):

```kotlin
    ...
    val resourceDir = getStringPropertyOrNull("misc.resourcedir")
    ...
            val url = if (resourceDir == null)
                SSResource::class.java.classLoader.getResource(fileName)
            else
                File("${resourceDir}/${fileName}").toURI().toURL()
    ...
```

Most probably there is a cleaner way to read the csv files inside the fat jar but I didn't bother to google it - I'm a bit in a hurry to go to my next exercise.


# Ktor

 I used [Ktor](https://ktor.io/) http server framework. Ktor provides all functionality for routing, cors etc. and it is very easy to use. Ktor also provides really nice test engine which is really easy and fast to use (see next chapter).


# Tests

I used [JUnit5](https://junit.org/junit5/docs/current/user-guide/) which was easy to configure in Kotlin's [build.gradle.kts](https://github.com/karimarttila/kotlin/blob/master/webstore-demo/simple-server/build.gradle.kts).

For web server testing I used Ktor's [TestEngine](https://ktor.io/servers/testing.html). It was a delightful surprise. The Ktor Test engine is really fast since it doesn't create the actual web server but instead just hooks directly to internal Ktor mechanisms. See examples in [serverTest.kt](https://github.com/karimarttila/kotlin/blob/master/webstore-demo/simple-server/src/test/kotlin/simpleserver/webserver/serverTest.kt).


You can run the tests as:

```bash
./gradlew clean test
```
 
 
 # Fat Jar

 You can build the fat jar as:

```bash
./gradlew clean test
```

... and then run the application in command line using the ```./run-server.sh``` script (but first edit the file and provide the path to resources directory).


# Summary

Kotlin as a language is really easy to learn and use. If I have a choice to use either Java or Kotlin I would choose Kotlin. Comparing Kotlin to Clojure is much more difficult since the languages are pretty different and also the development models in these two languages are different (Clojure's REPL driven development vs. Kotlin's more traditional programming development).
