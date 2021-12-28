package io.choerodon.devops.app.eventhandler.pipeline.test;

import java.io.IOException;

import org.hzero.boot.file.FileClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.pipeline.DevopsCiUnitTestResultVO;
import io.choerodon.devops.infra.enums.CiUnitTestTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/24 11:23
 */
@Component
public class CiMavenUnitTestReportHandler implements CiUnitTestReportHandler {
    @Autowired
    private FileClient fileClient;

    /**
     * 解析maven测试报告
     *
     * @param file 测试报告文件
     * @return
     */
    @Override
    public DevopsCiUnitTestResultVO analyseReport(MultipartFile file) {
        Document document = null;
        try {
            document = Jsoup.parse(new String(file.getBytes()));
        } catch (IOException e) {
            throw new CommonException(e);
        }
        Elements bodyTable = document.getElementsByClass("bodyTable");
        Element element = bodyTable.get(0);
        Element b = element.getElementsByClass("b").get(0);
        Long tests = Long.valueOf(b.getElementsByTag("td").get(0).text());
        Long errors = Long.valueOf(b.getElementsByTag("td").get(1).text());
        Long failures = Long.valueOf(b.getElementsByTag("td").get(2).text());
        Long skipped = Long.valueOf(b.getElementsByTag("td").get(3).text());

        Long passes = tests - errors - failures - skipped;

        return new DevopsCiUnitTestResultVO(tests,
                passes,
                errors + failures,
                skipped);
    }

    @Override
    public CiUnitTestTypeEnum getType() {
        return CiUnitTestTypeEnum.MAVEN_UNIT_TEST;
    }

    @Override
    public String uploadReport(Long devopsPipelineId, Long gitlabPipelineId, String jobName, String type, MultipartFile file) {
        String fileName = "Maven单元测试报告-" + "(" + jobName + ")";
        String directory = "pipeline/" + gitlabPipelineId;
        // 上传测试报告
        return fileClient.uploadFile(0L, "devops-service", directory, fileName, file);
    }
}
