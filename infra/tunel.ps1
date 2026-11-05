# Tunel SSH: localhost:3307 de tu PC -> 127.0.0.1:3306 de la instancia (MySQL).
# Deja esta ventana abierta mientras uses DBeaver o la API en local. Ctrl+C para cerrarlo.
$Clave = "C:\Users\aniri\Documents\aAWS_claves\ad-api-keys.pem"
$Ip    = "52.45.53.241"
Write-Host "Tunel abierto: 127.0.0.1:3307 -> MySQL de la instancia. No cierres esta ventana."
ssh -i $Clave -N -L 3307:127.0.0.1:3306 ubuntu@$Ip
