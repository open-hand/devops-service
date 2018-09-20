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
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsGitlabPipelineDO;
import io.choerodon.devops.infra.dataobject.gitlab.CommitStatuseDO;
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
        if (pipelineWebHookDTO.getUser().getUsername().equals("admin1")) {
            pipelineWebHookDTO.getUser().setUsername("admin");
        }
        UserE userE = iamRepository.queryByLoginName(pipelineWebHookDTO.getUser().getUsername());
        Integer gitlabUserId = ADMIN;
        if (userE != null) {
            gitlabUserId = TypeUtil.objToInteger(userAttrRepository.queryById(userE.getId()).getGitlabUserId());
        }
        List<CommitStatuseDO> commitStatuseDOS = gitlabProjectRepository
                .getCommitStatuse(applicationE.getGitlabProjectE().getId(), pipelineWebHookDTO.getObjectAttributes().getSha(), gitlabUserId);
        DevopsGitlabCommitE devopsGitlabCommitE = devopsGitlabCommitRepository.queryBySha(pipelineWebHookDTO.getObjectAttributes().getSha());
        if (devopsGitlabPipelineE == null) {
            devopsGitlabPipelineE = new DevopsGitlabPipelineE();
            devopsGitlabPipelineE.setAppId(applicationE.getId());
            devopsGitlabPipelineE.setPipelineCreateUserId(userE.getId());
            devopsGitlabPipelineE.setPipelineId(pipelineWebHookDTO.getObjectAttributes().getId());
            devopsGitlabPipelineE.setStatus(pipelineWebHookDTO.getObjectAttributes()
                    .getDetailedStatus());
            devopsGitlabPipelineE.setPipelineCreationDate(pipelineWebHookDTO.getObjectAttributes().getCreatedAt());
            if (devopsGitlabCommitE != null) {
                devopsGitlabPipelineE.initDevopsGitlabCommitEById(devopsGitlabCommitE.getId());
            }
            devopsGitlabPipelineE.setStage(JSONArray.toJSONString(commitStatuseDOS));
            devopsGitlabPipelineRepository.create(devopsGitlabPipelineE);
        } else {
            devopsGitlabPipelineE.setStatus(pipelineWebHookDTO.getObjectAttributes().getDetailedStatus());
            devopsGitlabPipelineE.setStage(JSONArray.toJSONString(commitStatuseDOS));
            if (devopsGitlabCommitE != null) {
                devopsGitlabPipelineE.initDevopsGitlabCommitEById(devopsGitlabCommitE.getId());
            }
            devopsGitlabPipelineRepository.update(devopsGitlabPipelineE);
        }
    }


    @Override
    public void updateStages(JobWebHookDTO jobWebHookDTO) {
        DevopsGitlabCommitE devopsGitlabCommitE = devopsGitlabCommitRepository.queryBySha(jobWebHookDTO.getSha());
        if (!jobWebHookDTO.getBuildStatus().equals("created")) {
            if (devopsGitlabCommitE != null) {
                DevopsGitlabPipelineE devopsGitlabPipelineE = devopsGitlabPipelineRepository.queryByCommitId(devopsGitlabCommitE.getId());
                if (devopsGitlabPipelineE != null) {
                    List<CommitStatuseDO> commitStatuseDOS = JSONArray.parseArray(devopsGitlabPipelineE.getStage(), CommitStatuseDO.class);
                    commitStatuseDOS.parallelStream().filter(commitStatuseDO -> jobWebHookDTO.getBuildName().equals(commitStatuseDO.getName())).forEach(commitStatuseDO -> {
                        commitStatuseDO.setStatus(jobWebHookDTO.getBuildStatus());
                    });
                    devopsGitlabPipelineE.setStage(JSONArray.toJSONString(commitStatuseDOS));
                    devopsGitlabPipelineRepository.update(devopsGitlabPipelineE);
                }
            }
        }
    }

    @Override
    public PipelineTimeDTO getPipelineTime(Long appId, Date startTime, Date endTime) {
        if (appId == null) {
            return new PipelineTimeDTO();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        PipelineTimeDTO pipelineTimeDTO = new PipelineTimeDTO();
        List<DevopsGitlabPipelineDO> devopsGitlabPipelineDOS = devopsGitlabPipelineRepository.listPipeline(appId, startTime, endTime);
        List<String> pipelineTimes = new LinkedList<>();
        List<String> refs = new LinkedList<>();
        List<String> versions = new LinkedList<>();
        List<Date> createDates = new LinkedList<>();
        devopsGitlabPipelineDOS.stream().forEach(devopsGitlabPipelineDO -> {
            refs.add(devopsGitlabPipelineDO.getRef() + "-" + devopsGitlabPipelineDO.getSha());
            createDates.add(devopsGitlabPipelineDO.getPipelineCreationDate());
            ApplicationVersionE applicationVersionE = applicationVersionRepository.queryByCommitSha(devopsGitlabPipelineDO.getSha());
            if (applicationVersionE != null) {
                versions.add(applicationVersionE.getVersion());
            } else {
                versions.add("");
            }
            List<CommitStatuseDO> commitStatuseDOS = JSONArray.parseArray(devopsGitlabPipelineDO.getStage(), CommitStatuseDO.class);
            pipelineTimes.add(getPipelineTime(commitStatuseDOS));
        });
        pipelineTimeDTO.setCreateDates(createDates);
        pipelineTimeDTO.setPipelineTime(pipelineTimes);
        pipelineTimeDTO.setRefs(refs);
        pipelineTimeDTO.setVersions(versions);
        return pipelineTimeDTO;
    }

    private String getPipelineTime(List<CommitStatuseDO> commitStatuseDOS) {
        Long diff = 0L;
        for (CommitStatuseDO commitStatuseDO : commitStatuseDOS) {
            try {
                if (commitStatuseDO.getFinished_at() != null && commitStatuseDO.getStarted_at() != null) {
                    diff = diff + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(commitStatuseDO.getFinished_at()).getTime() - new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(commitStatuseDO.getStarted_at()).getTime();
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
        Map<String, List<DevopsGitlabPipelineDO>> resultMaps = devopsGitlabPipelineDOS.stream()
                .collect(Collectors.groupingBy(t -> new java.sql.Date(t.getPipelineCreationDate().getTime()).toString()));
        List<String> creationDates = devopsGitlabPipelineDOS.parallelStream().map(deployDO -> new java.sql.Date(deployDO.getPipelineCreationDate().getTime()).toString()).collect(Collectors.toList());
        List<Long> PipelineFrequencys = new LinkedList<>();
        List<Long> PipelineSuccessFrequency = new LinkedList<>();
        List<Long> PipelineFailFrequency = new LinkedList<>();
        creationDates = new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        creationDates.stream().forEach(date -> {
            Long[] newPipelineFrequencys = {0L};
            Long[] newPipelineSuccessFrequency = {0L};
            Long[] newPipelineFailFrequency = {0L};
            resultMaps.get(date).stream().forEach(devopsGitlabPipelineDO -> {
                if (devopsGitlabPipelineDO.getStatus().equals("passed")) {
                    newPipelineSuccessFrequency[0] = newPipelineSuccessFrequency[0] + 1L;
                }
                if (devopsGitlabPipelineDO.getStatus().equals("failed")) {
                    newPipelineFailFrequency[0] = newPipelineFailFrequency[0] + 1L;
                }
                newPipelineFrequencys[0] = newPipelineSuccessFrequency[0] + newPipelineFailFrequency[0];
            });
            PipelineFrequencys.add(newPipelineFrequencys[0]);
            PipelineSuccessFrequency.add(newPipelineSuccessFrequency[0]);
            PipelineFailFrequency.add(newPipelineFailFrequency[0]);
        });
        pipelineFrequencyDTO.setCreateDates(creationDates);
        pipelineFrequencyDTO.setPipelineFailFrequency(PipelineFailFrequency);
        pipelineFrequencyDTO.setPipelineFrequencys(PipelineFrequencys);
        pipelineFrequencyDTO.setPipelineSuccessFrequency(PipelineSuccessFrequency);
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
        Map<String, List<DevopsGitlabPipelineDO>> refWithPipelines = devopsGitlabPipelineDOS.stream()
                .collect(Collectors.groupingBy(t -> t.getRef()));
        Map<String, Long> refWithPipelineIds = new HashMap<>();
        refWithPipelines.forEach((key, value) -> {
            Long pipeLineId = Collections.max(value.parallelStream().map(DevopsGitlabPipelineDO::getPipelineId).collect(Collectors.toList()));
            refWithPipelineIds.put(key, pipeLineId);
        });
        ApplicationE applicationE = applicationRepository.query(appId);
        ProjectE projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        devopsGitlabPipelineDOS.getContent().forEach(devopsGitlabPipelineDO -> {
            DevopsGitlabPipelineDTO devopsGitlabPipelineDTO = new DevopsGitlabPipelineDTO();
            if (devopsGitlabPipelineDO.getPipelineId().equals(refWithPipelineIds.get(devopsGitlabPipelineDO.getRef()))) {
                devopsGitlabPipelineDTO.setLatest(true);
            }
            devopsGitlabPipelineDTO.setCommit(devopsGitlabPipelineDO.getSha());
            devopsGitlabPipelineDTO.setCommitContent(devopsGitlabPipelineDO.getContent());
            UserE userE = iamRepository.queryById(devopsGitlabPipelineDO.getCommitUserId());
            if (userE != null) {
                devopsGitlabPipelineDTO.setCommitUserUrl(userE.getImageUrl());
            }
            UserE newUserE = iamRepository.queryById(devopsGitlabPipelineDO.getPipelineCreateUserId());
            if (newUserE != null) {
                devopsGitlabPipelineDTO.setPipelineUserUrl(newUserE.getImageUrl());
            }
            devopsGitlabPipelineDTO.setCreationDate(devopsGitlabPipelineDO.getPipelineCreationDate());
            devopsGitlabPipelineDTO.setPipelineId(devopsGitlabPipelineDO.getPipelineId());
            devopsGitlabPipelineDTO.setStatus(devopsGitlabPipelineDO.getStatus());
            devopsGitlabPipelineDTO.setRef(devopsGitlabPipelineDO.getRef());
            ApplicationVersionE applicationVersionE = applicationVersionRepository.queryByCommitSha(devopsGitlabPipelineDO.getSha());
            if (applicationVersionE != null) {
                devopsGitlabPipelineDTO.setVersion(applicationVersionE.getVersion());
            }
            List<CommitStatuseDO> commitStatuseDOS = JSONArray.parseArray(devopsGitlabPipelineDO.getStage(), CommitStatuseDO.class);
            devopsGitlabPipelineDTO.setPipelineTime(getPipelineTime(commitStatuseDOS));
            devopsGitlabPipelineDTO.setStages(commitStatuseDOS);
            devopsGitlabPipelineDTO.setGitlabUrl(gitlabUrl + "/"
                    + organization.getCode() + "-" + projectE.getCode() + "/"
                    + applicationE.getCode() + ".git");
            devopsGiltabPipelineDTOS.add(devopsGitlabPipelineDTO);
        });
        pageDevopsGitlabPipelineDTOS.setContent(devopsGiltabPipelineDTOS);
        return pageDevopsGitlabPipelineDTOS;
    }


    public String getDeployTime(Long diff) {
        float num = (float) diff / (60 * 1000);
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
    }
}
