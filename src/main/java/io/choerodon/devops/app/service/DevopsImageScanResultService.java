package io.choerodon.devops.app.service;

import java.util.Date;
import org.springframework.web.multipart.MultipartFile;


/**
 * Created by wangxiang on 2021/3/25
 */
public interface DevopsImageScanResultService {

    void resolveImageScanJson(Long jobId, Long gitlabPipelineId, Date startDate,Date endDate, MultipartFile file);
}
