package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
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
import io.choerodon.devops.api.vo.CiScheduleVariableVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.PipelineSchedule;
import io.choerodon.devops.infra.dto.gitlab.Variable;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.mapper.CiPipelineScheduleMapper;
import io.choerodon.devops.infra.util.*;

/**
 * devops_ci_pipeline_schedule(CiPipelineSchedule)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:00:27
 */
@Service
public class CiPipelineScheduleServiceImpl implements CiPipelineScheduleService {

    private static final String DEVOPS_SAVE_PIPELINE_SCHEDULE_FAILED = "devops.save.pipeline.schedule.failed";
    private static final String DEVOPS_CI_PIPELINE_SCHEDULE_NOT_FOUND = "devops.ci.pipeline.schedule.not.found";

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
    @Autowired
    private UserAttrService userAttrService;


    @Override
    @Transactional
    public CiPipelineScheduleDTO create(CiPipelineScheduleVO ciPipelineScheduleVO) {
        ScheduleUtil.validate(ciPipelineScheduleVO);

        Long appServiceId = ciPipelineScheduleVO.getAppServiceId();
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        int gitlabProjectId = TypeUtil.objToInt(appServiceDTO.getGitlabProjectId());

        String cron = ScheduleUtil.calculateGitlabCiCron(ciPipelineScheduleVO);

        PipelineSchedule pipelineSchedule = new PipelineSchedule();
        pipelineSchedule.setCron(cron);
        pipelineSchedule.setRef(ciPipelineScheduleVO.getRef());
        pipelineSchedule.setDescription(ciPipelineScheduleVO.getName());
        pipelineSchedule.setCronTimezone("Asia/Shanghai");

        PipelineSchedule pipelineSchedules;
        AppExternalConfigDTO appExternalConfigDTO = null;
        if (appServiceDTO.getExternalConfigId() != null) {
            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        }
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId());
        Integer gitlabUserId = null;
        if (userAttrDTO != null) {
            gitlabUserId = userAttrDTO.getGitlabUserId() == null ? null : TypeUtil.objToInt(userAttrDTO.getGitlabUserId());
        }
        // 1. 创建定时计划
        pipelineSchedules = gitlabServiceClientOperator
                .createPipelineSchedule(gitlabProjectId,
                        gitlabUserId,
                        appExternalConfigDTO,
                        pipelineSchedule);
        // 2. 如果有变量，创建变量
        if (!CollectionUtils.isEmpty(ciPipelineScheduleVO.getVariableVOList())) {
            AppExternalConfigDTO finalAppExternalConfigDTO = appExternalConfigDTO;
            ciPipelineScheduleVO.getVariableVOList().forEach(variable -> {
                Variable variable1 = new Variable();
                variable1.setKey(variable.getVariableKey());
                variable1.setValue(variable.getVariableValue());

                gitlabServiceClientOperator.createScheduleVariable(gitlabProjectId,
                        pipelineSchedules.getId(),
                        null,
                        finalAppExternalConfigDTO,
                        variable1);
            });
        }
        CiPipelineScheduleDTO ciPipelineScheduleDTO = ConvertUtils.convertObject(ciPipelineScheduleVO, CiPipelineScheduleDTO.class);
        ciPipelineScheduleDTO.setPipelineScheduleId(TypeUtil.objToLong(pipelineSchedules.getId()));

        MapperUtil.resultJudgedInsertSelective(ciPipelineScheduleMapper, ciPipelineScheduleDTO, DEVOPS_SAVE_PIPELINE_SCHEDULE_FAILED);
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

        List<PipelineSchedule> pipelineSchedules = null;
        try {
            pipelineSchedules = gitlabServiceClientOperator.listPipelineSchedules(TypeUtil.objToInt(appServiceDTO.getGitlabProjectId()),
                    null,
                    appExternalConfigDTO);
        } catch (Exception e) {
            pipelineSchedules = new ArrayList<>();
        }

        Map<Integer, PipelineSchedule> pipelineScheduleMap = pipelineSchedules.stream().collect(Collectors.toMap(PipelineSchedule::getId, Function.identity()));
        ciPipelineScheduleVOS.forEach(v -> {
            PipelineSchedule pipelineSchedule = pipelineScheduleMap.get(TypeUtil.objToInt(v.getPipelineScheduleId()));
            if (pipelineSchedule != null) {
                v.setNextRunAt(pipelineSchedule.getNextRunAt());
                v.setActive(pipelineSchedule.getActive());
            }
            List<CiScheduleVariableDTO> ciScheduleVariableDTOS = ciScheduleVariableService.listByScheduleId(v.getId());
            if (!CollectionUtils.isEmpty(ciScheduleVariableDTOS)) {
                List<CiScheduleVariableVO> ciScheduleVariableVOS = ConvertUtils.convertList(ciScheduleVariableDTOS, CiScheduleVariableVO.class);
                v.setVariableVOList(ciScheduleVariableVOS);
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
        PipelineSchedule pipelineSchedule = gitlabServiceClientOperator.queryPipelineSchedule(TypeUtil.objToInt(appServiceDTO.getGitlabProjectId()),
                null,
                TypeUtil.objToInt(ciPipelineScheduleDTO.getPipelineScheduleId()),
                appExternalConfigDTO);
        if (pipelineSchedule != null && pipelineSchedule.getId() != null) {
            gitlabServiceClientOperator.deletePipelineSchedule(TypeUtil.objToInt(appServiceDTO.getGitlabProjectId()),
                    null,
                    TypeUtil.objToInt(ciPipelineScheduleDTO.getPipelineScheduleId()),
                    appExternalConfigDTO);
        }

        ciPipelineScheduleMapper.deleteByPrimaryKey(id);
        ciScheduleVariableService.deleteByPipelineScheduleId(id);

    }

    @Override
    @Transactional
    public void update(Long id, CiPipelineScheduleVO ciPipelineScheduleVO) {
        ScheduleUtil.validate(ciPipelineScheduleVO);

        CiPipelineScheduleDTO ciPipelineScheduleDTO = ciPipelineScheduleMapper.selectByPrimaryKey(id);
        if (ciPipelineScheduleDTO == null) {
            throw new CommonException(DEVOPS_CI_PIPELINE_SCHEDULE_NOT_FOUND);
        }

        Long appServiceId = ciPipelineScheduleDTO.getAppServiceId();
        AppServiceDTO appServiceDTO = appServiceService.baseQuery(appServiceId);
        int gitlabProjectId = TypeUtil.objToInt(appServiceDTO.getGitlabProjectId());

        String cron = ScheduleUtil.calculateGitlabCiCron(ciPipelineScheduleVO);


        PipelineSchedule pipelineSchedules = null;
        AppExternalConfigDTO appExternalConfigDTO = null;
        if (appServiceDTO.getExternalConfigId() != null) {
            appExternalConfigDTO = appExternalConfigService.baseQueryWithPassword(appServiceDTO.getExternalConfigId());
        }
        pipelineSchedules = gitlabServiceClientOperator.queryPipelineSchedule(TypeUtil.objToInt(appServiceDTO.getGitlabProjectId()),
                null,
                TypeUtil.objToInt(ciPipelineScheduleDTO.getPipelineScheduleId()),
                appExternalConfigDTO);
        pipelineSchedules.setCron(cron);
        pipelineSchedules.setRef(ciPipelineScheduleVO.getRef());
        pipelineSchedules.setDescription(ciPipelineScheduleVO.getName());
        pipelineSchedules.setCronTimezone("Asia/Shanghai");
        if (pipelineSchedules == null) {
            // 不存在则创建
            // 1. 创建定时计划
            pipelineSchedules = gitlabServiceClientOperator
                    .createPipelineSchedule(gitlabProjectId,
                            null,
                            appExternalConfigDTO,
                            pipelineSchedules);
            // 2. 如果有变量，创建变量
            if (!CollectionUtils.isEmpty(ciPipelineScheduleVO.getVariableVOList())) {
                for (CiScheduleVariableVO variable : ciPipelineScheduleVO.getVariableVOList()) {
                    Variable variable1 = new Variable();
                    variable1.setKey(variable.getVariableKey());
                    variable1.setValue(variable.getVariableValue());

                    gitlabServiceClientOperator.createScheduleVariable(gitlabProjectId,
                            pipelineSchedules.getId(),
                            null,
                            appExternalConfigDTO,
                            variable1);
                }
            }
        } else {
            // 更新执行计划
            gitlabServiceClientOperator.updatePipelineSchedule(TypeUtil.objToInt(appServiceDTO.getGitlabProjectId()),
                    null,
                    TypeUtil.objToInt(ciPipelineScheduleDTO.getPipelineScheduleId()),
                    appExternalConfigDTO,
                    pipelineSchedules);
            // 更新变量
            List<Variable> variables = pipelineSchedules.getVariables();
            Map<String, String> existVarMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(variables)) {
                existVarMap = variables.stream().collect(Collectors.toMap(Variable::getKey, Variable::getValue));
            }

            List<CiScheduleVariableVO> variableVOList = ciPipelineScheduleVO.getVariableVOList();
            Map<String, String> newVarMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(variableVOList)) {
                newVarMap = variableVOList.stream().collect(Collectors.toMap(CiScheduleVariableVO::getVariableKey, CiScheduleVariableVO::getVariableValue));
            }

            List<Variable> addVarList = new ArrayList<>();
            List<Variable> delVarList = new ArrayList<>();
            List<Variable> editVarList = new ArrayList<>();

            // 1. 新增
            for (CiScheduleVariableVO ciScheduleVariableVO : variableVOList) {
                if (!existVarMap.containsKey(ciScheduleVariableVO.getVariableKey())) {
                    Variable variable = new Variable();
                    variable.setKey(ciScheduleVariableVO.getVariableKey());
                    variable.setValue(ciScheduleVariableVO.getVariableValue());
                    addVarList.add(variable);
                }
            }
            // 2. 删除
            for (Variable variable : variables) {
                if (!newVarMap.containsKey(variable.getKey())) {
                    delVarList.add(variable);
                }
            }
            // 3. 更新
            for (CiScheduleVariableVO ciScheduleVariableVO : variableVOList) {
                if (existVarMap.containsKey(ciScheduleVariableVO.getVariableKey())) {
                    Variable variable = new Variable();
                    variable.setKey(ciScheduleVariableVO.getVariableKey());
                    variable.setValue(ciScheduleVariableVO.getVariableValue());
                    editVarList.add(variable);
                }
            }
            if (!CollectionUtils.isEmpty(addVarList)) {
                for (Variable variable : addVarList) {
                    gitlabServiceClientOperator.createScheduleVariable(gitlabProjectId,
                            pipelineSchedules.getId(),
                            null,
                            appExternalConfigDTO,
                            variable);
                }
            }
            if (!CollectionUtils.isEmpty(delVarList)) {
                for (Variable variable : delVarList) {
                    gitlabServiceClientOperator.deleteScheduleVariable(gitlabProjectId,
                            pipelineSchedules.getId(),
                            null,
                            appExternalConfigDTO,
                            variable);
                }
            }
            if (!CollectionUtils.isEmpty(editVarList)) {
                for (Variable variable : editVarList) {
                    gitlabServiceClientOperator.editScheduleVariable(gitlabProjectId,
                            pipelineSchedules.getId(),
                            null,
                            appExternalConfigDTO,
                            variable);
                }
            }
        }

        CiPipelineScheduleDTO ciPipelineScheduleDTO1 = ConvertUtils.convertObject(ciPipelineScheduleVO, CiPipelineScheduleDTO.class);
        ciPipelineScheduleDTO1.setId(id);
        ciPipelineScheduleDTO1.setPipelineScheduleId(TypeUtil.objToLong(pipelineSchedules.getId()));
        ciPipelineScheduleDTO1.setAppServiceId(null);

        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(ciPipelineScheduleMapper, ciPipelineScheduleDTO1, DEVOPS_SAVE_PIPELINE_SCHEDULE_FAILED);

        // 先删除再新建
        ciScheduleVariableService.deleteByPipelineScheduleId(ciPipelineScheduleDTO.getId());
        if (!CollectionUtils.isEmpty(ciPipelineScheduleVO.getVariableVOList())) {
            for (CiScheduleVariableVO variable : ciPipelineScheduleVO.getVariableVOList()) {
                CiScheduleVariableDTO ciScheduleVariableDTO = ConvertUtils.convertObject(variable, CiScheduleVariableDTO.class);
                ciScheduleVariableDTO.setCiPipelineScheduleId(ciPipelineScheduleDTO.getId());
                ciScheduleVariableDTO.setPipelineScheduleId(TypeUtil.objToLong(pipelineSchedules.getId()));
                ciScheduleVariableService.baseCreate(ciScheduleVariableDTO);
            }
        }
    }
}

