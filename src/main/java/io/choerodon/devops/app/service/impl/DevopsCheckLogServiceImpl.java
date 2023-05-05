package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CertificationType;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCheckLogServiceImpl.class);

    public static final String FIX_PIPELINE_SONAR_DATA = "fix_pipeline_sonar_data";
    public static final String FIX_CERTIFICATE_TYPE = "fix_certificate_type";
    public static final String FIX_STATEFULSET = "fix_statefulset";

    public static final String DELETE_DEVOPS_ENV_RESOURCE_DETAIL_DATA = "deleteDevopsEnvResourceDetailData";


    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;
    @Autowired
    private DevopsCiStepMapper devopsCiStepMapper;


    @Autowired
    private CiTemplateStageBusMapper ciTemplateStageBusMapper;

    @Autowired
    private CiTemplateStageJobRelBusMapper ciTemplateStageJobRelBusMapper;
    @Autowired
    private CertificationService certificationService;
    @Autowired
    private DevopsEnvResourceMapper devopsEnvResourceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkLog(String task) {
        DevopsCheckLogDTO devopsCheckLogDTO = new DevopsCheckLogDTO();
        devopsCheckLogDTO.setLog(task);
        DevopsCheckLogDTO existDevopsCheckLogDTO = devopsCheckLogMapper.selectOne(devopsCheckLogDTO);
        if (existDevopsCheckLogDTO != null) {
            LOGGER.info("fix data task {} has already been executed", task);
            return;
        }
        devopsCheckLogDTO.setBeginCheckDate(new Date());
        switch (task) {
            case FIX_PIPELINE_SONAR_DATA:
                fixPipelineSonarData();
                break;
            case FIX_CERTIFICATE_TYPE:
                fixCertificateType();
                break;
            case FIX_STATEFULSET:
                fixStatefulset();
                break;
            default:
                LOGGER.info("version not matched");
                return;
        }
        devopsCheckLogDTO.setLog(task);
        devopsCheckLogDTO.setEndCheckDate(new Date());
        devopsCheckLogMapper.insert(devopsCheckLogDTO);
    }

    private void fixStatefulset() {
        // 查出所有有重复记录的statefulset
        List<Long> instanceIds = devopsEnvResourceMapper.listInstanceIdsDuplicatedStatefulset().stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(instanceIds)) {
            return;
        }
        // 查出详细的StatefulSet信息
        List<DevopsEnvResourceDTO> devopsEnvResourceDTOS = devopsEnvResourceMapper.listStatefulsetByInstanceIds(instanceIds);
        Map<String, List<DevopsEnvResourceDTO>> grouppingByInstanceAndName = devopsEnvResourceDTOS.stream().collect(Collectors.groupingBy(e -> e.getInstanceId() + "-" + e.getName()));
        List<Long> resourceIdsToDelete = new ArrayList<>();
        grouppingByInstanceAndName.forEach((key, value) -> {
            if (value.size() > 1) {
                List<DevopsEnvResourceDTO> sortedEnvResource = value.stream().sorted(Comparator.comparing(DevopsEnvResourceDTO::getLastUpdateDate).reversed()).collect(Collectors.toList());
                sortedEnvResource.subList(1, sortedEnvResource.size()).forEach(e -> {
                    resourceIdsToDelete.add(e.getId());
                });
            }
        });
        if (CollectionUtils.isEmpty(resourceIdsToDelete)) {
            return;
        }
        devopsEnvResourceMapper.deleteByIds(resourceIdsToDelete);
    }

    private void fixCertificateType() {
        int count = certificationService.queryCountWithNullType();
        int pageSize = 500;
        int total = (count + pageSize - 1) / pageSize;
        int pageNumber = 0;
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>start fix certification type>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
        do {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>certification type {}/{} >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!", pageNumber, total);
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(pageNumber);
            pageRequest.setSize(pageSize);
            Page<CertificationDTO> certificationDTOPage = PageHelper.doPage(pageRequest, () -> certificationService.listWithNullType());
            certificationDTOPage.getContent().forEach(c -> {
                if (c.getOrgCertId() != null && c.getCertificationFileId() != null && c.getEnvId() != null) {
                    c.setType(CertificationType.CHOOSE.getType());
                } else if (c.getOrgCertId() == null && c.getCertificationFileId() != null && c.getEnvId() != null) {
                    c.setType(CertificationType.UPLOAD.getType());
                } else if (c.getOrgCertId() == null && c.getCertificationFileId() == null && c.getEnvId() != null) {
                    c.setType(CertificationType.REQUEST.getType());
                }
                certificationService.baseUpdate(c);
            });
            pageNumber++;
        } while (pageNumber <= total);
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>end fix certification type>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
    }

    private void fixPipelineSonarData() {
        // 查询所有代码检查任务
        devopsCiStepMapper.updateSonarScanner("sonar-scanner -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN} -Dsonar.password=${SONAR_PASSWORD} -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.sourceEncoding=UTF-8 -Dsonar.sources=${SONAR_SOURCES} -Dsonar.qualitygate.wait=${SONAR_QUALITYGATE_WAIT_FLAG}");
        devopsCiStepMapper.updateSonarMaven("# 如果了配置maven仓库,运行时会下载settings.xml到根目录，此时可以使用-s settings.xml指定使用\n" +
                "#一、Java 8项目扫描指令\n" +
                "#使用java8进行编译，如果是多模块项目则使用install命令\n" +
                "export JAVA_HOME=/opt/java/openjdk8\n" +
                "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent verify -Dmaven.test.failure.ignore=true -DskipTests=${SONAR_SKIP_TEST_FLAG}\n" +
                "#使用java11进行扫描\n" +
                "export JAVA_HOME=/opt/java/openjdk\n" +
                "mvn sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN} -Dsonar.password=${SONAR_PASSWORD}  -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.qualitygate.wait=${SONAR_QUALITYGATE_WAIT_FLAG}\n" +
                "\n" +
                "#Java 11项目扫描指令\n" +
                "#mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent verify sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN} -Dsonar.password=${SONAR_PASSWORD} -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.qualitygate.wait=${SONAR_QUALITYGATE_WAIT_FLAG} -Dmaven.test.failure.ignore=true -DskipTests=${SONAR_SKIP_TEST_FLAG}");
    }


    @Override
    public void fixCiTemplateStageJobRelSequence() {
        List<CiTemplateStageDTO> ciTemplateStageDTOS = ciTemplateStageBusMapper.selectAll();
        if (!CollectionUtils.isEmpty(ciTemplateStageDTOS)) {
            ciTemplateStageDTOS.forEach(ciTemplateStageDTO -> {
                List<CiTemplateStageJobRelDTO> relDTOS = ciTemplateStageJobRelBusMapper.listByStageId(ciTemplateStageDTO.getId());
                relDTOS = relDTOS.stream().sorted(Comparator.comparing(CiTemplateStageJobRelDTO::getId)).collect(Collectors.toList());
                int sequence = 0;
                for (CiTemplateStageJobRelDTO relDTO : relDTOS) {
                    relDTO.setSequence(sequence);
                    ciTemplateStageJobRelBusMapper.updateByPrimaryKeySelective(relDTO);
                    sequence++;
                }
            });
        }
    }
}
