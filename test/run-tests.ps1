# Compila src + test y corre el harness de robustez desde un directorio temporal.
# Uso:  powershell -ExecutionPolicy Bypass -File test\run-tests.ps1
$ErrorActionPreference = 'Stop'

$here = Split-Path -Parent $PSScriptRoot
$out  = Join-Path $here 'bin-test'
$javac = if ($env:JAVAC) { $env:JAVAC } else { 'javac' }
$java  = if ($env:JAVA)  { $env:JAVA }  else { 'java' }

if (Test-Path $out) { Remove-Item -Recurse -Force $out }
New-Item -ItemType Directory -Path $out | Out-Null

Write-Host '>> compilando...'
$sources = Get-ChildItem -Recurse -Path (Join-Path $here 'src'), (Join-Path $here 'test') -Filter *.java | ForEach-Object { $_.FullName }
$sources | Set-Content -Encoding utf8 (Join-Path $out 'sources.txt')
& $javac -encoding UTF-8 -d $out ('@' + (Join-Path $out 'sources.txt'))
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$rundir = Join-Path ([System.IO.Path]::GetTempPath()) ('aa-tests-' + [System.Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $rundir | Out-Null
Write-Host ">> corriendo tests en $rundir"
Push-Location $rundir
try {
    & $java -Dfile.encoding=UTF-8 -cp $out tests.TestMain
    $code = $LASTEXITCODE
} finally {
    Pop-Location
    Remove-Item -Recurse -Force $rundir
}
exit $code
