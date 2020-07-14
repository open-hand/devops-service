package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/14 20:47
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_pipeline_record_rel")
public class DevopsPipelineRecordRelDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long pipelineId;

    private Long ciPipelineRecordId;    // 纯cd流水线，这个值为0

    private Long cdPipelineRecordId;    // 纯ci流水线，这个值为0

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiPipelineRecordId() {
        return ciPipelineRecordId;
    }

    public void setCiPipelineRecordId(Long ciPipelineRecordId) {
        this.ciPipelineRecordId = ciPipelineRecordId;
    }

    public Long getCdPipelineRecordId() {
        return cdPipelineRecordId;
    }

    public void setCdPipelineRecordId(Long cdPipelineRecordId) {
        this.cdPipelineRecordId = cdPipelineRecordId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }
}
