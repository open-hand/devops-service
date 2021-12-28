package io.choerodon.devops.app.eventhandler.pipeline.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.hzero.boot.file.FileClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.pipeline.CiNodeJsReportVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiUnitTestResultVO;
import io.choerodon.devops.infra.enums.CiUnitTestTypeEnum;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/27 15:03
 */
@Component
public class CiNodeJsUnitTestReportHandler implements CiUnitTestReportHandler {

    private static final String MOCHAWESOME_JSON_PATH = "mochawesome-report/mochawesome.json";
    
    @Autowired
    private FileClient fileClient;

    @Override
    public DevopsCiUnitTestResultVO analyseReport(MultipartFile file) {
        File file1 = new File("report.zip");

        try {
            file.transferTo(file1);
            ZipFile zipFile = new ZipFile(file1);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            String resultJson = "";
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (MOCHAWESOME_JSON_PATH.equals(zipEntry.getName())) {
                    resultJson = IOUtils.toString(zipFile.getInputStream(zipEntry), StandardCharsets.UTF_8);
                }
            }
            if (!StringUtils.hasText(resultJson)) {
                throw new CommonException("report.format.invalid");
            }
            CiNodeJsReportVO ciNodeJsReportVO = JsonHelper.unmarshalByJackson(resultJson, CiNodeJsReportVO.class);
            return ciNodeJsReportVO.getStats();

        } catch (IOException e) {
            throw new CommonException("analyse.report.failed");
        } finally {
            FileUtil.deleteDirectory(file1);
        }
    }

    @Override
    public String uploadReport(Long devopsPipelineId, Long gitlabPipelineId, String jobName, String type, MultipartFile file) {
        String fileName = "NodeJs单元测试报告-" + "(" + jobName + ")";
        String directory = "pipeline/" + gitlabPipelineId;
        // 上传测试报告
        return fileClient.uploadFile(0L, "devops-service", directory, fileName, file);
    }

    @Override
    public CiUnitTestTypeEnum getType() {
        return CiUnitTestTypeEnum.NODE_JS_UNIT_TEST;
    }


}
