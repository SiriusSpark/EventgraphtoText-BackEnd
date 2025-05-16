$headers = @{
    "Authorization" = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjIsInVzZXJuYW1lIjoidGVzdHVzZXIzIiwiaWF0IjoxNzQ2MjMzNDc3LCJleHAiOjE3NDYzMTk4Nzd9.ce89_1hIF85XH2jVCgJCD9jhpsVuKrZS02yM51tXnbo"
    "Content-Type" = "application/json"
}

$body = @{
    "type" = "CAUSES"
    "strength" = "低"
} | ConvertTo-Json

$uri = "http://localhost:8080/api/graph/edges/1"
$method = "PUT"

try {
    $response = Invoke-RestMethod -Uri $uri -Method $method -Headers $headers -Body $body 
    $response | ConvertTo-Json -Depth 5
} catch {
    Write-Host "Error: $_"
} 