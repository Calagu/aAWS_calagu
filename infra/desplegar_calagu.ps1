# En el PUESTO (PowerShell), desde la carpeta aAWS_calagu: compila, copia y reinicia la API en la instancia.
# Uso:  .\infra\desplegar_calagu.ps1            (primer despliegue: despues ejecuta crear_servicio.sh en la instancia)
#       .\infra\desplegar_calagu.ps1 -Reiniciar (despliegues siguientes)
param([switch]$Reiniciar)
$Clave = "C:\Users\aniri\Documents\aAWS_claves\ad-api-keys.pem"
$Ip    = "52.45.53.241"
$Destino = "ubuntu@$Ip"

Push-Location "$PSScriptRoot\..\biblioteca-api"
.\mvnw.cmd clean package -DskipTests          # genera target\biblioteca.jar
if ($LASTEXITCODE -ne 0) { Pop-Location; throw "Fallo la compilacion" }
scp -i $Clave target\biblioteca.jar "${Destino}:/tmp/"
Pop-Location

if ($Reiniciar) {
    ssh -i $Clave $Destino "sudo install -o biblioteca -g biblioteca -m 644 /tmp/biblioteca.jar /opt/biblioteca/ && sudo systemctl restart biblioteca && sleep 15 && curl -i http://127.0.0.1:8080/api/estado"
} else {
    scp -i $Clave -r "$PSScriptRoot" "${Destino}:~/"
    Write-Host "Ahora entra en la instancia (.\infra\conectar.ps1) y ejecuta: bash ~/infra/crear_servicio.sh"
}
