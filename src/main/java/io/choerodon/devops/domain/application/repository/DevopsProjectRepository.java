package io.choerodon.devops.domain.application.repository;

<<<<<<< HEAD
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectE;
=======
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
>>>>>>> [IMP] 修改AppControler重构
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;



/**
 * Created by younger on 2018/3/29.
 */
public interface DevopsProjectRepository {
    void createProject(DevopsProjectDTO devopsProjectDO);

    void updateProjectAttr(DevopsProjectDTO devopsProjectDO);

    DevopsProjectVO queryDevopsProject(Long projectId);

    DevopsProjectVO queryByGitlabGroupId(Integer gitlabGroupId);

    DevopsProjectVO queryByEnvGroupId(Integer envGroupId);

}
