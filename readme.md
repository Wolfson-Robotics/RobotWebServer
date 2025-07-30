# Instructions
Currently there is no online repository hosting this project. To use this project it must be installed to Maven Local, a repository that is of course locally installed on your drive.

## To install this to Maven Local:

1. Preresequites \
OpenJDK 21 \
Android SDK

2. Run this command in the project directory \
gradle publishToMavenLocal

The project should now build and install to your OS's .m2 location: \
https://www.baeldung.com/maven-local-repository

## To use this in a Gradle Project:

1. Make sure this is added to your repositories \
mavenLocal()

2. Implement it \
implementation 'org.wolfsonrobotics.robotwebserver:robotwebserver:1.0'

3. Sync Gradle in Android Studio

