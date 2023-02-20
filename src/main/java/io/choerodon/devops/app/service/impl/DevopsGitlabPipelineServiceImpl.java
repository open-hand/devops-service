package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_GITLAB_PIPELINE;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.Stage;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitStatusDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsGitlabPipelineMapper;
import io.choerodon.devops.infra.util.CustomContextUtil;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsGitlabPipelineServiceImpl implements DevopsGitlabPipelineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsGitlabPipelineServiceImpl.class);

    private static final Integer ADMIN = 1;
    private static final String SONARQUBE = "sonarqube";
    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private DevopsGitlabPipelineMapper devopsGitlabPipelineMapper;
    @Autowired
    private TransactionalProducer transactionalProducer;
    @Autowired
    @Lazy
    private SendNotificationService sendNotificationService;
    @Autowired
    private CheckGitlabAccessLevelService checkGitlabAccessLevelService;
    @Autowired
    private AppExternalConfigService appExternalConfigService;


    @Override
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = DEVOPS_GITLAB_PIPELINE, description = "gitlab pipeline创建到数据库", inputSchemaClass = PipelineWebHookVO.class)
    public void create(PipelineWebHookVO pipelineWebHookVO, String token) {
        pipelineWebHookVO.setToken(token);
        AppServiceDTO applicationDTO = applicationService.baseQueryByToken(token);
        if (applicationDTO == null) {
            LOGGER.info("application not exist:{}", token);
            return;
        }
        if (applicationDTO.getExternalConfigId() != null) {
            return;
        }
        String input = JsonHelper.marshalByJackson(pipelineWebHookVO);
        transactionalProducer.apply(
                StartSagaBuilder.newBuilder()
                        .withRefType("app")
                        .withRefId(applicationDTO.getId().toString())
                        .withSagaCode(DEVOPS_GITLAB_PIPELINE)
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(applicationDTO.getProjectId())
                        .withJson(input),
                builder -> {
                });
    }

    @Override
    public void handleCreate(PipelineWebHookVO pipelineWebHookVO) {
        AppServiceDTO applicationDTO = applicationService.baseQueryByToken(pipelineWebHookVO.getToken());
        DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = baseQueryByGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
        if ("admin1".equals(pipelineWebHookVO.getUser().getUsername()) || "root".equals(pipelineWebHookVO.getUser().getUsername())) {
            pipelineWebHookVO.getUser().setUsername("admin");
        }
        Integer gitlabUserId = ADMIN;
        UserAttrDTO userAttrE = userAttrService.baseQueryByGitlabUserName(pipelineWebHookVO.getUser().getUsername());
        if (userAttrE != null) {
            gitlabUserId = TypeUtil.objToInteger(userAttrE.getGitlabUserId());
            // 这里不设置用户上下文会报错
            CustomContextUtil.setDefaultIfNull(userAttrE.getIamUserId());
        } else {
            CustomContextUtil.setDefault();
        }
        //查询pipeline最新阶段信息
        List<Stage> stages = new ArrayList<>();
        List<String> stageNames = new ArrayList<>();
        AppExternalConfigDTO appExternalConfigDTO = null;
        if (applicationDTO.getExternalConfigId() != null) {
            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(applicationDTO.getExternalConfigId());
        }
        List<Integer> gitlabJobIds = gitlabServiceClientOperator.listJobs(applicationDTO.getGitlabProjectId(),
                TypeUtil.objToInteger(pipelineWebHookVO.getObjectAttributes().getId()),
                gitlabUserId, appExternalConfigDTO)
                .stream()
                .map(JobDTO::getId)
                .collect(Collectors.toList());

        Stage sonar = null;
        List<CommitStatusDTO> commitStatusDTOS = gitlabServiceClientOperator.listCommitStatus(applicationDTO.getGitlabProjectId(), pipelineWebHookVO.getObjectAttributes().getSha(), ADMIN);
        for (CommitStatusDTO commitStatusDTO : commitStatusDTOS) {
            if (gitlabJobIds.contains(commitStatusDTO.getId())) {
                Stage stage = getPipelineStage(commitStatusDTO);
                stages.add(stage);
            } else if (commitStatusDTO.getName().equals(SONARQUBE) && !stageNames.contains(SONARQUBE) && !stages.isEmpty()) {
                Stage stage = getPipelineStage(commitStatusDTO);
                sonar = stage;
                stages.add(stage);
                stageNames.add(commitStatusDTO.getName());
            }
        }
        DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.baseQueryByShaAndRef(pipelineWebHookVO.getObjectAttributes().getSha(), pipelineWebHookVO.getObjectAttributes().getRef());

        //pipeline不存在则创建,存在则更新状态和阶段信息
        if (devopsGitlabPipelineDTO == null) {
            devopsGitlabPipelineDTO = new DevopsGitlabPipelineDTO();
            devopsGitlabPipelineDTO.setAppServiceId(applicationDTO.getId());
            devopsGitlabPipelineDTO.setPipelineCreateUserId(userAttrE == null ? null : userAttrE.getIamUserId());
            devopsGitlabPipelineDTO.setPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
            devopsGitlabPipelineDTO.setStatus(pipelineWebHookVO.getObjectAttributes().getStatus());
            devopsGitlabPipelineDTO.setPipelineCreationDate(pipelineWebHookVO.getObjectAttributes().getCreatedAt());
            if (devopsGitlabCommitDTO != null) {
                // TODO 这里设置的 commitId 为空的情况可能是，gitlab 推送的 [push] 事件和 [pipeline] 事件顺序反了
                // 因为这个commit应该是由push事件产生的
                devopsGitlabPipelineDTO.setCommitId(devopsGitlabCommitDTO.getId());
            }
            devopsGitlabPipelineDTO.setStage(JSONArray.toJSONString(stages));
            baseCreate(devopsGitlabPipelineDTO);
        } else {
            devopsGitlabPipelineDTO.setStatus(pipelineWebHookVO.getObjectAttributes().getStatus());

            List<Stage> originalStages = JSONArray.parseArray(devopsGitlabPipelineDTO.getStage(), Stage.class);
            List<Stage> result = new ArrayList<>();
            originalStages.forEach(original -> {
                for (Stage stage : stages) {
                    if (stage.getName().equals(original.getName())) {
                        result.add(stage);
                        return;
                    }
                }
                result.add(original);
            });
            if (sonar != null && result.stream().noneMatch(x -> x.getName().equals(SONARQUBE))) {
                result.add(sonar);
            }
            devopsGitlabPipelineDTO.setStage(JSONArray.toJSONString(result));

            if (devopsGitlabCommitDTO != null) {
                devopsGitlabPipelineDTO.setCommitId(devopsGitlabCommitDTO.getId());
            }
            baseUpdate(devopsGitlabPipelineDTO);
        }

        // 发送流水线失败的通知
        if (PipelineStatus.FAILED.toValue().equals(pipelineWebHookVO.getObjectAttributes().getStatus())) {
            sendNotificationService.sendWhenCDFailure(pipelineWebHookVO.getObjectAttributes().getId(), applicationDTO, pipelineWebHookVO.getUser().getUsername());
        }
        //成功以后也要发送webhook json
        if (PipelineStatus.SUCCESS.toValue().equals(pipelineWebHookVO.getObjectAttributes().getStatus())) {
            sendNotificationService.sendWhenCDSuccess(applicationDTO, pipelineWebHookVO.getUser().getUsername());
        }
    }


    private Stage getPipelineStage(CommitStatusDTO commitStatusDTO) {
        Stage stage = new Stage();
        stage.setDescription(commitStatusDTO.getDescription());
        stage.setId(commitStatusDTO.getId());
        stage.setName(commitStatusDTO.getName());
        stage.setStatus(commitStatusDTO.getStatus());
        if (commitStatusDTO.getFinishedAt() != null) {
            stage.setFinishedAt(commitStatusDTO.getFinishedAt());
        }
        if (commitStatusDTO.getStartedAt() != null) {
            stage.setStartedAt(commitStatusDTO.getStartedAt());
        }
        return stage;
    }

    @Override
    public void updateStages(JobWebHookVO jobWebHookVO, String token) {
        //按照job的状态实时更新pipeline阶段的状态
        DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.baseQueryByShaAndRef(jobWebHookVO.getSha(), jobWebHookVO.getRef());

        if (jobWebHookVO.getCommit() == null || jobWebHookVO.getCommit().getId() == null) {
            LOGGER.info("The commit attribute or the commit.id attribute is null of jobWebHook {}", jobWebHookVO.getBuildName());
            return;
        }

        if (devopsGitlabCommitDTO != null && !"created".equals(jobWebHookVO.getBuildStatus())) {
            DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = baseQueryByGitlabPipelineId(jobWebHookVO.getCommit().getId());
            if (devopsGitlabPipelineDTO != null) {
                LOGGER.debug("Found gitlab pipeline by id {}", jobWebHookVO.getCommit().getId());
                List<Stage> stages = JSONArray.parseArray(devopsGitlabPipelineDTO.getStage(), Stage.class);
                stages.stream().filter(stage -> jobWebHookVO.getBuildName().equals(stage.getName())).forEach(stage ->
                        stage.setStatus(jobWebHookVO.getBuildStatus())
                );
                devopsGitlabPipelineDTO.setStage(JSONArray.toJSONString(stages));
                baseUpdate(devopsGitlabPipelineDTO);
            } else {
                LOGGER.debug("Not Found gitlab pipeline by id {}", jobWebHookVO.getCommit().getId());
            }
        }
    }

    @Override
    public PipelineTimeVO getPipelineTime(Long appServiceId, Date startTime, Date endTime) {
        if (appServiceId == null) {
            return new PipelineTimeVO();
        }
        PipelineTimeVO pipelineTimeVO = new PipelineTimeVO();
        List<DevopsGitlabPipelineDTO> devopsGitlabPipelineDOS = baseListByApplicationId(appServiceId, startTime, endTime);
        List<String> pipelineTimes = new LinkedList<>();
        List<String> refs = new LinkedList<>();
        List<String> versions = new LinkedList<>();
        List<Date> createDates = new LinkedList<>();
        devopsGitlabPipelineDOS.forEach(devopsGitlabPipelineDO -> {
            refs.add(devopsGitlabPipelineDO.getRef() + "-" + devopsGitlabPipelineDO.getSha());
            createDates.add(devopsGitlabPipelineDO.getPipelineCreationDate());
            List<AppServiceVersionDTO> applicationVersionList = appServiceVersionService.baseQueryByCommitSha(appServiceId, devopsGitlabPipelineDO.getRef(), devopsGitlabPipelineDO.getSha());
            if (!CollectionUtils.isEmpty(applicationVersionList)) {
                versions.add(applicationVersionList.get(0).getVersion());
            } else {
                versions.add("");
            }
            List<Stage> stages = JSONArray.parseArray(devopsGitlabPipelineDO.getStage(), Stage.class);
            //获取pipeline执行时间
            if (stages != null) {
                pipelineTimes.add(getPipelineTime(stages));
            }
        });
        pipelineTimeVO.setCreateDates(createDates);
        pipelineTimeVO.setPipelineTime(pipelineTimes);
        pipelineTimeVO.setRefs(refs);
        pipelineTimeVO.setVersions(versions);
        return pipelineTimeVO;
    }

    private String getPipelineTime(List<Stage> stages) {
        Long diff = 0L;
        for (Stage stage : stages) {
            try {
                if (stage.getFinishedAt() != null && stage.getStartedAt() != null) {
                    diff = diff + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(stage.getFinishedAt()).getTime() - new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stage.getStartedAt()).getTime();
                }
            } catch (ParseException e) {
                throw new CommonException(e);
            }
        }
        return getDeployTime(diff);
    }

    @Override
    public PipelineFrequencyVO getPipelineFrequency(Long appServiceId, Date startTime, Date endTime) {
        if (appServiceId == null) {
            return new PipelineFrequencyVO();
        }
        PipelineFrequencyVO pipelineFrequencyVO = new PipelineFrequencyVO();
        List<DevopsGitlabPipelineDTO> devopsGitlabPipelineDOS = baseListByApplicationId(appServiceId, startTime, endTime);
        //按照创建时间分组
        Map<String, List<DevopsGitlabPipelineDTO>> resultMaps = devopsGitlabPipelineDOS.stream()
                .collect(Collectors.groupingBy(t -> new java.sql.Date(t.getPipelineCreationDate().getTime()).toString()));
        //将创建时间排序
        List<String> creationDates = devopsGitlabPipelineDOS.stream().map(deployDO -> new java.sql.Date(deployDO.getPipelineCreationDate().getTime()).toString()).collect(Collectors.toList());
        List<Long> pipelineFrequencies = new LinkedList<>();
        List<Long> pipelineSuccessFrequency = new LinkedList<>();
        List<Long> pipelineFailFrequency = new LinkedList<>();
        creationDates = new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        creationDates.forEach(date -> {
            Long[] newPipelineFrequencies = {0L};
            Long[] newPipelineSuccessFrequency = {0L};
            Long[] newPipelineFailFrequency = {0L};
            resultMaps.get(date).forEach(devopsGitlabPipelineDO -> {
                if ("success".equals(devopsGitlabPipelineDO.getStatus())) {
                    newPipelineSuccessFrequency[0] = newPipelineSuccessFrequency[0] + 1L;
                }
                if ("failed".equals(devopsGitlabPipelineDO.getStatus())) {
                    newPipelineFailFrequency[0] = newPipelineFailFrequency[0] + 1L;
                }
                newPipelineFrequencies[0] = newPipelineSuccessFrequency[0] + newPipelineFailFrequency[0];
            });
            pipelineFrequencies.add(newPipelineFrequencies[0]);
            pipelineSuccessFrequency.add(newPipelineSuccessFrequency[0]);
            pipelineFailFrequency.add(newPipelineFailFrequency[0]);
        });
        pipelineFrequencyVO.setCreateDates(creationDates);
        pipelineFrequencyVO.setPipelineFailFrequency(pipelineFailFrequency);
        pipelineFrequencyVO.setPipelineFrequencys(pipelineFrequencies);
        pipelineFrequencyVO.setPipelineSuccessFrequency(pipelineSuccessFrequency);
        return pipelineFrequencyVO;
    }

    @Override
    public Page<DevopsGitlabPipelineVO> pageByOptions(Long appServiceId, String branch, PageRequest pageable, Date startTime, Date endTime) {
        AppServiceDTO appServiceDTO = applicationService.baseQuery(appServiceId);
        if (appServiceId == null) {
            return new Page<>();
        }
        Page<DevopsGitlabPipelineVO> pageDevopsGitlabPipelineDTOS = new Page<>();
        List<DevopsGitlabPipelineVO> devopsGiltabPipelineDTOS = new ArrayList<>();
        Page<DevopsGitlabPipelineDTO> devopsGitlabPipelineDOS = new Page<>();
        if (branch == null) {
            devopsGitlabPipelineDOS = basePageByApplicationId(appServiceId, pageable, startTime, endTime);
        } else {
            devopsGitlabPipelineDOS.setContent(baseListByAppIdAndBranch(appServiceId, branch));
        }
        BeanUtils.copyProperties(devopsGitlabPipelineDOS, pageDevopsGitlabPipelineDTOS);

        //按照ref分组
        Map<String, List<DevopsGitlabPipelineDTO>> refWithPipelines = devopsGitlabPipelineDOS.getContent().stream()
                .filter(pageDevopsGitlabPipelineDTO -> pageDevopsGitlabPipelineDTO.getRef() != null)
                .collect(Collectors.groupingBy(DevopsGitlabPipelineDTO::getRef));
        Map<String, Long> refWithPipelineIds = new HashMap<>();

        //获取每个分支上最新的一条pipeline记录，用于后续标记latest
        refWithPipelines.forEach((key, value) -> {
            //找出每个分支最新的pipline
            DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = devopsGitlabPipelineMapper.selectLatestPipline(appServiceId, key);
            List<Long> ids = value.stream().map(DevopsGitlabPipelineDTO::getPipelineId).collect(Collectors.toList());
            if (ids.contains(devopsGitlabPipelineDTO.getPipelineId())) {
                refWithPipelineIds.put(key, devopsGitlabPipelineDTO.getPipelineId());
            }
        });
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
        Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        //获取pipeline记录
        Set<Long> userIds = new HashSet<>();
        devopsGitlabPipelineDOS.getContent().forEach(devopsGitlabPipelineDO -> {
            userIds.add(devopsGitlabPipelineDO.getCommitUserId());
            userIds.add(devopsGitlabPipelineDO.getPipelineCreateUserId());
        });

        List<IamUserDTO> userES = baseServiceClientOperator.listUsersByIds(new ArrayList<>(userIds));
        devopsGitlabPipelineDOS.getContent().forEach(devopsGitlabPipelineDO -> {
            DevopsGitlabPipelineVO devopsGitlabPipelineDTO = new DevopsGitlabPipelineVO();
            if (devopsGitlabPipelineDO.getPipelineId().equals(refWithPipelineIds.get(devopsGitlabPipelineDO.getRef()))) {
                devopsGitlabPipelineDTO.setLatest(true);
            }

            devopsGitlabPipelineDTO.setCommit(devopsGitlabPipelineDO.getSha());
            devopsGitlabPipelineDTO.setCommitContent(devopsGitlabPipelineDO.getContent());
            userES.stream().filter(userE -> userE.getId().equals(devopsGitlabPipelineDO.getCommitUserId())).forEach(userE -> {
                devopsGitlabPipelineDTO.setCommitUserUrl(userE.getImageUrl());
                devopsGitlabPipelineDTO.setCommitUserLoginName(userE.getLdap() ? userE.getLoginName() : userE.getEmail());
                devopsGitlabPipelineDTO.setCommitUserName(userE.getRealName());
            });

            userES.stream().filter(userE -> userE.getId().equals(devopsGitlabPipelineDO.getPipelineCreateUserId())).forEach(userE -> {
                devopsGitlabPipelineDTO.setPipelineUserUrl(userE.getImageUrl());
                devopsGitlabPipelineDTO.setPipelineUserLoginName(userE.getLdap() ? userE.getLoginName() : userE.getEmail());
                devopsGitlabPipelineDTO.setPipelineUserName(userE.getRealName());
            });

            devopsGitlabPipelineDTO.setCreationDate(devopsGitlabPipelineDO.getPipelineCreationDate());
            devopsGitlabPipelineDTO.setGitlabProjectId(TypeUtil.objToLong(appServiceDTO.getGitlabProjectId()));
            devopsGitlabPipelineDTO.setPipelineId(devopsGitlabPipelineDO.getPipelineId());

            if (("success").equals(devopsGitlabPipelineDO.getStatus())) {
                devopsGitlabPipelineDTO.setStatus("passed");
            } else {
                devopsGitlabPipelineDTO.setStatus(devopsGitlabPipelineDO.getStatus());
            }

            devopsGitlabPipelineDTO.setRef(devopsGitlabPipelineDO.getRef());
            devopsGitlabPipelineDTO.setVersion(appServiceVersionService.baseQueryByPipelineId(devopsGitlabPipelineDTO.getPipelineId(), devopsGitlabPipelineDTO.getRef(), appServiceId));

            //pipeline阶段信息
            List<Stage> stages = JSONArray.parseArray(devopsGitlabPipelineDO.getStage(), Stage.class);
            if (stages != null) {
                devopsGitlabPipelineDTO.setPipelineTime(getPipelineTime(stages));
            }
            devopsGitlabPipelineDTO.setStages(stages);
            devopsGitlabPipelineDTO.setGitlabUrl(gitlabUrl + "/"
                    + organization.getTenantNum() + "-" + projectDTO.getDevopsComponentCode() + "/"
                    + appServiceDTO.getCode() + ".git");
            devopsGiltabPipelineDTOS.add(devopsGitlabPipelineDTO);
        });

        pageDevopsGitlabPipelineDTOS.setContent(devopsGiltabPipelineDTOS);
        return pageDevopsGitlabPipelineDTOS;
    }

    private String getDeployTime(Long diff) {
        float num = (float) diff / (60 * 1000);
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
    }

    @Override
    public void baseCreate(DevopsGitlabPipelineDTO devopsGitlabPipelineDTO) {
        if (devopsGitlabPipelineMapper.insert(devopsGitlabPipelineDTO) != 1) {
            throw new CommonException("devops.gitlab.pipeline.create");
        }
    }

    @Override
    public DevopsGitlabPipelineDTO baseQueryByGitlabPipelineId(Long id) {
        DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = new DevopsGitlabPipelineDTO();
        devopsGitlabPipelineDTO.setPipelineId(Objects.requireNonNull(id));
        return devopsGitlabPipelineMapper.selectOne(devopsGitlabPipelineDTO);
    }

    @Override
    public void baseUpdate(DevopsGitlabPipelineDTO devopsGitlabPipelineDTO) {
        devopsGitlabPipelineDTO.setObjectVersionNumber(devopsGitlabPipelineMapper.selectByPrimaryKey(devopsGitlabPipelineDTO.getId()).getObjectVersionNumber());
        if (devopsGitlabPipelineMapper.updateByPrimaryKeySelective(devopsGitlabPipelineDTO) != 1) {
            throw new CommonException("devops.gitlab.pipeline.update");
        }
    }

    @Override
    public DevopsGitlabPipelineDTO baseQueryByCommitId(Long commitId) {
        DevopsGitlabPipelineDTO devopsGitlabPipelineDO = new DevopsGitlabPipelineDTO();
        devopsGitlabPipelineDO.setCommitId(commitId);
        return devopsGitlabPipelineMapper.selectOne(devopsGitlabPipelineDO);
    }

    @Override
    public List<DevopsGitlabPipelineDTO> baseListByApplicationId(Long appServiceId, Date startTime, Date endTime) {
        return devopsGitlabPipelineMapper.listDevopsGitlabPipeline(appServiceId, new java.sql.Date(startTime.getTime()), new java.sql.Date(endTime.getTime()));
    }


    @Override
    public Page<DevopsGitlabPipelineDTO> basePageByApplicationId(Long appServiceId, PageRequest pageable, Date startTime, Date endTime) {
        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () ->
                devopsGitlabPipelineMapper.listDevopsGitlabPipeline(appServiceId, startTime == null ? null : new java.sql.Date(startTime.getTime()), endTime == null ? null : new java.sql.Date(endTime.getTime())));
    }

    @Override
    public List<DevopsGitlabPipelineDTO> baseListByAppIdAndBranch(Long appServiceId, String branch) {
        return devopsGitlabPipelineMapper.listByBranch(appServiceId, branch);
    }
}
