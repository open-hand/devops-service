package io.choerodon.devops.infra.common.util;

import java.util.Base64;

import org.nutz.lang.util.ByteInputStream;

/**
 * Creator: Runge
 * Date: 2018/9/10
 * Time: 09:21
 * Description:
 */
public class Base64Util {

    private Base64Util() {
    }

    /**
     * decode base64
     *
     * @param encodedString base64 encoded String
     * @return ByteInputStream decoded
     */
    public static ByteInputStream base64Decoder(String encodedString) {
        return new ByteInputStream(Base64.getDecoder().decode(encodedString));
    }

    /**
     * return decode base64 string
     *
     * @param plaintext The text which you want to encode to base64 text.
     * @return String decoded
     */
    public static String getBase64EncodedString(String plaintext) {
        return new String(Base64.getEncoder().encode(plaintext.getBytes()));
    }

    /**
     * return encode base string
     *
     * @param ciphetext The text which is encoded to base64 text.
     * @return String encoded
     */
    public static String getBase64DecodedString(String ciphetext) {
        return new String(Base64.getDecoder().decode(ciphetext.getBytes()));
    }
}
