package io.choerodon.devops.infra.util;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.enums.ClusterResourceType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * lihao
 * 获取组件版本配置工具类
 */
public class ComponentVersionUtil {
    private static final String ERROR_COMPONENT_CONFIG_PROPERTY_NOT_EXIST = "error.component.config.property.not.exist";
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentVersionUtil.class);
    //是否已经初始化
    private static boolean inited = false;
    private static final String COMPONENT_CONFIG_FILE_NAME = "/component/component.yml";
    private static final String COMPONENT_CONFIG_VALUE_FILE_FORMAT = "/component/%s";
    private static Map<String, AppServiceVersionDTO> componentVersionConfigsWithComponentName = new ConcurrentHashMap<>();
    private static Map<String, AppServiceVersionDTO> componentVersionConfigsWithChartName = new ConcurrentHashMap<>();


    private ComponentVersionUtil() {
    }

    public static AppServiceVersionDTO getComponentVersion(String chartName) {
        AppServiceVersionDTO appServiceVersionDTO = componentVersionConfigsWithChartName.get(chartName);
        if (appServiceVersionDTO != null) {
            return appServiceVersionDTO;
        } else if (inited) {
            return null;
        } else {
            init();
            return componentVersionConfigsWithChartName.get(chartName);
        }
    }

    public static AppServiceVersionDTO getComponentVersion(ClusterResourceType clusterResourceType) {
        String componentName = clusterResourceType.getType();
        AppServiceVersionDTO appServiceVersionDTO = componentVersionConfigsWithComponentName.get(componentName);
        if (appServiceVersionDTO != null) {
            return appServiceVersionDTO;
        } else if (inited) {
            return null;
        } else {
            init();
            return componentVersionConfigsWithComponentName.get(componentName);
        }
    }

    private static void init() {
        try {
            InputStream inputStream = Optional.ofNullable(ComponentVersionUtil.class.getResourceAsStream(COMPONENT_CONFIG_FILE_NAME))
                    .orElseThrow(() -> new CommonException("error.component.config.file.not.exist"));

            Yaml yaml = new Yaml();

            for (Object config : yaml.loadAll(inputStream)) {
                Map<String, String> componentConfig = (Map<String, String>) config;

                String valuesFileName = Optional.ofNullable(componentConfig.get("valuesFile"))
                        .orElseThrow(() -> new CommonException(ERROR_COMPONENT_CONFIG_PROPERTY_NOT_EXIST, "valuesFile"));
                InputStream valuesInputStream = Optional.ofNullable(ComponentVersionUtil.class.getResourceAsStream(String.format(COMPONENT_CONFIG_VALUE_FILE_FORMAT, valuesFileName)))
                        .orElseThrow(() -> new CommonException("error.component.config.values.not.exist", valuesFileName));
                String values = IOUtils.toString(valuesInputStream, StandardCharsets.UTF_8);

                //设置DTO属性
                AppServiceVersionDTO appServiceVersionDTO = new AppServiceVersionDTO();

                appServiceVersionDTO.setValues(values);
                appServiceVersionDTO.setImage(Optional.ofNullable(componentConfig.get("image"))
                        .orElseThrow(() -> new CommonException(ERROR_COMPONENT_CONFIG_PROPERTY_NOT_EXIST, "image")));
                appServiceVersionDTO.setVersion(Optional.ofNullable(componentConfig.get("version"))
                        .orElseThrow(() -> new CommonException(ERROR_COMPONENT_CONFIG_PROPERTY_NOT_EXIST, "version")));
                appServiceVersionDTO.setRepository(Optional.ofNullable(componentConfig.get("chartRepo"))
                        .orElseThrow(() -> new CommonException(ERROR_COMPONENT_CONFIG_PROPERTY_NOT_EXIST, "chartRepo")));
                appServiceVersionDTO.setChartName(Optional.ofNullable(componentConfig.get("chartName"))
                        .orElseThrow(() -> new CommonException(ERROR_COMPONENT_CONFIG_PROPERTY_NOT_EXIST, "chartName")));


                componentVersionConfigsWithChartName.put(appServiceVersionDTO.getChartName(), appServiceVersionDTO);
                componentVersionConfigsWithComponentName.put(Optional.ofNullable(componentConfig.get("componentName"))
                        .orElseThrow(() -> new CommonException(ERROR_COMPONENT_CONFIG_PROPERTY_NOT_EXIST, "componentName")), appServiceVersionDTO);

                valuesInputStream.close();
            }
            inputStream.close();
        } catch (IOException e) {
            LOGGER.error("Unexpected error occurred when reading the component version config files. The exception is {}.", e);
        }
    }
}
