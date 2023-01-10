package io.choerodon.devops.app.service.impl;


import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepCategoryVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.CiTemplateMavenBuildService;
import io.choerodon.devops.app.service.CiTemplateMavenPublishService;
import io.choerodon.devops.app.service.CiTemplateStepBusService;
import io.choerodon.devops.infra.constant.Constant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.devops.infra.utils.PipelineTemplateUtils;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.AssertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wangxiang on 2021/12/14
 */
@Service
public class CiTemplateStepBusServiceImpl implements CiTemplateStepBusService {

    private static final String STEP = "step";

    @Autowired
    private CiTemplateStepBusMapper ciTemplateStepBusMapper;
    @Autowired
    private CiTemplateStepCategoryBusMapper ciTemplateStepCategoryBusMapper;

    @Autowired
    private CiTemplateJobStepRelBusMapper ciTemplateJobStepRelBusMapper;

    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;


    @Autowired
    private PipelineTemplateUtils pipelineTemplateUtils;


    @Autowired
    private CiTemplateMavenBuildMapper ciTemplateMavenBuildMapper;

    @Autowired
    private CiTemplateMavenPublishMapper ciTemplateMavenPublishMapper;

    @Autowired
    private CiTemplateDockerMapper ciTemplateDockerMapper;

    @Autowired
    private CiTemplateSonarMapper ciTemplateSonarMapper;

    @Autowired
    private DevopsCiTplSonarQualityGateMapper devopsCiTplSonarQualityGateMapper;

    @Autowired
    private DevopsCiTplSonarQualityGateConditionMapper devopsCiTplSonarQualityGateConditionMapper;

    @Autowired
    private CiTemplateMavenBuildService ciTemplateMavenBuildService;

    @Autowired
    private CiTemplateStepBusService ciTemplateStepBusService;

    @Autowired
    private CiTemplateMavenPublishService ciTemplateMavenPublishService;


    @Override
    public Page<CiTemplateStepVO> pageTemplateStep(Long sourceId, String sourceType,
                                                   PageRequest pageRequest, String name,
                                                   Long categoryId, Boolean builtIn, String params) {
        Page<CiTemplateStepVO> templateStepDTOPageContent = PageHelper.doPage(pageRequest,
                () -> ciTemplateStepBusMapper.queryTemplateStepByParams(sourceId, sourceType,
                        pipelineTemplateUtils.getOrganizationId(sourceId, sourceType),
                        name, null, categoryId, builtIn, params));
        if (CollectionUtils.isEmpty(templateStepDTOPageContent)) {
            return templateStepDTOPageContent;
        }

        List<CiTemplateStepVO> ciTemplateStepVOS = templateStepDTOPageContent.getContent();
        List<CiTemplateStepVO> reTemplateStepVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(ciTemplateStepVOS)) {
            return templateStepDTOPageContent;
        }
        Map<String, List<CiTemplateStepVO>> stringListMap = ciTemplateStepVOS
                .stream().collect(Collectors.groupingBy(CiTemplateStepVO::getSourceType));
        List<CiTemplateStepVO> siteTemplates = stringListMap.get(ResourceLevel.PROJECT.value());
        if (!CollectionUtils.isEmpty(siteTemplates)) {
            reTemplateStepVOS.addAll(getSortedTemplate(siteTemplates));
        }
        List<CiTemplateStepVO> organizationTemplates = stringListMap.get(ResourceLevel.ORGANIZATION.value());
        if (!CollectionUtils.isEmpty(organizationTemplates)) {
            reTemplateStepVOS.addAll(getSortedTemplate(organizationTemplates));
        }
        List<CiTemplateStepVO> projectTemplates = stringListMap.get(ResourceLevel.SITE.value());
        if (!CollectionUtils.isEmpty(projectTemplates)) {
            reTemplateStepVOS.addAll(getSortedTemplate(projectTemplates));
        }

        templateStepDTOPageContent.setContent(reTemplateStepVOS);
        UserDTOFillUtil.fillUserInfo(templateStepDTOPageContent.getContent(), Constant.CREATED_BY, Constant.CREATOR);
        templateStepDTOPageContent.getContent().forEach(ciTemplateJobGroupVO -> {
            if (ciTemplateJobGroupVO.getBuiltIn()) {
                ciTemplateJobGroupVO.setCreator(null);
            }
        });
        return templateStepDTOPageContent;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateStepVO updateTemplateStep(Long sourceId, CiTemplateStepVO ciTemplateStepVO) {
        if (!checkTemplateStepName(sourceId, ciTemplateStepVO.getName(), ciTemplateStepVO.getId())) {
            throw new CommonException("error.step.name.already.exists");
        }

        CiTemplateStepDTO ciTemplateStepDTO = ciTemplateStepBusMapper.selectByPrimaryKey(ciTemplateStepVO.getId());
        AssertUtils.notNull(ciTemplateStepDTO, "error.ci.step.template.not.exist");
        AssertUtils.isTrue(!ciTemplateStepDTO.getBuiltIn(), "error.update.builtin.step.template");
        //是否预置这个字段不允许修改
        ciTemplateStepVO.setBuiltIn(ciTemplateStepDTO.getBuiltIn());
        BeanUtils.copyProperties(ciTemplateStepVO, ciTemplateStepDTO);
        ciTemplateStepBusMapper.updateByPrimaryKeySelective(ciTemplateStepDTO);
        ciTemplateStepVO.setId(ciTemplateStepDTO.getId());
        // 删除配置数据
        AbstractDevopsCiStepHandler stepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
        stepHandler.deleteTemplateStepConfig(ciTemplateStepVO);
        //插入配置数据
        stepHandler.saveTemplateStepConfig(ciTemplateStepVO);
        return ConvertUtils.convertObject(ciTemplateStepBusMapper
                .selectByPrimaryKey(ciTemplateStepVO.getId()), CiTemplateStepVO.class);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateStep(Long sourceId, String sourceType, Long ciStepTemplateId) {
        pipelineTemplateUtils.checkAccess(sourceId, sourceType);
        CiTemplateStepDTO ciTemplateStepDTO = ciTemplateStepBusMapper.selectByPrimaryKey(ciStepTemplateId);
        if (ciTemplateStepDTO == null) {
            return;
        }
        AssertUtils.isTrue(!ciTemplateStepDTO.getBuiltIn(), "error.delete.builtin.ci.step.template");
        ciTemplateStepBusMapper.deleteByPrimaryKey(ciTemplateStepDTO.getId());

        if (StringUtils.equalsIgnoreCase(ciTemplateStepDTO.getType(), DevopsCiStepTypeEnum.MAVEN_BUILD.value())) {
            CiTemplateMavenBuildDTO mavenBuildConfig = new CiTemplateMavenBuildDTO();
            mavenBuildConfig.setCiTemplateStepId(ciTemplateStepDTO.getId());
            ciTemplateMavenBuildMapper.delete(mavenBuildConfig);
        }
        if (StringUtils.equalsIgnoreCase(ciTemplateStepDTO.getType(), DevopsCiStepTypeEnum.MAVEN_PUBLISH.value())) {
            CiTemplateMavenPublishDTO mavenPublishConfig = new CiTemplateMavenPublishDTO();
            mavenPublishConfig.setCiTemplateStepId(ciTemplateStepDTO.getId());
            ciTemplateMavenPublishMapper.delete(mavenPublishConfig);
        }
        if (StringUtils.equalsIgnoreCase(ciTemplateStepDTO.getType(), DevopsCiStepTypeEnum.DOCKER_BUILD.value())) {
            CiTemplateDockerDTO ciTemplateDockerDTO = new CiTemplateDockerDTO();
            ciTemplateDockerDTO.setCiTemplateStepId(ciTemplateStepDTO.getId());
            ciTemplateDockerMapper.delete(ciTemplateDockerDTO);
        }
        if (StringUtils.equalsIgnoreCase(ciTemplateStepDTO.getType(), DevopsCiStepTypeEnum.UPLOAD_JAR.value())) {
            CiTemplateMavenPublishDTO mavenPublishConfig = new CiTemplateMavenPublishDTO();
            mavenPublishConfig.setCiTemplateStepId(ciTemplateStepDTO.getId());
            ciTemplateMavenPublishMapper.delete(mavenPublishConfig);
        }
        if (StringUtils.equalsIgnoreCase(ciTemplateStepDTO.getType(), DevopsCiStepTypeEnum.MAVEN_UNIT_TEST.value())) {
            CiTemplateMavenBuildDTO mavenBuildConfig = new CiTemplateMavenBuildDTO();
            mavenBuildConfig.setCiTemplateStepId(ciTemplateStepDTO.getId());
            ciTemplateMavenBuildMapper.delete(mavenBuildConfig);
        }

        if (StringUtils.equalsIgnoreCase(ciTemplateStepDTO.getType(), DevopsCiStepTypeEnum.SONAR.value())) {
            CiTemplateSonarDTO ciTemplateSonarDTO = new CiTemplateSonarDTO();
            ciTemplateSonarDTO.setCiTemplateStepId(ciTemplateStepDTO.getId());
            List<CiTemplateSonarDTO> ciTemplateSonarDTOS = ciTemplateSonarMapper.select(ciTemplateSonarDTO);
            if (!CollectionUtils.isEmpty(ciTemplateSonarDTOS)) {
                ciTemplateSonarDTOS.forEach(ciTemplateSonarDTO1 -> {
                    DevopsCiTplSonarQualityGateDTO devopsCiTplSonarQualityGateDTO = new DevopsCiTplSonarQualityGateDTO();
                    devopsCiTplSonarQualityGateDTO.setConfigId(ciTemplateSonarDTO1.getId());
                    List<DevopsCiTplSonarQualityGateDTO> devopsCiTplSonarQualityGateDTOS = devopsCiTplSonarQualityGateMapper.select(devopsCiTplSonarQualityGateDTO);
                    if (CollectionUtils.isEmpty(devopsCiTplSonarQualityGateDTOS)) {
                        return;
                    }
                    devopsCiTplSonarQualityGateDTOS.forEach(devopsCiTplSonarQualityGateDTO1 -> {
                        DevopsCiTplSonarQualityGateConditionDTO devopsCiTplSonarQualityGateConditionDTO = new DevopsCiTplSonarQualityGateConditionDTO();
                        devopsCiTplSonarQualityGateConditionDTO.setGateId(devopsCiTplSonarQualityGateDTO1.getId());
                        devopsCiTplSonarQualityGateConditionMapper.delete(devopsCiTplSonarQualityGateConditionDTO);
                    });
                    devopsCiTplSonarQualityGateMapper.delete(devopsCiTplSonarQualityGateDTO);

                });
            }
            ciTemplateSonarMapper.delete(ciTemplateSonarDTO);

            // 删除mvn配置
            CiTemplateMavenBuildDTO mavenBuildConfig = new CiTemplateMavenBuildDTO();
            mavenBuildConfig.setCiTemplateStepId(ciTemplateStepDTO.getId());
            ciTemplateMavenBuildMapper.delete(mavenBuildConfig);
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateStepVO createTemplateStep(Long sourceId, CiTemplateStepVO ciTemplateStepVO) {
        AssertUtils.notNull(ciTemplateStepVO, "error.ci.template.step.null");
        AssertUtils.isTrue(checkTemplateStepName(sourceId, ciTemplateStepVO.getName(), null),
                "error.step.name.already.exists");
        checkCategory(ciTemplateStepVO);
        CiTemplateStepDTO ciTemplateStepDTO = new CiTemplateStepDTO();
        BeanUtils.copyProperties(ciTemplateStepVO, ciTemplateStepDTO);
        if (ciTemplateStepBusMapper.insertSelective(ciTemplateStepDTO) != 1) {
            throw new CommonException("error.create.step.template");
        }
        //插入步骤的配置
        ciTemplateStepVO.setId(ciTemplateStepDTO.getId());
        AbstractDevopsCiStepHandler stepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
        stepHandler.saveTemplateStepConfig(ciTemplateStepVO);
        return ciTemplateStepVO;
    }


    @Override
    public List<CiTemplateStepVO> queryStepTemplateByJobId(Long sourceId, Long templateJobId) {
        List<CiTemplateStepDTO> ciTemplateStepDTOS = ciTemplateStepBusMapper
                .queryStepTemplateByJobIdAndSourceId(sourceId, templateJobId);
        List<CiTemplateStepVO> ciTemplateStepVOS = ConvertUtils.convertList(ciTemplateStepDTOS, CiTemplateStepVO.class);
        return ciTemplateStepVOS;
    }

    @Override
    public CiTemplateStepVO queryStepTemplateByStepId(Long sourceId, Long templateStepId) {
        CiTemplateStepDTO ciTemplateStepDTO = ciTemplateStepBusMapper.selectByPrimaryKey(templateStepId);
        //填充步骤的配置参数
        //添加步骤关联的配置信息
        CiTemplateStepVO ciTemplateStepVO = ConvertUtils.convertObject(ciTemplateStepDTO, CiTemplateStepVO.class);
        AbstractDevopsCiStepHandler stepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
        stepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
        return ciTemplateStepVO;
    }

    @Override
    public Boolean checkStepTemplateByStepId(Long sourceId, Long templateStepId) {
        CiTemplateJobStepRelDTO ciTemplateJobStepRelDTO = new CiTemplateJobStepRelDTO();
        ciTemplateJobStepRelDTO.setCiTemplateStepId(templateStepId);
        List<CiTemplateJobStepRelDTO> ciTemplateJobStepRelDTOS
                = ciTemplateJobStepRelBusMapper.select(ciTemplateJobStepRelDTO);
        return CollectionUtils.isEmpty(ciTemplateJobStepRelDTOS);
    }

    @Override
    public Boolean checkTemplateStepName(Long sourceId, String name, Long templateStepId) {
        return ciTemplateStepBusMapper.checkTemplateStepName(sourceId, name, templateStepId) == null;
    }

    @Override
    public List<CiTemplateStepVO> templateStepList(Long sourceId, String sourceType, String name) {
        return ConvertUtils.convertList(ciTemplateStepBusMapper.selectByParams(sourceId, sourceType,
                pipelineTemplateUtils.getOrganizationId(sourceId, sourceType), name), CiTemplateStepVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateStepByIds(Long projectId, Set<Long> ciStepTemplateIds) {
        if (!CollectionUtils.isEmpty(ciStepTemplateIds)) {
            ciStepTemplateIds.forEach(ciStepTemplateId -> {
                deleteTemplateStep(projectId, ResourceLevel.PROJECT.value(), ciStepTemplateId);
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNonVisibilityStep(Long sourceId, CiTemplateJobVO ciTemplateJobVO) {
        List<CiTemplateStepVO> devopsCiStepVOList = ciTemplateJobVO.getDevopsCiStepVOList();
        if (CollectionUtils.isEmpty(devopsCiStepVOList)) {
            return;
        }
        devopsCiStepVOList.forEach(ciTemplateStepVO -> {
            if (ciTemplateStepVO.getVisibility() == null || ciTemplateStepVO.getVisibility()) {
                return;
            }
            ciTemplateStepVO.setSourceType(ResourceLevel.PROJECT.value());
            ciTemplateStepVO.setBuiltIn(false);
            ciTemplateStepVO.setSourceId(sourceId);
            ciTemplateStepVO.setId(null);
            //插入不可见步骤之前先检查一下名称，如果重复再随机一下,只有项目层会走到这里
            if (!checkTemplateStepName(sourceId, ciTemplateStepVO.getName(), null)) {
                int indexOf = StringUtils.lastIndexOf(ciTemplateStepVO.getName(), BaseConstants.Symbol.LOWER_LINE);
                if (indexOf == -1) {
                    ciTemplateStepVO.setName(pipelineTemplateUtils.generateRandomName(PipelineTemplateUtils.STEP, sourceId, ciTemplateStepVO.getName()));
                } else {
                    String originName = StringUtils.substring(ciTemplateStepVO.getName(), 0, indexOf);
                    ciTemplateStepVO.setName(pipelineTemplateUtils.generateRandomName(PipelineTemplateUtils.STEP, sourceId, originName));
                }

            }
            CiTemplateStepVO templateStep = ciTemplateStepBusService.createTemplateStep(sourceId, ciTemplateStepVO);
//            //插入步骤的配置：
//            //MAVEN构建，MAVEN发布等等
//            if (StringUtils.equalsIgnoreCase(templateStep.getType(), DevopsCiStepTypeEnum.MAVEN_BUILD.value())) {
//                CiTemplateMavenBuildDTO mavenBuildConfig = ciTemplateStepVO.getMavenBuildConfig();
//                ciTemplateMavenBuildService.baseCreate(templateStep.getId(), mavenBuildConfig);
//            }
//            if (StringUtils.equalsIgnoreCase(templateStep.getType(), DevopsCiStepTypeEnum.MAVEN_PUBLISH.value())) {
//                CiTemplateMavenPublishDTO mavenPublishConfig = ciTemplateStepVO.getMavenPublishConfig();
//                ciTemplateMavenPublishService.baseCreate(templateStep.getId(), mavenPublishConfig);
//            }
//            if (StringUtils.equalsIgnoreCase(templateStep.getType(), DevopsCiStepTypeEnum.DOCKER_BUILD.value())) {
//                CiTemplateDockerDTO ciTemplateDockerDTO = ciTemplateStepVO.getDockerBuildConfig();
//                ciTemplateDockerDTO.setId(null);
//                ciTemplateDockerDTO.setCiTemplateStepId(templateStep.getId());
//                ciTemplateDockerMapper.insertSelective(ciTemplateDockerDTO);
//            }
//            if (StringUtils.equalsIgnoreCase(templateStep.getType(), DevopsCiStepTypeEnum.UPLOAD_JAR.value())) {
//                CiTemplateMavenPublishDTO mavenPublishConfig = ciTemplateStepVO.getMavenPublishConfig();
//                ciTemplateMavenPublishService.baseCreate(templateStep.getId(), mavenPublishConfig);
//            }
//            if (StringUtils.equalsIgnoreCase(templateStep.getType(), DevopsCiStepTypeEnum.MAVEN_UNIT_TEST.value())) {
//                CiTemplateMavenBuildDTO mavenBuildConfig = ciTemplateStepVO.getMavenBuildConfig();
//                ciTemplateMavenBuildService.baseCreate(templateStep.getId(), mavenBuildConfig);
//            }
//            if (StringUtils.equalsIgnoreCase(templateStep.getType(), DevopsCiStepTypeEnum.SONAR.value())) {
//                CiTemplateSonarDTO ciTemplateSonarDTO = ciTemplateStepVO.getSonarConfig();
//                ciTemplateSonarDTO.setId(null);
//                ciTemplateSonarDTO.setCiTemplateStepId(templateStep.getId());
//                ciTemplateSonarMapper.insertSelective(ciTemplateSonarDTO);
//                //插入Sonar的配置
//                DevopsCiTplSonarQualityGateDTO devopsCiTplSonarQualityGateDTO = ciTemplateSonarDTO.getDevopsCiSonarQualityGateVO();
//                if (devopsCiTplSonarQualityGateDTO != null) {
//                    if (devopsCiTplSonarQualityGateDTO.getGatesEnable()) {
//                        devopsCiTplSonarQualityGateDTO.setId(null);
//                        devopsCiTplSonarQualityGateDTO.setConfigId(ciTemplateSonarDTO.getId());
//                        devopsCiTplSonarQualityGateMapper.insertSelective(devopsCiTplSonarQualityGateDTO);
//                        //继续插入DevopsCiTplSonarQualityGateConditionDTO
//                        List<DevopsCiTplSonarQualityGateConditionDTO> sonarQualityGateConditionVOList = devopsCiTplSonarQualityGateDTO.getSonarQualityGateConditionVOList();
//                        if (!CollectionUtils.isEmpty(sonarQualityGateConditionVOList)) {
//                            sonarQualityGateConditionVOList.forEach(devopsCiTplSonarQualityGateConditionDTO -> {
//                                devopsCiTplSonarQualityGateConditionDTO.setId(null);
//                                devopsCiTplSonarQualityGateConditionDTO.setGateId(devopsCiTplSonarQualityGateDTO.getId());
//                            });
//                            devopsCiTplSonarQualityGateConditionMapper.insertList(sonarQualityGateConditionVOList);
//                        }
//                    }
//                }
//                // 保存mvn配置
//                CiTemplateMavenBuildDTO mavenBuildConfig = ciTemplateSonarDTO.getMavenBuildConfig();
//                if (mavenBuildConfig != null
//                        && (!CollectionUtils.isEmpty(mavenBuildConfig.getNexusMavenRepoIds())
//                        || !CollectionUtils.isEmpty(mavenBuildConfig.getRepos()))) {
//                    ciTemplateMavenBuildService.baseCreate(templateStep.getId(), mavenBuildConfig);
//                }
//            }

            ciTemplateStepVO.setId(templateStep.getId());
        });
        ciTemplateJobVO.setDevopsCiStepVOList(devopsCiStepVOList);
    }

    @Override
    public List<CiTemplateStepCategoryVO> listStepWithCategory(Long sourceId, String sourceType) {
        // 先查询所有步骤
        List<CiTemplateStepVO> ciTemplateStepVOS = templateStepList(sourceId, sourceType, null);
        ciTemplateStepVOS.forEach(ciTemplateStepVO -> {
            AbstractDevopsCiStepHandler devopsCiStepHandler
                    = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
            devopsCiStepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
        });
        Map<Long, List<CiTemplateStepVO>> categoryStepMap
                = ciTemplateStepVOS.stream().collect(Collectors.groupingBy(CiTemplateStepVO::getCategoryId));

        Set<Long> categoryIds = ciTemplateStepVOS.stream().map(CiTemplateStepVO::getCategoryId).collect(Collectors.toSet());
        List<CiTemplateStepCategoryDTO> ciTemplateStepCategoryDTOS
                = ciTemplateStepCategoryBusMapper.selectByIds(StringUtils.join(categoryIds, ","));
        List<CiTemplateStepCategoryVO> ciTemplateStepCategoryVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ciTemplateStepCategoryDTOS)) {
            ciTemplateStepCategoryVOS = ConvertUtils.convertList(ciTemplateStepCategoryDTOS, CiTemplateStepCategoryVO.class);
        }
        // 将步骤分组
        ciTemplateStepCategoryVOS.forEach(ciTemplateStepCategoryVO -> {
            List<CiTemplateStepVO> ciTemplateStepVOList = categoryStepMap.get(ciTemplateStepCategoryVO.getId());
            ciTemplateStepCategoryVO.setCiTemplateStepVOList(ciTemplateStepVOList);
        });

        //处理排序
        List<CiTemplateStepCategoryVO> templateStepCategoryVOS = sortedStepCategory(ciTemplateStepCategoryVOS);

        return templateStepCategoryVOS;

    }

    @Override
    public void initNonVisibilityStep(Long sourceId, CiTemplateStepVO ciTemplateStepVO) {
        ciTemplateStepVO.setName(pipelineTemplateUtils.generateRandomName(STEP, sourceId, ciTemplateStepVO.getName()));
        ciTemplateStepVO.setVisibility(Boolean.FALSE);
        ciTemplateStepVO.setId(null);
        ciTemplateStepVO.setCategoryId(0L);
        ciTemplateStepVO.setSourceType(ResourceLevel.PROJECT.value());
        ciTemplateStepVO.setSourceId(sourceId);
        ciTemplateStepVO.setBuiltIn(false);
    }

    @Override
    public boolean checkName(Long projectId, String newName) {
        CiTemplateStepDTO ciTemplateStepDTO = new CiTemplateStepDTO();
        ciTemplateStepDTO.setSourceId(projectId);
        ciTemplateStepDTO.setName(newName);
        return !CollectionUtils.isEmpty(ciTemplateStepBusMapper.select(ciTemplateStepDTO));
    }

    private List<CiTemplateStepCategoryVO> sortedStepCategory(List<CiTemplateStepCategoryVO> ciTemplateStepCategoryVOS) {
        List<CiTemplateStepCategoryVO> resultTemplateStepCategoryVOS = new ArrayList<>();
        //构建放在第一位 自定义的放在最后
        List<CiTemplateStepCategoryVO> customJobGroupVOS
                = ciTemplateStepCategoryVOS.stream().filter(stepCategoryVO -> !stepCategoryVO.getBuiltIn())
                .collect(Collectors.toList());
        List<CiTemplateStepCategoryVO> otherVos = ciTemplateStepCategoryVOS.stream()
                .filter(CiTemplateStepCategoryVO::getBuiltIn)
                .filter(ciTemplateStepCategoryVO -> StringUtils.equalsIgnoreCase(ciTemplateStepCategoryVO.getName(), "其他"))
                .collect(Collectors.toList());


        List<CiTemplateStepCategoryVO> firstVos = ciTemplateStepCategoryVOS
                .stream()
                .filter(ciTemplateStepCategoryVO -> StringUtils.equalsIgnoreCase(ciTemplateStepCategoryVO.getName(), "构建"))
                .collect(Collectors.toList());

        List<CiTemplateStepCategoryVO> groupVOS = ciTemplateStepCategoryVOS.stream().filter(CiTemplateStepCategoryVO::getBuiltIn)
                .filter(ciTemplateStepCategoryVO -> !StringUtils.equalsIgnoreCase(ciTemplateStepCategoryVO.getName(), "其他"))
                .filter(ciTemplateStepCategoryVO -> !StringUtils.equalsIgnoreCase(ciTemplateStepCategoryVO.getName(), "构建"))
                .collect(Collectors.toList());

        resultTemplateStepCategoryVOS.addAll(firstVos);
        resultTemplateStepCategoryVOS.addAll(groupVOS);
        resultTemplateStepCategoryVOS.addAll(otherVos);
        resultTemplateStepCategoryVOS.addAll(customJobGroupVOS);
        return resultTemplateStepCategoryVOS;
    }

    private void checkCategory(CiTemplateStepVO ciTemplateStepVO) {
        if (ciTemplateStepVO.getVisibility() != null || !ciTemplateStepVO.getVisibility()) {
            return;
        }
        CiTemplateStepCategoryDTO ciTemplateStepCategoryDTO
                = ciTemplateStepCategoryBusMapper.selectByPrimaryKey(ciTemplateStepVO.getCategoryId());
        AssertUtils.notNull(ciTemplateStepCategoryDTO, "error.step.template.not.exist");
    }

    private static List<CiTemplateStepVO> getSortedTemplate(List<CiTemplateStepVO> siteTemplates) {
        return siteTemplates.stream()
                .sorted(Comparator.comparing(CiTemplateStepVO::getCreationDate).reversed()).collect(Collectors.toList());
    }
}
