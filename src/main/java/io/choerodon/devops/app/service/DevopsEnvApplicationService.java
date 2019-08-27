package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.DevopsEnvMessageVO;
import io.choerodon.devops.infra.dto.DevopsEnvAppServiceDTO;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
public interface DevopsEnvApplicationService {
    List<DevopsEnvApplicationVO> batchCreate(DevopsEnvAppServiceVO devopsEnvAppServiceVO);

    /**
     * 删除与该环境关联的应用
     * @param envId
     * @param appServiceId
     */
    void delete(Long envId,Long appServiceId);

    /**
     * 查询环境下的所有应用
     *
     * @param envId
     * @return
     */
    List<AppServiceRepVO> listAppByEnvId(Long envId);

    /**
     * 查询应用在环境下的所有label
     *
     * @param envId
     * @param appServiceId
     * @return
     */
    List<Map<String,String>> listLabelByAppAndEnvId(Long envId, Long appServiceId);

    /**
     * 查询应用在环境下的所有端口
     *
     * @param envId
     * @param appServiceId
     * @return
     */
    List<DevopsEnvPortVO> listPortByAppAndEnvId(Long envId, Long appServiceId);

    DevopsEnvAppServiceDTO baseCreate(DevopsEnvAppServiceDTO devopsEnvAppServiceDTO);

    List<Long> baseListAppByEnvId(Long envId);

    List<DevopsEnvMessageVO> baseListResourceByEnvAndApp(Long envId, Long appServiceId);

    /**
     * 查询项目下可用的且没有与该环境关联的应用
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return 应用列表
     */
    List<BaseApplicationServiceVO> listNonRelatedAppService(Long projectId, Long envId);
}
