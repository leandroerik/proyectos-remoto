@echo off

echo Compilando backend

cd ./backend
call mvn clean install -U
call mvn compile
call java -jar -Dapple.awt.UIElement="true" target/backend-1.0.0-jar-with-dependencies.jar -h

pause