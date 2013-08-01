# Maven for Ant Users

First let's compare the Ant build scripts with the Maven scripts (called "pom" files as they describe the "project object model" of your builds.)

In the Ant script (ant/hello/build.xml) you will see all the boiler-plate you've probably seen in a thousand Ant scripts. Running the build will do all the standard stuff you expect of a build: Compile source and tests, execute tests, build a jar, generate javadocs.

    $ cd ant/hello
    $ ant clean all
    $ ant hello-javadoc

Looking at the Maven pom file, (maven/hello/pom.xml) you'll notice immediately that it's only a few lines long, yet it will do everything that the Ant script will do:

    $ cd maven/hello
    $ mvn clean install
    $ mvn javadoc:javadoc

This is possible because Maven is *extremely* opinionated -- it makes a lot of assumptions about your project that are valid for 90% of situations.

## Phases and Goals

So what's happening here? Instead of executing an explicit list of arbitrary commands recipe-fashion like Ant is, Maven is executing pre-defined tasks (called "goals") according to phases in a fixed build lifecycle where there may be zero or more goals bound to each phase in the lifecycle. The reason the Maven build file is so short compared to the Ant one is that Maven has a set of default goals that it binds to the lifecycle phases for this type of project. These goals can be customised, but the defaults are fine for us here.

Maven build lifecycle phases (a non-exhaustive list):

* generate-sources
* compile
* test-compile
* test
* package
* integration-test
* verify
* install
* deploy

Maven clean lifecycle phases:

* pre-clean
* clean

You can immediately see that in the command executed earlier, the "clean" and "install" arguments passed into the Maven command correspond to phases in the above lifecycles. When you execute a specific lifecycle phase, Maven will also execute all the preceeding phases, for example the command:

    $ mvn test

Will execute the all the goals bound to the "generate-sources" "compile" "test-compile" and "test" phases, in that order.

# Plugins

You may have guessed that the "javadoc:javadoc" command does something different. By default, there is no javadoc-generating goal bound to any lifecycle phase, which is why it was executed separately above. It's important to note at this point that all goals are provided by plugins for Maven and what you see in the command to generate the javadocs is the syntax to execute a specific goal provided by a specific plugin. The syntax is "plugin-name:goal-name" for example:

    $ mvn jar:test-jar

Will execute a goal called "test-jar" that is provided by the "jar" plugin. The "test-jar" goal creates a jar file containing a project's test classes -- this is not a goal that is bound to any lifecycle phase by default, but it is instructive to see that Maven gives you a lot of commonly needed functionality without having to write lots of build script boilerplate or even configure anything.

The goals (and the plugins that provide them) that are bound to the lifecycle phases by default are determined by a Maven project's packaging. The hello project pom file contains a <packaging/> tag that specifies jar packaging for example that instructs Maven to bind all the goals necessary to build your project into a jar file. There is also an example web project, hello-web, whose pom file specified war packaging, which is similar but will instruct Maven to bind all the goals necessary to build your project into a war file (for use in Java web containers) instead.

You can see which plugins are providing which goals by inspecting what is called the "effective" pom:

    $ cd maven/hello
    $ mvn help:effective-pom | less

This goal from the "help" plugin outputs what your project's pom file would look like if you were to explicitly specify all the defaults that Maven assumes that are not overidden by your project-specific configuration -- it is the effective pom used internally by Maven during the build. If you compare the effective pom of the hello project to that of the hello-web project:

    $ cd maven/hello-web
    $ mvn help:effective-pom | less

You can see that the only real difference between a jar-packaged project and a war-packaged project is the use of the "war" plugin instead of the "jar" plugin. The effective poms are probably longer than the equivilent ant script in some cases, but at least we didn't have to write it. :-)

# Customising Builds

If you wish to override the default configuration for any of the plugins you see in the effective pom, or bind more goals to the lifecycle phases that are not bound by default, then you can add configuration to the project pom file.

For example, the "compiler" plugin compiles code to Java 1.5 bytecode by default. If we wanted to target newer JREs, we could configure it to generate Java 1.6 bytecode by adding to the project's pom file the snippet contained in the "maven/hello/compiler-snippet.xml" file.

If we wanted to automatically build a jar containing the tests during the build, then we can bind the "jar:test-jar" goal that we executed earlier to an appropriate lifecycle phase. Add the snippet contained in the "maven/hello/jar-snippet.xml" file to the pom file to bind the test-jar goal to the "package" lifecycle phase. Now when you execute "mvn clean install" you will get two jar files instead of one.

# Dependency Management

One thing you see all the time in Ant-based projects is commiting all of your project's dependencies to source control. You inevitable want to use a third-party library in a project, but Ant has no mechanism to resolve or manage artifacts that your project might depend on. The easiest and therefore most common way of dealing with this is to commit the third-party libraries directly to source control. The problem with this is that it gets out of control quickly because not only do you have to have your direct dependencies, but your dependencys' dependencies, etc. 

Maven on the other hand, has internal magic that handles the resolution and download of dependencies automatically so that you don't have to worry about bundling them with your source code. All you have to do is add a reference to the dependency in your project's pom file. As a basic example, if you search for jar files in the Ant version of hello-web, you'll see some jar file, we actually need all the right versions of the dependencies to be present before we can event start the build:

    $ cd ant/hello-web
    $ find -name "*.jar"
    $ ant clean all

Whereas in the maven version of hello-web, you will see that there are no jar files present in the project because they are downloaded as part of the build process:

    $ cd maven/hello-web
    $ find -name "*.jar"
    $ mvn clean install

And you can just add references to new dependencies in the pom file when you need them. For example, try adding the snippet contained in the "maven/hello-web/hibernate-snippet.xml" file to the hello-web pom file. Now when you rebuild the web app, not only will the hibernate-core jar be included in the war file, but all of hibernate-core's dependencies too (non-direct dependencies of your project are generally called "transitive" dependencies.)

You can also specify the scope of the dependency in the pom file using the <scope/> tag -- you will have noticed that dependencies specified with test or provided scope are not bundled up inside the war file. The dependency scopes are defined as:

* compile - the default, these dependencies are available in all classpaths
* runtime - these dependencies are not required for compilation, they will only be available in the runtime and test classpaths
* provided - same as compile but the dependency is expected to be provided by the execution environment or container
* test - only available in the test classpaths
* system - like provided but you are expected to provide the dependency yourself
