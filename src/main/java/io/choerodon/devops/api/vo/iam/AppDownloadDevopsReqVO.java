package io.choerodon.devops.api.vo.iam;

import java.util.Set;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:38 2019/9/18
 * Description:
 */
public class AppDownloadDevopsReqVO {
    private Long serviceId;

    private Set<Long> serviceVersionIds;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Set<Long> getServiceVersionIds() {
        return serviceVersionIds;
    }

    public void setServiceVersionIds(Set<Long> serviceVersionIds) {
        this.serviceVersionIds = serviceVersionIds;
    }
}
