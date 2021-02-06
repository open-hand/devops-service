package io.choerodon.devops.api.vo.test;

import java.util.Set;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class ApiTestExecuteVO {
    @ApiModelProperty(name = "执行任务id")
    @Encrypt
    private Long taskId;

    @ApiModelProperty(name = "执行用例id")
    @Encrypt
    private Set<Long> caseIds;

    @ApiModelProperty(name = "执行配置id")
    @Encrypt
    private Long configId;

    @ApiModelProperty(name = "请求头")
    private String requestHeader;

    @ApiModelProperty(name = "用户自定义变量")
    private String userDefinedVariable;

    @ApiModelProperty(name = "域名")
    private String domain;
    @ApiModelProperty(name = "协议")
    private String protocol;

    @ApiModelProperty(name = "请求出现错误后的执行策略")
    private String actionAfterError;

    public ApiTestExecuteVO() {
    }

    public ApiTestExecuteVO(String requestHeader, String userDefinedVariable, String domain, String protocol, String actionAfterError) {
        this.requestHeader = requestHeader;
        this.userDefinedVariable = userDefinedVariable;
        this.domain = domain;
        this.protocol = protocol;
        this.actionAfterError = actionAfterError;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Set<Long> getCaseIds() {
        return caseIds;
    }

    public void setCaseIds(Set<Long> caseIds) {
        this.caseIds = caseIds;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
    }

    public String getUserDefinedVariable() {
        return userDefinedVariable;
    }

    public void setUserDefinedVariable(String userDefinedVariable) {
        this.userDefinedVariable = userDefinedVariable;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getActionAfterError() {
        return actionAfterError;
    }

    public ApiTestExecuteVO setActionAfterError(String actionAfterError) {
        this.actionAfterError = actionAfterError;
        return this;
    }
}
