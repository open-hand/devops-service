package io.choerodon.devops.app.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsJenkinsServerStatusCheckResponseVO;
import io.choerodon.devops.api.vo.DevopsJenkinsServerVO;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.infra.dto.DevopsJenkinsServerDTO;
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

    Boolean checkNameExists(Long projectId, Long jenkinsServerId, String serverName);

    List<DevopsJenkinsServerDTO> listByProjectId(Long projectId);

    DevopsJenkinsServerDTO queryById(Long id);

    List<DevopsJenkinsServerDTO> listAll(Long projectId, String status);

    ResponseEntity<Resource> downloadPlugin();

    String queryUserGuide(Long projectId);

}
