package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsBranchDTO;


/**
 * Creator: chenwei
 * Date: 2018/7/5
 * Time: 15:32
 * Description:
 */

public interface DevopsBranchRepository {

    List<DevopsBranchDTO> baseQueryDevopsBranchesByIssueId(Long issueId);
}
