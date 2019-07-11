package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.PipelineE;
import io.choerodon.devops.domain.application.repository.PipelineRepository;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.PipelineDO;
import io.choerodon.devops.infra.mapper.PipelineMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:24 2019/4/4
 * Description:
 */
@Component
public class PipelineRepositoryImpl implements PipelineRepository {
    private static final Gson gson = new Gson();
    @Autowired
    private PipelineMapper pipelineMapper;

    @Override
    public PageInfo<PipelineE> listByOptions(Long projectId, PageRequest pageRequest, String params, Map<String, Object> classifyParam) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        PageInfo<PipelineDO> pipelineDOS = PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(),PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo( () ->
                pipelineMapper.listByOptions(projectId, searchParamMap, paramMap, PageRequestUtil.checkSortIsEmpty(pageRequest), classifyParam));
        return ConvertPageHelper.convertPageInfo(pipelineDOS, PipelineE.class);
    }

    @Override
    public PipelineE create(Long projectId, PipelineE pipelineE) {
        PipelineDO pipelineDO = ConvertHelper.convert(pipelineE, PipelineDO.class);
        pipelineDO.setIsEnabled(1);
        if (pipelineMapper.insert(pipelineDO) != 1) {
            throw new CommonException("error.insert.pipeline");
        }
        return ConvertHelper.convert(pipelineDO, PipelineE.class);
    }

    @Override
    public PipelineE update(Long projectId, PipelineE pipelineE) {
        PipelineDO pipelineDO = ConvertHelper.convert(pipelineE, PipelineDO.class);
        pipelineDO.setIsEnabled(1);
        if (pipelineMapper.updateByPrimaryKey(pipelineDO) != 1) {
            throw new CommonException("error.update.pipeline");
        }
        return ConvertHelper.convert(pipelineDO, PipelineE.class);
    }

    @Override
    public PipelineE updateIsEnabled(Long pipelineId, Integer isEnabled) {
        PipelineDO pipelineDO = new PipelineDO();
        pipelineDO.setId(pipelineId);
        pipelineDO.setIsEnabled(isEnabled);
        pipelineDO.setObjectVersionNumber(pipelineMapper.selectByPrimaryKey(pipelineDO).getObjectVersionNumber());
        if (pipelineMapper.updateByPrimaryKeySelective(pipelineDO) != 1) {
            throw new CommonException("error.update.pipeline.is.enabled");
        }
        return ConvertHelper.convert(pipelineDO, PipelineE.class);
    }

    @Override
    public PipelineE queryById(Long pipelineId) {
        PipelineDO pipelineDO = new PipelineDO();
        pipelineDO.setId(pipelineId);
        return ConvertHelper.convert(pipelineMapper.selectByPrimaryKey(pipelineDO), PipelineE.class);
    }

    @Override
    public void delete(Long pipelineId) {
        PipelineDO pipelineDO = new PipelineDO();
        pipelineDO.setId(pipelineId);
        pipelineMapper.deleteByPrimaryKey(pipelineDO);
    }

    @Override
    public void checkName(Long projectId, String name) {
        PipelineDO pipelineDO = new PipelineDO();
        pipelineDO.setProjectId(projectId);
        pipelineDO.setName(name);
        if (pipelineMapper.select(pipelineDO).size() > 0) {
            throw new CommonException("error.pipeline.name.exit");
        }
    }

    @Override
    public List<PipelineE> queryByProjectId(Long projectId) {
        PipelineDO pipelineDO = new PipelineDO();
        pipelineDO.setProjectId(projectId);
        return ConvertHelper.convertList(pipelineMapper.select(pipelineDO), PipelineE.class);
    }
}
