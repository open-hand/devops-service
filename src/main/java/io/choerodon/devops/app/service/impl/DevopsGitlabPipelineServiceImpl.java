package io.choerodon.devops.app.service.impl;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.app.service.DevopsGitlabPipelineService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.Stage;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsGitlabPipelineDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsGitlabPipelineServiceImpl implements DevopsGitlabPipelineService {

    private static final Integer ADMIN = 1;
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
    @Saga(code = "devops-gitlab-pipeline", description = "gitlab-pipeline", inputSchemaClass = PipelineWebHookDTO.class)
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
        UserE userE = iamRepository.queryByLoginName(pipelineWebHookDTO.getUser().getUsername());
        Integer gitlabUserId = ADMIN;

        if (userE.getId() != null) {
            UserAttrE userAttrE = userAttrRepository.queryById(userE.getId());
            if (userAttrE != null) {
                gitlabUserId = TypeUtil.objToInteger(userAttrE.getGitlabUserId());
            }
        }
        //查询pipeline最新阶段信息

        List<Stage> stages = gitlabProjectRepository
                .getCommitStatuse(applicationE.getGitlabProjectE().getId(), pipelineWebHookDTO.getObjectAttributes().getSha(), gitlabUserId).stream().map(commitStatuseDO -> {
                    Stage stage = new Stage();
                    stage.setDescription(commitStatuseDO.getDescription());
                    stage.setName(commitStatuseDO.getName());
                    stage.setId(commitStatuseDO.getId());
                    stage.setStatus(commitStatuseDO.getStatus());
                    if (commitStatuseDO.getFinishedAt() != null) {
                        stage.setFinishedAt(commitStatuseDO.getFinishedAt());
                    }
                    if (commitStatuseDO.getStartedAt() != null) {
                        stage.setStartedAt(commitStatuseDO.getStartedAt());
                    }
                    if (pipelineWebHookDTO.getObjectAttributes().getRef().equals(commitStatuseDO.getRef())) {
                        return stage;
                    } else {
                        return null;
                    }
                }).collect(Collectors.toList());
        stages.removeAll(Collections.singleton(null));
        DevopsGitlabCommitE devopsGitlabCommitE = devopsGitlabCommitRepository.queryByShaAndRef(pipelineWebHookDTO.getObjectAttributes().getSha(), pipelineWebHookDTO.getObjectAttributes().getRef());
        //pipeline不存在则创建,存在则更新状态和阶段信息
        if (devopsGitlabPipelineE == null) {
            devopsGitlabPipelineE = new DevopsGitlabPipelineE();
            devopsGitlabPipelineE.setAppId(applicationE.getId());
            devopsGitlabPipelineE.setPipelineCreateUserId(userE.getId() == null ? null : userE.getId());
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
            devopsGitlabPipelineE.setStage(JSONArray.toJSONString(stages));
            if (devopsGitlabCommitE != null) {
                devopsGitlabPipelineE.initDevopsGitlabCommitEById(devopsGitlabCommitE.getId());
            }
            devopsGitlabPipelineRepository.update(devopsGitlabPipelineE);
        }
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
            ApplicationVersionE applicationVersionE = applicationVersionRepository.queryByCommitSha(devopsGitlabPipelineDO.getSha());
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
                if ("passed".equals(devopsGitlabPipelineDO.getStatus())) {
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
    public Page<DevopsGitlabPipelineDTO> pagePipelines(Long appId, PageRequest pageRequest, Date startTime, Date endTime) {
        if (appId == null) {
            return new Page<>();
        }
        Page<DevopsGitlabPipelineDTO> pageDevopsGitlabPipelineDTOS = new Page<>();
        List<DevopsGitlabPipelineDTO> devopsGiltabPipelineDTOS = new ArrayList<>();
        Page<DevopsGitlabPipelineDO> devopsGitlabPipelineDOS = devopsGitlabPipelineRepository.pagePipeline(appId, pageRequest, startTime, endTime);
        BeanUtils.copyProperties(devopsGitlabPipelineDOS, pageDevopsGitlabPipelineDTOS);

        //按照ref分组
        Map<String, List<DevopsGitlabPipelineDO>> refWithPipelines = devopsGitlabPipelineDOS.stream()
                .filter(pageDevopsGitlabPipelineDTO -> pageDevopsGitlabPipelineDTO.getRef() != null)
                .collect(Collectors.groupingBy(DevopsGitlabPipelineDO::getRef));
        Map<String, Long> refWithPipelineIds = new HashMap<>();

        //获取每个分支上最新的一条pipeline记录，用于后续标记latest
        refWithPipelines.forEach((key, value) -> {
            Long pipeLineId = Collections.max(value.stream().map(DevopsGitlabPipelineDO::getPipelineId).collect(Collectors.toList()));
            refWithPipelineIds.put(key, pipeLineId);
        });

        ApplicationE applicationE = applicationRepository.query(appId);
        ProjectE projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        //获取pipeline记录
        devopsGitlabPipelineDOS.getContent().forEach(devopsGitlabPipelineDO -> {
            DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = new DevopsGitlabPipelineDTO();
            if (devopsGitlabPipelineDO.getPipelineId().equals(refWithPipelineIds.get(devopsGitlabPipelineDO.getRef()))) {
                devopsGitlabPipelineDTO.setLatest(true);
            }
            devopsGitlabPipelineDTO.setCommit(devopsGitlabPipelineDO.getSha());
            devopsGitlabPipelineDTO.setCommitContent(devopsGitlabPipelineDO.getContent());
            UserE userE = iamRepository.queryUserByUserId(devopsGitlabPipelineDO.getCommitUserId());
            if (userE != null) {
                devopsGitlabPipelineDTO.setCommitUserUrl(userE.getImageUrl());
                devopsGitlabPipelineDTO.setCommitUserName(userE.getRealName());
            }
            UserE newUserE = iamRepository.queryUserByUserId(devopsGitlabPipelineDO.getPipelineCreateUserId());
            if (newUserE != null) {
                devopsGitlabPipelineDTO.setPipelineUserUrl(newUserE.getImageUrl());
                devopsGitlabPipelineDTO.setPipelineUserName(newUserE.getRealName());
            }
            devopsGitlabPipelineDTO.setCreationDate(devopsGitlabPipelineDO.getPipelineCreationDate());
            devopsGitlabPipelineDTO.setGitlabProjectId(TypeUtil.objToLong(applicationE.getGitlabProjectE().getId()));
            devopsGitlabPipelineDTO.setPipelineId(devopsGitlabPipelineDO.getPipelineId());
            devopsGitlabPipelineDTO.setStatus(devopsGitlabPipelineDO.getStatus());
            devopsGitlabPipelineDTO.setRef(devopsGitlabPipelineDO.getRef());
            ApplicationVersionE applicationVersionE = applicationVersionRepository.queryByCommitSha(devopsGitlabPipelineDO.getSha());
            if (applicationVersionE != null) {
                devopsGitlabPipelineDTO.setVersion(applicationVersionE.getVersion());
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
        pageDevopsGitlabPipelineDTOS.setContent(devopsGiltabPipelineDTOS);
        return pageDevopsGitlabPipelineDTOS;
    }


    private String getDeployTime(Long diff) {
        float num = (float) diff / (60 * 1000);
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
    }
}
