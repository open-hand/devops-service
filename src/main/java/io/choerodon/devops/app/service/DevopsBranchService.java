package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.apache.commons.lang.StringUtils;

/**
 * Created by Sheep on 2019/7/11.
 */
public interface DevopsBranchService {
    List<DevopsBranchDTO> baseQueryDevopsBranchesByIssueId(Long issueId);

    DevopsBranchDTO baseQueryByAppAndBranchName(Long appId, String branchName);

    void baseUpdateBranchIssue(Long appId, DevopsBranchDTO devopsBranchDTO);

    void baseUpdateBranchLastCommit(DevopsBranchDTO devopsBranchDTO);

    DevopsBranchDTO baseCreate(DevopsBranchDTO devopsBranchDTO);

    DevopsBranchDTO baseQuery(Long devopsBranchId);

    void baseUpdateBranch(DevopsBranchDTO devopsBranchDTO);

    PageInfo<DevopsBranchDTO> basePageBranch(Long appId, PageRequest pageRequest, String params);

    void baseDelete(Long appId, String branchName);

    List<DevopsBranchDTO> baseListByAppId(Long appId);

    List<DevopsBranchDTO> baseListByAppIdAndBranchName(Long appId, String branchName);

    DevopsBranchDTO baseQueryByBranchNameAndCommit(String branchName, String commit);

}
