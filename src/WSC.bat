@echo off

REM if "%1" == "" (
REM     echo Usage: WSC.bat target_url
REM     exit /b
REM )

java -classpath "./conf;./lib/*" net.pupha.wsc.WSC %1
