/*
 Copyright 2016  Richard Robinson @ HSCIC <rrobinson@hscic.gov.uk, rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.security;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.apache.commons.codec.binary.Base64;
import org.medipi.model.EncryptedAndSignedUploadDO;

/**
 * Service to provide conversion to- and from- the encrypted and/or signed
 * representations of payloads This class uses Nimbus JOSE + JWT library to
 * perform encryption and signing. This implements: Signing JWS RFC 7515
 * Encryption JWE RFC 7516
 *
 * @author Richard Robison rrobinson@nhs.net
 */
public class UploadEncryptionAdapter {

    private List<com.nimbusds.jose.util.Base64> certChain;

    /**
     * Client mode encrypts and signs the payload - used for sending data from
     * the patient device to the concentrator
     */
    public static final int CLIENTMODE = 0;

    /**
     * Server mode decrypts and verifies the signature of an incoming payload -
     * used for receiving data from the patient device to the concentrator
     */
    public static final int SERVERMODE = 1;

    /**
     * Sign mode signs an entity
     */
    public static final int SIGNMODE = 2;

    /**
     * Verify Signature mode verifies the signature of an entity
     */
    public static final int VERIFYSIGNATUREMODE = 3;

    /**
     * Test mode allows a testing function of encryption, signing, decryption
     * and signature verification - for testing
     */
    public static final int ALLMODE = 4;
    private int mode;
    private CertificateDefinitions cd;

    private RSAPrivateKey signPrivateKey;
    private X509Certificate[] signTrustCerts;

    private RSAPublicKey encryptPublicKey;
    private RSAPrivateKey encryptPrivateKey;
    private static final int AESKEYSIZE = 256;

    /**
     * Constructor
     */
    public UploadEncryptionAdapter() {

    }

    /**
     *
     * @param cd definitions to be used for loading certificates
     * @param properties
     * @param mode Execution mode
     * @return
     * @throws NoSuchAlgorithmException
     */
    public String init(CertificateDefinitions cd, int mode) throws NoSuchAlgorithmException {

        this.mode = mode;
        this.cd = cd;
        String error;
        switch (mode) {
            case CLIENTMODE:
                if ((error = loadClientSigningKeys()) != null) {
                    return error;
                }
                if ((error = loadClientEncryptionKeys()) != null) {
                    return error;
                }
                break;
            case SERVERMODE:
                if ((error = loadServerSigningKeys()) != null) {
                    return error;
                }
                if ((error = loadServerEncryptionKeys()) != null) {
                    return error;
                }
                break;
            case SIGNMODE:
                if ((error = loadClientSigningKeys()) != null) {
                    return error;
                }
                break;
            case VERIFYSIGNATUREMODE:
                if ((error = loadServerSigningKeys()) != null) {
                    return error;
                }
                break;
            case ALLMODE: // both client and server modes
                if ((error = loadClientSigningKeys()) != null) {
                    return error;
                }
                if ((error = loadClientEncryptionKeys()) != null) {
                    return error;
                }
                if ((error = loadServerSigningKeys()) != null) {
                    return error;
                }
                if ((error = loadServerEncryptionKeys()) != null) {
                    return error;
                }
                break;
            default:
                return "No recognised mode has been selected. ";
        }
        return null;
    }

    private String loadClientSigningKeys() {
        String signKey = cd.getSIGNKEYSTORELOCATION();
        String signAlias = cd.getSIGNKEYSTOREALIAS();
        String signPassword = cd.getSIGNKEYSTOREPASSWORD();
        if (signKey != null) {
            if (signAlias != null) {
                if (signPassword != null) {
                    try {
                        KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection(signPassword.toCharArray());
                        KeyStore sks = KeyStore.getInstance("JKS");
                        FileInputStream fis = new FileInputStream(signKey);
                        sks.load(fis, signPassword.toCharArray());
                        signPrivateKey = (RSAPrivateKey) (((KeyStore.PrivateKeyEntry) sks.getEntry(signAlias, pp)).getPrivateKey());
                        // We pass the full certificate chain but will only use the signing cert when this arrives at the host concentrator.
                        // The signing cert will be used to walk up the chain in the local trusted store
                        Certificate[] c = sks.getCertificateChain(signAlias);
                        certChain = new ArrayList<>();
                        for (Certificate cert : c) {
                            X509Certificate xcert = (X509Certificate) cert;
                            com.nimbusds.jose.util.Base64 b64 = com.nimbusds.jose.util.Base64.encode(xcert.getEncoded());
                            certChain.add(b64);
                        }
                        X509Certificate signingCert = (X509Certificate) sks.getCertificate(signAlias);
                        RSAPublicKey signPublicKey = (RSAPublicKey) signingCert.getPublicKey();
                        if (signingCert == null) {
                            return "Signing certificate is not present";
                        }
                        try {
                            signingCert.checkValidity();
                            return null;
                        } catch (CertificateNotYetValidException e) {
                            return "Signing Certificate is not yet valid";
                        } catch (CertificateExpiredException e) {
                            return "Signing Certificate has expired";
                        }

                    } catch (Exception e) {
                        return "error loading signing certificate: " + e.getLocalizedMessage();
                    }

                } else {
                    return "Warning: Property SIGNKEYSTOREPASSWORD not set";
                }
            } else {
                return "Warning: Property SIGNKEYSTOREALIAS not set";
            }
        } else {
            return "Warning: Property SIGNKEYSTORELOCATION not set";
        }
    }

    private String loadClientEncryptionKeys() {
//THIS IS FOR THE CLIENT TO ENCRYPT
        try {
            if (cd.getEncryptTruststorePEM() != null) {
                InputStream stream = new ByteArrayInputStream(cd.getEncryptTruststorePEM());
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate cert = cf.generateCertificate(stream);
                X509Certificate x509cert = (X509Certificate) cert;
                encryptPublicKey = (RSAPublicKey) x509cert.getPublicKey();
            } else {
                String truststoreLocation = cd.getENCRYPTTRUSTSTORELOCATION();
                String truststorePass = cd.getENCRYPTTRUSTSTOREPASSWORD();
                String truststoreAlias = cd.getENCRYPTTRUSTSTOREALIAS();
                if (truststoreLocation != null) {
                    if (truststorePass != null) {
                        if (truststoreAlias != null) {
                            KeyStore trustStore = loadStore(truststoreLocation, truststorePass);
                            X509Certificate trustcert = (X509Certificate) trustStore.getCertificate(truststoreAlias);
                            encryptPublicKey = (RSAPublicKey) trustcert.getPublicKey();
                        } else {
                            return "Encryption truststore alias not set";
                        }
                    } else {
                        return "Encryption truststore password not set";
                    }
                } else {
                    return "Encryption truststore location not set";
                }
            }
            return null;
        } catch (Exception e) {
            return "error loading encrypt certificate: " + e.getLocalizedMessage();
        }
    }

    private String loadServerSigningKeys() {
        try {

            String truststoreLocation = cd.getSIGNTRUSTSTORELOCATION();
            String truststorePass = cd.getSIGNTRUSTSTOREPASSWORD();
            if (truststoreLocation != null) {
                if (truststorePass != null) {

                    KeyStore trustStore = loadStore(truststoreLocation, truststorePass);

                    signTrustCerts = new X509Certificate[trustStore.size()];
                    int i = 0;
                    Enumeration<String> alias = trustStore.aliases();

                    while (alias.hasMoreElements()) {
                        signTrustCerts[i++] = (X509Certificate) trustStore.getCertificate(alias
                                .nextElement());
                    }
                } else {
                    return "Signature truststore password not set";
                }
            } else {
                return "Signature truststore location not set";
            }

            return null;
        } catch (Exception e) {
            return "error loading Signing certificate: " + e.getLocalizedMessage();
        }
    }

    private String loadServerEncryptionKeys() {
// This for SERVER decryption
        String encryptKey = cd.getENCRYPTKEYSTORELOCATION();
        String encryptAlias = cd.getENCRYPTKEYSTOREALIAS();
        String encryptPassword = cd.getENCRYPTKEYSTOREPASSWORD();
        if (encryptKey != null) {
            if (encryptAlias != null) {
                if (encryptPassword != null) {
                    try {
                        //THIS IS FOR THE SERVER DECRYPT
                        KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection(encryptPassword.toCharArray());
                        KeyStore sks = KeyStore.getInstance("JKS");
                        FileInputStream fis = new FileInputStream(encryptKey);
                        sks.load(fis, encryptPassword.toCharArray());
                        encryptPrivateKey = (RSAPrivateKey) (((KeyStore.PrivateKeyEntry) sks.getEntry(encryptAlias, pp)).getPrivateKey());
                        return null;
                    } catch (Exception e) {
                        return "error loading Encryption certificate: " + e.getLocalizedMessage();
                    }
                } else {
                    return "Warning: Property ENCRYPTKEYSTOREPASSWORD not set";
                }
            } else {
                return "Warning: Property ENCRYPTKEYSTOREALIAS not set";
            }
        } else {
            return "Warning: Property " + "medipi.json.encrypt.keystore.location" + " not set";
        }
    }

    private KeyStore loadStore(String trustStoreFile, String password) throws Exception {
        KeyStore store = KeyStore.getInstance("JKS");
        store.load(new FileInputStream(trustStoreFile), password.toCharArray());
        return store;
    }

    /**
     * Method to encryptAndSign a payload
     *
     * @param dp payload to be encrypted
     * @return Encrypted and signed representation of the data payload
     * @throws Exception
     */
    public EncryptedAndSignedUploadDO encryptAndSign(Serializable dp) throws Exception {
        // serialize the object
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = null;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(dp);
                byte[] yourBytes = bos.toByteArray();
                String signedPayload = signPayload(yourBytes);
                KeyGenerator kgen;
                try {
                    kgen = KeyGenerator.getInstance("AES");
                    kgen.init(AESKEYSIZE);
                } catch (NoSuchAlgorithmException ex) {
                    throw new Exception("encryption algorithm is unrecognised. " + ex.getLocalizedMessage());
                }
                SecretKey key = kgen.generateKey();
                EncryptedAndSignedUploadDO easu = new EncryptedAndSignedUploadDO(UUID.randomUUID().toString());
                String aesEncryptedPayload = symmetricallyEncrypt(signedPayload, key);
                String rsaEncryptedSharedKey = encryptSharedKey(key);
                return new EncryptedAndSignedUploadDO(UUID.randomUUID().toString(), rsaEncryptedSharedKey, aesEncryptedPayload);

            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ex) {
                    // ignore close exception
                }
                try {
                    bos.close();
                } catch (IOException ex) {
                    // ignore close exception
                }
            }

        } catch (IOException ex) {
            throw new Exception("cannot serialise payload for transmission ." + ex.getLocalizedMessage());
        }
    }

    /**
     * Method to sign a payload
     *
     * @param pay payload to be signed
     * @return String serialisation of the signed JWSObject
     * @throws Exception
     */
    public String signPayload(byte[] pay) throws Exception {
        try {
            // Create RSA-signer with the private key
            JWSSigner signer = new RSASSASigner(signPrivateKey);

            // Prepare JWS object with simple string as payload
            JWSHeader.Builder builder = new JWSHeader.Builder(JWSAlgorithm.RS256);
            builder.x509CertChain(certChain);
            JWSObject jwsObject = new JWSObject(builder.build(), new Payload(pay));

            // Compute the RSA signature
            jwsObject.sign(signer);

            return jwsObject.serialize();
        } catch (JOSEException ex) {
            throw new Exception("unable to sign the payload. " + ex.getLocalizedMessage());
        }
    }

    private String symmetricallyEncrypt(String signedPayload, SecretKey key) throws Exception {
        try {
            // ENCRYPT PAYLOAD USING AES SHARED KEY

            // Create the header
            JWEHeader skHeader = new JWEHeader(JWEAlgorithm.A256KW, EncryptionMethod.A256GCM);

            // Set the plain text
            Payload payload = new Payload(signedPayload);

            // Create the JWE object and encryptAndSign it
            JWEObject jweObject = new JWEObject(skHeader, payload);

            jweObject.encrypt(new AESEncrypter(key.getEncoded()));

            // Serialise to compact JOSE form...
            return jweObject.serialize();
        } catch (JOSEException ex) {
            throw new Exception("cannot encrypt payload. " + ex.getLocalizedMessage());
        }
    }

    private String encryptSharedKey(SecretKey key) throws Exception {
        try {
            // ENCRYPT THE SHARED KEY WITH ASYMETRIC ENCRYPTION AND PUT IN JWT
            Base64 b64 = new Base64();
            byte[] encryptedKey = b64.encode(key.getEncoded());
            // Compose the JWT claims set
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(new String(encryptedKey, StandardCharsets.UTF_8))
                    .build();

//            System.out.println(claimsSet.toJSONObject());

            // Request JWT encrypted with RSA-OAEP-256 and 256-bit AES/GCM
            JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM);

            // Create the encrypted JWT object
            EncryptedJWT jwt = new EncryptedJWT(header, claimsSet);

            // Create an encrypter with the specified public RSA key
            RSAEncrypter encrypter = new RSAEncrypter(encryptPublicKey);

            // Do the actual encryption
            jwt.encrypt(encrypter);

            // Serialise to JWT compact form
            return jwt.serialize();
        } catch (JOSEException ex) {
            throw new Exception("cannot encrypt key. " + ex.getLocalizedMessage());
        }
    }

    /**
     * Method to decrypt and verify the signature of an object
     *
     * @param easu Encrypted and signed object
     * @return object in the clear
     * @throws Exception
     */
    public Object decryptAndVerify(EncryptedAndSignedUploadDO easu) throws Exception {

        // Decrypt assymmetric key
        EncryptedJWT jwt;
        try {
            jwt = EncryptedJWT.parse(easu.getEncryptedKey());
        } catch (ParseException ex) {
            throw new Exception("cannot understand incoming encrypted key. " + ex.getLocalizedMessage());
        }

        // Create a decrypter with the specified private RSA key
        RSADecrypter decrypter = new RSADecrypter(encryptPrivateKey);

        try {
            // Decrypt
            jwt.decrypt(decrypter);
        } catch (JOSEException ex) {
            throw new Exception("cannot decrypt shared key. " + ex.getLocalizedMessage());
        }

        // Decrypt
        Base64 b64 = new Base64();
        byte[] sharedKey;
        JWEObject jweObject;
        try {
            sharedKey = b64.decode(jwt.getJWTClaimsSet().getSubject().getBytes(StandardCharsets.UTF_8));

            // Use the decrypted asymmetic key to decryptAndVerify the symmetric payload
            jweObject = JWEObject.parse(easu.getCipherData());
        } catch (ParseException ex) {
            throw new Exception("cannot understand incoming encrypted key or cipherdata. " + ex.getLocalizedMessage());
        }

        try {
            jweObject.decrypt(new AESDecrypter(sharedKey));
        } catch (JOSEException ex) {
            throw new Exception("cannot decrypt incoming payload. " + ex.getLocalizedMessage());
        }
        JWSObject jwsObject;
        try {
            jwsObject = JWSObject.parse(jweObject.getPayload().toString());
        } catch (ParseException ex) {
            throw new Exception("cannot understand incoming payload. " + ex.getLocalizedMessage());
        }

        if (verifySignature(jwsObject)) {
            return serializePayload(jwsObject);
        } else {
            throw new Exception("Signature does not verify.");
        }

    }

    /**
     * Method to verify the signature of a JWSObject
     *
     * @param jwsObject signature to be verified
     * @return boolean pass/fail of verification
     * @throws Exception
     * @throws CertificateException
     */
    public boolean verifySignature(JWSObject jwsObject) throws Exception, CertificateException {
        // Verify the Signature

        List<com.nimbusds.jose.util.Base64> certs = jwsObject.getHeader().getX509CertChain();
        RSAPublicKey signPublicKey = null;
        X509Certificate clientCert = null;
        for (com.nimbusds.jose.util.Base64 b : certs) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(b.decode());
            // Here we assume that the first certificate will be the cert used for signing
            if (signPublicKey == null) {
                clientCert = (X509Certificate) certFactory.generateCertificate(in);
                signPublicKey = (RSAPublicKey) clientCert.getPublicKey();
            }
        }
        if (signPublicKey == null) {
            throw new Exception("No valid patient signing certificate was recevied with the signature");
        } else if (validateKeyChain(clientCert, signTrustCerts)) {
            try {
                JWSVerifier verifier = new RSASSAVerifier(signPublicKey);
                if (jwsObject.verify(verifier)) {
//                    System.out.println(jwsObject.getPayload().toString());
                    return true;
                } else {
                    throw new Exception("Signature does not verify.");
                }
            } catch (JOSEException ex) {
                throw new Exception("signature does not verify. " + ex.getLocalizedMessage());
            }

        } else {
            throw new Exception("signature certificate has not been signed by the trusted store chain");
        }
    }

    private Object serializePayload(JWSObject jwsObject) throws Exception {
        try {
            byte b[] = jwsObject.getPayload().toBytes();
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            return si.readObject();
        } catch (Exception e) {
            throw new Exception("Unable to parse signed and encrypted payload. " + e.getLocalizedMessage());
        }
    }

    /**
     * Validate keychain
     *
     * @param client is the client X509Certificate
     * @param trustedCerts is Array containing all trusted X509Certificate
     * @return true if validation until root certificate success, false
     * otherwise
     * @throws CertificateException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static boolean validateKeyChain(X509Certificate client,
            X509Certificate... trustedCerts) throws CertificateException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException {
        boolean found = false;
        int i = trustedCerts.length;
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        TrustAnchor anchor;
        Set anchors;
        CertPath path;
        List list;
        PKIXParameters params;
        CertPathValidator validator = CertPathValidator.getInstance("PKIX");

        while (!found && i > 0) {
            anchor = new TrustAnchor(trustedCerts[--i], null);
            anchors = Collections.singleton(anchor);

            list = Arrays.asList(new Certificate[]{client});
            path = cf.generateCertPath(list);

            params = new PKIXParameters(anchors);
            params.setRevocationEnabled(false);

            if (client.getIssuerDN().equals(trustedCerts[i].getSubjectDN())) {
                try {
                    validator.validate(path, params);
                    if (isSelfSigned(trustedCerts[i])) {
                        // found root ca
                        found = true;
//                        System.out.println("validating root" + trustedCerts[i].getSubjectX500Principal().getName());
                    } else if (!client.equals(trustedCerts[i])) {
                        // find parent ca
//                        System.out.println("validating via:" + trustedCerts[i].getSubjectX500Principal().getName());
                        found = validateKeyChain(trustedCerts[i], trustedCerts);
                    }
                } catch (CertPathValidatorException e) {
                    // validation fail, check next certifiacet in the trustedCerts array
                }
            }
        }

        return found;
    }

    /**
     *
     * @param cert is X509Certificate that will be tested
     * @return true if cert is self signed, false otherwise
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static boolean isSelfSigned(X509Certificate cert)
            throws CertificateException, NoSuchAlgorithmException,
            NoSuchProviderException {
        try {
            PublicKey key = cert.getPublicKey();

            cert.verify(key);
            return true;
        } catch (SignatureException sigEx) {
            return false;
        } catch (InvalidKeyException keyEx) {
            return false;
        }
    }
}
