$JDK = "C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot"
$GAME = "E:\Games\Starsector"
$MOD = "E:\Games\Starsector_api\mod\TripadExtension"

# Clean
if (Test-Path "$MOD\build\classes") { Remove-Item -Recurse -Force "$MOD\build\classes" }
New-Item -ItemType Directory -Force "$MOD\build\classes" | Out-Null

# Compile
Write-Host "=== Compiling ==="
$CP = "$GAME\starsector-core\starfarer.api.jar;$GAME\starsector-core\lwjgl.jar;$GAME\starsector-core\lwjgl_util.jar;$GAME\starsector-core\json.jar;$GAME\starsector-core\log4j-1.2.9.jar;$GAME\mods\LazyLib\jars\LazyLib.jar;$GAME\mods\NexusUI\jars\NexusUI.jar;$GAME\mods\LunaLib-2.0.5\jars\LunaLib.jar"
& "$JDK\bin\javac" -source 8 -target 8 -cp $CP -d "$MOD\build\classes" "$MOD\src\com\tripadextension\TripadButton.java" "$MOD\src\com\tripadextension\TripadButtonGroup.java" "$MOD\src\com\tripadextension\TripadButtonSpec.java" "$MOD\src\com\tripadextension\TripadClickHandler.java" "$MOD\src\com\tripadextension\TripadIconRenderer.java" "$MOD\src\com\tripadextension\TripadManager.java" "$MOD\src\com\tripadextension\TripadModPlugin.java" "$MOD\src\com\tripadextension\TripadOverlay.java" "$MOD\src\com\tripadextension\TripadRenderer.java" "$MOD\src\com\tripadextension\TripadSettings.java" 2>&1

if ($LASTEXITCODE -ne 0) { Write-Host "COMPILATION FAILED"; exit 1 }

# JAR
Write-Host "=== Creating JAR ==="
& "$JDK\bin\jar" cf "$MOD\jars\TripadExtension.jar" -C "$MOD\build\classes" . 2>&1

# Install
Write-Host "=== Installing ==="
if (-not (Test-Path "$GAME\mods\TripadExtension\jars")) { New-Item -ItemType Directory -Force "$GAME\mods\TripadExtension\jars" | Out-Null }
if (-not (Test-Path "$GAME\mods\TripadExtension\data\config")) { New-Item -ItemType Directory -Force "$GAME\mods\TripadExtension\data\config" | Out-Null }
Copy-Item -Force "$MOD\jars\TripadExtension.jar" "$GAME\mods\TripadExtension\jars\"
Copy-Item -Force "$MOD\mod_info.json" "$GAME\mods\TripadExtension\"
Copy-Item -Force "$MOD\data\config\LunaSettings.csv" "$GAME\mods\TripadExtension\data\config\"

Write-Host "=== DONE ==="
