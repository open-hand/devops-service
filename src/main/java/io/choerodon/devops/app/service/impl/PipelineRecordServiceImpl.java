package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.PipelineRecordService;
import io.choerodon.devops.infra.dto.PipelineRecordDTO;
import io.choerodon.devops.infra.mapper.PipelineRecordMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:12 2019/7/15
 * Description:
 */
@Service
public class PipelineRecordServiceImpl implements PipelineRecordService {
    private static final Gson gson = new Gson();

    @Autowired
    private PipelineRecordMapper pipelineRecordMapper;

    @Override
    public PageInfo<PipelineRecordDTO> basePageByOptions(Long projectId, Long pipelineId, PageRequest pageRequest, String params, Map<String, Object> classifyParam) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                pipelineRecordMapper.listByOptions(projectId, pipelineId, searchParamMap, paramMap, classifyParam));
    }

    @Override
    public PipelineRecordDTO baseCreate(PipelineRecordDTO pipelineRecordDTO) {
        if (pipelineRecordMapper.insert(pipelineRecordDTO) != 1) {
            throw new CommonException("error.insert.pipeline.record");
        }
        return pipelineRecordDTO;
    }

    @Override
    public PipelineRecordDTO baseUpdate(PipelineRecordDTO pipelineRecordDTO) {
        pipelineRecordDTO.setObjectVersionNumber(pipelineRecordMapper.selectByPrimaryKey(pipelineRecordDTO).getObjectVersionNumber());
        if (pipelineRecordMapper.updateByPrimaryKeySelective(pipelineRecordDTO) != 1) {
            throw new CommonException("error.update.pipeline.record");
        }
        return pipelineRecordDTO;
    }

    @Override
    public PipelineRecordDTO baseQueryById(Long recordId) {
        return pipelineRecordMapper.selectByPrimaryKey(recordId);
    }

    @Override
    public List<PipelineRecordDTO> baseQueryByPipelineId(Long pipelineId) {
        PipelineRecordDTO pipelineRecordDO = new PipelineRecordDTO();
        pipelineRecordDO.setPipelineId(pipelineId);
        return pipelineRecordMapper.select(pipelineRecordDO);
    }

    @Override
    public void baseUpdateWithEdited(Long pipelineId) {
        pipelineRecordMapper.updateEdited(pipelineId);
    }

    @Override
    public List<Long> baseQueryAllRecordUserIds(Long pipelineRecordId) {
        return pipelineRecordMapper.queryAllRecordUserIds(pipelineRecordId);
    }

}
