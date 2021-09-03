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

    @ApiModelProperty("主机部署类型 jar/customize")
    // HostDeployType
    private String hostDeployType;

    @ApiModelProperty("jar部署详情")
    private JarDeploy jarDeploy;

    @ApiModelProperty("自定义部署customize详情")
    private Customize customize;

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

        @ApiModelProperty("工作目录,默认值/temp")
        private String workingPath;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

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
