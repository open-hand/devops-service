package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.pipeline.ConfigFileRelVO;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplatePipelineVO;
import io.choerodon.devops.api.vo.template.CiTemplateStageVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.devops.infra.util.UserSyncErrorBuilder;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/12/3
 */
@Service
public class CiPipelineTemplateBusServiceImpl implements CiPipelineTemplateBusService {

    private static final String DEFAULT_VERSION_NAME_RULE = "${C7N_COMMIT_TIME}-${C7N_BRANCH}";

    @Autowired
    private CiPipelineTemplateBusMapper ciPipelineTemplateBusMapper;

    @Autowired
    private CiTemplateStageBusMapper ciTemplateStageBusMapper;

    @Autowired
    private CiTemplateJobBusMapper ciTemplateJobBusMapper;

    @Autowired
    private CiTemplateStepBusMapper ciTemplateStepBusMapper;

    @Autowired
    private CiTemplateCategoryBusMapper ciTemplateCategoryBusMapper;

    @Autowired
    private CiTemplateStageJobRelMapper ciTemplateStageJobRelMapper;

    @Autowired
    private CiTemplateStageJobRelBusMapper ciTemplateStageJobRelBusMapper;

    @Autowired
    private CiTemplateVariableBusMapper ciTemplateVariableBusMapper;

    @Autowired
    private UserSyncErrorBuilder.PipelineTemplateUtils pipelineTemplateUtils;

    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;

    @Autowired
    private CiTemplateJobBusService ciTemplateJobBusService;

    @Autowired
    private CiTemplateStepBusService ciTemplateStepBusService;

    @Autowired
    private CiTemplateJobStepRelBusMapper ciTemplateJobStepRelBusMapper;

    @Autowired
    private CiTemplateStageBusService ciTemplateStageBusService;

    @Autowired
    private CiTemplateVariableBusService ciTemplateVariableBusService;

    @Autowired
    private CiTplJobConfigFileRelService ciTplJobConfigFileRelService;


    @Override
    public Page<CiTemplatePipelineVO> pagePipelineTemplate(Long sourceId, String sourceType,
                                                           PageRequest pageRequest, String name, Long categoryId,
                                                           Boolean builtIn, Boolean enable, String params) {
        Page<CiTemplatePipelineVO> pipelineTemplateVOS = queryBaseCiTemplatePipelinePage(sourceId, sourceType,
                pageRequest, name, categoryId, builtIn, enable, params);
        if (CollectionUtils.isEmpty(pipelineTemplateVOS.getContent())) return pipelineTemplateVOS;
        UserDTOFillUtil.fillUserInfo(pipelineTemplateVOS.getContent(), Constant.CREATED_BY, Constant.CREATOR);
        setUserEmpty(pipelineTemplateVOS);
        return pipelineTemplateVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidPipelineTemplate(Long sourceId, Long ciPipelineTemplateId) {
        CiTemplatePipelineDTO pipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciPipelineTemplateId);
        checkPipelineTemplate(pipelineTemplateDTO);
        pipelineTemplateDTO.setEnable(Boolean.FALSE);
        ciPipelineTemplateBusMapper.updateByPrimaryKey(pipelineTemplateDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enablePipelineTemplate(Long sourceId, Long ciPipelineTemplateId) {
        CiTemplatePipelineDTO pipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciPipelineTemplateId);
        checkPipelineTemplate(pipelineTemplateDTO);
        pipelineTemplateDTO.setEnable(Boolean.TRUE);
        ciPipelineTemplateBusMapper.updateByPrimaryKey(pipelineTemplateDTO);
    }


    private static void additionalCheck(CiTemplatePipelineVO devopsPipelineTemplateVO) {
        if (devopsPipelineTemplateVO
                .getTemplateStageVOS()
                .stream()
                .flatMap(stage -> stage.getCiTemplateJobVOList().stream())
                .noneMatch(job -> Boolean.TRUE.equals(job.getEnabled()))) {
            throw new CommonException("devops.ci.job.is.all.disable");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplatePipelineVO createPipelineTemplate(Long sourceId, String sourceType,
                                                       CiTemplatePipelineVO devopsPipelineTemplateVO) {
        AssertUtils.isTrue(checkPipelineTemplateName(sourceId, devopsPipelineTemplateVO.getName(), null),
                "error.pipeline.template.name.exist");
        checkPipelineCategory(devopsPipelineTemplateVO);
        checkStageName(devopsPipelineTemplateVO);
        pipelineTemplateUtils.checkAccess(sourceId, sourceType);
        // 流水线任务至少有一个是启用的
        additionalCheck(devopsPipelineTemplateVO);


        //1.插入流水线模板
        CiTemplatePipelineDTO ciTemplatePipelineDTO = baseInsertPipelineTemplate(sourceId, devopsPipelineTemplateVO);

        List<CiTemplateStageVO> templateStageVOS = devopsPipelineTemplateVO.getTemplateStageVOS();
        AtomicReference<Long> sequence = new AtomicReference<>(0L);
        templateStageVOS.forEach(ciTemplateStageVO -> {
            //2.插入stage数据
            CiTemplateStageDTO ciTemplateStageDTO = baseInsertStage(ciTemplatePipelineDTO, sequence, ciTemplateStageVO);
            List<CiTemplateJobVO> ciTemplateJobVOList = ciTemplateStageVO.getCiTemplateJobVOList();
            if (CollectionUtils.isEmpty(ciTemplateJobVOList)) return;
            // 保存为模板的还要插入 不可见的job 与 step
            ciTemplateJobBusService.createNonVisibilityJob(sourceId, sourceType, ciTemplateJobVOList);
            // 插入Stage与Job的关系
            baseInsertStageJobRel(ciTemplateStageDTO, ciTemplateJobVOList);
        });
        //插入变量的数据
        baseInsertVariable(devopsPipelineTemplateVO, ciTemplatePipelineDTO);
        return ConvertUtils.convertObject(ciTemplatePipelineDTO, CiTemplatePipelineVO.class);
    }


    @Override
    public CiTemplatePipelineVO queryPipelineTemplateById(Long sourceId, Long ciPipelineTemplateId) {
        //1. 查询流水线模板
        CiTemplatePipelineVO ciTemplatePipelineVO = queryBaseCiPipelineTemplate(ciPipelineTemplateId);
        UserSyncErrorBuilder.PipelineTemplateUtils.threadLocal.set(ciTemplatePipelineVO);
        //2.查询模板下面的阶段
        List<CiTemplateStageVO> ciTemplateStageVOS = queryBaseCiTemplateStage(ciPipelineTemplateId);
        ciTemplateStageVOS.forEach(ciTemplateStageVO -> {
            handStage(ciTemplateStageVO);
        });
        ciTemplatePipelineVO.setTemplateStageVOS(ciTemplateStageVOS);
        return ciTemplatePipelineVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplatePipelineVO updatePipelineTemplate(Long sourceId, String sourceType,
                                                       CiTemplatePipelineVO devopsPipelineTemplateVO) {
        AssertUtils.isTrue(checkPipelineTemplateName(sourceId, devopsPipelineTemplateVO.getName(),
                devopsPipelineTemplateVO.getId()),
                "error.pipeline.template.name.exist");
        checkPipelineCategory(devopsPipelineTemplateVO);
        checkStageName(devopsPipelineTemplateVO);
        pipelineTemplateUtils.checkAccess(sourceId, sourceType);
        additionalCheck(devopsPipelineTemplateVO);
        CiTemplatePipelineDTO pipelineTemplateDTO
                = ciPipelineTemplateBusMapper.selectByPrimaryKey(devopsPipelineTemplateVO.getId());
        if (pipelineTemplateDTO == null) {
            return new CiTemplatePipelineVO();
        }
        CiTemplateStageDTO record = new CiTemplateStageDTO();
        record.setPipelineTemplateId(pipelineTemplateDTO.getId());
        List<CiTemplateStageDTO> ciTemplateStageDTOS = ciTemplateStageBusMapper.select(record);

        AssertUtils.isTrue(!pipelineTemplateDTO.getBuiltIn(), "error.pipeline.built.in");

        List<CiTemplateStageVO> templateStageVOS = devopsPipelineTemplateVO.getTemplateStageVOS();
        //处理步骤模板的字段
        handNonVisibilityStep(templateStageVOS);
        //删除旧的阶段(包含阶段关系，任务，任务关系，任务配置，步骤关系，步骤配置)
        ciTemplateStageDTOS.forEach(templateStageDTO -> {
            ciTemplateStageBusService.deleteStageById(sourceId, templateStageDTO.getId());
        });

        //插入不可见的的job step
        insertNonVisibilityJob(sourceId, templateStageVOS);
        //插入新的阶段  阶段与job的关系
        insertStageAndJobRel(pipelineTemplateDTO, templateStageVOS);
        //更新变量
        updateVariable(devopsPipelineTemplateVO, pipelineTemplateDTO);

        BeanUtils.copyProperties(devopsPipelineTemplateVO, pipelineTemplateDTO);
        pipelineTemplateDTO.setSourceId(sourceId);
        ciPipelineTemplateBusMapper.updateByPrimaryKey(pipelineTemplateDTO);
        return devopsPipelineTemplateVO;
    }

    private void handNonVisibilityStep(List<CiTemplateStageVO> ciTemplateStageVOS) {
        if (CollectionUtils.isEmpty(ciTemplateStageVOS)) {
            return;
        }
        ciTemplateStageVOS.forEach(ciTemplateStageVO -> {
            List<CiTemplateJobVO> ciTemplateJobVOList = ciTemplateStageVO.getCiTemplateJobVOList();
            if (CollectionUtils.isEmpty(ciTemplateJobVOList)) {
                return;
            }
            ciTemplateJobVOList.forEach(ciTemplateJobVO -> {
                List<CiTemplateStepVO> devopsCiStepVOList = ciTemplateJobVO.getDevopsCiStepVOList();
                if (CollectionUtils.isEmpty(devopsCiStepVOList)) {
                    return;
                }
                devopsCiStepVOList.forEach(ciTemplateStepVO -> {
                    CiTemplateStepDTO ciTemplateStepDTO = ciTemplateStepBusMapper.selectByPrimaryKey(ciTemplateStepVO.getId());
                    if (Objects.isNull(ciTemplateStepDTO)) {
                        return;
                    }
                    ciTemplateStepVO.setCategoryId(0L);
                    ciTemplateStepVO.setVisibility(ciTemplateStepDTO.getVisibility());
                });
            });
        });

    }

    private void insertNonVisibilityJob(Long sourceId, List<CiTemplateStageVO> templateStageVOS) {
        templateStageVOS.forEach(ciTemplateStageVO -> {
            List<CiTemplateJobVO> ciTemplateJobVOList = ciTemplateStageVO.getCiTemplateJobVOList();
            if (CollectionUtils.isEmpty(ciTemplateJobVOList)) return;
            // 保存为模板的还要插入 不可见的job 与 step
            ciTemplateJobBusService.createNonVisibilityJob(sourceId, ResourceLevel.PROJECT.value(), ciTemplateJobVOList);
        });
    }

    private void deleteNonVisibilityJob(List<CiTemplateJobDTO> templateStageVOS) {
        templateStageVOS.stream().filter(ciTemplateJobDTO -> !ciTemplateJobDTO.getVisibility())
                .forEach(ciTemplateJobDTO -> {
                    CiTemplateJobStepRelDTO ciTemplateJobStepRelDTO = new CiTemplateJobStepRelDTO();
                    ciTemplateJobStepRelDTO.setCiTemplateJobId(ciTemplateJobDTO.getId());
                    List<CiTemplateJobStepRelDTO> ciTemplateJobStepRelDTOS
                            = ciTemplateJobStepRelBusMapper.select(ciTemplateJobStepRelDTO);
                    if (!CollectionUtils.isEmpty(ciTemplateJobStepRelDTOS)) {
                        Set<Long> stepIds = ciTemplateJobStepRelDTOS.stream()
                                .map(CiTemplateJobStepRelDTO::getCiTemplateStepId).collect(Collectors.toSet());
                        ciTemplateStepBusMapper.deleteByIds(stepIds);
                    }
                    //删除Steps
                    ciTemplateJobStepRelBusMapper.deleteByJobId(ciTemplateJobDTO.getId());
                    //删除job
                    ciTemplateJobBusMapper.deleteByPrimaryKey(ciTemplateJobDTO.getId());
                    //删除CD配置
                    ciTemplateJobBusService.deleteTemplateJobConfig(ciTemplateJobDTO);
                });
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePipelineTemplate(Long sourceId, Long ciTemplatePipelineId) {
        CiTemplatePipelineDTO pipelineTemplateDTO = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciTemplatePipelineId);
        if (pipelineTemplateDTO == null) {
            return;
        }
        AssertUtils.isTrue(!pipelineTemplateDTO.getBuiltIn(), "error.pipeline.built.in");
        //查询阶段
        CiTemplateStageDTO record = new CiTemplateStageDTO();
        record.setPipelineTemplateId(pipelineTemplateDTO.getId());
        List<CiTemplateStageDTO> ciTemplateStageDTOS = ciTemplateStageBusMapper.select(record);

        //删除stage
        ciTemplateStageDTOS.forEach(ciTemplateStageDTO -> {
            ciTemplateStageBusService.deleteStageById(sourceId, ciTemplateStageDTO.getId());
        });
        //删除变量
        ciTemplateVariableBusService.deleteByTemplatePipelineId(ciTemplatePipelineId);
        //删除流水线模板
        ciPipelineTemplateBusMapper.deleteByPrimaryKey(ciTemplatePipelineId);
    }

    @Override
    public Boolean checkPipelineTemplateName(Long sourceId, String name, Long ciPipelineTemplateId) {
        return ciPipelineTemplateBusMapper.checkPipelineName(sourceId, name, ciPipelineTemplateId) == null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplatePipelineVO savePipelineTemplate(Long sourceId, CiTemplatePipelineVO ciTemplatePipelineVO) {
        /**
         *  此处生成流水线模板，对于后端生成的任务模板、步骤模板，需要做屏蔽了
         */
        // 保存为流水线模板前的预处理
        List<CiTemplateStageVO> templateStageVOS = ciTemplatePipelineVO.getTemplateStageVOS();
        if (CollectionUtils.isEmpty(templateStageVOS)) {
            return new CiTemplatePipelineVO();
        }
        ciTemplatePipelineVO.setSourceType(ResourceLevel.PROJECT.value());
        ciTemplatePipelineVO.setSourceId(sourceId);
        ciTemplatePipelineVO.setEnable(true);
        ciTemplatePipelineVO.setBuiltIn(false);
        ciTemplatePipelineVO.setId(null);
        templateStageVOS.forEach(ciTemplateStageVO -> {
            handNonVisibilityStage(sourceId, ciTemplateStageVO);
        });
        CiTemplatePipelineVO pipelineTemplate = createPipelineTemplate(sourceId,
                ResourceLevel.PROJECT.value(), ciTemplatePipelineVO);
        return pipelineTemplate;
    }


    private void checkPipelineTemplate(CiTemplatePipelineDTO pipelineTemplateDTO) {
        AssertUtils.notNull(pipelineTemplateDTO, "error.pipeline.template.is.null");
    }

    /**
     * 流水线阶段名称在流水线内唯一
     *
     * @param devopsPipelineTemplateVO
     */
    private void checkStageName(CiTemplatePipelineVO devopsPipelineTemplateVO) {
        List<CiTemplateStageVO> templateStageVOS = devopsPipelineTemplateVO.getTemplateStageVOS();
        if (CollectionUtils.isEmpty(templateStageVOS)) {
            return;
        }
        Set<String> stageNames = templateStageVOS.stream().map(CiTemplateStageVO::getName).collect(Collectors.toSet());
        if (stageNames.size() < templateStageVOS.size()) {
            throw new CommonException("error.pipeline.template.stage.name.exist");
        }
    }

    /**
     * 流水线分类合法有效
     *
     * @param devopsPipelineTemplateVO
     */
    private void checkPipelineCategory(CiTemplatePipelineVO devopsPipelineTemplateVO) {
        CiTemplateCategoryDTO ciTemplateCategoryDTO
                = ciTemplateCategoryBusMapper.selectByPrimaryKey(devopsPipelineTemplateVO.getCiTemplateCategoryId());
        AssertUtils.notNull(ciTemplateCategoryDTO, "error.ci.template.category.null");
    }

    private void fillStepConfigInfo(List<CiTemplateStepVO> ciTemplateStepVOS) {
        ciTemplateStepVOS.forEach(ciTemplateStepVO -> {
            // 添加步骤关联的配置信息
            AbstractDevopsCiStepHandler stepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
            stepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
        });
    }

    private List<CiTemplateStepVO> queryBaseCiTemplateStep(CiTemplateJobVO ciTemplateJobVO) {
        List<CiTemplateStepDTO> ciTemplateStepDTOS = ciTemplateStepBusMapper.queryStepTemplateByJobId(ciTemplateJobVO.getId());
        if (CollectionUtils.isEmpty(ciTemplateStepDTOS)) {
            return Collections.emptyList();
        }
        List<CiTemplateStepVO> ciTemplateStepVOS = ConvertUtils.convertList(ciTemplateStepDTOS, CiTemplateStepVO.class);
        return ciTemplateStepVOS;
    }

    private List<CiTemplateJobVO> queryBaseCiTemplateJob(CiTemplateStageVO ciTemplateStageVO) {
        //通过阶段id 查找JOB
        List<CiTemplateStageJobRelDTO> ciTemplateStageJobRelDTOS = ciTemplateStageJobRelBusMapper.listByStageId(ciTemplateStageVO.getId());
        if (CollectionUtils.isEmpty(ciTemplateStageJobRelDTOS)) {
            return Collections.emptyList();
        }
        Set<Long> jobIds = ciTemplateStageJobRelDTOS.stream().map(CiTemplateStageJobRelDTO::getCiTemplateJobId).collect(Collectors.toSet());
        List<CiTemplateJobVO> ciTemplateJobDTOS = ciTemplateJobBusMapper.listByIds(jobIds);
        if (CollectionUtils.isEmpty(ciTemplateJobDTOS)) {
            return Collections.emptyList();
        }
        Map<Long, CiTemplateJobVO> ciTemplateJobDTOSMappedById = ciTemplateJobDTOS.stream().collect(Collectors.toMap(CiTemplateJobVO::getId, Function.identity()));
        return ciTemplateStageJobRelDTOS.stream().sorted(Comparator.comparing(CiTemplateStageJobRelDTO::getSequence))
                .filter(ciTemplateStageJobRelDTO -> ciTemplateJobDTOSMappedById.get(ciTemplateStageJobRelDTO.getCiTemplateJobId()) != null)
                .map(ciTemplateStageJobRelDTO -> {
                    CiTemplateJobVO ciTemplateJobVO = ciTemplateJobDTOSMappedById.get(ciTemplateStageJobRelDTO.getCiTemplateJobId());
                    ciTemplateJobVO.setEnabled(ciTemplateStageJobRelDTO.getEnabled());
                    return ciTemplateJobVO;
                }).collect(Collectors.toList());
    }

    private List<CiTemplateStageVO> queryBaseCiTemplateStage(Long ciPipelineTemplateId) {
        CiTemplateStageDTO record = new CiTemplateStageDTO();
        record.setPipelineTemplateId(ciPipelineTemplateId);
        List<CiTemplateStageDTO> ciTemplateStageDTOS = ciTemplateStageBusMapper.select(record);
        return ConvertUtils.convertList(ciTemplateStageDTOS, CiTemplateStageVO.class);
    }

    private CiTemplatePipelineVO queryBaseCiPipelineTemplate(Long ciPipelineTemplateId) {
        CiTemplatePipelineDTO ciPipelineTemplateDTO
                = ciPipelineTemplateBusMapper.selectByPrimaryKey(ciPipelineTemplateId);
        AssertUtils.notNull(ciPipelineTemplateDTO, "error.pipeline.template.is.null");
        CiTemplatePipelineVO ciTemplatePipelineVO
                = ConvertUtils.convertObject(ciPipelineTemplateDTO, CiTemplatePipelineVO.class);
        if (ObjectUtils.isEmpty(ciTemplatePipelineVO.getVersionName())
                || DEFAULT_VERSION_NAME_RULE.equals(ciTemplatePipelineVO.getVersionName())) {
            ciTemplatePipelineVO.setVersionStrategy(false);
        }
        return ciTemplatePipelineVO;
    }

    private void handStage(CiTemplateStageVO ciTemplateStageVO) {
        //查询阶段下的job
        List<CiTemplateJobVO> ciTemplateJobVOS = queryBaseCiTemplateJob(ciTemplateStageVO);
        ciTemplateJobVOS.forEach(ciTemplateJobVO -> {
            //根据job step
            List<CiTemplateStepVO> ciTemplateStepVOS = queryBaseCiTemplateStep(ciTemplateJobVO);
            //根据步骤模板的类型填充配置信息
            fillStepConfigInfo(ciTemplateStepVOS);
            //填充CD Job的配置信息
            //根据步骤的类型填充CD的配置信息
            ciTemplateJobBusService.fillCdJobConfig(ciTemplateJobVO);
            List<CiTplJobConfigFileRelDTO> ciTplJobConfigFileRelDTOS = ciTplJobConfigFileRelService.listByJobId(ciTemplateJobVO.getId());
            if (!CollectionUtils.isEmpty(ciTplJobConfigFileRelDTOS)) {
                ciTemplateJobVO.setConfigFileRelList(ConvertUtils.convertList(ciTplJobConfigFileRelDTOS, ConfigFileRelVO.class));
            }

            ciTemplateJobVO.setDevopsCiStepVOList(ciTemplateStepVOS);
        });
        ciTemplateStageVO.setCiTemplateJobVOList(ciTemplateJobVOS);
    }


    private static void setUserEmpty(Page<CiTemplatePipelineVO> pipelineTemplateVOS) {
        pipelineTemplateVOS.getContent().forEach(ciTemplateJobGroupVO -> {
            if (ciTemplateJobGroupVO.getBuiltIn()) {
                ciTemplateJobGroupVO.setCreator(null);
            }
        });
    }

    private Page<CiTemplatePipelineVO> queryBaseCiTemplatePipelinePage(Long sourceId, String sourceType,
                                                                       PageRequest pageRequest, String name,
                                                                       Long categoryId, Boolean builtIn,
                                                                       Boolean enable, String params) {
        Page<CiTemplatePipelineVO> pipelineTemplateVOS = PageHelper.doPage(pageRequest,
                () -> ciPipelineTemplateBusMapper
                        .queryDevopsPipelineTemplateByParams(sourceId, sourceType,
                                pipelineTemplateUtils.getOrganizationId(sourceId, sourceType), name,
                                null, categoryId, builtIn, enable, params));
        //处理组内排序，组内按照时间倒序
        List<CiTemplatePipelineVO> templatePipelineVOS = pipelineTemplateVOS.getContent();
        List<CiTemplatePipelineVO> reTemplatePipelineVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(templatePipelineVOS)) {
            return pipelineTemplateVOS;
        }
        Map<String, List<CiTemplatePipelineVO>> stringListMap = templatePipelineVOS.stream()
                .collect(Collectors.groupingBy(CiTemplatePipelineVO::getSourceType));
        List<CiTemplatePipelineVO> siteTemplates = stringListMap.get(ResourceLevel.PROJECT.value());
        if (!CollectionUtils.isEmpty(siteTemplates)) {
            reTemplatePipelineVOS.addAll(getSortedTemplate(siteTemplates));
        }
        List<CiTemplatePipelineVO> organizationTemplates = stringListMap.get(ResourceLevel.ORGANIZATION.value());
        if (!CollectionUtils.isEmpty(organizationTemplates)) {
            reTemplatePipelineVOS.addAll(getSortedTemplate(organizationTemplates));
        }
        List<CiTemplatePipelineVO> projectTemplates = stringListMap.get(ResourceLevel.SITE.value());
        if (!CollectionUtils.isEmpty(projectTemplates)) {
            reTemplatePipelineVOS.addAll(getSortedTemplate(projectTemplates));
        }
        pipelineTemplateVOS.setContent(reTemplatePipelineVOS);
        return pipelineTemplateVOS;
    }

    private static List<CiTemplatePipelineVO> getSortedTemplate(List<CiTemplatePipelineVO> siteTemplates) {
        return siteTemplates.stream().sorted(Comparator.comparing(CiTemplatePipelineVO::getCreationDate).reversed())
                .collect(Collectors.toList());
    }


    private CiTemplatePipelineDTO baseInsertPipelineTemplate(Long sourceId, CiTemplatePipelineVO devopsPipelineTemplateVO) {
        CiTemplatePipelineDTO ciTemplatePipelineDTO = new CiTemplatePipelineDTO();
        BeanUtils.copyProperties(devopsPipelineTemplateVO, ciTemplatePipelineDTO);
        ciTemplatePipelineDTO.setSourceId(sourceId);
        ciPipelineTemplateBusMapper.insertSelective(ciTemplatePipelineDTO);
        return ciTemplatePipelineDTO;
    }

    private void baseInsertVariable(CiTemplatePipelineVO devopsPipelineTemplateVO,
                                    CiTemplatePipelineDTO ciTemplatePipelineDTO) {
        if (!CollectionUtils.isEmpty(devopsPipelineTemplateVO.getCiTemplateVariableVOS())) {
            devopsPipelineTemplateVO.getCiTemplateVariableVOS().forEach(ciTemplateVariableVO -> {
                if (ciTemplateVariableVO.getVariableKey() == null || ciTemplateVariableVO.getVariableKey() == null)
                    return;
                CiTemplateVariableDTO ciTemplateVariableDTO
                        = ConvertUtils.convertObject(ciTemplateVariableVO, CiTemplateVariableDTO.class);
                ciTemplateVariableDTO.setPipelineTemplateId(ciTemplatePipelineDTO.getId());
                ciTemplateVariableDTO.setId(null);
                ciTemplateVariableBusMapper.insert(ciTemplateVariableDTO);
            });
        }
    }

    private void baseInsertStageJobRel(CiTemplateStageDTO ciTemplateStageDTO, List<CiTemplateJobVO> ciTemplateJobVOList) {
        AtomicReference<Integer> jobSequence = new AtomicReference<>(0);
        ciTemplateJobVOList.forEach(ciTemplateJobVO -> {
            // 保存为模板的jobId为null
            if (ciTemplateJobVO.getId() == null || ciTemplateJobVO.getId() == 0L) {
                return;
            }
            //插入阶段与job的关联关系
            CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
            ciTemplateStageJobRelDTO.setCiTemplateJobId(ciTemplateJobVO.getId());
            ciTemplateStageJobRelDTO.setEnabled(ciTemplateJobVO.getEnabled());
            ciTemplateStageJobRelDTO.setCiTemplateStageId(ciTemplateStageDTO.getId());
            ciTemplateStageJobRelDTO.setSequence(jobSequence.get());
            if (CollectionUtils.isEmpty(ciTemplateStageJobRelMapper.select(ciTemplateStageJobRelDTO))) {
                ciTemplateStageJobRelMapper.insertSelective(ciTemplateStageJobRelDTO);
            }
            jobSequence.getAndSet(jobSequence.get() + 1);
        });
    }

    private CiTemplateStageDTO baseInsertStage(CiTemplatePipelineDTO ciTemplatePipelineDTO,
                                               AtomicReference<Long> sequence, CiTemplateStageVO ciTemplateStageVO) {
        CiTemplateStageDTO ciTemplateStageDTO = new CiTemplateStageDTO();
        BeanUtils.copyProperties(ciTemplateStageVO, ciTemplateStageDTO);
        ciTemplateStageDTO.setPipelineTemplateId(ciTemplatePipelineDTO.getId());
        ciTemplateStageDTO.setSequence(sequence.get());
        sequence.getAndSet(sequence.get() + 1);
        ciTemplateStageBusMapper.insertSelective(ciTemplateStageDTO);
        return ciTemplateStageDTO;
    }


    private void updateVariable(CiTemplatePipelineVO devopsPipelineTemplateVO,
                                CiTemplatePipelineDTO pipelineTemplateDTO) {
        ciTemplateVariableBusService.deleteByTemplatePipelineId(pipelineTemplateDTO.getId());

        if (!CollectionUtils.isEmpty(devopsPipelineTemplateVO.getCiTemplateVariableVOS())) {
            devopsPipelineTemplateVO.getCiTemplateVariableVOS().forEach(ciTemplateVariableVO -> {
                if (ciTemplateVariableVO.getVariableKey() == null || ciTemplateVariableVO.getVariableKey() == null) {
                    return;
                }
                CiTemplateVariableDTO ciTemplateVariableDTO
                        = ConvertUtils.convertObject(ciTemplateVariableVO, CiTemplateVariableDTO.class);
                ciTemplateVariableDTO.setPipelineTemplateId(pipelineTemplateDTO.getId());
                ciTemplateVariableDTO.setId(null);
                ciTemplateVariableBusMapper.insert(ciTemplateVariableDTO);
            });
        }
    }

    private void insertStageAndJobRel(CiTemplatePipelineDTO pipelineTemplateDTO, List<CiTemplateStageVO> templateStageVOS) {
        AtomicReference<Long> sequence = new AtomicReference<>(0L);
        templateStageVOS.forEach(ciTemplateStageVO -> {
            //2.插入stage数据
            CiTemplateStageDTO ciTemplateStageDTO = new CiTemplateStageDTO();
            BeanUtils.copyProperties(ciTemplateStageVO, ciTemplateStageDTO);
            ciTemplateStageDTO.setPipelineTemplateId(pipelineTemplateDTO.getId());
            ciTemplateStageDTO.setSequence(sequence.get());
            ciTemplateStageDTO.setId(null);
            ciTemplateStageBusMapper.insertSelective(ciTemplateStageDTO);
            sequence.getAndSet(sequence.get() + 1);

            List<CiTemplateJobVO> ciTemplateJobVOList = ciTemplateStageVO.getCiTemplateJobVOList();
            if (CollectionUtils.isEmpty(ciTemplateJobVOList)) {
                return;
            }
            AtomicReference<Integer> jobSequence = new AtomicReference<>(0);
            ciTemplateJobVOList.forEach(ciTemplateJobVO -> {
                //插入阶段与job的关联关系
                CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
                ciTemplateStageJobRelDTO.setCiTemplateJobId(ciTemplateJobVO.getId());
                ciTemplateStageJobRelDTO.setEnabled(ciTemplateJobVO.getEnabled());
                ciTemplateStageJobRelDTO.setCiTemplateStageId(ciTemplateStageDTO.getId());
                ciTemplateStageJobRelDTO.setSequence(jobSequence.get());
                ciTemplateStageJobRelDTO.setId(null);
                jobSequence.getAndSet(jobSequence.get() + 1);
                ciTemplateStageJobRelMapper.insertSelective(ciTemplateStageJobRelDTO);

                List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateJobVO.getDevopsCiStepVOList();
                if (CollectionUtils.isEmpty(ciTemplateStepVOS)) {
                    return;
                }
            });
        });
    }


    private void handNonVisibilityStage(Long sourceId, CiTemplateStageVO ciTemplateStageVO) {
        List<CiTemplateJobVO> ciTemplateJobVOList = ciTemplateStageVO.getCiTemplateJobVOList();
        if (CollectionUtils.isEmpty(ciTemplateJobVOList)) {
            return;
        }
        ciTemplateStageVO.setId(null);
        ciTemplateStageVO.setVisibility(false);
        ciTemplateJobVOList.forEach(ciTemplateJobVO -> {
            //「新任务模板」的名称为「任务名称_5位随机数」
            ciTemplateJobBusService.initNonVisibilityJob(sourceId, ciTemplateJobVO);
            List<CiTemplateStepVO> devopsCiStepVOList = ciTemplateJobVO.getDevopsCiStepVOList();
            if (CollectionUtils.isEmpty(devopsCiStepVOList)) {
                return;
            }
            //「新步骤模板」的名称为「步骤名称_5位随机数」；
            devopsCiStepVOList.forEach(ciTemplateStepVO -> {
                ciTemplateStepBusService.initNonVisibilityStep(sourceId, ciTemplateStepVO);
            });
        });
    }

}
