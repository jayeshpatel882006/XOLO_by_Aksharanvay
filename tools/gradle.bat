@echo off
REM Local build helper for XOLO.
REM Pins Gradle to Android Studio's bundled JDK 21 so the build does not depend
REM on whatever java.exe happens to be first on PATH.
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0.."
call gradlew.bat %*
