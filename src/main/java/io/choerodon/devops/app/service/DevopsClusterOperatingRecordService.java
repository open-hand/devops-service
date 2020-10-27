package io.choerodon.devops.app.service;

import javax.annotation.Nullable;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/27 17:49
 */
public interface DevopsClusterOperatingRecordService {

    void saveOperatingRecord(Long clusterId, Long nodeId, String operatingType, String status,@Nullable String errorMsg);
}
