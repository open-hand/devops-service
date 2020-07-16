package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author zmf
 * @since 2/18/20
 */
public class DevopsPolarisRecordRespVO {
    @Encrypt
    @ApiModelProperty("自增id")
    private Long id;

    @Encrypt
    @ApiModelProperty("纪录对象id(集群id或环境id)")
    private Long scopeId;

    @ApiModelProperty("纪录对象类型(cluster/env)")
    private String scope;

    @ApiModelProperty("操作状态")
    private String status;

    @ApiModelProperty("这次扫描开始时间")
    private Date scanDateTime;

    @ApiModelProperty("上次扫描结束时间")
    private Date lastScanDateTime;

    @ApiModelProperty("通过的检测项数量")
    private Long successes;

    @ApiModelProperty("警告的检测项数量")
    private Long warnings;

    @ApiModelProperty("错误的检测项数量")
    private Long errors;

    @ApiModelProperty("扫描出的集群版本")
    private String kubernetesVersion;

    @ApiModelProperty("pod数量")
    private Long pods;

    @ApiModelProperty("namespace数量")
    private Long namespaces;

    @ApiModelProperty("节点数量")
    private Long nodes;

    @ApiModelProperty("实例数量 / 只有环境类型的纪录有此字段")
    private Long instanceCount;

    @ApiModelProperty("健康检查的分数")
    private Long score;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScopeId() {
        return scopeId;
    }

    public void setScopeId(Long scopeId) {
        this.scopeId = scopeId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getScanDateTime() {
        return scanDateTime;
    }

    public void setScanDateTime(Date scanDateTime) {
        this.scanDateTime = scanDateTime;
    }

    public Date getLastScanDateTime() {
        return lastScanDateTime;
    }

    public void setLastScanDateTime(Date lastScanDateTime) {
        this.lastScanDateTime = lastScanDateTime;
    }

    public Long getSuccesses() {
        return successes;
    }

    public void setSuccesses(Long successes) {
        this.successes = successes;
    }

    public Long getWarnings() {
        return warnings;
    }

    public void setWarnings(Long warnings) {
        this.warnings = warnings;
    }

    public Long getErrors() {
        return errors;
    }

    public void setErrors(Long errors) {
        this.errors = errors;
    }

    public String getKubernetesVersion() {
        return kubernetesVersion;
    }

    public void setKubernetesVersion(String kubernetesVersion) {
        this.kubernetesVersion = kubernetesVersion;
    }

    public Long getPods() {
        return pods;
    }

    public void setPods(Long pods) {
        this.pods = pods;
    }

    public Long getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Long namespaces) {
        this.namespaces = namespaces;
    }

    public Long getNodes() {
        return nodes;
    }

    public void setNodes(Long nodes) {
        this.nodes = nodes;
    }

    public Long getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(Long instanceCount) {
        this.instanceCount = instanceCount;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }
}
