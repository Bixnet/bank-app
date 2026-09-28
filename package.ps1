$ErrorActionPreference = "Stop"
Remove-Item -Recurse -Force build, dist -ErrorAction SilentlyContinue
New-Item -ItemType Directory build\classes, build\input | Out-Null

# compile the app (tests and junit are not needed here)
$sources = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName
javac -cp "libs/sqlite-jdbc-3.45.1.0.jar;libs/flatlaf-3.4.jar" -d build\classes $sources

# runnable jar that finds its libraries next to it
"Main-Class: bank.Main`nClass-Path: sqlite-jdbc-3.45.1.0.jar flatlaf-3.4.jar`n" | Out-File build\manifest.txt -Encoding ascii
jar --create --file build\input\bank-app.jar --manifest build\manifest.txt -C build\classes .
Copy-Item libs\sqlite-jdbc-3.45.1.0.jar, libs\flatlaf-3.4.jar build\input\

# native app image
jpackage --type app-image --name BankApp --input build\input --main-jar bank-app.jar --main-class bank.Main --dest dist