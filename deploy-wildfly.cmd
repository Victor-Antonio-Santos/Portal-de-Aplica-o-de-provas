@echo off
setlocal

if "%JAVA_HOME%"=="" set "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202"
if "%WILDFLY_HOME%"=="" set "WILDFLY_HOME=E:\wildfly-14.0.0.Final"

set "CLI=%WILDFLY_HOME%\bin\jboss-cli.bat"
set "WAR=%~dp0target\provas-online.war"

if not exist "%CLI%" (
  echo Nao encontrei o jboss-cli em "%CLI%".
  exit /b 1
)

if not exist "%WAR%" (
  echo Nao encontrei o arquivo "%WAR%".
  echo Rode "mvn clean package" antes do deploy.
  exit /b 1
)

call "%CLI%" --connect --command="deploy \"%WAR%\" --force"
exit /b %errorlevel%
