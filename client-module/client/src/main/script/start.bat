@echo off
if "%OS%" == "Windows_NT" setlocal

rem --------------------------------------------------------------------------
rem Start script for the Reaper Server
rem
rem $Id: start.bat,v 1.0 2016/11/17 ln$
rem ---------------------------------------------------------------------------

rem 设置java运行环境
rem Make sure prerequisite environment variables are set 
set NULS_JAVA_HOME=..\jre
if not "%NULS_JAVA_HOME%" == "" goto gotJavaHome
echo The NULS_JAVA_HOME environment variable is not defined
echo This environment variable is needed to run this program
goto end
:gotJavaHome
if not exist "%NULS_JAVA_HOME%\bin\java.exe" goto noJavaHome
goto okJavaHome
:noJavaHome 
if not "%JAVA_HOME%" == "" goto useSysJavaHome
echo The NULS_JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end 
:useSysJavaHome
set NULS_JAVA_HOME=%JAVA_HOME%
goto okJavaHome
:okJavaHome

rem 打印NULS_JAVA_HOME变量
rem echo NULS_JAVA_HOME=%NULS_JAVA_HOME%

rem 设置SERVER_HOME变量
rem Guess SERVER_HOME if not defined
if not "%SERVER_HOME%" == "" goto goReaperHome
set SERVER_HOME=.

if exist "%SERVER_HOME%\bin\start.bat" goto okReaperHome
set SERVER_HOME=..

if exist "%SERVER_HOME%\bin\start.bat" goto okReaperHome
set SERVER_HOME=%~dp0
cd %SERVER_HOME%
cd ..
set SERVER_HOME=%cd%

:goReaperHome
echo %SERVER_HOME%
if exist "%SERVER_HOME%\bin\start.bat" goto okReaperHome
echo The SERVER_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okReaperHome

rem 设置CLASSPATH
set CLASSPATH=%NULS_JAVA_HOME%\lib\tools.jar
set CLASSPATH=%CLASSPATH%;%SERVER_HOME%
set CLASSPATH=%CLASSPATH%;%SERVER_HOME%\conf;%SERVER_HOME%\libs\*

rem call classpath.bat
rem del classpath.bat

rem 打印CLASSPATH变量
rem echo CLASSPATH=%CLASSPATH%


set _EXECJAVA="%NULS_JAVA_HOME%\bin\javaw"
set _JAVA_OPTS=-Dfile.encoding=UTF-8 -Xms1024m -Xmx4096m  -XX:NewSize=256m -XX:MaxNewSize=256m -XX:PermSize=256M -XX:MaxPermSize=256M
set _MAINCLASS=io.nuls.client.Bootstrap

goto doExec

:doExec
echo NULS starting
%_EXECJAVA%  %_JAVA_OPTS%  -classpath  "%CLASSPATH%"  %_MAINCLASS%

:end
