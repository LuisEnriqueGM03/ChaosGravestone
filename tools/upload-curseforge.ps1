# Sube los jars del mod a CurseForge (uno por cargador).
# Token: variable de entorno CURSEFORGE_TOKEN (de la sesión o guardada con `setx`).
# Uso:  powershell -File tools/upload-curseforge.ps1 [-Loader fabric|neoforge|forge|all] [-DryRun]
# Antes: ./gradlew build  y  ./gradlew -p forge build
param(
	[string]$ProjectId = "1728749",
	[string]$ReleaseType = "release",
	[ValidateSet("fabric", "neoforge", "forge", "all")]
	[string]$Loader = "all",
	[switch]$DryRun
)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent

$token = $env:CURSEFORGE_TOKEN
if (-not $token) { $token = [Environment]::GetEnvironmentVariable("CURSEFORGE_TOKEN", "User") }
if (-not $token) { throw "Falta CURSEFORGE_TOKEN (setx CURSEFORGE_TOKEN <token>)" }

$version = ((Get-Content "$root\gradle.properties") | Where-Object { $_ -match '^version=' }) -replace '^version=', ''
$mcVersion = ((Get-Content "$rootgradle.properties") | Where-Object { $_ -match '^minecraft_version=' }) -replace '^minecraft_version=', ''
$api = "https://minecraft.curseforge.com/api"
$all = Invoke-RestMethod -Uri "$api/game/versions" -Headers @{ "X-Api-Token" = $token }

# Por cargador: nombre de la versión en CurseForge y dependencias obligatorias.
$loaders = @{
	fabric   = @{ GameVersion = "Fabric"; Relations = @(@{ slug = "fabric-api"; type = "requiredDependency" }) }
	neoforge = @{ GameVersion = "NeoForge"; Relations = @() }
	forge    = @{ GameVersion = "Forge"; Relations = @() }
}
$selected = if ($Loader -eq "all") { "fabric", "neoforge", "forge" } else { , $Loader }

$failed = $false
foreach ($name in $selected) {
	$cfg = $loaders[$name]
	$jar = Get-Item "$root\$name\build\libs\chaosgravestone-$name-$mcVersion-$version.jar" -ErrorAction SilentlyContinue
	if (-not $jar) { throw "No existe $name/build/libs/chaosgravestone-$name-$mcVersion-$version.jar; ejecuta gradlew build" }

	# Ids de CurseForge para versión de juego, cargador, Java y entorno.
	$ids = foreach ($wanted in $mcVersion, $cfg.GameVersion, "Java 21", "Client", "Server") {
		$match = $all | Where-Object { $_.name -eq $wanted } | Select-Object -First 1
		if (-not $match) { throw "CurseForge no tiene la versión '$wanted'" }
		Write-Host "  $wanted -> $($match.id)"
		$match.id
	}

	$meta = @{
		# ReadAllText: en PowerShell 5.1, Get-Content -Raw añade propiedades que ConvertTo-Json serializa.
		changelog     = [IO.File]::ReadAllText("$root\tools\changelog.md", [Text.Encoding]::UTF8)
		changelogType = "markdown"
		displayName   = "Chaos Gravestone - $version ($mcVersion)"
		gameVersions  = @($ids)
		releaseType   = $ReleaseType
	}
	# CurseForge rechaza relations.projects vacío: solo se envía si hay dependencias.
	if ($cfg.Relations.Count -gt 0) { $meta.relations = @{ projects = @($cfg.Relations) } }
	$metadata = $meta | ConvertTo-Json -Depth 5
	$metaFile = Join-Path $env:TEMP "chaosgravestone-cf-metadata-$name.json"
	[IO.File]::WriteAllText($metaFile, $metadata, (New-Object Text.UTF8Encoding $false))

	Write-Host "Proyecto $ProjectId | $($jar.Name) | Chaos Gravestone - $version | $ReleaseType"
	if ($DryRun) {
		Write-Host "DryRun: no se sube nada."
		Remove-Item $metaFile
		continue
	}

	$response = (& curl.exe -s -w "`nHTTP %{http_code}" -H "X-Api-Token: $token" `
		-F "metadata=<$metaFile" -F "file=@$($jar.FullName)" "$api/projects/$ProjectId/upload-file") -join "`n"
	Remove-Item $metaFile
	Write-Host $response
	if ($response -notmatch "HTTP 200") { $failed = $true }
}
if ($failed) { exit 1 }
