package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.host.ResourceUsageInfoVO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;

/**
 * 主机资源资源VO
 *
 * @author xingxingwu.hand-china.com 2021/07/13 17:02
 */
public class HostDetailResourceVO {
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("项目名称")
    private String projectName;
    @Encrypt
    @ApiModelProperty("主机id")
    private Long id;
    @ApiModelProperty("主机名称")
    private String name;
    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostStatus}
     */
    @ApiModelProperty("主机状态")
    private String hostStatus;
    @ApiModelProperty("主机ip")
    private String hostIp;
    @ApiModelProperty("ssh端口号")
    private Integer sshPort;
    @ApiModelProperty("主机cpu使用情况")
    private String cpu;
    @ApiModelProperty("主机内存使用情况")
    private String mem;
    @ApiModelProperty("主机硬盘使用情况")
    private String disk;

    public static HostDetailResourceVO build(@NotNull DevopsHostDTO hostDTO, ResourceUsageInfoVO resourceUsageInfoVO, String projectName) {
        HostDetailResourceVO hostResourceVO = new HostDetailResourceVO();
        hostResourceVO.projectId = hostDTO.getProjectId();
        hostResourceVO.projectName = projectName;
        hostResourceVO.id = hostDTO.getId();
        hostResourceVO.name = hostDTO.getName();
        hostResourceVO.hostStatus = hostDTO.getHostStatus();
        hostResourceVO.hostIp = hostDTO.getHostIp();
        hostResourceVO.sshPort = hostDTO.getSshPort();
        if (resourceUsageInfoVO != null) {
            hostResourceVO.cpu = resourceUsageInfoVO.getCpu();
            hostResourceVO.mem = resourceUsageInfoVO.getMem();
            hostResourceVO.disk = resourceUsageInfoVO.getDisk();
        }
        return hostResourceVO;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(String hostStatus) {
        this.hostStatus = hostStatus;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMem() {
        return mem;
    }

    public void setMem(String mem) {
        this.mem = mem;
    }

    public String getDisk() {
        return disk;
    }

    public void setDisk(String disk) {
        this.disk = disk;
    }
}
