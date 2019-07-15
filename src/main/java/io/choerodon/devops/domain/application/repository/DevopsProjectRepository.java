package io.choerodon.devops.domain.application.repository;

<<<<<<< HEAD
<<<<<<< HEAD
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectE;
=======
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
>>>>>>> [IMP] 修改AppControler重构
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;

=======
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
>>>>>>> [IMP]重构后端断码


/**
 * Created by younger on 2018/3/29.
 */
public interface DevopsProjectRepository {
    void baseCreate(DevopsProjectDTO devopsProjectDTO);

    void baseUpdate(DevopsProjectDTO devopsProjectDTO);

    DevopsProjectDTO baseQueryByProjectId(Long projectId);

    DevopsProjectDTO baseQueryByGitlabAppGroupId(Integer appGroupId);

    DevopsProjectDTO baseQueryByGitlabEnvGroupId(Integer envGroupId);

}
