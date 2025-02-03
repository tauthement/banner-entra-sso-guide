### Configuring AppXtender

1. When creating the Entra application, add the following URL as the “Reply URL (Assertion Consumer Service URL)”. Modify as needed for your server.
	* Ex. `https://appxtender.univ.edu/AppXtender/BdmSamlSso`
2. Install the BDM SSO Files.
	* This is already well covered by Ellucian in their documentation. Please visit the [this link](https://resources.elluciancloud.com/bundle/banner_bdm_acn_install/page/t_install_bdm_sso_files.html) for more information.
	* Follow the Ellucian documentation to edit the “Login.cshtml” and “Index.cshtml” file. 
3. Copy the PFX keystore to the AppXtender install location.
4. Locate the `bdm.sso.config` file within the AppXtender install location. Edit the following tags:
	* `SsoMode`
		* Set to `SAML2`
		* Ex. `<SsoMode>SAML2</SsoMode>`
	* `IdpSsoUrl`
		* Set to the `Login URL` found in Box 4 of the Entra SAML configuration page. You can also use the URL in the example, but make sure to replace the tenant ID with your own.
		* Ex. `<IdpSsoUrl>https://login.microsoftonline.com/*tenant-id*/saml2</IdpSsoUrl>`
	* `ServiceProviderId`
		* Set to the entity ID of the Entra application. This can be found in Box 1 of the Entra SAML configuration page.
		* Ex. `<ServiceProviderId>banner-bdm-test-sp</ServiceProviderId>`
	* `EncryptedAssertions`
		* Set to false
		* Ex.`<EncryptedAssertions>false</EncryptedAssertions>`
	* `IdpPublicKey`
		* Download the `Certificate (Raw)` file from Box 3 in the Entra SAML configuration page. 
			* Place this file in the AppXtender install directory.
		* Set the value to the filename of the certificate.
		* Ex. `<IdpPublicKey>entra-idp.cer</IdpPublicKey>`
	* `IdpPublicKeyPassword`
		* Leave this empty as the downloaded certificate file from Entra has no password.
		* Ex. `<IdpPublicKeyPassword></IdpPublicKeyPassword>`
	* `SpPrivateKey`
		* Set to the PFX filename that was copied earlier to the AppXtender install directory.
		* Ex. `<SpPrivateKey>samlkeystore.pfx</SpPrivateKey>`
	* `SpPrivateKeyPassword`
		* Enter the password of the PFX file.
		* Ex. `<SpPrivateKeyPassword>keystorepassword</SpPrivateKeyPassword>`
	* `ArtifactServiceUrl`
		* Enter the URL for AppXtender
		* Ex. `<ArtifactServiceUrl>https://server.univ.edu/AppXtender</ArtifactServiceUrl>`
	* `UDCIDAttributeName`
		* Set to the name of the attribute being sent over from Entra. This is commonly set to “UDC_IDENTIFIER”.
		* Ex. `<UDCIDAttributeName>UDC_IDENTIFIER</UDCIDAttributeName>`
5. Save the `bdm.sso.config` file.
6. Restart IIS
7. Use the following URL format to access the site via SSO.
	* Replace `Datasourcename` with the name of your data source in AppXtender. (ie. TEST, PROD, etc)
	* Ex. `https://server.univ.edu/AppXtender/DataSources/*Datasourcename*/?sso=true&DSN=*Datasourcename*`

### Configuring SSO Integration with Banner Admin Pages and AppXtender
1. Go to `EXAINST` in Banner Admin Pages
2. Set the ApplicationXtender Web Access Root value:
	* Modify the URL and port to point to your AppXtender instance 
	* Ex. `https://server.univ.edu/AppXtender/ISubmitQuery.aspx?sso=true`
