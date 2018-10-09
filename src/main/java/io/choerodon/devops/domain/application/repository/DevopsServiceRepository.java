package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.DevopsServiceE;
import io.choerodon.devops.domain.application.valueobject.DevopsServiceV;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/13.
 */
public interface DevopsServiceRepository {

    DevopsServiceE insert(DevopsServiceE devopsServiceE);

    DevopsServiceE query(Long id);

    void delete(Long id);

    void update(DevopsServiceE devopsServiceE);

    Boolean checkName(Long projectId, Long envId, String name);

    Page<DevopsServiceV> listDevopsServiceByPage(Long projectId, Long envId,
                                                 PageRequest pageRequest, String searchParam);

    List<DevopsServiceV> listDevopsService(Long envId);

    DevopsServiceV selectById(Long id);

    List<Long> selectDeployedEnv();

    DevopsServiceE selectByNameAndEnvId(String name, Long envId);

    Boolean checkEnvHasService(Long envId);

    List<DevopsServiceE> list();

    List<DevopsServiceE> selectByEnvId(Long envId);
}
