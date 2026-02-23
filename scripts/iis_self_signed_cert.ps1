# Create the self-signed certificate in Personal (My)
$cert = New-SelfSignedCertificate `
    -DnsName "bdms-test.nicholls.edu" `
    -CertStoreLocation "Cert:\LocalMachine\My" `
    -FriendlyName "AppEnhancer SSO Certificate" `
    -KeyAlgorithm RSA `
    -KeyLength 2048 `
    -HashAlgorithm SHA1 `
    -Provider "Microsoft RSA SChannel Cryptographic Provider" `
    -KeyExportPolicy Exportable `
    -NotAfter (Get-Date).AddYears(10)

# Export the certificate (public key only)
$cerPath = "$env:TEMP\AppEnhancer-SSO.cer"
Export-Certificate -Cert $cert -FilePath $cerPath

# Import into Trusted Root Certification Authorities
Import-Certificate `
    -FilePath $cerPath `
    -CertStoreLocation "Cert:\LocalMachine\Root"

# Optional cleanup
Remove-Item $cerPath