package io.choerodon.devops.app.service.impl;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CiPipelineScheduleVO;
import io.choerodon.devops.app.service.AppExternalConfigService;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.CiPipelineScheduleService;
import io.choerodon.devops.app.service.CiScheduleVariableService;
import io.choerodon.devops.infra.dto.AppExternalConfigDTO;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.CiPipelineScheduleDTO;
import io.choerodon.devops.infra.dto.CiScheduleVariableDTO;
import io.choerodon.devops.infra.dto.gitlab.PipelineSchedule;
import io.choerodon.devops.infra.dto.gitlab.Variable;
import io.choerodon.devops.infra.enums.CiPipelineScheduleTriggerTypeEnum;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.CiPipelineScheduleMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.util.UserDTOFillUtil;

/**
 * devops_ci_pipeline_schedule(CiPipelineSchedule)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:00:27
 */
@Service
public class CiPipelineScheduleServiceImpl implements CiPipelineScheduleService {
    @Autowired
    private CiPipelineScheduleMapper ciPipelineScheduleMapper;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private CiScheduleVariableService ciScheduleVariableService;
    @Autowired
    private AppExternalConfigService appExternalConfigService;


    @Override
    @Transactional
    public CiPipelineScheduleDTO create(CiPipelineScheduleVO ciPipelineScheduleVO) {
        validate(ciPipelineScheduleVO);

        Long appServiceId = ciPipelineScheduleVO.getAppServiceId();
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        int gitlabProjectId = TypeUtil.objToInt(appServiceDTO.getGitlabProjectId());

        String cron = calculateCron(ciPipelineScheduleVO);

        PipelineSchedule pipelineSchedule = new PipelineSchedule();
        pipelineSchedule.setCron(cron);
        pipelineSchedule.setRef(ciPipelineScheduleVO.getRef());
        pipelineSchedule.setDescription(ciPipelineScheduleVO.getName());
        pipelineSchedule.setCronTimezone("UTC");

        PipelineSchedule pipelineSchedules;
        AppExternalConfigDTO appExternalConfigDTO = null;
        if (appServiceDTO.getExternalConfigId() != null) {
            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        }
        // 1. 创建定时计划
        pipelineSchedules = gitlabServiceClientOperator
                .createPipelineSchedule(gitlabProjectId,
                        null,
                        appExternalConfigDTO,
                        pipelineSchedule);
        // 2. 如果有变量，创建变量
        if (!CollectionUtils.isEmpty(ciPipelineScheduleVO.getVariableVOList())) {
            AppExternalConfigDTO finalAppExternalConfigDTO = appExternalConfigDTO;
            ciPipelineScheduleVO.getVariableVOList().forEach(variable -> {
                Variable variable1 = new Variable();
                variable1.setKey(variable.getKey());
                variable1.setValue(variable.getValue());

                gitlabServiceClientOperator.createScheduleVariable(gitlabProjectId,
                        pipelineSchedules.getId(),
                        null,
                        finalAppExternalConfigDTO,
                        variable1);
            });
        }
        CiPipelineScheduleDTO ciPipelineScheduleDTO = ConvertUtils.convertObject(ciPipelineScheduleVO, CiPipelineScheduleDTO.class);
        ciPipelineScheduleDTO.setPipelineScheduleId(TypeUtil.objToLong(pipelineSchedules.getId()));

        MapperUtil.resultJudgedInsertSelective(ciPipelineScheduleMapper, ciPipelineScheduleDTO, "error.save.pipeline.schedule.failed");
        if (!CollectionUtils.isEmpty(ciPipelineScheduleVO.getVariableVOList())) {
            ciPipelineScheduleVO.getVariableVOList().forEach(variable -> {
                CiScheduleVariableDTO ciScheduleVariableDTO = ConvertUtils.convertObject(variable, CiScheduleVariableDTO.class);
                ciScheduleVariableDTO.setCiPipelineScheduleId(ciPipelineScheduleDTO.getId());
                ciScheduleVariableDTO.setPipelineScheduleId(TypeUtil.objToLong(pipelineSchedules.getId()));
                ciScheduleVariableService.baseCreate(ciScheduleVariableDTO);
            });
        }

        return ciPipelineScheduleDTO;
    }

    @Override
    public List<CiPipelineScheduleVO> listByAppServiceId(Long projectId, Long appServiceId) {

        CiPipelineScheduleDTO ciPipelineScheduleDTO = new CiPipelineScheduleDTO();
        ciPipelineScheduleDTO.setAppServiceId(appServiceId);

        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);


        List<CiPipelineScheduleDTO> ciPipelineScheduleDTOS = ciPipelineScheduleMapper.select(ciPipelineScheduleDTO);

        List<CiPipelineScheduleVO> ciPipelineScheduleVOS = ConvertUtils.convertList(ciPipelineScheduleDTOS, CiPipelineScheduleVO.class);

        UserDTOFillUtil.fillUserInfo(ciPipelineScheduleVOS, "lastUpdatedBy", "userDTO");

        AppExternalConfigDTO appExternalConfigDTO = null;
        if (appServiceDTO.getExternalConfigId() != null) {
            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        }

        List<PipelineSchedule> pipelineSchedules = gitlabServiceClientOperator.listPipelineSchedules(TypeUtil.objToInt(appServiceDTO.getGitlabProjectId()),
                null,
                appExternalConfigDTO);

        Map<Integer, PipelineSchedule> pipelineScheduleMap = pipelineSchedules.stream().collect(Collectors.toMap(PipelineSchedule::getId, Function.identity()));
        ciPipelineScheduleVOS.forEach(v -> {
            PipelineSchedule pipelineSchedule = pipelineScheduleMap.get(v.getPipelineScheduleId());
            if (pipelineSchedule != null) {
                v.setNextRunAt(pipelineSchedule.getNextRunAt());
                v.setActive(pipelineSchedule.getActive());
            }
        });

        return ciPipelineScheduleVOS;
    }

    @Override
    public void enableSchedule(Long projectId, Long id) {
        CiPipelineScheduleDTO ciPipelineScheduleDTO = ciPipelineScheduleMapper.selectByPrimaryKey(id);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciPipelineScheduleDTO.getAppServiceId());

        AppExternalConfigDTO appExternalConfigDTO = null;
        if (appServiceDTO.getExternalConfigId() != null) {
            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        }
        PipelineSchedule pipelineSchedule = gitlabServiceClientOperator.queryPipelineSchedule(TypeUtil.objToInt(appServiceDTO.getGitlabProjectId()),
                null,
                TypeUtil.objToInt(ciPipelineScheduleDTO.getPipelineScheduleId()),
                appExternalConfigDTO);

        if (Boolean.TRUE.equals(pipelineSchedule.getActive())) {
            return;
        }
        pipelineSchedule.setActive(true);
        gitlabServiceClientOperator.updatePipelineSchedule(TypeUtil.objToInt(appServiceDTO.getGitlabProjectId()),
                null,
                TypeUtil.objToInt(ciPipelineScheduleDTO.getPipelineScheduleId()),
                appExternalConfigDTO,
                pipelineSchedule);
    }

    @Override
    public void disableSchedule(Long projectId, Long id) {
        CiPipelineScheduleDTO ciPipelineScheduleDTO = ciPipelineScheduleMapper.selectByPrimaryKey(id);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciPipelineScheduleDTO.getAppServiceId());

        AppExternalConfigDTO appExternalConfigDTO = null;
        if (appServiceDTO.getExternalConfigId() != null) {
            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        }
        PipelineSchedule pipelineSchedule = gitlabServiceClientOperator.queryPipelineSchedule(TypeUtil.objToInt(appServiceDTO.getGitlabProjectId()),
                null,
                TypeUtil.objToInt(ciPipelineScheduleDTO.getPipelineScheduleId()),
                appExternalConfigDTO);

        if (Boolean.FALSE.equals(pipelineSchedule.getActive())) {
            return;
        }
        pipelineSchedule.setActive(false);
        gitlabServiceClientOperator.updatePipelineSchedule(TypeUtil.objToInt(appServiceDTO.getGitlabProjectId()),
                null,
                TypeUtil.objToInt(ciPipelineScheduleDTO.getPipelineScheduleId()),
                appExternalConfigDTO,
                pipelineSchedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long projectId, Long id) {
        CiPipelineScheduleDTO ciPipelineScheduleDTO = ciPipelineScheduleMapper.selectByPrimaryKey(id);
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(ciPipelineScheduleDTO.getAppServiceId());

        AppExternalConfigDTO appExternalConfigDTO = null;
        if (appServiceDTO.getExternalConfigId() != null) {
            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        }
        gitlabServiceClientOperator.deletePipelineSchedule(TypeUtil.objToInt(appServiceDTO.getGitlabProjectId()),
                null,
                TypeUtil.objToInt(ciPipelineScheduleDTO.getPipelineScheduleId()),
                appExternalConfigDTO);
        ciPipelineScheduleMapper.deleteByPrimaryKey(id);
        ciScheduleVariableService.deleteByPipelineScheduleId(id);

    }

    @Override
    @Transactional
    public void update(CiPipelineScheduleVO ciPipelineScheduleVO) {
        validate(ciPipelineScheduleVO);

        Long appServiceId = ciPipelineScheduleVO.getAppServiceId();
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        int gitlabProjectId = TypeUtil.objToInt(appServiceDTO.getGitlabProjectId());

        String cron = calculateCron(ciPipelineScheduleVO);

        PipelineSchedule pipelineSchedule = new PipelineSchedule();
        pipelineSchedule.setCron(cron);
        pipelineSchedule.setRef(ciPipelineScheduleVO.getRef());
        pipelineSchedule.setDescription(ciPipelineScheduleVO.getName());
        pipelineSchedule.setCronTimezone("UTC");

        PipelineSchedule pipelineSchedules;
        AppExternalConfigDTO appExternalConfigDTO = null;
        if (appServiceDTO.getExternalConfigId() != null) {
            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        }
        // 1. 创建定时计划
        pipelineSchedules = gitlabServiceClientOperator
                .createPipelineSchedule(gitlabProjectId,
                        null,
                        appExternalConfigDTO,
                        pipelineSchedule);
        // 2. 如果有变量，创建变量
        if (!CollectionUtils.isEmpty(ciPipelineScheduleVO.getVariableVOList())) {
            AppExternalConfigDTO finalAppExternalConfigDTO = appExternalConfigDTO;
            ciPipelineScheduleVO.getVariableVOList().forEach(variable -> {
                Variable variable1 = new Variable();
                variable1.setKey(variable.getKey());
                variable1.setValue(variable.getValue());

                gitlabServiceClientOperator.createScheduleVariable(gitlabProjectId,
                        pipelineSchedules.getId(),
                        null,
                        finalAppExternalConfigDTO,
                        variable1);
            });
        }
        CiPipelineScheduleDTO ciPipelineScheduleDTO = ConvertUtils.convertObject(ciPipelineScheduleVO, CiPipelineScheduleDTO.class);
        ciPipelineScheduleDTO.setPipelineScheduleId(TypeUtil.objToLong(pipelineSchedules.getId()));

        MapperUtil.resultJudgedInsertSelective(ciPipelineScheduleMapper, ciPipelineScheduleDTO, "error.save.pipeline.schedule.failed");
        if (!CollectionUtils.isEmpty(ciPipelineScheduleVO.getVariableVOList())) {
            ciPipelineScheduleVO.getVariableVOList().forEach(variable -> {
                CiScheduleVariableDTO ciScheduleVariableDTO = ConvertUtils.convertObject(variable, CiScheduleVariableDTO.class);
                ciScheduleVariableDTO.setCiPipelineScheduleId(ciPipelineScheduleDTO.getId());
                ciScheduleVariableDTO.setPipelineScheduleId(TypeUtil.objToLong(pipelineSchedules.getId()));
                ciScheduleVariableService.baseCreate(ciScheduleVariableDTO);
            });
        }
    }

    /**
     * 计算cron表达式
     * @param ciPipelineScheduleVO
     * @return
     */
    private String calculateCron(CiPipelineScheduleVO ciPipelineScheduleVO) {
        String cronTemplate = "%s %s * * %s";
        String minute = "";
        String hour = "";
        if (CiPipelineScheduleTriggerTypeEnum.PERIOD.value().equals(ciPipelineScheduleVO.getTriggerType())) {
            if (ciPipelineScheduleVO.getPeriod() >= 60) {
                minute = "0";
                hour = ciPipelineScheduleVO.getStartHourOfDay() + "-" + ciPipelineScheduleVO.getEndHourOfDay() + "/" + (ciPipelineScheduleVO.getPeriod() / 60);
            } else {
                minute = "0-59/" + ciPipelineScheduleVO.getPeriod();
                hour = ciPipelineScheduleVO.getStartHourOfDay() + "-" + ciPipelineScheduleVO.getEndHourOfDay();
            }
        } else if (CiPipelineScheduleTriggerTypeEnum.SINGLE.value().equals(ciPipelineScheduleVO.getTriggerType())) {
            String[] split = ciPipelineScheduleVO.getExecuteTime().split(":");
            minute = split[1];
            hour = split[0];
        } else {
            throw new CommonException("error.trigger.type.invalid");
        }


        String week = ciPipelineScheduleVO.getWeekNumber();
        return String.format(cronTemplate, minute, hour, week);
    }

    private void validate(CiPipelineScheduleVO ciPipelineScheduleVO) {
        if (CiPipelineScheduleTriggerTypeEnum.PERIOD.value().equals(ciPipelineScheduleVO.getTriggerType())) {
            if (ciPipelineScheduleVO.getStartHourOfDay() == null) {
                throw new CommonException("error.start.hour.of.day.is.null");
            }
            if (ciPipelineScheduleVO.getEndHourOfDay() == null) {
                throw new CommonException("error.end.hour.of.day.is.null");
            }
            if (ciPipelineScheduleVO.getPeriod() == null) {
                throw new CommonException("error.period.is.null");
            }
        } else if (CiPipelineScheduleTriggerTypeEnum.SINGLE.value().equals(ciPipelineScheduleVO.getTriggerType())) {
            if (ciPipelineScheduleVO.getExecuteTime() == null) {
                throw new CommonException("error.executeTime.is.null");
            }
        } else {
            throw new CommonException("error.trigger.type.invalid");
        }
    }
}

