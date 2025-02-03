## Banner Admin Pages and Banner Access Management

Banner Admin Pages and Banner Access Management both follow very similar procedures with only slight variations. Unlike the Application Navigator and Self-Service apps, you will not be editing an app's .groovy file. These two apps are commonly deployed and configured through ESM. If you use ESM, I recommend checking out the Single Sign-On Handbook for more information. I previously had information regarding the manual configuration of these apps to use SAML, but I wasn't confident in the information shared so it was removed. While it should be possible to manually configure the applications, I recommend using ESM. 

Please note that both apps use the WorkSpaces version of the apps (BannerAdmin.ws.war and BannerAccessMgmt.ws.war) for SSO. You will be configuring those apps and not the plain ones (BannerAdmin.war and BannerAccessMgmt.war). 

Another note is that the “Federation Metadata XML” file downloaded from Entra does not seem to work with these two apps as the identity provider XML metadata file. Instead, use the sample identity provider XML metadata file included in this project.

If you use the “bansecr” account with Banner Access Management, you will need to have this account synced or manually created in your Microsoft Entra tenant.

### Task List for Admin Pages and Access Management

- [ ] [Create an app in Entra](microsoft-entra.md#creating-an-entra-application)
    - [ ] [Configure Entra application for Banner Admin Pages](#configuring-entra-for-banner-admin-pages)
    - [ ] [Configure Entra application for Banner Access Management](#configuring-entra-for-banner-access-management)
- [ ] [Configure a Sign-in Frequency Conditional Access Policy](microsoft-entra.md#creating-a-sign-in-frequency-conditional-access-policy)
- [ ] [Configure Tomcat's Cookie Settings](#configuring-tomcats-cookie-settings)
- [ ] [Locate or create a Java Keystore](keystores.md#creating-a-java-keystore-for-service-provider-applications)
- [ ] [Create a Service Provider XML File](sp-metadata-file.md#creating-a-service-provider-xml-metadata-file)
- [ ] [Create an Identity Provider XML File](idp-metadata-file.md#banner-admin-pages-banner-access-management-appxtenderappenhancer-and-banner-8-self-service-idp-xml-metadata-file)
- [ ] [Configure ESM and deploy application](#deploying-with-esm)
- [ ] [Configure the application session timeout](#configuring-the-admin-pages-and-access-management-session-timeout)
- [ ] [Configure the application SAML token lifetime](#configuring-the-admin-pages-and-access-management-saml-token-lifetime)

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

### Configuring Tomcat's Cookie Settings

When Application Navigator opens a form in Admin Pages, it loads the Admin Pages site within an inline frame. This is considered a cross-site request if Admin Pages is installed on a different server than Application Navigator. Just like any other SSO site, Admin Pages must go through the SSO flow to ensure the user is authenticated. This is mostly an invisible process since you are already logged in to Entra by logging in to Application Navigator. Duing this process, the browser stores a cookie from login.microsoftonline.com, which is considered a third-party cookie since it does not originate from the Application Navigator server. 

Recent versions of browsers have become stricter about cross-site requests and third-party cookies due to privacy concerns. By default, [browsers will assume the SameSite attribute of Lax on cookies](https://hacks.mozilla.org/2020/08/changes-to-samesite-cookie-behavior/), which restricts usage of inline frames and third-party cookies. To get around this, we must configure the Admin Pages Tomcat server to send cookies with the SameSite attribute set to None, which allows for both cross-site and same site requests. 

To ensure that Admin Pages and Entra loads properly within Application Navigator, you must configure [Tomcat's Cookie Processor Component](https://tomcat.apache.org/tomcat-9.0-doc/config/cookie-processor.html). This only needs to be configured on the Tomcat server hosting Banner Admin Pages.

While this setting allows the site to work in Google Chrome and Microsoft Edge, it breaks compatibility with Mozilla Firefox. However, this may change in the future. 

Configuring this component requires editing `context.xml` and `web.xml`, which can both be found in the `conf` directory of your Tomcat install. Make sure to restart Tomcat after editing these files for the change to take affect.

More info can be found at Ellucian's knowledge base in [Article 000049779](https://elluciansupport.service-now.com/customer_center?id=kb_article_view&sysparm_article=000049779).

#### context.xml

Add the following line within the `<Context>` tags:

```xml
<CookieProcessor sameSiteCookies="none" />
```

#### web.xml

You can leave the `<session-timeout>` variable set to its default value of 30 (minutes). Since BannerAdmin.ws and BannerAccessMgmt.ws contain their own `web.xml` file in the application's `WEB-INF` directory, Tomcat will use the settings in that `web.xml` instead.

Edit the `<session-config>` element to include the `<cookie-config>` element. Since we are using `SameSite=none` for the cookie, the [Secure attribute must be set](https://hacks.mozilla.org/2020/08/changes-to-samesite-cookie-behavior/) on the cookie. Setting the Secure attribute also requires that the [server uses HTTPS](https://medium.com/@yadav-ajay/using-httponly-and-secure-cookies-on-web-servers-how-to-do-it-52ccf0eabfb4).

The `<http-only>` setting ensures that the cookie can only be used by the browser and not scripts.

```xml
<session-config>
    <session-timeout>30</session-timeout>
    <cookie-config>
        <http-only>true</http-only>
        <secure>true</secure>
    </cookie-config>
</session-config>
```

### Configuring Banner Admin Pages and Banner Access Management

#### Java Keystore File

Please refer to the [Creating Keystores](keystores.md#creating-keystores) section to learn how to create a keystore. The same keystore can be used for all of the apps in this guide. Place the Java keystore file in a location on the app server that is readable by Tomcat. For instance, I stored mine in `/u01/app/tomcat/saml` along with my XML metadata files. The directory is owned by the `tomcat` user and group. This file does not need to be placed on the build server.  

#### Service Provider XML Metadata File

For Banner Admin Pages and Banner Access Management, the service provider XML metadata file creation process will be the same as the process for Banner 9 Self-Service apps and Application Navigator. Follow the [Creating a Service Provider XML Metadata File](sp-metadata-file.md#creating-a-service-provider-xml-metadata-file) instructions in this guide.

If you are using ESM to deploy these two apps, you will need to store this file on your build server (usually JobSub). Make sure it is readable by ESM.

Ex. `/u02/esmshare/BANNER9WAR/saml_configuration`

#### Identity Provider XML Metadata File

Banner Admin Pages and Banner Access Management cannot use the "Federation Metadata XML" file downloaded from Entra, in my testing. Instead, you must use the sample XML metadata file from the `examples` directory of this project. Please see the [Banner Admin Pages, Banner Access Management, AppXtender/AppEnhancer, and Banner 8 Self-Service IDP XML Metadata File](idp-metadata-file.md#banner-admin-pages-banner-access-management-appxtenderappenhancer-and-banner-8-self-service-idp-xml-metadata-file) section of [Creating an Identity Provider XML Metadata File](idp-metadata-file.md#creating-an-identity-provider-xml-metadata-file) for more instructions.

This file should be stored in the same location as the service provider XML metadata file if you are using ESM. Make sure it is readable by ESM. 

Ex. `/u02/esmshare/BANNER9WAR/saml_configuration`

#### Deploying with ESM

Please note that anytime you make changes to the SAML settings in ESM, you will need to redeploy the application for it to apply the changes. You will then need to reconfigure the [application session timeout](#configuring-the-admin-pages-and-access-management-session-timeout) and [SAML token lifetime](#configuring-the-admin-pages-and-access-management-saml-token-lifetime).

1. Before signing in to ESM, place the service provider metadata and identity provider metadata files somewhere on your build server. This is commonly on JobSub. For example, I created a `saml_configuration` directory within the `BANNER9WAR` directory.
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
        * Choose `SAML SSO` from the dropdown menu.
    * `Identity Service Url`
        * This field is only for CAS users. Leave this field empty since Entra will be using SAML.
    * `SAML Configuration Files Directory (full path, Jobsub)`
        * Enter the full path to the directory where the service provider metadata, identity provider metadata, and keystore files reside. This is usually on the Jobsub server.
        * Ex. `/u02/esmshare/BANNER9WAR/saml_configuration`
    * `SAML Service Provider Metadata File Name`
        * Enter the filename of the service provider metadata file. Only the filename is needed, not the full path.
        * Ex. `banner-saml-sp.xml`
    * `SAML Identity Provider Metadata File Name`
        * Enter the filename of the identity provider metadata file. Only the filename is needed, not the full path.
        * Ex. `banner-saml-idp.xml`
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
        * Ex. `https://server.univ.edu:8443/applicationNavigator`
7. Click Save
8. Deploy Banner Admin Pages and Banner Access Management using ESM. ESM will bundle the needed configuration and files into the WAR file.

#### Configuring the Admin Pages and Access Management Session Timeout

Admin Pages and Access Management must be configured to have a application session timeout that is a little bit higher than Application Navigator's session timeout. Setting it a little bit higher will account for the logout timer that Application Navigator shows after a user has been idle for the amount of time set in Application Navigator's `seamless.sessiontimeout` setting in GUACONF.

A good way to figure out how long you should set the Tomcat session timeout is to use the following formula. More info on the `seamless.*` settings can be found in the [Application Navigator documentation](https://resources.elluciancloud.com/bundle/banner_appnav_acn_use/page/r_configuration.html). For example, in my environment I use 260 minutes for the session timeout since I have Application Navigator set to have a 4-hour idle session timeout and a 5 minute session timeout notification. (240 minutes + 5 minutes + 15 minutes)

```
session-timeout = seamless.sessionTimeout + seamless.sessionTimeoutNotification + 15 minutes
```

##### web.xml

The `web.xml` file can be found can be found in the `WEB-INF` directory of the deployed BannerAdmin.ws and BannerAccessMgmt.ws directories. I have not found a way to edit this file before deployment with ESM so what I normally do is allow the WAR file to deploy, stop Tomcat, edit the `web.xml` file, then restart Tomcat. Please note that this setting only determines the session timeout for the Tomcat session, not the SAML token lifetime. 

1. Open `BannerAdmin.ws/WEB-INF/web.xml` or `BannerAccessMgmt.ws/WEB-INF/web.xml`
2. Find the `<session-config>` element
3. Edit the `<session-timeout>` element

    ```xml
    <session-config>
        <session-timeout>260</session-timeout>
    </session-config>
    ```
4. Restart Tomcat for the changes to take effect

#### Configuring the Admin Pages and Access Management SAML Token Lifetime

To prevent mismatched SAML token ages amongst apps, you must configure the `maxAuthenticationAge` property of Banner Admin Pages and Banner Access Management. This should be the same value used for Application Navigator's `maxAuthenticationAge` property within the SAML config. Just like the web.xml

1. Open `BannerAdmin.ws/WEB-INF/applicationContext.xml` or `BannerAccessMgmt.ws/WEB-INF/applicationContext.xml`
2. Find the bean with `id="webSSOprofileConsumer"`
    * Replace the `webSSOprofileConsumer` line with the following:

        ```xml
            <bean id="webSSOprofileConsumer" class="org.springframework.security.saml.websso.WebSSOProfileConsumerImpl" lazy-init="true">
                <property name="maxAuthenticationAge" value="43200"/> 
            </bean>
        ```
3. Make sure to set the value of the `maxAuthenticationAge` property to the time period set in your [Entra sign-in frequency policy](microsoft-entra.md#creating-a-sign-in-frequency-conditional-access-policy). The value is in seconds (43200 seconds = 12 hours). 
4. Restart Tomcat for the changes to take effect

#### Testing the Admin Pages' SSO Without Application Navigator

If you would like to test Admin Pages on its own or if you don't have Application Navigator setup for Entra yet, there is a simple way to access a form. It requires that you have another Entra-configured app that you can login to first. I recommend using an incognito/private window so your browser starts with a clean slate and has no previously stored cookies. Please note that this URL is using the `BannerAdmin` app and not the `BannerAdmin.ws` app. This URL should load `GUACONF` once you are done with the SSO flow.

1. Open an incognito/private window.
2. Login to Admin Pages using the /saml/login/initialize page.
    
    Ex. `https://server.univ.edu:8443/BannerAdmin.ws/saml/login/initialize`
3. After logging in, visit your Admin Pages using the following URL format. Edit the URL to point to your server.

    Ex. `https://server.univ.edu:8443/BannerAdmin/?form=GUACONF`
4. If successful, you should see the GUACONF form load.