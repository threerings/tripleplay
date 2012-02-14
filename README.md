Triple Play
===========

Triple Play is a collection of game-related utility classes that can be used
with the [PlayN] library on all of its myriad platform targets.

* [API documentation] is available.

Building
--------

The library is built using [SBT] or [Maven].

Invoke `xsbt publish-local` to build and install the library to your local
Ivy repository (i.e. `~/.ivy2/local`).

Invoke `mvn install` to build and install the library to your local Maven
repository (i.e. `~/.m2/repository`).

Artifacts
---------

To add a Triple Play dependency to a Maven project, add the following to your
`pom.xml`:

    <dependencies>
      <dependency>
        <groupId>com.threerings</groupId>
        <artifactId>tripleplay</artifactId>
        <version>1.1</version>
      </dependency>
    </dependencies>

To add it to an Ivy, SBT, or other Maven repository using project, simply
remove the vast majority of the boilerplate above.

If you prefer to download pre-built binaries, those can be had here:

* [tripleplay-1.1.jar](http://repo2.maven.org/maven2/com/threerings/tripleplay/1.1/tripleplay-1.1.jar)

GWT
---

When using TriplePlay in a [PlayN] game that targets the HTML5 or Flash
backends, you must also add a reference to the [GWT] module to your
`FooGame.gwt.xml` file, like so:

    <inherits name="tripleplay.TriplePlay"/>

Distribution
------------

Triple Play is released under the New BSD License. The most recent version of
the library is available at http://github.com/threerings/tripleplay

Contact
-------

Questions, comments, and other worldly endeavors can be handled via the [Three
Rings Libraries](http://groups.google.com/group/ooo-libs) Google Group.

[PlayN]: http://code.google.com/p/playn
[API documentation]: http://threerings.github.com/tripleplay/apidocs/overview-summary.html
[SBT]: http://github.com/harrah/xsbt/wiki/Setup
[Maven]: http://maven.apache.org/
[GWT]: http://code.google.com/webtoolkit/
