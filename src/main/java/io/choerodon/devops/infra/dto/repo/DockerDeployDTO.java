package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 15:44
 */
public class DockerDeployDTO {
    @ApiModelProperty("实例id")
    private String instanceId;
    @ApiModelProperty("镜像地址")
    private String image;
    @ApiModelProperty("容器名")
    private String containerName;
    @ApiModelProperty("容器id")
    private String containerId;
    @ApiModelProperty("镜像拉取账户信息")
    private DockerPullAccountDTO dockerPullAccountDTO;
    @ApiModelProperty("应用code")
    private String appCode;
    @ApiModelProperty("应用版本")
    private String version;

    private String repoName;

    private String repoType;
    @Encrypt
    private Long repoId;

    @ApiModelProperty("默认仓库的镜像名称")
    private String imageName;

    @ApiModelProperty("默认仓库的镜像版本")
    private String tag;

    @ApiModelProperty("自定义仓库的用户名")
    private String userName;

    @ApiModelProperty("自定义仓库的密码")
    private String passWord;

    @ApiModelProperty("自定义仓库是否是私库")
    private Boolean privateRepository;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    private String cmd;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public DockerPullAccountDTO getDockerPullAccountDTO() {
        return dockerPullAccountDTO;
    }

    public void setDockerPullAccountDTO(DockerPullAccountDTO dockerPullAccountDTO) {
        this.dockerPullAccountDTO = dockerPullAccountDTO;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
}
