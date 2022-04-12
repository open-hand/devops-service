package io.choerodon.devops.api.vo.host;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/2 10:52
 */

public class DevopsDockerInstanceVO extends DevopsHostInstanceVO {

    @ApiModelProperty("容器id")
    private String containerId;
    @ApiModelProperty("镜像名")
    private String image;
    @ApiModelProperty("端口映射列表")
    private String ports;
    /**
     * {@link io.choerodon.devops.infra.enums.deploy.DockerInstanceStatusEnum}
     */

    @ApiModelProperty("部署来源")
    private String sourceType;


    private List<DockerPortMapping> portMappingList;

    private IamUserDTO deployer;
    private Long createdBy;


    @ApiModelProperty("主机应用id")
    @Encrypt
    private Long appId;

    @ApiModelProperty("镜像仓库的类型，默认的还是自定义的")
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
    @ApiModelProperty("操作命令")
    private DevopsHostCommandDTO devopsHostCommandDTO;

    public String getDockerCommand() {
        return dockerCommand;
    }

    public void setDockerCommand(String dockerCommand) {
        this.dockerCommand = dockerCommand;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getRepoId() {
        return repoId;
    }

    public void setRepoId(Long repoId) {
        this.repoId = repoId;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public IamUserDTO getDeployer() {
        return deployer;
    }

    public void setDeployer(IamUserDTO deployer) {
        this.deployer = deployer;
    }


    public DevopsHostCommandDTO getDevopsHostCommandDTO() {
        return devopsHostCommandDTO;
    }

    public void setDevopsHostCommandDTO(DevopsHostCommandDTO devopsHostCommandDTO) {
        this.devopsHostCommandDTO = devopsHostCommandDTO;
    }

    public List<DockerPortMapping> getPortMappingList() {
        return portMappingList;
    }

    public void setPortMappingList(List<DockerPortMapping> portMappingList) {
        this.portMappingList = portMappingList;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
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


    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

}
