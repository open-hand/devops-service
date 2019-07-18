package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvMessageVO;
import io.choerodon.devops.infra.dto.DevopsEnvApplicationDTO;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
public interface DevopsEnvApplicationService {
    List<DevopsEnvApplicationVO> batchCreate(DevopsEnvApplicationCreationVO devopsEnvApplicationCreationVO);

    List<ApplicationRepVO> queryAppByEnvId(Long envId);


    /**
     * 查询环境下的所有应用
     *
     * @param envId
     * @return
     */
    List<ApplicationRepVO> listAppByEnvId(Long envId);

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
    List<DevopsEnvPortDTO> listPortByAppAndEnvId(Long envId, Long appId);

    DevopsEnvApplicationDTO baseCreate(DevopsEnvApplicationDTO devopsEnvApplicationDTO);

    List<Long> baseListAppByEnvId(Long envId);

    List<DevopsEnvMessageVO> baseListResourceByEnvAndApp(Long envId, Long appId);
}
