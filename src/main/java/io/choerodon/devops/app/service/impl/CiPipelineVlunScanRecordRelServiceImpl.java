package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.api.vo.ImageScanResultVO;
import io.choerodon.devops.api.vo.vuln.VulnTargetVO;
import io.choerodon.devops.app.service.CiPipelineVlunScanRecordRelService;
import io.choerodon.devops.infra.mapper.CiPipelineVlunScanRecordRelMapper;
import io.choerodon.devops.infra.util.JsonHelper;

/**
 * ci流水线漏洞扫描记录关系表(CiPipelineVlunScanRecordRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:24
 */
@Service
public class CiPipelineVlunScanRecordRelServiceImpl implements CiPipelineVlunScanRecordRelService {
    @Autowired
    private CiPipelineVlunScanRecordRelMapper ciPipelineVlunScanRecordRelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadVulnResult(Long gitlabPipelineId, String jobName, String token, MultipartFile file) {
        //file 有可能为null,如果镜像没有漏洞这个报告文件就是空的
        String content = null;
        List<ImageScanResultVO> imageScanResultVOS = new ArrayList<>();

        try {
            JsonNode jsonNode = JsonHelper.OBJECT_MAPPER.readTree(file.getBytes());
            List<VulnTargetVO> results = JsonHelper.unmarshalByJackson(jsonNode.get("Results").toString(), new TypeReference<List<VulnTargetVO>>() {
            });

            for (VulnTargetVO result : results) {

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

