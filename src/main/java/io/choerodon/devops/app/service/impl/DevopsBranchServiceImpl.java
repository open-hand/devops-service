package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.TagDTO;
import io.choerodon.devops.app.service.DevopsBranchService;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.DevopsBranchE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.infra.dto.DevopsBranchDO;
import io.choerodon.devops.infra.dto.gitlab.TagDO;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/11.
 */

@Service
public class DevopsBranchServiceImpl implements DevopsBranchService {



    public PageInfo<DevopsBranchE> listBranches(Long appId, PageRequest pageRequest, String params) {

        PageInfo<DevopsBranchDO> devopsBranchDOS;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = json.deserialize(params, Map.class);
            if (maps.get(TypeUtil.SEARCH_PARAM).equals("")) {
                devopsBranchDOS = PageHelper.startPage(
                        pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsBranchMapper.list(
                        appId, null,
                        TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            } else {
                devopsBranchDOS = PageHelper.startPage(
                        pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsBranchMapper.list(
                        appId, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            }
        } else {
            devopsBranchDOS = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsBranchMapper.list(appId, null, null));
        }
        return ConvertPageHelper.convertPageInfo(devopsBranchDOS, DevopsBranchE.class);
    }

    public void deleteDevopsBranch(Long appId, String branchName) {
        DevopsBranchDO devopsBranchDO = devopsBranchMapper.queryByAppAndBranchName(appId, branchName);
        if (devopsBranchDO != null) {
            devopsBranchDO.setDeleted(true);
            devopsBranchMapper.updateByPrimaryKeySelective(devopsBranchDO);
        }
    }

    @Override
    public List<DevopsBranchE> listDevopsBranchesByAppIdAndBranchName(Long appId, String branchName) {
        DevopsBranchDO devopsBranchDO = new DevopsBranchDO();
        devopsBranchDO.setAppId(appId);
        devopsBranchDO.setBranchName(branchName);
        return ConvertHelper.convertList(devopsBranchMapper.select(devopsBranchDO), DevopsBranchE.class);
    }

    @Override
    public List<DevopsBranchE> listDevopsBranchesByAppId(Long appId) {
        DevopsBranchDO devopsBranchDO = new DevopsBranchDO();
        devopsBranchDO.setAppId(appId);
        return ConvertHelper.convertList(devopsBranchMapper.select(devopsBranchDO), DevopsBranchE.class);
    }

    @Override
    public DevopsBranchE queryByBranchNameAndCommit(String branchName, String commit) {
        DevopsBranchDO devopsBranchDO = new DevopsBranchDO();
        devopsBranchDO.setBranchName(branchName);
        devopsBranchDO.setCheckoutCommit(commit);
        return ConvertHelper.convert(devopsBranchMapper.selectOne(devopsBranchDO), DevopsBranchE.class);
    }



}
