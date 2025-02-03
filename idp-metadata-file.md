## Creating an Identity Provider XML Metadata File

Some apps discussed in this guide allow you to use the "Federation Metadata XML” file downloaded from Entra. For those apps that cannot use the downloaded file, you must manually create an identity provider metadata file.

### Banner 9 Self-Service Apps and Application Navigator IDP XML Metadata File

In my testing, these apps support using the "Federation Metadata XML" file that can be downloaded from the MS Entra SAML configuration page. 

1. Go to the Entra SAML configuration page
2. In the “SAML Certificates” section, click Download next to "Federation Metadata XML”.
3. Rename the downloaded file to a name of your choosing. Preferably one without spaces so you don't have to escape any of the spaces later.
    * Ex. `banner-stuss-test-idp.xml`
4. Store the file to a location on your server that is readable by your Tomcat server. This file location will be used in the SSB App Groovy File section.
    * Ex. `/u01/app/tomcat/saml`

### Banner Admin Pages, Banner Access Management, AppXtender/AppEnhancer, and Banner 8 Self-Service IDP XML Metadata File

1. Download the [sample identity provider XML metadata file](/examples/banner-saml-idp.xml) from this project's [examples](/examples/) directory.
2. To edit this file, you will need the identity provider PEM certificate that was downloaded during the “Box 3 - SAML Configuration” section of the Entra application setup. You will also need the URLs from “Box 4 - Set up *Entra App Name*”.
3. Edit the following elements in the XML file.
    * `<md:EntityDescriptor>`
        * Change the “entityID” attribute to match the “Microsoft Entra Identifier” from the Entra SAML setup in “Box 4 - Set up *Entra App Name*”. This URL will start with sts.windows.net and include your Entra tenant ID.
        * Ex. `https://sts.windows.net/*entra-tenant-id*/`
    * `<ds:X509Certificate>`
        * Within the two `<ds:X509Certificate>` tags, insert the identity provider public certificate that was downloaded in `Box 3 - SAML Certificates` of [Creating an Entra Application](microsoft-entra.md#creating-an-entra-application).
        * It should **not** contain the `-----BEGIN CERTIFICATE-----` and `-----END CERTIFICATE-----` lines. You only need the Base64 encoded part of the file.
    * `<md:SingleLogoutService>`
        * In both `<md:SingleLogoutService>` elements, edit the `Location` attributes to the same URL. Copy the URL from the “Logout URL” field found in the Entra SAML configuration under “Box 4 - Set up *Entra App Name*”.
    * `<md:SingleSignOnService>`
        * In both `<md:SingleSignOnService>` elements, edit the `Location` attributes to the same URL. Copy the URL from the “Login URL” field found in the Entra SAML configuration under “Box 4 - Set up *Entra App Name*”.
4. Save the file and store it to a location on your server that is readable by your Tomcat server. This file location will be used in the SSB App Groovy File section.
    * Ex. `/u01/app/tomcat/saml`