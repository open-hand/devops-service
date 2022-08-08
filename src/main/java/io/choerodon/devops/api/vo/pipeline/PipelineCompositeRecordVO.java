package io.choerodon.devops.api.vo.pipeline;

import java.util.Date;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/11/16 14:54
 */
public class PipelineCompositeRecordVO {
    @Encrypt
    private Long id;
    private String ciStatus;
    private String cdStatus;
    private Date creationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCiStatus() {
        return ciStatus;
    }

    public void setCiStatus(String ciStatus) {
        this.ciStatus = ciStatus;
    }

    public String getCdStatus() {
        return cdStatus;
    }

    public void setCdStatus(String cdStatus) {
        this.cdStatus = cdStatus;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
