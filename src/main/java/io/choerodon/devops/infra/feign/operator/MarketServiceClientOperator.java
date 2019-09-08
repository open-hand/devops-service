package io.choerodon.devops.infra.feign.operator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.feign.MarketServiceClient;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:38 2019/9/6
 * Description:
 */
@Component
public class MarketServiceClientOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketServiceClientOperator.class);

    @Autowired
    private MarketServiceClient marketServiceClient;

    public Boolean uploadFile(String appVersion, MultipartFile[] multipartFiles, String imageUrl) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("imageUrl", imageUrl);
            map.put("files", multipartFiles);
            return marketServiceClient.uploadFile(appVersion, map).getBody();
        } catch (Exception e) {
            throw new CommonException("error.upload.file.within", e);
        }
    }

    public Boolean updateAppPublishInfoFix(String code,
                                           String version,
                                           String marketApplicationVOStr,
                                           MultipartFile[] multipartFiles,
                                           String imageUrl) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("marketApplicationVOStr", marketApplicationVOStr);
            map.put("files", multipartFiles);
            map.put("imageUrl", imageUrl);
            return marketServiceClient.updateAppPublishInfoFix(code, version, map).getBody();
        } catch (Exception e) {
            throw new CommonException("error.upload.file.fix.version.within", e);
        }
    }
}
