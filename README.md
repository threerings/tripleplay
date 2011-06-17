Triple Play
===========

Triple Play is a collection of game-related utility classes that can be used
with the [ForPlay](http://code.google.com/p/forplay) library on all of its
myriad platform targets.

Building
--------

The library is built using [Ant](http://ant.apache.org/).

Invoke `ant -p` to see documentation on the build targets.

Artifacts
---------

To add a Triple Play dependency to a Maven project, add the following to your
`pom.xml`:

    <repositories>
      <repository>
        <id>ooo-repo</id>
        <url>http://threerings.github.com/maven-repo</url>
      </repository>
    </repositories>
    <dependencies>
      <dependency>
        <groupId>com.threerings</groupId>
        <artifactId>tripleplay</artifactId>
        <version>1.0</version>
      </dependency>
    </dependencies>

To add it to an Ivy, SBT, or other Maven repository using project, simply
remove the vast majority of the boilerplate above.

If you prefer to download pre-built binaries, those can be had here:

* [tripleplay-1.0.jar](http://threerings.github.com/maven-repo/com/threerings/tripleplay/1.0/tripleplay-1.0.jar)

Distribution
------------

Triple Play is released under the New BSD License. The most recent version of
the library is available at http://github.com/threerings/tripleplay

Contact
-------

Questions, comments, and other worldly endeavors can be handled via the [Three
Rings Libraries](http://groups.google.com/group/ooo-libs) Google Group.
