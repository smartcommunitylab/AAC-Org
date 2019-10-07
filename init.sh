#!/bin/sh
if [ -z "${APIM_HOSTNAME}" ]; then
  echo "APIM_HOSTNAME is not set"
else
  rm -f cert.pem && echo -n | openssl s_client -connect ${APIM_HOSTNAME}:9443 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > ./cert.pem
  keytool -import -trustcacerts -file cert.pem -alias root -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt
fi
java -jar app.jar
