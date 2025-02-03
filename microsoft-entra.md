## Microsoft Entra

Formerly called Microsoft Azure Active Directory (Azure AD), Microsoft Entra is a cloud-based identity and access management service that provides single sign-on (SSO) capabilities for organizations. MS Entra supports two SSO protocols, OpenID Connect and SAML 2.0. This guide will be using SAML 2.0 since Ellucian supports SAML and CAS on most of their apps. CAS is not supported by MS Entra.

### Creating an Entra Application

1. Login to the Microsoft Entra Admin Center
2. Go to Application > Enterprise Applications in the menu
3. Click on `New Application`
4. On the Browse Microsoft Entra Gallery page, click on `Create your own application`
5. In the popup, enter a name for your application.
6. In the `What are you looking to do with your application?` section, choose `Integrate any other application you don't find in the gallery (Non-gallery)`. This will allow you to create an application capable of using SAML.
7. Click Create
8. Add users and groups allowed to authentication
	1. Within the Enterprise Application menu, click on `Users and groups`.
	2. Click `Add user/group`
	3. On the `Add Assignment` page, click on `None selected` under `Users and groups`.
	4. Add any user and/or groups that are allowed to login to the Entra application. If users are not specified individually or in a specified group, the user will receive a message about the application being blocked after logging in. 
		* Please note that Entra does not recursively look at nested group membership. Suppose Group 1 is added to the “Users and groups” page and a user is in Group 2. If Group 2 is a nested member of Group 1, Entra will not traverse nested group memberships. Group 2 will need to be added to the `Users and groups` page as well.
	5. Click Assign
9. In the enterprise application menu, click on “Single sign-on”.
10. Click on the SAML box
11. On the “Set up Single Sign-On with SAML” page, you will see five boxes labeled with numbers. The steps below will detail each one.
	* **Box 1 - Basic SAML Configuration**
		* Click Edit at the top right of the first box labeled `Basic SAML Configuration`. Enter a unique name in the “Identifier (Entity ID)” field. This name will be used in your service provider XML metadata file later on and should be unique to each Banner application and unique to your Entra tenant. 
			* Ex. `banner-stuss-test-sp`
		* Enter the ACS (Assertion Consumer Service) URL of your Banner app in the `Reply URL` field. 
			* Below is just an example. Each Banner app section will include more specific examples. Please check the respective section instead of copying this one.
			* Ex. `https://server.univ.edu:8443/StudentSelfService/saml/SSO`
			* The URL is case-sensitive.
			* Modify the URL and port to point to your server.
		* Enter a URL in the `Logout URL` field. This URL will be used to send data to the app in the event of a Single Logout (SLO) being triggered. If you don't use SLO, you may ignore this field. The URL will follow the example below. Replace `entity-id` with the Entity ID entered in the “Identifier” field. Below is just an example. Each Banner app section will include more specific examples.
			* Ex. `https://server.univ.edu:8443/StudentSelfService/saml/SingleLogout/alias/*entity-id*`
		* Click Save
	* **Box 2 - Attributes & Claims**
		* Click Edit at the top right of the `Attributes & Claims` box.
		* You can leave the `Unique User Identifier (Name ID)` claim alone unless an app requires it. None of the apps in this guide use it.
		* Delete all of the default claims under `Additional Claims`. None of these will be used in the app.
		* Click `Add new claim` at the top of the page. 
		* Enter `UDC_IDENTIFIER` in the Name field. It must be in all caps. 
			* This is the default attribute shipped by Ellucian for all apps. The name can be changed in the respective SSB app's groovy file under the “authenticationAssertionAttribute” setting. You will need this value synced to your Entra tenant. If you're unsure if the value is synced, contact your Entra administrators. This value will be used to identify the user in the Banner DB. 
		* Leave `Namespace` empty.
		* Select `Attribute` for “Source”
		* For `Source Attribute`, select the attribute which your UDCID is being synced to.
		* Leave the `Claim Conditions` and `Advanced SAML claims options` sections alone.
		* Click Save
	* **Box 3 - SAML Certificates**
		* Click the `Download` link next to `Federation Metadata XML`.
			* Save and rename this file to be relevant to the app you will be deploying. This will be the identity provider XML metadata file for some of the apps. Each app will have its own identity provider XML metadata file. 
		* Click the `Download` link next to `Certificate (Base64)`.
			* This will be referred to as your identity provider certificate. Each Entra app will have its own identity provider certificate. If you downloaded the identity provider XML metadata file, you don't need to download the certificate since it's already included in the XML file. However, if you want to manually create your own identity provider XML metadata files, you will need to download the identity provider certificate. 
            * To keep track of things, I recommend renaming the file similar to your entity ID.
                * Ex. `banner-stuss-test-idp.cer`
		* For `Signing Option`, select `Sign SAML Assertion`.
		* For `Signing Algorithm`, select `SHA-256`. 
		* Under `Notification Email Addresses`, add any email addresses that should be alerted when the certificate is about to expire. You will need to update the certificate in the identity provider XML metadata file when it expires.
	* **Box 4 - Set up "Entra App Name"**
		* There's nothing to configure here. The URLs listed here will be used later on in the app configuration files. You may want to document them, or come back to them later.
	* **Box 5 - Test Single Sign-on with "Entra App Name"**
		* Ignore this step for now. If the Banner app is not configured yet, this test will not work. You can use this after the Banner app is setup.

### Creating a Sign-In Frequency Conditional Access Policy

A Conditional Access policy will need to be configured for the sign-in frequency in Banner apps to avoid getting errors such as “Invalid username/password” or “Session Initialization Failed”. When you configure your AppNav, Banner Admin Pages, and Banner Self-Services apps, you should set the variable called `maxAuthenticationAge` (in seconds) to match the timeout period you set here. For example, 1 hour = 3600 seconds. The `maxAuthenticationAge` variable should not be higher or lower than the timeout period set in Entra or you will get the aforementioned errors. The current default session period for Entra is 90 days. The lowest amount of time you can set is one hour. For the sake of this documentation, I will use a 12-hour sign-in frequency. By setting it to 12 hours, users should only have to login once a work day on a device and not be over burdened with multi-factor authentication. 

Please note that this value will be separate from the Tomcat session timeout period and the Banner application session timeout period. The SAML token lifetime and application session timeout period are different and ideally, the SAML token lifetime should last longer than the application session timeout period. By having a longer SAML token lifetime, the application session can expire and free up memory on the server. When a user needs to access the application after the session timeout cookie expires but while the SAML token is still valid, the user will be automatically logged in using SSO.

Please refer to Microsoft's documentation on how to [configure adaptive session lifetime policies](https://learn.microsoft.com/en-us/entra/identity/conditional-access/howto-conditional-access-session-lifetime). You should only need to set up Policy 1 (Sign-in Frequency Control). Policy 2 and 3 are optional. 

You will need to add new Entra applications to the conditional access policy each time an application is created. If you have multiple applications to create, I suggest creating the Entra applications first then adding multiple applications to the policy all at once to save time.