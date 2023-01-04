package io.choerodon.devops.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.app.eventhandler.pipeline.step.AbstractDevopsCiStepHandler;
import io.choerodon.devops.app.service.CiTemplateJobBusService;
import io.choerodon.devops.app.service.CiTemplateStepBusService;
import io.choerodon.devops.app.service.CiTemplateStepService;
import io.choerodon.devops.app.service.impl.config.TemplateJobConfigService;
import io.choerodon.devops.infra.dto.CiTemplateJobDTO;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.devops.infra.dto.CiTemplateJobStepRelDTO;
import io.choerodon.devops.infra.dto.CiTemplateStageJobRelDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.mapper.CiTemplateJobBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobGroupBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateJobStepRelBusMapper;
import io.choerodon.devops.infra.mapper.CiTemplateStageJobRelBusMapper;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.devops.infra.utils.PipelineTemplateUtils;
import io.choerodon.devops.infra.utils.TemplateJobTypeUtils;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * Created by wangxiang on 2021/12/16
 */
@Service
public class CiTemplateJobBusServiceImpl implements CiTemplateJobBusService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CiTemplateJobBusServiceImpl.class);

    private static final int MAX_NAME_LENGTH = 60;
    private static final String TEMPLATE_JOB_CONFIG_SERVICE = "TemplateJobConfigService";
    private static final String JOB = "job";

    @Autowired
    private CiTemplateJobBusMapper ciTemplateJobBusMapper;

    @Autowired
    private CiTemplateJobGroupBusMapper ciTemplateJobGroupBusMapper;

    @Autowired
    private CiTemplateJobStepRelBusMapper ciTemplateJobStepRelBusMapper;

    @Autowired
    private CiTemplateStageJobRelBusMapper ciTemplateStageJobRelBusMapper;

    @Autowired
    private CiTemplateStepService ciTemplateStepService;

    @Autowired
    private CiTemplateStepBusService ciTemplateStepBusService;

    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;

    @Autowired
    private PipelineTemplateUtils pipelineTemplateUtils;

    @Autowired
    private Map<String, TemplateJobConfigService> stringTemplateJobConfigServiceMap;


    @Override
    public List<CiTemplateJobVO> queryTemplateJobsByGroupId(Long sourceId, Long ciTemplateJobGroupId) {
        List<CiTemplateJobVO> ciTemplateJobVOS = queryBaseTemplateJob(sourceId, ciTemplateJobGroupId);
        // 填充任务中的步骤信息
        if (CollectionUtils.isEmpty(ciTemplateJobVOS)) {
            return Collections.emptyList();
        }
        Set<Long> jobIds = ciTemplateJobVOS.stream().map(CiTemplateJobVO::getId).collect(Collectors.toSet());
        List<CiTemplateStepVO> ciTemplateStepVOS = ciTemplateStepService.listByJobIds(jobIds);
        Map<Long, List<CiTemplateStepVO>> jobStepsMap = ciTemplateStepVOS.stream()
                .collect(Collectors.groupingBy(CiTemplateStepVO::getCiTemplateJobId));

        ciTemplateJobVOS.forEach(ciTemplateJobVO -> {
            handTemplateJob(jobStepsMap, ciTemplateJobVO);
        });
        return ciTemplateJobVOS;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateJobVO createTemplateJob(Long sourceId, String sourceType, CiTemplateJobVO ciTemplateJobVO) {
        if (ciTemplateJobVO == null) {
            return null;
        }
        ciTemplateJobVO.setId(null);
        pipelineTemplateUtils.checkAccess(sourceId, sourceType);
        checkParam(ciTemplateJobVO);
        AssertUtils.isTrue(isNameUnique(ciTemplateJobVO.getName(), sourceId, null),
                "error.job.template.name.exist");
        CiTemplateJobDTO ciTemplateJobDTO = ConvertUtils.convertObject(ciTemplateJobVO, CiTemplateJobDTO.class);
        String type = TemplateJobTypeUtils.stringStringMap.get(ciTemplateJobVO.getType());
        if (!StringUtils.isEmpty(type)) {
            ciTemplateJobDTO.setConfigId(stringTemplateJobConfigServiceMap.get(type + TEMPLATE_JOB_CONFIG_SERVICE).baseInsert(ciTemplateJobVO));
        }
        // 插入job记录
        ciTemplateJobBusMapper.insertSelective(ciTemplateJobDTO);
        if (!CollectionUtils.isEmpty(ciTemplateJobVO.getDevopsCiStepVOList())) {
            insertBaseJobStepRel(ciTemplateJobVO, ciTemplateJobDTO);
        }
        return ConvertUtils.convertObject(ciTemplateJobDTO, CiTemplateJobVO.class);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public CiTemplateJobVO updateTemplateJob(Long sourceId, String sourceType, CiTemplateJobVO ciTemplateJobVO) {
        pipelineTemplateUtils.checkAccess(sourceId, sourceType);
        checkParam(ciTemplateJobVO);
        AssertUtils.isTrue(isNameUnique(ciTemplateJobVO.getName(), sourceId, ciTemplateJobVO.getId()),
                "error.job.template.name.exist");

        CiTemplateJobDTO templateJobDTO = ciTemplateJobBusMapper.selectByPrimaryKey(ciTemplateJobVO.getId());
        AssertUtils.notNull(templateJobDTO, "error.templateJobDTO.is.null");
        CiTemplateJobDTO ciTemplateJobDTO = ConvertUtils.convertObject(ciTemplateJobVO, CiTemplateJobDTO.class);
        // 更新job记录
        ciTemplateJobDTO.setObjectVersionNumber(templateJobDTO.getObjectVersionNumber());
        ciTemplateJobBusMapper.updateByPrimaryKeySelective(ciTemplateJobDTO);
        // 更新job和step关系
        if (!CollectionUtils.isEmpty(ciTemplateJobVO.getDevopsCiStepVOList())) {
            // 先删除旧关系
            ciTemplateJobStepRelBusMapper.deleteByJobId(ciTemplateJobVO.getId());
            // 添加job和step关系
            insertBaseJobStepRel(ciTemplateJobVO, ciTemplateJobDTO);
        }
        return ConvertUtils.convertObject(ciTemplateJobDTO, CiTemplateJobVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateJob(Long sourceId, String sourceType, Long jobId) {
        pipelineTemplateUtils.checkAccess(sourceId, sourceType);
        // 删除与steps的关系
        ciTemplateJobStepRelBusMapper.deleteByJobId(jobId);
        // 删除job
        ciTemplateJobBusMapper.deleteByPrimaryKey(jobId);
    }

    @Override
    public Boolean isNameUnique(String name, Long sourceId, Long jobId) {
        return ciTemplateJobBusMapper.isNameUnique(name, sourceId, jobId) == null;
    }


    @Override
    public Page<CiTemplateJobVO> pageTemplateJobs(Long sourceId, String sourceType, PageRequest pageRequest,
                                                  String name, Long groupId, Boolean builtIn, String params) {

        Page<CiTemplateJobVO> ciTemplateJobVOPage = PageHelper.doPage(pageRequest,
                () -> ciTemplateJobBusMapper.pageUnderOrgLevel(sourceId, sourceType,
                        pipelineTemplateUtils.getOrganizationId(sourceId, sourceType),
                        name, null, groupId, builtIn, params));
        //处理组内排序，组内按照时间倒序
        List<CiTemplateJobVO> templatePipelineVOS = ciTemplateJobVOPage.getContent();
        List<CiTemplateJobVO> reTemplatePipelineVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(templatePipelineVOS)) {
            return ciTemplateJobVOPage;
        }
        Map<String, List<CiTemplateJobVO>> stringListMap = templatePipelineVOS.stream()
                .collect(Collectors.groupingBy(CiTemplateJobVO::getSourceType));
        List<CiTemplateJobVO> siteTemplates = stringListMap.get(ResourceLevel.PROJECT.value());
        if (!CollectionUtils.isEmpty(siteTemplates)) {
            reTemplatePipelineVOS.addAll(getSortedTemplate(siteTemplates));
        }
        List<CiTemplateJobVO> organizationTemplates = stringListMap.get(ResourceLevel.ORGANIZATION.value());
        if (!CollectionUtils.isEmpty(organizationTemplates)) {
            reTemplatePipelineVOS.addAll(getSortedTemplate(organizationTemplates));
        }
        List<CiTemplateJobVO> projectTemplates = stringListMap.get(ResourceLevel.SITE.value());
        if (!CollectionUtils.isEmpty(projectTemplates)) {
            reTemplatePipelineVOS.addAll(getSortedTemplate(projectTemplates));
        }
        ciTemplateJobVOPage.setContent(reTemplatePipelineVOS);
        UserDTOFillUtil.fillUserInfo(ciTemplateJobVOPage.getContent()
                .stream().collect(Collectors.toList()), "createdBy", "creator");
        ciTemplateJobVOPage.getContent().forEach(ciTemplateJobGroupVO -> {
            if (ciTemplateJobGroupVO.getBuiltIn()) {
                ciTemplateJobGroupVO.setCreator(null);
            }
        });
        return ciTemplateJobVOPage;
    }

    @Override
    public Boolean checkJobTemplateByJobId(Long sourceId, Long templateJobId) {
        CiTemplateStageJobRelDTO ciTemplateStageJobRelDTO = new CiTemplateStageJobRelDTO();
        ciTemplateStageJobRelDTO.setCiTemplateJobId(templateJobId);
        List<CiTemplateStageJobRelDTO> ciTemplateStageJobRelDTOS
                = ciTemplateStageJobRelBusMapper.select(ciTemplateStageJobRelDTO);
        return CollectionUtils.isEmpty(ciTemplateStageJobRelDTOS);
    }

    @Override
    public List<CiTemplateJobVO> listTemplateJobs(Long sourceId, String sourceType) {
        return ciTemplateJobBusMapper.queryAllCiTemplateJob(sourceId, sourceType,
                pipelineTemplateUtils.getOrganizationId(sourceId, sourceType));
    }

    @Override
    public CiTemplateJobVO queryTemplateByJobById(Long sourceId, Long templateJobId) {
        CiTemplateJobDTO ciTemplateJobDTO = ciTemplateJobBusMapper.selectByPrimaryKey(templateJobId);
        if (ciTemplateJobDTO == null) {
            return new CiTemplateJobVO();
        }
        CiTemplateJobVO ciTemplateJobVO = ConvertUtils.convertObject(ciTemplateJobDTO, CiTemplateJobVO.class);
        //根据步骤的类型填充CD的配置信息
        fillCdJobConfig(ciTemplateJobVO);
        CiTemplateJobGroupDTO ciTemplateJobGroupDTO = ciTemplateJobGroupBusMapper.selectByPrimaryKey(ciTemplateJobVO.getGroupId());
        ciTemplateJobVO.setCiTemplateJobGroupDTO(ciTemplateJobGroupDTO);
        //查询step
        List<CiTemplateStepVO> templateStepVOList = ciTemplateStepService
                .listByJobIds(Arrays.asList(ciTemplateJobVO.getId()).stream().collect(Collectors.toSet()));
        List<CiTemplateStepVO> reTemplateStepVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(templateStepVOList)) {
            List<CiTemplateStepVO> finalReTemplateStepVOS = reTemplateStepVOS;
            templateStepVOList.forEach(ciTemplateStepVO -> {
                // 添加步骤关联的配置信息
                AbstractDevopsCiStepHandler stepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
                stepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
                finalReTemplateStepVOS.add(ciTemplateStepVO);
            });
            //步骤按照sequence
            reTemplateStepVOS = finalReTemplateStepVOS.stream()
                    .sorted(Comparator.comparing(CiTemplateStepVO::getSequence)).collect(Collectors.toList());
        }
        ciTemplateJobVO.setDevopsCiStepVOList(reTemplateStepVOS);
        ciTemplateJobVO.setOpenParallel(Objects.isNull(ciTemplateJobVO.getParallel()) ? Boolean.FALSE : Boolean.TRUE);

        return ciTemplateJobVO;
    }

    @Override
    public void fillCdJobConfig(CiTemplateJobVO ciTemplateJobVO) {
        String type = TemplateJobTypeUtils.stringStringMap.get(ciTemplateJobVO.getType());
        if (StringUtils.isEmpty(type)) {
            return;
        }
        stringTemplateJobConfigServiceMap.get(type + TEMPLATE_JOB_CONFIG_SERVICE).fillCdJobConfig(ciTemplateJobVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateJobConfig(CiTemplateJobDTO ciTemplateJobDTO) {
        String type = TemplateJobTypeUtils.stringStringMap.get(ciTemplateJobDTO.getType());
        if (StringUtils.isEmpty(type)) {
            return;
        }
        stringTemplateJobConfigServiceMap.get(type + TEMPLATE_JOB_CONFIG_SERVICE)
                .baseDelete(ConvertUtils.convertObject(ciTemplateJobDTO, CiTemplateJobVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateJobByIds(Set<Long> ciTemplateJobIds) {
        if (CollectionUtils.isEmpty(ciTemplateJobIds)) {
            return;
        }
        //判断是不是不可见的job，job的配置，步骤，步骤的配置。
        List<CiTemplateJobDTO> ciTemplateJobDTOS = ciTemplateJobBusMapper.selectByIds(StringUtils.join(ciTemplateJobIds, ","));
        if (CollectionUtils.isEmpty(ciTemplateJobDTOS)) {
            return;
        }
        ciTemplateJobDTOS.forEach(ciTemplateJobDTO -> {
            if (ciTemplateJobDTO.getBuiltIn()) {
                return;
            } else {
                deleteTemplateJobConfig(ciTemplateJobDTO);
                // 看看是不是非预置的关联到了预置的数据
//                CiTemplateJobDTO templateJobDTO = new CiTemplateJobDTO();
//                templateJobDTO.setBuiltIn(Boolean.TRUE);
//                templateJobDTO.setType(ciTemplateJobDTO.getType());
//                List<CiTemplateJobDTO> templateJobDTOS = ciTemplateJobBusMapper.select(templateJobDTO);
//                if (CollectionUtils.isEmpty(templateJobDTOS)) {
//                    deleteTemplateJobConfig(ciTemplateJobDTO);
//                } else {
//                    CiTemplateJobDTO jobDTO = templateJobDTOS.get(0);
//                    Long configId = jobDTO.getConfigId();
//                    if (configId.longValue() != ciTemplateJobDTO.getConfigId().longValue()) {
//                        deleteTemplateJobConfig(ciTemplateJobDTO);
//                    }
//                }
                ciTemplateJobBusMapper.deleteByPrimaryKey(ciTemplateJobDTO.getId());
            }
        });

//        ciTemplateJobBusMapper.deleteNonVisibilityJobByIds(ciTemplatejobIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNonVisibilityJob(Long sourceId, String sourceType, List<CiTemplateJobVO> ciTemplateJobVOList) {
        if (!StringUtils.equalsIgnoreCase(sourceType, ResourceLevel.PROJECT.value())) {
            return;
        }
        ciTemplateJobVOList.forEach(ciTemplateJobVO -> {
            if (ciTemplateJobVO.getVisibility() == null || ciTemplateJobVO.getVisibility()) {
                return;
            }
            //插入不可见的步骤
            ciTemplateStepBusService.createNonVisibilityStep(sourceId, ciTemplateJobVO);
            //插入不可见的任务
            //插入不可见任务之前先检查一下名称，如果重复再随机一下,只有项目层会走到这里
            if (!isNameUnique(ciTemplateJobVO.getName(), sourceId, null)) {
                int indexOf = StringUtils.lastIndexOf(ciTemplateJobVO.getName(), BaseConstants.Symbol.LOWER_LINE);
                if (indexOf == -1) {
                    ciTemplateJobVO.setName(pipelineTemplateUtils.generateRandomName(PipelineTemplateUtils.JOB, sourceId, ciTemplateJobVO.getName()));
                } else {
                    String originName = StringUtils.substring(ciTemplateJobVO.getName(), 0, indexOf);
                    ciTemplateJobVO.setName(pipelineTemplateUtils.generateRandomName(PipelineTemplateUtils.JOB, sourceId, originName));
                }

            }
            CiTemplateJobVO templateJob = createTemplateJob(sourceId, sourceType, ciTemplateJobVO);
            ciTemplateJobVO.setId(templateJob.getId());
        });
    }

    @Override
    public boolean checkName(Long projectId, String newName) {
        CiTemplateJobDTO ciTemplateJobDTO = new CiTemplateJobDTO();
        ciTemplateJobDTO.setSourceId(projectId);
        ciTemplateJobDTO.setName(newName);
        return !CollectionUtils.isEmpty(ciTemplateJobBusMapper.select(ciTemplateJobDTO));
    }

    @Override
    public void initNonVisibilityJob(Long sourceId, CiTemplateJobVO ciTemplateJobVO) {
        ciTemplateJobVO.setName(pipelineTemplateUtils.generateRandomName(JOB, sourceId, ciTemplateJobVO.getName()));
        ciTemplateJobVO.setVisibility(Boolean.FALSE);
        ciTemplateJobVO.setId(null);
//        ciTemplateJobVO.setGroupId(0L);
        //处理group
        ciTemplateJobVO.setGroupId(fillGroupId(ciTemplateJobVO.getGroupType()));
        ciTemplateJobVO.setSourceType(ResourceLevel.PROJECT.value());
        ciTemplateJobVO.setSourceId(sourceId);
        ciTemplateJobVO.setBuiltIn(false);
    }


    private void checkParam(CiTemplateJobVO ciTemplateJobVO) {
        // 检验名称
        AssertUtils.isTrue(!(ciTemplateJobVO.getName().length() > MAX_NAME_LENGTH),
                "error.ci.template.job.name.length");

        // 如果是普通创建类型的任务，需要校验关联的步骤不为空
//        AssertUtils.isTrue(!(CiJobTypeEnum.NORMAL.value().equals(ciTemplateJobVO.getType())
//                && ciTemplateJobVO.getDevopsCiStepVOList().size() == 0), "error.ci.template.job.normal.step.size");
        if (ciTemplateJobVO.getVisibility() == null || !ciTemplateJobVO.getVisibility()) {
            return;
        }
        // 绑定的组不能为空或不存在
        AssertUtils.isTrue(!(ciTemplateJobVO.getGroupId() == null
                        || ciTemplateJobGroupBusMapper.selectByPrimaryKey(ciTemplateJobVO.getGroupId()) == null),
                "error.ci.template.job.group.exist");
    }

    private void handTemplateJob(Map<Long, List<CiTemplateStepVO>> jobStepsMap, CiTemplateJobVO ciTemplateJobVO) {
        List<CiTemplateStepVO> ciTemplateStepVOList = jobStepsMap.get(ciTemplateJobVO.getId());
        if (!CollectionUtils.isEmpty(ciTemplateStepVOList)) {
            List<CiTemplateStepVO> templateStepVOList = new ArrayList<>();
            ciTemplateStepVOList.forEach(ciTemplateStepVO -> {
                // 添加步骤关联的配置信息
                AbstractDevopsCiStepHandler stepHandler = devopsCiStepOperator.getHandlerOrThrowE(ciTemplateStepVO.getType());
                stepHandler.fillTemplateStepConfigInfo(ciTemplateStepVO);
                templateStepVOList.add(ciTemplateStepVO);
            });
            //步骤按照sequence排序
            List<CiTemplateStepVO> reTemplateStepVOS = templateStepVOList.stream()
                    .sorted(Comparator.comparing(CiTemplateStepVO::getSequence)).collect(Collectors.toList());
            ciTemplateJobVO.setDevopsCiStepVOList(reTemplateStepVOS);
        }
    }

    private List<CiTemplateJobVO> queryBaseTemplateJob(Long sourceId, Long ciTemplateJobGroupId) {
        CiTemplateJobDTO record = new CiTemplateJobDTO();
        record.setGroupId(ciTemplateJobGroupId);
        //平台层的查不到组织层的job
        if (sourceId == 0) {
            record.setSourceId(sourceId);
        }
        List<CiTemplateJobDTO> ciTemplateJobDTOS = ciTemplateJobBusMapper.select(record);
        List<CiTemplateJobVO> ciTemplateJobVOS = ConvertUtils.convertList(ciTemplateJobDTOS, CiTemplateJobVO.class);
        return ciTemplateJobVOS;
    }

    private void insertBaseJobStepRel(CiTemplateJobVO ciTemplateJobVO, CiTemplateJobDTO ciTemplateJobDTO) {
        //校验不能有重复的步骤
        List<Long> stepListIds = ciTemplateJobVO.getDevopsCiStepVOList().stream().map(CiTemplateStepVO::getId).collect(Collectors.toList());
        Set<Long> stepSetIds = ciTemplateJobVO.getDevopsCiStepVOList().stream().map(CiTemplateStepVO::getId).collect(Collectors.toSet());
        AssertUtils.isTrue(stepListIds.size() == stepSetIds.size(), "devops.error.step.repeat");
        // 添加job和step关系
        AtomicReference<Long> sequence = new AtomicReference<>(0L);
        ciTemplateJobVO.getDevopsCiStepVOList().forEach(ciTemplateStepVO -> {
            CiTemplateJobStepRelDTO ciTemplateJobStepRelDTO = new CiTemplateJobStepRelDTO();
            ciTemplateJobStepRelDTO.setCiTemplateJobId(ciTemplateJobDTO.getId());
            ciTemplateJobStepRelDTO.setCiTemplateStepId(ciTemplateStepVO.getId());
            ciTemplateJobStepRelDTO.setSequence(sequence.get());
            sequence.getAndSet(sequence.get() + 1);
            ciTemplateJobStepRelBusMapper.insert(ciTemplateJobStepRelDTO);
        });
    }

    private static List<CiTemplateJobVO> getSortedTemplate(List<CiTemplateJobVO> siteTemplates) {
        return siteTemplates.stream()
                .sorted(Comparator.comparing(CiTemplateJobVO::getCreationDate).reversed()).collect(Collectors.toList());
    }

    private Long fillGroupId(String groupType) {
        if (StringUtils.isBlank(groupType)) {
            return BaseConstants.DEFAULT_TENANT_ID;
        }
        CiTemplateJobGroupDTO ciTemplateJobGroupDTO = new CiTemplateJobGroupDTO();
        ciTemplateJobGroupDTO.setType(groupType);
        CiTemplateJobGroupDTO templateJobGroupDTO = ciTemplateJobGroupBusMapper.selectOne(ciTemplateJobGroupDTO);
        return templateJobGroupDTO == null ? BaseConstants.DEFAULT_TENANT_ID : templateJobGroupDTO.getId();
    }

}
