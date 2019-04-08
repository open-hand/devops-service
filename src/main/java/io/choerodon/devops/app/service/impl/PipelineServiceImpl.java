package io.choerodon.devops.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.PipelineDTO;
import io.choerodon.devops.api.dto.PipelineRecordDTO;
import io.choerodon.devops.api.dto.PipelineStageRecordDTO;
import io.choerodon.devops.app.service.PipelineService;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.repository.PipelineRecordRepository;
import io.choerodon.devops.domain.application.repository.PipelineRepository;
import io.choerodon.devops.domain.application.repository.PipelineStageRecordRepository;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:57 2019/4/3
 * Description:
 */
public class PipelineServiceImpl implements PipelineService {
    @Autowired
    private PipelineRepository pipelineRepository;
    @Autowired
    private PipelineRecordRepository pipelineRecordRepository;
    @Autowired
    private PipelineStageRecordRepository stageRecordRepository;
    @Autowired
    private IamRepository iamRepository;

    @Override
    public Page<PipelineDTO> listByOptions(Long projectId, PageRequest pageRequest, String params) {
        Page<PipelineDTO> pipelineDTOS = ConvertPageHelper.convertPage(pipelineRepository.listByOptions(projectId, pageRequest, params), PipelineDTO.class);
        Page<PipelineDTO> page = new Page<>();
        page.setContent(pipelineDTOS.getContent().stream().peek(t -> {
            UserE userE = iamRepository.queryUserByUserId(t.getCreateBy());
            t.setCreateUserName(userE.getLoginName());
            t.setCreateUserUrl(userE.getImageUrl());
            t.setCreateUserRealName(userE.getRealName());
        }).collect(Collectors.toList()));
        return page;
    }

    @Override
    public Page<PipelineRecordDTO> listRecords(Long projectId, Long pipelineId, PageRequest pageRequest, String params) {
        Page<PipelineRecordDTO> pageRecordDTOS = ConvertPageHelper.convertPage(
                pipelineRecordRepository.listByOptions(projectId, pipelineId, pageRequest, params), PipelineRecordDTO.class);
        List<PipelineRecordDTO> pipelineRecordDTOS = pageRecordDTOS.getContent().stream().peek(t ->
                t.setStageDTOList(ConvertHelper.convertList(stageRecordRepository.list(projectId, pipelineId), PipelineStageRecordDTO.class)))
                .collect(Collectors.toList());
        pageRecordDTOS.setContent(pipelineRecordDTOS);
        return pageRecordDTOS;
    }
}

