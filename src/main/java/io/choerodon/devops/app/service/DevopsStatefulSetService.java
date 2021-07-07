package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsStatefulSetVO;
import io.choerodon.devops.api.vo.StatefulSetInfoVO;
import io.choerodon.devops.infra.dto.DevopsStatefulSetDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:20
 */
public interface DevopsStatefulSetService extends WorkloadBaseService<DevopsStatefulSetDTO, DevopsStatefulSetVO> {
    Page<StatefulSetInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance);
}
