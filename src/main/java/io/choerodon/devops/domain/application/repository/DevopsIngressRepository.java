package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsIngressDTO;
import io.choerodon.devops.domain.application.entity.DevopsIngressE;
import io.choerodon.devops.domain.application.entity.DevopsIngressPathE;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;

/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 16:06
 * Description:
 */
public interface DevopsIngressRepository {
    DevopsIngressDO createIngress(DevopsIngressDO devopsIngressDO);

    void updateIngressAndIngressPath(DevopsIngressDO devopsIngressDO);

    void updateIngress(DevopsIngressDO devopsIngressDO);


    PageInfo<DevopsIngressDTO> getIngress(Long projectId, Long envId, Long serviceId, PageRequest pageRequest, String params);

    DevopsIngressDTO getIngress(Long projectId, Long ingressId);

    DevopsIngressDO getIngress(Long ingressId);

    void deleteIngress(Long ingressId);

    Long setStatus(Long envId, String name, String status);

    List<String> queryIngressNameByServiceId(Long serviceId);

    Boolean checkIngressName(Long envId, String name);

    Boolean checkIngressAndPath(Long envId, String domain, String path, Long id);

    DevopsIngressE selectByEnvAndName(Long envId, String name);

    DevopsIngressE insertIngress(DevopsIngressE devopsIngressE);

    void insertIngressPath(DevopsIngressPathE devopsIngressPathE);

    List<DevopsIngressPathE> selectByEnvIdAndServiceName(Long envId, String serviceName);

    List<DevopsIngressPathE> selectByEnvIdAndServiceId(Long envId, Long serviceId);

    List<DevopsIngressPathE> selectByIngressId(Long ingressId);

    List<DevopsIngressE> listByEnvId(Long envId);

    void updateIngressPath(DevopsIngressPathE devopsIngressPathE);

    void deleteIngressPath(Long ingressId);

    Boolean checkEnvHasIngress(Long envId);

    List<DevopsIngressE> list();

    void deleteIngressAndIngressPathByEnvId(Long envId);
}
