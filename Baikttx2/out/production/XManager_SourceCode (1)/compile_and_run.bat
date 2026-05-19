@echo off
echo Dang bien dich...
javac *.java
if %errorlevel% neq 0 (
    echo Bien dich that bai!
    pause
    exit /b 1
)
echo Bien dich thanh cong! Dang chay...
java MainApp
pause
