package io.choerodon.devops.api.vo.deploy;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.api.vo.HostConnectionVO;

/**
 * @author scp
 * @date 2020/6/30
 * @description 主机部署配置信息
 */
public class DeployConfigVO {
    @ApiModelProperty("主机详情")
    @Valid
    @NotNull(message = "error.hostConnectionVO.is.null")
    private HostConnectionVO hostConnectionVO;

    @ApiModelProperty("部署类型 env/host")
    @NotNull(message = "error.deployType.is.null")
    private String deployType;

    @ApiModelProperty("镜像部署详情")
    @Valid
    private ImageDeploy imageDeploy;

    @ApiModelProperty("jar部署详情")
    @Valid
    private JarDeploy jarDeploy;

    @ApiModelProperty("部署对象类型 image/jar")
    private String deployObjectType;
    @ApiModelProperty("环境部署详情")
    @Valid
    private AppServiceDeployVO appServiceDeployVO;

    public static class ImageDeploy {

        @ApiModelProperty("仓库名")
        @NotNull(message = "error.repoName.is.null")
        private String repoName;

        @ApiModelProperty("仓库类型")
        @NotNull(message = "error.repoType.is.null")
        private String repoType;

        @Encrypt
        @ApiModelProperty("仓库Id")
        @NotNull(message = "error.repoId.is.null")
        private String repoId;

        @ApiModelProperty("镜像名称")
        @NotNull(message = "error.imageName.is.null")
        private String imageName;

        @Encrypt
        @ApiModelProperty("镜像Id")
        @NotNull(message = "error.imageId.is.null")
        private Long imageId;


        @ApiModelProperty("镜像版本")
        @NotNull(message = "error.tag.is.null")
        private String tag;

        @ApiModelProperty("部署values")
        @NotNull(message = "error.value.is.null")
        private String value;

        @ApiModelProperty("容器名称")
        @NotNull(message = "error.containerName.is.null")
        private String containerName;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getRepoType() {
            return repoType;
        }

        public void setRepoType(String repoType) {
            this.repoType = repoType;
        }

        public String getRepoId() {
            return repoId;
        }

        public void setRepoId(String repoId) {
            this.repoId = repoId;
        }

        public String getRepoName() {
            return repoName;
        }

        public void setRepoName(String repoName) {
            this.repoName = repoName;
        }

        public String getImageName() {
            return imageName;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        public Long getImageId() {
            return imageId;
        }

        public void setImageId(Long imageId) {
            this.imageId = imageId;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    public static class JarDeploy {

        @ApiModelProperty("服务名")
        @NotNull(message = "error.serverName.is.null")
        private String serverName;
        @ApiModelProperty("仓库名")
        private String neRepositoryName;

        @Encrypt
        @ApiModelProperty("仓库id")
        @NotNull(message = "error.repositoryId.is.null")
        private Long repositoryId;

        @ApiModelProperty("groupId")
        @NotNull(message = "error.groupId.is.null")
        private String groupId;

        @ApiModelProperty("artifactId")
        @NotNull(message = "error.artifactId.is.null")
        private String artifactId;

        @ApiModelProperty("版本")
        @NotNull(message = "error.version.is.null")
        private String version;

        @ApiModelProperty("部署values")
        @NotNull(message = "error.value.is.null")
        private String value;

        @ApiModelProperty("工作目录,默认值/temp")
        @NotNull(message = "error.workingPath.is.null")
        private String workingPath;

        public String getWorkingPath() {
            return workingPath;
        }

        public void setWorkingPath(String workingPath) {
            this.workingPath = workingPath;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getNeRepositoryName() {
            return neRepositoryName;
        }

        public void setNeRepositoryName(String neRepositoryName) {
            this.neRepositoryName = neRepositoryName;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Long getRepositoryId() {
            return repositoryId;
        }

        public void setRepositoryId(Long repositoryId) {
            this.repositoryId = repositoryId;
        }
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public ImageDeploy getImageDeploy() {
        return imageDeploy;
    }

    public void setImageDeploy(ImageDeploy imageDeploy) {
        this.imageDeploy = imageDeploy;
    }

    public JarDeploy getJarDeploy() {
        return jarDeploy;
    }

    public void setJarDeploy(JarDeploy jarDeploy) {
        this.jarDeploy = jarDeploy;
    }

    public HostConnectionVO getHostConnectionVO() {
        return hostConnectionVO;
    }

    public void setHostConnectionVO(HostConnectionVO hostConnectionVO) {
        this.hostConnectionVO = hostConnectionVO;
    }

    public AppServiceDeployVO getAppServiceDeployVO() {
        return appServiceDeployVO;
    }

    public void setAppServiceDeployVO(AppServiceDeployVO appServiceDeployVO) {
        this.appServiceDeployVO = appServiceDeployVO;
    }

    public String getDeployObjectType() {
        return deployObjectType;
    }

    public void setDeployObjectType(String deployObjectType) {
        this.deployObjectType = deployObjectType;
    }
}
