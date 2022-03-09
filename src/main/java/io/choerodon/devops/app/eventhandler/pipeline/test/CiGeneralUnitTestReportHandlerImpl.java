package io.choerodon.devops.app.eventhandler.pipeline.test;

import org.hzero.boot.file.FileClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.api.vo.pipeline.DevopsCiUnitTestResultVO;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.enums.CiUnitTestTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/2/14 14:16
 */
@Component
public class CiGeneralUnitTestReportHandlerImpl implements CiUnitTestReportHandler {
    @Autowired
    private FileClient fileClient;

    @Override
    public DevopsCiUnitTestResultVO analyseReport(MultipartFile file) {
        // do noting
        return new DevopsCiUnitTestResultVO();
    }

    @Override
    public CiUnitTestTypeEnum getType() {
        return CiUnitTestTypeEnum.GENERAL_UNIT_TEST;
    }

    @Override
    public String uploadReport(Long devopsPipelineId, Long gitlabPipelineId, String jobName, String type, MultipartFile file) {
        String fileName = "单元测试报告-" + "(" + jobName + ")";
        String directory = "pipeline/" + gitlabPipelineId;
        // 上传测试报告
        return fileClient.uploadFile(0L, MiscConstants.DEVOPS_SERVICE_BUCKET_NAME, directory, fileName, file);
    }
}
