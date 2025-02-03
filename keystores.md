## Creating Keystores

A Java keystore is needed to generate a self-signed certificate for the service provider applications (Admin Pages, Self-Service apps, Banner 8 Self-Service, etc.). The commands used in this documentation are for the Linux/macOS version of keytool. However, the Windows version of keytool should be very similar. Since the arguments available in keytool can vary depending on the version installed, it is recommended to look up the documentation for that version.

The Java keystore should be stored somewhere on your server that is readable by the Linux user running your Tomcat server. The command below will generate a self-signed certificate that is valid for 9999 days. Please modify the validity period to your liking. To ensure compatibility with Entra, the keystore must use SHA256 with RSA as the signing algorithm. For compatibility with Banner apps, the keystore type must be in JKS format. In my testing, the Banner apps do not support PKCS12 keystores for the SAML configuration.

Once the keystore is created, the X.509 public certificate will need to be exported. Keep the file stored away to be used within the service provider XML metadata file later. The same keystore file (and exported public certificate) can be used for all of the applications. You do not need to create a keystore for each application. 

It is also recommended to create a keystore for each environment (DEV, TEST, PROD) just to keep things separate, but not required. Do what is best for your work environment. This documentation will be based around the TEST environment.

### Creating a Java Keystore for Service Provider Applications

This file will only be used for the applications. You do not need to import this certificate into Entra.

1. Open a command line/terminal
2. Navigate to a directory to store the Java keystore
3. Run the following command:
	
	```
	keytool -genkey -keyalg RSA -alias banner-saml-sp
			-keystore samlkeystore.jks -validity 9999
			-keysize 2048 -storetype JKS -sigalg SHA256withRSA
	```
4. Store the Java keystore on your server in a directory that is readable by the operating system user that runs the Tomcat application.
	* For this documentation, the `/u01/app/tomcat/saml` directory will be used and `tomcat` is the user that runs the Tomcat application.
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