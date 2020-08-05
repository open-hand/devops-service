package io.choerodon.devops.app.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;

import java.util.stream.Collectors;

import org.apache.tomcat.util.log.UserDataHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.PipelineConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class CiCdPipelineRecordServiceImpl implements CiCdPipelineRecordService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;

    @Autowired
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;

    @Autowired
    private DevopsCdStageRecordMapper devopsCdStageRecordMapper;
    @Autowired
    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;
    @Autowired
    @Lazy
    private DevopsCdJobRecordService devopsCdJobRecordService;
    @Autowired
    private DevopsCdStageRecordService devopsCdStageRecordService;

    @Autowired
    @Lazy
    private DevopsCdPipelineService devopsCdPipelineService;

    @Autowired
    private WorkFlowServiceOperator workFlowServiceOperator;

    @Autowired
    private DevopsCiPipelineService devopsCiPipelineService;

    @Autowired
    private DevopsCiStageMapper devopsCiStageMapper;

    @Autowired
    private AppServiceMapper appServiceMapper;

    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper;

    @Autowired
    private DevopsPipelineRecordRelService devopsPipelineRecordRelService;

    @Autowired
    private DevopsPipelineRecordRelMapper devopsPipelineRecordRelMapper;

    @Autowired
    private CheckGitlabAccessLevelService checkGitlabAccessLevelService;

    @Autowired
    private AppServiceService appServiceService;

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    @Autowired
    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;

    @Autowired
    private UserAttrService userAttrService;


    @Override
    public CiCdPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long recordRelId) {
        DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = devopsPipelineRecordRelMapper.selectByPrimaryKey(recordRelId);
        if (Objects.isNull(devopsPipelineRecordRelDTO)) {
            return null;
        }
        DevopsPipelineRecordRelVO devopsPipelineRecordRelVO = relDtoToRelVO(devopsPipelineRecordRelDTO);
        CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
        ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(devopsPipelineRecordRelVO.getId());
        DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = devopsCiPipelineRecordService.queryPipelineRecordDetails(projectId, devopsPipelineRecordRelVO.getCiPipelineRecordId());
        DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = devopsCdPipelineRecordService.queryPipelineRecordDetails(projectId, devopsPipelineRecordRelVO.getCdPipelineRecordId());
        //ci和cd都有记录
        List<StageRecordVO> stageRecordVOS = new ArrayList<>();
        if (devopsCiPipelineRecordVO != null && devopsCdPipelineRecordVO != null) {
            stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOList());
            stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
            ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
            ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());
            //计算记录的状态
            CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
            ciCdPipelineRecordVO.setCiRecordId(devopsCiPipelineRecordVO.getId());
            ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
            ciCdPipelineRecordVO.setCommit(devopsCiPipelineRecordVO.getCommit());
            ciCdPipelineRecordVO.setGitlabTriggerRef(devopsCiPipelineRecordVO.getGitlabTriggerRef());
            ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
            CiCdPipelineVO ciCdPipelineVO = ConvertUtils.convertObject(devopsCiPipelineRecordVO.getDevopsCiPipelineVO(), CiCdPipelineVO.class);
            //触发人员 执行时间 流程耗时
            fillPipelineVO(devopsCiPipelineRecordVO.getUsername(), stageRecordVOS, devopsCiPipelineRecordVO.getCreatedDate(), ciCdPipelineVO, ciCdPipelineRecordVO);
            //cicd 剔除跳过的阶段
            if (isFirstRecord(devopsPipelineRecordRelVO)) {
                ciCdPipelineRecordVO.setStageRecordVOS(null);
            }
            ciCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineRecordVO.getDevopsCdPipelineDeatilVO());
        }
        //纯ci
        if (devopsCiPipelineRecordVO != null && devopsCdPipelineRecordVO == null) {
            stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOList());
            ciCdPipelineRecordVO.setCommit(devopsCiPipelineRecordVO.getCommit());
            ciCdPipelineRecordVO.setGitlabTriggerRef(devopsCiPipelineRecordVO.getGitlabTriggerRef());
            ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
            ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());
            ciCdPipelineRecordVO.setStatus(devopsCiPipelineRecordVO.getStatus());
            ciCdPipelineRecordVO.setCiRecordId(devopsCiPipelineRecordVO.getId());
            ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
            CiCdPipelineVO ciCdPipelineVO = ConvertUtils.convertObject(devopsCiPipelineRecordVO.getDevopsCiPipelineVO(), CiCdPipelineVO.class);
            fillPipelineVO(devopsCiPipelineRecordVO.getUsername(), stageRecordVOS, devopsCiPipelineRecordVO.getCreatedDate(), ciCdPipelineVO, ciCdPipelineRecordVO);
        }
        //纯cd
        if (devopsCiPipelineRecordVO == null && devopsCdPipelineRecordVO != null) {
            stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
            ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
            ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
            ciCdPipelineRecordVO.setStatus(devopsCdPipelineRecordVO.getStatus());
            ciCdPipelineRecordVO.setCommit(devopsCdPipelineRecordVO.getCommit());
            ciCdPipelineRecordVO.setGitlabTriggerRef(devopsCdPipelineRecordVO.getRef());
            ciCdPipelineRecordVO.setCreatedDate(devopsCdPipelineRecordVO.getCreatedDate());
            CiCdPipelineVO ciCdPipelineVO = ConvertUtils.convertObject(devopsCdPipelineRecordVO.getCiCdPipelineVO(), CiCdPipelineVO.class);
            fillPipelineVO(devopsCdPipelineRecordVO.getUsername(), stageRecordVOS, devopsCdPipelineRecordVO.getCreatedDate(), ciCdPipelineVO, ciCdPipelineRecordVO);
            ciCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineRecordVO.getDevopsCdPipelineDeatilVO());
        }
        return ciCdPipelineRecordVO;
    }

    private boolean isFirstRecord(DevopsPipelineRecordRelVO devopsPipelineRecordRelVO) {
        //如果为流水线的下的第一条记录则返回为null
        Long pipelineId = devopsPipelineRecordRelVO.getPipelineId();
        DevopsPipelineRecordRelDTO recordRelDTO = new DevopsPipelineRecordRelDTO();
        recordRelDTO.setPipelineId(pipelineId);
        List<DevopsPipelineRecordRelDTO> select = devopsPipelineRecordRelMapper.select(recordRelDTO);
        if (select.size() == 1) {
            return true;
        }
        List<DevopsPipelineRecordRelVO> devopsPipelineRecordRelVOS = ConvertUtils.convertList(select, this::relDtoToRelVO);
        CiCdPipelineUtils.recordListSort(devopsPipelineRecordRelVOS);
        return devopsPipelineRecordRelVO.getId().compareTo(devopsPipelineRecordRelVOS.get(devopsPipelineRecordRelVOS.size() - 1).getId()) == 0 ? true : false;
    }

    private void fillPipelineVO(String userName, List<StageRecordVO> stageRecordVOS, Date executeDate, CiCdPipelineVO ciCdPipelineVO, CiCdPipelineRecordVO ciCdPipelineRecordVO) {
        ciCdPipelineVO.setCreateUserName(userName);
        if (!CollectionUtils.isEmpty(stageRecordVOS)) {
            Optional<Long> reduce = stageRecordVOS.stream().filter(stageRecordVO -> !Objects.isNull(stageRecordVO.getDurationSeconds())).map(StageRecordVO::getDurationSeconds).reduce((aLong, aLong2) -> aLong + aLong2);
            if (reduce != null && reduce.isPresent()) {
                ciCdPipelineVO.setTime(reduce.get());
            }
        }
        ciCdPipelineVO.setLatestExecuteDate(executeDate);
        ciCdPipelineRecordVO.setCiCdPipelineVO(ciCdPipelineVO);
    }

    @Override
    public void retryPipeline(Long projectId, Long pipelineRecordRelId, Long gitlabPipelineId, Long gitlabProjectId) {
        AppServiceDTO appServiceDTO = appServiceService.queryByGitlabProjectId(gitlabProjectId);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, appServiceDTO.getId(), AppServiceEvent.CICD_PIPELINE_RETRY);
        DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = devopsPipelineRecordRelService.queryById(pipelineRecordRelId);

        Long ciPipelineRecordId = devopsPipelineRecordRelDTO.getCiPipelineRecordId();
        Long cdPipelineRecordId = devopsPipelineRecordRelDTO.getCdPipelineRecordId();

        if (PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID.equals(ciPipelineRecordId)) {
            retryCdPipeline(projectId, cdPipelineRecordId);
        } else {
            // 查询ci阶段状态
            DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryById(ciPipelineRecordId);

            if (PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordDTO.getStatus())) {
                // ci成功 && 存在cd阶段 则重试cd
                if (!PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID.equals(cdPipelineRecordId)) {
                    retryCdPipeline(projectId, cdPipelineRecordId);
                }
            } else {
                // ci未完成，则重试ci
                devopsCiPipelineRecordService.retry(projectId, gitlabPipelineId, gitlabProjectId);
            }
        }
    }

    private void retryCdPipeline(Long projectId, Long cdPipelineRecordId) {
        retryCdPipeline(projectId, cdPipelineRecordId, false);
    }

    @Transactional
    @Override
    public void retryCdPipeline(Long projectId, Long cdPipelineRecordId, Boolean checkEnvPermission) {
        // 1.查询是否有任务可重试
        DevopsCdStageRecordDTO cdStageRecordDTO = devopsCdStageRecordMapper.queryFailedOrCancelStage(cdPipelineRecordId);
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.queryFailedOrCancelJob(cdStageRecordDTO.getId());
        if (ObjectUtils.isEmpty(cdStageRecordDTO) || ObjectUtils.isEmpty(cdJobRecordDTO)) {
            LOGGER.warn("no job or stage failed!!");
            return;
        }
        // 1.1 对于部署任务 校验环境权限
        if (checkEnvPermission && cdJobRecordDTO.getType().equals(JobTypeEnum.CD_DEPLOY.value())) {
            DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = devopsCdEnvDeployInfoService.queryById(cdJobRecordDTO.getDeployInfoId());
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsCdEnvDeployInfoDTO.getEnvId());
            UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        }

        // 2. 根据装填获取DevopsPipelineDTO
        DevopsPipelineDTO devopsPipelineDTO = devopsCdPipelineRecordService.createCDWorkFlowDTO(cdPipelineRecordId, true);

        // 3 更新business key 更新状态
        DevopsCdPipelineRecordDTO devopsCdPipelineRecordDTO = devopsCdPipelineRecordService.queryById(cdPipelineRecordId);
        devopsCdPipelineRecordDTO.setBusinessKey(GenerateUUID.generateUUID());
        devopsCdPipelineRecordDTO.setStatus(PipelineStatus.RUNNING.toValue());
        devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);
        devopsCdStageRecordService.updateStatusById(cdStageRecordDTO.getId(), PipelineStatus.RUNNING.toValue());
        devopsCdJobRecordService.updateStatusById(cdJobRecordDTO.getId(), PipelineStatus.RUNNING.toValue());

        try {
            CustomUserDetails details = DetailsHelper.getUserDetails();
            // 执行流水线
            devopsCdPipelineService.createWorkFlow(projectId, devopsPipelineDTO, details.getUsername(), details.getUserId(), details.getOrganizationId());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            devopsCdPipelineRecordDTO.setStatus(WorkFlowStatus.FAILED.toValue());
//            devopsCdPipelineRecordDTO.setErrorInfo(e.getMessage());
            devopsCdPipelineRecordService.update(devopsCdPipelineRecordDTO);
        }
    }

    @Override
    public void cancel(Long projectId, Long pipelineRecordRelId, Long gitlabPipelineId, Long gitlabProjectId) {
        DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO = devopsPipelineRecordRelService.queryById(pipelineRecordRelId);
        // 首先查询ci阶段状态
        Long ciPipelineRecordId = devopsPipelineRecordRelDTO.getCiPipelineRecordId();
        Long cdPipelineRecordId = devopsPipelineRecordRelDTO.getCdPipelineRecordId();

        if (PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID.equals(ciPipelineRecordId)) {
            cancelCdPipeline(ciPipelineRecordId);
        } else {
            // 查询ci阶段状态
            DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryById(ciPipelineRecordId);

            if (PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordDTO.getStatus())) {
                // ci成功 && 存在cd阶段 则取消cd
                if (!PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID.equals(cdPipelineRecordId)) {
                    cancelCdPipeline(cdPipelineRecordId);
                }
            } else {
                // ci未完成，则取消ci、cd
                devopsCiPipelineRecordService.cancel(projectId, gitlabPipelineId, gitlabProjectId);
                // 存在cd阶段 则同时取消cd
                if (!PipelineConstants.DEFAULT_CI_CD_PIPELINE_RECORD_ID.equals(cdPipelineRecordId)) {
                    cancelCdPipeline(cdPipelineRecordId);
                }
            }
        }

    }


    @Override
    public void executeNew(Long projectId, Long pipelineId, Long gitlabProjectId, String ref, Boolean tag) {
        CiCdPipelineVO ciCdPipelineVO = devopsCiPipelineService.queryById(pipelineId);
        checkGitlabAccessLevelService.checkGitlabPermission(projectId, ciCdPipelineVO.getAppServiceId(), AppServiceEvent.CICD_PIPELINE_NEW_PERFORM);
        DevopsCiStageDTO devopsCdStageDTO = new DevopsCiStageDTO();
        devopsCdStageDTO.setCiPipelineId(pipelineId);
        if (devopsCiStageMapper.selectCount(devopsCdStageDTO) == 0) {
            AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(ciCdPipelineVO.getAppServiceId());
            DevopsGitlabCommitDTO devopsGitlabCommitDTO = new DevopsGitlabCommitDTO();
            devopsGitlabCommitDTO.setAppServiceId(appServiceDTO.getId());
            devopsGitlabCommitDTO.setRef(ref);
            List<DevopsGitlabCommitDTO> devopsGitlabCommitDTOS = devopsGitlabCommitMapper.select(devopsGitlabCommitDTO);
            if (CollectionUtils.isEmpty(devopsGitlabCommitDTOS)) {
                throw new CommonException("error.no.commit.information.under.the.application.service");
            }
            Date commitDate = devopsGitlabCommitDTOS.get(0).getCommitDate();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
            String sinceDate = simpleDateFormat.format(commitDate);
            List<CommitDTO> commitDTOList = gitlabServiceClientOperator.getCommits(TypeUtil.objToInteger(gitlabProjectId), ref, sinceDate);
            if (CollectionUtils.isEmpty(commitDTOList)) {
                throw new CommonException("error.ref.no.commit");
            }
            devopsCdPipelineService.triggerCdPipeline(appServiceDTO.getToken(), commitDTOList.get(0).getId(), ref, tag,null);
        } else {
            devopsCiPipelineService.executeNew(projectId, pipelineId, gitlabProjectId, ref);
        }
    }

    @Transactional
    public void cancelCdPipeline(Long pipelineRecordId) {
        DevopsCdPipelineRecordDTO pipelineRecordDTO = devopsCdPipelineRecordService.queryById(pipelineRecordId);
        DevopsCdStageRecordDTO cdStageRecordDTO = devopsCdStageRecordMapper.queryPendingAndRunning(pipelineRecordId);
        DevopsCdJobRecordDTO cdJobRecordDTO = devopsCdJobRecordMapper.queryPendingAndRunning(cdStageRecordDTO.getId());

        if (!ObjectUtils.isEmpty(cdStageRecordDTO)) {
            devopsCdStageRecordService.updateStatusById(cdStageRecordDTO.getId(), PipelineStatus.CANCELED.toValue());
        }
        if (!ObjectUtils.isEmpty(cdJobRecordDTO)) {
            if (cdJobRecordDTO.getType().equals(JobTypeEnum.CD_DEPLOY.value())) {
                DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = devopsCdEnvDeployInfoService.queryById(cdJobRecordDTO.getDeployInfoId());
                DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsCdEnvDeployInfoDTO.getEnvId());
                UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
            }

            devopsCdJobRecordService.updateStatusById(cdJobRecordDTO.getId(), PipelineStatus.CANCELED.toValue());
        }

        devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.CANCELED.toValue());
        workFlowServiceOperator.stopInstance(pipelineRecordDTO.getProjectId(), pipelineRecordDTO.getBusinessKey());
    }

    @Override
    public Page<CiCdPipelineRecordVO> pagingPipelineRecord(Long projectId, Long pipelineId, PageRequest pageable) {
        Page<CiCdPipelineRecordVO> ciCdPipelineRecordVOPage = new Page<>();
        List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS = new ArrayList<>();
        Page<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS = devopsPipelineRecordRelService.pagingPipelineRel(pipelineId, pageable);
        if (Objects.isNull(devopsPipelineRecordRelDTOS) || CollectionUtils.isEmpty(devopsPipelineRecordRelDTOS.getContent())) {
            return null;
        }
        Page<CiCdPipelineRecordVO> cdPipelineRecordVOS = ConvertUtils.convertPage(devopsPipelineRecordRelDTOS, this::dtoToVo);
        cdPipelineRecordVOS.getContent().forEach(recordVO -> {
            DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = devopsCiPipelineRecordService.queryByCiPipelineRecordId(recordVO.getCiRecordId());
            DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = devopsCdPipelineRecordService.queryByCdPipelineRecordId(recordVO.getCdRecordId());
            if (devopsCiPipelineRecordVO != null && devopsCdPipelineRecordVO != null) {
                CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
                List<StageRecordVO> stageRecordVOS = new ArrayList<>();
                //收集这条ci流水线的所有stage
                ciCdPipelineRecordVO.setCiStatus(devopsCiPipelineRecordVO.getStatus());
                ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
                ciCdPipelineRecordVO.setCiRecordId(devopsCiPipelineRecordVO.getId());
                ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());

                stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOList());

                ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
                ciCdPipelineRecordVO.setCdStatus(devopsCdPipelineRecordVO.getStatus());
                stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
                ciCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineRecordVO.getDevopsCdPipelineDeatilVO());
                ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(recordVO.getDevopsPipelineRecordRelId());
                ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
                //计算状态
                CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
                ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);
            }
            if (devopsCiPipelineRecordVO != null && devopsCdPipelineRecordVO == null) {
                CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
                List<StageRecordVO> stageRecordVOS = new ArrayList<>();
                //收集这条ci流水线的所有stage
                ciCdPipelineRecordVO.setCiStatus(devopsCiPipelineRecordVO.getStatus());
                ciCdPipelineRecordVO.setCreatedDate(devopsCiPipelineRecordVO.getCreatedDate());
                ciCdPipelineRecordVO.setCiRecordId(devopsCiPipelineRecordVO.getId());
                ciCdPipelineRecordVO.setGitlabPipelineId(devopsCiPipelineRecordVO.getGitlabPipelineId());
                stageRecordVOS.addAll(devopsCiPipelineRecordVO.getStageRecordVOList());
                ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
                ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(recordVO.getDevopsPipelineRecordRelId());
                CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
                ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);
            }

            if (devopsCiPipelineRecordVO == null && devopsCdPipelineRecordVO != null) {
                CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
                List<StageRecordVO> stageRecordVOS = new ArrayList<>();
                //收集这条ci流水线的所有stage
                ciCdPipelineRecordVO.setCdStatus(devopsCdPipelineRecordVO.getStatus());
                ciCdPipelineRecordVO.setCreatedDate(devopsCdPipelineRecordVO.getCreatedDate());
                ciCdPipelineRecordVO.setCdRecordId(devopsCdPipelineRecordVO.getId());
                ciCdPipelineRecordVO.setGitlabPipelineId(Objects.isNull(devopsCdPipelineRecordVO.getGitlabPipelineId()) ? null : devopsCdPipelineRecordVO.getGitlabPipelineId());
                ciCdPipelineRecordVO.setDevopsCdPipelineDeatilVO(devopsCdPipelineRecordVO.getDevopsCdPipelineDeatilVO());
                stageRecordVOS.addAll(devopsCdPipelineRecordVO.getDevopsCdStageRecordVOS());
                ciCdPipelineRecordVO.setStageRecordVOS(stageRecordVOS);
                ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(recordVO.getDevopsPipelineRecordRelId());
                CiCdPipelineUtils.calculateStatus(ciCdPipelineRecordVO, devopsCiPipelineRecordVO, devopsCdPipelineRecordVO);
                ciCdPipelineRecordVOS.add(ciCdPipelineRecordVO);
            }
        });
        assemblePage(ciCdPipelineRecordVOS, ciCdPipelineRecordVOPage, devopsPipelineRecordRelDTOS);
        // 排序
        ciCdPipelineRecordVOPage.setContent(ciCdPipelineRecordVOS);
        CiCdPipelineUtils.recordListSort(ciCdPipelineRecordVOPage.getContent());
        return ciCdPipelineRecordVOPage;
    }

    private CiCdPipelineRecordVO dtoToVo(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO) {
        CiCdPipelineRecordVO ciCdPipelineRecordVO = new CiCdPipelineRecordVO();
        ciCdPipelineRecordVO.setDevopsPipelineRecordRelId(devopsPipelineRecordRelDTO.getId());
        ciCdPipelineRecordVO.setCiRecordId(devopsPipelineRecordRelDTO.getCiPipelineRecordId());
        ciCdPipelineRecordVO.setCdRecordId(devopsPipelineRecordRelDTO.getCdPipelineRecordId());
        return ciCdPipelineRecordVO;
    }

    private DevopsPipelineRecordRelVO relDtoToRelVO(DevopsPipelineRecordRelDTO devopsPipelineRecordRelDTO) {
        DevopsPipelineRecordRelVO devopsPipelineRecordRelVO = new DevopsPipelineRecordRelVO();
        BeanUtils.copyProperties(devopsPipelineRecordRelDTO, devopsPipelineRecordRelVO);
        devopsPipelineRecordRelVO.setCreatedDate(devopsPipelineRecordRelDTO.getCreationDate());
        return devopsPipelineRecordRelVO;
    }

    private void assemblePage(List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS, Page<CiCdPipelineRecordVO> ciCdPipelineRecordVO, Page<DevopsPipelineRecordRelDTO> devopsPipelineRecordRelDTOS) {
        //计算状态
//        calculateRecordStatus(ciCdPipelineRecordVOS);
        ciCdPipelineRecordVO.setTotalElements(devopsPipelineRecordRelDTOS.getTotalElements());
        ciCdPipelineRecordVO.setSize(devopsPipelineRecordRelDTOS.getSize());
        ciCdPipelineRecordVO.setNumber(devopsPipelineRecordRelDTOS.getNumber());
        ciCdPipelineRecordVO.setTotalPages(devopsPipelineRecordRelDTOS.getTotalPages());
        ciCdPipelineRecordVO.setNumberOfElements(devopsPipelineRecordRelDTOS.getNumberOfElements());
        ciCdPipelineRecordVO = ConvertUtils.convertPage(devopsPipelineRecordRelDTOS, CiCdPipelineRecordVO.class);
        ciCdPipelineRecordVO.setContent(ciCdPipelineRecordVOS);

    }

    private void calculateRecordStatus(List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS) {
        if (CollectionUtils.isEmpty(ciCdPipelineRecordVOS)) {
            return;
        }
        ciCdPipelineRecordVOS.forEach(ciCdPipelineRecordVO -> {
            if (JobStatusEnum.SUCCESS.value().equals(ciCdPipelineRecordVO.getCiStatus()) && JobStatusEnum.SUCCESS.value().equals(ciCdPipelineRecordVO.getCdStatus())) {
                ciCdPipelineRecordVO.setStatus(JobStatusEnum.SUCCESS.value());
            } else if (!JobStatusEnum.SUCCESS.value().equals(ciCdPipelineRecordVO.getCiStatus())) {
                ciCdPipelineRecordVO.setStatus(ciCdPipelineRecordVO.getCiStatus());
            } else if (JobStatusEnum.SUCCESS.value().equals(ciCdPipelineRecordVO.getCiStatus()) && JobStatusEnum.CREATED.value().equals(ciCdPipelineRecordVO.getCdStatus())) {
                ciCdPipelineRecordVO.setStatus(JobStatusEnum.RUNNING.value());
            } else {
                ciCdPipelineRecordVO.setStatus(ciCdPipelineRecordVO.getCdStatus());
            }
        });
    }

    private Page<CiCdPipelineRecordVO> assembleCdPage(Page<CiCdPipelineRecordVO> ciCdPipelineRecordVOPage,
                                                      List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS,
                                                      Page<DevopsCdPipelineRecordVO> devopsCdPipelineRecordVOS) {
        calculateRecordStatus(ciCdPipelineRecordVOS);
        ciCdPipelineRecordVOPage = ConvertUtils.convertPage(devopsCdPipelineRecordVOS, CiCdPipelineRecordVO.class);
        ciCdPipelineRecordVOPage.setContent(ciCdPipelineRecordVOS);
        return ciCdPipelineRecordVOPage;
    }


}
