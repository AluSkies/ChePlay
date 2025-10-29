$proc = Start-Process -FilePath "./mvnw.cmd" -ArgumentList "spring-boot:run" -PassThru -WindowStyle Hidden
try {
    Start-Sleep -Seconds 18
    $resp = Invoke-RestMethod "http://localhost:8080/api/recommendations/debug?user=u1&limit=25"
    $resp | ConvertTo-Json -Depth 6 | Out-File -FilePath "friends_debug_u1.json" -Encoding utf8
} finally {
    if ($proc -and -not $proc.HasExited) {
        try {
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        } catch {
        }
    }
}
