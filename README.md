jpaannotationsprocessor
=======================

JPA Annotation Processor based on "Better JPA, Better JAXB, and Better Annotations Processing with Java SE 6"

==============================================================================
JPA ORM Annotations Processor README File
==============================================================================
                   README FILE FOR CODE ACCOMPANYING ARTICLE
"BETTER JPA, BETTER JAXB, AND BETTER ANNOTATIONS PROCESSING WITH JAVA SE 6"
------------------------------------------------------------------------------

DISCLAIMER:

The code accompanying the Oracle Technology Network (OTN) article
"Better JPA, Better JAXB, and Better Annotations Processing with Java SE 6" is
meant solely to illustrate concepts related to improved and integratd
annotations processing support and improved and integrated JAXB support in
Java SE 6.  The code samples included here were designed and written solely
to illustrate the APIs involved and to illustrate the concept of using
annotations processing and JAXB to write object-relational mapping files
(often named orm.xml) for JPA-based applications from in-source JPA-related
annotations.  The code included here was not designed, written, or tested for
production use.  Only a subset of JPA annotations are currently read from
within code and written out in the orm.xml file.  Even some of these
annotations are only partially supported (not all attributes of these
annotations are supported).  This code is merely a "proof of concept" for
the concept of being able to leverage Java SE 6's annotation processing
capability and JAXB capability to write orm.xml and/or other JPA OR mapping
files based on in-source annotations.


ABOUT THIS ANNOTATIONS PROCESSOR

This is a partial implementation of an annotations processor that processes
annotations related to Java Persistence API (JPA) object-relational mapping
(ORM).  Specifically, this processor writes out an XML file that describes
the object-relational mapping as described by the in-source annotations.
Not all JPA ORM annotations are handled by this partial implementation and not
all handled annotations are completely handled.  Rather, the real purpose
of this annotation processor is to demonstrate Java SE 6's built-in
annotations processing support and JAXB 2.0 support.  However, this processor
could be extended to be a more fully functional JPA ORM annotations processor
by following the same general approaches and patterns used in this partial
processor.


HOW TO CREATE AND BUILD THE JAVA CLASSES FOR THE JPA ORM SCHEMA

The code samples included here contain the already generated Java classes
that correspond to the JPA ORM XML Schema description.  They were generated
using the JAXB 2 implementation that is delivered with Java SE 6.  These
JAXB-generated classes do not need to be generated again, but explanation
of how to do so is included here for those who are interested.

The XML Schema that describes the XML we want our annotations processor to
generate to match in-code annotations is defined at
http://java.sun.com/xml/ns/persistence/orm_1_0.xsd.  This XML Schema file is
necessary to generate Java classes with JAXB.

JAXB 2.0 Reference Implementation (RI) is an integrated part of Java SE 6.
Its xjc binding compiler is used to create Java classes that map to the XML
Schema file noted above.  Use a command similar to the following command to
generate these classes (you will see the message "grammar is not specified"
if you forget to include the name of the XML Schema on the end [orm_1_0.xsd]
or if that particular XML Schema file is not in the current directory or
the directory specified by the path you put in front of the schema name
when typing in the command):

   xjc -p marx.jpa.persistence.jaxb -d C:\jpaJaxb\src orm_1_0.xsd

You can replace the value following -p (package specification) with your own
preferred package naming for the generated Java classes.  Likewise, you can
replace the -d (destination directory) with your own choice of source code
directory to which the JAXB-generated Java classes should be written.  The
last argument is the name of the XML Schema file to be used as the source
for generation of mapping Java classes (downloaded from URL above).

The above command will generate Java classes that bind to the provided XML
Schema and will place them under a subdirectory structure matching the -p
package under the -d source directory.

These generated classes should be compiled into .class files or even into a
.jar file that can then be placed in the classpath of the annotation processor
code (see next step).  In the accompanying code, these files are already
generated and the included Ant build.xml file compiles these already
JAXB-generated Java source files into .class files and then packages those
.class files into a single JAR to be used by the annotations processor.


HOW TO BUILD THE PROCESSOR

The custom processor depends on Java SE 6 interfaces and classes and the
integrated annotation processing support built-in to Java 6.  To keep things
simple, the code for this processor was limited to three classes:
   * AnnotationEntityProcessor.java
   * AnnotationEntityProcessorUtils.java
   * JpaAnnotationsConstants.java

These three classes can be built with the standard javac compiler if a JPA
provider's JPA library is in the classpath (such as the toplink-essentials.jar
from the JPA reference implementation) and if the JAXB-generated classes for
the ORM schema are in the classpath (see above).

An Ant build.xml file is included with the code (see below) to automate much
of this build process.  The instructions below detail how to set the
build.properties file appropriately to build correctly in your specific
environment.


ANT SCRIPT:

A build.xml file is included with the code sample that allows the code in
these examples to be built.  This build.xml file and its accompanying
build.properties file are in the 'root' directory of the code examples.
Additional details on running Ant with this build.xml file can be obtained
by running 'ant help' (assuming that you have Ant installed appropriately
in your environment).  The build.xml file should not require changes for
your environment, but the build.properties file almost certainly will require
some changes. These properties that require changes are marked with heavy
commenting in the build.properties file.

Once the appropriate settings have been made in the build.xml file, the main
targets you will want to run could be run on a single line:

	ant build runAnnotationsProcessor runTestDriverWithXml

Running 'ant -p' provides some descriptive information on major Ant targets
defined in this build.xml file, but additional detail on the most significant
targers is included here.  The main targets to use, once the build.properties
file has appropriate values assigned to all the properties, are 'build'
(builds the code and builds the database schema), 'clean' (removes the
compiled code and other derived files and cleans up the database schema),
'runAnnotationsProcessor' (runs the partial JPA annotation processor against
the sample JPA entity classes), and 'runTestDriverWithXml' (tests JPA entity
classes using generated orm.xml file to override/replace in-source
annotations).

'clean' target:
   Main target to use to clean up database, built .class files, and other
   generated files.  Calls some of the other 'clean' targets listed below.

'build' target:
   Main target for building three main packages of source code:
     * dustin.jpa package:
            Sample Java JPA-decorated entity classes.
     * marx.apt.jpa package:
            Annotations processing code that processes the JPA-related
            annotations in JPA entity source code and writes out equivalent
            orm.xml file.
     * marx.persistence.jpa.jaxb package:
            JAXB-generated classes generated based on the orm_1_0.xsd schema.
            Used here to write correct XML description for JPA object-
            relational mapping to override in-source annotations.

'runAnnotationsProcessor' target:
   This target is the main target that runs our JPA annotations processor
   (from the marx.apt.jpa package) against out sample JPA code (from the
   dustin.jpa package) and invokes the marx.persistence.jpa.jaxb package
   to write out the JPA ORM XML equivalent of the in-source JPA annotations
   in the sample code in dustin.jpa package.

'runTestDriverWithXml' target:
   This target should be run after the 'build' and 'runAnnotationsProcessor'
   targets have been run to test the annotations processing out.  It runs
   the sample code in dustin.jpa, but uses the generated orm.xml file
   (from runAnnotationsProcessor target) in lieu of the in-code annotations.

'javadoc' target:
    Use this target to create HTML documentation based on codes' Javadoc
    comments.

'compileAnnotationsProcessor' target:
   This compiles the Java source files that handle the processing of JPA
   annotations embedded within provided Java code and writes out the
   mapping information to an orm.xml file.

'compileJaxbOrmClasses' target:
   This compiles the JAXB-generated Java source files into .class files and
   then packages them into a single .jar file to be used by the annotation
   processing code.

'compileJpaCode' target:
   This complies the Java source files that are JPA entities and have been
   decorated with JPA annotations.

'buildDatabase' target:
   This builds the necessary 'Album'-themed tables in the database and
   populates them.  This target also leads to introduction of a DB sequence.

'cleanDatabase target:
   Removes the tables from the database that were added as a result of the
   'buildDatabase' target.  Also removes a sequence added by that target.



ADDITIONAL REFERENCES

1. OTN's JPA Annotations Reference is invaluable for writing a JPA
   annotations processor and is available at
   http://www.oracle.com/technology/products/ias/toplink/jpa/resources/toplink-jpa-annotations.html#Column
2. Other OTN JPA resources are available at
   http://www.oracle.com/technology/products/ias/toplink/jpa/index.html
