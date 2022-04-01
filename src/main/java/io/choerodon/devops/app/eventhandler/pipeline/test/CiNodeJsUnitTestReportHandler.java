package io.choerodon.devops.app.eventhandler.pipeline.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.hzero.boot.file.FileClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.pipeline.CiNodeJsReportVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiUnitTestResultVO;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.enums.CiUnitTestTypeEnum;
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
    public DevopsCiUnitTestResultVO analyseReport(MultipartFile multfile) {
        // 获取文件名
        try {
            TarArchiveInputStream fin = new TarArchiveInputStream(multfile.getInputStream());
            TarArchiveEntry entry;
            // 将 tar 文件解压到 extractPath 目录下
            String resultJson = "";
            while ((entry = fin.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                if (MOCHAWESOME_JSON_PATH.equals(entry.getName())) {
                    resultJson = IOUtils.toString(fin, StandardCharsets.UTF_8);
                    break;
                }
            }
            if (!StringUtils.hasText(resultJson)) {
                throw new CommonException("report.format.invalid");
            }
            CiNodeJsReportVO ciNodeJsReportVO = JsonHelper.unmarshalByJackson(resultJson, CiNodeJsReportVO.class);
            return ciNodeJsReportVO.getStats();

        } catch (IOException e) {
            throw new CommonException("analyse.report.failed");
        }
    }

    @Override
    public String uploadReport(Long devopsPipelineId, Long gitlabPipelineId, String jobName, String type, MultipartFile file) {
        String fileName = "NodeJs单元测试报告-" + "(" + jobName + ")";
        String directory = "pipeline/" + gitlabPipelineId;
        // 上传测试报告
        return fileClient.uploadFile(0L, MiscConstants.DEVOPS_SERVICE_BUCKET_NAME, directory, fileName, file);
    }

    @Override
    public CiUnitTestTypeEnum getType() {
        return CiUnitTestTypeEnum.NODE_JS_UNIT_TEST;
    }


}
