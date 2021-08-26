package io.choerodon.devops.api.vo;

import io.choerodon.devops.api.vo.host.ResourceUsageInfoVO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.validation.constraints.NotNull;

/**
 * 主机资源资源VO
 *
 * @author xingxingwu.hand-china.com 2021/07/13 17:02
 */
public class HostDetailResourceVO {
    private Long projectId;
    private String projectName;
    @Encrypt
    private Long id;
    private String name;
    /**
     * {@link io.choerodon.devops.infra.enums.DevopsHostStatus}
     */
    private String hostStatus;
    private String hostIp;
    private Integer sshPort;
    private String cpu;
    private String mem;
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
