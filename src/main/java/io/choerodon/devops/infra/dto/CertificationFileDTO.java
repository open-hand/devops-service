package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

<<<<<<< HEAD
import io.choerodon.mybatis.entity.BaseDTO;
=======
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
>>>>>>> [ADD] add ModifyAudit VersionAudit for table dto

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:51
 * Description:
 */

@ModifyAudit
@VersionAudit
@Table(name = "devops_certification_file")
public class CertificationFileDTO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String certFile;
    private String keyFile;

    /**
     * construct cert file
     *
     * @param certFile cert file
     * @param keyFile  private key file
     */
    public CertificationFileDTO(String certFile, String keyFile) {
        this.certFile = certFile;
        this.keyFile = keyFile;
    }

    public CertificationFileDTO() {

    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getCertFile() {
        return certFile;
    }

    public void setCertFile(String certFile) {
        this.certFile = certFile;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }
}
