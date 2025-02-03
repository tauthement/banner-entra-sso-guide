## AppXtender/AppEnhancer (Banner Document Managment)

OpenText AppXtender is an app that integrates with Banner Document Management. Recently, the app was renamed to AppEnhancer. I will refer to it as AppXtender since I used AppXtender 20.4 to write this guide. It looks like some of these settings may have changed in AppEnhancer, but I do not have an install to test it with. Please refer to your version's documentation if these instructions do not work.

To get AppXtender working with Entra, you must first install the SSO files. Instructions to download and install the files can be found in the Ellucian documentation linked in the [Configuring AppXtender](#configuring-appxtender) section.

### Configuring Entra for AppXtender

* Entity ID = `banner-bdm-test-sp`
    * This can be whatever you want. Just make it unique to your Entra tenant.
* Reply URL (ACS URL) = `https://server.univ.edu/AppXtender/BdmSamlSso`
    * Assume the URL is case-sensitive.
    * Modify the URL and port to point to your server.
* Logout URL = 
    * Leave blank. The BDM documentation does not mention a logout URL.
* Include the following attribute:
    * `UDC_IDENTIFIER`

### PKCS #12 Keystore File

Please refer to the [Creating a PKCS #12 version of the Java Keystore](#creating-a-pkcs-12-version-of-the-java-keystore) section to create a PKCS #12 version of the Java keystore. Place the file in the AppXtender install directory. This directory will also have the `bdm.sso.config` file that needs to be edited.

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