package io.choerodon.devops.infra.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.springframework.util.StringUtils;

/**
 * @author zmf
 * @since 10/24/19
 */
public class LogUtil {
    private LogUtil() {
    }

    /**
     * This is used to cut and drop the content that are longer than the specified max length.
     * For example,
     * you can use it to save message in db to ensure that the content won't overflow the database field capacity.
     *
     * @param content   content
     * @param maxLength max length to return
     * @return the result after cut
     */
    public static String cutOutString(String content, int maxLength) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        if (maxLength <= 0) {
            return content;
        }
        return content.length() > maxLength ? content.substring(0, maxLength) : content;
    }

    /**
     * read the content of the throwable to string
     *
     * @param throwable the throwable to be read
     * @return the content
     */
    public static String readContentOfThrowable(Throwable throwable) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(byteArrayOutputStream);
        throwable.printStackTrace(ps);
        ps.flush();
        return new String(byteArrayOutputStream.toByteArray());
    }

    /**
     * log that the object with a certain id is null in info level.
     *
     * @param objectType objectType (AppService, Project, DevopsEnvCommand)
     * @param objectId   id
     * @param logger     LOGGER
     */
    public static void loggerInfoObjectNullWithId(String objectType, Long objectId, Logger logger) {
        logger.info("{} is null with id {}", objectType, objectId);
    }

    /**
     * log that the object with a certain id is null in warn level.
     *
     * @param objectType objectType (AppService, Project, DevopsEnvCommand)
     * @param objectId   id
     * @param logger     LOGGER
     */
    public static void loggerWarnObjectNullWithId(String objectType, Long objectId, Logger logger) {
        logger.warn("{} is null with id {}", objectType, objectId);
    }

    public static String deleteNewLine(String value) {
        return value.replace("\n", "");
    }
}
