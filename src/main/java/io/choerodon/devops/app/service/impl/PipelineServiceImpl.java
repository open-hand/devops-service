package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.PipelineVO;
import io.choerodon.devops.api.vo.cd.PipelineStageVO;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.app.service.PipelineStageService;
import io.choerodon.devops.app.service.PipelineVersionService;
import io.choerodon.devops.infra.dto.PipelineDTO;
import io.choerodon.devops.infra.dto.PipelineVersionDTO;
import io.choerodon.devops.infra.mapper.PipelineMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线表(Pipeline)应用服务
 *
 * @author
 * @since 2022-11-24 15:50:13
 */
@Service
public class PipelineServiceImpl implements PipelineService {
    private static final String DEVOPS_SAVE_PIPELINE_FAILED = "devops.save.pipeline.failed";

    @Autowired
    private PipelineMapper pipelineMapper;
    @Autowired
    private PipelineStageService pipelineStageService;
    @Autowired
    private PipelineVersionService pipelineVersionService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineDTO pipelineDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineMapper, pipelineDTO, DEVOPS_SAVE_PIPELINE_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineDTO create(Long projectId, PipelineVO pipelineVO) {

        //
        PipelineDTO pipelineDTO = ConvertUtils.convertObject(pipelineVO, PipelineDTO.class);
        baseCreate(pipelineDTO);

        PipelineVersionDTO pipelineVersionDTO = pipelineVersionService.createByPipelineId(pipelineDTO.getId());

        List<PipelineStageVO> stageList = pipelineVO.getStageList();
        if (CollectionUtils.isEmpty(stageList)) {
            throw new CommonException("devops.pipeline.stage.is.empty");
        }
        stageList.forEach(stage -> {
            pipelineStageService.saveStage(projectId, pipelineDTO.getId(), pipelineVersionDTO.getId(), stage);
        });
        return pipelineDTO;
    }

    @Override
    public void enable(Long projectId, Long id) {

    }
}

