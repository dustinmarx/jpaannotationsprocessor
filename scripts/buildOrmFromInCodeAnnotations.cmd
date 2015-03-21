REM -----------------------
REM - Process Annotations -
REM -----------------------

REM ----- Set up environment variables for script -----
REM -- Change these variables to match your setup.
REM -- Assumes files unzipped in same format they were
REM -- originally zipped in and unzipped in C: drive.
REM -- Change the HOME_DIR, possibly the MAIN_DIR,
REM -- and the JPA_LIB variables appropriately
REM -- to map to your environment, but the remainder of
REM -- the variables defined here should be okay as-is.
set HOME_DIR=C:
set MAIN_DIR=%HOME_DIR%\otnJava6
set JPA_LIB=%HOME_DIR%\glassfish-persistence-b50\toplink-essentials.jar
set SOURCE_DIR=%MAIN_DIR%\src
set CLASSES_DIR=%MAIN_DIR%\classes
set JAR_DIR=%MAIN_DIR%\jar
set JPA_PACKAGE=dustin\jpa
set ANNOTATIONS_PACKAGE=marx\apt\jpa

REM ----- Display environment variables to be used -----
echo HOME_DIR: %HOME_DIR%
echo MAIN_DIR: %MAIN_DIR%
echo SOURCE_DIR: %SOURCE_DIR%
echo CLASSES_DIR: %CLASSES_DIR%
echo JPA_PACKAGE: %JPA_PACKAGE%
echo ANNOTATIONS_PACKAGE: %ANNOTATIONS_PACKAGE%

REM ----- Change to directory with JPA-annotated classes -----
cd %SOURCE_DIR%\%JPA_PACKAGE%

REM ----- Run annotations processor -----
javac -processor marx.apt.jpa.AnnotationEntityProcessor -proc:only -processorpath %CLASSES_DIR%;%JAR_DIR%\jpa-jaxb.jar;%JPA_LIB%;%CLASSES_DIR%\%ANNOTATIONS_PACKAGE% -cp %JPA_LIB% -AxmlOverrideAnnotations=true -AuseUpperCaseColumnNames=true -Xlint:unchecked *.java

cd %MAIN_DIR%\scripts

REM ----- Clear/unset variables used in this script -----
set MAIN_DIR=
set HOME_DIR=
set SOURCE_DIR=
set JPA_PACKAGE=
set ANNOTATIONS_PACKAGE=
