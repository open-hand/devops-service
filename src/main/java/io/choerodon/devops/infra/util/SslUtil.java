package io.choerodon.devops.infra.util;

import io.choerodon.core.exception.CommonException;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Sheep on 2019/5/30.
 */
public class SslUtil {

    private SslUtil() {

    }

    private static final Logger logger = LoggerFactory.getLogger(SslUtil.class);


    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * 默认是RSA/NONE/PKCS1Padding
     */
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    /**
     * RSA密钥长度必须是64的倍数，在512~65536之间。默认是1024
     */
    private static final int KEY_SIZE = 2048;

    /**
     * RSA最大加密明文大小:明文长度(bytes) <= 密钥长度(bytes)-11
     */
    private static final int MAX_ENCRYPT_BLOCK = KEY_SIZE / 8 - 11;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = KEY_SIZE / 8;


    public static PublicKey getPublicKey(File cert) {
        try (FileInputStream fin = new FileInputStream(cert);) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate crt = cf.generateCertificate(fin);
            return crt.getPublicKey();
        } catch (CertificateException | IOException e) {
            logger.info(e.getMessage(), e);
        }
        return null;
    }


    public static PrivateKey getPrivateKey(File key) {
        PrivateKey privKey = null;
        try (PemReader pemReader = new PemReader(new FileReader(key));) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] pemContent = pemObject.getContent();
            //支持从PKCS#1或PKCS#8 格式的私钥文件中提取私钥
            if (pemObject.getType().endsWith("RSA PRIVATE KEY")) {
                // 取得私钥  for PKCS#1
                RSAPrivateKey asn1PrivKey = RSAPrivateKey.getInstance(pemContent);
                RSAPrivateKeySpec rsaPrivKeySpec = new RSAPrivateKeySpec(asn1PrivKey.getModulus(), asn1PrivKey.getPrivateExponent());
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                privKey = keyFactory.generatePrivate(rsaPrivKeySpec);
            } else if (pemObject.getType().endsWith("PRIVATE KEY")) {
                //取得私钥 for PKCS#8
                PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(pemContent);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                privKey = kf.generatePrivate(privKeySpec);
            }
            return privKey;
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            return null;
        }
    }


    /**
     * 加密
     *
     * @param key
     * @param plainBytes
     * @return
     */
    public static byte[] encrypt(PublicKey key, byte[] plainBytes) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            int inputLen = plainBytes.length;
            if (inputLen <= MAX_ENCRYPT_BLOCK) {
                return cipher.doFinal(plainBytes);
            }

            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 数据太长对数据分段加密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(plainBytes, offSet, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(plainBytes, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
            return out.toByteArray();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 IOException | BadPaddingException e) {
            logger.info(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据公钥加密字符串
     *
     * @param key
     * @param plainText 需要加密的字符串
     * @return
     */
    public static String encrypt(PublicKey key, String plainText) {
        byte[] encodeBytes = encrypt(key, plainText.getBytes(DEFAULT_CHARSET));
        return Base64.encodeBase64String(encodeBytes);
    }

    /**
     * 解密
     *
     * @param key
     * @param encodedText
     * @return
     */
    public static String decrypt(PrivateKey key, byte[] encodedText) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            int inputLen = encodedText.length;

            if (inputLen <= MAX_DECRYPT_BLOCK) {
                return new String(cipher.doFinal(encodedText), DEFAULT_CHARSET);
            }


            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(encodedText, offSet, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(encodedText, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_DECRYPT_BLOCK;
            }
            return new String(out.toByteArray(), DEFAULT_CHARSET);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 IOException | BadPaddingException e) {
            logger.info(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据私钥解密加密过的字符串
     *
     * @param key
     * @param encodedText 加密过的字符串
     * @return 解密后的字符串
     */
    public static String decrypt(PrivateKey key, String encodedText) {
        byte[] bytes = Base64.decodeBase64(encodedText);
        return decrypt(key, bytes);
    }


    /**
     * 验证证书
     *
     * @param cert
     * @return
     */
    public static PublicKey validateCert(File cert) {
        if (cert == null) {
            throw new CommonException("devops.crt.file.not.exist");
        }
        PublicKey publicKey = getPublicKey(cert);
        if (publicKey == null) {
            throw new CommonException("devops.crt.file.format.error");
        }
        return publicKey;
    }

    /**
     * 验证私钥
     *
     * @param key
     * @return
     */
    public static PrivateKey validatePrivateKey(File key) {
        if (key == null) {
            throw new CommonException("devops.key.file.not.exist");
        }
        PrivateKey privateKey = getPrivateKey(key);
        if (privateKey == null) {
            throw new CommonException("devops.key.file.format.error");
        }
        return privateKey;
    }

    /**
     * 验证证书私钥是否匹配,如果不匹配返回错误消息
     *
     * @param cert
     * @param key
     * @return 错误消息
     */
    public static void validate(File cert, File key) {

        PublicKey publicKey = validateCert(cert);//验证证书
        PrivateKey privateKey = validatePrivateKey(key);//验证私钥

        String str = "testCert";//测试字符串
        String encryptStr = encrypt(publicKey, str);//根据证书公钥对字符串进行加密
        String decryptStr = decrypt(privateKey, encryptStr);//根据证书私钥对加密字符串进行解密
        if (!str.equals(decryptStr)) {//字符串根据证书公钥加密，私钥解密后不能还原说明证书与私钥不匹配
            throw new CommonException("devops.key.crt.not.equal");
        }
    }

    public static CertInfo parseCert(File certFile) {
        CertInfo certInfo = new CertInfo();
        try {
            // 从文件加载证书
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(Files.newInputStream(certFile.toPath()));

            // 获取证书的主题
            String subject = cert.getSubjectDN().getName();

            List<String> domains = new ArrayList<>();
            for (List<?> subjectAlternativeName : cert.getSubjectAlternativeNames()) {
                domains.add((String) subjectAlternativeName.get(1));
            }

            // 获取证书的有效期
            certInfo.setValidFrom(cert.getNotBefore());
            certInfo.setValidUntil(cert.getNotAfter());
            certInfo.setDomains(domains);
        } catch (CertificateException | IOException e) {
            throw new CommonException("error.parse.crt", e.getMessage());
        }
        return certInfo;
    }

    public static class CertInfo {
        private List<String> domains;
        private Date validFrom;
        private Date validUntil;

        public List<String> getDomains() {
            return domains;
        }

        public void setDomains(List<String> domains) {
            this.domains = domains;
        }

        public Date getValidFrom() {
            return validFrom;
        }

        public void setValidFrom(Date validFrom) {
            this.validFrom = validFrom;
        }

        public Date getValidUntil() {
            return validUntil;
        }

        public void setValidUntil(Date validUntil) {
            this.validUntil = validUntil;
        }
    }
}
