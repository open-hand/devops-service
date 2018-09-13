package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:51
 * Description:
 */
@ModifyAudit
@Table(name = "devops_certification_file")
public class CertificationFileDO {
    @Id
    @GeneratedValue
    private Long id;

    private Long certId;
    private String certFile;
    private String keyFile;

    /**
     * construct cert file
     *
     * @param certId   certification id
     * @param certFile cert file
     * @param keyFile  private key file
     */
    public CertificationFileDO(Long certId, String certFile, String keyFile) {
        this.certId = certId;
        this.certFile = certFile;
        this.keyFile = keyFile;
    }

    public CertificationFileDO() {

    }

    public CertificationFileDO(Long certId) {
        this.certId = certId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCertId() {
        return certId;
    }

    public void setCertId(Long certId) {
        this.certId = certId;
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
