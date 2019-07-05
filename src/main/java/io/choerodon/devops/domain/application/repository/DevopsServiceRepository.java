package io.choerodon.devops.domain.application.repository;

import com.github.pagehelper.PageInfo;

import java.util.List;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.DevopsServiceE;
import io.choerodon.devops.domain.application.valueobject.DevopsServiceV;

/**
 * Created by Zenger on 2018/4/13.
 */
public interface DevopsServiceRepository {

    DevopsServiceE insert(DevopsServiceE devopsServiceE);

    DevopsServiceE query(Long id);

    void delete(Long id);

    void update(DevopsServiceE devopsServiceE);

    Boolean checkName(Long envId, String name);


    PageInfo<DevopsServiceV> listDevopsServiceByPage(Long projectId, Long envId, Long instanceId,
                                                     PageRequest pageRequest, String searchParam, Long appId);

    List<DevopsServiceV> listDevopsService(Long envId);

    DevopsServiceV selectById(Long id);

    List<Long> selectDeployedEnv();

    DevopsServiceE selectByNameAndEnvId(String name, Long envId);

    Boolean checkEnvHasService(Long envId);

    List<DevopsServiceE> list();

    List<DevopsServiceE> selectByEnvId(Long envId);

    void setLablesToNull(Long id);

    void deleteServiceAndInstanceByEnvId(Long envId);

    void setEndPointToNull(Long id);
}
