@echo off
setlocal

if "%JAVA_HOME%"=="" set "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202"
if "%WILDFLY_HOME%"=="" set "WILDFLY_HOME=E:\wildfly-14.0.0.Final"

set "STANDALONE=%WILDFLY_HOME%\bin\standalone.bat"

if not exist "%STANDALONE%" (
  echo Nao encontrei o WildFly em "%STANDALONE%".
  exit /b 1
)

call "%STANDALONE%"
exit /b %errorlevel%
