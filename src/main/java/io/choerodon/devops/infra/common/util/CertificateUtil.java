package io.choerodon.devops.infra.common.util;

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.nutz.lang.stream.StringInputStream;

/**
 * Creator: Runge
 * Date: 2018/9/10
 * Time: 09:21
 * Description:
 */
public class CertificateUtil {

    /**
     * decode certificate
     *
     * @param encodedCert encoded cert string
     * @return X509Certificate
     * @throws CertificateException CertificateException
     */
    public static X509Certificate decodeCert(InputStream encodedCert) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(encodedCert);
    }
}
