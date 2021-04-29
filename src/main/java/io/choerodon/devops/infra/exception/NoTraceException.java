package io.choerodon.devops.infra.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringUtils;

import io.choerodon.core.exception.CommonException;

/**
 * 只打印code而没有trace信息的异常，
 * 用于在saga的异常信息中去除trace
 *
 * @author zmf
 * @since 2021/1/29
 */
public class NoTraceException extends CommonException {

    public NoTraceException(String code, Object... parameters) {
        super(code, parameters);
    }

    public NoTraceException(String code, Throwable cause, Object... parameters) {
        super(code, cause, parameters);
    }

    public NoTraceException(String code, Throwable cause) {
        super(code, cause);
    }

    public NoTraceException(Throwable cause, Object... parameters) {
        super(cause, parameters);
    }

    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream stream) {
        stream.println(getCode());
        stream.flush();
    }

    @Override
    public void printStackTrace(PrintWriter writer) {
        writer.println(getCode());
        writer.flush();
    }

    @Override
    public String getTrace() {
        return StringUtils.EMPTY;
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
    }

    @Override
    public synchronized Throwable getCause() {
        return null;
    }
}
