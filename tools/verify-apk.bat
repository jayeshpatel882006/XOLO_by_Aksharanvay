@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "APKSIGNER=C:\Users\Asus\AppData\Local\Android\Sdk\build-tools\34.0.0\apksigner.bat"
call "%APKSIGNER%" verify --verbose --print-certs "%~dp0..\app\build\outputs\apk\release\app-release.apk"
