# Integrating Microsoft Entra with Ellucian Banner Apps

**Author:** Tim Authement, Nicholls State University <br>
**Source:** [https://github.com/tauthement/banner-entra-sso-guide](https://github.com/tauthement/banner-entra-sso-guide)

## Table of Contents

* [Introduction](#introduction)
* [Microsoft Entra](microsoft-entra.md#microsoft-entra)
	* [Creating an Entra Application](microsoft-entra.md#creating-an-entra-application)
	* [Creating a Sign-In Frequency Conditional Access Policy](microsoft-entra.md#creating-a-sign-in-frequency-conditional-access-policy)
* [Creating Keystores](keystores.md#creating-keystores)
	* [Creating a Java Keystore for Service Provider Applications](keystores.md#creating-a-java-keystore-for-service-provider-applications)
	* [Creating a PKCS #12 version of the Java Keystore](keystores.md#creating-a-pkcs-12-version-of-the-java-keystore)
	* [Exporting the Service Provider Public Certificate](keystores.md#exporting-the-service-provider-public-certificate)
* [Creating a Service Provider XML Metadata File](sp-metadata-file.md#creating-a-service-provider-xml-metadata-file)
* [Creating an Identity Provider XML Metadata File](idp-metadata-file.md#creating-an-identity-provider-xml-metadata-file)
	* [Banner 9 Self-Service Apps and Application Navigator IDP XML Metadata File](idp-metadata-file.md#banner-9-self-service-apps-and-application-navigator-idp-xml-metadata-file)
	* [Banner Admin Pages, Banner Access Management, AppXtender/AppEnhancer, and Banner 8 Self-Service](idp-metadata-file.md#banner-admin-pages-banner-access-management-appxtenderappenhancer-and-banner-8-self-service-idp-xml-metadata-file)
* [Banner 9 Self-Service Apps and Application Navigator](banner9-ssb-appnav.md#banner-9-self-service-apps-and-application-navigator)
    * [Task List for Banner 9 SSB and Application Navigator](banner9-ssb-appnav.md#task-list-for-banner-9-ssb-and-application-navigator)
	* [Configuring Entra for Banner 9 Self-Service Apps and Application Navigator](banner9-ssb-appnav.md#configuring-entra-for-banner-9-self-service-apps-and-application-navigator)
	* [Configuring Banner 9 Self-Service Apps and Application Navigator](banner9-ssb-appnav.md#configuring-banner-9-self-service-apps-and-application-navigator)
		* [Java Keystore File](banner9-ssb-appnav.md#java-keystore-file)
		* [Service Provider XML Metadata File](banner9-ssb-appnav.md#service-provider-xml-metadata-file)
		* [Identity Provider XML Metadata File](banner9-ssb-appnav.md#identity-provider-xml-metadata-file)
		* [Application Groovy File](banner9-ssb-appnav.md#application-groovy-file)
    * [Configuring Session Timeouts for Application Navigator and SSB 9 Apps](banner9-ssb-appnav.md#configuring-session-timeouts-for-application-navigator-and-ssb-9-apps)
	* [Whitelisting Application Navigator in Google Chrome](banner9-ssb-appnav.md#whitelisting-application-navigator-in-google-chrome)
	* [Whitelisting Application Navigator in Mozilla Firefox](banner9-ssb-appnav.md#whitelisting-application-navigator-in-mozilla-firefox)
* [Banner Admin Pages and Banner Access Management](banner-admin-pages-access-mgmt.md#banner-admin-pages-and-banner-access-management)
    * [Task List for Admin Pages and Access Management](banner-admin-pages-access-mgmt.md#task-list-for-admin-pages-and-access-management)
	* [Configuring Entra for Banner Admin Pages](banner-admin-pages-access-mgmt.md#configuring-entra-for-banner-admin-pages)
	* [Configuring Entra for Banner Access Management](banner-admin-pages-access-mgmt.md#configuring-entra-for-banner-access-management)
	* [Configuring Tomcat's Cookie Settings](banner-admin-pages-access-mgmt.md#configuring-tomcats-cookie-settings)
	* [Configuring Banner Admin Pages and Banner Access Management](banner-admin-pages-access-mgmt.md#configuring-banner-admin-pages-and-banner-access-management)
		* [Java Keystore File](banner-admin-pages-access-mgmt.md#java-keystore-file)
		* [Service Provider XML Metadata File](banner-admin-pages-access-mgmt.md#service-provider-xml-metadata-file)
		* [Identity Provider XML Metadata File](banner-admin-pages-access-mgmt.md#identity-provider-xml-metadata-file)
		* [Deploying with ESM](banner-admin-pages-access-mgmt.md#deploying-with-esm)
		* [Configuring the Admin Pages and Access Management Session Timeout](banner-admin-pages-access-mgmt.md#configuring-the-admin-pages-and-access-management-session-timeout)
		* [Configuring the Admin Pages and Access Management SAML Token Lifetime](banner-admin-pages-access-mgmt.md#configuring-the-admin-pages-and-access-management-saml-token-lifetime)
        * [Testing the Admin Pages' SSO Without Application Navigator](banner-admin-pages-access-mgmt.md#testing-the-admin-pages-sso-without-application-navigator)
* [Banner 8 Self-Service](banner8-self-service.md#banner-8-self-service)
	* [Configuring Entra for Banner 8 Self-Service](banner8-self-service.md#configuring-entra-for-banner-8-self-service)
	* [Java Keystore File](banner8-self-service.md#java-keystore-file)
	* [Service Provider XML Metadata File](banner8-self-service.md#service-provider-xml-metadata-file)
	* [Identity Provider XML Metadata File](banner8-self-service.md#identity-provider-xml-metadata-file)
	* [Configuring SSOManager](banner8-self-service.md#configuring-ssomanager)
    * [Configuring the Tomcat Session Timeout for SSB8](banner8-self-service.md#configuring-the-tomcat-session-timeout-for-ssb8)
	* [Update GUACONF](banner8-self-service.md#update-guaconf)
* [AppXtender/AppEnhancer (Banner Document Management)](appxtender.md#appxtenderappenhancer-banner-document-managment)
	* [Configuring Entra for AppXtender](appxtender.md#configuring-entra-for-appxtender)
	* [PKCS #12 Keystore File](appxtender.md#pkcs-12-keystore-file)
	* [Configuring AppXtender](appxtender.md#configuring-appxtender)
	* [Configuring SSO Integration with Banner and AppXtender](appxtender.md#configuring-sso-integration-with-banner-admin-pages-and-appxtender)

## Introduction

The purpose of this documentation is to explain how to integrate Microsoft Entra as a single sign-on (SSO) provider with various Ellucian Banner apps. While Ellucian does have documentation pertaining to setting up SSO with different apps, those documents only cover Ethos Identity Management as the identity provider. I have done my best, through trial and error, to compile documentation to get several Banner applications working with Microsoft Entra and SAML. What works in my environment may not work in your environment. Modify your approach as needed. This documentation should only serve as a jump start on integrating Microsoft Entra with Banner apps.

Some key words in this document are service provider (SP) and identity provider (IDP). A service provider will be an application which provides a service to your users, such as Application Navigator, Student Self-Service, and Student Registration Self-Service. An identity provider is a service that provides authentication for your application, such as Microsoft Entra or Ethos Identity Management.

This documentation will cover manually configuring the applications and briefly discuss using ESM to deploy Banner Admin Pages and Banner Access Management. You should modify these methods to suit your deployment methods, or work with the person responsible for deploying your applications. The respective applications will have documentation in the Customer Center that discuss deployment. The Ellucian SSO Handbook is also a great resource for more information on implementing SAML with your applications. 

DISCLAIMER: This documentation is presented without warranty. I don't claim to be an expert in this subject matter, but I have been working with single sign-on for several years. I'm bound to get things wrong and will gladly admit when I am. These instructions mostly reflect my own institution's environment. You know your environment better than I do. Please modify these instructions to your own needs and let me know if you think anything should be corrected. 
