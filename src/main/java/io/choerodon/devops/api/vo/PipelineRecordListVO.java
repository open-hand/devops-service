package io.choerodon.devops.api.vo;

import java.util.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:27 2019/4/19
 * Description:
 */
public class PipelineRecordListVO {
    private Long id;
    private Date creationTime;

    public PipelineRecordListVO(Long id, Date creationTime) {
        this.id = id;
        this.creationTime = creationTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
