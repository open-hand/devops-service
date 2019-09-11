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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.Stage;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitStatusDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsGitlabPipelineMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class DevopsGitlabPipelineServiceImpl implements DevopsGitlabPipelineService {

    private static final Integer ADMIN = 1;
    private static final String SONARQUBE = "sonarqube";
    private ObjectMapper objectMapper = new ObjectMapper();
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
    private DevopsProjectService devopsProjectService;


    @Override
    @Saga(code = DEVOPS_GITLAB_PIPELINE, description = "gitlab pipeline创建到数据库", inputSchemaClass = PipelineWebHookVO.class)
    public void create(PipelineWebHookVO pipelineWebHookVO, String token) {
        pipelineWebHookVO.setToken(token);
        AppServiceDTO applicationDTO = applicationService.baseQueryByToken(token);
        try {
            String input = objectMapper.writeValueAsString(pipelineWebHookVO);
            transactionalProducer.apply(
                    StartSagaBuilder.newBuilder()
                            .withRefType("app")
                            .withRefId(applicationDTO.getId().toString())
                            .withSagaCode(DEVOPS_GITLAB_PIPELINE)
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
        DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = baseQueryByGitlabPipelineId(pipelineWebHookVO.getObjectAttributes().getId());
        if ("admin1".equals(pipelineWebHookVO.getUser().getUsername()) || "root".equals(pipelineWebHookVO.getUser().getUsername())) {
            pipelineWebHookVO.getUser().setUsername("admin");
        }
        Integer gitlabUserId = ADMIN;
        UserAttrDTO userAttrE = userAttrService.baseQueryByGitlabUserName(pipelineWebHookVO.getUser().getUsername());
        if (userAttrE != null) {
            gitlabUserId = TypeUtil.objToInteger(userAttrE.getGitlabUserId());
        }
        //查询pipeline最新阶段信息
        List<Stage> stages = new ArrayList<>();
        List<String> stageNames = new ArrayList<>();
        List<Integer> gitlabJobIds = gitlabServiceClientOperator.listJobs(applicationDTO.getGitlabProjectId(), TypeUtil.objToInteger(pipelineWebHookVO.getObjectAttributes().getId()), gitlabUserId)
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
    public void updateStages(JobWebHookVO jobWebHookVO) {
        //按照job的状态实时更新pipeline阶段的状态
        DevopsGitlabCommitDTO devopsGitlabCommitDTO = devopsGitlabCommitService.baseQueryByShaAndRef(jobWebHookVO.getSha(), jobWebHookVO.getRef());
        if (devopsGitlabCommitDTO != null && !"created".equals(jobWebHookVO.getBuildStatus())) {
            DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = baseQueryByCommitId(devopsGitlabCommitDTO.getId());
            if (devopsGitlabPipelineDTO != null) {
                List<Stage> stages = JSONArray.parseArray(devopsGitlabPipelineDTO.getStage(), Stage.class);
                stages.stream().filter(stage -> jobWebHookVO.getBuildName().equals(stage.getName())).forEach(stage ->
                        stage.setStatus(jobWebHookVO.getBuildStatus())
                );
                devopsGitlabPipelineDTO.setStage(JSONArray.toJSONString(stages));
                baseUpdate(devopsGitlabPipelineDTO);
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
            AppServiceVersionDTO applicationVersionE = appServiceVersionService.baseQueryByCommitSha(appServiceId, devopsGitlabPipelineDO.getRef(), devopsGitlabPipelineDO.getSha());
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
    public PageInfo<DevopsGitlabPipelineVO> pageByOptions(Long appServiceId, String branch, PageRequest pageRequest, Date startTime, Date endTime) {
        if (appServiceId == null) {
            return new PageInfo<>();
        }
        PageInfo<DevopsGitlabPipelineVO> pageDevopsGitlabPipelineDTOS = new PageInfo<>();
        List<DevopsGitlabPipelineVO> devopsGiltabPipelineDTOS = new ArrayList<>();
        PageInfo<DevopsGitlabPipelineDTO> devopsGitlabPipelineDOS = new PageInfo<>();
        if (branch == null) {
            devopsGitlabPipelineDOS = basePageByApplicationId(appServiceId, pageRequest, startTime, endTime);
        } else {
            devopsGitlabPipelineDOS.setList(baseListByAppIdAndBranch(appServiceId, branch));
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

        AppServiceDTO appServiceDTO = applicationService.baseQuery(appServiceId);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
        OrganizationDTO organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        //获取pipeline记录
        Set<Long> userIds = new HashSet<>();
        devopsGitlabPipelineDOS.getList().forEach(devopsGitlabPipelineDO -> {
            userIds.add(devopsGitlabPipelineDO.getCommitUserId());
            userIds.add(devopsGitlabPipelineDO.getPipelineCreateUserId());
        });

        List<IamUserDTO> userES = baseServiceClientOperator.listUsersByIds(new ArrayList<>(userIds));
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
            devopsGitlabPipelineDTO.setGitlabProjectId(TypeUtil.objToLong(appServiceDTO.getGitlabProjectId()));
            devopsGitlabPipelineDTO.setPipelineId(devopsGitlabPipelineDO.getPipelineId());

            if (("success").equals(devopsGitlabPipelineDO.getStatus())) {
                devopsGitlabPipelineDTO.setStatus("passed");
            } else {
                devopsGitlabPipelineDTO.setStatus(devopsGitlabPipelineDO.getStatus());
            }

            devopsGitlabPipelineDTO.setRef(devopsGitlabPipelineDO.getRef());
            String version = appServiceVersionService.baseQueryByPipelineId(devopsGitlabPipelineDO.getPipelineId(), devopsGitlabPipelineDO.getRef(), appServiceId);
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
                    + appServiceDTO.getCode() + ".git");
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
    public List<DevopsGitlabPipelineDTO> baseListByApplicationId(Long appServiceId, Date startTime, Date endTime) {
        return devopsGitlabPipelineMapper.listDevopsGitlabPipeline(appServiceId, new java.sql.Date(startTime.getTime()), new java.sql.Date(endTime.getTime()));
    }


    @Override
    public PageInfo<DevopsGitlabPipelineDTO> basePageByApplicationId(Long appServiceId, PageRequest pageRequest, Date startTime, Date endTime) {
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                devopsGitlabPipelineMapper.listDevopsGitlabPipeline(appServiceId, startTime == null ? null : new java.sql.Date(startTime.getTime()), endTime == null ? null : new java.sql.Date(endTime.getTime())));
    }

    @Override
    public void baseDeleteWithoutCommit() {
        devopsGitlabPipelineMapper.deleteWithoutCommit();
    }

    @Override
    public List<DevopsGitlabPipelineDTO> baseListByAppIdAndBranch(Long appServiceId, String branch) {
        return devopsGitlabPipelineMapper.listByBranch(appServiceId, branch);
    }
}
