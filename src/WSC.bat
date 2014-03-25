@echo off

if "%1" == "" (
    echo Usage: WSC.bat target_url
    exit /b
)

java -classpath "./conf;./lib/*" net.pupha.wsc.WSC %1
