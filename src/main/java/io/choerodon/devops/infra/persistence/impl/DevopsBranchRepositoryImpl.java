package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.choerodon.devops.domain.application.repository.DevopsBranchRepository;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.mapper.DevopsBranchMapper;

/**
 * Creator: chenwei
 * Date: 18-7-5
 * Time: 下午3:39
 * Description:
 */

@Repository
public class DevopsBranchRepositoryImpl implements DevopsBranchRepository {

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
