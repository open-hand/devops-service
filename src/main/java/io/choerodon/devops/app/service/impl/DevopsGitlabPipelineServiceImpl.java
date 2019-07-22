package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_GITLAB_PIPELINE;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.domain.application.valueobject.Stage;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitStatusDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsGitlabPipelineMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DevopsGitlabPipelineServiceImpl implements DevopsGitlabPipelineService {

    private static final Integer ADMIN = 1;
    private static final String SONARQUBE = "sonarqube";
    private ObjectMapper objectMapper = new ObjectMapper();
    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private ApplicationVersionService applicationVersionService;
    @Autowired
    private DevopsGitlabPipelineMapper devopsGitlabPipelineMapper;
    @Autowired
    private TransactionalProducer transactionalProducer;

    @Override
    @Saga(code = DEVOPS_GITLAB_PIPELINE, description = "gitlab pipeline创建到数据库", inputSchemaClass = PipelineWebHookDTO.class)
    public void create(PipelineWebHookDTO pipelineWebHookDTO, String token) {
        pipelineWebHookDTO.setToken(token);
        ApplicationDTO applicationDTO = applicationService.baseQueryByToken(token);
        try {
            String input = objectMapper.writeValueAsString(pipelineWebHookDTO);
            transactionalProducer.apply(
                    StartSagaBuilder.newBuilder()
                            .withRefType("app")
                            .withRefId(applicationDTO.getId().toString())
                            .withSagaCode(DEVOPS_GITLAB_PIPELINE)
                            .withJson(input),
                    builder -> {});
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    @Override
    public void handleCreate(PipelineWebHookDTO pipelineWebHookDTO) {
        ApplicationDTO applicationDTO = applicationService.baseQueryByToken(pipelineWebHookDTO.getToken());
        DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = baseQueryByGitlabPipelineId(pipelineWebHookDTO.getObjectAttributes().getId());
        if ("admin1".equals(pipelineWebHookDTO.getUser().getUsername()) || "root".equals(pipelineWebHookDTO.getUser().getUsername())) {
            pipelineWebHookDTO.getUser().setUsername("admin");
        }
        Integer gitlabUserId = ADMIN;
        UserAttrDTO userAttrE = userAttrService.baseQueryByGitlabUserName(pipelineWebHookDTO.getUser().getUsername());
        if (userAttrE != null) {
            gitlabUserId = TypeUtil.objToInteger(userAttrE.getGitlabUserId());
        }
        //查询pipeline最新阶段信息


        List<Stage> stages = new ArrayList<>();
        List<String> stageNames = new ArrayList<>();
        List<Integer> gitlabJobIds = gitlabServiceClientOperator.listJobs(applicationDTO.getGitlabProjectId(), TypeUtil.objToInteger(pipelineWebHookDTO.getObjectAttributes().getId()), gitlabUserId)
                .stream()
                .map(JobDTO::getId)
                .collect(Collectors.toList());

        Stage sonar = null;
        List<CommitStatusDTO> commitStatusDTOS = gitlabServiceClientOperator.listCommitStatus(applicationDTO.getGitlabProjectId(), pipelineWebHookDTO.getObjectAttributes().getSha(), ADMIN);
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
        DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.baseQueryByShaAndRef(pipelineWebHookDTO.getObjectAttributes().getSha(), pipelineWebHookDTO.getObjectAttributes().getRef());

        //pipeline不存在则创建,存在则更新状态和阶段信息
        if (devopsGitlabPipelineDTO == null) {
            devopsGitlabPipelineDTO = new DevopsGitlabPipelineDTO();
            devopsGitlabPipelineDTO.setAppId(applicationDTO.getId());
            devopsGitlabPipelineDTO.setPipelineCreateUserId(userAttrE == null ? null : userAttrE.getIamUserId());
            devopsGitlabPipelineDTO.setPipelineId(pipelineWebHookDTO.getObjectAttributes().getId());
            devopsGitlabPipelineDTO.setStatus(pipelineWebHookDTO.getObjectAttributes().getStatus());
            devopsGitlabPipelineDTO.setPipelineCreationDate(pipelineWebHookDTO.getObjectAttributes().getCreatedAt());
            if (devopsGitlabCommitDTO != null) {
                devopsGitlabPipelineDTO.setCommitId(devopsGitlabCommitDTO.getId());
            }
            devopsGitlabPipelineDTO.setStage(JSONArray.toJSONString(stages));
            baseCreate(devopsGitlabPipelineDTO);
        } else {
            devopsGitlabPipelineDTO.setStatus(pipelineWebHookDTO.getObjectAttributes().getStatus());

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
    public void updateStages(JobWebHookDTO jobWebHookDTO) {
        //按照job的状态实时更新pipeline阶段的状态
        DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.baseQueryByShaAndRef(jobWebHookDTO.getSha(), jobWebHookDTO.getRef());
        if (devopsGitlabCommitDTO != null && !"created".equals(jobWebHookDTO.getBuildStatus())) {
            DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = baseQueryByCommitId(devopsGitlabCommitDTO.getId());
            if (devopsGitlabPipelineDTO != null) {
                List<Stage> stages = JSONArray.parseArray(devopsGitlabPipelineDTO.getStage(), Stage.class);
                stages.stream().filter(stage -> jobWebHookDTO.getBuildName().equals(stage.getName())).forEach(stage ->
                        stage.setStatus(jobWebHookDTO.getBuildStatus())
                );
                devopsGitlabPipelineDTO.setStage(JSONArray.toJSONString(stages));
                baseUpdate(devopsGitlabPipelineDTO);
            }
        }
    }

    @Override
    public PipelineTimeDTO getPipelineTime(Long appId, Date startTime, Date endTime) {
        if (appId == null) {
            return new PipelineTimeDTO();
        }
        PipelineTimeDTO pipelineTimeDTO = new PipelineTimeDTO();
        List<DevopsGitlabPipelineDTO> devopsGitlabPipelineDOS = baseListByApplicationId(appId, startTime, endTime);
        List<String> pipelineTimes = new LinkedList<>();
        List<String> refs = new LinkedList<>();
        List<String> versions = new LinkedList<>();
        List<Date> createDates = new LinkedList<>();
        devopsGitlabPipelineDOS.forEach(devopsGitlabPipelineDO -> {
            refs.add(devopsGitlabPipelineDO.getRef() + "-" + devopsGitlabPipelineDO.getSha());
            createDates.add(devopsGitlabPipelineDO.getPipelineCreationDate());
            ApplicationVersionDTO applicationVersionE = applicationVersionService.baseQueryByCommitSha(appId, devopsGitlabPipelineDO.getRef(), devopsGitlabPipelineDO.getSha());
            if (applicationVersionE != null) {
                versions.add(applicationVersionE.getVersion());
            } else {
                versions.add("");
            }
            List<Stage> stages = JSONArray.parseArray(devopsGitlabPipelineDO.getStage(), Stage.class);
            //获取pipeline执行时间
            if (stages != null) {
                pipelineTimes.add(getPipelineTime(stages));
            }
        });
        pipelineTimeDTO.setCreateDates(createDates);
        pipelineTimeDTO.setPipelineTime(pipelineTimes);
        pipelineTimeDTO.setRefs(refs);
        pipelineTimeDTO.setVersions(versions);
        return pipelineTimeDTO;
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
    public PipelineFrequencyDTO getPipelineFrequency(Long appId, Date startTime, Date endTime) {
        if (appId == null) {
            return new PipelineFrequencyDTO();
        }
        PipelineFrequencyDTO pipelineFrequencyDTO = new PipelineFrequencyDTO();
        List<DevopsGitlabPipelineDTO> devopsGitlabPipelineDOS = baseListByApplicationId(appId, startTime, endTime);
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
        pipelineFrequencyDTO.setCreateDates(creationDates);
        pipelineFrequencyDTO.setPipelineFailFrequency(pipelineFailFrequency);
        pipelineFrequencyDTO.setPipelineFrequencys(pipelineFrequencies);
        pipelineFrequencyDTO.setPipelineSuccessFrequency(pipelineSuccessFrequency);
        return pipelineFrequencyDTO;
    }

    @Override
    public PageInfo<DevopsGitlabPipelineVO> pageByOptions(Long appId, String branch, PageRequest pageRequest, Date startTime, Date endTime) {
        if (appId == null) {
            return new PageInfo<>();
        }
        PageInfo<DevopsGitlabPipelineVO> pageDevopsGitlabPipelineDTOS = new PageInfo<>();
        List<DevopsGitlabPipelineVO> devopsGiltabPipelineDTOS = new ArrayList<>();
        PageInfo<DevopsGitlabPipelineDTO> devopsGitlabPipelineDOS = new PageInfo<>();
        if (branch == null) {
            devopsGitlabPipelineDOS = basePageByApplicationId(appId, pageRequest, startTime, endTime);
        } else {
            devopsGitlabPipelineDOS.setList(baseListByAppIdAndBranch(appId, branch));
        }
        BeanUtils.copyProperties(devopsGitlabPipelineDOS, pageDevopsGitlabPipelineDTOS);

        //按照ref分组
        Map<String, List<DevopsGitlabPipelineDTO>> refWithPipelines = devopsGitlabPipelineDOS.getList().stream()
                .filter(pageDevopsGitlabPipelineDTO -> pageDevopsGitlabPipelineDTO.getRef() != null)
                .collect(Collectors.groupingBy(DevopsGitlabPipelineDTO::getRef));
        Map<String, Long> refWithPipelineIds = new HashMap<>();

        //获取每个分支上最新的一条pipeline记录，用于后续标记latest
        refWithPipelines.forEach((key, value) -> {
            Long pipeLineId = Collections.max(value.stream().map(DevopsGitlabPipelineDTO::getPipelineId).collect(Collectors.toList()));
            refWithPipelineIds.put(key, pipeLineId);
        });

        ApplicationDTO applicationDTO = applicationService.baseQuery(appId);
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(applicationDTO.getProjectId());
        OrganizationDTO organization = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        //获取pipeline记录
        Set<Long> userIds = new HashSet<>();
        devopsGitlabPipelineDOS.getList().forEach(devopsGitlabPipelineDO -> {
            userIds.add(devopsGitlabPipelineDO.getCommitUserId());
            userIds.add(devopsGitlabPipelineDO.getPipelineCreateUserId());
        });

        List<IamUserDTO> userES = iamServiceClientOperator.listUsersByIds(new ArrayList<>(userIds));
        devopsGitlabPipelineDOS.getList().forEach(devopsGitlabPipelineDO -> {
            DevopsGitlabPipelineVO devopsGitlabPipelineDTO = new DevopsGitlabPipelineVO();
            if (devopsGitlabPipelineDO.getPipelineId().equals(refWithPipelineIds.get(devopsGitlabPipelineDO.getRef()))) {
                devopsGitlabPipelineDTO.setLatest(true);
            }

            devopsGitlabPipelineDTO.setCommit(devopsGitlabPipelineDO.getSha());
            devopsGitlabPipelineDTO.setCommitContent(devopsGitlabPipelineDO.getContent());
            userES.stream().filter(userE -> userE.getId().equals(devopsGitlabPipelineDO.getCommitUserId())).forEach(userE -> {
                devopsGitlabPipelineDTO.setCommitUserUrl(userE.getImageUrl());
                devopsGitlabPipelineDTO.setCommitUserName(userE.getRealName());
            });

            userES.stream().filter(userE -> userE.getId().equals(devopsGitlabPipelineDO.getPipelineCreateUserId())).forEach(userE -> {
                devopsGitlabPipelineDTO.setPipelineUserUrl(userE.getImageUrl());
                devopsGitlabPipelineDTO.setPipelineUserName(userE.getRealName());
            });

            devopsGitlabPipelineDTO.setCreationDate(devopsGitlabPipelineDO.getPipelineCreationDate());
            devopsGitlabPipelineDTO.setGitlabProjectId(TypeUtil.objToLong(applicationDTO.getGitlabProjectId()));
            devopsGitlabPipelineDTO.setPipelineId(devopsGitlabPipelineDO.getPipelineId());

            if (("success").equals(devopsGitlabPipelineDO.getStatus())) {
                devopsGitlabPipelineDTO.setStatus("passed");
            } else {
                devopsGitlabPipelineDTO.setStatus(devopsGitlabPipelineDO.getStatus());
            }

            devopsGitlabPipelineDTO.setRef(devopsGitlabPipelineDO.getRef());
            String version = applicationVersionService.baseQueryByPipelineId(devopsGitlabPipelineDO.getPipelineId(), devopsGitlabPipelineDO.getRef(), appId);
            if (version != null) {
                devopsGitlabPipelineDTO.setVersion(version);
            }

            //pipeline阶段信息
            List<Stage> stages = JSONArray.parseArray(devopsGitlabPipelineDO.getStage(), Stage.class);
            if (stages != null) {
                devopsGitlabPipelineDTO.setPipelineTime(getPipelineTime(stages));
            }
            devopsGitlabPipelineDTO.setStages(stages);
            devopsGitlabPipelineDTO.setGitlabUrl(gitlabUrl + "/"
                    + organization.getCode() + "-" + projectDTO.getCode() + "/"
                    + applicationDTO.getCode() + ".git");
            devopsGiltabPipelineDTOS.add(devopsGitlabPipelineDTO);
        });

        pageDevopsGitlabPipelineDTOS.setList(devopsGiltabPipelineDTOS);
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
            throw new CommonException("error.gitlab.pipeline.create");
        }
    }

    @Override
    public DevopsGitlabPipelineDTO baseQueryByGitlabPipelineId(Long id) {
        DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = new DevopsGitlabPipelineDTO();
        devopsGitlabPipelineDTO.setPipelineId(id);
        return devopsGitlabPipelineMapper.selectOne(devopsGitlabPipelineDTO);
    }

    @Override
    public void baseUpdate(DevopsGitlabPipelineDTO devopsGitlabPipelineDTO) {
        devopsGitlabPipelineDTO.setObjectVersionNumber(devopsGitlabPipelineMapper.selectByPrimaryKey(devopsGitlabPipelineDTO.getId()).getObjectVersionNumber());
        if (devopsGitlabPipelineMapper.updateByPrimaryKeySelective(devopsGitlabPipelineDTO) != 1) {
            throw new CommonException("error.gitlab.pipeline.update");
        }
    }

    @Override
    public DevopsGitlabPipelineDTO baseQueryByCommitId(Long commitId) {
        DevopsGitlabPipelineDTO devopsGitlabPipelineDO = new DevopsGitlabPipelineDTO();
        devopsGitlabPipelineDO.setCommitId(commitId);
        return devopsGitlabPipelineMapper.selectOne(devopsGitlabPipelineDO);
    }

    @Override
    public List<DevopsGitlabPipelineDTO> baseListByApplicationId(Long appId, Date startTime, Date endTime) {
        return devopsGitlabPipelineMapper.listDevopsGitlabPipeline(appId, new java.sql.Date(startTime.getTime()), new java.sql.Date(endTime.getTime()));
    }


    @Override
    public PageInfo<DevopsGitlabPipelineDTO> basePageByApplicationId(Long appId, PageRequest pageRequest, Date startTime, Date endTime) {
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                devopsGitlabPipelineMapper.listDevopsGitlabPipeline(appId, startTime == null ? null : new java.sql.Date(startTime.getTime()), endTime == null ? null : new java.sql.Date(endTime.getTime())));
    }

    @Override
    public void baseDeleteWithoutCommit() {
        devopsGitlabPipelineMapper.deleteWithoutCommit();
    }

    @Override
    public List<DevopsGitlabPipelineDTO> baseListByAppIdAndBranch(Long appId, String branch) {
        return devopsGitlabPipelineMapper.listByBranch(appId, branch);
    }
}
