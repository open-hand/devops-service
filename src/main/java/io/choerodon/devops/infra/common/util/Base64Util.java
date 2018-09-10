package io.choerodon.devops.infra.common.util;

import java.util.Arrays;
import java.util.Base64;

import org.nutz.lang.util.ByteInputStream;

/**
 * Creator: Runge
 * Date: 2018/9/10
 * Time: 09:21
 * Description:
 */
public class Base64Util {

    /**
     * decode base64
     *
     * @param encodedString base64 encoded String
     * @return String decoded
     */
    public static ByteInputStream base64Decoder(String encodedString) {
        return new ByteInputStream(Base64.getDecoder().decode(encodedString));
    }
}
