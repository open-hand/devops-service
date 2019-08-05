package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.DevopsEnvMessageVO;
import io.choerodon.devops.infra.dto.DevopsEnvApplicationDTO;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
public interface DevopsEnvApplicationService {
    List<DevopsEnvApplicationVO> batchCreate(DevopsEnvApplicationCreationVO devopsEnvApplicationCreationVO);


    /**
     * 查询环境下的所有应用
     *
     * @param envId
     * @return
     */
    List<ApplicationServiceRepVO> listAppByEnvId(Long envId);

    /**
     * 查询应用在环境下的所有label
     *
     * @param envId
     * @param appId
     * @return
     */
    List<DevopsEnvLabelVO> listLabelByAppAndEnvId(Long envId, Long appId);

    /**
     * 查询应用在环境下的所有端口
     *
     * @param envId
     * @param appId
     * @return
     */
    List<DevopsEnvPortVO> listPortByAppAndEnvId(Long envId, Long appId);

    DevopsEnvApplicationDTO baseCreate(DevopsEnvApplicationDTO devopsEnvApplicationDTO);

    List<Long> baseListAppByEnvId(Long envId);

    List<DevopsEnvMessageVO> baseListResourceByEnvAndApp(Long envId, Long appId);

    /**
     * 查询项目下可用的且没有与该环境关联的应用
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return 应用列表
     */
    List<BaseApplicationVO> listNonRelatedApplications(Long projectId, Long envId);
}
