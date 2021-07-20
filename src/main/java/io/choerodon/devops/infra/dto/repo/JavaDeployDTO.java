package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 10:24
 */
public class JavaDeployDTO {
    @ApiModelProperty("jar包下载信息")
    private JarPullInfoDTO jarPullInfoDTO;
    @ApiModelProperty("jar包名称")
    private String jarName;
    @ApiModelProperty("当前部署实例id")
    private String instanceId;
    @ApiModelProperty("部署jar命令")
    private String cmd;

    public JavaDeployDTO(JarPullInfoDTO jarPullInfoDTO, String jarName, String instanceId, String cmd) {
        this.jarPullInfoDTO = jarPullInfoDTO;
        this.jarName = jarName;
        this.instanceId = instanceId;
        this.cmd = cmd;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public JarPullInfoDTO getJarPullInfoDTO() {
        return jarPullInfoDTO;
    }

    public void setJarPullInfoDTO(JarPullInfoDTO jarPullInfoDTO) {
        this.jarPullInfoDTO = jarPullInfoDTO;
    }
}
