package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈统计项目下应用服务数量VO〉
 *
 * @author wanghao
 * @since 2020/6/29 17:29
 */
public class ProjectAppSvcCountVO {
    private Long projectId;
    private Integer appSvcNum;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getAppSvcNum() {
        return appSvcNum;
    }

    public void setAppSvcNum(Integer appSvcNum) {
        this.appSvcNum = appSvcNum;
    }
}
