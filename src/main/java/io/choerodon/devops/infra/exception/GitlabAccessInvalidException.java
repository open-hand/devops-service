package io.choerodon.devops.infra.exception;

import io.choerodon.core.exception.CommonException;

/**
 * @author scp
 * @date 2020/6/11
 * @description
 */
public class GitlabAccessInvalidException extends CommonException {

    private final transient Object[] parameters;
    private String code;

    public GitlabAccessInvalidException(String code, Object... parameters) {
        super(code);
        this.parameters = parameters;
        this.code = code;
    }

    @Override
    public Object[] getParameters() {
        return this.parameters;
    }

    @Override
    public String getCode() {
        return this.code;
    }
}
