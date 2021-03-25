package io.choerodon.devops.app.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsImageScanResultVO;
import io.choerodon.devops.api.vo.ImageScanResultVO;
import io.choerodon.devops.api.vo.VulnerabilitieVO;
import io.choerodon.devops.app.service.DevopsImageScanResultService;
import io.choerodon.devops.infra.dto.DevopsImageScanResultDTO;
import io.choerodon.devops.infra.enums.ImageSecurityEnum;
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


    @Override
    public void resolveImageScanJson(Long jobId, Long gitlabPipelineId, Date startDate, Date endDate, MultipartFile file) {
        LOGGER.debug("startDate:{},endDate:{}", startDate, endDate);
        String content = null;
        try {
            content = new String(file.getBytes(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (StringUtils.isEmpty(content)) {
            return;
        }

        LOGGER.debug("trivy scan result:{}", content);
        List<ImageScanResultVO> imageScanResultVOS = JsonHelper.unmarshalByJackson(content, new TypeReference<List<ImageScanResultVO>>() {
        });
        if (CollectionUtils.isEmpty(imageScanResultVOS)) {
            return;
        }
        ImageScanResultVO imageScanResultVO = imageScanResultVOS.get(0);
        List<VulnerabilitieVO> vulnerabilities = imageScanResultVO.getVulnerabilities();
        vulnerabilities.forEach(vulnerabilitieVO -> {
            DevopsImageScanResultDTO devopsImageScanResultDTO = new DevopsImageScanResultDTO();
            devopsImageScanResultDTO.setTarget(imageScanResultVO.getTarget());
            BeanUtils.copyProperties(vulnerabilitieVO, devopsImageScanResultDTO);
            devopsImageScanResultDTO.setStartDate(startDate);
            devopsImageScanResultDTO.setEndDate(endDate);
            devopsImageScanResultDTO.setJobId(jobId);
            devopsImageScanResultDTO.setGitlabPipelineId(gitlabPipelineId);
            DevopsImageScanResultDTO scanResultDTO = new DevopsImageScanResultDTO();
            scanResultDTO.setJobId(jobId);
            scanResultDTO.setGitlabPipelineId(gitlabPipelineId);
            DevopsImageScanResultDTO resultDTO = devopsImageScanResultMapper.selectOne(scanResultDTO);
            if (Objects.isNull(resultDTO)) {
                devopsImageScanResultMapper.insert(devopsImageScanResultDTO);
            } else {
                BeanUtils.copyProperties(devopsImageScanResultDTO, resultDTO);
                devopsImageScanResultMapper.updateByPrimaryKeySelective(resultDTO);
            }
        });

    }


    @Override
    public ImageScanResultVO queryImageInfo(Long projectId, Long gitlabPipelineId, Long jobId) {
        DevopsImageScanResultDTO devopsImageScanResultDTO = new DevopsImageScanResultDTO();
        devopsImageScanResultDTO.setGitlabPipelineId(gitlabPipelineId);
        devopsImageScanResultDTO.setJobId(jobId);
        List<DevopsImageScanResultDTO> devopsImageScanResultDTOS = devopsImageScanResultMapper.select(devopsImageScanResultDTO);
        ImageScanResultVO imageScanResultVO = new ImageScanResultVO();
        if (CollectionUtils.isEmpty(devopsImageScanResultDTOS)) {
            return imageScanResultVO;
        }
        //筛选各个级别的漏洞
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

        if (imageScanResultVO.getUnknownCount() > 0) {
            imageScanResultVO.setLevel(ImageSecurityEnum.UNKNOWN.getValue());
        } else if (imageScanResultVO.getLowCount() > 0) {
            imageScanResultVO.setLevel(ImageSecurityEnum.LOW.getValue());
        } else if (imageScanResultVO.getMediumCount() > 0) {
            imageScanResultVO.setLevel(ImageSecurityEnum.MEDIUM.getValue());
        } else if (imageScanResultVO.getHighCount() > 0) {
            imageScanResultVO.setLevel(ImageSecurityEnum.HIGH.getValue());
        } else if (imageScanResultVO.getCriticalCount() > 0) {
            imageScanResultVO.setLevel(ImageSecurityEnum.CRITICAL.getValue());
        }
        DevopsImageScanResultDTO scanResultDTO = devopsImageScanResultDTOS.get(0);
        imageScanResultVO.setSpendTime(scanResultDTO.getEndDate().getTime() - scanResultDTO.getStartDate().getTime());
        imageScanResultVO.setStartDate(scanResultDTO.getStartDate());
        return imageScanResultVO;
    }

    @Override
    public Page<DevopsImageScanResultVO> pageByOptions(Long projectId, Long gitlabPipelineId, Long jobId, PageRequest pageRequest, String options) {
        Page<DevopsImageScanResultDTO> devopsImageScanResultDTOPage = PageHelper.doPageAndSort(pageRequest, () -> devopsImageScanResultMapper.pageByOptions(gitlabPipelineId, jobId, options));
        Page<DevopsImageScanResultVO> devopsImageScanResultVOS = ConvertUtils.convertPage(devopsImageScanResultDTOPage, DevopsImageScanResultVO.class);
        return devopsImageScanResultVOS;
    }

}
