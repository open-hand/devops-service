package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_GITLAB_CI_PIPELINE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.PipelineWebHookVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.mapper.DevopsCiJobRecordMapper;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineRecordMapper;
import org.springframework.stereotype.Service;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:26
 */
@Service
public class DevopsCiPipelineRecordServiceImpl implements DevopsCiPipelineRecordService {

    private DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper;
    private DevopsCiJobRecordService devopsCiJobRecordService;
    private DevopsCiJobRecordMapper devopsCiJobRecordMapper;
    private DevopsCiPipelineService devopsCiPipelineService;
    private AppServiceService applicationService;
    private TransactionalProducer transactionalProducer;
    private UserAttrService userAttrService;

    private ObjectMapper objectMapper = new ObjectMapper();

    public DevopsCiPipelineRecordServiceImpl(DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper, DevopsCiJobRecordService devopsCiJobRecordService, DevopsCiJobRecordMapper devopsCiJobRecordMapper, DevopsCiPipelineService devopsCiPipelineService, AppServiceService applicationService, TransactionalProducer transactionalProducer, UserAttrService userAttrService) {
        this.devopsCiPipelineRecordMapper = devopsCiPipelineRecordMapper;
        this.devopsCiJobRecordService = devopsCiJobRecordService;
        this.devopsCiJobRecordMapper = devopsCiJobRecordMapper;
        this.devopsCiPipelineService = devopsCiPipelineService;
        this.applicationService = applicationService;
        this.transactionalProducer = transactionalProducer;
        this.userAttrService = userAttrService;
    }

    @Override
    @Saga(code = DEVOPS_GITLAB_CI_PIPELINE, description = "gitlab ci pipeline创建到数据库", inputSchemaClass = PipelineWebHookVO.class)
    public void create(PipelineWebHookVO pipelineWebHookVO, String token) {
        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(token);
        DevopsCiPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(appServiceDTO.getId());
        if (devopsCiPipelineDTO == null || Boolean.FALSE.equals(devopsCiPipelineDTO.getEnabled())) {
            return;
        }
        pipelineWebHookVO.setToken(token);
        try {
            String input = objectMapper.writeValueAsString(pipelineWebHookVO);
            transactionalProducer.apply(
                    StartSagaBuilder.newBuilder()
                            .withRefType("app")
                            .withRefId(appServiceDTO.getId().toString())
                            .withSagaCode(DEVOPS_GITLAB_CI_PIPELINE)
                            .withJson(input),
                    builder -> {
                    });
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    public void handleCreate(PipelineWebHookVO pipelineWebHookVO) {
        AppServiceDTO applicationDTO = applicationService.baseQueryByToken(pipelineWebHookVO.getToken());
        DevopsCiPipelineDTO devopsCiPipelineDTO = devopsCiPipelineService.queryByAppSvcId(applicationDTO.getId());

        DevopsCiPipelineRecordDTO record = new DevopsCiPipelineRecordDTO();
        record.setCiPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordMapper.selectOne(record);
        Long iamUserId = getIamUserIdByGitlabUserName(pipelineWebHookVO.getUser().getUsername());

        //pipeline不存在则创建,存在则更新状态和阶段信息
        if (devopsCiPipelineRecordDTO == null) {
            devopsCiPipelineRecordDTO = new DevopsCiPipelineRecordDTO();
            devopsCiPipelineRecordDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
            devopsCiPipelineRecordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO.setTriggerUserId(iamUserId);
            devopsCiPipelineRecordDTO.setCommitSha(pipelineWebHookVO.getObjectAttributes().getSha());
            devopsCiPipelineRecordDTO.setCreatedDate(pipelineWebHookVO.getObjectAttributes().getCreatedAt());
            devopsCiPipelineRecordDTO.setFinishedDate(pipelineWebHookVO.getObjectAttributes().getFinishedAt());
            devopsCiPipelineRecordDTO.setDurationSeconds(pipelineWebHookVO.getObjectAttributes().getDuration());
            devopsCiPipelineRecordDTO.setStatus(pipelineWebHookVO.getObjectAttributes().getStatus());
            devopsCiPipelineRecordMapper.insertSelective(devopsCiPipelineRecordDTO);
            // 保存job执行记录
            Long pipelineRecordId = devopsCiPipelineRecordDTO.getId();
            pipelineWebHookVO.getBuilds().forEach(ciJobWebHookVO -> {
                DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByGitlabJobId(ciJobWebHookVO.getId());
                if (devopsCiJobRecordDTO == null) {
                    devopsCiJobRecordDTO = new DevopsCiJobRecordDTO();
                    devopsCiJobRecordDTO.setGitlabJobId(ciJobWebHookVO.getId());
                    devopsCiJobRecordDTO.setCiPipelineRecordId(pipelineRecordId);
                    devopsCiJobRecordDTO.setStartedDate(ciJobWebHookVO.getStartedAt());
                    devopsCiJobRecordDTO.setFinishedDate(ciJobWebHookVO.getFinishedAt());
                    devopsCiJobRecordDTO.setStage(ciJobWebHookVO.getStage());
                    devopsCiJobRecordDTO.setName(ciJobWebHookVO.getName());
                    devopsCiJobRecordDTO.setStatus(ciJobWebHookVO.getStatus());
                    devopsCiJobRecordDTO.setTriggerUserId(getIamUserIdByGitlabUserName(ciJobWebHookVO.getUser().getUsername()));
                    devopsCiJobRecordMapper.insertSelective(devopsCiJobRecordDTO);
                } else {
                    devopsCiJobRecordDTO.setCiPipelineRecordId(pipelineRecordId);
                    devopsCiJobRecordDTO.setStartedDate(ciJobWebHookVO.getStartedAt());
                    devopsCiJobRecordDTO.setFinishedDate(ciJobWebHookVO.getFinishedAt());
                    devopsCiJobRecordDTO.setStatus(ciJobWebHookVO.getStatus());
                    devopsCiJobRecordDTO.setTriggerUserId(getIamUserIdByGitlabUserName(ciJobWebHookVO.getUser().getUsername()));
                    devopsCiJobRecordMapper.updateByPrimaryKeySelective(devopsCiJobRecordDTO);
                }
            });

        } else {
            devopsCiPipelineRecordDTO.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
            devopsCiPipelineRecordDTO.setTriggerUserId(iamUserId);
            devopsCiPipelineRecordDTO.setCommitSha(pipelineWebHookVO.getObjectAttributes().getSha());
            devopsCiPipelineRecordDTO.setCreatedDate(pipelineWebHookVO.getObjectAttributes().getCreatedAt());
            devopsCiPipelineRecordDTO.setFinishedDate(pipelineWebHookVO.getObjectAttributes().getFinishedAt());
            devopsCiPipelineRecordDTO.setDurationSeconds(pipelineWebHookVO.getObjectAttributes().getDuration());
            devopsCiPipelineRecordDTO.setStatus(pipelineWebHookVO.getObjectAttributes().getStatus());
            devopsCiPipelineRecordMapper.updateByPrimaryKeySelective(devopsCiPipelineRecordDTO);
            // 更新job状态
            // 保存job执行记录
            pipelineWebHookVO.getBuilds().forEach(ciJobWebHookVO -> {
                DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByGitlabJobId(ciJobWebHookVO.getId());
                if (devopsCiJobRecordDTO != null) {
                    devopsCiJobRecordDTO.setStartedDate(ciJobWebHookVO.getStartedAt());
                    devopsCiJobRecordDTO.setFinishedDate(ciJobWebHookVO.getFinishedAt());
                    devopsCiJobRecordDTO.setStatus(ciJobWebHookVO.getStatus());
                    devopsCiJobRecordDTO.setTriggerUserId(getIamUserIdByGitlabUserName(ciJobWebHookVO.getUser().getUsername()));
                    devopsCiJobRecordMapper.updateByPrimaryKeySelective(devopsCiJobRecordDTO);
                }

            });
        }
    }

    private Long getIamUserIdByGitlabUserName(String username) {
        if ("admin1".equals(username) || "root".equals(username)) {
            username = "admin";
        }
        UserAttrDTO userAttrE = userAttrService.baseQueryByGitlabUserName(username);
        return userAttrE.getIamUserId();
    }
}
