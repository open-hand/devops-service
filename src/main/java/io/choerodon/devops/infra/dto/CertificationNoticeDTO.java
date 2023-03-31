package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created lihao
 * Date: 2023/03/27
 * Time: 19:51
 * Description:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_certification_notice")
public class CertificationNoticeDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String type;
    private Long objectId;
    private Long certificationId;

    public CertificationNoticeDTO() {
    }

    public CertificationNoticeDTO(String type, Long objectId, Long certificationId) {
        this.type = type;
        this.objectId = objectId;
        this.certificationId = certificationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getCertificationId() {
        return certificationId;
    }

    public void setCertificationId(Long certificationId) {
        this.certificationId = certificationId;
    }
}
