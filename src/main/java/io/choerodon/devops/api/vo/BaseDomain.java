package io.choerodon.devops.api.vo;

import java.util.Date;

/**
 * Created by wangxiang on 2020/7/27
 */
public class BaseDomain {
    // cicd 执行记录创建时间
    private Date createdDate;

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
