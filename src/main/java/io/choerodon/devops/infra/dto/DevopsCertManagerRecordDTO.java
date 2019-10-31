package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.entity.BaseDTO;

import javax.persistence.Table;

/**
 * @author zhaotianxin
 * @since 2019/10/30
 */
@Table(name = "devops_cert_manager")
public class DevopsCertManagerRecordDTO extends BaseDTO {
    private Long id;
    private String status;
    private String error;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
