package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author scp
 * @date 2020/6/30
 * @description 主机部署配置信息
 */
public class CdHostDeployConfigVO {
    @ApiModelProperty("主机详情")
    private HostConnectionVO hostConnectionVO;

    @ApiModelProperty("应用名称")
    private String appName;
    @ApiModelProperty("应用编码")
    private String appCode;

    @ApiModelProperty("主机部署类型 jar/customize")
    // HostDeployType
    private String hostDeployType;

    @Encrypt
    @ApiModelProperty("主机ID")
    private Long hostId;

    @ApiModelProperty("jar部署详情")
    private JarDeploy jarDeploy;

    @ApiModelProperty("前置命令")
    private String preCommand;
    @ApiModelProperty("启动命令")
    private String runAppCommand;
    @ApiModelProperty("后置命令")
    private String postCommand;

    public static class JarDeploy {
        @ApiModelProperty("部署来源：matchDeploy(匹配部署)/pipelineDeploy(流水线部署)")
        private String deploySource;

        @ApiModelProperty("流水线部署 流水线任务名称")
        private String pipelineTask;

        @ApiModelProperty("服务名")
        private String serverName;
        @ApiModelProperty("仓库名")
        private String neRepositoryName;

        @ApiModelProperty("实例名")
        private String name;

        @Encrypt
        @ApiModelProperty("仓库id")
        private Long repositoryId;

        @ApiModelProperty("groupId")
        private String groupId;

        @ApiModelProperty("artifactId")
        private String artifactId;

        @ApiModelProperty("版本正则")
        private String versionRegular;

        @ApiModelProperty("部署values")
        private String value;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

    public String getPreCommand() {
        return preCommand;
    }

    public void setPreCommand(String preCommand) {
        this.preCommand = preCommand;
    }

    public String getRunAppCommand() {
        return runAppCommand;
    }

    public void setRunAppCommand(String runAppCommand) {
        this.runAppCommand = runAppCommand;
    }

    public String getPostCommand() {
        return postCommand;
    }

    public void setPostCommand(String postCommand) {
        this.postCommand = postCommand;
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

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getHostDeployType() {
        return hostDeployType;
    }

    public void setHostDeployType(String hostDeployType) {
        this.hostDeployType = hostDeployType;
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
