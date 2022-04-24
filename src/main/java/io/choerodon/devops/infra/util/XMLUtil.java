package io.choerodon.devops.infra.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangxiang on 2021/2/2
 */
public class XMLUtil {

    private XMLUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtil.class);

    public static Object convertXmlFileToObject(Class<?> clazz, String settings) {

        Object xmlObject = null;
        try {

            JAXBContext context = JAXBContext.newInstance(clazz);

            Unmarshaller unmarshaller = context.createUnmarshaller();

            xmlObject = unmarshaller.unmarshal(getStringStream(settings));

        } catch (Exception e) {
            LOGGER.error("error.xml.to.object", e);
        }
        return xmlObject;
    }

    public static InputStream getStringStream(String sInputString) {
        if (sInputString != null && !sInputString.trim().equals("")) {
            try {
                return new ByteArrayInputStream(sInputString.getBytes());
            } catch (Exception ex) {
                LOGGER.error("error.get.stream", ex);
            }
        }
        return null;
    }
}
