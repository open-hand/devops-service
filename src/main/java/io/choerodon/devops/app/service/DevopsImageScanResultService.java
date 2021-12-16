package io.choerodon.devops.app.service;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsImageScanResultVO;
import io.choerodon.devops.api.vo.ImageScanResultVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


/**
 * Created by wangxiang on 2021/3/25
 */
public interface DevopsImageScanResultService {

    void resolveImageScanJson(Long gitlabPipelineId, Long jobId, Date startDate, Date endDate, MultipartFile file, String token, String jobName);

    Page<DevopsImageScanResultVO> pageByOptions(Long projectId, Long gitlabPipelineId, PageRequest pageRequest, String options);

    ImageScanResultVO queryImageInfo(Long projectId, Long gitlabPipelineId);
}
