package io.choerodon.devops.app.service.impl;

import java.util.List;

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
    public List<DevopsBranchDTO> baseGetDevopsBranchesByIssueId(Long issueId) {
        DevopsBranchDTO queryDevopsBranchDTO = new DevopsBranchDTO();
        queryDevopsBranchDTO.setIssueId(issueId);
        queryDevopsBranchDTO.setDeleted(false);
        return devopsBranchMapper.select(queryDevopsBranchDTO);
    }
}
