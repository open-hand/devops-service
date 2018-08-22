package io.choerodon.devops.infra.dataobject;

import com.google.gson.Gson;
import io.choerodon.mybatis.annotation.ModifyAudit;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:51
 * Description:
 */

@ModifyAudit
@Table(name = "devops_certification")
public class CertificationDO {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private Long envId;
    private String domains;
    private String status;

    public CertificationDO() {
    }

    /**
     * Certification constructor
     *
     * @param name    Certification's name
     * @param envId   Certification's enviroment ID
     * @param domains Certification's domains json format
     * @param status  Certification's status
     */
    public CertificationDO(String name,
                           Long envId,
                           String domains,
                           String status) {
        this.name = name;
        this.envId = envId;
        this.domains = domains;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getDomains() {
        return domains;
    }

    public void setDomains(String domains) {
        this.domains = domains;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
