# Integrating Microsoft Entra with Ellucian Banner Apps

### *by Tim Authement, Nicholls State University*


## Introduction

The purpose of this documentation is to explain how to integrate Microsoft Entra as a single sign-on (SSO) provider with various Ellucian Banner apps. While Ellucian does have documentation pertaining to setting up SSO with different apps, those documents only cover Ethos Identity Management as the identity provider. I have done my best, through trial and error, to compile documentation to get several Banner applications working with Microsoft Entra and SAML. What works in my environment may not work in your environment. Modify your approach as needed. This documentation should only serve as a jump start on integrating Microsoft Entra with Banner apps.

Some key words in this document are service provider (SP) and identity provider (IDP). A service provider will be an application which provides a service to your users, such as Application Navigator, Student Self-Service, and Student Registration Self-Service. An identity provider is a service that provides authentication for your application, such as Microsoft Entra or Ethos Identity Management.

This documentation will cover manually configuring the applications and briefly discuss using ESM to deploy Banner Admin Pages and Banner Access Management. You should modify these methods to suit your deployment methods, or work with the person responsible for deploying your applications. The respective applications will have documentation in the Customer Center that discuss deployment. The Ellucian SSO Handbook is also a great resource for more information on implementing SAML with your applications. 

This documentation is presented without warranty. You know your environment better than I do. Modify these instructions to your own needs. 

## Microsoft Entra

Formerly called Microsoft Azure Active Directory (Azure AD), Microsoft Entra is a cloud-based identity and access management service that provides single sign-on (SSO) capabilities for organizations. MS Entra supports two SSO protocols, OpenID Connect and SAML 2.0. This guide will be using SAML 2.0 since Ellucian supports SAML and CAS on most of their apps. CAS is not supported by MS Entra.

### Creating an Entra Application

1. Login to the Microsoft Entra Admin Center
2. Go to Application > Enterprise Applications in the menu
3. Click on “New Application”
4. On the Browse Microsoft Entra Gallery page, click on “Create your own application”
5. In the popup, enter a name for your application.
6. In the “What are you looking to do with your application?” section, choose “Integrate any other application you don't find in the gallery (Non-gallery)”. This will allow you to create an application capable of using SAML.
7. Click Create
8. Add users and groups allowed to authentication
	1. Within the Enterprise Application menu, click on “Users and groups”.
	2. Click “Add user/group”
	3. On the “Add Assignment” page, click on “None selected” under “Users and groups”.
	4. Add any user and/or groups that are allowed to login to the Entra application. If users are not specified individually or in a specified group, the user will receive a message about the application being blocked after logging in. 
		* Please note that Entra does not recursively look at nested group membership. Suppose Group 1 is added to the “Users and groups” page and a user is in Group 2. If Group 2 is a nested member of Group 1, Entra will not traverse nested group memberships. Group 2 will need to be added to the “Users and groups” page as well.
	5. Click Assign
9. In the enterprise application menu, click on “Single sign-on”.
10. Click on the SAML box
11. On the “Set up Single Sign-On with SAML” page, you will see five boxes labeled with numbers. The steps below will detail each one.
	* **Box 1 - Basic SAML Configuration**
		* Click Edit at the top right of the first box labeled “Basic SAML Configuration”.
Enter a unique name in the “Identifier (Entity ID)” field. This name will be used in your service provider XML file later on and should be unique to each Banner application. 
			* Example: `banner-stuss-test-sp`
		* Enter the ACS (Assertion Consumer Service) URL of your Banner app in the “Reply URL” field. Below is just an example. Each Banner app section will include more specific examples. 
			* Example: `https://server.univ.edu:8443/StudentSelfService/saml/SSO`
			* The URL is case-sensitive.
			* Modify the URL and port to point to your server.
		* Enter a URL in the “Logout URL” field. This URL will be used to send data to the app in the event of a Single Logout (SLO) being triggered. If you don’t use SLO, you may ignore this field. The URL will follow the example below. Replace <entity-id> with the Entity ID entered in the “Identifier” field. Below is just an example. Each Banner app section will include more specific examples.
			* Example: `https://server.univ.edu:8443/StudentSelfService/saml/SingleLogout/alias/<entity-id>`
		* Click Save
	* **Box 2 - Attributes & Claims**
		* Click Edit at the top right of the “Attributes & Claims” box.
		* You can leave the “Unique User Identifier (Name ID)” claim alone unless an app requires it. None of the apps in this guide use it.
		* Delete all of the default claims under “Additional Claims”. None of these will be used in the app.
		* Click “Add new claim” at the top of the page. 
		* Enter “UDC_IDENTIFIER” in the Name field. It must be in all caps. 
			*This is the default attribute shipped by Ellucian for all apps. The name can be changed in the respective SSB app’s groovy file under the “authenticationAssertionAttribute” setting. You will need this value synced to your Entra tenant. If you’re unsure if the value is synced, contact your Entra administrators. This value will be used to identify the user in the Banner DB. 
		* Leave “Namespace” empty.
		* Select “Attribute” for “Source”
		* For “Source Attribute”, select the attribute which your UDCID is being synced to.
		* Leave the “Claim Conditions” and “Advanced SAML claims options” sections alone.
		* Click Save
	* **Box 3 - SAML Certificates**
		* Click on the Download link next to “Federation Metadata XML”.
			* Save and rename this file to be relevant to the app you will be deploying. This will be the identity provider metadata XML file for some of the apps. Each app will have its own IDP metadata XML file. 
		* Click Edit at the top right of the “SAML Certificates” box.
Here you will find what will be referred to as your IDP certificate. Each Entra app will have its own IDP certificate. If you downloaded the IDP metadata XML file, you don’t need to download the certificate since it’s already included in the XML file. However, if you want to manually create your own IDP XML files, you will need to download the IDP certificate. 
			* In the list of certificates, click on the three dots of the Active certificate.
			* Click on “PEM certificate download” to download the X.509 version of the certificate. This will be used in the setup of the applications.
				* To keep track of things, I recommend renaming the file similar to your entity ID.
					* Example: `banner-stuss-test-idp.pem`
		* For “Signing Option”, select “Sign SAML Assertion”.
		* For “Signing Algorithm”, select “SHA-256”. 
		* Under “Notification Email Addresses,” add any email addresses that should be alerted when the certificate is about to expire. You will need to update the certificate in the IDP XML file when it expires.
	* **Box 4 - Set up "Entra App Name"**
		* There’s nothing to configure here. The URLs listed here will be used later on in the app configuration files. You may want to document them, or come back to them later.
	* **Box 5 - Test Single Sign-on with "Entra App Name"**
		* Ignore this step for now. If the Banner app is not configured yet, this test will not work. You can use this after the Banner app is setup.

### Setting a Session Timeout Period

You will need to create a Conditional Access policy to configure the session timeout period for Banner apps to avoid getting errors such as “Invalid username/password” or “Session Initialization Failed”. When you configure your Banner Admin and Banner Self-Services apps, you should set the variable called “maxAuthenticationAge” (in seconds) to match the timeout period you set here. For example, 1 hour = 3600 seconds. The “maxAuthenticationAge” variable should not be lower than the timeout period set in Entra or you will errors. The current default session period for Entra is 90 days. The lowest amount of time you can set is one hour.

Please refer to Microsoft’s documentation on how to [configure adaptive session lifetime policies](https://learn.microsoft.com/en-us/entra/identity/conditional-access/howto-conditional-access-session-lifetime). You should only need to set up Policy 1 (Sign-in Frequency Control). Policy 2 and 3 are optional. 

You will need to add new Entra applications to the conditional access policy each time an application is created. If you have multiple applications to create, I suggest creating the Entra applications first then adding multiple applications to the policy all at once to save time.

## Creating Keystores

A Java keystore is needed to generate a self-signed certificate for the service provider (SP) applications (Admin Pages, Self-Service apps, Banner 8 Self-Serivce, etc.). The commands used in this documentation are for the Linux/macOS version of keytool. However, the Windows version of keytool should be very similar. Since the arguments available in keytool can vary depending on the version installed, it is recommended to look up the documentation for that version.

The Java keystore should be stored somewhere on your server that is readable by the Linux user running your Tomcat server. The command below will generate a self-signed certificate that is valid for 9999 days. Please modify the validity period to your liking. To ensure compatibility with Entra, the keystore must use SHA256 with RSA as the signing algorithm. For compatibility with Banner apps, the keystore type must be in JKS format. In my testing, the Banner apps do not support PKCS12 keystores for the SAML configuration.

Once the keystore is created, the X.509 public certificate will need to be exported. Keep the file stored away to be used within the service provider XML file later. The same keystore file (and exported public certificate) can be used for all of the applications. You do not need to create a keystore for each application. 

It is also recommended to create a keystore for each environment (DEV, TEST, PROD) just to keep things separate, but not required. Do what is best for your work environment. This documentation will be based around the TEST environment.

### Creating the Java Keystore for Service Provider Applications

1. Open a command line/terminal
2. Navigate to a directory to store the Java keystore
3. Run the following command:
	* <pre>keytool -genkey -keyalg RSA -alias banner-saml-sp
			-keystore samlkeystore.jks -validity 9999
			-keysize 2048 -storetype JKS -sigalg SHA256withRSA</pre>
4. Store the Java keystore on your server in a directory that is readable by the OS user that runs the Tomcat application.
	* For this documentation, the `/u01/app/tomcat/saml` directory will be used and “tomcat” is the user that runs the Tomcat application.
5. Safely store the passwords used for the keystore and signing key. You will need these later when you configure SAML for the applications.

### Creating a PKCS #12 version of the Java Keystore

This version of the self-signed certificate will be used for Banner AppXtender. If you do not use AppXtender, feel free to ignore this step.

1. Convert the JKS file to a PFX file using the following command:
	* <pre>keytool -importkeystore -srckeystore samlkeystore.jks 
	 -srcstoretype JKS -deststoretype PKCS12 -destkeystore samlkeystore.p12</pre>
2. You will need to enter the destination and source keystore passwords. This keystore and password will only be used in the AppXtender configuration file.

### Exporting the Service Provider Public Certificate

This is a two-step process. You will first need to export the public certificate of the signing key from the Java keystore. Then you will need to convert the public certificate to X.509 format. 

1. Export the public certificate using the following command:
	* `keytool -export -alias banner-saml-sp -keystore samlkeystore.jks -file banner-saml-sp.pem`
2. A file named “banner-saml-sp.pem” will be created.
3. Convert the PEM file to X.509 format using the following command:
	* `keytool -printcert -rfc -file banner-saml-sp.pem > banner-saml-sp.crt`
4. A file named “banner-saml-sp.crt” will be created.
5. This will be used as your service provider (SP) certificate later on.

## Creating a Service Provider XML File

1. Download the example service provider XML file.
2. To edit this file, you will need the entity ID from “Box 1 - Basic SAML Configuration” of the Entra application setup. You will also need the service provider public certificate that was exported in the “Exporting the Service Provider Public Certificate” section.
3. Edit the following elements in the XML file.
	* `<md:SingleSignOnService>`
		* Set the `ID` and `entityID` attribute to be the same entity ID used in the Entra application setup (box 1 of the SAML setup).
	* `<ds:X509Certificate>`
		* In both `<ds:X509Certificate>` elements, add the X.509 certificate which you exported from the Java keystore. 
		* You only need to copy the encoded section between `-----BEGIN CERTIFICATE-----` and `-----END CERTIFICATE-----`. 
	* `<md:SingleLogoutService>`
		* In both `<md:SingleLogoutService>` elements, edit the `Location` attributes to the same URL. Use the following example and modify it for the application you are configuring. Replace <entity-id> with the Entity ID used in step 1b.
		* Example: `https://server.univ.edu:8443/StudentSelfService/saml/SingleLogout/alias/<entity-id>`
		* The URL is case-sensitive.
		* Modify the URL and port to point to your server.
	* `<md:AssertionConsumerService>`
		* In both `<md:AssertionConsumerService>` elements, edit the `Location` attributes to the same URL. Use the following example and modify it for the application you are configuring.
		* Example: `https://server.univ.edu:8443/StudentSelfService/saml/SSO`
		* The URL is case-sensitive.
		* Modify the URL and port to point to your server.
4. Save the file 
5. Banner 9 Self-Service and Application Navigator only:
	* Store the XML file in a directory on your server that is readable by your Tomcat server. 
	* Example: `/u01/app/tomcat/saml`
	* Other apps like Banner Admin Pages, Banner Access Management, and SSOManager (Banner 8 Self-Service), will require the metadata files to be added to the WAR files.

## Creating an Identity Provider XML File

Some apps discussed in this guide allow you to use an IDP metadata file downloaded from Entra. For those apps that cannot use the downloaded file, you must manually create an IDP metadata file.

### Banner 9 Self-Service Apps and Application Navigator

In my testing, these apps support using the Federation Metadata XML file that can be downloaded from the MS Entra SAML configuration page. 

1. Go to the Entra SAML configuration page
2. In the “SAML Certificates” section, click Download next to “Federated Metadata XML”.
3. Rename the downloaded file to a name of your choosing. Preferably one without spaces so you don’t have to escape any of the spaces later.
	* Example: `banner-stuss-test-idp.xml`
4. Store the file to a location on your server that is readable by your Tomcat server. This file location will be used in the SSB App Groovy File section.
	* Example: `/u01/app/tomcat/saml`

### Banner Admin Pages, Banner Access Management, AppXtender/AppEnhancer, and Banner 8 Self-Service

1. Download the sample identity provider XML file.
2. To edit this file, you will need the identity provider PEM certificate that was downloaded during the “Box 3 - SAML Configuration” section of the Entra application setup. You will also need the URLs from “Box 4 - Set up *Entra App Name*”.
3. Edit the following elements in the XML file.
	* `<md:EntityDescriptor>`
		* Change the “entityID” attribute to match the “Microsoft Entra Identifier” from the Entra SAML setup in “Box 4 - Set up *Entra App Name*”. This URL will start with sts.windows.net and include your Entra tenant ID.
Ex. https://sts.windows.net/*entra-tenant-id*/
	* `<ds:X509Certificate>`
		* Within the two <ds:X509Certificate> tags, insert the identity provider public certificate that was downloaded (PEM Certificate Download) from the "SAML Certificates" section of the Entra SAML configuration.
	* `<md:SingleLogoutService>`
		* In both `<md:SingleLogoutService>` elements, edit the `Location` attributes to the same URL. Copy the URL from the “Logout URL” field found in the Entra SAML configuration under “Box 4 - Set up *Entra App Name*”.
	* `<md:SingleSignOnService>`
		* In both `<md:SingleSignOnService>` elements, edit the `Location` attributes to the same URL. Copy the URL from the “Login URL” field found in the Entra SAML configuration under “Box 4 - Set up *Entra App Name*”.
4. Save the file and store it to a location on your server that is readable by your Tomcat server. This file location will be used in the SSB App Groovy File section.
	* Example: `/u01/app/tomcat/saml`

## Configuring Banner 9 Self-Service Apps and Application Navigator

An sample Student Self-Service groovy file is available in the `examples` directory, but do not use the sample file for all applications. It is only for demonstration purposes. The configuration files can change between releases and each app has different configurations. However, the SAML configuration should be fairly similar in all of the apps. You must modify the groovy file delivered with the version of the app being deployed.

**NOTE:** If you were previously using CAS for SSO, make sure you set “active” to false in the “CAS Configuration” section.
	
* Example: `active = false`
	
The following settings will be broken down by the sections within the .groovy file.

### Authentication Provider Configuration 

* `ssoEnabled`
	* Set to true to enable single sign-on for the application.
	* Example: `boolean ssoEnabled = true`
* `authenticationProvider`
	* Set it to “saml” to use SAML as the SSO protocol.
	* **NOTE:** Use single quotes.
	* Example: `authenticationProvider = ‘saml’`
* authenticationAssertionAttribute
	* Set to “UDC_IDENTIFIER”

### SAML Configuration

* `grails.plugin.springsecurity.saml.active`
	* Set this to true to enable SAML for the app.
	* Example: `grails.plugin.springsecurity.saml.active = true`
* `banner.sso.authentication.saml.localLogout`
	* If you do not wish for the app to participate in Single Logout, set this to true.
	* This value does need to be in quotes despite being a boolean.
	* Example: `banner.sso.authentication.saml.localLogout = 'false'`
* `grails.plugin.springsecurity.saml.keyManager.storeFile`
	* Set to the file location where the Java keystore is stored on the server. 	* This will need to be prepended with “file:”.
	* Example: `‘file:/u01/app/tomcat/saml/samlkeystore.jks’`
* `grails.plugin.springsecurity.saml.keyManager.storePass`
	* Set to the password used for the keystore. This may not be the same password as the signing key password for the signing key within the keystore. 	* Refer to the passwords you stored when you created the keystore.
* `grails.plugin.springsecurity.saml.keyManager.passwords`
	* Use an associative array with the alias of the certificate within the Java keystore as the key and the certificate’s signing key password.
	* Example: `grails.plugin.springsecurity.saml.keyManager.passwords = [ 'certificate-alias': 'signing-key-password' ]`
* `grails.plugin.springsecurity.saml.keyManager.defaultKey`
	* Set to the ‘certificate-alias’ from the previous step.
	* Example: `grails.plugin.springsecurity.saml.keyManager.defaultKey = 'certificate-alias'`
* `grails.plugin.springsecurity.saml.maxAuthenticationAge`
	* Set this value to the amount of time (in seconds) that is configured in the conditional access policy created in the “Setting a Session Timeout Period” section.
	* This setting should not be lower than the session time configured in the conditional access policy or you will receive error messages when accessing the application.
	* So you don’t have to bust out the calculator app: 3600 seconds = 1 hour
* `grails.plugin.springsecurity.saml.metadata.sp.file`
	* Set to the file location, on the server, of your service provider XML file created earlier. 
	* **NOTE:** You do not need to prepend it with “file:”.
	* Example: `/u01/app/tomcat/saml/banner-stuss-test-sp.xml`
* `grails.plugin.springsecurity.saml.metadata.providers`
	* Set to the file location, on the server, of your identity provider XML file created earlier.
	* This is an associative array. The key should be “eis” and the value set to the file location.
	* Example: `[eis:'/u01/app/tomcat/saml/banner-entra-idp.xml']`
* grails.plugin.springsecurity.saml.metadata.defaultIdp
	* Set this to the entity ID of the IDP (Entra). 
	* This can be found in the “Set up *Entra App Name*” box of the Entra SAML setup. The URL will start with https://sts.windows.net and include your Entra tenant ID.
	* Example: `https://sts.windows.net/*entra-tenant-id*/`
* `grails.plugin.springsecurity.saml.metadata.sp.defaults`
	* This is an associative array of settings for the service provider.
local
	* Set to true. No quotes are needed for booleans.
	* Example: `local: true`
* `alias`
	* Set to the entity ID of your SSB application. 
	* This is the value of “Identifier (Entity ID)” in the “Basic SAML Configuration” section of the Entra SAML configuration. It is also referenced in the service provider XML file.
	* Ex. alias: `'ssb-app-entityid-sp'`
* `securityProfile`
	* Set to ‘metaiop’.
	* Since we’re using a self-signed certificate, we need to use “metaiop”. According to [this site](https://docs.spring.io/spring-security-saml/docs/current/reference/html/security.html#configuration-security-profiles-metaiop), “MetaIOP mode certificates are not checked for expiration or revocation.”
	* Example: `securityProfile: 'metaiop'`
* `signingKey`
	* Set to the alias of the certificate within the Java keystore.
	* Example: `signingKey: 'certificate-alias'`
* `encryptionKey`
	* Set to the alias of the certificate within the Java keystore.
	* Ex. `encryptionKey: 'certificate-alias'`
* `tlsKey`
Set to the alias of the certificate within the Java keystore.
	* Example: `tlsKey: 'certificate-alias'`
* `requireArtifactResolveSigned`
	* Set to false.
	* Example: `requireArtifactResolveSigned: false`
* `requireLogoutRequestSigned`
	* Set to false.
	* Example: `requireLogoutRequestSigned: false`
* `requireLogoutResponseSigned`
	* Set to false.
	* Example: `requireLogoutResponseSigned: false`

Save the file and redeploy using your preferred deployment method.

