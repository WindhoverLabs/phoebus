#!/bin/sh
#mvn -Djavafx.platform=linux clean verify

VERSION="4.6.6"

cd phoebus-product/target
unzip product-$VERSION-SNAPSHOT.zip
mkdir deploy
cp -r product-$VERSION-SNAPSHOT/ deploy/
cd deploy

#At the moment jpackage does not work properly. One day hopefully it will so that we can embed the JVM inside of the product. One day...
#jpackage --name phoebus --input product-$(VERSION)-SNAPSHOT --type app-image --main-jar product-$(VERSION)-SNAPSHOT.jar --java-options --java-options --runtime-image /usr/lib/jvm/java-11-openjdk-amd64/

#Build AppImage for linux

mkdir Commander.AppDir/
mkdir -p Commander.AppDir/usr/bin
mkdir -p Commander.AppDir/usr/lib

# Copy files..
cp -r product-$VERSION-SNAPSHOT/* Commander.AppDir/
cp -r product-$VERSION-SNAPSHOT/phoebus.sh Commander.AppDir/AppRun

#mkdir Commander.AppDir/AppRun
#mkdir Commander.AppDir/Commander.desktop
#mkdir Commander.AppDir/Commander.png

