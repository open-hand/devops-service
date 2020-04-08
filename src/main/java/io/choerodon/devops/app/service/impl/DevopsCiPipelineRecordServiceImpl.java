package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_GITLAB_CI_PIPELINE;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CiJobWebHookVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCiStageRecordVO;
import io.choerodon.devops.api.vo.PipelineWebHookVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.JobStatusEnum;
import io.choerodon.devops.infra.mapper.DevopsCiJobRecordMapper;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineRecordMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.PageRequestUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    private DevopsCiStageService devopsCiStageService;
    private DevopsCiJobService devopsCiJobService;
    private DevopsCiJobRecordMapper devopsCiJobRecordMapper;
    private DevopsCiPipelineService devopsCiPipelineService;
    private AppServiceService applicationService;
    private TransactionalProducer transactionalProducer;
    private UserAttrService userAttrService;

    private ObjectMapper objectMapper = new ObjectMapper();

    public DevopsCiPipelineRecordServiceImpl(DevopsCiPipelineRecordMapper devopsCiPipelineRecordMapper,
                                             DevopsCiJobRecordService devopsCiJobRecordService,
                                             DevopsCiStageService devopsCiStageService,
                                             DevopsCiJobService devopsCiJobService,
                                             DevopsCiJobRecordMapper devopsCiJobRecordMapper,
                                             DevopsCiPipelineService devopsCiPipelineService,
                                             AppServiceService applicationService,
                                             TransactionalProducer transactionalProducer,
                                             UserAttrService userAttrService) {
        this.devopsCiPipelineRecordMapper = devopsCiPipelineRecordMapper;
        this.devopsCiJobRecordService = devopsCiJobRecordService;
        this.devopsCiStageService = devopsCiStageService;
        this.devopsCiJobService = devopsCiJobService;
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
        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(devopsCiPipelineDTO.getId());
        List<DevopsCiJobDTO> devopsCiJobDTOS = devopsCiJobService.listByPipelineId(devopsCiPipelineDTO.getId());
        Map<Long, DevopsCiStageDTO> stageMap = devopsCiStageDTOList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));
        Map<String, DevopsCiJobDTO> jobMap = devopsCiJobDTOS.stream().collect(Collectors.toMap(v -> v.getName(), v -> v));
        // 检验是否是手动修改gitlab-ci.yaml文件生成的流水线记录
        for(CiJobWebHookVO job : pipelineWebHookVO.getBuilds()) {
            DevopsCiJobDTO devopsCiJobDTO = jobMap.get(job.getName());
            if (devopsCiJobDTO == null) {
                return;
            } else {
                DevopsCiStageDTO devopsCiStageDTO = stageMap.get(devopsCiJobDTO.getCiStageId());
                if (devopsCiStageDTO == null || !devopsCiStageDTO.getName().equals(job.getStage())) {
                    return;
                }
            }
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
        record.setGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
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

    @Override
    public PageInfo<DevopsCiPipelineRecordVO> pagingPipelineRecord(Long projectId, Long ciPipelineId, Pageable pageable) {
        PageInfo<DevopsCiPipelineRecordVO> pipelineRecordInfo = PageHelper
                .startPage(pageable.getPageNumber(), pageable.getPageSize(), PageRequestUtil.getOrderBy(pageable))
                .doSelectPageInfo(() -> devopsCiPipelineRecordMapper.listByCiPipelineId(ciPipelineId));
        List<DevopsCiPipelineRecordVO> pipelineRecordVOList = pipelineRecordInfo.getList();
        if (CollectionUtils.isEmpty(pipelineRecordVOList)) {
            return pipelineRecordInfo;
        }
        pipelineRecordVOList.forEach(pipelineRecord -> {
            // 查询流水线记录下的job记录
            DevopsCiJobRecordDTO recordDTO = new DevopsCiJobRecordDTO();
            recordDTO.setCiPipelineRecordId(pipelineRecord.getId());
            List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOS = devopsCiJobRecordMapper.select(recordDTO);
            Map<String, List<DevopsCiJobRecordDTO>> jobRecordMap = devopsCiJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getStage));
            // 查询阶段信息
            List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(ciPipelineId);
            List<DevopsCiStageRecordVO> devopsCiStageRecordVOS = ConvertUtils.convertList(devopsCiStageDTOList, DevopsCiStageRecordVO.class);

            // 计算stage状态
            devopsCiStageRecordVOS.forEach(stageRecord -> {
                List<DevopsCiJobRecordDTO> ciJobRecordDTOS = jobRecordMap.get(stageRecord.getName());
                if (!CollectionUtils.isEmpty(ciJobRecordDTOS)) {
                    Map<String, List<DevopsCiJobRecordDTO>> statsuMap = ciJobRecordDTOS.stream().collect(Collectors.groupingBy(DevopsCiJobRecordDTO::getStatus));

                    if (CollectionUtils.isEmpty(statsuMap.get(JobStatusEnum.CREATED.value()))) {
                        stageRecord.setStatus(JobStatusEnum.CREATED.value());
                    } else if (CollectionUtils.isEmpty(statsuMap.get(JobStatusEnum.PENDING.value()))) {
                        stageRecord.setStatus(JobStatusEnum.PENDING.value());
                    } else if (CollectionUtils.isEmpty(statsuMap.get(JobStatusEnum.RUNNING.value()))) {
                        stageRecord.setStatus(JobStatusEnum.RUNNING.value());
                    } else if (CollectionUtils.isEmpty(statsuMap.get(JobStatusEnum.FAILED.value()))) {
                        stageRecord.setStatus(JobStatusEnum.FAILED.value());
                    } else if (CollectionUtils.isEmpty(statsuMap.get(JobStatusEnum.SUCCESS.value()))) {
                        stageRecord.setStatus(JobStatusEnum.SUCCESS.value());
                    } else if (CollectionUtils.isEmpty(statsuMap.get(JobStatusEnum.CANCELED.value()))) {
                        stageRecord.setStatus(JobStatusEnum.CANCELED.value());
                    } else if (CollectionUtils.isEmpty(statsuMap.get(JobStatusEnum.SKIPPED.value()))) {
                        stageRecord.setStatus(JobStatusEnum.SKIPPED.value());
                    } else if (CollectionUtils.isEmpty(statsuMap.get(JobStatusEnum.MANUAL.value()))) {
                        stageRecord.setStatus(JobStatusEnum.MANUAL.value());
                    }
                }

            });
            // stage排序
            devopsCiStageRecordVOS = devopsCiStageRecordVOS.stream().sorted(Comparator.comparing(DevopsCiStageRecordVO::getSequence)).filter(v -> v.getStatus() != null).collect(Collectors.toList());
            pipelineRecord.setStageRecordVOList(devopsCiStageRecordVOS);
        });
        return pipelineRecordInfo;
    }

    private Long getIamUserIdByGitlabUserName(String username) {
        if ("admin1".equals(username) || "root".equals(username)) {
            username = "admin";
        }
        UserAttrDTO userAttrE = userAttrService.baseQueryByGitlabUserName(username);
        return userAttrE.getIamUserId();
    }
}
