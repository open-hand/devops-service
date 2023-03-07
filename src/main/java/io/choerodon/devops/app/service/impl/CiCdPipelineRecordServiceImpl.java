//package io.choerodon.devops.app.service.impl;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import com.google.gson.Gson;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.CollectionUtils;
//
//import io.choerodon.core.domain.Page;
//import io.choerodon.core.exception.CommonException;
//import io.choerodon.core.oauth.CustomUserDetails;
//import io.choerodon.core.oauth.DetailsHelper;
//import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
//import io.choerodon.devops.api.vo.CiPipelineRecordVO;
//import io.choerodon.devops.api.vo.DevopsPipelineRecordRelVO;
//import io.choerodon.devops.app.service.*;
//import io.choerodon.devops.infra.constant.PipelineCheckConstant;
//import io.choerodon.devops.infra.dto.*;
//import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
//import io.choerodon.devops.infra.enums.AppServiceEvent;
//import io.choerodon.devops.infra.enums.JobTypeEnum;
//import io.choerodon.devops.infra.enums.PipelineStatus;
//import io.choerodon.devops.infra.enums.WorkFlowStatus;
//import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
//import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator;
//import io.choerodon.devops.infra.mapper.*;
//import io.choerodon.devops.infra.util.CiCdPipelineUtils;
//import io.choerodon.devops.infra.util.ConvertUtils;
//import io.choerodon.devops.infra.util.GenerateUUID;
//import io.choerodon.mybatis.pagehelper.PageHelper;
//import io.choerodon.mybatis.pagehelper.domain.PageRequest;
//
//@Service
//public class CiCdPipelineRecordServiceImpl implements CiCdPipelineRecordService {
//    private static final Logger LOGGER = LoggerFactory.getLogger(CiCdPipelineRecordServiceImpl.class);
//
//    private static final String DEVOPS_REF_NO_COMMIT = "devops.ref.no.commit";
//
//    @Autowired
//    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
//
//    @Autowired
//    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;
//
//    @Autowired
//    private DevopsCdStageRecordMapper devopsCdStageRecordMapper;
//    @Autowired
//    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;
////    @Autowired
////    @Lazy
////    private DevopsCdJobRecordService devopsCdJobRecordService;
////    @Autowired
////    private DevopsCdStageRecordService devopsCdStageRecordService;
//
////    @Autowired
////    @Lazy
////    private DevopsCdPipelineService devopsCdPipelineService;
//
//    @Autowired
//    private WorkFlowServiceOperator workFlowServiceOperator;
//
//    @Autowired
//    @Lazy
//    private DevopsCiPipelineService devopsCiPipelineService;
//
//    @Autowired
//    private DevopsCiStageMapper devopsCiStageMapper;
//
//    @Autowired
//    private AppServiceMapper appServiceMapper;
//
//    @Autowired
//    private GitlabServiceClientOperator gitlabServiceClientOperator;
//
//    @Autowired
//    private DevopsPipelineRecordRelService devopsPipelineRecordRelService;
//
//    @Autowired
//    private DevopsPipelineRecordRelMapper devopsPipelineRecordRelMapper;
//
//    @Autowired
//    private CheckGitlabAccessLevelService checkGitlabAccessLevelService;
//
//    @Autowired
//    private AppServiceService appServiceService;
//
//    @Autowired
//    private DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;
//
//    @Autowired
//    private AppExternalConfigService appExternalConfigService;
//
//
//    private static final Gson gson = new Gson();
//
////    @Override
////    public CiCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long recordRelId) {
////        DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = devopsPipelineRecordRelMapper.selectByPrimaryKey(recordRelId);
////        if (Objects.isNull(devopsPipelineRecordRelDTO)) {
////            return new CiCdPipelineRecordVO();
////        }
////        CiCdPipelineDTO ciCdPipelineDTO = devopsCiCdPipelineMapper.selectByPrimaryKey(devopsPipelineRecordRelDTO.getPipelineId());
////        DevopsPipelineRecordRelVO devopsPipelineRecordRelVO = relDtoToRelVO(devopsPipelineRecordRelDTO);
////        CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
////        ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(devopsPipelineRecordRelVO.getId());
////        DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = devopsCiPipelineRecordService.queryPipelineRecordDetails(projectId, devopsPipelineRecordRelVO.getCiPipelineRecordId());
////        DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = devopsCdPipelineRecordService.queryPipelineRecordDetails(projectId, devopsPipelineRecordRelVO.getCdPipelineRecordId());
////        //ci和cd都有记录
////        List<StageRecordVO> stageRecordVOS = new ArrayList<>();
////        //cicd记录
////        if (!Objects.isNull(devopsCiPipelineRecordVO) && !CollectionUtils.isEmpty(devopsCiPipelineRecordVO.getStageRecordVOS())
////                && !Objects.isNull(devopsCdPipelineRecordVO) && !CollectionUtils.isEmpty(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS())) {
////            stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOS());
////            stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
////            ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
////            ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());
////            //计算记录的状态
////            CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
////            ciCdPipelineRecordVO.setCiRecordId(devopsCiPipelineRecordVO.getId());
////            ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
////            ciCdPipelineRecordVO.setCommit(devopsCiPipelineRecordVO.getCommit());
////            ciCdPipelineRecordVO.setGitlabTriggerRef(devopsCiPipelineRecordVO.getGitlabTriggerRef());
////            ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
////            CiCdPipelineVO ciCdPipelineVO = ConvertUtils.convertObject(devopsCiPipelineRecordVO.getCiCdPipelineVO(), CiCdPipelineVO.class);
////            //触发人员 执行时间 流程耗时
////            fillPipelineVO(devopsCiPipelineRecordVO.getUsername(), stageRecordVOS, devopsCiPipelineRecordVO.getCreatedDate(), ciCdPipelineVO, ciCdPipelineRecordVO);
////            //cicd 剔除跳过的阶段
//////            if (isFirstRecord(devopsPipelineRecordRelVO)) {
//////                ciCdPipelineRecordVO.setStageRecordVOS(null);
//////            }
////            ciCdPipelineRecordVO.setPipelineName(ciCdPipelineDTO.getName());
////            ciCdPipelineRecordVO.setGitlabProjectId(devopsCiPipelineRecordVO.getGitlabProjectId());
////            ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());
////            ciCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineRecordVO.getDevopsCdPipelineDeatilVO());
////        }
////        //纯ci
////        if ((!Objects.isNull(devopsCiPipelineRecordVO) && !CollectionUtils.isEmpty(devopsCiPipelineRecordVO.getStageRecordVOS()))
////                && (Objects.isNull(devopsCdPipelineRecordVO) || CollectionUtils.isEmpty(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS()))) {
////            stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOS());
////            ciCdPipelineRecordVO.setCommit(devopsCiPipelineRecordVO.getCommit());
////            ciCdPipelineRecordVO.setGitlabTriggerRef(devopsCiPipelineRecordVO.getGitlabTriggerRef());
////            ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
////            ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());
////            ciCdPipelineRecordVO.setStatus(devopsCiPipelineRecordVO.getStatus());
////            ciCdPipelineRecordVO.setCiRecordId(devopsCiPipelineRecordVO.getId());
////            ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
////            CiCdPipelineVO ciCdPipelineVO = ConvertUtils.convertObject(devopsCiPipelineRecordVO.getCiCdPipelineVO(), CiCdPipelineVO.class);
////            fillPipelineVO(devopsCiPipelineRecordVO.getUsername(), stageRecordVOS, devopsCiPipelineRecordVO.getCreatedDate(), ciCdPipelineVO, ciCdPipelineRecordVO);
////            ciCdPipelineRecordVO.setPipelineName(ciCdPipelineDTO.getName());
////            ciCdPipelineRecordVO.setGitlabProjectId(devopsCiPipelineRecordVO.getGitlabProjectId());
////            ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());
////        }
////        //纯cd
////        if ((Objects.isNull(devopsCiPipelineRecordVO) || CollectionUtils.isEmpty(devopsCiPipelineRecordVO.getStageRecordVOS()))
////                && (!Objects.isNull(devopsCdPipelineRecordVO) && !CollectionUtils.isEmpty(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS()))) {
////            stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
////            ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
////            ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
////            ciCdPipelineRecordVO.setStatus(devopsCdPipelineRecordVO.getStatus());
////            ciCdPipelineRecordVO.setCommit(devopsCdPipelineRecordVO.getCommit());
////            ciCdPipelineRecordVO.setGitlabTriggerRef(devopsCdPipelineRecordVO.getRef());
////            ciCdPipelineRecordVO.setCreatedDate(devopsCdPipelineRecordVO.getCreatedDate());
////            CiCdPipelineVO ciCdPipelineVO = ConvertUtils.convertObject(devopsCdPipelineRecordVO.getCiCdPipelineVO(), CiCdPipelineVO.class);
////            fillPipelineVO(devopsCdPipelineRecordVO.getUsername(), stageRecordVOS, devopsCdPipelineRecordVO.getCreatedDate(), ciCdPipelineVO, ciCdPipelineRecordVO);
////            ciCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineRecordVO.getDevopsCdPipelineDeatilVO());
////            ciCdPipelineRecordVO.setPipelineName(ciCdPipelineDTO.getName());
////            ciCdPipelineRecordVO.setGitlabPipelineId(devopsCdPipelineRecordVO.getGitlabPipelineId());
////        }
////        //处理viewId
////        ciCdPipelineRecordVO.setViewId(CiCdPipelineUtils.handleId(ciCdPipelineRecordVO.getDevopsPipelineRecordRelId()));
////        return ciCdPipelineRecordVO;
////    }
//
////    private void fillPipelineVO(String userName, List<StageRecordVO> stageRecordVOS, Date executeDate, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineRecordVO ciCdPipelineRecordVO) {
////        ciCdPipelineVO.setCreateUserName(userName);
////        if (!CollectionUtils.isEmpty(stageRecordVOS)) {
////            Optional<Long> reduce = stageRecordVOS.stream().filter(stageRecordVO -> !Objects.isNull(stageRecordVO.getDurationSeconds())).map(StageRecordVO::getDurationSeconds).reduce((aLong, aLong2) -> aLong + aLong2);
////            reduce.ifPresent(ciCdPipelineVO::setTime);
////        }
////        ciCdPipelineVO.setLatestExecuteDate(executeDate);
////        ciCdPipelineRecordVO.setCiCdPipelineVO(ciCdPipelineVO);
////    }
//
//    @Override
//    public void retryPipeline(Long projectId, Long ciPipelineRecordId, Long gitlabProjectId) {
//        AppServiceDTO appServiceDTO = appServiceService.queryByGitlabProjectId(gitlabProjectId);
//        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceDTO.getId(), AppServiceEvent.CICD_PIPELINE_RETRY);
////        DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = devopsPipelineRecordRelService.queryById(pipelineRecordRelId);
//
////        Long ciPipelineRecordId = devopsPipelineRecordRelDTO.getCiPipelineRecordId();
////        Long cdPipelineRecordId = devopsPipelineRecordRelDTO.getCdPipelineRecordId();
//
////        if (PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID.equals(ciPipelineRecordId)) {
////            retryCdPipeline(projectId, cdPipelineRecordId);
////        } else {
//        // 查询ci阶段状态
//        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryById(ciPipelineRecordId);
//
////            if (PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordDTO.getStatus())) {
////                // ci成功 && 存在cd阶段 则重试cd
////                if (!PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID.equals(cdPipelineRecordId)) {
////                    retryCdPipeline(projectId, cdPipelineRecordId);
////                }
////            } else {
//        // ci未完成，则重试ci
//        // 更新cd阶段状态为created
////                if (!PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID.equals(cdPipelineRecordId)) {
////                    updateCanceledStageAndJob2Created(cdPipelineRecordId);
////                }
//        devopsCiPipelineRecordService.retry(projectId, devopsCiPipelineRecordDTO.getGitlabPipelineId(), gitlabProjectId);
////            }
////        }
//    }
//
//    private void updateCanceledStageAndJob2Created(Long cdPipelineRecordId) {
//        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryByPipelineRecordId(cdPipelineRecordId);
//        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
//            devopsCdStageRecordDTOS.forEach(stage -> {
//                if (PipelineStatus.CANCELED.toValue().equals(stage.getStatus())) {
//                    devopsCdStageRecordService.updateStatusById(stage.getId(), PipelineStatus.CREATED.toValue());
//                    List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryByStageRecordId(stage.getId());
//                    if (!CollectionUtils.isEmpty(devopsCdJobRecordDTOS)) {
//                        devopsCdJobRecordDTOS.forEach(job -> {
//                            if (PipelineStatus.CANCELED.toValue().equals(job.getStatus())) {
//                                devopsCdJobRecordService.updateStatusById(job.getId(), PipelineStatus.CREATED.toValue());
//                            }
//                        });
//
//                    }
//                }
//            });
//        }
//    }
//
////    private void retryCdPipeline(Long projectId, Long cdPipelineRecordId) {
////        retryCdPipeline(projectId, cdPipelineRecordId, false);
////    }
//
//    @Transactional
//    @Override
//    public void retryCdPipeline(Long projectId, Long cdPipelineRecordId, Boolean checkEnvPermission) {
//        // 1.查询是否有任务可重试
//        DevopsCdStageRecordDTO firstStage;
//        DevopsCdJobRecordDTO firstJob;
//
//        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(cdPipelineRecordId);
//        if (PipelineStatus.CANCELED.toValue().equals(devopsCdPipelineRecordDTO.getStatus())) {
//            firstStage = devopsCdStageRecordMapper.queryFirstStageByPipelineRecordIdAndStatus(cdPipelineRecordId, PipelineStatus.CANCELED.toValue());
//            firstJob = devopsCdJobRecordMapper.queryFirstJobByStageRecordIdAndStatus(firstStage.getId(), PipelineStatus.CANCELED.toValue());
//
//            List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryStageWithPipelineRecordIdAndStatus(cdPipelineRecordId, PipelineStatus.CANCELED.toValue());
//            devopsCdStageRecordDTOS.forEach(devopsCdStageRecordDTO -> {
//                devopsCdStageRecordService.updateStatusById(devopsCdStageRecordDTO.getId(), PipelineStatus.CREATED.toValue());
//                List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryJobWithStageRecordIdAndStatus(devopsCdStageRecordDTO.getId(), PipelineStatus.CANCELED.toValue());
//                devopsCdJobRecordDTOS.forEach(devopsCdJobRecordDTO -> devopsCdJobRecordService.updateStatusById(devopsCdJobRecordDTO.getId(), PipelineStatus.CREATED.toValue()));
//            });
//        } else if (PipelineStatus.FAILED.toValue().equals(devopsCdPipelineRecordDTO.getStatus())) {
//            firstStage = devopsCdStageRecordMapper.queryFirstStageByPipelineRecordIdAndStatus(cdPipelineRecordId, PipelineStatus.FAILED.toValue());
//            firstJob = devopsCdJobRecordMapper.queryFirstJobByStageRecordIdAndStatus(firstStage.getId(), PipelineStatus.FAILED.toValue());
//        } else {
//            return;
//        }
//        // 2. 根据装填获取DevopsPipelineDTO
//
//        String businessKey = GenerateUUID.generateUUID();
//        DevopsPipelineDTO devopsPipelineDTO = devopsCdPipelineRecordService.createCDWorkFlowDTO(cdPipelineRecordId, true);
//        devopsPipelineDTO.setBusinessKey(businessKey);
//        // 3 更新business key 更新状态
//        devopsCdPipelineRecordDTO.setBusinessKey(businessKey);
//        devopsCdPipelineRecordDTO.setBpmDefinition(gson.toJson(devopsPipelineDTO));
//        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
//        devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);
//        devopsCdStageRecordService.updateStatusById(firstStage.getId(), PipelineStatus.RUNNING.toValue());
//        devopsCdJobRecordService.updateStatusById(firstJob.getId(), PipelineStatus.RUNNING.toValue());
//
//        try {
//            CustomUserDetails details = DetailsHelper.getUserDetails();
//            // 执行流水线
//            devopsCdPipelineService.createWorkFlow(projectId, devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
//        } catch (Exception e) {
//            LOGGER.error(e.getMessage());
//            devopsCdPipelineRecordDTO.setStatus(WorkFlowStatus.FAILED.toValue());
//            devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);
//        }
//    }
//
//    @Override
//    @Transactional
//    public void cancel(Long projectId, Long ciPipelineRecordId, Long gitlabProjectId) {
////        DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = devopsPipelineRecordRelService.queryById(pipelineRecordRelId);
////        // 首先查询ci阶段状态
////        Long ciPipelineRecordId = devopsPipelineRecordRelDTO.getCiPipelineRecordId();
////        Long cdPipelineRecordId = devopsPipelineRecordRelDTO.getCdPipelineRecordId();
//
////        if (PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID.equals(ciPipelineRecordId)) {
////            cancelCdPipeline(cdPipelineRecordId);
////        } else {
////            // 查询ci阶段状态
//        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryById(ciPipelineRecordId);
////
////            if (PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordDTO.getStatus())) {
////                // ci成功 && 存在cd阶段 则取消cd
////                if (!PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID.equals(cdPipelineRecordId)) {
////                    cancelCdPipeline(cdPipelineRecordId);
////                }
////            } else {
//        // ci未完成，则取消ci
//        devopsCiPipelineRecordService.cancel(projectId, devopsCiPipelineRecordDTO.getGitlabPipelineId(), gitlabProjectId);
////            }
////        }
//
//    }
//
//
//    @Override
//    @Transactional
//    public void executeNew(Long projectId, Long pipelineId, Long gitlabProjectId, String ref, Boolean tag, Map<String, String> variables) {
////        CiCdPipelineVO ciCdPipelineVO = devopsCiPipelineService.queryById(pipelineId);
////        DevopsCiStageDTO devopsCiStageDTO = new DevopsCiStageDTO();
////        devopsCiStageDTO.setCiPipelineId(pipelineId);
////        if (devopsCiStageMapper.selectCount(devopsCiStageDTO) == 0) {
////            AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(ciCdPipelineVO.getAppServiceId());
////
////            AppExternalConfigDTO appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
////            String sha;
////            List<CommitDTO> commitDTOList;
////            if (appExternalConfigDTO == null) {
////                commitDTOList = gitlabServiceClientOperator.getCommits(TypeUtil.objToInteger(gitlabProjectId), ref);
////                if (CollectionUtils.isEmpty(commitDTOList)) {
////                    throw new CommonException(DEVOPS_REF_NO_COMMIT);
////                }
////            } else {
////                commitDTOList = gitlabServiceClientOperator.listExternalCommits(TypeUtil.objToInteger(gitlabProjectId), 1, 5, appExternalConfigDTO);
////            }
////            sha = commitDTOList.get(0).getId();
////
////            devopsCdPipelineService.triggerCdPipeline(projectId, appServiceDTO.getToken(), sha, ref, tag, null);
////        } else {
//        devopsCiPipelineService.executeNew(projectId, pipelineId, gitlabProjectId, ref, variables);
////        }
//    }
//
////    @Transactional
////    public void cancelCdPipeline(Long pipelineRecordId) {
////        DevopsCdPipelineRecordDTO pipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
////        // 只有未执行、执行中的流水线可以取消执行
////
////        if (PipelineStatus.CREATED.toValue().equals(pipelineRecordDTO.getStatus())) {
////            cancelCreateOrRunningStageAndJob(pipelineRecordId);
////        } else if (PipelineStatus.RUNNING.toValue().equals(pipelineRecordDTO.getStatus())) {
////            List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordService.queryStageWithPipelineRecordIdAndStatus(pipelineRecordId, PipelineStatus.RUNNING.toValue());
////
////            if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
////                DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordDTOS.get(0);
////                List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = devopsCdJobRecordService.queryJobWithStageRecordIdAndStatus(devopsCdStageRecordDTO.getId(), PipelineStatus.RUNNING.toValue());
////                if (!CollectionUtils.isEmpty(devopsCdJobRecordDTOS)) {
////                    DevopsCdJobRecordDTO devopsCdJobRecordDTO = devopsCdJobRecordDTOS.get(0);
////                    if (JobTypeEnum.CD_AUDIT.value().equals(devopsCdJobRecordDTO.getType())) {
////                        throw new CommonException(PipelineCheckConstant.DEVOPS_CANCEL_AUDITING_PIPELINE);
////                    }
////                }
////            }
////            cancelCreateOrRunningStageAndJob(pipelineRecordId);
////        } else {
////            return;
////        }
////        // 修改流水线记录状态为cancel
////        devopsCdPipelineRecordService.updateStatusById(pipelineRecordDTO.getId(), PipelineStatus.CANCELED.toValue());
////        workFlowServiceOperator.stopInstance(pipelineRecordDTO.getProjectId(), pipelineRecordDTO.getBusinessKey());
////    }
////
////    private void cancelCreateOrRunningStageAndJob(Long pipelineRecordId) {
////        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = new ArrayList<>();
////        List<DevopsCdStageRecordDTO> createdRecordDTOS = devopsCdStageRecordService.queryStageWithPipelineRecordIdAndStatus(pipelineRecordId, PipelineStatus.CREATED.toValue());
////        List<DevopsCdStageRecordDTO> runningRecordDTOS = devopsCdStageRecordService.queryStageWithPipelineRecordIdAndStatus(pipelineRecordId, PipelineStatus.RUNNING.toValue());
////        if (!CollectionUtils.isEmpty(createdRecordDTOS)) {
////            devopsCdStageRecordDTOS.addAll(createdRecordDTOS);
////        }
////        if (!CollectionUtils.isEmpty(runningRecordDTOS)) {
////            devopsCdStageRecordDTOS.addAll(runningRecordDTOS);
////        }
////        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
////            devopsCdStageRecordDTOS.forEach(devopsCdStageRecordDTO -> {
////                devopsCdStageRecordService.updateStatusById(devopsCdStageRecordDTO.getId(), PipelineStatus.CANCELED.toValue());
////                List<DevopsCdJobRecordDTO> devopsCdJobRecordDTOS = new ArrayList<>();
////                List<DevopsCdJobRecordDTO> createdJobRecordDTOS = devopsCdJobRecordService.queryJobWithStageRecordIdAndStatus(devopsCdStageRecordDTO.getId(), PipelineStatus.CREATED.toValue());
////                List<DevopsCdJobRecordDTO> runningJobRecordDTOS = devopsCdJobRecordService.queryJobWithStageRecordIdAndStatus(devopsCdStageRecordDTO.getId(), PipelineStatus.RUNNING.toValue());
////                if (!CollectionUtils.isEmpty(createdJobRecordDTOS)) {
////                    devopsCdJobRecordDTOS.addAll(createdJobRecordDTOS);
////                }
////                if (!CollectionUtils.isEmpty(runningJobRecordDTOS)) {
////                    devopsCdJobRecordDTOS.addAll(runningJobRecordDTOS);
////                }
////                if (!CollectionUtils.isEmpty(devopsCdJobRecordDTOS)) {
////                    devopsCdJobRecordDTOS.forEach(devopsCdJobRecordDTO -> devopsCdJobRecordService.updateStatusById(devopsCdJobRecordDTO.getId(), PipelineStatus.CANCELED.toValue()));
////                }
////            });
////        }
////    }
//
//    @Override
//    public Page<CiPipelineRecordVO> pagingPipelineRecord(Long projectId, Long pipelineId, PageRequest pageable) {
////        Page<CiCdPipelineRecordVO> ciCdPipelineRecordVOPage = new Page<>();
////        List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS = new ArrayList<>();
////        Page<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS = devopsPipelineRecordRelService.pagingPipelineRel(pipelineId, pageable);
////        if (Objects.isNull(devopsPipelineRecordRelDTOS) || CollectionUtils.isEmpty(devopsPipelineRecordRelDTOS.getContent())) {
////            return new Page<>();
////        }
//        Page<CiPipelineRecordVO> recordPage = PageHelper.doPage(pageable, () -> devopsCiPipelineRecordService.listByPipelineId(pipelineId));
////        CiCdPipelineVO ciCdPipelineVO = devopsCiPipelineService.queryById(pipelineId);
////        Page<CiCdPipelineRecordVO> cdPipelineRecordVOS = ConvertUtils.convertPage(devopsPipelineRecordRelDTOS, this::dtoToVo);
//        recordPage.forEach(recordVO -> {
//            devopsCiPipelineRecordService.fillAdditionalInfo(recordVO);
//            recordVO.setViewId(CiCdPipelineUtils.handleId(recordVO.getId()));
//
//            // 填充前端需要的字段
//            recordVO.setCiRecordId(recordVO.getId());
//            recordVO.setPipelineId(recordVO.getCiPipelineId());
//        });
//
////        cdPipelineRecordVOS.getContent().forEach(recordVO -> {
////            DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = devopsCiPipelineRecordService.queryByCiPipelineRecordId(recordVO.getCiRecordId());
////            DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = devopsCdPipelineRecordService.queryByCdPipelineRecordId(recordVO.getCdRecordId());
////            if (devopsCiPipelineRecordVO != null && devopsCdPipelineRecordVO != null) {
////                CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
////                List<StageRecordVO> stageRecordVOS = new ArrayList<>();
////                //收集这条ci流水线的所有stage
////                ciCdPipelineRecordVO.setCiStatus(devopsCiPipelineRecordVO.getStatus());
////                ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
////                ciCdPipelineRecordVO.setCiRecordId(devopsCiPipelineRecordVO.getId());
////                ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());
////                ciCdPipelineRecordVO.setGitlabProjectId(ciCdPipelineVO.getGitlabProjectId());
////
////                stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOList());
////
////                ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
////                ciCdPipelineRecordVO.setCdStatus(devopsCdPipelineRecordVO.getStatus());
////                stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
////                ciCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineRecordVO.getDevopsCdPipelineDeatilVO());
////                ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(recordVO.getDevopsPipelineRecordRelId());
////                ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
////                //计算状态
////                CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
////                ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);
////            }
////            if (devopsCiPipelineRecordVO != null && devopsCdPipelineRecordVO == null) {
////                CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
////                List<StageRecordVO> stageRecordVOS = new ArrayList<>();
////                //收集这条ci流水线的所有stage
////                ciCdPipelineRecordVO.setCiStatus(devopsCiPipelineRecordVO.getStatus());
////                ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
////                ciCdPipelineRecordVO.setCiRecordId(devopsCiPipelineRecordVO.getId());
////                ciCdPipelineRecordVO.setGitlabProjectId(ciCdPipelineVO.getGitlabProjectId());
////                ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());
////                stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOList());
////                ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
////                ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(recordVO.getDevopsPipelineRecordRelId());
////                CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
////                ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);
////            }
////
////            if (devopsCiPipelineRecordVO == null && devopsCdPipelineRecordVO != null) {
////                CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
////                List<StageRecordVO> stageRecordVOS = new ArrayList<>();
////                //收集这条ci流水线的所有stage
////                ciCdPipelineRecordVO.setCdStatus(devopsCdPipelineRecordVO.getStatus());
////                ciCdPipelineRecordVO.setCreatedDate(devopsCdPipelineRecordVO.getCreatedDate());
////                ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
////                ciCdPipelineRecordVO.setGitlabProjectId(ciCdPipelineVO.getGitlabProjectId());
////                ciCdPipelineRecordVO.setGitlabPipelineId(Objects.isNull(devopsCdPipelineRecordVO.getGitlabPipelineId()) ? null : devopsCdPipelineRecordVO.getGitlabPipelineId());
////                ciCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineRecordVO.getDevopsCdPipelineDeatilVO());
////                stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
////                ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
////                ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(recordVO.getDevopsPipelineRecordRelId());
////                CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
////                ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);
////            }
////        });
////        assemblePage(ciCdPipelineRecordVOS, ciCdPipelineRecordVOPage, devopsPipelineRecordRelDTOS);
//        // 排序
////        ciCdPipelineRecordVOPage.setContent(ciCdPipelineRecordVOS);
////        CiCdPipelineUtils.recordListSort(ciCdPipelineRecordVOPage.getContent());
//        //填充viewId
//
//        return recordPage;
//    }
//
//
////    private CiCdPipelineRecordVO dtoToVo(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO) {
////        CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
////        ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(devopsPipelineRecordRelDTO.getId());
////        ciCdPipelineRecordVO.setCiRecordId(devopsPipelineRecordRelDTO.getCiPipelineRecordId());
////        ciCdPipelineRecordVO.setCdRecordId(devopsPipelineRecordRelDTO.getCdPipelineRecordId());
////        return ciCdPipelineRecordVO;
////    }
////
////    private DevopsPipelineRecordRelVO relDtoToRelVO(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO) {
////        DevopsPipelineRecordRelVO devopsPipelineRecordRelVO = new DevopsPipelineRecordRelVO();
////        BeanUtils.copyProperties(devopsPipelineRecordRelDTO, devopsPipelineRecordRelVO);
////        devopsPipelineRecordRelVO.setCreatedDate(devopsPipelineRecordRelDTO.getCreationDate());
////        return devopsPipelineRecordRelVO;
////    }
////
////    private void assemblePage(List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS, Page<CiCdPipelineRecordVO> ciCdPipelineRecordVO, Page<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS) {
////        ciCdPipelineRecordVO.setTotalElements(devopsPipelineRecordRelDTOS.getTotalElements());
////        ciCdPipelineRecordVO.setSize(devopsPipelineRecordRelDTOS.getSize());
////        ciCdPipelineRecordVO.setNumber(devopsPipelineRecordRelDTOS.getNumber());
////        ciCdPipelineRecordVO.setTotalPages(devopsPipelineRecordRelDTOS.getTotalPages());
////        ciCdPipelineRecordVO.setNumberOfElements(devopsPipelineRecordRelDTOS.getNumberOfElements());
////        ciCdPipelineRecordVO = ConvertUtils.convertPage(devopsPipelineRecordRelDTOS, CiCdPipelineRecordVO.class);
////        ciCdPipelineRecordVO.setContent(ciCdPipelineRecordVOS);
////
////    }
//}
