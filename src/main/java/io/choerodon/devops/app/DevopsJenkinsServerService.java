package io.choerodon.devops.app;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsJenkinsServerStatusCheckResponseVO;
import io.choerodon.devops.api.vo.DevopsJenkinsServerVO;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsJenkinsServerService {
    DevopsJenkinsServerVO create(Long projectId, DevopsJenkinsServerVO devopsJenkinsServerVO);

    void update(Long projectId, DevopsJenkinsServerVO devopsJenkinsServerVO);

    DevopsJenkinsServerStatusCheckResponseVO checkConnection(Long projectId, DevopsJenkinsServerVO devopsJenkinsServerVO);

    void enable(Long projectId, Long jenkinsId);

    void disable(Long projectId, Long jenkinsId);

    void delete(Long projectId, Long jenkinsId);

    Page<DevopsJenkinsServerVO> pageServer(Long projectId, PageRequest pageable, SearchVO searchVO);

    DevopsJenkinsServerVO query(Long projectId, Long jenkinsServerId);

    Boolean checkNameExist(Long projectId, Long jenkinsServerId, String serverName);
}
