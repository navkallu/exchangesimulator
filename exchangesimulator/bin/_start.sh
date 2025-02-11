jp=../lib/
#cp=../../classes

#
# setup the classpath
#

cp=${cp}:${jp}/exchangefix-simulator-1.0-SNAPSHOT.jar
cp=${cp}:${jp}/quickfixj-all-1.6.0.jar
cp=${cp}:${jp}/quickfixj-core-1.6.0.jar
cp=${cp}:${jp}/slf4j-api-1.7.22.jar
cp=${cp}:${jp}/slf4j-jdk14-1.7.22.jar
cp=${cp}:${jp}/mina-core-2.0.16.jar
cp=${cp}:${jp}/jackson-core-2.12.3.jar
cp=${cp}:${jp}/jackson-databind-2.12.3.jar
cp=${cp}:${jp}/jackson-annotations-2.12.3.jar


java -version
echo on

#
# echo out the command line
#

echo "java -mx256M -classpath "$cp"  $*"

#
# finally execute the JVM
#

java -mx256M -classpath "$cp"  $* >Simulator.log


