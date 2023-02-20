package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_MERGE_REQUEST_PASS;
import static java.util.stream.Collectors.toMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import io.choerodon.devops.api.vo.MergeRequestVO;
import io.choerodon.devops.api.vo.hrds.MemberPrivilegeViewDTO;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.DevopsMergeRequestPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.DevopsIssueRelObjectTypeEnum;
import io.choerodon.devops.infra.enums.MergeRequestState;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.HrdsCodeRepoClientOperator;
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
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
    @Value("${services.gitlab.url}")
    private String gitlabUrl;
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
    @Autowired
    private DevopsIssueRelService devopsIssueRelService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private HrdsCodeRepoClientOperator hrdsCodeRepoClientOperator;

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

    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = DEVOPS_MERGE_REQUEST_PASS,
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
                LOGGER.info("devops.save.merge.request");
            }
        } else {
            devopsMergeRequestDTO.setId(mergeRequestId);
            devopsMergeRequestDTO.setObjectVersionNumber(mergeRequestETemp.getObjectVersionNumber());
            if (baseUpdate(devopsMergeRequestDTO) == 0) {
                throw new CommonException("devops.update.merge.request");
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
            Set<Long> ids = new HashSet<>();
            ids.add(devopsBranchDTO.getId());
            Map<Long, List<Long>> issueIdsBranchIdMap = devopsIssueRelService.listMappedIssueIdsByObjectTypeAndObjectId(DevopsIssueRelObjectTypeEnum.BRANCH.getValue(), ids);
            List<Long> issueIds = issueIdsBranchIdMap.get(devopsBranchDTO.getId());
            if (!CollectionUtils.isEmpty(issueIds)) {
                // 如果该分支绑定了某个issue，先判断绑定的issue关联的所有分支是否已合并，都合并的话发送saga
                List<Long> issueIdsToSend = new ArrayList<>();
                issueIds.forEach(id -> {
                            Long issueId = devopsBranchDTO.getIssueId();
                            if (issueId != null) {
                                List<DevopsBranchDTO> devopsBranchDTOS = devopsBranchService.baseListDevopsBranchesByIssueId(issueId);
                                Set<String> branchNames = devopsBranchDTOS.stream().map(DevopsBranchDTO::getBranchName).collect(Collectors.toSet());
                                // 如果tag关联了2个及以上分支，那么已合并分支数量等于tag关联分支数量才发送saga
                                if (branchNames.size() >= 2) {
                                    Integer count = devopsMergeRequestMapper.countMergedBranchesByName(branchNames, gitlabProjectId);
                                    if (count == branchNames.size()) {
                                        issueIdsToSend.add(id);
                                    }
                                } else if (branchNames.size() == 1) {
                                    // 如果tag只关联一个分支，发送saga
                                    issueIdsToSend.add(id);
                                }
                            }
                        }
                );
                if (!CollectionUtils.isEmpty(issueIdsToSend)) {
                    applyMergeRequestSaga(issueIdsToSend, appServiceDTO);
                }
            }
            sendNotificationService.sendWhenMergeRequestPassed(gitProjectId, devopsMergeRequestDTO.getGitlabMergeRequestId(), operatorUserLoginName);
        }
    }

    @Override
    public DevopsMergeRequestDTO baseCountMergeRequest(Integer gitlabProjectId) {
        return devopsMergeRequestMapper.countMergeRequest(gitlabProjectId, DetailsHelper.getUserDetails() == null ? 0L : DetailsHelper.getUserDetails().getUserId());
    }

    @Override
    public Page<MergeRequestVO> getMergeRequestToBeChecked(Long projectId, Set<Long> appServiceIdsToSearch, String param, PageRequest pageRequest) {
        // 如果没有指定应用服务，那么查出当前项目下权限大于20的应用
        if (CollectionUtils.isEmpty(appServiceIdsToSearch)) {
            appServiceIdsToSearch = new HashSet<>();
            Set<Long> appServiceIds = applicationService.listAllIdsByProjectId(projectId);
            Map<Long, MemberPrivilegeViewDTO> memberPrivilegeViewDTOMap = hrdsCodeRepoClientOperator.selfPrivilege(null, projectId, appServiceIds).stream().collect(toMap(MemberPrivilegeViewDTO::getRepositoryId, Function.identity()));
            Set<Long> idSet = memberPrivilegeViewDTOMap.keySet();
            for (Long id : idSet) {
                MemberPrivilegeViewDTO memberPrivilegeViewDTO = memberPrivilegeViewDTOMap.get(id);
                if (memberPrivilegeViewDTO != null && memberPrivilegeViewDTO.getAccessLevel() != null && memberPrivilegeViewDTO.getAccessLevel() > 20) {
                    appServiceIdsToSearch.add(id);
                }
            }
        }

        Set<Long> finalAppServiceIdsToSearch = appServiceIdsToSearch;
        if (CollectionUtils.isEmpty(finalAppServiceIdsToSearch)) {
            return new Page<>();
        }
        Page<MergeRequestVO> mergeRequestVOPage = PageHelper.doPage(pageRequest, () -> devopsMergeRequestMapper.listMergeRequestToBeChecked(projectId, finalAppServiceIdsToSearch, param));
        Set<Long> gitlabUserIds = new HashSet<>();
        mergeRequestVOPage.getContent().forEach(mergeRequestVO -> {
            if (mergeRequestVO.getAuthorId() != null) {
                gitlabUserIds.add(mergeRequestVO.getAuthorId());
            }
            if (mergeRequestVO.getAssigneeId() != null) {
                gitlabUserIds.add(mergeRequestVO.getAssigneeId());
            }
        });
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Tenant tenant = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        List<UserAttrDTO> userAttrDTOS = userAttrService.baseListByGitlabUserIds(new ArrayList<>(gitlabUserIds));
        List<Long> iamUserIds = userAttrDTOS.stream().map(UserAttrDTO::getIamUserId).collect(Collectors.toList());
        Map<Long, Long> gitlabIamUserIdMap = userAttrDTOS.stream().collect(Collectors.toMap(UserAttrDTO::getGitlabUserId, UserAttrDTO::getIamUserId));
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByIds(iamUserIds);
        Map<Long, IamUserDTO> iamUserDTOMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getId, Function.identity()));
        mergeRequestVOPage.getContent().forEach(mergeRequestVO -> {
            String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
            String mergeRequestUrl = String.format("%s%s%s-%s/%s/merge_requests/%s",
                    gitlabUrl, urlSlash, tenant.getTenantNum(), projectDTO.getDevopsComponentCode(), mergeRequestVO.getAppServiceCode(), mergeRequestVO.getGitlabMergeRequestId());
            mergeRequestVO.setIamAuthor(ConvertUtils.convertObject(iamUserDTOMap.get(gitlabIamUserIdMap.get(mergeRequestVO.getAuthorId())), UserVO.class));
            mergeRequestVO.setIamAssignee(ConvertUtils.convertObject(iamUserDTOMap.get(gitlabIamUserIdMap.get(mergeRequestVO.getAssigneeId())), UserVO.class));
            mergeRequestVO.setGitlabUrl(mergeRequestUrl);
        });
        return mergeRequestVOPage;
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

    private void applyMergeRequestSaga(List<Long> issueIds, AppServiceDTO appServiceDTO) {
        DevopsMergeRequestPayload devopsMergeRequestPayload = new DevopsMergeRequestPayload();
        devopsMergeRequestPayload.setIssueIds(issueIds);
        devopsMergeRequestPayload.setServiceCode(appServiceDTO.getCode());
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
}
