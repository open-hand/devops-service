package io.choerodon.devops.infra.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import sun.misc.BASE64Decoder;

import io.choerodon.core.exception.CommonException;

/**
 * Creator: Runge
 * Date: 2018/9/10
 * Time: 09:21
 * Description:
 */
public class Base64Util {

    private static final BASE64Decoder decoder = new BASE64Decoder();

    private Base64Util() {
    }

    /**
     * decode base64
     *
     * @param encodedString base64 encoded String
     * @return ByteInputStream decoded
     */
    public static ByteArrayInputStream base64Decoder(String encodedString) {
        return new ByteArrayInputStream(Base64.getDecoder().decode(encodedString));
    }

    /**
     * return encoded base64 string
     *
     * @param plaintext The text which you want to encode to base64 text.
     * @return String encoded
     */
    public static String getBase64EncodedString(String plaintext) {
        return new String(Base64.getEncoder().encode(plaintext.getBytes()));
    }

    /**
     * return decoded base string
     *
     * @param cipherText The text which is encoded to base64 text.
     * @return String decoded
     */
    public static String getBase64DecodedString(String cipherText) {
        return new String(Base64.getDecoder().decode(cipherText.getBytes()));
    }

    public static String decodeBuffer(String encodeStr) {
        try {
            return new String(decoder.decodeBuffer(encodeStr), StandardCharsets.UTF_8);
        } catch (IOException e) {
           throw new CommonException(e);
        }
    }
}
