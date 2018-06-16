@echo off

if "%OS%" == "Windows_NT" setlocal

ping /n 3 127.1>nul

set SERVER_HOME=%~dp0
cd %SERVER_HOME%
cd ..
set SERVER_HOME=%cd%

rd /s /Q %SERVER_HOME%\libs
rd /s /Q %SERVER_HOME%\conf
md %SERVER_HOME%\libs
md %SERVER_HOME%\conf

echo d|xcopy %SERVER_HOME%\temp\%1%\conf\*.* .\conf\ /s/y
echo d|xcopy %SERVER_HOME%\temp\%1%\libs\*.* .\libs\ /s/y

cd bin
 
goto doExec

:doExec
call start.bat

:end