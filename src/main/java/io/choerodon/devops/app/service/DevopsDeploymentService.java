package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DeploymentInfoVO;
import io.choerodon.devops.api.vo.DevopsDeployGroupContainerConfigVO;
import io.choerodon.devops.api.vo.DevopsDeploymentVO;
import io.choerodon.devops.api.vo.DevopsEnvPortVO;
import io.choerodon.devops.api.vo.InstanceControllerDetailVO;
import io.choerodon.devops.infra.dto.DevopsDeploymentDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:17
 */
public interface DevopsDeploymentService extends WorkloadBaseService<DevopsDeploymentDTO, DevopsDeploymentVO> {

    DevopsDeploymentVO queryByDeploymentIdWithResourceDetail(Long deploymentId);

    Page<DeploymentInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance);

    InstanceControllerDetailVO getInstanceResourceDetailYaml(Long deploymentId);

    InstanceControllerDetailVO getInstanceResourceDetailJson(Long deploymentId);

    void startDeployment(Long projectId, Long deploymentId);

    void stopDeployment(Long projectId, Long deploymentId);

    List<DevopsEnvPortVO>  listPortByDeploymentId(Long deploymentId);

    List<DevopsEnvPortVO> listPortByDevopsEnvMessageVOS(List<DevopsDeployGroupContainerConfigVO> devopsDeployGroupContainerConfigVOS);
}
