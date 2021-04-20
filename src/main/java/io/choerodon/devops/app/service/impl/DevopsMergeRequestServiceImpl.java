package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_MERGE_REQUEST_PASS;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsMergeRequestVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.DevopsMergeRequestPayload;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsBranchService;
import io.choerodon.devops.app.service.DevopsMergeRequestService;
import io.choerodon.devops.app.service.SendNotificationService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.infra.enums.MergeRequestState;
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/15.
 */
@Service
public class DevopsMergeRequestServiceImpl implements DevopsMergeRequestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsMergeRequestServiceImpl.class);
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper;
    @Autowired
    @Lazy
    private SendNotificationService sendNotificationService;
    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private DevopsBranchService devopsBranchService;
    @Autowired
    private TransactionalProducer producer;

    @Override
    public List<DevopsMergeRequestDTO> baseListBySourceBranch(String sourceBranchName, Long gitLabProjectId) {
        return devopsMergeRequestMapper.listBySourceBranch(gitLabProjectId.intValue(), sourceBranchName);
    }

    @Override
    public DevopsMergeRequestDTO baseQueryByAppIdAndMergeRequestId(Long projectId, Long gitlabMergeRequestId) {
        DevopsMergeRequestDTO devopsMergeRequestDTO = new DevopsMergeRequestDTO();
        devopsMergeRequestDTO.setGitlabProjectId(projectId);
        devopsMergeRequestDTO.setGitlabMergeRequestId(gitlabMergeRequestId);
        return devopsMergeRequestMapper
                .selectOne(devopsMergeRequestDTO);
    }

    @Override
    public Page<DevopsMergeRequestDTO> basePageByOptions(Integer gitlabProjectId, String state, PageRequest pageable) {
        // 如果传入的state字段是这个值，表明的是查询待这个用户审核的MergeRequest
        if ("assignee".equals(state)) {
            return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () ->
                    devopsMergeRequestMapper.listToBeAuditedByThisUser(gitlabProjectId, DetailsHelper.getUserDetails() == null ? 0L : DetailsHelper.getUserDetails().getUserId()));
        } else {
            // 否则的话按照state字段查询
            DevopsMergeRequestDTO devopsMergeRequestDTO = new DevopsMergeRequestDTO();
            devopsMergeRequestDTO.setGitlabProjectId(gitlabProjectId.longValue());
            devopsMergeRequestDTO.setState(state);
            return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () ->
                    devopsMergeRequestMapper.select(devopsMergeRequestDTO));
        }
    }

    @Override
    public List<DevopsMergeRequestDTO> baseQueryByGitlabProjectId(Integer gitlabProjectId) {
        DevopsMergeRequestDTO devopsMergeRequestDTO = new DevopsMergeRequestDTO();
        devopsMergeRequestDTO.setGitlabProjectId(gitlabProjectId.longValue());
        return devopsMergeRequestMapper
                .select(devopsMergeRequestDTO);
    }

    @Override
    public Integer baseUpdate(DevopsMergeRequestDTO devopsMergeRequestDTO) {
        return devopsMergeRequestMapper.updateByPrimaryKey(devopsMergeRequestDTO);
    }

    @Override
    public void create(DevopsMergeRequestVO devopsMergeRequestVO, String token) {
        baseCreate(devopsMergeRequestVO, token);
    }

    @Saga(code = DEVOPS_MERGE_REQUEST_PASS,
            description = "合并请求通过的saga事件",
            inputSchemaClass = DevopsMergeRequestPayload.class
    )
    @Override
    public void baseCreate(DevopsMergeRequestVO devopsMergeRequestVO, String token) {
        DevopsMergeRequestDTO devopsMergeRequestDTO = voToDto(devopsMergeRequestVO);
        Long gitlabProjectId = devopsMergeRequestDTO.getGitlabProjectId();
        Long gitlabMergeRequestId = devopsMergeRequestDTO.getGitlabMergeRequestId();
        DevopsMergeRequestDTO mergeRequestETemp = baseQueryByAppIdAndMergeRequestId(gitlabProjectId, gitlabMergeRequestId);
        Long mergeRequestId = mergeRequestETemp != null ? mergeRequestETemp.getId() : null;
        if (mergeRequestId == null) {
            try {
                devopsMergeRequestMapper.insert(devopsMergeRequestDTO);
            } catch (Exception e) {
                LOGGER.info("error.save.merge.request");
            }
        } else {
            devopsMergeRequestDTO.setId(mergeRequestId);
            devopsMergeRequestDTO.setObjectVersionNumber(mergeRequestETemp.getObjectVersionNumber());
            if (baseUpdate(devopsMergeRequestDTO) == 0) {
                throw new CommonException("error.update.merge.request");
            }
        }

        // 发送关于Merge Request的相关通知以及saga
        String operatorUserLoginName = devopsMergeRequestVO.getUser() == null ? null : devopsMergeRequestVO.getUser().getUsername();
        Integer gitProjectId = TypeUtil.objToInteger(gitlabProjectId);
        if (MergeRequestState.OPENED.getValue().equals(devopsMergeRequestDTO.getState())) {
            sendNotificationService.sendWhenMergeRequestAuditEvent(gitProjectId, devopsMergeRequestDTO.getGitlabMergeRequestId());
        } else if (MergeRequestState.CLOSED.getValue().equals(devopsMergeRequestDTO.getState())) {
            sendNotificationService.sendWhenMergeRequestClosed(gitProjectId, devopsMergeRequestDTO.getGitlabMergeRequestId(), operatorUserLoginName);
        } else if (MergeRequestState.MERGED.getValue().equals(devopsMergeRequestDTO.getState())) {
            AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(token);
            DevopsBranchDTO devopsBranchDTO = devopsBranchService.baseQueryByAppAndBranchName(appServiceDTO.getId(), devopsMergeRequestVO.getObjectAttributes().getSourceBranch());
            // 如果该分支绑定了某个issue，那么发送saga
            if (devopsBranchDTO.getIssueId() != null) {
                DevopsMergeRequestPayload devopsMergeRequestPayload = new DevopsMergeRequestPayload();
                devopsMergeRequestPayload.setIssueId(devopsBranchDTO.getIssueId());
                devopsMergeRequestPayload.setServiceCode(appServiceDTO.getCode());
                devopsMergeRequestPayload.setSourceBranchName(devopsMergeRequestVO.getObjectAttributes().getSourceBranch());
                devopsMergeRequestPayload.setTargetBranchName(devopsMergeRequestVO.getObjectAttributes().getTargetBranch());
                devopsMergeRequestPayload.setProjectId(appServiceDTO.getProjectId());

                producer.apply(StartSagaBuilder
                                .newBuilder()
                                .withLevel(ResourceLevel.PROJECT)
                                .withSourceId(appServiceDTO.getProjectId())
                                .withRefType("tag")
                                .withSagaCode(SagaTopicCodeConstants.DEVOPS_MERGE_REQUEST_PASS),
                        builder -> builder
                                .withPayloadAndSerialize(devopsMergeRequestPayload)
                                .withRefId(appServiceDTO.getId().toString()));
            }

            sendNotificationService.sendWhenMergeRequestPassed(gitProjectId, devopsMergeRequestDTO.getGitlabMergeRequestId(), operatorUserLoginName);
        }
    }

    @Override
    public DevopsMergeRequestDTO baseCountMergeRequest(Integer gitlabProjectId) {
        return devopsMergeRequestMapper.countMergeRequest(gitlabProjectId, DetailsHelper.getUserDetails() == null ? 0L : DetailsHelper.getUserDetails().getUserId());
    }

    private DevopsMergeRequestDTO voToDto(DevopsMergeRequestVO devopsMergeRequestVO) {
        DevopsMergeRequestDTO devopsMergeRequestDTO = new DevopsMergeRequestDTO();
        devopsMergeRequestDTO.setGitlabProjectId(devopsMergeRequestVO.getProject().getId());
        devopsMergeRequestDTO.setGitlabMergeRequestId(devopsMergeRequestVO.getObjectAttributes().getIid());
        devopsMergeRequestDTO.setSourceBranch(devopsMergeRequestVO.getObjectAttributes().getSourceBranch());
        devopsMergeRequestDTO.setTargetBranch(devopsMergeRequestVO.getObjectAttributes().getTargetBranch());
        devopsMergeRequestDTO.setAuthorId(devopsMergeRequestVO.getObjectAttributes().getAuthorId());
        devopsMergeRequestDTO.setAssigneeId(devopsMergeRequestVO.getObjectAttributes().getAssigneeId());
        devopsMergeRequestDTO.setState(devopsMergeRequestVO.getObjectAttributes().getState());
        devopsMergeRequestDTO.setTitle(devopsMergeRequestVO.getObjectAttributes().getTitle());
        devopsMergeRequestDTO.setCreatedAt(devopsMergeRequestVO.getObjectAttributes().getCreatedAt());
        devopsMergeRequestDTO.setUpdatedAt(devopsMergeRequestVO.getObjectAttributes().getUpdatedAt());
        return devopsMergeRequestDTO;
    }
}
