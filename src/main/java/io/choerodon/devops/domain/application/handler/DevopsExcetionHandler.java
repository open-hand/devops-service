package io.choerodon.devops.domain.application.handler;

import io.choerodon.core.exception.ExceptionResponse;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.resource.handler.ControllerExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;

/**
 * @author zmf
 */
@ControllerAdvice
public class DevopsExcetionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @Autowired
    private MessageSource messageSource;


    /**
     * 处理CI流水线中获取应用版本信息接口的异常 (POST /ci)
     *
     * @param exception 异常
     * @return 400 的状态码
     */
    @ExceptionHandler(DevopsCiInvalidException.class)
    public ResponseEntity<ExceptionResponse> process(DevopsCiInvalidException exception) {
        LOGGER.info("exception info {}", exception.getTrace());
        String message = null;
        try {
            message = messageSource.getMessage(exception.getCode(), exception.getParameters(), locale());
        } catch (Exception e) {
            LOGGER.trace("exception message {}", exception);
        }
        return new ResponseEntity<>(
                new ExceptionResponse(true, exception.getCode(), message != null ? message : exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * 返回用户的语言类型
     *
     * @return Locale
     */
    private Locale locale() {
        CustomUserDetails details = DetailsHelper.getUserDetails();
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (details != null && "en_US".equals(details.getLanguage())) {
            locale = Locale.US;
        }
        return locale;
    }
}
