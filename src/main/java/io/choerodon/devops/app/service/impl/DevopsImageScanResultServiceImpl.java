package io.choerodon.devops.app.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.DevopsImageScanResultVO;
import io.choerodon.devops.api.vo.ImageScanResultVO;
import io.choerodon.devops.api.vo.VulnerabilitieVO;
import io.choerodon.devops.app.service.DevopsImageScanResultService;
import io.choerodon.devops.infra.dto.DevopsImageScanResultDTO;
import io.choerodon.devops.infra.mapper.DevopsImageScanResultMapper;
import io.choerodon.devops.infra.util.JsonHelper;

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

}
