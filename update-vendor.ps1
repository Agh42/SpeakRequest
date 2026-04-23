#!/usr/bin/env pwsh
# update-vendor.ps1
#
# Downloads (or refreshes) locally-mirrored vendor assets so the application
# can run on a LAN with no internet access.
#
# Assets downloaded:
#   vendor/tailwind.cdn.js      – Tailwind CSS CDN runtime (used by chair.html)
#   vendor/google-fonts.css     – Google Fonts stylesheet (all URL rewritten to local)
#   vendor/fonts/*.woff2        – Manrope, Inter, Material Symbols Outlined font files
#
# Usage:  .\update-vendor.ps1
# Run this whenever you have internet and want to refresh the cached assets.

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot  = $PSScriptRoot
$staticDir = Join-Path $repoRoot "src\main\resources\static"
$vendorDir = Join-Path $staticDir "vendor"
$fontDir   = Join-Path $vendorDir "fonts"

New-Item -ItemType Directory -Force -Path $fontDir | Out-Null

# ── 1. Tailwind CDN runtime ────────────────────────────────────────────────
Write-Host "Downloading Tailwind CDN runtime..."
Invoke-WebRequest -Uri "https://cdn.tailwindcss.com" `
    -OutFile (Join-Path $vendorDir "tailwind.cdn.js") `
    -UseBasicParsing
$twKb = [Math]::Round((Get-Item (Join-Path $vendorDir "tailwind.cdn.js")).Length / 1KB, 0)
Write-Host "  OK  tailwind.cdn.js  ($twKb KB)"

# ── 2. Google Fonts CSS ────────────────────────────────────────────────────
# Single stylesheet covers all pages:
#   chair.html       – Manrope, Inter, Material Symbols Outlined
#   participant.html – Manrope, Inter
Write-Host "Downloading Google Fonts CSS..."
$fontCssUrl = "https://fonts.googleapis.com/css2?" +
    "family=Manrope:wght@500;700;800" +
    "&family=Inter:wght@400;500;600" +
    "&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0" +
    "&display=swap"
$ua  = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
$css = (Invoke-WebRequest -Uri $fontCssUrl -Headers @{ "User-Agent" = $ua } -UseBasicParsing).Content

# ── 3. Download font files and rewrite URLs ────────────────────────────────
Write-Host "Downloading font files..."
Remove-Item (Join-Path $fontDir "*") -Force -ErrorAction SilentlyContinue

$fontUrls = [regex]::Matches($css, "https://fonts\.gstatic\.com/[^)'\s]+") |
    ForEach-Object { $_.Value } | Select-Object -Unique

$seen = @{}
foreach ($u in $fontUrls) {
    $baseName = [System.IO.Path]::GetFileName(([Uri]$u).AbsolutePath)
    if ($seen.ContainsKey($baseName)) {
        $ext      = [System.IO.Path]::GetExtension($baseName)
        $stem     = [System.IO.Path]::GetFileNameWithoutExtension($baseName)
        $baseName = "${stem}_$($seen[$baseName])${ext}"
    }
    $seen[$baseName] = ($seen[$baseName] -as [int]) + 1

    $dest = Join-Path $fontDir $baseName
    Invoke-WebRequest -Uri $u -OutFile $dest -UseBasicParsing
    $css = $css.Replace($u, "/vendor/fonts/$baseName")
}

# ── 4. Write rewritten CSS ─────────────────────────────────────────────────
$cssPath = Join-Path $vendorDir "google-fonts.css"
Set-Content -Path $cssPath -Value $css -Encoding UTF8

# Sanity check: no external gstatic URLs should remain
$remaining = Select-String -Path $cssPath -Pattern "fonts\.gstatic\.com" -Quiet
if ($remaining) {
    Write-Warning "Some gstatic URLs were not rewritten – check $cssPath"
} else {
    Write-Host "  OK  all font URLs rewritten to local paths"
}

# ── 5. Summary ─────────────────────────────────────────────────────────────
$fontFiles = Get-ChildItem $fontDir
$fontTotal = [Math]::Round(($fontFiles | Measure-Object -Property Length -Sum).Sum / 1KB, 0)
$cssKb     = [Math]::Round((Get-Item $cssPath).Length / 1KB, 1)
Write-Host ""
Write-Host "Vendor assets updated:"
Write-Host "  vendor/tailwind.cdn.js    $twKb KB"
Write-Host "  vendor/google-fonts.css   $cssKb KB"
Write-Host "  vendor/fonts/             $($fontFiles.Count) files, $fontTotal KB total"
