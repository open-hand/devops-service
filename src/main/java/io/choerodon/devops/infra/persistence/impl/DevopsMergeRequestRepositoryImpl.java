package io.choerodon.devops.infra.persistence.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
import io.choerodon.devops.domain.application.repository.DevopsMergeRequestRepository;
import io.choerodon.devops.infra.common.util.PageRequestUtil;
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO;
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class DevopsMergeRequestRepositoryImpl implements DevopsMergeRequestRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsMergeRequestRepositoryImpl.class);

    @Autowired
    DevopsMergeRequestMapper devopsMergeRequestMapper;

    @Override
    public Integer create(DevopsMergeRequestE devopsMergeRequestE) {
        DevopsMergeRequestDO devopsMergeRequestDO = ConvertHelper.convert(devopsMergeRequestE,
                DevopsMergeRequestDO.class);
        return devopsMergeRequestMapper.insert(devopsMergeRequestDO);
    }

    @Override
    public List<DevopsMergeRequestE> getBySourceBranch(String sourceBranchName, Long gitLabProjectId) {
        return ConvertHelper.convertList(
                devopsMergeRequestMapper.listByProjectIdAndBranch(gitLabProjectId.intValue(),sourceBranchName), DevopsMergeRequestE.class);
    }

    @Override
    public DevopsMergeRequestE queryByAppIdAndGitlabId(Long projectId, Long gitlabMergeRequestId) {
        DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO();
        devopsMergeRequestDO.setProjectId(projectId);
        devopsMergeRequestDO.setGitlabMergeRequestId(gitlabMergeRequestId);
        return ConvertHelper.convert(devopsMergeRequestMapper
                .selectOne(devopsMergeRequestDO), DevopsMergeRequestE.class);
    }

    @Override
    public PageInfo<DevopsMergeRequestE> getMergeRequestList(Integer gitlabProjectId, String state, PageRequest pageRequest) {
        PageInfo<Object> page = PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                devopsMergeRequestMapper.getByProjectIdAndState(gitlabProjectId, state));
        return ConvertPageHelper.convertPageInfo(page, DevopsMergeRequestE.class);
    }

    @Override
    public PageInfo<DevopsMergeRequestE> getByGitlabProjectId(Integer gitlabProjectId, PageRequest pageRequest) {
        DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO();
        devopsMergeRequestDO.setProjectId((long) gitlabProjectId);
        PageInfo<Object> page = PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(),PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo( () -> devopsMergeRequestMapper
                .select(devopsMergeRequestDO));
        return ConvertPageHelper.convertPageInfo(page, DevopsMergeRequestE.class);
    }

    @Override
    public List<DevopsMergeRequestE> getByGitlabProjectId(Integer gitlabProjectId) {
        DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO();
        devopsMergeRequestDO.setProjectId((long) gitlabProjectId);
        List<DevopsMergeRequestDO> devopsMergeRequestDOs = devopsMergeRequestMapper
                .select(devopsMergeRequestDO);
        return ConvertHelper.convertList(devopsMergeRequestDOs, DevopsMergeRequestE.class);
    }

    @Override
    public Integer update(DevopsMergeRequestE devopsMergeRequestE) {
        DevopsMergeRequestDO devopsMergeRequestDO = ConvertHelper
                .convert(devopsMergeRequestE, DevopsMergeRequestDO.class);
        return devopsMergeRequestMapper.updateByPrimaryKey(devopsMergeRequestDO);
    }

    @Override
    public void saveDevopsMergeRequest(DevopsMergeRequestE devopsMergeRequestE) {
        Long projectId = devopsMergeRequestE.getProjectId();
        Long gitlabMergeRequestId = devopsMergeRequestE.getGitlabMergeRequestId();
        DevopsMergeRequestE mergeRequestETemp = queryByAppIdAndGitlabId(projectId, gitlabMergeRequestId);
        Long mergeRequestId = mergeRequestETemp != null ? mergeRequestETemp.getId() : null;
        if (mergeRequestId == null) {
            try {
                create(devopsMergeRequestE);
            } catch (Exception e) {
                LOGGER.info("error.save.merge.request");
            }
        } else {
            devopsMergeRequestE.setId(mergeRequestId);
            devopsMergeRequestE.setObjectVersionNumber(mergeRequestETemp.getObjectVersionNumber());
            if (update(devopsMergeRequestE) == 0) {
                throw new CommonException("error.update.merge.request");
            }
        }
    }
}
