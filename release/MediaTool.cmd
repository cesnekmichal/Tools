@echo off
CHCP 65001
CD /D "%~dp0"

echo Spoustim kompilaci EXE...
"c:\Program Files (x86)\Launch4j\launch4jc.exe" MediaTool.xml

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo Kompilace selhala s chybou %ERRORLEVEL%.
    echo.
    pause
) ELSE (
    echo.
    echo Kompilace probehla uspesne.
    echo.
)

EXIT /B 0
