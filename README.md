Triple Play
===========

Triple Play is a collection of game-related utility classes that can be used with the [PlayN]
library on all of its myriad platform targets.

Various documentation-like-things are available:
* [Release Notes]
* A [demo of the UI toolkit]
* [API documentation]

Building
--------

The library is built using [Maven] or [SBT].

Invoke `mvn install` to build and install the library to your local Maven repository (i.e.
`~/.m2/repository`).

Invoke `sbt publish-local` to build and install the library to your local Ivy repository (i.e.
`~/.ivy2/local`).

- To deploy artifacts to bintray
	```
	cd tripleplay
	mvn clean -Pall
	mvn versions:set -Pall -DnewVersion=2.0.1
	git commit -am "Release version"
	git tag tripleplay-2.0.1
	git push --tags
	mvn clean -Pall
	mvn install -Pall -DskipTests -DskipExec
	mvn deploy -Prelease -Pall -DskipTests -DskipExec
	mvn versions:set -Pall -DnewVersion=2.0.2-SNAPSHOT
	git commit -am "Next snapshot version"
	git push
	```

- To release
	```
	cd playn
	mvn release:prepare release:perform -DskipTests=true -Prelease -Darguments="-DskipTests=true -Prelease"
	```

Artifacts
---------

To add a Triple Play dependency to a Maven project, add the following to your `pom.xml`:

    <dependencies>
      <dependency>
        <groupId>com.threerings</groupId>
        <artifactId>tripleplay</artifactId>
        <version>${playn.version}</version>
      </dependency>
    </dependencies>

GWT/HTML5
---------

When using Triple Play in a [PlayN] game that targets the HTML5 or Flash backends, you must make
some additional changes.

Add the following to your `html/pom.xml` (and/or `flash/pom.xml`):

    <dependencies>
      <dependency>
        <groupId>com.threerings</groupId>
        <artifactId>tripleplay</artifactId>
        <version>${playn.version}</version>
        <classifier>sources</classifier>
      </dependency>
    </dependencies>

Add a reference to the Triple Play [GWT] module to your `FooGame.gwt.xml` file, like so:

    <inherits name="tripleplay.TriplePlay"/>

Finally modify the gwt-maven-plugin to override `disableClassMetadata` which PlayN enables by
default:

    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>gwt-maven-plugin</artifactId>
      <configuration>
        <disableClassMetadata>false</disableClassMetadata>
      </configuration>
      ...
    </plugin>

Distribution
------------

Triple Play is released under the New BSD License. The most recent version of the library is
available at https://github.com/threerings/tripleplay

Contact
-------

Questions, comments, and other worldly endeavors can be handled via the
[Three Rings Libraries](http://groups.google.com/group/ooo-libs) Google Group.

[API documentation]: http://threerings.github.com/tripleplay/apidocs/overview-summary.html
[GWT]: http://code.google.com/webtoolkit/
[Maven]: http://maven.apache.org/
[PlayN]: http://code.google.com/p/playn
[Release Notes]: https://github.com/threerings/tripleplay/wiki/ReleaseNotes
[SBT]: http://github.com/harrah/xsbt/wiki/Setup
[demo of the UI toolkit]: http://threerings.github.com/tripleplay/widgetdemo.html
