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

Please refer to the [Creating Keystores](keystores.md#creating-keystores) section to learn how to create a keystore. The same keystore can be used for all of the apps in this guide. The keystore for SSOManager will be packaged into the WAR file. See step 3 within the [Configuring SSOManager](#configuring-ssomanager) section. 

### Service Provider XML Metadata File

Copy the service provider XML metadata file found in the `examples` directory of this project. Modify it according to the instructions in the [Creating a Service Provider XML Metadata File](sp-metadata-file.md#creating-a-service-provider-xml-metadata-file) instructions in this guide. 

Store this file locally to be used in the [Configuring SSOManager](#configuring-ssomanager) section.

### Identity Provider XML Metadata File

The "Federation Metadata XML" metadata file downloaded from Entra did not work with SSOManager when I tested it. Instead, I used the sample identity provider XML metadata file found in the `examples` directory of this project. Please see the [Banner Admin Pages, Banner Access Management, AppXtender/AppEnhancer, and Banner 8 Self-Service IDP XML Metadata File](idp-metadata-file.md#banner-admin-pages-banner-access-management-appxtenderappenhancer-and-banner-8-self-service-idp-xml-metadata-file) section of [Creating an Identity Provider XML Metadata File](idp-metadata-file.md#creating-an-identity-provider-xml-metadata-file) for more instructions.

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
4. Edit the `securityContext.xml` file.
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
5. In `securityContext.xml`, find the bean with `class=”org.springframework.security.saml.metadata.ExtendedMetadata”`.
    * Edit the property with `name=”signingKey”`. 
    * Set the value attribute to the certificate alias. Remove the brackets.

        ```xml
        <property name="signingKey" value="[certificate-alias]"/>
        ```
6. In `securityContext.xml`, find the bean with `id=”webSSOprofileConsumer”`.
    * This will set the SAML token lifetime.
    * Replace with the following lines and set the `maxAuthenticationAge` value (in seconds) to match your [Entra sign-in frequency policy](microsoft-entra.md#creating-a-sign-in-frequency-conditional-access-policy).

        ```xml
        <bean id="webSSOprofileConsumer" class="org.springframework.security.saml.websso.WebSSOProfileConsumerImpl" lazy-init="true">
            <property name="maxAuthenticationAge" value="43200" />
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
11. Once SSOManager is deployed, access the SSOManager configuration website.
    * Go to the `SSB Configuration` tab
    * Set the mode to `SAML 2.0`
    * Set UDC ID Indicator to `Cookie`
    * Set UDC ID Key to `UDC_IDENTIFIER`
12. Click Save

### Configuring the Tomcat Session Timeout for SSB8

Self-Service Banner 8's session timeout is configured using Tomcat's own `web.xml` file unlike Application Navigator and Banner 9 SSB apps, which use a value set in GUACONF. 

1. Edit `conf/web.xml` within the Tomcat install directory
2. Find the `<session-config>` element
3. Edit the `<session-timeout>` value to an integer in minutes

    ```xml
    <session-config>
        <session-timeout>240</session-timeout>
    </session-config>
    ```

4. Restart the Tomcat server once you are doing editing for the setting to apply.

### Update GUACONF

1. Login to Banner Admin Pages
2. Open `GUACONF`
3. Select `GLOBAL` for the Application ID
4. On the Configurations tab, set `banner8.SS.url` to:
    * `https://server.univ.edu:8445/ssomanager/saml/login?relayState=/c/auth/SSB?pkg=`
    * Modify the URL and port to point to your server.
5. On the Self-Service Parameters tab, set the following:
    * `IDMLOGINURI`
        * Set to the Login URL found in the Entra SAML configuration
            * Ex. `https://login.microsoftonline.com/<tenant-id>/saml2`
                * Set the `<tenant-id>` to your Entra tenant ID.
    * `IDMLOGOUTURI`
        * Set to the SSOManager logout URL
            * Ex. `https://server.univ.edu:8445/ssomanager/saml/logout`
            * This URL is case-sensitive.
            * Modify the URL and port to point to your server.
6. Click Save 