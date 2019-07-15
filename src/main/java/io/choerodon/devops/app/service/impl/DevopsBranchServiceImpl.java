package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsBranchE;
import io.choerodon.devops.app.service.DevopsBranchService;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.mapper.DevopsBranchMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



/**
 * Created by Sheep on 2019/7/11.
 */

@Service
public class DevopsBranchServiceImpl implements DevopsBranchService {

    private static final JSON json = new JSON();


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


    public PageInfo<DevopsBranchDTO> basePageBranch(Long appId, PageRequest pageRequest, String params) {

        PageInfo<DevopsBranchDTO> devopsBranchDTOPageInfo;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = json.deserialize(params, Map.class);
            if (maps.get(TypeUtil.SEARCH_PARAM).equals("")) {
                devopsBranchDTOPageInfo = PageHelper.startPage(
                        pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsBranchMapper.list(
                        appId, null,
                        TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            } else {
                devopsBranchDTOPageInfo = PageHelper.startPage(
                        pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsBranchMapper.list(
                        appId, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            }
        } else {
            devopsBranchDTOPageInfo = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsBranchMapper.list(appId, null, null));
        }
        return devopsBranchDTOPageInfo;
    }


    public void baseDelete(Long appId, String branchName) {
        DevopsBranchDTO devopsBranchDTO = devopsBranchMapper.queryByAppAndBranchName(appId, branchName);
        if (devopsBranchDTO != null) {
            devopsBranchDTO.setDeleted(true);
            devopsBranchMapper.updateByPrimaryKeySelective(devopsBranchDTO);
        }
    }


    public List<DevopsBranchDTO> baseListByAppId(Long appId) {
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
        devopsBranchDTO.setAppId(appId);
        return devopsBranchMapper.select(devopsBranchDTO);
    }


    public List<DevopsBranchDTO> baseListByAppIdAndBranchName(Long appId, String branchName) {
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO();
        devopsBranchDTO.setAppId(appId);
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
