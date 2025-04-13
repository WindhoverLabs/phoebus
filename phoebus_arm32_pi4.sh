#!/bin/sh
#
# Phoebus launcher for Linux or Mac OS X

# When deploying, change "TOP"
# to the absolute installation path
# TOP="."
THIS_SCRIPT="$(realpath "$0")";
TOP="${THIS_SCRIPT%/*}";
echo "Phoebus started at $(date)" >> /home/pi/phoebus-debug.log
# Ideally, assert that Java is found
# export JAVA_HOME=/opt/jdk-9
# export PATH="$JAVA_HOME/bin:$PATH"

if [ -d "${TOP}/target" ]
then
  TOP="$TOP/target"
fi

if [ -d "${TOP}/update" ]
then
  echo "Installing update..."
  cd ${TOP}
  rm -rf doc lib
  mv update/* .
  rmdir update
  echo "Updated."
fi


JAR=`echo ${TOP}/product-*.jar`
echo $JAR
# To get one instance, use server mode
OPT="-server 4918"
JDK_JAVA_OPTIONS=" -DCA_DISABLE_REPEATER=true"
JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS -Dnashorn.args=--no-deprecation-warning"
JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS -Djdk.gtk.verbose=false -Djdk.gtk.version=2 -Dprism.forceGPU=true"
JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS -Dlogback.configurationFile=/home/train/epics-tools/setup/settings/logback.xml"
JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS -Dorg.csstudio.javafx.rtplot.update_counter=false"
export JDK_JAVA_OPTIONS


filter1="-help"
filter2="-main"

firstarg=$1;

if test "${firstarg#*$filter1}" != "$firstarg"; then
  # Run MEDM converter etc. in foreground
  java -jar $JAR $OPT "$@"
elif test "${firstarg#*$filter2}" != "$firstarg"; then
    exec java -jar $JAR $OPT "$@"
else
  # Run UI as separate thread
  java -jar --module-path /usr/share/openjfx/lib --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.web,javafx.swing $JAR $OPT "$@"
fi
sleep 10
echo "Phoebus ended at $(date)" >> /home/pi/phoebus-debug.log
