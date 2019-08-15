package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsBranchService;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.mapper.DevopsBranchMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Created by Sheep on 2019/7/11.
 */

@Service
public class DevopsBranchServiceImpl implements DevopsBranchService {

    private JSON json = new JSON();


    @Autowired
    private DevopsBranchMapper devopsBranchMapper;

    @Override
    public List<DevopsBranchDTO> baseListDevopsBranchesByIssueId(Long issueId) {
        DevopsBranchDTO queryDevopsBranchDTO = new DevopsBranchDTO();
        queryDevopsBranchDTO.setIssueId(issueId);
        queryDevopsBranchDTO.setDeleted(false);
        return devopsBranchMapper.select(queryDevopsBranchDTO);
    }

    public DevopsBranchDTO baseQueryByAppAndBranchName(Long appServiceId, String branchName) {
        return devopsBranchMapper
                .queryByAppAndBranchName(appServiceId, branchName);
    }

    public void baseUpdateBranchIssue(Long appServiceId, DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchDTO oldDevopsBranchDTO = devopsBranchMapper
                .queryByAppAndBranchName(appServiceId, devopsBranchDTO.getBranchName());
        oldDevopsBranchDTO.setIssueId(devopsBranchDTO.getIssueId());
        devopsBranchMapper.updateByPrimaryKey(devopsBranchDTO);
    }

    public void baseUpdateBranchLastCommit(DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchDTO oldDevopsBranchDTO = devopsBranchMapper
                .queryByAppAndBranchName(devopsBranchDTO.getAppServiceId(), devopsBranchDTO.getBranchName());
        oldDevopsBranchDTO.setLastCommit(devopsBranchDTO.getLastCommit());
        oldDevopsBranchDTO.setLastCommitDate(devopsBranchDTO.getLastCommitDate());
        oldDevopsBranchDTO.setLastCommitMsg(devopsBranchDTO.getLastCommitMsg());
        oldDevopsBranchDTO.setLastCommitUser(devopsBranchDTO.getLastCommitUser());
        devopsBranchMapper.updateByPrimaryKey(oldDevopsBranchDTO);
    }

    public DevopsBranchDTO baseCreate(DevopsBranchDTO devopsBranchDTO) {
        DevopsBranchDTO exist = new DevopsBranchDTO();
        exist.setAppServiceId(devopsBranchDTO.getAppServiceId());
        exist.setBranchName(devopsBranchDTO.getBranchName());
        if (devopsBranchMapper.selectOne(exist) != null) {
            throw new CommonException("error.branch.exist");
        }
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


    public PageInfo<DevopsBranchDTO> basePageBranch(Long appServiceId, PageRequest pageRequest, String params) {

        PageInfo<DevopsBranchDTO> devopsBranchDTOPageInfo;
        Map<String, Object> maps = TypeUtil.castMapParams(params);
        devopsBranchDTOPageInfo = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest))
                .doSelectPageInfo(
                        () -> devopsBranchMapper.list(appServiceId,
                                TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                                TypeUtil.cast(maps.get(TypeUtil.PARAMS))));
        return devopsBranchDTOPageInfo;
    }


    public void baseDelete(Long appServiceId, String branchName) {
        DevopsBranchDTO devopsBranchDTO = devopsBranchMapper.queryByAppAndBranchName(appServiceId, branchName);
        if (devopsBranchDTO != null) {
            devopsBranchMapper.delete(devopsBranchDTO);
        }
    }

    public List<DevopsBranchDTO> baseListByAppId(Long appServiceId) {
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
        devopsBranchDTO.setAppServiceId(appServiceId);
        return devopsBranchMapper.select(devopsBranchDTO);
    }


    public List<DevopsBranchDTO> baseListByAppIdAndBranchName(Long appServiceId, String branchName) {
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
        devopsBranchDTO.setAppServiceId(appServiceId);
        devopsBranchDTO.setBranchName(branchName);
        return devopsBranchMapper.select(devopsBranchDTO);
    }


    public DevopsBranchDTO baseQueryByBranchNameAndCommit(String branchName, String commit) {
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
        devopsBranchDTO.setBranchName(branchName);
        devopsBranchDTO.setCheckoutCommit(commit);
        return devopsBranchMapper.selectOne(devopsBranchDTO);
    }

}
