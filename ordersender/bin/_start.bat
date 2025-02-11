@echo off
rem This batch program starts a java application and passes in the commandline arguements
rem set PATH=c:\path\to\jvm\bin;

set JP=..\lib

set cp=%cp%;%JP%\exchangefix-simulator-1.0-SNAPSHOT.jar
set cp=%cp%;%JP%\quickfixj-all-1.6.0.jar
set cp=%cp%;%JP%\quickfixj-core-1.6.0.jar
set cp=%cp%;%JP%\slf4j-api-1.7.22.jar
set cp=%cp%;%JP%\slf4j-jdk14-1.7.22.jar
set cp=%cp%;%JP%\mina-core-2.0.16.jar
set cp=%cp%;%JP%\jackson-core-2.12.3.jar
set cp=%cp%;%JP%\jackson-databind-2.12.3.jar
set cp=%cp%;%JP%\jackson-annotations-2.12.3.jar


"C:\Program Files\Java\jdk-17"\bin\java -version
@echo on

"C:\Program Files\Java\jdk-17"\bin\java -Xms64M -Xmx256M -classpath "%cp%"  %*
pause
exit
