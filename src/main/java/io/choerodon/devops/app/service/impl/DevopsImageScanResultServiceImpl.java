package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsImageScanResultVO;
import io.choerodon.devops.api.vo.ImageScanResultVO;
import io.choerodon.devops.api.vo.VulnerabilitieVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiDockerBuildConfigService;
import io.choerodon.devops.app.service.DevopsCiStepService;
import io.choerodon.devops.app.service.DevopsImageScanResultService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiDockerBuildConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiStepDTO;
import io.choerodon.devops.infra.dto.DevopsImageScanResultDTO;
import io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum;
import io.choerodon.devops.infra.enums.ImageSecurityEnum;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.mapper.DevopsCiJobMapper;
import io.choerodon.devops.infra.mapper.DevopsImageScanResultMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by wangxiang on 2021/3/25
 */
@Service
public class DevopsImageScanResultServiceImpl implements DevopsImageScanResultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsImageScanResultServiceImpl.class);

    @Autowired
    private DevopsImageScanResultMapper devopsImageScanResultMapper;

    @Autowired
    private AppServiceService appServiceService;

    @Autowired
    private DevopsCiJobMapper devopsCiJobMapper;

    @Autowired
    private DevopsCiStepService devopsCiStepService;
    @Autowired
    private DevopsCiDockerBuildConfigService devopsCiDockerBuildConfigService;

    private static void securityMonitor(Integer integer, DevopsCiDockerBuildConfigDTO securityConditionConfigVO) {
        if (StringUtils.equalsIgnoreCase("<=", securityConditionConfigVO.getSecurityControlConditions())) {
            if (integer > securityConditionConfigVO.getVulnerabilityCount()) {
                LOGGER.info("loophole count:{},security control:{}", integer, securityConditionConfigVO.getVulnerabilityCount());
                throw new DevopsCiInvalidException("Does not meet the security control conditions," + securityConditionConfigVO.getSeverity()
                        + " loophole count:" + integer);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveImageScanJson(Long gitlabPipelineId,
                                     Long jobId,
                                     Date startDate,
                                     Date endDate,
                                     MultipartFile file,
                                     String token,
                                     String jobName) {
        LOGGER.info(">>>>>>>>>>>>>>>>>>startDate:{},endDate:{}", startDate, endDate);
        //file 有可能为null,如果镜像没有漏洞这个报告文件就是空的
        String content = null;
        List<ImageScanResultVO> imageScanResultVOS = new ArrayList<>();
        try {
            content = new String(file.getBytes(), "UTF-8");
            LOGGER.debug(">>>>>>>>>>>>>>>>>>>trivy scan result:{}", content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(Objects.requireNonNull(token));
        Long appServiceId = appServiceDTO.getId();
        if (StringUtils.isEmpty(content)) {
            handEmptyScanResult(gitlabPipelineId, startDate, endDate, appServiceId);
            return;
        }
        imageScanResultVOS = JsonHelper.unmarshalByJackson(content, new TypeReference<List<ImageScanResultVO>>() {
        });
        if (CollectionUtils.isEmpty(imageScanResultVOS)) {
            handEmptyScanResult(gitlabPipelineId, startDate, endDate, appServiceId);
            return;
        }
        //查询数据库是否存在，不存在则插入
        insertOrUpdateData(gitlabPipelineId, startDate, endDate, jobName, imageScanResultVOS, appServiceId);
        //检查门禁条件
        checkSecurityCondition(gitlabPipelineId, jobId);
    }

    private void checkSecurityCondition(Long gitlabPipelineId, Long jobId) {
        if (!Objects.isNull(jobId) && jobId > 0) {
            List<DevopsCiStepDTO> devopsCiStepDTOList = devopsCiStepService.listByJobId(jobId);
            if (CollectionUtils.isEmpty(devopsCiStepDTOList)) {
                return;
            }
            List<DevopsCiStepDTO> collect = devopsCiStepDTOList.stream().filter(v -> DevopsCiStepTypeEnum.DOCKER_BUILD.value().equals(v.getType())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect)) {
                return;
            }
            DevopsCiStepDTO devopsCiStepDTO = collect.get(0);
            DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO = devopsCiDockerBuildConfigService.queryByStepId(devopsCiStepDTO.getId());

            check(gitlabPipelineId, devopsCiDockerBuildConfigDTO);

        }
    }

    private void insertOrUpdateData(Long gitlabPipelineId, Date startDate, Date endDate, String jobName, List<ImageScanResultVO> imageScanResultVOS, Long appServiceId) {
        DevopsImageScanResultDTO existScanResult = new DevopsImageScanResultDTO();
        existScanResult.setAppServiceId(appServiceId);
        existScanResult.setGitlabPipelineId(gitlabPipelineId);
        existScanResult.setJobName(jobName);
        if (devopsImageScanResultMapper.selectCount(existScanResult) > 0) {
            //批量更新
            devopsImageScanResultMapper.updateScanDate(startDate, endDate, appServiceId, gitlabPipelineId, jobName);
        } else {
            //批量插入
            batchInsertData(gitlabPipelineId, startDate, endDate, jobName, imageScanResultVOS, appServiceId);
        }
    }

    private void batchInsertData(Long gitlabPipelineId, Date startDate, Date endDate, String jobName, List<ImageScanResultVO> imageScanResultVOS, Long appServiceId) {
        ImageScanResultVO imageScanResultVO = imageScanResultVOS.get(0);
        List<VulnerabilitieVO> vulnerabilities = imageScanResultVO.getVulnerabilities();
        List<DevopsImageScanResultDTO> devopsImageScanResultDTOS = new ArrayList<>();

        vulnerabilities.forEach(vulnerabilitieVO -> {
            DevopsImageScanResultDTO devopsImageScanResultDTO = new DevopsImageScanResultDTO();
            devopsImageScanResultDTO.setTarget(imageScanResultVO.getTarget());
            devopsImageScanResultDTO.setAppServiceId(appServiceId);
            devopsImageScanResultDTO.setJobName(jobName);
            devopsImageScanResultDTO.setTarget(imageScanResultVO.getTarget());
            BeanUtils.copyProperties(vulnerabilitieVO, devopsImageScanResultDTO);
            devopsImageScanResultDTO.setStartDate(startDate);
            devopsImageScanResultDTO.setEndDate(endDate);
            devopsImageScanResultDTO.setGitlabPipelineId(gitlabPipelineId);
            devopsImageScanResultDTOS.add(devopsImageScanResultDTO);
        });
        devopsImageScanResultMapper.insertScanResultBatch(devopsImageScanResultDTOS);
    }

    private void handEmptyScanResult(Long gitlabPipelineId, Date startDate, Date endDate, Long appServiceId) {
        DevopsImageScanResultDTO devopsImageScanResultDTO = new DevopsImageScanResultDTO();
        devopsImageScanResultDTO.setGitlabPipelineId(gitlabPipelineId);
        devopsImageScanResultDTO.setStartDate(startDate);
        devopsImageScanResultDTO.setEndDate(endDate);
        devopsImageScanResultDTO.setAppServiceId(appServiceId);

        DevopsImageScanResultDTO scanResultDTO = new DevopsImageScanResultDTO();
        scanResultDTO.setGitlabPipelineId(gitlabPipelineId);
        scanResultDTO.setAppServiceId(appServiceId);
        DevopsImageScanResultDTO resultDTO = devopsImageScanResultMapper.selectOne(scanResultDTO);
        if (Objects.isNull(resultDTO)) {
            devopsImageScanResultMapper.insert(devopsImageScanResultDTO);
        } else {
            BeanUtils.copyProperties(devopsImageScanResultDTO, resultDTO);
            devopsImageScanResultMapper.updateByPrimaryKeySelective(resultDTO);
        }
    }

    private void check(Long gitlabPipelineId, DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO) {
        if (Boolean.TRUE.equals(devopsCiDockerBuildConfigDTO.getSecurityControl())) {

            DevopsImageScanResultDTO devopsImageScanResultDTO = new DevopsImageScanResultDTO();
            devopsImageScanResultDTO.setGitlabPipelineId(gitlabPipelineId);
            List<DevopsImageScanResultDTO> devopsImageScanResultDTOS = devopsImageScanResultMapper.select(devopsImageScanResultDTO);
            if (CollectionUtils.isEmpty(devopsImageScanResultDTOS)) {
                return;
            }
            switch (ImageSecurityEnum.valueOf(devopsCiDockerBuildConfigDTO.getSeverity())) {
                case HIGH:
                    Integer highCount = getHighCount(devopsImageScanResultDTOS);
                    securityMonitor(highCount, devopsCiDockerBuildConfigDTO);
                    break;
                case CRITICAL:
                    Integer criticalCount = getCriticalCount(devopsImageScanResultDTOS);
                    securityMonitor(criticalCount, devopsCiDockerBuildConfigDTO);
                    break;
                case MEDIUM:
                    Integer mediumCount = getMediumCount(devopsImageScanResultDTOS);
                    securityMonitor(mediumCount, devopsCiDockerBuildConfigDTO);
                    break;
                case LOW:
                    Integer lowCount = getLowCount(devopsImageScanResultDTOS);
                    securityMonitor(lowCount, devopsCiDockerBuildConfigDTO);
                    break;
                default:
                    throw new DevopsCiInvalidException("security level not exist: {}", devopsCiDockerBuildConfigDTO.getSeverity());
            }
        }
    }

    private Integer getLowCount(List<DevopsImageScanResultDTO> devopsImageScanResultDTOS) {
        ImageScanResultVO imageScanResultVO = new ImageScanResultVO();
        handLoophole(devopsImageScanResultDTOS, imageScanResultVO);
        return imageScanResultVO.getHighCount() + imageScanResultVO.getCriticalCount() + imageScanResultVO.getMediumCount() + imageScanResultVO.getLowCount();
    }

    private Integer getMediumCount(List<DevopsImageScanResultDTO> devopsImageScanResultDTOS) {
        ImageScanResultVO imageScanResultVO = new ImageScanResultVO();
        handLoophole(devopsImageScanResultDTOS, imageScanResultVO);
        return imageScanResultVO.getHighCount() + imageScanResultVO.getCriticalCount() + imageScanResultVO.getMediumCount();
    }


    private Integer getHighCount(List<DevopsImageScanResultDTO> devopsImageScanResultDTOS) {
        ImageScanResultVO imageScanResultVO = new ImageScanResultVO();
        handLoophole(devopsImageScanResultDTOS, imageScanResultVO);
        return imageScanResultVO.getHighCount() + imageScanResultVO.getCriticalCount();
    }

    private Integer getCriticalCount(List<DevopsImageScanResultDTO> devopsImageScanResultDTOS) {
        ImageScanResultVO imageScanResultVO = new ImageScanResultVO();
        handLoophole(devopsImageScanResultDTOS, imageScanResultVO);
        return imageScanResultVO.getCriticalCount();
    }


    @Override
    public ImageScanResultVO queryImageInfo(Long projectId, Long gitlabPipelineId) {
        DevopsImageScanResultDTO devopsImageScanResultDTO = new DevopsImageScanResultDTO();
        devopsImageScanResultDTO.setGitlabPipelineId(gitlabPipelineId);
        List<DevopsImageScanResultDTO> devopsImageScanResultDTOS = devopsImageScanResultMapper.select(devopsImageScanResultDTO);
        ImageScanResultVO imageScanResultVO = new ImageScanResultVO();
        if (CollectionUtils.isEmpty(devopsImageScanResultDTOS)) {
            return imageScanResultVO;
        }
        //筛选各个级别的漏洞
        handLoophole(devopsImageScanResultDTOS, imageScanResultVO);

        if (imageScanResultVO.getUnknownCount() > 0) {
            imageScanResultVO.setLevel(ImageSecurityEnum.UNKNOWN.getValue());
        }
        if (imageScanResultVO.getLowCount() > 0) {
            imageScanResultVO.setLevel(ImageSecurityEnum.LOW.getValue());
        }
        if (imageScanResultVO.getMediumCount() > 0) {
            imageScanResultVO.setLevel(ImageSecurityEnum.MEDIUM.getValue());
        }
        if (imageScanResultVO.getHighCount() > 0) {
            imageScanResultVO.setLevel(ImageSecurityEnum.HIGH.getValue());
        }
        if (imageScanResultVO.getCriticalCount() > 0) {
            imageScanResultVO.setLevel(ImageSecurityEnum.CRITICAL.getValue());
        }
        DevopsImageScanResultDTO scanResultDTO = devopsImageScanResultDTOS.get(0);
        imageScanResultVO.setSpendTime(scanResultDTO.getEndDate().getTime() - scanResultDTO.getStartDate().getTime());
        imageScanResultVO.setStartDate(scanResultDTO.getStartDate());
        return imageScanResultVO;
    }

    @Override
    @Transactional
    public void deleteByAppServiceId(Long appServiceId) {
        Assert.notNull(appServiceId, ResourceCheckConstant.ERROR_APP_SERVICE_ID_IS_NULL);

        DevopsImageScanResultDTO devopsImageScanResultDTO = new DevopsImageScanResultDTO();
        devopsImageScanResultDTO.setAppServiceId(appServiceId);

        devopsImageScanResultMapper.delete(devopsImageScanResultDTO);
    }

    private void handLoophole(List<DevopsImageScanResultDTO> devopsImageScanResultDTOS, ImageScanResultVO imageScanResultVO) {
        List<DevopsImageScanResultDTO> imageScanUnknown = devopsImageScanResultDTOS.stream().filter(devopsImageScanResultDTO1 -> StringUtils.equalsIgnoreCase(devopsImageScanResultDTO1.getSeverity(), ImageSecurityEnum.UNKNOWN.getValue())).collect(Collectors.toList());
        imageScanResultVO.setUnknownCount(CollectionUtils.isEmpty(imageScanUnknown) ? 0 : imageScanUnknown.size());
        List<DevopsImageScanResultDTO> imageScanLow = devopsImageScanResultDTOS.stream().filter(devopsImageScanResultDTO1 -> StringUtils.equalsIgnoreCase(devopsImageScanResultDTO1.getSeverity(), ImageSecurityEnum.LOW.getValue())).collect(Collectors.toList());
        imageScanResultVO.setLowCount(CollectionUtils.isEmpty(imageScanLow) ? 0 : imageScanLow.size());
        List<DevopsImageScanResultDTO> imageScanMedium = devopsImageScanResultDTOS.stream().filter(devopsImageScanResultDTO1 -> StringUtils.equalsIgnoreCase(devopsImageScanResultDTO1.getSeverity(), ImageSecurityEnum.MEDIUM.getValue())).collect(Collectors.toList());
        imageScanResultVO.setMediumCount(CollectionUtils.isEmpty(imageScanMedium) ? 0 : imageScanMedium.size());
        List<DevopsImageScanResultDTO> imageScanHigh = devopsImageScanResultDTOS.stream().filter(devopsImageScanResultDTO1 -> StringUtils.equalsIgnoreCase(devopsImageScanResultDTO1.getSeverity(), ImageSecurityEnum.HIGH.getValue())).collect(Collectors.toList());
        imageScanResultVO.setHighCount(CollectionUtils.isEmpty(imageScanHigh) ? 0 : imageScanHigh.size());
        List<DevopsImageScanResultDTO> imageScanCritical = devopsImageScanResultDTOS.stream().filter(devopsImageScanResultDTO1 -> StringUtils.equalsIgnoreCase(devopsImageScanResultDTO1.getSeverity(), ImageSecurityEnum.CRITICAL.getValue())).collect(Collectors.toList());
        imageScanResultVO.setCriticalCount(CollectionUtils.isEmpty(imageScanCritical) ? 0 : imageScanCritical.size());
    }

    @Override
    public Page<DevopsImageScanResultVO> pageByOptions(Long projectId, Long gitlabPipelineId, PageRequest pageRequest, String options) {
        Page<DevopsImageScanResultDTO> devopsImageScanResultDTOPage = PageHelper.doPageAndSort(pageRequest, () -> devopsImageScanResultMapper.pageByOptions(gitlabPipelineId, options));
        Page<DevopsImageScanResultVO> devopsImageScanResultVOS = ConvertUtils.convertPage(devopsImageScanResultDTOPage, DevopsImageScanResultVO.class);
        List<DevopsImageScanResultVO> imageScanResultVOS = devopsImageScanResultVOS.getContent().stream().filter(devopsImageScanResultVO -> !StringUtils.isEmpty(devopsImageScanResultVO.getVulnerabilityCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(imageScanResultVOS)) {
            return new Page<>();
        }
        return devopsImageScanResultVOS;
    }

}
