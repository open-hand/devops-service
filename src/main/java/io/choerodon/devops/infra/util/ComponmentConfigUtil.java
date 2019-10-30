package io.choerodon.devops.infra.util;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * lihao
 * 获取组件版本配置工具类
 */
public class ComponmentConfigUtil {
    private static final String COMPONMENT_CONFIG_FILE_FORMAT = "/componment/%s.yml";
    private static Map<String, AppServiceVersionDTO> componmentVersionConfigs=new HashMap<>();

    private ComponmentConfigUtil() {
    }

    public static AppServiceVersionDTO getComponmentVersion(String componmentName) {
        AppServiceVersionDTO appServiceVersionDTO = componmentVersionConfigs.get(componmentName);
        if (appServiceVersionDTO != null) {
            return appServiceVersionDTO;
        } else {
            InputStream inputStream = Optional.ofNullable(ComponmentConfigUtil.class.getResourceAsStream(String.format(COMPONMENT_CONFIG_FILE_FORMAT, componmentName)))
                    .orElseThrow(() -> new CommonException("error.componment.config.file.not.exist"));

            Yaml yaml = new Yaml();
            Map<String, String> config = yaml.load(inputStream);
            appServiceVersionDTO = new AppServiceVersionDTO();
            appServiceVersionDTO.setImage(Optional.ofNullable(config.get("image"))
                    .orElseThrow(() -> new CommonException("error.componment.config.property.not.exist","image")));
            appServiceVersionDTO.setVersion(Optional.ofNullable(config.get("version"))
                    .orElseThrow(() -> new CommonException("error.componment.config.property.not.exist","version")));
            appServiceVersionDTO.setRepository(Optional.ofNullable(config.get("image"))
                    .orElseThrow(() -> new CommonException("error.componment.config.property.not.exist","image")));
            appServiceVersionDTO.setValues(Optional.ofNullable(config.get("values"))
                    .orElseThrow(() -> new CommonException("error.componment.config.property.not.exist","values")));
            componmentVersionConfigs.put(componmentName, appServiceVersionDTO);
            return appServiceVersionDTO;
        }
    }
}
