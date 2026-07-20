@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MAVEN_OPTS=-Duser.home=C:\Users\va176\Documents\NEWPRO~1\PROVAS~1"
"C:\Program Files\NetBeans-12.0\netbeans\java\maven\bin\mvn.cmd" -Dmaven.repo.local=C:\Users\va176\Documents\NEWPRO~1\PROVAS~1\.m2\repository jetty:run
