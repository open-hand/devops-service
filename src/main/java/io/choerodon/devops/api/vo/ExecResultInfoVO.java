package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

public class ExecResultInfoVO {
    @ApiModelProperty("指令")
    private String command;
    @ApiModelProperty("标准输出")
    private String stdOut;
    @ApiModelProperty("标准错误输出")
    private String stdErr;
    @ApiModelProperty("退出码")
    private Integer exitCode;

    public String getStdOut() {
        return stdOut;
    }

    public ExecResultInfoVO setStdOut(String stdOut) {
        this.stdOut = stdOut;
        return this;
    }

    public String getStdErr() {
        return stdErr;
    }

    public ExecResultInfoVO setStdErr(String stdErr) {
        this.stdErr = stdErr;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public String toString() {
        return String.format("The command is %s, exitCode is %s, stdOut is %s, stdError is %s", command, exitCode, stdOut, stdErr);
    }
}
