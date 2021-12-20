package crypto;

import org.bouncycastle.crypto.engines.RSABlindingEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSABlindingFactorGenerator;
import org.bouncycastle.crypto.params.RSABlindingParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;

public class RsaEnvelope {
    private final RSAKeyParameters publicKey;
    private final RSABlindingFactorGenerator blindingFactorGenerator;
    private final RSABlindingParameters blindingParameters;

    public RsaEnvelope(String publicKeyPem) {
        this(toRsaPublicKey(publicKeyPem));
    }

    public RsaEnvelope(RSAKeyParameters publicKey) {
        this.publicKey = publicKey;
        blindingFactorGenerator = new RSABlindingFactorGenerator();
        blindingFactorGenerator.init(publicKey);
        BigInteger blindingFactor = blindingFactorGenerator.generateBlindingFactor();
        blindingParameters = new RSABlindingParameters(publicKey, blindingFactor);
    }

    public byte[] create(byte[] content) {
        try {
            byte[] contentDigest = MessageDigest.getInstance("SHA-256").digest(content);

            RSABlindingEngine engine = new RSABlindingEngine();
            engine.init(true, blindingParameters);
            return engine.processBlock(contentDigest, 0, contentDigest.length);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] revealedSignature(byte[] signatureOnEnvelope) {
        RSABlindingEngine engine = new RSABlindingEngine();
        engine.init(false, blindingParameters);
        return engine.processBlock(signatureOnEnvelope, 0, signatureOnEnvelope.length);
    }

    public static RSAKeyParameters toRsaPublicKey(String pem) {
        String replacedPem = pem.replace("-----BEGIN PUBLIC KEY-----", "-----BEGIN PUBLIC KEY-----\n");
        replacedPem = replacedPem.replace("-----END PUBLIC KEY-----", "\n-----END PUBLIC KEY-----");

        StringReader stringReader = new StringReader(replacedPem);
        PemReader pemReader = new PemReader(stringReader);
        try {
            PemObject pemObject = pemReader.readPemObject();
            byte[] publicKeyBytes = pemObject.getContent();
            return (RSAKeyParameters) PublicKeyFactory.createKey(publicKeyBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        RsaEnvelope envelope = new RsaEnvelope("-----BEGIN PUBLIC KEY-----MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAtBHXIFq7BmxOZRg0nZZY9az/iBNHuYSXjlIee2fyiePfKaew4lRESgGPncZIGh+uJvk13bqvX/OaB5l0bKauoMHfkyk3T2wVEv0Fm7kJqp96x7A8RPHnpFyfQEz926qnuaxAUGZ/BhEp8r9GldqRVraN7botV70rO7EGsqCj4z1FZKpJ7HJvyHYPiTzho2ENDyMQ6Py2r1puUTeNZdS0RocJ2TvTTBVQ93X4exh81RqcG/BGlwygcbwSD8PeN2cftL1JLLGO7e8TqPW9lr5nR0IfbczyzZWX323iq80R2NyNtxWYOAQI1r+rJM/1lXtq1uNdKFfMpbZINkWAcYKTeatRhOgamGPlxdm+87tThG9Y5AbDlp7qeXk8HYwNeBdmjElTmcZlmWDgTVttQkqBKAfEA+HzAhpMa3ZvDuVny+wyl4ktcxpQ2H88vZzHR/Q0YKlfsRXQ5+UZha7Vm5VpeJ/eUBt6m3zTW3aZJ3bVZyLXlRog8qv8e6oxyP45vvG51c/WWFqnmaEXewrx1TbldAAAsRwhKv0PdLV9Popltug/+JY0aa2hbk+XEcza+TNarmchwFvFDnJsTv5Ml6oYBvgzevoghN+v1T4d5M2YMd1acaLQA790/P2HjgaFP2zIxkT8N/WE/deQbfH3fjfklqm4S6ccHqw5cRN2EHqyJmsCAwEAAQ==-----END PUBLIC KEY-----");
        System.out.println(Arrays.toString(envelope.create("Hello World".getBytes())));
    }
}
