@echo off

echo Compilando frontend para levantar junto con el backend

cd %~dp0/../homebanking-frontend
call npm install -g pnpm
call pnpm install
call pnpm run build

xcopy /E /I /H /K /Y .\dist\homebanking-angular ..\backend-unificado\backend\src\main\resources\web\hb

pause
