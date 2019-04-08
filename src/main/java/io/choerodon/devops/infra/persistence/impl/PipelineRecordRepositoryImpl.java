package io.choerodon.devops.infra.persistence.impl;

import com.google.gson.Gson;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.PipelineE;
import io.choerodon.devops.domain.application.entity.PipelineRecordE;
import io.choerodon.devops.domain.application.repository.PipelineRecordRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.PipelineRecordDO;
import io.choerodon.devops.infra.mapper.PipelineRecordMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:39 2019/4/4
 * Description:
 */
public class PipelineRecordRepositoryImpl implements PipelineRecordRepository {
    private static final Gson gson = new Gson();

    @Autowired
    private PipelineRecordMapper pipelineRecordMapper;

    @Override
    public Page<PipelineRecordE> listByOptions(Long projectId, Long pipelineId, PageRequest pageRequest, String params) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        Page<PipelineRecordDO> pipelineDOS = PageHelper.doPageAndSort(pageRequest, () ->
                pipelineRecordMapper.listByOptions(projectId, pipelineId, searchParamMap, paramMap));
        return ConvertPageHelper.convertPage(pipelineDOS, PipelineRecordE.class);
    }
}
