$lines = Get-Content .\fintech-core\.env
$tokenLine = $lines | Where-Object { $_ -match '^TELEGRAM_BOT_TOKEN=' }
if (-not $tokenLine) { Write-Error 'TELEGRAM_BOT_TOKEN not found in .env'; exit 2 }
$token = $tokenLine -replace '^TELEGRAM_BOT_TOKEN=',''
$publicLine = $lines | Where-Object { $_ -match '^PUBLIC_URL=' }
if (-not $publicLine) { Write-Error 'PUBLIC_URL not found in .env'; exit 2 }
$public = $publicLine -replace '^PUBLIC_URL=',''
Write-Host "Setting webhook to $public/api/v1/telegram/webhook (token hidden)"
try {
    $set = Invoke-RestMethod -Method Get -Uri ("https://api.telegram.org/bot$token/setWebhook?url=$public/api/v1/telegram/webhook") -ErrorAction Stop
    $set | ConvertTo-Json -Depth 5
} catch {
    Write-Error $_.Exception.Message; exit 3
}
try {
    $info = Invoke-RestMethod -Method Get -Uri ("https://api.telegram.org/bot$token/getWebhookInfo") -ErrorAction Stop
    $info | ConvertTo-Json -Depth 5
} catch {
    Write-Error $_.Exception.Message; exit 4
}

