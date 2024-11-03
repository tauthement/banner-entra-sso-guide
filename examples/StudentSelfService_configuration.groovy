/** ****************************************************************************
         Copyright 2013-2024 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

/******************************************************************************
This file contains configuration needed by the Banner XE Student web
application. Please refer to the Installation guide for additional information
regarding the configuration items contained within this file.

This configuration file contains the following sections:

    * Self Service Support
    * CAS SSO Configuration (supporting administrative and self service users)

     NOTE: DataSource and JNDI configuration resides in the cross-module
           'banner_configuration.groovy' file.

***************************************************************************** **/

// ******************************************************************************
//                       +++ Self Service Support +++
// ******************************************************************************

ssbEnabled = true
ssbOracleUsersProxied = true
guestAuthenticationEnabled = false //Set to true if enabling Proxy Access.

/** *********************************************************************************
  Set 'isExperienceIntegrated' to true for accessing the SSB application only in
  Experience. Set to false to access the SSB application in standalone mode.
  Default value is 'false'
************************************************************************************ */
isExperienceIntegrated = false

/** *****************************************************************************
 *                                                                              *
 *                        OAuth2 configuration                               *
 *                                                                              *
 ***************************************************************************** **/
banner.oauth2.issuerJwksURi= "https://oauth.prod.10005.elluciancloud.com/jwks"
banner.oauth2.issuer = "https://oauth.prod.10005.elluciancloud.com"
banner.oauth2.audiance="https://elluciancloud.com"


/** *****************************************************************************
 *                                                                              *
 *                AUTHENTICATION PROVIDER CONFIGURATION                         *
 *                                                                              *
 ***************************************************************************** **/
//
// Set authenticationProvider to either default, cas or saml.
// If using cas or saml, Either the CAS CONFIGURATION or the SAML CONFIGURATION
// will also need configured/uncommented as well as set to active.
//
boolean ssoEnabled = true
banner {
    sso{
       authenticationProvider = 'saml'
       authenticationAssertionAttribute = 'UDC_IDENTIFIER'
       if(authenticationProvider == 'cas' || authenticationProvider == 'saml'){
       ssoEnabled = true
       }
    }
}
if(ssoEnabled)
{
   grails.plugin.springsecurity.failureHandler.defaultFailureUrl = '/login/error'
}

/** *****************************************************************************
 *                                                                              *
 *                             CAS CONFIGURATION                                *
 *                                                                              *
 ***************************************************************************** **/
// set active = true when authentication provider section configured for cas
grails {
    plugin {
        springsecurity {
            cas {
                active = false
                serverUrlPrefix  = 'https://idp.univ.edu:443/cas'
                serviceUrl       = 'https://server.univ.edu/StudentSelfService/login/cas'
                serverName       = 'https://server.univ.edu'
                proxyCallbackUrl = 'https://server.univ.edu/StudentSelfService/secure/receptor'
                loginUri         = '/login'
                sendRenew        = false
                proxyReceptorUrl = '/secure/receptor'
                useSingleSignout = false
                key = 'grails-spring-security-cas'
                artifactParameter = 'SAMLart'
                serviceParameter = 'TARGET'
                serverUrlEncoding = 'UTF-8'
                filterProcessesUrl = '/login/cas'
                if (useSingleSignout) {
                    grails.plugin.springsecurity.useSessionFixationPrevention = false
                }
            }
            logout {
                afterLogoutUrl = 'https://idp.univ.edu:443/cas/logout?service=https://server.univ.edu'
            }
        }
    }
}

grails.plugin.springsecurity.logout.mepErrorLogoutUrl='/logout/logoutPage'

/** *****************************************************************************
 *                                                                              *
 *           Home Page link when error happens during authentication.           *
 *                                                                              *
 ***************************************************************************** **/
grails.plugin.springsecurity.homePageUrl="https://server.univ.edu:8443/StudentSelfService/"


/** *****************************************************************************
 *                                                                              *
 *                 Eliminate access to the WEB-INF folder                       *
 *                                                                              *
 ***************************************************************************** **/
grails.resources.adhoc.includes = ['/images/**', '/css/**', '/js/**', '/plugins/**']
grails.resources.adhoc.excludes = ['/WEB-INF/**']


/** *****************************************************************************
 *                                                                              *
 * The errors reported by YUI in each of these files are because YUI            *
 * compressor/minifier does not support ES5 – the version of JavaScript         *
 * incorporated in browsers since IE9.   Specifically, ES5 allows use of JS     *
 * reserved words as property names with the ‘.NAME’ syntax(e.g., “object.case”)*
 * This results in a syntax error in YUI minifier, but is legal ES5 syntax.     *
 *                                                                              *
 ***************************************************************************** **/
grails.resources.mappers.yuijsminify.excludes = ['**/*.min.js','**/angularjs-color-picker.js', '**/m.js', '**/bundle-aurora_defer.js']


/************************************************************
    Extensibility extensions & i18n file location
************************************************************/

webAppExtensibility {
    locations {
       extensions = ""
       resources = ""
    }
    adminRoles = "ROLE_SELFSERVICE-WTAILORADMIN_BAN_DEFAULT_M"
}


/** *****************************************************************************
 *                                                                              *
 *                        SAML CONFIGURATION                                    *
 *        Un-comment the below code when authentication mode is saml.           *
 *                                                                              *
 ***************************************************************************** **/

// set active = true when authentication provider section configured for saml
grails.plugin.springsecurity.saml.active = true
grails.plugin.springsecurity.auth.loginFormUrl = '/saml/login'
grails.plugin.springsecurity.saml.afterLogoutUrl ='/logout/customLogout'

banner.sso.authentication.saml.localLogout='false' // To disable single logout set this to true,default 'false'.

grails.plugin.springsecurity.saml.keyManager.storeFile = 'file:/u01/app/tomcat/saml/samlkeystore-test.jks'  // for unix File based Example:- 'file:/home/u02/samlkeystore.jks'
grails.plugin.springsecurity.saml.keyManager.storePass = 'keystore-password'
grails.plugin.springsecurity.saml.keyManager.passwords = [ 'certificate-alias': 'signing-key-password' ]  // banner-<short-appName>-sp is the value set in Ellucian Ethos Identity Service provider setup
grails.plugin.springsecurity.saml.keyManager.defaultKey = 'certificate-alias'                 // banner-<short-appName>-sp is the value set in Ellucian Ethos Identity Service provider setup
grails.plugin.springsecurity.saml.maxAuthenticationAge = 43200   //value in seconds

grails.plugin.springsecurity.saml.metadata.sp.file = '/u01/app/tomcat/saml/banner-stuss-test-sp.xml'     // for unix file based Example:-'/home/u02/sp-local.xml'
grails.plugin.springsecurity.saml.metadata.providers = [eis: '/u01/app/tomcat/saml/banner-stuss-test-idp.xml'] // for unix file based Example: '/home/u02/idp-local.xml'
grails.plugin.springsecurity.saml.metadata.defaultIdp = 'https://sts.windows.net/<entra-tenant-id>/' //Same value as configured in the IDP xml
grails.plugin.springsecurity.saml.metadata.sp.defaults = [
        local: true,
        alias: 'ssb-app-entityid',                                   // banner-<short-appName>-sp is the value set in EIS Service provider setup
        securityProfile: 'metaiop',
        signingKey: 'certificate-alias',                              // banner-<short-appName>-sp is the value set in EIS Service provider setup
        encryptionKey: 'certificate-alias',                           // banner-<short-appName>-sp is the value set in EIS Service provider setup
        tlsKey: 'certificate-alias',                                  // banner-<short-appName>-sp is the value set in EIS Service provider setup
        requireArtifactResolveSigned: false,
        requireLogoutRequestSigned: false,
        requireLogoutResponseSigned: false
]


/** **********************************************************************************
 *                                                                                   *
 *   Cross-frame scripting vulnerability when integrating with Application Navigator.*
 *   This setting is needed if the application needs to work inside                  *
 *   Application Navigator and the secured application pages will be accessible      *
 *   as part of the single-sign on solution.                                         *
 *                                                                                   *
 ********************************************************************************* **/
grails.plugin.xframeoptions.urlPattern = '/login/auth'
grails.plugin.xframeoptions.deny = true

/*******************************************************************************
 *                                                                              *
 *                      ConfigJob (Platform 9.39)                               *
 *                                                                              *
 ***************************************************************************** **/
/* Set feature.enableConfigJob to true for configJob to run as configured and
set feature.enableConfigJob to false for configJob to NOT run as configured */

feature.enableConfigJob = true

/* Set feature.enableApplicationPageRoleJob to true for applicationPageRoleJob to run as configured and
set feature.enableApplicationPageRoleJob to false for applicationPageRoleJob to NOT run as configured */

feature.enableApplicationPageRoleJob = true

/** ********************************************************************************
 *                                                                                 *
 *                   SS Config Dynamic Loading Job Properties                      *
 *                                                                                 *
 *                   Cron Expressions:                                             *
 *                                                                                 *
 *                   ┌───────────── second (0-59)                                  *
 *                   │ ┌───────────── minute (0 - 59)                              *
 *                   │ │ ┌───────────── hour (0 - 23)                              *
 *                   │ │ │ ┌───────────── day of the month (1 - 31)                *
 *                   │ │ │ │ ┌───────────── month (1 - 12) (or JAN-DEC)            *
 *                   │ │ │ │ │ ┌───────────── day of the week (0 - 7)              *
 *                   │ │ │ │ │ │          (or MON-SUN -- 0 or 7 is Sunday)         *
 *                   │ │ │ │ │ │                                                   *
 *                   * * * * * *                                                   *
 *                                                                                 *
 ******************************************************************************* **/
/*ConfigJob - the job scheduled to update the configuration properties from DB
ApplicationPageRoleJob - the job scheduled to update the interceptedUrlMap from DB. */

configJob {
    // Recommended default is every 1 hour starting at 00am, of every day - "0 0 */1 * * ?"
    // Cron expression lesser than 30 mins will fall back to 30 mins.
    cronExpression = "0 0 */1 * * ?"
}
applicationPageRoleJob {
    // Recommended default is once at 00:00:00am every day - "0 0 0 * * ?"
    // Cron expression lesser than 30 mins will fall back to 30 mins.
    cronExpression = "0 0 0 * * ?"
}

/********************************************************************************
*                                                                               *
*                           Target Server                                       *
********************************************************************************/
/** *****************************************************************************
 *                                                                              *
 *                Application Server Configuration                              *
 * When deployed to Tomcat, targetServer="tomcat"                               *
 * When deployed to WebLogic, targetServer="weblogic"                           *
 *                                                                              *
 ***************************************************************************** **/
targetServer="tomcat"

/**************************************************************************************
* List of allowed domains configuration for Ellucian Experience                       *
* Do not change this configuration unless instructed.                                 *
* Do not move this configuration to Banner Applications Configurations (GUACONF) page.*
***************************************************************************************/
allowedExperienceDomains=[
"https://experience-test.elluciancloud.com",
"https://experience.elluciancloud.com",
"https://experience-test.elluciancloud.ca",
"https://experience.elluciancloud.ca",
"https://experience-test.elluciancloud.ie",
"https://experience.elluciancloud.ie",
"https://experience-test.elluciancloud.com.au",
"https://experience.elluciancloud.com.au"]

/** *****************************************************************************
 *                                                                              *
 *                 Text Manager Configuration                                   *
 *                                                                              *
 ***************************************************************************** **/
/*
Below configurations are required for an application in order to enable Text Manager Translations

    *  enableTextManagerTranslations
        To Enable Text Manager translations, set to false if its not required for an application.
        setting it to false completely disables the translations from Text Manager in both MEP and Non-MEP environment

    *  enableTextManagerTranslationsInMEP
        To Enable Text Manager translations in MEP environment for an application.
        set to true if the TextManager tables are MEPed and Translations are required as per institution.
*/
enableTextManagerTranslations = true
enableTextManagerTranslationsInMEP = false
