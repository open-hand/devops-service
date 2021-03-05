package io.choerodon.devops.api.vo.pipeline;

import java.util.Date;

/**
 * Created by wangxiang on 2021/3/5
 */
public class ExecuteDetailVO {

    private Date executeDate;
    private String executeTime;

    public Date getExecuteDate() {
        return executeDate;
    }

    public void setExecuteDate(Date executeDate) {
        this.executeDate = executeDate;
    }

    public String getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(String executeTime) {
        this.executeTime = executeTime;
    }
}
