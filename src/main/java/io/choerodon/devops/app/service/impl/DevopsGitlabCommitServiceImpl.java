package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_GIT_TAG_DELETE;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.hzero.mybatis.BatchInsertHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.CommitFormRecordVO;
import io.choerodon.devops.api.vo.CommitFormUserVO;
import io.choerodon.devops.api.vo.DevopsGitlabCommitVO;
import io.choerodon.devops.api.vo.PushWebHookVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.DevopsGitlabTagPayload;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsBranchService;
import io.choerodon.devops.app.service.DevopsGitlabCommitService;
import io.choerodon.devops.app.service.DevopsIssueRelService;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;
import io.choerodon.devops.infra.dto.DevopsIssueRelDTO;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.DevopsIssueRelObjectTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.IamAdminIdHolder;
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


@Service
public class DevopsGitlabCommitServiceImpl implements DevopsGitlabCommitService {

    private static final Gson gson = new Gson();
    private static final Integer ADMIN = 1;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper;
    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsBranchService devopsBranchService;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    @Qualifier("devopsIssueRelBatchInsertHelper")
    private BatchInsertHelper<DevopsIssueRelDTO> batchInsertHelper;
    @Autowired
    private DevopsIssueRelService devopsIssueRelService;

    @Override
    public void create(PushWebHookVO pushWebHookVO, String token, String operate) {
        AppServiceDTO applicationDTO = applicationService.baseQueryByToken(token);
        String ref = pushWebHookVO.getRef().split("/")[2];
        if (!pushWebHookVO.getCommits().isEmpty()) {
            DevopsBranchDTO devopsBranchDTO = devopsBranchService.baseQueryByAppAndBranchNameWithIssueIds(applicationDTO.getId(), ref);
            pushWebHookVO.getCommits().forEach(commitDTO -> {
                DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.baseQueryByShaAndRef(commitDTO.getId(), ref);

                if (devopsGitlabCommitDTO == null) {
                    devopsGitlabCommitDTO = new DevopsGitlabCommitDTO();
                    devopsGitlabCommitDTO.setAppServiceId(applicationDTO.getId());
                    devopsGitlabCommitDTO.setCommitContent(commitDTO.getMessage());
                    devopsGitlabCommitDTO.setCommitSha(commitDTO.getId());
                    devopsGitlabCommitDTO.setRef(ref);
                    devopsGitlabCommitDTO.setUrl(commitDTO.getUrl());
                    if ("root".equals(commitDTO.getAuthor().getName())) {
                        devopsGitlabCommitDTO.setUserId(IamAdminIdHolder.getAdminId());
                    } else {
                        IamUserDTO iamUserDTO = baseServiceClientOperator.queryByEmail(applicationDTO.getProjectId(),
                                commitDTO.getAuthor().getEmail());
                        if (iamUserDTO != null) {
                            devopsGitlabCommitDTO.setUserId(iamUserDTO.getId());
                        }
                    }
                    devopsGitlabCommitDTO.setCommitDate(commitDTO.getTimestamp());
                    devopsGitlabCommitService.baseCreate(devopsGitlabCommitDTO);
                    // 如果分支和issue关联了，且此次操作为提交代码，添加关联关系
                    if (!CollectionUtils.isEmpty(devopsBranchDTO.getIssueIds()) && operate.equals(GitOpsConstants.COMMIT)) {
                        devopsIssueRelService.addRelation(DevopsIssueRelObjectTypeEnum.COMMIT.getValue(), devopsGitlabCommitDTO.getId(), devopsBranchDTO.getId(), applicationDTO.getProjectId(), applicationDTO.getCode(), devopsBranchDTO.getIssueIds());
                    }
                }
            });
        } else {
            //直接从一个分支切出来另外一个分支，没有commits记录（所以下面插入的commit不需要关联issueId）
            DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.baseQueryByShaAndRef(pushWebHookVO.getCheckoutSha(), ref);
            if (devopsGitlabCommitDTO == null) {
                CommitDTO commitDTO = gitlabServiceClientOperator.queryCommit(TypeUtil.objToInteger(applicationDTO.getGitlabProjectId()), pushWebHookVO.getCheckoutSha(), ADMIN);
                devopsGitlabCommitDTO = new DevopsGitlabCommitDTO();
                devopsGitlabCommitDTO.setAppServiceId(applicationDTO.getId());
                devopsGitlabCommitDTO.setCommitContent(commitDTO.getMessage());
                devopsGitlabCommitDTO.setCommitSha(commitDTO.getId());
                devopsGitlabCommitDTO.setRef(ref);
                devopsGitlabCommitDTO.setUrl(commitDTO.getUrl());
                if ("root".equals(commitDTO.getAuthorName())) {
                    devopsGitlabCommitDTO.setUserId(IamAdminIdHolder.getAdminId());
                } else {
                    IamUserDTO userE = baseServiceClientOperator.queryByEmail(applicationDTO.getProjectId(),
                            commitDTO.getAuthorEmail());
                    if (userE != null) {
                        devopsGitlabCommitDTO.setUserId(userE.getId());
                    }
                }
                devopsGitlabCommitDTO.setCommitDate(commitDTO.getCommittedDate());
                devopsGitlabCommitService.baseCreate(devopsGitlabCommitDTO);
            }
        }

    }

    @Override
    @Saga(code = DEVOPS_GIT_TAG_DELETE, description = "删除tag", inputSchemaClass = DevopsGitlabTagPayload.class)
    public void deleteTag(PushWebHookVO pushWebHookVO, String token) {
        String ref = pushWebHookVO.getRef().split("/")[2];
        AppServiceDTO appServiceDTO = applicationService.baseQueryByToken(token);

        DevopsGitlabTagPayload devopsGitlabTagPayload = new DevopsGitlabTagPayload();
        devopsGitlabTagPayload.setProjectId(appServiceDTO.getProjectId());
        devopsGitlabTagPayload.setServiceCode(appServiceDTO.getCode());
        devopsGitlabTagPayload.setTag(ref);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(appServiceDTO.getProjectId())
                        .withRefType("tag")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_GIT_TAG_DELETE),
                builder -> builder
                        .withPayloadAndSerialize(devopsGitlabTagPayload)
                        .withRefId(appServiceDTO.getId().toString()));
    }

    @Override
    public DevopsGitlabCommitVO queryCommits(Long projectId, String appServiceIds, Date startDate, Date
            endDate) {

        List<Long> appServiceIdsMap = gson.fromJson(appServiceIds, new TypeToken<List<Long>>() {
        }.getType());
        if (appServiceIdsMap.isEmpty()) {
            return new DevopsGitlabCommitVO();
        }

        // 查询应用列表下所有commit记录
        List<DevopsGitlabCommitDTO> devopsGitlabCommitDTOS = devopsGitlabCommitService
                .baseListByOptions(projectId, appServiceIdsMap, startDate, endDate);
        if (devopsGitlabCommitDTOS.isEmpty()) {
            return new DevopsGitlabCommitVO();
        }

        // 获得去重后的所有用户信息
        Map<Long, IamUserDTO> userMap = getUserDOMap(devopsGitlabCommitDTOS);

        // 获取用户分别的commit
        List<CommitFormUserVO> commitFormUserVOS = getCommitFormUserDTOList(devopsGitlabCommitDTOS, userMap);

        // 获取总的commit(将所有用户的commit_date放入一个数组)，按照时间先后排序
        List<Date> totalCommitsDate = getTotalDates(commitFormUserVOS);
        Collections.sort(totalCommitsDate);

        return new DevopsGitlabCommitVO(commitFormUserVOS, totalCommitsDate);
    }

    @Override
    public Page<CommitFormRecordVO> pageRecordCommits(Long projectId, String appServiceIds, PageRequest
            pageable, Date startDate, Date endDate) {

        List<Long> appServiceIdsMap = gson.fromJson(appServiceIds, new TypeToken<List<Long>>() {
        }.getType());
        if (appServiceIdsMap.isEmpty()) {
            return new Page<>();
        }

        // 查询应用列表下所有commit记录
        List<DevopsGitlabCommitDTO> devopsGitlabCommitES = devopsGitlabCommitService
                .baseListByOptions(projectId, appServiceIdsMap, startDate, endDate);
        Map<Long, IamUserDTO> userMap = getUserDOMap(devopsGitlabCommitES);
        // 获取最近的commit(返回所有的commit记录，按时间先后排序，分页查询)
        return getCommitFormRecordDTOS(projectId, appServiceIdsMap, pageable, userMap, startDate, endDate);
    }

    @Override
    public Page<CommitFormRecordVO> listUserRecentCommits(List<ProjectDTO> projectDTOList, PageRequest pageable, Date time) {
        List<Long> projectIds = projectDTOList.stream().map(ProjectDTO::getId).collect(Collectors.toList());
        Long userId = DetailsHelper.getUserDetails().getUserId();
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userId);
        // 获取最近的commit(返回所有的commit记录，按时间先后排序，分页查询)
        Page<DevopsGitlabCommitDTO> devopsGitlabCommitDTOPage = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                () -> devopsGitlabCommitMapper.listUserRecentCommits(projectIds, userId, new java.sql.Date(time.getTime())));
        List<CommitFormRecordVO> commitFormRecordVOList = new ArrayList<>();
        devopsGitlabCommitDTOPage.getContent().forEach(e -> {
            Long eUserId = e.getUserId();
            CommitFormRecordVO commitFormRecordVO;
            commitFormRecordVO = new CommitFormRecordVO(eUserId, e, iamUserDTO.getLdap(), iamUserDTO.getLoginName(), iamUserDTO.getRealName(), iamUserDTO.getEmail(), iamUserDTO.getImageUrl());
            commitFormRecordVOList.add(commitFormRecordVO);
        });
        Page<CommitFormRecordVO> commitFormRecordVOPageInfo = new Page<>();
        BeanUtils.copyProperties(devopsGitlabCommitDTOPage, commitFormRecordVOPageInfo);
        commitFormRecordVOPageInfo.setContent(commitFormRecordVOList);

        return commitFormRecordVOPageInfo;
    }

    private Map<Long, IamUserDTO> getUserDOMap(List<DevopsGitlabCommitDTO> devopsGitlabCommitDTOS) {
        // 获取users
        List<IamUserDTO> userEList = baseServiceClientOperator.listUsersByIds(devopsGitlabCommitDTOS.stream().map(
                DevopsGitlabCommitDTO::getUserId).distinct().collect(Collectors.toList()));

        return userEList.stream().collect(Collectors.toMap(IamUserDTO::getId, u -> u, (u1, u2) -> u1));
    }

    private List<CommitFormUserVO> getCommitFormUserDTOList
            (List<DevopsGitlabCommitDTO> devopsGitlabCommitDTOS,
             Map<Long, IamUserDTO> userMap) {
        List<CommitFormUserVO> commitFormUserVOS = new ArrayList<>();
        // 遍历list，key为userid，value为list
        Map<Long, List<DevopsGitlabCommitDTO>> map = new HashMap<>();
        for (DevopsGitlabCommitDTO devopsGitlabCommitDTO : devopsGitlabCommitDTOS) {
            Long userId = devopsGitlabCommitDTO.getUserId();
            if (userId == null && !map.containsKey(0L)) {
                List<DevopsGitlabCommitDTO> commitDTOS = new ArrayList<>();
                commitDTOS.add(devopsGitlabCommitDTO);
                map.put(0L, commitDTOS);
            } else if (userId == null && map.containsKey(0L)) {
                map.get(0L).add(devopsGitlabCommitDTO);
            } else if (userId != null && !map.containsKey(userId)) {
                List<DevopsGitlabCommitDTO> commitDTOS = new ArrayList<>();
                commitDTOS.add(devopsGitlabCommitDTO);
                map.put(userId, commitDTOS);
            } else {
                map.get(userId).add(devopsGitlabCommitDTO);
            }
        }
        map.forEach((userId, list) -> {
            IamUserDTO iamUserDTO = userMap.get(userId);
            if (!ObjectUtils.isEmpty(iamUserDTO)) {
                String loginName = iamUserDTO.getLdap() ? iamUserDTO.getLoginName() : iamUserDTO.getEmail();
                String name = iamUserDTO.getRealName() + "(" + loginName + ")";
                String imgUrl = iamUserDTO.getImageUrl();
                // 遍历list，将每个用户的所有commit date取出放入List<Date>，然后保存为DTO
                List<Date> date = new ArrayList<>();
                list.forEach(e -> date.add(e.getCommitDate()));
                commitFormUserVOS.add(new CommitFormUserVO(userId, name, imgUrl, date));
            } else {
                String name = "Unknown" + "(" + userId + ")";
                List<Date> date = new ArrayList<>();
                list.forEach(e -> date.add(e.getCommitDate()));
                commitFormUserVOS.add(new CommitFormUserVO(userId, name, null, date));
            }
        });
        return commitFormUserVOS;
    }

    private Page<CommitFormRecordVO> getCommitFormRecordDTOS(Long projectId, List<Long> appServiceIds, PageRequest pageable,
                                                             Map<Long, IamUserDTO> userMap, Date startDate, Date endDate) {
        return devopsGitlabCommitService.basePageByOptions(projectId, appServiceIds, pageable, userMap, startDate, endDate);
    }

    private List<Date> getTotalDates(List<CommitFormUserVO> commitFormUserVOS) {
        List<Date> totalCommitsDate = new ArrayList<>();
        commitFormUserVOS.forEach(e -> totalCommitsDate.addAll(e.getCommitDates()));
        return totalCommitsDate;
    }

    @Override
    public DevopsGitlabCommitDTO baseCreate(DevopsGitlabCommitDTO devopsGitlabCommitDTO) {
        if (!checkExist(devopsGitlabCommitDTO)) {
            if (devopsGitlabCommitMapper.insert(devopsGitlabCommitDTO) != 1) {
                throw new CommonException("error.gitlab.commit.create");
            }
        }
        return devopsGitlabCommitDTO;
    }

    @Override
    public DevopsGitlabCommitDTO baseQueryByShaAndRef(String sha, String ref) {
        DevopsGitlabCommitDTO devopsGitlabCommitDTO = new DevopsGitlabCommitDTO();
        devopsGitlabCommitDTO.setCommitSha(sha);
        devopsGitlabCommitDTO.setRef(ref);
        return devopsGitlabCommitMapper.selectOne(devopsGitlabCommitDTO);
    }

    @Override
    public List<DevopsGitlabCommitDTO> baseListByOptions(Long projectId, List<Long> appServiceIds, Date
            startDate, Date endDate) {
        List<DevopsGitlabCommitDTO> devopsGitlabCommitDOList = devopsGitlabCommitMapper
                .listCommits(projectId, appServiceIds, new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
        if (devopsGitlabCommitDOList == null || devopsGitlabCommitDOList.isEmpty()) {
            return new ArrayList<>();
        }
        return devopsGitlabCommitDOList;
    }

    @Override
    public Page<CommitFormRecordVO> basePageByOptions(Long projectId, List<Long> appServiceIds,
                                                      PageRequest pageable, Map<Long, IamUserDTO> userMap,
                                                      Date startDate, Date endDate) {
        Page<DevopsGitlabCommitDTO> devopsGitlabCommitDTOPage = PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                () -> devopsGitlabCommitMapper.listCommits(projectId, appServiceIds, new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime())));
        return getCommitFormRecordVOPage(userMap, devopsGitlabCommitDTOPage);
    }

    private Page<CommitFormRecordVO> getCommitFormRecordVOPage(Map<Long, IamUserDTO> userMap, Page<DevopsGitlabCommitDTO> devopsGitlabCommitDTOPage) {
        List<CommitFormRecordVO> commitFormRecordVOList = new ArrayList<>();
        devopsGitlabCommitDTOPage.getContent().forEach(e -> {
            Long eUserId = e.getUserId();
            IamUserDTO user = userMap.get(eUserId);
            CommitFormRecordVO commitFormRecordVO;
            if (user != null) {
                String loginName = user.getLdap() ? user.getLoginName() : user.getEmail();
                commitFormRecordVO = new CommitFormRecordVO(
                        eUserId, user.getImageUrl(), user.getRealName() + " " + loginName, e);
            } else {
                commitFormRecordVO = new CommitFormRecordVO(
                        null, null, null, e);
            }
            commitFormRecordVOList.add(commitFormRecordVO);
        });
        Page<CommitFormRecordVO> commitFormRecordVOPageInfo = new Page<>();
        BeanUtils.copyProperties(devopsGitlabCommitDTOPage, commitFormRecordVOPageInfo);
        commitFormRecordVOPageInfo.setContent(commitFormRecordVOList);

        return commitFormRecordVOPageInfo;
    }

    @Override
    public void baseUpdate(DevopsGitlabCommitDTO devopsGitlabCommitDTO) {
        DevopsGitlabCommitDTO oldDevopsGitlabCommitDO = devopsGitlabCommitMapper.selectByPrimaryKey(devopsGitlabCommitDTO.getId());
        devopsGitlabCommitDTO.setObjectVersionNumber(oldDevopsGitlabCommitDO.getObjectVersionNumber());
        if (devopsGitlabCommitMapper.updateByPrimaryKeySelective(devopsGitlabCommitDTO) != 1) {
            throw new CommonException("error.gitlab.commit.update");
        }
    }

    @Override
    public List<DevopsGitlabCommitDTO> baseListByAppIdAndBranch(Long appServiceIds, String branch, Date
            startDate) {
        return devopsGitlabCommitMapper.queryByAppIdAndBranch(appServiceIds, branch, startDate == null ? null : new java.sql.Date(startDate.getTime()));
    }

    @Override
    public Set<Long> listIdsByCommitSha(Set<String> commitSha) {
        return devopsGitlabCommitMapper.listIdsByCommitSha(commitSha).stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private boolean checkExist(DevopsGitlabCommitDTO devopsGitlabCommitDTO) {
        devopsGitlabCommitDTO.setCommitSha(devopsGitlabCommitDTO.getCommitSha());
        devopsGitlabCommitDTO.setRef(devopsGitlabCommitDTO.getRef());
        return devopsGitlabCommitMapper.selectOne(devopsGitlabCommitDTO) != null;
    }

    @Override
    public void fixIssueId() {
        int totalCount = devopsGitlabCommitMapper.countBranchBoundWithIssue();
        int pageNumber = 0;
        int pageSize = 100;
        int totalPage = (totalCount + pageSize - 1) / pageSize;
        do {
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(pageNumber);
            pageRequest.setSize(pageSize);
            Page<DevopsGitlabCommitDTO> result = PageHelper.doPage(pageRequest, () -> devopsGitlabCommitMapper.listCommitBoundWithIssue());
            if (!CollectionUtils.isEmpty(result.getContent())) {
                List<DevopsIssueRelDTO> devopsIssueRelDTOList = result.getContent().stream().map(b -> {
                    DevopsIssueRelDTO devopsIssueRelDTO = new DevopsIssueRelDTO();
                    devopsIssueRelDTO.setIssueId(b.getIssueId());
                    devopsIssueRelDTO.setObject(DevopsIssueRelObjectTypeEnum.COMMIT.getValue());
                    devopsIssueRelDTO.setObjectId(b.getId());
                    return devopsIssueRelDTO;
                }).collect(Collectors.toList());
                batchInsertHelper.batchInsert(devopsIssueRelDTOList);
            }
            pageNumber++;
        } while (pageNumber < totalPage);
    }

    @Override
    public List<DevopsBranchDTO> baseListDevopsBranchesByIssueId(Long issueId) {
        return devopsGitlabCommitMapper.baseListDevopsBranchesByIssueId(issueId);
    }
}
