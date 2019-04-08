package io.choerodon.devops.infra.persistence.impl;

import com.google.gson.Gson;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.PipelineE;
import io.choerodon.devops.domain.application.repository.PipelineRepository;
import io.choerodon.devops.infra.common.util.PageRequestUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.PipelineDO;
import io.choerodon.devops.infra.mapper.PipelineMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:24 2019/4/4
 * Description:
 */
public class PipelineRepositoryImpl implements PipelineRepository {
    private static final Gson gson = new Gson();
    @Autowired
    private PipelineMapper pipelineMapper;

    @Override
    public Page<PipelineE> listByOptions(Long projectId, PageRequest pageRequest, String params) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        Page<PipelineDO> pipelineDOS = PageHelper.doPageAndSort(pageRequest, () ->
                pipelineMapper.listByOptions(projectId, searchParamMap, paramMap, PageRequestUtil.checkSortIsEmpty(pageRequest)));
        return ConvertPageHelper.convertPage(pipelineDOS, PipelineE.class);
    }
}
