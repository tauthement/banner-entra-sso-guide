## Banner 9 Self-Service Apps and Application Navigator

An sample Student Self-Service groovy file is available in the `examples` directory, but do not use the sample file for all applications. It is only for demonstration purposes. The configuration files can change between releases and each app has different configurations. However, the SAML configuration should be fairly similar in all of the apps. You must modify the groovy file delivered with the version of the app being deployed.

The following Banner 9 Self-Service apps have been tested with these instructions: Communication Management, Employee SSB, Extensibility, Faculty SSB, Finance SSB, General SSB, Student Registration SSB, and Student SSB. Application Navigator is not a SSB app, but it is built on the same framework as the SSB 9 apps so it uses a similar configuration.

### Task List for Banner 9 SSB and Application Navigator

- [ ] [Create an app in Entra](microsoft-entra.md#creating-an-entra-application)
    - [ ] [Configuring Entra application for Banner 9 SSB and Application Navigator](#configuring-entra-for-banner-9-self-service-apps-and-application-navigator)
- [ ] [Configure a Sign-in Frequency Conditional Access Policy](microsoft-entra.md#creating-a-sign-in-frequency-conditional-access-policy)
- [ ] [Locate or create a Java Keystore](keystores.md#creating-a-java-keystore-for-service-provider-applications)
- [ ] [Create a Service Provider XML File](sp-metadata-file.md#creating-a-service-provider-xml-metadata-file)
- [ ] [Create an Identity Provider XML File](idp-metadata-file.md#banner-9-self-service-apps-and-application-navigator-idp-xml-metadata-file)
- [ ] [Configure the application's .groovy file](#application-groovy-file)
- [ ] [Configure the application session timeout](#configuring-session-timeouts-for-application-navigator-and-ssb-9-apps)
- [ ] [Whitelist Application Navigator in Google Chrome](#whitelisting-application-navigator-in-google-chrome)
- [ ] [Whitelist Application Navigator in Mozilla Firefox](#whitelisting-application-navigator-in-mozilla-firefox)

### Configuring Entra for Banner 9 Self-Service Apps and Application Navigator

Use the following settings for creating an app in Entra:

* Entity ID = `banner-stuss-test-sp`
    * This can be whatever you want. Just make it unique to your Entra tenant. 
    * I will be using Banner 9 Student Self-Service as an example.
* Reply URL (ACS URL) = `https://server.univ.edu:8443/StudentSelfService/saml/SSO`
    * The URL is case-sensitive. Make sure “SSO” is capitalized at the end.
    * Modify the URL and port to point to your server.
* Logout URL = `https://server.univ.edu:8443/StudentSelfService/saml/SingleLogout/alias/banner-stuss-test-sp`
    * Make sure to edit the entity ID at the end of the URL.
    * This URL is case-sensitive.
    * Modify the URL and port to point to your server.
* Include the following attribute:
    * `UDC_IDENTIFIER`

### Configuring Banner 9 Self-Service Apps and Application Navigator

#### Java Keystore File

Please refer to the [Creating Keystores](keystores.md#creating-keystores) section to learn how to create a keystore. The same keystore can be used for all of the apps in this guide. Place the Java keystore file in a location on the app server that is readable by Tomcat. For instance, I stored mine in `/u01/app/tomcat/saml` along with my XML metadata files. The directory is owned by the `tomcat` user and group.

#### Service Provider XML Metadata File

Copy the service provider XML metadata file found in the `examples` directory of this project. Modify it according to the instructions in the [Creating a Service Provider XML Metadata File](sp-metadata-file.md#creating-a-service-provider-xml-metadata-file) instructions in this guide. 

The file will need to be stored on the same server as your application and be readable by Tomcat. For example, I stored the file at `/u01/app/tomcat/saml` and made the `tomcat` user and group the owner of the directory and the files within it. I store the identity provider XML metadata and Java keystore files in there as well.

#### Identity Provider XML Metadata File

For the Banner 9 Self-Service apps and Application Navigator, I had luck with using the "Federation Metadata XML" file downloaded from Entra. Please see the [Banner 9 Self-Service Apps and Application Navigator](#banner-9-self-service-apps-and-application-navigator) section of [Creating an Identity Provider XML Metadata File](idp-metadata-file.md#creating-an-identity-provider-xml-metadata-file) for more instructions. If those files don't work for you, please try manually creating the identity provider XML metadata file using the sample file in the `examples` directory of this project.

This file should be stored in the same location as the service provider XML metadata file and Java keystore.

#### Application Groovy File
    
Each of these apps are configured using a .groovy file. If you've ever deployed a SSB 9 app, you should be familiar with these. A [sample file](/examples/StudentSelfService_configuration.groovy) based on Student Self-Service's config file can be found in the [examples](/examples/) directory of this project.

The following settings will be broken down by the sections within the .groovy file.

##### Authentication Provider Configuration 

* `ssoEnabled`
    * Set to `true` to enable single sign-on for the application.
    * `boolean ssoEnabled = true`
* `authenticationProvider`
    * Set it to `saml` to use SAML as the SSO protocol.
    * **NOTE:** Use single quotes.
    * `authenticationProvider = 'saml'`
* `authenticationAssertionAttribute`
    * Set to `UDC_IDENTIFIER`
    * `authenticationAssertionAttribute = 'UDC_IDENTIFIER'`

##### CAS Configuration

This will make sure that CAS is disabled. It is important to only have one single sign-on method enabled at a time.

* `active`
    * Set to `false`
    * Since SAML will be used, make sure CAS is disabled. Double check this setting if you were previously using CAS.
    * Ex. `active = false`	

##### SAML Configuration

* `grails.plugin.springsecurity.saml.active`
    * Set this to `true` to enable SAML for the app.
    * Ex. `grails.plugin.springsecurity.saml.active = true`
* `banner.sso.authentication.saml.localLogout`
    * If you do not wish for the app to participate in Single Logout, set this to true.
    * This value needs to be in quotes despite being a boolean.
    * Ex. `banner.sso.authentication.saml.localLogout = 'false'`
* `grails.plugin.springsecurity.saml.keyManager.storeFile`
    * Set to the file location where the Java keystore is stored on the server. 	
    * This will need to be prepended with `file:`.
    * Ex. `grails.plugin.springsecurity.saml.keyManager.storeFile = 'file:/u01/app/tomcat/saml/samlkeystore.jks'`
* `grails.plugin.springsecurity.saml.keyManager.storePass`
    * Set to the password used for the keystore. This may not be the same password as the signing key password for the signing key within the keystore. 	
    * Refer to the passwords you stored when you created the keystore.
    * Ex. `grails.plugin.springsecurity.saml.keyManager.storePass = 'keystore-password'`
* `grails.plugin.springsecurity.saml.keyManager.passwords`
    * Use an associative array with the alias of the certificate within the Java keystore as the key and the certificate's signing key password.
    * Ex. `grails.plugin.springsecurity.saml.keyManager.passwords = ['certificate-alias':'signing-key-password']`
* `grails.plugin.springsecurity.saml.keyManager.defaultKey`
    * Set to the `certificate-alias` from the previous step.
    * Ex. `grails.plugin.springsecurity.saml.keyManager.defaultKey = 'certificate-alias'`
* `grails.plugin.springsecurity.saml.maxAuthenticationAge`
    * This value sets the SAML token lifetime of the application. Set this value to be equal to the [Sign-In Frequency Conditional Access Policy](microsoft-entra.md#creating-a-sign-in-frequency-conditional-access-policy). 
        * Entra's [default SAML token lifetime](https://learn.microsoft.com/en-us/entra/identity-platform/configurable-token-lifetimes#token-lifetime-policies-for-access-saml-and-id-tokens) is 1 hour. This is separate from the application's SAML token lifetime, which you are configuring here.
        * According to [this Ellucian guide](https://elluciansupport.service-now.com/customer_center?sys_kb_id=b85a1214c34d56986f99f8da050131e0&id=kb_article_view), "the SAML token lifetime setting for Banner applications should be greater than the IdP (Identity Provider) SAML token lifetime setting." Therefore, you must set the SAML token lifetime for any application to be greater than 1 hour.
    * A few examples: 3600 seconds = 1 hour, 14400 seconds = 4 hours, 43200 seconds = 12 hours
    * Ex. `grails.plugin.springsecurity.saml.maxAuthenticationAge = 43200`
* `grails.plugin.springsecurity.saml.metadata.sp.file`
    * Set to the file location, on the server, of your service provider XML metadata file created earlier. 
    * **NOTE:** You **do not** need to prepend it with `file:`.
    * Ex. `grails.plugin.springsecurity.saml.metadata.sp.file = /u01/app/tomcat/saml/banner-stuss-test-sp.xml`
* `grails.plugin.springsecurity.saml.metadata.providers`
    * Set to the file location, on the server, of your identity provider XML metadata file created earlier.
    * This is an associative array. The key should be `eis` and the value set to the file location.
    * Ex. `grails.plugin.springsecurity.saml.metadata.providers = [eis:'/u01/app/tomcat/saml/banner-entra-idp.xml']`
* `grails.plugin.springsecurity.saml.metadata.defaultIdp`
    * Set this to the entity ID of the identity provider (Entra). 
    * This URL can be found in the “Set up *Entra App Name*” box of the Entra SAML setup as the `Microsoft Entra Identifier`. The URL will start with `https://sts.windows.net` and include your Entra tenant ID.
    * Ex. `grails.plugin.springsecurity.saml.metadata.defaultIdp = https://sts.windows.net/*entra-tenant-id*/`
* `grails.plugin.springsecurity.saml.metadata.sp.defaults`
    * This is an associative array of settings for the service provider.
    * `local`
        * Set to `true`. No quotes are needed for booleans.
        * Ex. `local: true`
    * `alias`
        * Set to the entity ID of your SSB application. 
        * This is the value of `Identifier (Entity ID)` in the `Basic SAML Configuration` section of the Entra SAML configuration setup in [Creating an Entra Application](microsoft-entra.md#creating-an-entra-application). It is also referenced in the service provider XML metadata file.
        * Ex. `alias: 'banner-stuss-test-sp'`
    * `securityProfile`
        * Set to `metaiop`.
        * Ex. `securityProfile: 'metaiop'`
    * `signingKey`
        * Set to the alias of the certificate within the [Java keystore](keystores.md#creating-a-java-keystore-for-service-provider-applications).
        * Ex. `signingKey: 'certificate-alias'`
    * `encryptionKey`
        * Set to the alias of the certificate within the [Java keystore](keystores.md#creating-a-java-keystore-for-service-provider-applications).
        * Ex. `encryptionKey: 'certificate-alias'`
    * `tlsKey`
        * Set to the alias of the certificate within the [Java keystore](keystores.md#creating-a-java-keystore-for-service-provider-applications).
        * Ex. `tlsKey: 'certificate-alias'`
    * `requireArtifactResolveSigned`
        * Set to `false`.
        * Ex. `requireArtifactResolveSigned: false`
    * `requireLogoutRequestSigned`
        * Set to `false`.
        * Ex. `requireLogoutRequestSigned: false`
    * `requireLogoutResponseSigned`
        * Set to `false`.
        * Ex. `requireLogoutResponseSigned: false`
* Save the file and redeploy using your preferred deployment method.

### Configuring Session Timeouts for Application Navigator and SSB 9 Apps

Application Navigator and Banner 9 Self-Service each have their own session timeout settings. Sessions are stored in Tomcat's memory and once a user's session has been inactive for the configured period, it is freed from Tomcat's memory. This may result in the user needing to reauthenticate. If the user's SAML token for the application is still valid, the application should go through the SSO flow without the user needing to reauthenticate with their credentials. 

For example, if you set the session timeout to be 4 hours and the SAML token is valid for 12 hours, a user could access the site again after 5 hours of inactivity and login without needing to reauthenticate. Tomcat will just create a new session. However, if the SAML token and the Tomcat session have both expired, the user will be prompted to reauthenticate with their credentials.

This setting is often configured using the `web.xml` file found in Tomcat's `conf` directory. However, if an application includes a `web.xml` with its `WEB-INF` directory, it will override Tomcat's `web.xml` settings. In this instance, Application Navigator and Banner 9 Self-Service override the setting on the application side through a setting in GUACONF.

**NOTE:** The SAML token lifetime is configured using the `grails.plugin.springsecurity.saml.maxAuthenticationAge` setting in the application's groovy file. The SAML token lifetime is not the same setting as the Tomcat session timeout. You must configure both. 

#### Configuring Session Timeouts for Banner 9 Self-Service Applications

1. Login to Banner Admin Pages
2. Open `GUACONF`
3. Select the Application ID of the Banner 9 Self-Service you are configuring
    ex. `SSS` for Student Self-Serivce
4. Click OK
5. On the Configurations tab, set the `defaultWebSessionTimeout` value to an integer in the amount of seconds
    ex. `14400` (4 hours)
6. Click Save

#### Configuring the Session Timeout for Application Navigator

1. Login to Banner Admin Pages
2. Open `GUACONF`
3. Select `AppNav` as the Application ID
4. Click OK
5. On the Configurations tab, set the `seamless.sessionTimeout` value to an integer in the amount of minutes
    ex. `240` (4 hours)
6. Click Save

### Whitelisting Application Navigator in Google Chrome

Google Chrome now blocks third-party cookies by default to limit cross-site tracking. A third-party cookie is any cookie that does not belong to a server using the same domain as your Application Navigator server. For instance, a cookie from server2.univ.edu would be a first-party cookie if your Application Navigator server is appnav-test.univ.edu since it uses the same domain (univ.edu). A cookie from login.microsoftonline.com does not use the univ.edu domain, so it would be considered a third-party cookie. 

When you load a form in Application Navigator, App Nav attempts to load Admin Pages in an inline frame, which contacts login.microsoftonline.com to verify that the user is logged in to Entra. When Entra tries to store a third-party cookie from login.microsoftonline.com, Application Navigator will not finish loading the Admin Pages form due to the Chrome blocking third-party cookies. This usually results in an error that states "Authentication Failed / Problem in external authentication service".

To fix this, we must set Google Chrome to allow Application Navigator to load third-party cookies.

* Open Chrome's settings
* Go to `Privacy and security`
* Click `Third-party cookies`
* Scroll to the `Sites allowed to use third-party cookies` section
* Click the `Add` button
* Add the URI of the Application Navigator server
    * Example: `appnav-test.univ.edu`
    * NOTE: You may also whitelist your entire domain by using the following format: `[*.]univ.edu`.
* Click `Add`
* Reload Application Navigator and try to load a form.

If your institution uses an MDM to manage computers, Google Chrome can be mass-configured to whitelist Application Navigator using the [BlockThirdPartyCookies](https://chromeenterprise.google/policies/#BlockThirdPartyCookies) and [CookiesAllowedForUrls](https://chromeenterprise.google/policies/#CookiesAllowedForUrls) settings. The `BlockThirdPartyCookies` setting must be set to `true` for `CookiesAllowedForUrls` to work. Please speak to your MDM administrator about deploying the exception list.

Additional information can be found at the following site: [Allow or restrict third-party cookies](https://support.google.com/chrome/a/answer/14439269).

### Whitelisting Application Navigator in Mozilla Firefox

Just like Google Chrome, Mozilla has decided to start blocking third-party cookies in Firefox. Firefox's Enhanced Tracking Protection default setting of "Standard" doesn't allow for Admin Pages to load because of the cookie setting as well as Entra sending the `X-Frame-Options=deny` header in the response. We can get around this by whitelisting Application Navigator's URL in Firefox's Enhanced Tracking Protection settings.

* Open Firefox's settings
* Go to `Privacy & Security`
* Under the `Enhanced Tracking Protection` section, click `Manage Exceptions...` 
* In the `Address of website` field, enter the base URL of your Application Navigator server.
    * Example: `https://appnav-test.univ.edu:8443`
    * Add all instances of Application Navigator that your institution uses (ie. TEST, PROD).
* Click `Add Exception`
* Click `Save Changes`
* Reload Application Navigator and try to load a form. 

If your institution uses an MDM to manage computers, Firefox can be mass-configured to whitelist Application Navigator using the [EnableTrackingProtection](https://mozilla.github.io/policy-templates/#enabletrackingprotection) setting. Please speak to your MDM administrator about deploying the exception list.