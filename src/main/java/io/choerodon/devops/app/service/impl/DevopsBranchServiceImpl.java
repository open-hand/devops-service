package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsBranchService;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.mapper.DevopsBranchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



/**
 * Created by Sheep on 2019/7/11.
 */

@Service
public class DevopsBranchServiceImpl implements DevopsBranchService {

    @Autowired
    private DevopsBranchMapper devopsBranchMapper;

    @Override
    public List<DevopsBranchDTO> baseQueryDevopsBranchesByIssueId(Long issueId) {
        DevopsBranchDTO queryDevopsBranchDTO = new DevopsBranchDTO();
        queryDevopsBranchDTO.setIssueId(issueId);
        queryDevopsBranchDTO.setDeleted(false);
        return devopsBranchMapper.select(queryDevopsBranchDTO);
    }

    public DevopsBranchDTO baseQueryByAppAndBranchName(Long appId, String branchName) {
        return devopsBranchMapper
                .queryByAppAndBranchName(appId, branchName);
    }

    public void baseUpdateBranchIssue(Long appId, DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchDTO oldDevopsBranchDTO = devopsBranchMapper
                .queryByAppAndBranchName(appId, devopsBranchDTO.getBranchName());
        oldDevopsBranchDTO.setIssueId(devopsBranchDTO.getIssueId());
        devopsBranchMapper.updateByPrimaryKey(devopsBranchDTO);
    }

    public void baseUpdateBranchLastCommit(DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchDTO oldDevopsBranchDTO = devopsBranchMapper
                .queryByAppAndBranchName(devopsBranchDTO.getAppId(), devopsBranchDTO.getBranchName());
        oldDevopsBranchDTO.setLastCommit(devopsBranchDTO.getLastCommit());
        oldDevopsBranchDTO.setLastCommitDate(devopsBranchDTO.getLastCommitDate());
        oldDevopsBranchDTO.setLastCommitMsg(devopsBranchDTO.getLastCommitMsg());
        oldDevopsBranchDTO.setLastCommitUser(devopsBranchDTO.getLastCommitUser());
        devopsBranchMapper.updateByPrimaryKey(oldDevopsBranchDTO);

    }

    public DevopsBranchDTO baseCreate(DevopsBranchDTO devopsBranchDTO) {
        devopsBranchDTO.setDeleted(false);
        devopsBranchMapper.insert(devopsBranchDTO);
        return devopsBranchDTO;
    }

    public DevopsBranchDTO baseQuery(Long devopsBranchId) {
        return devopsBranchMapper.selectByPrimaryKey(devopsBranchId);
    }

    public void baseUpdateBranch(DevopsBranchDTO devopsBranchDTO) {
        devopsBranchDTO.setObjectVersionNumber(devopsBranchMapper.selectByPrimaryKey(devopsBranchDTO.getId()).getObjectVersionNumber());
        if (devopsBranchMapper.updateByPrimaryKey(devopsBranchDTO) != 1) {
            throw new CommonException("error.branch.update");
        }
    }
}
