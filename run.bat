@echo off  
echo Compiling EthioTour Connect...
if not exist build\classes mkdir build\classes

REM Download dependencies if lib folder doesn't exist
if not exist lib mkdir lib
REM lib is the directory where the dependencies are stored
if not exist lib\postgresql-42.7.3.jar (
    echo Downloading PostgreSQL JDBC...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar' -OutFile 'lib\postgresql-42.7.3.jar'"
)
if not exist lib\HikariCP-5.1.0.jar (
    echo Downloading HikariCP...
    @REM HikariCP is a database connection pool
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar' -OutFile 'lib\HikariCP-5.1.0.jar'"
)
if not exist lib\SLF4J-api-2.0.7.jar (
    echo Downloading SLF4J API...
    @REM SLF4J API is a logging framework to log messages to the console
    @REM there would not be an implementation of the logging framework, but there would be a simple one that logs messages to the console
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.7/slf4j-api-2.0.7.jar' -OutFile 'lib\SLF4J-api-2.0.7.jar'"
)
if not exist lib\flatlaf-3.4.1.jar (
    echo Downloading FlatLaf...
    @REM FlatLaf is a modern LaC (Look and Feel) for Swing applications
    @REM it is used to make the application look modern and attractive
    @REM it is also used to make the application look more professional
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/formdev/flatlaf/3.4.1/flatlaf-3.4.1.jar' -OutFile 'lib\flatlaf-3.4.1.jar'"
)
if not exist lib\flatlaf-extras-3.4.1.jar (
    echo Downloading FlatLaf Extras...
    @REM FlatLaf Extras is a set of additional components for FlatLaf
    @REM it is used to make the application look more modern and attractive
    @REM it is also used to make the application look more professional
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/formdev/flatlaf-extras/3.4.1/flatlaf-extras-3.4.1.jar' -OutFile 'lib\flatlaf-extras-3.4.1.jar'"
)
if not exist lib\sqlite-jdbc-3.45.1.0.jar (
    echo Downloading SQLite JDBC...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar' -OutFile 'lib\sqlite-jdbc-3.45.1.0.jar'"
)
if not exist lib\slf4j-simple-2.0.7.jar (
    echo Downloading SLF4J Simple...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.7/slf4j-simple-2.0.7.jar' -OutFile 'lib\slf4j-simple-2.0.7.jar'"
)

echo Compiling with classpath: .;lib\*
javac -d build\classes -cp ".;lib\*" src\main\java\com\ethiotour\model\*.java ^
           src\main\java\com\ethiotour\util\*.java ^
           src\main\java\com\ethiotour\config\*.java ^
           src\main\java\com\ethiotour\service\*.java ^
           src\main\java\com\ethiotour\controller\*.java ^
           src\main\java\com\ethiotour\view\*.java ^
           src\main\java\com\ethiotour\EthioTourApp.java ^
           src\main\java\com\ethiotour\DemoApp.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compilation successful!
    echo.
    echo Copying configuration files...
    if not exist build\classes\resources mkdir build\classes\resources
    copy /Y src\main\resources\database.properties build\classes\resources\database.properties >nul
    copy /Y src\main\resources\seed_data.sql build\classes\resources\seed_data.sql >nul
    copy /Y src\main\resources\postgresql_seed_data.sql build\classes\postgresql_seed_data.sql >nul
    if not exist build\classes\images mkdir build\classes\images
    copy /Y src\main\resources\images\*.png build\classes\images\ >nul
    
    echo Starting EthioTour Connect...
    echo.
    java --enable-native-access=ALL-UNNAMED -cp "build\classes;build\classes\resources;lib\*" com.ethiotour.EthioTourApp
) else (
    echo Compilation failed. Please check the errors above.
)
pause
@REM "pause" is used to keep the console window open after the application closes
