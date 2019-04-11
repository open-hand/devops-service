package io.choerodon.devops.infra.persistence.impl;

import com.google.gson.Gson;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.PipelineValueE;
import io.choerodon.devops.domain.application.repository.PipelineValueRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.PipelineValueDO;
import io.choerodon.devops.infra.mapper.PipelineValueMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:04 2019/4/10
 * Description:
 */
@Component
public class PipelineValueRepositoryImpl implements PipelineValueRepository {
    private static final Gson gson = new Gson();

    @Autowired
    private PipelineValueMapper valueMapper;

    @Override
    public Page<PipelineValueE> listByOptions(Long projectId, Long appId, Long envId, PageRequest pageRequest, String params) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        Page<PipelineValueDO> devopsAutoDeployDOS = PageHelper
                .doPageAndSort(pageRequest, () -> valueMapper.listByOptions(projectId, appId, envId, searchParamMap, paramMap));
        return ConvertPageHelper.convertPage(devopsAutoDeployDOS, PipelineValueE.class);
    }

    @Override
    public PipelineValueE createOrUpdate(PipelineValueE pipelineRecordE) {
        PipelineValueDO pipelineValueDO = ConvertHelper.convert(pipelineRecordE, PipelineValueDO.class);
        if (pipelineValueDO.getId() != null) {
            if (valueMapper.insert(pipelineValueDO) != 1) {
                throw new CommonException("error.insert.pipeline.value");
            }
        } else {
            if (valueMapper.updateByPrimaryKey(pipelineValueDO) != 1) {
                throw new CommonException("error.insert.pipeline.value");
            }
            pipelineValueDO.setObjectVersionNumber(null);
        }
        return ConvertHelper.convert(valueMapper.selectOne(pipelineValueDO), PipelineValueE.class);
    }

    @Override
    public void delete(Long valueId) {
        PipelineValueDO pipelineValueDO = new PipelineValueDO();
        pipelineValueDO.setId(valueId);
        valueMapper.deleteByPrimaryKey(pipelineValueDO);
    }

    @Override
    public PipelineValueE queryById(Long valueId) {
        PipelineValueDO pipelineValueDO = new PipelineValueDO();
        pipelineValueDO.setId(valueId);
        return ConvertHelper.convert(valueMapper.selectByPrimaryKey(pipelineValueDO), PipelineValueE.class);
    }
}
