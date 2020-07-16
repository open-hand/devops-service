package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 发送Polaris扫描请求时的数据结构
 *
 * @author zmf
 * @since 2/17/20
 */
public class ClusterPolarisScanningVO {
    @Encrypt
    @ApiModelProperty("扫描关联的扫描纪录id")
    private Long recordId;
    @ApiModelProperty("为null时表示扫描整个集群所有的namespace，有值时表示扫描一个指定的namespace")
    private String namespace;

    public ClusterPolarisScanningVO() {
    }

    public ClusterPolarisScanningVO(Long recordId, String namespace) {
        this.recordId = recordId;
        this.namespace = namespace;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return "ClusterPolarisScanningVO{" +
                "recordId=" + recordId +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
