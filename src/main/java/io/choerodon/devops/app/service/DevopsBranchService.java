package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsBranchDTO;

/**
 * Created by Sheep on 2019/7/11.
 */
public interface DevopsBranchService {
    List<DevopsBranchDTO> baseGetDevopsBranchesByIssueId(Long issueId);
}
