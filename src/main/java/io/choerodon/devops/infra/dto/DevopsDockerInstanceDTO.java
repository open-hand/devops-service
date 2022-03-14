package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 11:53
 */
@Table(name = "devops_docker_instance")
@ModifyAudit
@VersionAudit
public class DevopsDockerInstanceDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Encrypt
    @ApiModelProperty("主机id")
    private Long hostId;
    @ApiModelProperty("容器名")
    private String name;
    @ApiModelProperty("容器id")
    private String containerId;
    @ApiModelProperty("镜像名")
    private String image;
    @ApiModelProperty("端口映射列表")
    private String ports;
    /**
     * {@link io.choerodon.devops.infra.enums.deploy.DockerInstanceStatusEnum}
     */
    @ApiModelProperty("容器状态")
    private String status;
    /**
     * {@link io.choerodon.devops.infra.enums.AppSourceType}
     */
    @ApiModelProperty("部署来源,当前项目还是其他来源")
    private String sourceType;

    @ApiModelProperty("主机应用id")
    @Encrypt
    private Long appId;

    @ApiModelProperty("镜像仓库的类型，默认的还是自定义的")
    /**
     * {@link io.choerodon.devops.infra.enums.host.DevopsHostDeployType}
     */
    private String repoType;

    @ApiModelProperty("仓库的名称，默认的才有")
    private String repoName;

    @ApiModelProperty("仓库的id,默认的才有")
    private Long repoId;

    @ApiModelProperty("自定义仓库的用户名")
    private String userName;

    @ApiModelProperty("自定义仓库的密码")
    private String passWord;

    @ApiModelProperty("自定义仓库是否是私库")
    private Boolean privateRepository;

    @ApiModelProperty("默认仓库的镜像名称")
    private String imageName;

    @ApiModelProperty("默认仓库的镜像版本")
    private String tag;

    @ApiModelProperty("命令框的命令")
    private String dockerCommand;

    public String getDockerCommand() {
        return dockerCommand;
    }

    public void setDockerCommand(String dockerCommand) {
        this.dockerCommand = dockerCommand;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public Boolean getPrivateRepository() {
        return privateRepository;
    }

    public void setPrivateRepository(Boolean privateRepository) {
        this.privateRepository = privateRepository;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Long getRepoId() {
        return repoId;
    }

    public void setRepoId(Long repoId) {
        this.repoId = repoId;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public DevopsDockerInstanceDTO() {
    }

    public DevopsDockerInstanceDTO(Long hostId, String name) {
        this.hostId = hostId;
        this.name = name;
    }

    public DevopsDockerInstanceDTO(Long hostId, String name, String image, String status, String sourceType, Long appId) {
        this.hostId = hostId;
        this.name = name;
        this.image = image;
        this.status = status;
        this.sourceType = sourceType;
        this.appId = appId;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
