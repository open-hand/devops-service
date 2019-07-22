package io.choerodon.devops.infra.persistence.impl;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.infra.util.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.DevopsGitlabPipelineRepository;
import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDTO;
import io.choerodon.devops.infra.mapper.DevopsGitlabPipelineMapper;

@Service
public class DevopsGitlabPipelineRepositoryImpl implements DevopsGitlabPipelineRepository {


    @Autowired
    private DevopsGitlabPipelineMapper devopsGitlabPipelineMapper;


    @Override
    public void baseCreate(DevopsGitlabPipelineE devopsGitlabPipelineE) {
        DevopsGitlabPipelineDTO devopsGitlabPipelineDO = ConvertHelper.convert(devopsGitlabPipelineE, DevopsGitlabPipelineDTO.class);
        if (devopsGitlabPipelineMapper.insert(devopsGitlabPipelineDO) != 1) {
            throw new CommonException("error.gitlab.pipeline.create");
        }
    }

    @Override
    public DevopsGitlabPipelineE baseQueryByGitlabPipelineId(Long id) {
        DevopsGitlabPipelineDTO devopsGitlabPipelineDO = new DevopsGitlabPipelineDTO();
        devopsGitlabPipelineDO.setPipelineId(id);
        return ConvertHelper.convert(devopsGitlabPipelineMapper.selectOne(devopsGitlabPipelineDO), DevopsGitlabPipelineE.class);
    }

    @Override
    public void baseUpdate(DevopsGitlabPipelineE devopsGitlabPipelineE) {
        DevopsGitlabPipelineDTO devopsGitlabPipelineDO = devopsGitlabPipelineMapper.selectByPrimaryKey(devopsGitlabPipelineE.getId());
        DevopsGitlabPipelineDTO updateDevopsGitlabPipelineDO = ConvertHelper.convert(devopsGitlabPipelineE, DevopsGitlabPipelineDTO.class);
        updateDevopsGitlabPipelineDO.setObjectVersionNumber(devopsGitlabPipelineDO.getObjectVersionNumber());
        if (devopsGitlabPipelineMapper.updateByPrimaryKeySelective(updateDevopsGitlabPipelineDO) != 1) {
            throw new CommonException("error.gitlab.pipeline.update");
        }
    }

    @Override
    public DevopsGitlabPipelineE baseQueryByCommitId(Long commitId) {
        DevopsGitlabPipelineDTO devopsGitlabPipelineDO = new DevopsGitlabPipelineDTO();
        devopsGitlabPipelineDO.setCommitId(commitId);
        return ConvertHelper.convert(devopsGitlabPipelineMapper.selectOne(devopsGitlabPipelineDO), DevopsGitlabPipelineE.class);
    }

    @Override
    public List<DevopsGitlabPipelineDTO> baseListByApplicationId(Long appId, Date startTime, Date endTime) {
        return devopsGitlabPipelineMapper.listDevopsGitlabPipeline(appId, new java.sql.Date(startTime.getTime()), new java.sql.Date(endTime.getTime()));
    }


    @Override
    public PageInfo<DevopsGitlabPipelineDTO> basePageByApplicationId(Long appId, PageRequest pageRequest, Date startTime, Date endTime) {
        return PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                devopsGitlabPipelineMapper.listDevopsGitlabPipeline(appId, startTime == null ? null : new java.sql.Date(startTime.getTime()), endTime == null ? null : new java.sql.Date(endTime.getTime())));
    }

    @Override
    public void baseDeleteWithoutCommit() {
        devopsGitlabPipelineMapper.deleteWithoutCommit();
    }

    @Override
    public List<DevopsGitlabPipelineDTO> baseListByAppIdAndBranch(Long appId, String branch) {
        return devopsGitlabPipelineMapper.listByBranch(appId, branch);
    }
}
