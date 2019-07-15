package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsIngressE;
<<<<<<< HEAD
import io.choerodon.devops.api.vo.iam.entity.DevopsIngressPathE;
<<<<<<< HEAD
import io.choerodon.devops.infra.dto.DevopsIngressDO;


=======
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
>>>>>>> [IMP] 修改AppControler重构

=======
import io.choerodon.devops.infra.dto.DevopsIngressDTO;
import io.choerodon.devops.infra.dto.DevopsIngressPathDTO;


>>>>>>> [IMP]重构后端断码
/**
 * Creator: Runge
 * Date: 2018/4/20
 * Time: 16:06
 * Description:
 */
public interface DevopsIngressRepository {
    DevopsIngressDTO baseCreateIngressAndPath(DevopsIngressDTO devopsIngressDTO);

    void baseUpdateIngressAndIngressPath(DevopsIngressDTO devopsIngressDTO);

    void baseUpdateIngress(DevopsIngressDTO devopsIngressDTO);


    PageInfo<DevopsIngressVO> basePageByOptions(Long projectId, Long envId, Long serviceId, PageRequest pageRequest, String params);

    DevopsIngressVO baseQuery(Long projectId, Long ingressId);

    DevopsIngressDTO baseQuery(Long ingressId);

    void baseDelete(Long ingressId);

    Long baseUpdateStatus(Long envId, String name, String status);

    List<String> baseListNameByServiceId(Long serviceId);

    Boolean baseCheckName(Long envId, String name);

    Boolean baseCheckPath(Long envId, String domain, String path, Long id);

    DevopsIngressDTO baseCheckByEnvAndName(Long envId, String name);

    DevopsIngressDTO baseCreateIngress(DevopsIngressE devopsIngressE);

    void baseCreatePath(DevopsIngressPathDTO devopsIngressPathDTO);

    List<DevopsIngressPathDTO> baseListPathByEnvIdAndServiceName(Long envId, String serviceName);

    List<DevopsIngressPathDTO> baseListPathByEnvIdAndServiceId(Long envId, Long serviceId);

    List<DevopsIngressPathDTO> baseListPathByIngressId(Long ingressId);

    List<DevopsIngressDTO> baseListByEnvId(Long envId);

    void baseUpdatePath(DevopsIngressPathDTO devopsIngressPathDTO);

    void baseDeletePathByIngressId(Long ingressId);

    Boolean baseCheckByEnv(Long envId);

    List<DevopsIngressDTO> baseList();

    void baseDeleteByEnv(Long envId);
}
