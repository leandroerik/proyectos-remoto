copy /Y ..\backend\pom.xml .
call docker build --no-cache . -t bhbuild/bacuni
call docker push bhbuild/bacuni

pause