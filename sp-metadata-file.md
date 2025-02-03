## Creating a Service Provider XML Metadata File

1. Download the [sample service provider XML metadata file](/examples/banner-saml-sp.xml) from this project's [examples](/examples/) directory.
2. To edit this file, you will need the entity ID from “Box 1 - Basic SAML Configuration” of the Entra application setup. You will also need the service provider public certificate that was exported in the [Exporting the Service Provider Public Certificate](keystores.md#exporting-the-service-provider-public-certificate) section.
3. Edit the following elements in the XML file.
    * `<md:EntityDescriptor>`
        * Set the `ID` and `entityID` attribute to be the same entity ID used in the Entra application setup (box 1 of the SAML setup).
    * `<ds:X509Certificate>`
        * In both `<ds:X509Certificate>` elements, add the X.509 certificate which you exported from the Java keystore. 
        * It should **not** contain the `-----BEGIN CERTIFICATE-----` and `-----END CERTIFICATE-----` lines. You only need the Base64 encoded part of the file.
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