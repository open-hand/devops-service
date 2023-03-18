package io.choerodon.devops.api.vo.pipeline;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class DevopsCiHostDeployInfoVO {
    @Encrypt
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Encrypt
    private Long hostId;

    private String deployType;  // 部署类型：新建实例 create 替换实例 update

    @Encrypt
    private Long appId;

    private String appName;

    private String appCode;
    private String hostDeployType;

    private String deployJson;

    private String preCommand;
    private String runCommand;
    private String postCommand;
    @ApiModelProperty("删除命令")
    private String killCommand;
    @ApiModelProperty("健康探针")
    private String healthProb;
    @ApiModelProperty("删除命令")
    private String dockerCommand;

    private String imageJobName;

    @ApiModelProperty("部署来源")
    private String deploySource;

    @ApiModelProperty("容器名称")
    private String containerName;

    @ApiModelProperty("关联的构建任务名称")
    private String pipelineTask;

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

    @ApiModelProperty("版本正则")
    private String versionRegular;

    @ApiModelProperty("dockercompose执行命令")
    private String dockerComposeRunCommand;

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDockerComposeRunCommand() {
        return dockerComposeRunCommand;
    }

    public void setDockerComposeRunCommand(String dockerComposeRunCommand) {
        this.dockerComposeRunCommand = dockerComposeRunCommand;
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

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
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

    public String getVersionRegular() {
        return versionRegular;
    }

    public void setVersionRegular(String versionRegular) {
        this.versionRegular = versionRegular;
    }

    public String getDeploySource() {
        return deploySource;
    }

    public void setDeploySource(String deploySource) {
        this.deploySource = deploySource;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getPipelineTask() {
        return pipelineTask;
    }

    public void setPipelineTask(String pipelineTask) {
        this.pipelineTask = pipelineTask;
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

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getHostDeployType() {
        return hostDeployType;
    }

    public void setHostDeployType(String hostDeployType) {
        this.hostDeployType = hostDeployType;
    }

    public String getDeployJson() {
        return deployJson;
    }

    public void setDeployJson(String deployJson) {
        this.deployJson = deployJson;
    }

    public String getPreCommand() {
        return preCommand;
    }

    public void setPreCommand(String preCommand) {
        this.preCommand = preCommand;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public void setRunCommand(String runCommand) {
        this.runCommand = runCommand;
    }

    public String getPostCommand() {
        return postCommand;
    }

    public void setPostCommand(String postCommand) {
        this.postCommand = postCommand;
    }

    public String getKillCommand() {
        return killCommand;
    }

    public void setKillCommand(String killCommand) {
        this.killCommand = killCommand;
    }

    public String getHealthProb() {
        return healthProb;
    }

    public void setHealthProb(String healthProb) {
        this.healthProb = healthProb;
    }

    public String getDockerCommand() {
        return dockerCommand;
    }

    public void setDockerCommand(String dockerCommand) {
        this.dockerCommand = dockerCommand;
    }

    public String getImageJobName() {
        return imageJobName;
    }

    public void setImageJobName(String imageJobName) {
        this.imageJobName = imageJobName;
    }

    public static class JarDeploy {
        @ApiModelProperty("部署来源：matchDeploy(匹配部署)/pipelineDeploy(流水线部署)")
        private String deploySource;

        @ApiModelProperty("流水线部署 流水线任务名称")
        private String pipelineTask;

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

        @ApiModelProperty("版本正则")
        private String versionRegular;

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

        public String getVersionRegular() {
            return versionRegular;
        }

        public void setVersionRegular(String versionRegular) {
            this.versionRegular = versionRegular;
        }

        public Long getRepositoryId() {
            return repositoryId;
        }

        public void setRepositoryId(Long repositoryId) {
            this.repositoryId = repositoryId;
        }

        public String getDeploySource() {
            return deploySource;
        }

        public void setDeploySource(String deploySource) {
            this.deploySource = deploySource;
        }

        public String getPipelineTask() {
            return pipelineTask;
        }

        public void setPipelineTask(String pipelineTask) {
            this.pipelineTask = pipelineTask;
        }
    }

    public static class ImageDeploy {
        @ApiModelProperty("部署来源：matchDeploy(匹配部署)/pipelineDeploy(流水线部署)")
        private String deploySource;

        @ApiModelProperty("流水线部署 流水线任务名称")
        private String pipelineTask;

        @ApiModelProperty("仓库名")
        private String repoName;

        @ApiModelProperty("仓库类型")
        private String repoType;

        @Encrypt
        @ApiModelProperty("仓库Id")
        private Long repoId;

        @ApiModelProperty("镜像名称")
        private String imageName;

        @Encrypt
        @ApiModelProperty("镜像Id")
        private Long imageId;

        @ApiModelProperty("匹配类型")
        private String matchType;

        @ApiModelProperty("匹配内容")
        private String matchContent;

        @ApiModelProperty("部署values")
        private String value;

        @ApiModelProperty("容器名称")
        private String containerName;

        public String getRepoName() {
            return repoName;
        }

        public void setRepoName(String repoName) {
            this.repoName = repoName;
        }

        public Long getImageId() {
            return imageId;
        }

        public void setImageId(Long imageId) {
            this.imageId = imageId;
        }

        public String getMatchType() {
            return matchType;
        }

        public void setMatchType(String matchType) {
            this.matchType = matchType;
        }

        public String getMatchContent() {
            return matchContent;
        }

        public void setMatchContent(String matchContent) {
            this.matchContent = matchContent;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Long getRepoId() {
            return repoId;
        }

        public void setRepoId(Long repoId) {
            this.repoId = repoId;
        }

        public String getImageName() {
            return imageName;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }

        public String getRepoType() {
            return repoType;
        }

        public void setRepoType(String repoType) {
            this.repoType = repoType;
        }

        public String getDeploySource() {
            return deploySource;
        }

        public void setDeploySource(String deploySource) {
            this.deploySource = deploySource;
        }

        public String getPipelineTask() {
            return pipelineTask;
        }

        public void setPipelineTask(String pipelineTask) {
            this.pipelineTask = pipelineTask;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }
    }
}
