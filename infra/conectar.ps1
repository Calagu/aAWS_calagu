# Abre una sesion SSH en la instancia ad-calagu (IP publica actual: 52.45.53.241)
# Si reinicias el laboratorio la IP cambia: editala aqui y en el grupo de seguridad (Mi IP).
$Clave = "C:\Users\aniri\Documents\aAWS_claves\ad-api-keys.pem"
$Ip    = "52.45.53.241"
icacls $Clave /inheritance:r | Out-Null
icacls $Clave /grant:r "$($env:USERNAME):(R)" | Out-Null
ssh -i $Clave ubuntu@$Ip
