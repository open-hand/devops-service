package io.choerodon.devops.app.eventhandler.pipeline;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.api.vo.pipeline.DevopsCiUnitTestResultVO;
import io.choerodon.devops.infra.enums.CiUnitTestTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/24 11:16
 */
public interface CiUnitTestReportHandler {

    /**
     * 解析测试报告，读取测试用例总数、失败用例总数、跳过用例总数
     *
     * @param file 测试报告文件
     * @return 解析结果
     */
    DevopsCiUnitTestResultVO analyseReport(MultipartFile file);

    CiUnitTestTypeEnum getType();

    String uploadReport(Long devopsPipelineId, Long gitlabPipelineId, String jobName, String type, MultipartFile file);
}
