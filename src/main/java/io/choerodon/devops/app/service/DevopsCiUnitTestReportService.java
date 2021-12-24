package io.choerodon.devops.app.service;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.infra.dto.DevopsCiUnitTestReportDTO;

/**
 * 单元测试报告(DevopsCiUnitTestReport)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-24 10:30:48
 */
public interface DevopsCiUnitTestReportService {

    /**
     * 上传测试报告
     *
     * @param gitlabPipelineId
     * @param jobName
     * @param token
     * @param type
     * @param file
     */
    void uploadUnitTest(Long gitlabPipelineId, String jobName, String token, String type, MultipartFile file);


    /**
     * 根据唯一索引查询测试报告
     *
     * @param devopsPipelineId
     * @param jobName
     * @param type
     * @return
     */
    DevopsCiUnitTestReportDTO queryByUniqueIndex(Long devopsPipelineId, Long gitlabPipelineId, String jobName, String type);

    void baseCreate(DevopsCiUnitTestReportDTO devopsCiUnitTestReportDTO);

    void baseUpdate(DevopsCiUnitTestReportDTO devopsCiUnitTestReportDTO);
}
