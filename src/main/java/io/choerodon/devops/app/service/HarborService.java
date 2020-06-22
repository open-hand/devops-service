package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.app.eventhandler.payload.HarborPayload;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoConfigDTO;
import io.choerodon.devops.infra.dto.harbor.User;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.HarborClient;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:30
 * Description:
 */
public interface HarborService {

    User convertHarborUser(ProjectDTO projectDTO, Boolean isPush, String username);

    List<HarborRepoConfigDTO> listAllCustomRepoByProject(Long projectId);

    DevopsConfigDTO queryRepoConfigToDevopsConfig(Long projectId, Long id,String operateType);

    DevopsConfigDTO queryRepoConfigByIdToDevopsConfig(Long appServiceId,Long projectId, Long harborConfigId, String repoType,String operateType);
}
