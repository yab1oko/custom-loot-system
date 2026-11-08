# Splits CanStash prop models into body + lid for BER animation.
$ErrorActionPreference = "Stop"
$blockDir = Join-Path $PSScriptRoot "..\src\main\resources\assets\cts\models\block"
$bsDir = Join-Path $PSScriptRoot "..\src\main\resources\assets\cts\blockstates"
$blockDir = (Resolve-Path $blockDir).Path
$bsDir = (Resolve-Path $bsDir).Path

# name = lid element indices (closed model)
$props = @{
  toolbox       = @(0,1,2,3,4,5)
  medkit        = @(4,5,6)
  ammo_box      = @(7,8,9)
  toilet        = @(0)
  safe          = @(7,8)
  microwave     = @(0,1,2,3,4)
  fridge        = @(0,1,2)
  washing_machine = @(1,2,3,4,5)
  medical_crate = @(1,2,3,4)
  crate         = @(0,1,2,3,4)
  waste_drum    = @(1,2,3,4,5,6,7)
}

function Get-ParticleTex($textures) {
  if ($null -eq $textures) { return "minecraft:block/oak_planks" }
  if ($textures.particle) { return [string]$textures.particle }
  $props = $textures.PSObject.Properties | Where-Object { $_.Name -ne 'particle' } | Select-Object -First 1
  if ($props) { return [string]$props.Value }
  return "minecraft:block/oak_planks"
}

function Write-PartModel($src, $indices, $outPath, $isLid) {
  $j = Get-Content $src -Raw -Encoding UTF8 | ConvertFrom-Json
  $all = @($j.elements)
  $picked = New-Object System.Collections.Generic.List[object]
  for ($i = 0; $i -lt $all.Count; $i++) {
    $take = $indices -contains $i
    if ($isLid) {
      if ($take) { $picked.Add($all[$i]) }
    } else {
      if (-not $take) { $picked.Add($all[$i]) }
    }
  }
  # Rebuild as hashtable for ConvertTo-Json
  $rt = "minecraft:cutout"
  if ($null -ne $j.render_type) { $rt = [string]$j.render_type }
  $obj = New-Object psobject -Property @{
    format_version = [string]$j.format_version
    ambientocclusion = $false
    credit = [string]$j.credit
    texture_size = $j.texture_size
    render_type = $rt
    textures = $j.textures
    elements = @($picked.ToArray())
  }
  $json = $obj | ConvertTo-Json -Depth 40
  [System.IO.File]::WriteAllText($outPath, $json)
}

foreach ($name in $props.Keys) {
  $src = Join-Path $blockDir "$name.json"
  if (-not (Test-Path $src)) { Write-Error "Missing $src"; continue }
  $lidIdx = $props[$name]
  Write-PartModel $src $lidIdx (Join-Path $blockDir "${name}_body.json") $false
  Write-PartModel $src $lidIdx (Join-Path $blockDir "${name}_lid.json") $true

  $closed = Get-Content $src -Raw -Encoding UTF8 | ConvertFrom-Json
  $particle = Get-ParticleTex $closed.textures
  $particleModel = Join-Path $blockDir "${name}_particle.json"
  $pjson = "{`n  `"textures`": {`n    `"particle`": `"$particle`"`n  }`n}`n"
  [System.IO.File]::WriteAllText($particleModel, $pjson)

  $bs = Join-Path $bsDir "$name.json"
  $bsContent = @"
{
  "variants": {
    "facing=north,looted=false": { "model": "cts:block/${name}_particle" },
    "facing=east,looted=false": { "model": "cts:block/${name}_particle" },
    "facing=south,looted=false": { "model": "cts:block/${name}_particle" },
    "facing=west,looted=false": { "model": "cts:block/${name}_particle" },
    "facing=north,looted=true": { "model": "cts:block/${name}_particle" },
    "facing=east,looted=true": { "model": "cts:block/${name}_particle" },
    "facing=south,looted=true": { "model": "cts:block/${name}_particle" },
    "facing=west,looted=true": { "model": "cts:block/${name}_particle" }
  }
}
"@
  [System.IO.File]::WriteAllText($bs, $bsContent)
  Write-Host "split $name (lid idxs: $($lidIdx -join ','))"
}
Write-Host "done"
