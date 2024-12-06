# Integrating Microsoft Entra with Ellucian Banner Apps

**Author:** Tim Authement, Nicholls State University <br>
**Source:** [https://github.com/tauthement/banner-entra-sso-guide](https://github.com/tauthement/banner-entra-sso-guide)

## Table of Contents

* [Introduction](#introduction)
* [Microsoft Entra](#microsoft-entra)
	* [Creating an Entra Application](#creating-an-entra-application)
	* [Setting a Session Timeout Period](#setting-a-session-timeout-period)
* [Creating Keystores](#creating-keystores)
	* [Creating a Java Keystore for Service Provider Applications](#creating-a-java-keystore-for-service-provider-applications)
	* [Creating a PKCS #12 version of the Java Keystore](#creating-a-pkcs-12-version-of-the-java-keystore)
	* [Exporting the Service Provider Public Certificate](#exporting-the-service-provider-public-certificate)
* [Creating a Service Provider XML Metadata File](#creating-a-service-provider-xml-metadata-file)
* [Creating an Identity Provider XML Metadata File](#creating-an-identity-provider-xml-metadata-file)
	* [Banner 9 Self-Service Apps and Application Navigator](#banner-9-self-service-apps-and-application-navigator)
	* [Banner Admin Pages, Banner Access Management, AppXtender/AppEnhancer, and Banner 8 Self-Service](#banner-admin-pages-banner-access-management-appxtenderappenhancer-and-banner-8-self-service)
* [Banner 9 Self-Service Apps and Application Navigator](#banner-9-self-service-apps-and-application-navigator-1)
	* [Configuring Entra for Banner 9 Self-Service Apps and Application Navigator](#configuring-entra-for-banner-9-self-service-apps-and-application-navigator)
	* [Configuring Banner 9 Self-Service Apps and Application Navigator](#configuring-banner-9-self-service-apps-and-application-navigator)
		* [Java Keystore File](#java-keystore-file)
		* [Service Provider XML Metadata File](#service-provider-xml-metadata-file)
		* [Identity Provider XML Metadata File](#identity-provider-xml-metadata-file)
		* [Application Groovy File](#application-groovy-file)
	* [Whitelisting Application Navigator in Google Chrome](#whitelisting-application-navigator-in-google-chrome)
	* [Whitelisting Application Navigator in Mozilla Firefox](#whitelisting-application-navigator-in-mozilla-firefox)
* [Banner Admin Pages and Banner Access Management](#banner-admin-pages-and-banner-access-management)
	* [Configuring Entra for Banner Admin Pages](#configuring-entra-for-banner-admin-pages)
	* [Configuring Entra for Banner Access Management](#configuring-entra-for-banner-access-management)
	* [Configuring Tomcat's Cookie Processor Component and Session Timeout](#configuring-tomcats-cookie-processor-component-and-session-timeout)
	* [Configuring Banner Admin Pages and Banner Access Management](#configuring-banner-admin-pages-and-banner-access-management)
		* [Java Keystore File](#java-keystore-file-1)
		* [Service Provider XML Metadata File](#service-provider-xml-metadata-file-1)
		* [Identity Provider XML Metadata File](#identity-provider-xml-metadata-file-1)
		* [Deploying with ESM](#deploying-with-esm)
		* [Manual Configuration](#manual-configuration)
* [Banner 8 Self-Service](#banner-8-self-service)
	* [Configuring Entra for Banner 8 Self-Service](#configuring-entra-for-banner-8-self-service)
	* [Java Keystore File](#java-keystore-file-2)
	* [Service Provider XML Metadata File](#service-provider-xml-metadata-file-2)
	* [Identity Provider XML Metadata File](#identity-provider-xml-metadata-file-2)
	* [Configuring SSOManager](#configuring-ssomanager)
	* [Update GUACONF](#update-guaconf)
* [AppXtender/AppEnhancer (Banner Document Management)](#appxtenderappenhancer-banner-document-managment)
	* [Configuring Entra for AppXtender](#configuring-entra-for-appxtender)
	* [PKCS #12 Keystore File](#pkcs-12-keystore-file)
	* [Configuring AppXtender](#configuring-appxtender)
	* [Configuring SSO Integration with Banner and AppXtender](#configuring-sso-integration-with-banner-admin-pages-and-appxtender)

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
		* Click Edit at the top right of the first box labeled “Basic SAML Configuration”. Enter a unique name in the “Identifier (Entity ID)” field. This name will be used in your service provider XML metadata file later on and should be unique to each Banner application. 
			* Ex. `banner-stuss-test-sp`
		* Enter the ACS (Assertion Consumer Service) URL of your Banner app in the “Reply URL” field. 
			* Below is just an example. Each Banner app section will include more specific examples. Please check the respective section instead of copying this one.
			* Ex. `https://server.univ.edu:8443/StudentSelfService/saml/SSO`
			* The URL is case-sensitive.
			* Modify the URL and port to point to your server.
		* Enter a URL in the “Logout URL” field. This URL will be used to send data to the app in the event of a Single Logout (SLO) being triggered. If you don't use SLO, you may ignore this field. The URL will follow the example below. Replace `entity-id` with the Entity ID entered in the “Identifier” field. Below is just an example. Each Banner app section will include more specific examples.
			* Ex. `https://server.univ.edu:8443/StudentSelfService/saml/SingleLogout/alias/*entity-id*`
		* Click Save
	* **Box 2 - Attributes & Claims**
		* Click Edit at the top right of the “Attributes & Claims” box.
		* You can leave the “Unique User Identifier (Name ID)” claim alone unless an app requires it. None of the apps in this guide use it.
		* Delete all of the default claims under “Additional Claims”. None of these will be used in the app.
		* Click “Add new claim” at the top of the page. 
		* Enter “UDC_IDENTIFIER” in the Name field. It must be in all caps. 
			* This is the default attribute shipped by Ellucian for all apps. The name can be changed in the respective SSB app's groovy file under the “authenticationAssertionAttribute” setting. You will need this value synced to your Entra tenant. If you're unsure if the value is synced, contact your Entra administrators. This value will be used to identify the user in the Banner DB. 
		* Leave “Namespace” empty.
		* Select “Attribute” for “Source”
		* For “Source Attribute”, select the attribute which your UDCID is being synced to.
		* Leave the “Claim Conditions” and “Advanced SAML claims options” sections alone.
		* Click Save
	* **Box 3 - SAML Certificates**
		* Click on the Download link next to “Federation Metadata XML”.
			* Save and rename this file to be relevant to the app you will be deploying. This will be the identity provider XML metadata file for some of the apps. Each app will have its own identity provider XML metadata file. 
		* Click Edit at the top right of the “SAML Certificates” box.
			* Here you will find what will be referred to as your identity provider certificate. Each Entra app will have its own identity provider certificate. If you downloaded the identity provider XML metadata file, you don't need to download the certificate since it's already included in the XML file. However, if you want to manually create your own identity provider XML metadata files, you will need to download the identity provider certificate. 
				* In the list of certificates, click on the three dots of the Active certificate.
				* Click on “PEM certificate download” to download the X.509 version of the certificate. This will be used in the setup of the applications.
					* To keep track of things, I recommend renaming the file similar to your entity ID.
						* Ex. `banner-stuss-test-idp.pem`
		* For “Signing Option”, select “Sign SAML Assertion”.
		* For “Signing Algorithm”, select “SHA-256”. 
		* Under “Notification Email Addresses,” add any email addresses that should be alerted when the certificate is about to expire. You will need to update the certificate in the identity provider XML metadata file when it expires.
	* **Box 4 - Set up "Entra App Name"**
		* There's nothing to configure here. The URLs listed here will be used later on in the app configuration files. You may want to document them, or come back to them later.
	* **Box 5 - Test Single Sign-on with "Entra App Name"**
		* Ignore this step for now. If the Banner app is not configured yet, this test will not work. You can use this after the Banner app is setup.

### Setting a Session Timeout Period

You will need to create a Conditional Access policy to configure the session timeout period for Banner apps to avoid getting errors such as “Invalid username/password” or “Session Initialization Failed”. When you configure your Banner Admin and Banner Self-Services apps, you should set the variable called “maxAuthenticationAge” (in seconds) to match the timeout period you set here. For example, 1 hour = 3600 seconds. The “maxAuthenticationAge” variable should not be lower than the timeout period set in Entra or you will get errors. The current default session period for Entra is 90 days. The lowest amount of time you can set is one hour.

Please refer to Microsoft's documentation on how to [configure adaptive session lifetime policies](https://learn.microsoft.com/en-us/entra/identity/conditional-access/howto-conditional-access-session-lifetime). You should only need to set up Policy 1 (Sign-in Frequency Control). Policy 2 and 3 are optional. 

You will need to add new Entra applications to the conditional access policy each time an application is created. If you have multiple applications to create, I suggest creating the Entra applications first then adding multiple applications to the policy all at once to save time.

Tomcat must also be configured to match the session timeout. Please see the [Configuring Tomcat's Cookie Processor Component and Session Timeout](#configuring-tomcats-cookie-processor-component-and-session-timeout) section to configure Tomcat.

## Creating Keystores

A Java keystore is needed to generate a self-signed certificate for the service provider applications (Admin Pages, Self-Service apps, Banner 8 Self-Service, etc.). The commands used in this documentation are for the Linux/macOS version of keytool. However, the Windows version of keytool should be very similar. Since the arguments available in keytool can vary depending on the version installed, it is recommended to look up the documentation for that version.

The Java keystore should be stored somewhere on your server that is readable by the Linux user running your Tomcat server. The command below will generate a self-signed certificate that is valid for 9999 days. Please modify the validity period to your liking. To ensure compatibility with Entra, the keystore must use SHA256 with RSA as the signing algorithm. For compatibility with Banner apps, the keystore type must be in JKS format. In my testing, the Banner apps do not support PKCS12 keystores for the SAML configuration.

Once the keystore is created, the X.509 public certificate will need to be exported. Keep the file stored away to be used within the service provider XML metadata file later. The same keystore file (and exported public certificate) can be used for all of the applications. You do not need to create a keystore for each application. 

It is also recommended to create a keystore for each environment (DEV, TEST, PROD) just to keep things separate, but not required. Do what is best for your work environment. This documentation will be based around the TEST environment.

### Creating a Java Keystore for Service Provider Applications

1. Open a command line/terminal
2. Navigate to a directory to store the Java keystore
3. Run the following command:
	
	```
	keytool -genkey -keyalg RSA -alias banner-saml-sp
			-keystore samlkeystore.jks -validity 9999
			-keysize 2048 -storetype JKS -sigalg SHA256withRSA
	```
4. Store the Java keystore on your server in a directory that is readable by the OS user that runs the Tomcat application.
	* For this documentation, the `/u01/app/tomcat/saml` directory will be used and “tomcat” is the user that runs the Tomcat application.
5. Safely store the passwords used for the keystore and signing key. You will need these later when you configure SAML for the applications.

### Creating a PKCS #12 version of the Java Keystore

This version of the self-signed certificate will be used for Banner AppXtender. If you do not use AppXtender, feel free to ignore this step.

1. Convert the JKS file to a PFX file using the following command:

	```
	keytool -importkeystore -srckeystore samlkeystore.jks 
	 		-srcstoretype JKS -deststoretype PKCS12 
			-destkeystore samlkeystore.pfx
	```
2. You will need to enter the destination and source keystore passwords. This keystore and password will only be used in the AppXtender configuration file.

### Exporting the Service Provider Public Certificate

This is a two-step process. You will first need to export the public certificate of the signing key from the Java keystore. Then you will need to convert the public certificate to X.509 format. 

1. Export the public certificate using the following command:
	
	```
	keytool -export -alias banner-saml-sp 
		-keystore samlkeystore.jks -file banner-saml-sp.pem
	```
2. A file named `banner-saml-sp.pem` will be created.
3. Convert the PEM file to X.509 format using the following command:
	
	```
	keytool -printcert -rfc -file banner-saml-sp.pem > banner-saml-sp.crt
	````
4. A file named `banner-saml-sp.crt` will be created.
5. This will be used as your service provider certificate in your service provider XML metadata file.

## Creating a Service Provider XML Metadata File

1. Download the example service provider XML metadata file.
2. To edit this file, you will need the entity ID from “Box 1 - Basic SAML Configuration” of the Entra application setup. You will also need the service provider public certificate that was exported in the [Exporting the Service Provider Public Certificate](exporting-the-service-provider-public-certificate) section.
3. Edit the following elements in the XML file.
	* `<md:SingleSignOnService>`
		* Set the `ID` and `entityID` attribute to be the same entity ID used in the Entra application setup (box 1 of the SAML setup).
	* `<ds:X509Certificate>`
		* In both `<ds:X509Certificate>` elements, add the X.509 certificate which you exported from the Java keystore. 
		* You only need to copy the encoded section between `-----BEGIN CERTIFICATE-----` and `-----END CERTIFICATE-----`. 
	* `<md:SingleLogoutService>`
		* In both `<md:SingleLogoutService>` elements, edit the `Location` attributes to the same URL. Use the following example and modify it for the application you are configuring. Replace `entity-id` with the app's entity ID.
		* Ex. `https://server.univ.edu:8443/StudentSelfService/saml/SingleLogout/alias/*entity-id*`
		* The URL is case-sensitive.
		* Modify the URL and port to point to your server.
	* `<md:AssertionConsumerService>`
		* In both `<md:AssertionConsumerService>` elements, edit the `Location` attributes to the same URL. Use the following example and modify it for the application you are configuring.
		* Ex. `https://server.univ.edu:8443/StudentSelfService/saml/SSO`
		* The URL is case-sensitive.
		* Modify the URL and port to point to your server.
4. Save the file 
5. Banner 9 Self-Service and Application Navigator only:
	* Store the XML file in a directory on your server that is readable by your Tomcat server. 
	* Ex. `/u01/app/tomcat/saml`
	* Other apps like Banner Admin Pages, Banner Access Management, and SSOManager (Banner 8 Self-Service), will require the metadata files to be added to the WAR files.

## Creating an Identity Provider XML Metadata File

Some apps discussed in this guide allow you to use the "Federation Metadata XML” file downloaded from Entra. For those apps that cannot use the downloaded file, you must manually create an identity provider metadata file.

### Banner 9 Self-Service Apps and Application Navigator

In my testing, these apps support using the "Federation Metadata XML" file that can be downloaded from the MS Entra SAML configuration page. 

1. Go to the Entra SAML configuration page
2. In the “SAML Certificates” section, click Download next to "Federation Metadata XML”.
3. Rename the downloaded file to a name of your choosing. Preferably one without spaces so you don't have to escape any of the spaces later.
	* Ex. `banner-stuss-test-idp.xml`
4. Store the file to a location on your server that is readable by your Tomcat server. This file location will be used in the SSB App Groovy File section.
	* Ex. `/u01/app/tomcat/saml`

### Banner Admin Pages, Banner Access Management, AppXtender/AppEnhancer, and Banner 8 Self-Service

1. Download the sample identity provider XML file.
2. To edit this file, you will need the identity provider PEM certificate that was downloaded during the “Box 3 - SAML Configuration” section of the Entra application setup. You will also need the URLs from “Box 4 - Set up *Entra App Name*”.
3. Edit the following elements in the XML file.
	* `<md:EntityDescriptor>`
		* Change the “entityID” attribute to match the “Microsoft Entra Identifier” from the Entra SAML setup in “Box 4 - Set up *Entra App Name*”. This URL will start with sts.windows.net and include your Entra tenant ID.
		* Ex. `https://sts.windows.net/*entra-tenant-id*/`
	* `<ds:X509Certificate>`
		* Within the two `<ds:X509Certificate>` tags, insert the identity provider public certificate that was downloaded (PEM Certificate Download) from the "SAML Certificates" section of the Entra SAML configuration.
	* `<md:SingleLogoutService>`
		* In both `<md:SingleLogoutService>` elements, edit the `Location` attributes to the same URL. Copy the URL from the “Logout URL” field found in the Entra SAML configuration under “Box 4 - Set up *Entra App Name*”.
	* `<md:SingleSignOnService>`
		* In both `<md:SingleSignOnService>` elements, edit the `Location` attributes to the same URL. Copy the URL from the “Login URL” field found in the Entra SAML configuration under “Box 4 - Set up *Entra App Name*”.
4. Save the file and store it to a location on your server that is readable by your Tomcat server. This file location will be used in the SSB App Groovy File section.
	* Ex. `/u01/app/tomcat/saml`

## Banner 9 Self-Service Apps and Application Navigator

An sample Student Self-Service groovy file is available in the `examples` directory, but do not use the sample file for all applications. It is only for demonstration purposes. The configuration files can change between releases and each app has different configurations. However, the SAML configuration should be fairly similar in all of the apps. You must modify the groovy file delivered with the version of the app being deployed.

The following Banner 9 Self-Service apps have been tested with these instructions: Communication Management, Employee SSB, Extensibility, Faculty SSB, Finance SSB, General SSB, Student Registration SSB, and Student SSB. Application Navigator is not a SSB app, but it has a very similar configuration.

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

Please refer to the [Creating Keystores](#creating-keystores) section to learn how to create a keystore. The same keystore can be used for all of the apps in this guide. Place the Java keystore file in a location on the app server that is readable by Tomcat. For instance, I stored mine in `/u01/app/tomcat/saml` along with my XML metadata files. The directory is owned by the `tomcat` user and group.

#### Service Provider XML Metadata File

Copy the service provider XML metadata file found in the `examples` directory of this project. Modify it according to the instructions in the [Creating a Service Provider XML Metadata File](#creating-a-service-provider-xml-metadata-file) instructions in this guide. 

The file will need to be stored on the same server as your application and be readable by Tomcat. For example, I stored the file at `/u01/app/tomcat/saml` and made the `tomcat` user and group the owner of the directory and the files within it. I store the identity provider XML metadata and Java keystore files in there as well.

#### Identity Provider XML Metadata File

For the Banner 9 Self-Service apps and Application Navigator, I had luck with using the "Federation Metadata XML" file downloaded from Entra. Please see the [Banner 9 Self-Service Apps and Application Navigator](#banner-9-self-service-apps-and-application-navigator) section of [Creating an Identity Provider XML Metadata File](#creating-an-identity-provider-xml-metadata-file) for more instructions. If those files don't work for you, please try manually creating the identity provider XML metadata file using the sample file in the `examples` directory of this project.

This file should be stored in the same location as the service provider XML metadata file and Java keystore.

#### Application Groovy File
	
Each of these apps are configured using a .groovy file. If you've ever deployed a SSB 9 app, you should be familiar with these. A sample file based on Student Self-Service's config file can be found in the `examples` directory of this project.

The following settings will be broken down by the sections within the .groovy file.

##### Authentication Provider Configuration 

* `ssoEnabled`
	* Set to `true` to enable single sign-on for the application.
	* `boolean ssoEnabled = true`
* `authenticationProvider`
	* Set it to `saml` to use SAML as the SSO protocol.
	* **NOTE:** Use single quotes.
	* `authenticationProvider = 'saml'`
* authenticationAssertionAttribute
	* Set to `UDC_IDENTIFIER`
	* `authenticationAssertionAttribute = 'UDC_IDENTIFIER'`

##### CAS Configuration

* `active`
	* Set to `false`
	* Since SAML will be used, make sure CAS is disabled. Double check this setting if you were previously using CAS.
	* `active = false`	

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
	* Set this value to the amount of time (in seconds) that is configured in the conditional access policy created in the “Setting a Session Timeout Period” section.
	* This setting should not be lower than the session time configured in the conditional access policy or you will receive error messages when accessing the application.
	* So you don't have to bust out the calculator app: 3600 seconds = 1 hour
	* Ex. `grails.plugin.springsecurity.saml.maxAuthenticationAge = 3600`
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
	* This URL can be found in the “Set up *Entra App Name*” box of the Entra SAML setup as the `Microsoft Entra Identifier`. The URL will start with https://sts.windows.net and include your Entra tenant ID.
	* Ex. `grails.plugin.springsecurity.saml.metadata.defaultIdp = https://sts.windows.net/*entra-tenant-id*/`
* `grails.plugin.springsecurity.saml.metadata.sp.defaults`
	* This is an associative array of settings for the service provider.
	* `local`
		* Set to `true`. No quotes are needed for booleans.
		* Ex. `local: true`
	* `alias`
		* Set to the entity ID of your SSB application. 
		* This is the value of “Identifier (Entity ID)” in the “Basic SAML Configuration” section of the Entra SAML configuration. It is also referenced in the service provider XML metadata file.
		* Ex. `alias: 'banner-stuss-test-sp'`
	* `securityProfile`
		* Set to `metaiop`.
		* Ex. `securityProfile: 'metaiop'`
	* `signingKey`
		* Set to the alias of the certificate within the Java keystore.
		* Ex. `signingKey: 'certificate-alias'`
	* `encryptionKey`
		* Set to the alias of the certificate within the Java keystore.
		* Ex. `encryptionKey: 'certificate-alias'`
	* `tlsKey`
		* Set to the alias of the certificate within the Java keystore.
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

### Whitelisting Application Navigator in Google Chrome

Google Chrome has begun blocking third-party cookies by default to limit cross-site tracking. A third-party cookie is any cookie that does not belong to a server using the same domain as your Application Navigator server. For instance, a cookie from server2.univ.edu would be a first-party cookie if your Application Navigator server is appnav-test.univ.edu. A cookie from login.microsoftonline.com does not use the univ.edu domain, so it would be considered a third-party cookie. 

When you load a form in Application Navigator, App Nav attempts to load Admin Pages in an inline frame, which contacts login.microsoftonline.com to verify that the user is logged in to Entra. When Entra tries to store a third-party cookie from login.microsoftonline.com, Application Navigator will not finish loading the Admin Pages form due to the browser settings. This usually results in an error that states "Authentication Failed / Problem in external authentication services".

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

Additional information can be found at the following site: [https://support.google.com/chrome/a/answer/14439269](Allow or restrict third-party cookies).

### Whitelisting Application Navigator in Mozilla Firefox

Just like Google Chrome, Mozilla has decided to start blocking third-party cookies in Firefox. Firefox's Enhanced Tracking Protection default setting of "Standard" doesn't allow for Admin Pages to load because of the cookie setting as well as Entra sending the `X-Frame-Options=deny` header in the response. We can get around this by whitelisting Application Navigator's URL in Firefox's Enhanced Tracking Protection settings.

* Open Firefox's settings
* Go to `Privacy & Security`
* Click `Manage Exceptions...` 
* In the `Address of website` field, enter the base URL of your Application Navigator server.
    * Example: `https://appnav-test.univ.edu:8443`
    * Add all instances of Application Navigator that your institution uses (ie. TEST, PROD).
* Click `Add Exception`
* Click `Save Changes`
* Reload Application Navigator and try to load a form. 

If your institution uses an MDM to manage computers, Firefox can be mass-configured to whitelist Application Navigator using the [EnableTrackingProtection](https://mozilla.github.io/policy-templates/#enabletrackingprotection) setting. Please speak to your MDM administrator about deploying the exception list.



## Banner Admin Pages and Banner Access Management

Banner Admin Pages and Banner Access Management both follow very similar procedures with only slight variations. Unlike the Application Navigator and Self-Service apps, you will not be editing an app's .groovy file. These two apps are commonly deployed and configured through ESM. If you use ESM, I recommend checking out the Single Sign-On Handbook for more information. I will try to cover ESM and manual deployment the best I can. 

Please note that both apps use the WorkSpaces version of the apps (BannerAdmin.ws.war and BannerAccessMgmt.ws.war) for SSO. You will be configuring those apps and not the plain ones (BannerAdmin.war and BannerAccessMgmt.war). 

Another note is that the “Federation Metadata XML” file does not seem to work with these two apps as the identity provider XML metadata file. Instead, use the sample identity provider XML metadata file included in this project.

If you use the “bansecr” account with Banner Access Management, you will need to have this account synced or manually created in your Microsoft Entra tenant.

### Configuring Entra for Banner Admin Pages

Use the following settings for creating an app in Entra:

* Entity ID = `banner-admin-test-sp` 
	* This can be whatever you want. Just make it unique to your Entra tenant.
* Reply URL (ACS URL) = `https://server.univ.edu:8443/BannerAdmin.ws/saml/SSO`
    * The URL is case-sensitive. Make sure “SSO” is capitalized at the end.
    * Modify the URL and port to point to your server.
* Logout URL = `https://server.univ.edu:8443/BannerAdmin.ws/saml/SingleLogout`
    * This URL is case-sensitive.
    * Modify the URL and port to point to your server.
* Include the following attribute:
    * `UDC_IDENTIFIER`

### Configuring Entra for Banner Access Management

Use the following settings for creating an app in Entra:

* Entity ID = `banner-accessmgmt-test-sp` 
	* This can be whatever you want. Just make it unique to your Entra tenant.
* Reply URL (ACS URL) = `https://server.univ.edu:8443/BannerAccessMgmt.ws/saml/SSO`
    * The URL is case-sensitive. Make sure “SSO” is capitalized at the end.
    * Modify the URL and port to point to your server.
* Logout URL = `https://server.univ.edu:8443/BannerAccessMgmt.ws/saml/SingleLogout`
    * This URL is case-sensitive.
    * Modify the URL and port to point to your server.
* Include the following attribute:
    * `UDC_IDENTIFIER`

### Configuring Tomcat's Cookie Processor Component and Session Timeout

To ensure that Entra works with Admin Pages, you must configure Tomcat's cookie processor component. The SameSite attribute for the Admin Pages cookie will be set to "none", which allows a browser to send cookies for both cross-site and same site requests. 

While this setting allows the site to work in Google Chrome and Microsoft Edge, it somewhat breaks compatibility with Mozilla Firefox. Please see the [Whitelisting Application Navigator in Mozilla Firefox](#whitelisting-application-navigator-in-mozilla-firefox) section for a fix. Safari on macOS seems to be incompatible as well, but I have not been able to find a fix. So you may need to ask users to use Chrome, Edge, or Firefox instead.

Configuring this component requires editing `context.xml` and `web.xml`, which can both be found in the `conf` directory of your Tomcat install. Make sure to restart Tomcat after editing these files for the change to take affect.

More info can be found at Ellucian's knowledge base in [Article 000049779](https://elluciansupport.service-now.com/customer_center?id=kb_article_view&sysparm_article=000049779).

#### context.xml

Add the following line within the `<Context>` tags:

```xml
<CookieProcessor sameSiteCookies="none" />
```

#### web.xml

Edit the `<session-config>` element to include the `<cookie-config>` element. Since we are using `SameSite=none` for the cookie, the [Secure attribute must be set](https://hacks.mozilla.org/2020/08/changes-to-samesite-cookie-behavior/) on the cookie. Setting the Secure attribute also requires that the [server uses HTTPS](https://medium.com/@yadav-ajay/using-httponly-and-secure-cookies-on-web-servers-how-to-do-it-52ccf0eabfb4).

The `<http-only>` setting ensures that the cookie can only be used by the browser and not scripts.

Make sure the value in `<session-timeout>` matches the [session timeout period](#setting-a-session-timeout-period) set in Entra. The value should be in minutes.

```xml
<session-config>
    <session-timeout>60</session-timeout>
    <cookie-config>
		<http-only>true</http-only>
		<secure>true</secure>
    </cookie-config>
</session-config>
```

### Configuring Banner Admin Pages and Banner Access Management

#### Java Keystore File

Please refer to the [Creating Keystores](#creating-keystores) section to learn how to create a keystore. The same keystore can be used for all of the apps in this guide. Place the Java keystore file in a location on the app server that is readable by Tomcat. For instance, I stored mine in `/u01/app/tomcat/saml` along with my XML metadata files. The directory is owned by the `tomcat` user and group. This file does not need to be placed on the build server.  

#### Service Provider XML Metadata File

For Banner Admin Pages and Banner Access Management, the service provider XML metadata file creation process will be the same as the process for Banner 9 Self-Service apps and Application Navigator. Follow the [Creating a Service Provider XML Metadata File](#creating-a-service-provider-xml-metadata-file) instructions in this guide.

If you are using ESM to deploy these two apps, you will need to store this file on your build server (usually JobSub). Make sure it is readable by ESM.

Ex. `/u02/esmshare/BANNER9WAR/saml_configuration`

#### Identity Provider XML Metadata File

Banner Admin Pages and Banner Access Management cannot use the "Federation Metadata XML" file downloaded from Entra, in my testing. Instead, you must use the sample XML metadata file from the `examples` directory of this project. Please see the [Banner Admin Pages, Banner Access Management, AppXtender/AppEnhancer, and Banner 8 Self-Service](#banner-admin-pages-banner-access-management-appxtenderappenhancer-and-banner-8-self-service) section of [Creating an Identity Provider XML Metadata File](#creating-an-identity-provider-xml-metadata-file) for more instructions.

This file should be stored in the same location as the service provider XML metadata file if you are using ESM. Make sure it is readable by ESM. 

Ex. `/u02/esmshare/BANNER9WAR/saml_configuration`

#### Deploying with ESM

1. Before signing in to ESM, place the service provider metadata and identity provider metadata files somewhere on your build server. Commonly, this is on JobSub. For example, I created a `saml_configuration` directory within the `BANNER9WAR` directory.
    * Ex. `/u02/esmshare/BANNER9WAR/saml_configuration`  
    * Make sure the new directory and files are owned by the same user and group that the other directories are owned by.

        ```
		chown user:group -R saml_configuration
		```
    * Failure to do this can lead to failed builds.
2. The keystore file will need to be stored somewhere on your app server that is readable by Tomcat.  
    * Ex. `/u01/app/tomcat/saml`
3. Login to ESM and select your environment
4. Go to `Env Settings`
5. Scroll to the `Banner Administrative Pages Configuration` or `Banner Access Management Configuration` section
6. Fill in the SAML configuration information
    * `Login Authentication Method`
        * Choose `SAML SSO`
    * `Identity Service Url`
        * This field is for CAS users. You may leave this field empty since you are using SAML.
    * `SAML Configuration Files Directory (full path, Jobsub)`
        * Enter the full path to the directory where the service provider metadata, identity provider metadata, and keystore files reside. This is usually on the Jobsub server.
    * `SAML Service Provider Metadata File Name`
        * Enter the filename of the service provider metadata file. Only the filename is needed, not the full path.
    * `SAML Identity Provider Metadata File Name`
        * Enter the filename of the identity provider metadata file. Only the filename is needed, not the full path.
    * `SAML Keystore File Name (full path, App Server)`
        * Enter the full path of the keystore file on the app server. The app server is the server hosting Banner Admin Pages or Banner Access Management.
        * Ex. `/u01/app/tomcat/saml/samlkeystore.jks`
    * `SAML Keystore Password`
        * Enter the password to the SAML keystore. A second field will appear once you start entering the password. Retype the password in the second field to confirm the password.
    * `SAML Signing Key Alias`
        * Enter the alias of the certificate within the Java keystore.
    * `SAML Signing Key Password`
        * Enter the password for the signing certificate in the Java keystore. You will need to enter the password twice to confirm the password.
    * `Application Navigator URL`
        * This field is only located under the `Banner Administrative Pages Configuration` section.
        * Enter the URL to your Application Navigator install.
7. Click Save
8. Deploy Banner Admin Pages and Banner Access Management like you normally would using ESM. ESM will bundle the needed configuration and files into the WAR file.

#### Manual Configuration

> **NOTE:** <br>
> Since manual deployment may not be the same for every institution, I will only cover the broad view of what needs to be edited and configured. Adapt these instructions to your manual deployment methods.

1. The SAML configuration files for Banner Admin Pages and Banner Access Management can be found in the following locations:
    * `/$CATALINA_HOME/webapps/BannerAdmin.ws/WEB-INF/classes`
    * `/$CATALINA_HOME/webapps/BannerAccessMgmt.ws/WEB-INF/classes`
2. Place the service provider XML file, identity provider XML file, and Java keystore within the `classes` directory. 
3. Edit the `config.properties` file
    * `saml.keystore`
        * Enter the full path of the SAML keystore's location on the app server. Prepend the path with `file://`.
            * Ex. `file:///u01/app/tomcat/saml/samlkeystore.jks`
    * `saml.keystore.password`
        * Enter the password for the Java keystore.
    * `saml.logout.success.url`
        * Keep the default location.
		* Ex. `${webapp.location}/saml/logout`
    * `saml.sign.key.alias`
        * Enter the alias of the signing certificate within the Java keystore.
    * `saml.sign.key.password`
        * Enter the password of the signing certificate within the Java keystore.
    * `saml.sp.metadata.filename`
        * Enter the filename of the service provider XML file within the `classes` directory. 
		* You only need to enter the filename, not the full path.
		* Ex. `banner-admin-test-sp.xml`
    * `saml.idp.metadata.filename`
        * Enter the filename of the identity provider XML file within the `classes` directory. 
		* You only need to enter the filename, not the full path.
		* Ex. `banner-admin-test-idp.xml`
4. Save the `config.properties` file.
5. Modify `applicationContext.xml`
    * This file is located in the “WEB-INF” directory. A preconfigured version for SAML can be found in “applicationContext_saml.xml”. 
	* Since you will be using SAML, copy the contents of “applicationContext_saml.xml” to “applicationContext.xml”, or you can overwrite the file using the copy command.

		```
		cp applicationContext_saml.xml applicationContext.xml
		```
6. Save the file
7. Redeploy the application using your preferred deployment method. If you're just editing the files in the unpacked install location, reboot Tomcat and the new configuration will take effect. If you would like to modify the WAR file, see the [Configuring SSOManager](#configuring-ssomanager) instructions in the [Banner 8 Self-Service](#banner-8-self-service) section for a relevant guide.

## Banner 8 Self-Service

Banner 8 Self-Service uses the SSOManager app to facilitate single sign-on. SSOManager is part of the Banner Enterprise Identity Services (BEIS) package. Documentation for SSOManager can be found within the BEIS documentation.

Configuring SSOManager requires that the WAR file is unpacked, edited, then repacked. Doing so requires that Java (and the jar executable) is installed on the machine that you use to modify the WAR file.

### Configuring Entra for Banner 8 Self-Service

Use the following settings for creating an app in Entra:

* Entity ID = `banner-ssb8-test-sp`
	* This can be whatever you want. Just make it unique to your Entra tenant.
    * The BEIS documentation states that the entity ID must be “ssomanager”, but I've had success setting it to something different. If you have a TEST and PROD instance, you will need unique values for each anyway. 
* Reply URL (ACS URL) = `https://server.univ.edu:8445/ssomanager/saml/SSO`
    * The URL is case-sensitive. Make sure “SSO” is capitalized at the end.
    * Modify the URL and port to point to your server.
* Logout URL = `https://server.univ.edu:8445/ssomanager/saml/SingleLogout`
    * This URL is case-sensitive.
    * Modify the URL and port to point to your server.
* Include the following attribute:
    * `UDC_IDENTIFIER`

### Java Keystore File

Please refer to the [Creating Keystores](#creating-keystores) section to learn how to create a keystore. The same keystore can be used for all of the apps in this guide. The keystore for SSOManager will be packaged into the WAR file. See step 3 within the [Configuring SSOManager](#configuring-ssomanager) section. 

### Service Provider XML Metadata File

Copy the service provider XML metadata file found in the `examples` directory of this project. Modify it according to the instructions in the [Creating a Service Provider XML Metadata File](#creating-a-service-provider-xml-metadata-file) instructions in this guide. 

Store this file locally to be used in the [Configuring SSOManager](#configuring-ssomanager) section.

### Identity Provider XML Metadata File

The "Federation Metadata XML" metadata file downloaded from Entra did not work with SSOManager when I tested it. Instead, I used the sample identity provider XML metadata file found in the `examples` directory of this project. Please see the [Banner Admin Pages, Banner Access Management, AppXtender/AppEnhancer, and Banner 8 Self-Service](#banner-admin-pages-banner-access-management-appxtenderappenhancer-and-banner-8-self-service) section of [Creating an Identity Provider XML Metadata File](#creating-an-identity-provider-xml-metadata-file) for more instructions.

Store this file to be used in the [Configuring SSOManager](#configuring-ssomanager) section.

### Configuring SSOManager

1. Copy the `ssomanager.war` file to another location on a local machine or the server. 
2. Unpack the WAR file using the following command:

	```
	java xvf ssomanager.war
	```
3. Copy the Java keystore to the `ssomanager/WEB-INF/classes` directory and rename it to samlKeystore.jks (with an uppercase K)

    ```
	cp samlkeystore.jks ssomanager/WEB-INF/classes/samlKeystore.jks
	```
    * Overwrite the old file when prompted
    * **NOTE:** If you don't want to rename the file, just edit the filename in the securityContext.xml file.
4. Edit the `securityContext.xml` file
    * Find the bean with `id=”keyManager”`
        * Edit the following info surrounded in brackets. Remove the brackets.
		
            ```xml
                <bean id="keyManager" class="org.springframework.security.saml.key.JKSKeyManager">
                    <constructor-arg value="classpath:samlKeystore.jks"/>
                    <constructor-arg type="java.lang.String" value="[keystore-password]"/>
                    <constructor-arg>
                        <map>
                            <entry key="[certificate-alias]" value="[private-key-password]"/>
                        </map>
                    </constructor-arg>
                    <constructor-arg type="java.lang.String" value="[certificate-alias]"/>
                </bean>
            ```
5. Find the bean with `class=”org.springframework.security.saml.metadata.ExtendedMetadata”`
	* Edit the property with `name=”signingKey”`. 
	* Set the value attribute to the certificate alias. Remove the brackets.

		```xml
		<property name="signingKey" value="[certificate-alias]"/>
		```
6. Find the bean with `id=”webSSOprofileConsumer”`
	* Replace with the following lines and set the `maxAuthenticationAge` value (in seconds) to match your Entra session timeout period.

		```xml
		<bean id="webSSOprofileConsumer" class="org.springframework.security.saml.websso.WebSSOProfileConsumerImpl" lazy-init="true">
      		<property name="maxAuthenticationAge" value="3600" />
		</bean>
		```
7. Copy the identity provider XML metadata file to the `ssomanager/WEB-INF/classes` directory.
	* The “Federation Metadata XML” file from the Entra SAML configuration site will work here.
8. Rename the identity provider XML metadata file to `idp-metadata.xml` and overwrite the old file.

	```
	mv entra-idp-metadata.xml idp-metadata.xml
	```
9. Repack the WAR file
	* Navigate to the directory where the WAR file is stored.
	* Use the following command to repack the WAR file:

		```
		jar uvf ssomanager.war WEB-INF
		```
10. Deploy the new version of `ssomanager.war`
11. Once SSOManager is done deploying, access the SSOManager configuration website.
	* Go to the `SSB Configuration` tab
	* Set the mode to `SAML 2.0`
	* Set UDC ID Indicator to `Cookie`
	* Set UDC ID Key to `UDC_IDENTIFIER`
12. Click Save

### Update GUACONF

1. Go to Banner Admin Pages
2. Open `GUACONF`
3. Select `GLOBAL` for the Application ID
4. On the Configurations tab, set `banner8.SS.url` to:
	* `https://server.univ.edu:8445/ssomanager/saml/login?relayState=/c/auth/SSB?pkg=`
	* Modify the URL and port to point to your server.
5. On the Self-Service Parameters tab, set the following:
	* `IDMLOGINURI`
		* Set to the Login URL found in the Entra SAML configuration
			* Ex. `https://login.microsoftonline.com/<tenant-id>/saml2`
			* Set the <tenant-id> to your Entra tenant ID.
	* `IDMLOGOUTURI`
		* Set to the SSOManager logout URL
			* Ex. `https://server.univ.edu:8445/ssomanager/saml/logout`
			* This URL is case-sensitive.
			* Modify the URL and port to point to your server.
6. Click Save 

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
