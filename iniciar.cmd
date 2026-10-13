@echo off
setlocal
cd /d "%~dp0"
if not defined JAVA_HOME if exist "C:\Program Files\Java\jdk-21\bin\javac.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-21"
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
where javac >nul 2>&1
if errorlevel 1 (
    echo Instala JDK 21 y configura JAVA_HOME antes de continuar.
    pause
    exit /b 1
)
call mvnw.cmd spring-boot:run
if errorlevel 1 pause
