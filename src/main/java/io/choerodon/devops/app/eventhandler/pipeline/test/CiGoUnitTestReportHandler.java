package io.choerodon.devops.app.eventhandler.pipeline.test;

import java.io.IOException;
import java.util.List;

import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.hzero.boot.file.FileClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.pipeline.DevopsCiUnitTestResultVO;
import io.choerodon.devops.infra.enums.CiUnitTestTypeEnum;

@Component
public class CiGoUnitTestReportHandler implements CiUnitTestReportHandler {
    @Autowired
    private FileClient fileClient;

    @Override
    public CiUnitTestTypeEnum getType() {
        return CiUnitTestTypeEnum.GO_UNIT_TEST;
    }

    @Override
    public DevopsCiUnitTestResultVO analyseReport(MultipartFile file) {
        XPathParser parser;
        try {
            parser = new XPathParser(new String(file.getBytes()));
        } catch (IOException e) {
            throw new CommonException(e);
        }
        List<XNode> testSuites = parser.evalNodes("/testsuites/testsuite");
        long totalTests = 0;
        long totalFailures = 0;
        for (XNode xNode : testSuites) {
            totalTests += xNode.getLongAttribute("tests");
            totalFailures += xNode.getLongAttribute("failures");
        }
        return new DevopsCiUnitTestResultVO(totalTests, totalTests - totalFailures, totalFailures, 0L);
    }

    @Override
    public String uploadReport(Long devopsPipelineId, Long gitlabPipelineId, String jobName, String type, MultipartFile file) {
        String fileName = "Golang单元测试报告-" + "(" + jobName + ")";
        String directory = "pipeline/" + gitlabPipelineId;
        // 上传测试报告
        return fileClient.uploadFile(0L, "devops-service", directory, fileName, file);
    }
}
