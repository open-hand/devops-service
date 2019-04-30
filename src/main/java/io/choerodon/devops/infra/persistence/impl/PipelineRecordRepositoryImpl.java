package io.choerodon.devops.infra.persistence.impl;

import com.google.gson.Gson;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.PipelineRecordE;
import io.choerodon.devops.domain.application.repository.PipelineRecordRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.PipelineRecordDO;
import io.choerodon.devops.infra.mapper.PipelineRecordMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:39 2019/4/4
 * Description:
 */
@Component
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

    @Override
    public PipelineRecordE create(PipelineRecordE pipelineRecordE) {
        PipelineRecordDO pipelineRecordDO = ConvertHelper.convert(pipelineRecordE, PipelineRecordDO.class);
        if (pipelineRecordMapper.insert(pipelineRecordDO) != 1) {
            throw new CommonException("error.insert.pipeline.record");
        }
        return ConvertHelper.convert(pipelineRecordMapper.selectOne(pipelineRecordDO), PipelineRecordE.class);
    }

    @Override
    public PipelineRecordE update(PipelineRecordE pipelineRecordE) {
        System.out.println(pipelineRecordE.getStatus());
        PipelineRecordDO pipelineRecordDO = ConvertHelper.convert(pipelineRecordE, PipelineRecordDO.class);
        pipelineRecordDO.setObjectVersionNumber(pipelineRecordMapper.selectByPrimaryKey(pipelineRecordDO).getObjectVersionNumber());
        if (pipelineRecordMapper.updateByPrimaryKeySelective(pipelineRecordDO) != 1) {
            throw new CommonException("error.update.pipeline.record");
        }
        pipelineRecordDO.setObjectVersionNumber(null);
        return ConvertHelper.convert(pipelineRecordMapper.selectOne(pipelineRecordDO), PipelineRecordE.class);
    }


    @Override
    public PipelineRecordE queryById(Long recordId) {
        return ConvertHelper.convert(pipelineRecordMapper.queryById(recordId), PipelineRecordE.class);
    }

    @Override
    public List<PipelineRecordE> queryByPipelineId(Long pipelineId) {
        PipelineRecordDO pipelineRecordDO = new PipelineRecordDO();
        pipelineRecordDO.setPipelineId(pipelineId);
        return ConvertHelper.convertList(pipelineRecordMapper.select(pipelineRecordDO), PipelineRecordE.class);
    }
}
