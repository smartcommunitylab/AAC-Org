# NiFi Connector

This document explains how multitenancy works in NiFi and how to configure the connector so that the Organization Management server's operations are reflected in NiFi.\
If you're not interested in how multi-tenancy is represented in NiFi, skip to the [Certificates](#certificates) section to create the necessary certificates.\
If you don't need to create certificates and only need to configure the connector, skip to the [Configuration](#configuration) section.

## Multi-tenancy in NiFi
The idea of multi-tenancy in NiFi is that **process groups** represent tenants and have **policies** defined for them, listing which **users** or **user groups** are allowed to view or alter them. User groups are equivalent to teams, so if permission to view a process group is given to a user group, all users belonging to it can view it.

Users will still be able to see other teams’ process groups on the flow, but they will only appear as rectangles that they can neither interact with or view details of. The only information they can see about them is the amount of data they are processing.

## Certificates
Executing tenant and user management operations in a secured NiFi instance requires specific authorizations, so Organization Manager needs to act with the permissions granted to the administrator user.\
Since _OpenID Connect_ is used to secure NiFi, we have to authenticate by providing the administrator’s SSL certificate and configuring NiFi to recognize it.\
This section describes how to do this and is heavily based on a very useful and detailed [article](https://community.hortonworks.com/content/supportkb/151106/nifi-how-to-create-your-own-certs-for-securing-nif.html) by Matt Clarke from the Hortonworks Community.

Two pieces of software are needed for this process:
- [Keytool](https://docs.oracle.com/javase/6/docs/technotes/tools/windows/keytool.html): comes bundled with Java’s JRE, so you should find it in your Java installation folder, usually in `C:\Program Files\Java\jre1.8.0_191\bin`, depending on your version.
- [OpenSSL](https://www.openssl.org/source/)

The steps described in this section are written for Windows’ _Command Prompt_, so the syntax for paths and the like may vary depending on your OS. Remember to quote paths if they contain spaces.

### Step 1: Creating a Certificate Authority (CA)
The first thing to do is creating a _Certificate Authority_ (_CA_). This CA will sign the administrator’s certificate, stating that it can be trusted.\
Change directory to the `bin` subfolder of your OpenSSL installation (for example, `cd C:\OpenSSL\openssl-1.0.2j\bin`). If you don’t, you will need to replace `openssl` in each command with the **path to the openssl.exe file**.

#### 1.1 Creating the CA’s private key
This will create the private key for your CA and place it in the `C:\certs` folder. If you omit the path and just write `myCA.key`, it will be in the **same folder as openssl.exe**. You will be asked to choose a password.

`openssl genrsa -aes128 -out C:\certs\myCA.key 4096`

#### 1.2 Creating a _pem_ certificate
This command creates the CA’s certificate. You will be asked to provide the password you chose in [1.1](#11-creating-the-cas-private-key). You will then have to fill the CA’s profile (country, organization name, etc.): the data you insert in this step is not important for our purposes, but it might be preferable to pick something that will help you recognize this certificate.\
`1095` is the validity (in days) of the certificate, feel free to change it as you see fit.

`openssl req -x509 -new -key C:\certs\myCA.key -days 1095 -out C:\certs\myCA.pem`

#### 1.3 Converting from _pem_ to _der_
Converting the certificate into **der** format is necessary for the next step, performed by Keytool.

`openssl x509 -outform der -in C:\certs\myCA.pem -out C:\certs\myCA.der`

### Step 2: Creating NiFi’s Truststore
A truststore lists which certificates can be trusted. It is necessary to add the CA’s certificate to this truststore, otherwise the CA’s signature on the administrator’s certificate will be meaningless.

Change directory to where **keytool.exe** is located (probably something like `C:\Program Files\Java\jre1.8.0_191\bin`), or replace `keytool` with the **path to the keytool.exe file**.

#### 2.1 Creating the truststore
This will create the truststore and include the CA’s certificate in it, meaning it can be trusted. You will be asked to choose a password for the truststore. It will then show you the CA’s certificate and ask you to confirm it can be trusted by typing the word _yes_ in your system’s language.

`keytool -import -keystore C:\certs\truststore.jks -file C:\certs\myCA.der -alias myCA`

#### 2.2 Configuring NiFi to use the new truststore
Open the `nifi.properties` file (it can be found inside the `conf` subfolder of your NiFi installation) and edit the following fields. In newer NiFi versions, the `needClientAuth` field may not be present, in which case you can omit it. The password for the `truststorePasswd` field is the one you chose in [2.1](#21-creating-the-truststore).

`nifi.security.truststore=C:/certs/truststore.jks`\
`nifi.security.truststoreType=JKS`\
`nifi.security.truststorePasswd=MyTruststorePassword`\
`nifi.security.needClientAuth=true`

### Step 3: Generating a keystore for the NiFi server
This is not strictly related to our tenant-providing process; however, when running a secured instance of NiFi, it is necessary for it to have a keystore, and for the browser that accesses NiFi to trust it. Since we just created a CA, we can use it to create NiFi’s keystore.

#### 3.1 Generate a keystore for the NiFi server
Change directory to the path to **keytool.exe** (for example `cd C:\Program Files\Java\jre1.8.0_191\bin`), or replace `keytool` with the **path to keytool.exe**.

The following command will generate the keystore. It will ask you to choose a password for the keystore, and then to fill the profile of the certificate, similarly to what happened when generating the CA. When asked for the full name (it should be the first thing asked after password confirmation), insert your domain’s name. If you’re doing this on localhost, simply type `localhost`.

Finally, it will ask you to choose a password for the private key. If you just hit enter, it will use the same password as the keystore’s.

`keytool -genkey -alias nifiserver -keyalg RSA -keystore C:\certs\nifiserver.jks -keysize 2048`

#### 3.2 Generating a certificate sign request
This command will generate a certificate with a request to sign it. It may ask for both the passwords you chose in [3.1](#31-generate-a-keystore-for-the-nifi-server): first the keystore’s password and then the private key’s password. If they are the same, it will only ask once.

`keytool -certreq -alias nifiserver -keystore C:\certs\nifiserver.jks -file C:\certs\nifiserver.csr`

#### 3.3 Signing the NiFi server’s certificate
Once again, change directory to OpenSSL’s `bin` subfolder, or replace `openssl` accordingly.

This command will have the CA sign your NiFi server’s certificate to state that it can be trusted. It will ask for the password you chose in [1.1](#11-creating-the-cas-private-key).

`openssl x509 -sha256 -req -in C:\certs\nifiserver.csr -CA C:\certs\myCA.pem -CAkey C:\certs\myCA.key -CAcreateserial -out C:\certs\nifiserver.crt -days 730`

#### 3.4 Import the CA’s public key into the keystore
Switch back to Keytool’s folder, or replace `keytool` accordingly.

This command will include the CA’s public key into your keystore, so that it may be used to verify your certificate’s validity. It will ask for the keystore’s password, which you chose in [3.1](#31-generate-a-keystore-for-the-nifi-server). You will have to confirm that the certificate can be trusted by typing _yes_ in your system’s language.

`keytool -import -keystore C:\certs\nifiserver.jks -file C:\certs\myCA.pem`

#### 3.5 Import the signed NiFi server’s certificate into the keystore
This command will import the certificate you signed in [3.3](#33-signing-the-nifi-servers-certificate) into the keystore. It will ask for the two passwords you chose in [3.1](#31-generate-a-keystore-for-the-nifi-server): first the keystore’s password and then the private key’s password, or just one of them if they are the same.

`keytool -import -trustcacerts -alias nifiserver -file C:\certs\nifiserver.crt -keystore C:\certs\nifiserver.jks`

#### 3.6 Configuring NiFi to use the new keystore
Open the `nifi.properties` file (from the `conf` subfolder of your NiFi installation) and edit the following properties, using the two passwords chosen in [3.1](#31-generate-a-keystore-for-the-nifi-server):

`nifi.security.keystore=C:/certs/nifiserver.jks`\
`nifi.security.keystoreType=JKS`\
`nifi.security.keystorePassword=MyKeystorePassword`\
`nifi.security.keyPassword=MyPrivateKeyPassword`

#### 3.7 Adding the CA to your browser
When accessing NiFi, your browser will likely state that the connection cannot be trusted. This is because, even though the NiFi server’s certificate is signed by your CA, your browser does not know your CA.

It may offer you to add an exception, but at this point you might as well add the CA you created to the list of trusted CAs.

For example, to do it in Mozilla Firefox:\
**Settings** > **Options** > **Privacy and security** > **Show certificates** (on the right, near the bottom, in the _Certificates_ section) > **Authorities** tab > **Import** > open your `myCA.pem` file and check both boxes.\
You might need to restart your browser. Afterwards, you should be able to access NiFi. If it still says the connection cannot be trusted, you might have inserted the wrong name in [3.1](#31-generate-a-keystore-for-the-nifi-server), and have to repeat steps [3.1](#31-generate-a-keystore-for-the-nifi-server) through [3.5](#35-import-the-signed-nifi-servers-certificate-into-the-keystore).

### Step 4: Make the CA sign the administrator’s certificate
By having the administrator’s certificate signed by the CA, it will be recognized as valid by NiFi, since it trusts the CA.
Change directory back to the `bin` subfolder of your OpenSSL installation, or replace `openssl` with the **path to the openssl.exe file**.

#### 4.1 Generating the administrator’s certificate’s private key
Same command as when you created the CA’s private key. It will ask you to choose a password.

`openssl genrsa -aes128 -out C:\certs\admin.key 2048`

#### 4.2 Generating a certificate sign request
Like in step 3.2, this command will generate a certificate with a request to sign it. You will be asked to provide the password to the private key you just created. It will then ask you to fill the profile of the certificate, similarly to what you did with the CA.\
It is now important to provide the name of the administrator (for example in **Common Name**, or **Email Address**), as it will be used by NiFi to associate this certificate to the admin user account (see [4.5](#45-configure-nifi-to-find-the-administrators-name) for more information). The other fields are not very meaningful, but again, try to pick something that will help you recognize the certificate.

Also note that it will ask you for a **challenge password** and an **optional company name**. The challenge password is very rarely used by some CAs when requesting to revoke a certificate. Both fields can safely be left blank.

`openssl req -new -key C:\certs\admin.key -out C:\certs\admin.csr`

#### 4.3 Signing the administrator’s certificate
You can now have the CA sign your administrator’s certificate. It will ask for the password you created in [1.1](#11-creating-the-cas-private-key).

`openssl x509 -req -in C:\certs\admin.csr -CA C:\certs\myCA.pem -CAkey C:\certs\myCA.key  -CAcreateserial -out C:\certs\admin.crt -days 730`

#### 4.4 Converting from _crt_ to _p12_
This command will convert the signed certificate into **p12** format. It will ask you to provide the password you chose in [4.1](#41-generating-the-administrators-certificates-private-key), and then it will ask you to choose an export password, needed to extract the certificate from the p12 file.

`openssl pkcs12 -export -out C:\certs\admin.p12 -inkey C:\certs\admin.key -in C:\certs\admin.crt -certfile C:\certs\myCA.pem -certpbe PBE-SHA1-3DES -name “admin”`

#### 4.5 Configure NiFi to find the administrator’s name
Finally, you have to uncomment two lines from the `nifi.properties` file (inside the `conf` subfolder of your NiFi installation) and give them proper values.\
They are regular expressions used by NiFi to find the administrator’s name inside the certificate you created in [4.2](#42-generating-a-certificate-sign-request). Configuring these two lines incorrectly can lead to 403 errors.

The field names are: `EMAILADDRESS` (Email address), `CN` (Common Name), `OU` (Organizational Unit Name), `O` (Organization Name), `L` (Locality Name), `ST` (State or Province Name), `C` (Country Code).

This particular configuration will take the administrator’s name from the `Common Name` field, but you can alter it depending on how you filled the profile during [4.2](#42-generating-a-certificate-sign-request).\
`nifi.security.identity.mapping.pattern.dn=^(EMAILADDRESS=(.*?), )?CN=(.*?), OU=(.*?), O=(.*?), L=(.*?), ST=(.*?), C=(.*?)$`\
`nifi.security.identity.mapping.value.dn=$3`

## Configuration
Many of the fields represent NiFi API end-points and have fixed values: although unlikely, there is a chance that they may change in newer versions of NiFi. If you suspect this has happened, you should be able to find the new end-point in the [official documentation](https://nifi.apache.org/docs/nifi-docs/rest-api/index.html).

-	`name`: Name of the component, only needed for display.
-	`componentId`: ID of the component, should be `nifi`
-	`scope`: Scope of the component, should be `components/nifi`
-	`implementation`: Full class name of the class implementing the component. The class designed for NiFi is `it.smartcommunitylab.nificonnector.NiFiConnector`; alternatively, the value `it.smartcommunitylab.orgmanager.componentsmodel.DefaultComponentImpl` may be used to disable the NiFi connector.
-	`roles`: Comma-separated list of roles that may be assigned to users via OMC. It should consist of all roles listed in the `readRoles` field plus all roles listed in the `writeRoles` field.
-	`host`: URI where NiFi is hosted.
-	`listUsersApi`: NiFi API end-point for listing users. Should be `/nifi-api/tenants/users`, unless a new version of NiFi changes it into something different.
-	`createUserApi`: NiFi API end-point for creating a user. Should be `/nifi-api/tenants/users`
-	`deleteUserApi`: NiFi API end-point for deleting a user. Should be `/nifi-api/tenants/users/`
-	`listUserGroupsApi`: NiFi API end-point for listing user groups. Should be `/nifi-api/tenants/user-groups`
-	`createUserGroupApi`: NiFi API end-point for creating a user group. Should be `/nifi-api/tenants/user-groups`
-	`updateUserGroupApi`: NiFi API end-point for updating a user group. Should be `/nifi-api/tenants/user-groups/`
-	`deleteUserGroupApi`: NiFi API end-point for deleting a user group. Should be `/nifi-api/tenants/user-groups/`
-	`getPolicyApi`: NiFi API end-point to retrieve a policy. Should be `/nifi-api/policies/`
-	`createPolicyApi`: NiFi API end-point to create a policy. Should be `/nifi-api/policies`
-	`updatePolicyApi`: NiFi API end-point to update a policy. Should be `/nifi-api/policies/`
-	`listProcessGroupsApi`: NiFi API end-point to list process groups. Should be `/nifi-api/process-groups/`
-	`getProcessGroupApi`: NiFi API end-point to retrieve a process group. Should be `/nifi-api/process-groups/`
-	`createProcessGroupApi`: NiFi API end-point to create a process group. Should be `/nifi-api/process-groups/`
-	`deleteProcessGroupApi`: NiFi API end-point to delete a process group. Should be `/nifi-api/process-groups/`
-	`accessApi`: NiFi API end-point to retrieve the status of the current access. Should be `/nifi-api/access`
-	`keystorePath`: Absolute path to the certificate. Following the example in the [Certificates](#certificates) section, it would have the value `C:/certs/admin.p12`, determined in [4.4](#44-converting-from-crt-to-p12).
-	`keystoreType`: Type of the certificate. In the example, it would have the value `PKCS12`.
-	`keystoreExportPassword`: The password for the certificate. In the example, it would have the value chosen in [4.4](#44-converting-from-crt-to-p12).
-	`truststorePath`: Absolute path to the truststore. In the example, it would be `C:/certs/truststore.jks`, determined in [2.1](#21-creating-the-truststore).
-	`truststoreType`: Type of the truststore. In the example, it would have the value `JKS`.
-	`truststorePassword`: Password of the truststore. In the example, it would have the value chosen in [2.1](#21-creating-the-truststore).
-	`adminName`: Name of the administrator user.
-	`ownerRole`: Role used by AAC to indicate ownership. Should be `ROLE_PROVIDER`. Will have both read and write permissions on process groups.
-	`readRoles`: Names of the roles which will have read-only permissions on process groups. While multiple roles may be listed, separated by a comma, they would all be equivalent, so listing 1 role only is advisable. The `roles` field should contain all roles listed in this field and all roles listed in the `writeRoles` field, or consistency issues may arise.
-	`writeRoles`: Names of roles which will have both read and write permissions (just like the owner role) on process groups. While multiple roles may be listed, separated by a comma, they would all be equivalent, so listing 1 role only is advisable. The `roles` field should contain all roles listed in this field and all roles listed in the `readRoles` field, or consistency issues may arise.
