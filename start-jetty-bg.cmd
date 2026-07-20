@echo off
setlocal
cd /d "%~dp0"
if exist jetty.out.log del /f /q jetty.out.log
if exist jetty.err.log del /f /q jetty.err.log
start "" /b cmd /c ""%~dp0start-jetty.cmd" 1>>"%~dp0jetty.out.log" 2>>"%~dp0jetty.err.log""
