package io.choerodon.devops.domain.application.handler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import io.choerodon.core.exception.CommonException;

public class GitOpsExplainException extends CommonException {

    private final transient Object[] parameters;
    private String code;
    private String filePath;
    private String errorCode;

    public GitOpsExplainException(String code, Object... parameters) {
        super(code);
        this.parameters = parameters;
        this.code = code;
    }

    public GitOpsExplainException(String code, String filePath) {
        super(code);
        this.code = code;
        this.filePath = filePath;
        this.parameters = new Object[0];
    }

    public GitOpsExplainException(String code, String filePath, Object... parameters) {
        super(code);
        this.parameters = parameters;
        this.code = code;
        this.filePath = filePath;
    }

    public GitOpsExplainException(String code, String filePath, String errorCode, Object... parameters) {
        super(code);
        this.parameters = parameters;
        this.errorCode = errorCode;
        this.code = code;
        this.filePath = filePath;
    }

    public GitOpsExplainException(String code, Throwable cause, Object... parameters) {
        super(code, cause);
        this.parameters = parameters;
        this.code = code;
    }

    public GitOpsExplainException(String code, Throwable cause) {
        super(code, cause);
        this.code = code;
        this.parameters = new Object[0];
    }

    public GitOpsExplainException(Throwable cause, Object... parameters) {
        super(cause);
        this.parameters = parameters;
    }

    @Override
    public Object[] getParameters() {
        return this.parameters;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getTrace() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        this.printStackTrace(ps);
        ps.flush();
        return new String(baos.toByteArray());
    }

    @Override
    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new LinkedHashMap();
        map.put("code", this.code);
        map.put("message", super.getMessage());
        return map;
    }

    public String getFilePath() {
        return filePath;
    }


    public String getErrorCode() {
        return errorCode;
    }


}
