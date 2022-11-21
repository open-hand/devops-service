package io.choerodon.devops.app.eventhandler.pipeline.job;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.app.service.DevopsCiPipelineService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.devops.infra.dto.DevopsEnvUserPermissionDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsEnvUserPermissionMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 14:47
 */
public abstract class AbstractAppDeployJobHandlerImpl extends AbstractJobHandler {

    @Autowired
    protected DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsEnvUserPermissionMapper devopsEnvUserPermissionMapper;
    @Autowired
    @Lazy
    private DevopsCiPipelineService devopsCiPipelineService;

    @Override
    public void fillJobAdditionalInfo(DevopsCiJobVO devopsCiJobVO) {
        CiCdPipelineDTO ciCdPipelineDTO = devopsCiPipelineService.baseQueryById(devopsCiJobVO.getCiPipelineId());
        Long projectId = ciCdPipelineDTO.getProjectId();
        DevopsEnvironmentDTO devopsEnvironmentDTO = queryEnvironmentByJobConfigId(devopsCiJobVO.getConfigId());
        devopsCiJobVO.setEdit(isEditCdJob(devopsEnvironmentDTO, projectId));
    }

    protected abstract DevopsEnvironmentDTO queryEnvironmentByJobConfigId(Long configId);

    private boolean isEditCdJob(DevopsEnvironmentDTO devopsEnvironmentDTO, Long projectId) {
        if (Objects.isNull(devopsEnvironmentDTO)) {
            return Boolean.FALSE;
        }
        if (devopsEnvironmentDTO.getSkipCheckPermission()) {
            return Boolean.TRUE;
        }
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if (baseServiceClientOperator.isProjectOwner(userDetails.getUserId(), projectId)) {
            return Boolean.TRUE;
        }
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        if (baseServiceClientOperator.isOrganzationRoot(userDetails.getUserId(), projectDTO.getOrganizationId()) || userDetails.getAdmin()) {
            return Boolean.TRUE;
        }
        DevopsEnvUserPermissionDTO devopsEnvUserPermissionDTO = new DevopsEnvUserPermissionDTO();
        devopsEnvUserPermissionDTO.setEnvId(devopsEnvironmentDTO.getId());
        devopsEnvUserPermissionDTO.setIamUserId(userDetails.getUserId());
        List<DevopsEnvUserPermissionDTO> devopsEnvUserPermissionDTOS = devopsEnvUserPermissionMapper.select(devopsEnvUserPermissionDTO);
        if (!CollectionUtils.isEmpty(devopsEnvUserPermissionDTOS)) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
