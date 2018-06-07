@ECHO OFF 

if "%OS%" == "Windows_NT" setlocal
pushd "%cd%"
cd ..
set SERVER_HOME=%cd%
cd bin
set CLASSPATH=%JAVA_HOME%\lib\tools.jar
set CLASSPATH=%CLASSPATH%;%SERVER_HOME%
set CLASSPATH=%CLASSPATH%;%SERVER_HOME%\conf;%SERVER_HOME%\libs\*
 
set _EXECJAVA="%JAVA_HOME%\bin\javaw"
set _JAVA_OPTS=-Xms1024m -Xmx4096m  -XX:NewSize=256m -XX:MaxNewSize=256m -XX:PermSize=256M -XX:MaxPermSize=256M
set _MAINCLASS=io.nuls.client.Bootstrap

goto doExec

:doExec
%_EXECJAVA% %_JAVA_OPTS% -classpath "%CLASSPATH%" %_MAINCLASS% 

:end