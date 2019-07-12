package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsEnvCommitDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:54 2019/7/12
 * Description:
 */
public interface DevopsEnvCommitService {

    DevopsEnvCommitDTO baseCreate(DevopsEnvCommitDTO devopsEnvCommitDTO);

    DevopsEnvCommitDTO baseQueryByEnvIdAndCommit(Long envId, String commit);

    DevopsEnvCommitDTO baseQuery(Long id);

    List<DevopsEnvCommitDTO> baseListByEnvId(Long envId);
}
