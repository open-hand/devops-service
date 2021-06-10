package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DaemonSetInfoVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:19
 */
public interface DevopsDaemonSetService {

    Page<DaemonSetInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance);

}
