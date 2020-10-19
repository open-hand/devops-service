package io.choerodon.devops.api.vo.deploy;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.HostConnectionVO;

/**
 * @author scp
 * @date 2020/6/30
 * @description 主机部署配置信息
 */
public class HostDeployConfigVO {
    @ApiModelProperty("主机详情")
    private HostConnectionVO hostConnectionVO;

    @ApiModelProperty("主机部署类型 image/jar/customize")
    // HostDeployType
    private String hostDeployType;

    @ApiModelProperty("镜像部署详情")
    private ImageDeploy imageDeploy;

    @ApiModelProperty("jar部署详情")
    private JarDeploy jarDeploy;

    @ApiModelProperty("自定义部署customize详情")
    private Customize customize;

    public static class ImageDeploy {

        @ApiModelProperty("仓库名")
        private String repoName;

        @ApiModelProperty("仓库类型")
        private String repoType;

        @Encrypt
        @ApiModelProperty("仓库Id")
        private String repoId;

        @ApiModelProperty("镜像名称")
        private String imageName;

        @Encrypt
        @ApiModelProperty("镜像Id")
        private Long imageId;


        @ApiModelProperty("镜像版本")
        private String tag;

        @ApiModelProperty("部署values")
        private String value;

        @ApiModelProperty("容器名称")
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
        private String serverName;
        @ApiModelProperty("仓库名")
        private String neRepositoryName;

        @Encrypt
        @ApiModelProperty("仓库id")
        private Long repositoryId;

        @ApiModelProperty("groupId")
        private String groupId;

        @ApiModelProperty("artifactId")
        private String artifactId;

        @ApiModelProperty("版本")
        private String version;

        @ApiModelProperty("部署values")
        private String value;

        @ApiModelProperty("工作目录,默认值/temp")
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

    public static class Customize {

        @ApiModelProperty("部署values")
        private String values;

        public String getValues() {
            return values;
        }

        public void setValues(String values) {
            this.values = values;
        }
    }

    public String getHostDeployType() {
        return hostDeployType;
    }

    public void setHostDeployType(String hostDeployType) {
        this.hostDeployType = hostDeployType;
    }

    public Customize getCustomize() {
        return customize;
    }

    public void setCustomize(Customize customize) {
        this.customize = customize;
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
}
