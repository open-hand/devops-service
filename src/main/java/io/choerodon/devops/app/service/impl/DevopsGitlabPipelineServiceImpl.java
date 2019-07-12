package io.choerodon.devops.app.service.impl;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsGitlabPipelineService;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabJobE;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.domain.application.valueobject.Stage;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDO;
import io.choerodon.devops.infra.dto.gitlab.CommitStatuseDTO;
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
    private DevopsGitlabPipelineRepository devopsGitlabPipelineRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DevopsGitlabCommitRepository devopsGitlabCommitRepository;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;
    @Autowired
    private SagaClient sagaClient;

    @Override
    @Saga(code = "devops-gitlab-pipeline", description = "gitlab pipeline创建到数据库", inputSchemaClass = PipelineWebHookDTO.class)
    public void create(PipelineWebHookDTO pipelineWebHookDTO, String token) {
        pipelineWebHookDTO.setToken(token);
        ApplicationE applicationE = applicationRepository.queryByToken(token);
        try {
            String input;
            input = objectMapper.writeValueAsString(pipelineWebHookDTO);
            sagaClient.startSaga("devops-gitlab-pipeline", new StartInstanceDTO(input, "app", applicationE.getId().toString()));
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }


    @Override
    public void handleCreate(PipelineWebHookDTO pipelineWebHookDTO) {
        ApplicationE applicationE = applicationRepository.queryByToken(pipelineWebHookDTO.getToken());
        DevopsGitlabPipelineE devopsGitlabPipelineE = devopsGitlabPipelineRepository.queryByGitlabPipelineId(pipelineWebHookDTO.getObjectAttributes().getId());
        if ("admin1".equals(pipelineWebHookDTO.getUser().getUsername()) || "root".equals(pipelineWebHookDTO.getUser().getUsername())) {
            pipelineWebHookDTO.getUser().setUsername("admin");
        }
        Integer gitlabUserId = ADMIN;
        UserAttrE userAttrE = userAttrRepository.queryByGitlabUserName(pipelineWebHookDTO.getUser().getUsername());
        if (userAttrE != null) {
            gitlabUserId = TypeUtil.objToInteger(userAttrE.getGitlabUserId());
        }
        //查询pipeline最新阶段信息


        List<Stage> stages = new ArrayList<>();
        List<String> stageNames = new ArrayList<>();
        List<Integer> gitlabJobIds = gitlabProjectRepository
                .listJobs(applicationE.getGitlabProjectE().getId(), TypeUtil.objToInteger(pipelineWebHookDTO.getObjectAttributes().getId()), gitlabUserId).stream().map(GitlabJobE::getId).collect(Collectors.toList());

        Stage sonar = null;
        List<CommitStatuseDTO> commitStatuseDTOS = gitlabProjectRepository.getCommitStatus(applicationE.getGitlabProjectE().getId(), pipelineWebHookDTO.getObjectAttributes().getSha(), ADMIN);
        for (CommitStatuseDTO commitStatuseDTO : commitStatuseDTOS) {
            if (gitlabJobIds.contains(commitStatuseDTO.getId())) {
                Stage stage = getPipelibeStage(commitStatuseDTO);
                stages.add(stage);
            } else if (commitStatuseDTO.getName().equals(SONARQUBE) && !stageNames.contains(SONARQUBE) && !stages.isEmpty()) {
                Stage stage = getPipelibeStage(commitStatuseDTO);
                sonar = stage;
                stages.add(stage);
                stageNames.add(commitStatuseDTO.getName());
            }
        }
        DevopsGitlabCommitE devopsGitlabCommitE = devopsGitlabCommitRepository.queryByShaAndRef(pipelineWebHookDTO.getObjectAttributes().getSha(), pipelineWebHookDTO.getObjectAttributes().getRef());

        //pipeline不存在则创建,存在则更新状态和阶段信息
        if (devopsGitlabPipelineE == null) {
            devopsGitlabPipelineE = new DevopsGitlabPipelineE();
            devopsGitlabPipelineE.setAppId(applicationE.getId());
            devopsGitlabPipelineE.setPipelineCreateUserId(userAttrE == null ? null : userAttrE.getIamUserId());
            devopsGitlabPipelineE.setPipelineId(pipelineWebHookDTO.getObjectAttributes().getId());
            devopsGitlabPipelineE.setStatus(pipelineWebHookDTO.getObjectAttributes().getStatus());
            devopsGitlabPipelineE.setPipelineCreationDate(pipelineWebHookDTO.getObjectAttributes().getCreatedAt());
            if (devopsGitlabCommitE != null) {
                devopsGitlabPipelineE.initDevopsGitlabCommitEById(devopsGitlabCommitE.getId());
            }
            devopsGitlabPipelineE.setStage(JSONArray.toJSONString(stages));
            devopsGitlabPipelineRepository.create(devopsGitlabPipelineE);
        } else {
            devopsGitlabPipelineE.setStatus(pipelineWebHookDTO.getObjectAttributes().getStatus());

            List<Stage> originalStages = JSONArray.parseArray(devopsGitlabPipelineE.getStage(), Stage.class);
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
            devopsGitlabPipelineE.setStage(JSONArray.toJSONString(result));

            if (devopsGitlabCommitE != null) {
                devopsGitlabPipelineE.initDevopsGitlabCommitEById(devopsGitlabCommitE.getId());
            }
            devopsGitlabPipelineRepository.update(devopsGitlabPipelineE);
        }
    }


    private Stage getPipelibeStage(CommitStatuseDTO commitStatuseDTO) {
        Stage stage = new Stage();
        stage.setDescription(commitStatuseDTO.getDescription());
        stage.setId(commitStatuseDTO.getId());
        stage.setName(commitStatuseDTO.getName());
        stage.setStatus(commitStatuseDTO.getStatus());
        if (commitStatuseDTO.getFinishedAt() != null) {
            stage.setFinishedAt(commitStatuseDTO.getFinishedAt());
        }
        if (commitStatuseDTO.getStartedAt() != null) {
            stage.setStartedAt(commitStatuseDTO.getStartedAt());
        }
        return stage;
    }

    @Override
    public void updateStages(JobWebHookDTO jobWebHookDTO) {
        //按照job的状态实时更新pipeline阶段的状态
        DevopsGitlabCommitE devopsGitlabCommitE = devopsGitlabCommitRepository.queryByShaAndRef(jobWebHookDTO.getSha(), jobWebHookDTO.getRef());
        if (devopsGitlabCommitE != null && !"created".equals(jobWebHookDTO.getBuildStatus())) {
            DevopsGitlabPipelineE devopsGitlabPipelineE = devopsGitlabPipelineRepository.queryByCommitId(devopsGitlabCommitE.getId());
            if (devopsGitlabPipelineE != null) {
                List<Stage> stages = JSONArray.parseArray(devopsGitlabPipelineE.getStage(), Stage.class);
                stages.stream().filter(stage -> jobWebHookDTO.getBuildName().equals(stage.getName())).forEach(stage ->
                        stage.setStatus(jobWebHookDTO.getBuildStatus())
                );
                devopsGitlabPipelineE.setStage(JSONArray.toJSONString(stages));
                devopsGitlabPipelineRepository.update(devopsGitlabPipelineE);
            }
        }
    }

    @Override
    public PipelineTimeDTO getPipelineTime(Long appId, Date startTime, Date endTime) {
        if (appId == null) {
            return new PipelineTimeDTO();
        }
        PipelineTimeDTO pipelineTimeDTO = new PipelineTimeDTO();
        List<DevopsGitlabPipelineDO> devopsGitlabPipelineDOS = devopsGitlabPipelineRepository.listPipeline(appId, startTime, endTime);
        List<String> pipelineTimes = new LinkedList<>();
        List<String> refs = new LinkedList<>();
        List<String> versions = new LinkedList<>();
        List<Date> createDates = new LinkedList<>();
        devopsGitlabPipelineDOS.forEach(devopsGitlabPipelineDO -> {
            refs.add(devopsGitlabPipelineDO.getRef() + "-" + devopsGitlabPipelineDO.getSha());
            createDates.add(devopsGitlabPipelineDO.getPipelineCreationDate());
            ApplicationVersionE applicationVersionE = applicationVersionRepository.baseQueryByCommitSha(appId, devopsGitlabPipelineDO.getRef(), devopsGitlabPipelineDO.getSha());
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
        List<DevopsGitlabPipelineDO> devopsGitlabPipelineDOS = devopsGitlabPipelineRepository.listPipeline(appId, startTime, endTime);
        //按照创建时间分组
        Map<String, List<DevopsGitlabPipelineDO>> resultMaps = devopsGitlabPipelineDOS.stream()
                .collect(Collectors.groupingBy(t -> new java.sql.Date(t.getPipelineCreationDate().getTime()).toString()));
        //将创建时间排序
        List<String> creationDates = devopsGitlabPipelineDOS.stream().map(deployDO -> new java.sql.Date(deployDO.getPipelineCreationDate().getTime()).toString()).collect(Collectors.toList());
        List<Long> pipelineFrequencys = new LinkedList<>();
        List<Long> pipelineSuccessFrequency = new LinkedList<>();
        List<Long> pipelineFailFrequency = new LinkedList<>();
        creationDates = new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        creationDates.forEach(date -> {
            Long[] newPipelineFrequencys = {0L};
            Long[] newPipelineSuccessFrequency = {0L};
            Long[] newPipelineFailFrequency = {0L};
            resultMaps.get(date).forEach(devopsGitlabPipelineDO -> {
                if ("success".equals(devopsGitlabPipelineDO.getStatus())) {
                    newPipelineSuccessFrequency[0] = newPipelineSuccessFrequency[0] + 1L;
                }
                if ("failed".equals(devopsGitlabPipelineDO.getStatus())) {
                    newPipelineFailFrequency[0] = newPipelineFailFrequency[0] + 1L;
                }
                newPipelineFrequencys[0] = newPipelineSuccessFrequency[0] + newPipelineFailFrequency[0];
            });
            pipelineFrequencys.add(newPipelineFrequencys[0]);
            pipelineSuccessFrequency.add(newPipelineSuccessFrequency[0]);
            pipelineFailFrequency.add(newPipelineFailFrequency[0]);
        });
        pipelineFrequencyDTO.setCreateDates(creationDates);
        pipelineFrequencyDTO.setPipelineFailFrequency(pipelineFailFrequency);
        pipelineFrequencyDTO.setPipelineFrequencys(pipelineFrequencys);
        pipelineFrequencyDTO.setPipelineSuccessFrequency(pipelineSuccessFrequency);
        return pipelineFrequencyDTO;
    }


    @Override
    public PageInfo<DevopsGitlabPipelineDTO> pagePipelines(Long appId, String branch, PageRequest pageRequest, Date startTime, Date endTime) {
        if (appId == null) {
            return new PageInfo<>();
        }
        PageInfo<DevopsGitlabPipelineDTO> pageDevopsGitlabPipelineDTOS = new PageInfo<>();
        List<DevopsGitlabPipelineDTO> devopsGiltabPipelineDTOS = new ArrayList<>();
        PageInfo<DevopsGitlabPipelineDO> devopsGitlabPipelineDOS = new PageInfo<>();
        if (branch == null) {
            devopsGitlabPipelineDOS = devopsGitlabPipelineRepository.pagePipeline(appId, pageRequest, startTime, endTime);
        } else {
            devopsGitlabPipelineDOS.setList(devopsGitlabPipelineRepository.listByBranch(appId, branch));
        }
        BeanUtils.copyProperties(devopsGitlabPipelineDOS, pageDevopsGitlabPipelineDTOS);

        //按照ref分组
        Map<String, List<DevopsGitlabPipelineDO>> refWithPipelines = devopsGitlabPipelineDOS.getList().stream()
                .filter(pageDevopsGitlabPipelineDTO -> pageDevopsGitlabPipelineDTO.getRef() != null)
                .collect(Collectors.groupingBy(DevopsGitlabPipelineDO::getRef));
        Map<String, Long> refWithPipelineIds = new HashMap<>();

        //获取每个分支上最新的一条pipeline记录，用于后续标记latest
        refWithPipelines.forEach((key, value) -> {
            Long pipeLineId = Collections.max(value.stream().map(DevopsGitlabPipelineDO::getPipelineId).collect(Collectors.toList()));
            refWithPipelineIds.put(key, pipeLineId);
        });

        ApplicationE applicationE = applicationRepository.query(appId);
        ProjectVO projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
        OrganizationVO organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        //获取pipeline记录
        Set<Long> userIds = new HashSet<>();
        devopsGitlabPipelineDOS.getList().stream().forEach(devopsGitlabPipelineDO -> {
            userIds.add(devopsGitlabPipelineDO.getCommitUserId());
            userIds.add(devopsGitlabPipelineDO.getPipelineCreateUserId());
        });
        List<UserE> userES = iamRepository.listUsersByIds(new ArrayList<>(userIds));
        devopsGitlabPipelineDOS.getList().forEach(devopsGitlabPipelineDO -> {
            DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = new DevopsGitlabPipelineDTO();
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
            devopsGitlabPipelineDTO.setGitlabProjectId(TypeUtil.objToLong(applicationE.getGitlabProjectE().getId()));
            devopsGitlabPipelineDTO.setPipelineId(devopsGitlabPipelineDO.getPipelineId());
            if (("success").equals(devopsGitlabPipelineDO.getStatus())) {
                devopsGitlabPipelineDTO.setStatus("passed");
            } else {
                devopsGitlabPipelineDTO.setStatus(devopsGitlabPipelineDO.getStatus());
            }
            devopsGitlabPipelineDTO.setRef(devopsGitlabPipelineDO.getRef());
            String version = applicationVersionRepository.baseQueryByPipelineId(devopsGitlabPipelineDO.getPipelineId(), devopsGitlabPipelineDO.getRef(), appId);
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
                    + organization.getCode() + "-" + projectE.getCode() + "/"
                    + applicationE.getCode() + ".git");
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
}
