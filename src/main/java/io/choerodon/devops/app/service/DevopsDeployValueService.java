package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.DevopsDeployValueVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsDeployValueE;
import io.choerodon.devops.infra.dto.DevopsDeployValueDTO;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:57 2019/4/10
 * Description:
 */
public interface DevopsDeployValueService {
    DevopsDeployValueVO createOrUpdate(Long projectId, DevopsDeployValueVO pipelineValueDTO);

    void delete(Long projectId, Long valueId);

    PageInfo<DevopsDeployValueVO> listByOptions(Long projectId, Long appId, Long envId, PageRequest pageRequest, String params);

    DevopsDeployValueVO queryById(Long pipelineId, Long valueId);

    void checkName(Long projectId, String name);

    List<DevopsDeployValueVO> queryByAppIdAndEnvId(Long projectId, Long appId, Long envId);

    Boolean checkDelete(Long projectId, Long valueId);

    PageInfo<DevopsDeployValueDTO> basePageByOptions(Long projectId, Long appId, Long envId, Long userId, PageRequest pageRequest, String params);

    DevopsDeployValueDTO baseCreateOrUpdate(DevopsDeployValueDTO pipelineRecordE);

    void baseDelete(Long valueId);

    DevopsDeployValueDTO baseQueryById(Long valueId);

    void baseCheckName(Long projectId, String name);

    List<DevopsDeployValueDTO> baseQueryByAppIdAndEnvId(Long projectId, Long appId, Long envId);
}
