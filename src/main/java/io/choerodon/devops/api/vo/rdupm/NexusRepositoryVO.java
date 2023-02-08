package io.choerodon.devops.api.vo.rdupm;


/**
 * 制品库_nexus仓库信息表
 *
 * @author weisen.yang@hand-china.com 2020-03-27 11:43:00
 */

public class NexusRepositoryVO {

    /**
     * 表ID，主键，供其他表做外键
     */
    private Long repositoryId;
    /**
     * nexus服务配置ID: rdupm_nexus_server_config主键
     */
    private Long configId;
    /**
     * nexus仓库名称
     */
    private String neRepositoryName;

    private String name;

    private Long organizationId;

    private Long projectId;

    private Integer allowAnonymous;

    /**
     * 是否是关联仓库引入的。1 是；0 不是
     */
    private Integer isRelated;

    private Long tenantId;

    private String repoType;

    private String enableFlag;

    private String repoUrl;

    private NexusUserVO nexusUser;

    public NexusUserVO getNexusUser() {
        return nexusUser;
    }

    public void setNexusUser(NexusUserVO nexusUser) {
        this.nexusUser = nexusUser;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getNeRepositoryName() {
        return neRepositoryName;
    }

    public void setNeRepositoryName(String neRepositoryName) {
        this.neRepositoryName = neRepositoryName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(Integer allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public Integer getIsRelated() {
        return isRelated;
    }

    public void setIsRelated(Integer isRelated) {
        this.isRelated = isRelated;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public String getEnableFlag() {
        return enableFlag;
    }

    public void setEnableFlag(String enableFlag) {
        this.enableFlag = enableFlag;
    }
}
