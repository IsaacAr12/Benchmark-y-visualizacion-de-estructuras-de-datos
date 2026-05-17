@echo off
setlocal enabledelayedexpansion

set MAIN_CLASS=ui.BenchmarkApp
set SRC_DIR=src
set OUT_DIR=out
set JAVAFX_LIB=lib\javafx-sdk-21\lib
set GSON_JAR=lib\gson-2.10.1.jar

:: Limpiar y crear out
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"

:: Compilar TODOS los .java en una sola llamada
echo Compilando todos los archivos...
dir /s /b "%SRC_DIR%\*.java" > sources.txt
javac --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "%GSON_JAR%" -sourcepath "%SRC_DIR%" -d "%OUT_DIR%" @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: La compilacion fallo.
    pause
    exit /b 1
)

echo Compilacion exitosa.
echo.

:: Ejecutar
echo Ejecutando %MAIN_CLASS%...
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "%OUT_DIR%;%GSON_JAR%" %MAIN_CLASS%

endlocal
pause