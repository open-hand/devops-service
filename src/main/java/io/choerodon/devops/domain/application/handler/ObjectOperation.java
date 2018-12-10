package io.choerodon.devops.domain.application.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1beta1Ingress;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.infra.common.util.SkipNullRepresenterUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;

public class ObjectOperation<T> {

    public static final String UPDATE = "update";
    private static final String C7NTAG = "!!io.choerodon.devops.domain.application.valueobject.C7nHelmRelease";
    private static final String INGTAG = "!!io.kubernetes.client.models.V1beta1Ingress";
    private static final String SVCTAG = "!!io.kubernetes.client.models.V1Service";
    private static final String CERTTAG = "!!io.choerodon.devops.domain.application.valueobject.C7nCertification";
    private static final String CONFIGMAPTAG = "!!io.kubernetes.client.models.V1ConfigMap";
    private static final String SECRET = "!!io.kubernetes.client.models.V1Secret";

    private T type;

    public T getType() {
        return type;
    }

    public void setType(T type) {
        this.type = type;
    }

    /**
     * operate files in GitLab
     *
     * @param fileCode           file's code
     * @param gitlabEnvProjectId Environment corresponding GitLab project ID
     * @param operationType      operation type
     * @param userId             GitLab user ID
     */
    public void operationEnvGitlabFile(String fileCode, Integer gitlabEnvProjectId, String operationType,
                                       Long userId, Long objectId, String objectType, Long envId, String filePath) {
        GitlabRepository gitlabRepository = ApplicationContextHelper.getSpringFactory().getBean(GitlabRepository.class);
        Tag tag = new Tag(type.getClass().toString());
        Yaml yaml = getYamlObject(tag);
        String content = yaml.dump(type).replace("!<" + tag.getValue() + ">", "---");
        if (operationType.equals("create")) {
            String path = fileCode + ".yaml";
            gitlabRepository.createFile(gitlabEnvProjectId, path, content,
                    "ADD FILE", TypeUtil.objToInteger(userId));

        } else {
            DevopsEnvFileResourceRepository devopsEnvFileResourceRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceRepository.class);
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(envId, objectId, objectType);
            if (devopsEnvFileResourceE == null) {
                throw new CommonException("error.fileResource.not.exist");
            }
            gitlabRepository.updateFile(gitlabEnvProjectId, devopsEnvFileResourceE.getFilePath(), getUpdateContent(type,
                    devopsEnvFileResourceE.getFilePath(), objectType, filePath, operationType),
                    "UPDATE FILE", TypeUtil.objToInteger(userId));
        }
    }

    private Yaml getYamlObject(Tag tag) {
        SkipNullRepresenterUtil skipNullRepresenter = new SkipNullRepresenterUtil();
        skipNullRepresenter.addClassTag(type.getClass(), tag);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowReadOnlyProperties(true);
        return new Yaml(skipNullRepresenter, options);
    }

    private String getUpdateContent(T t, String filePath, String objectType, String path, String operationType) {
        Yaml yaml = new Yaml();
        StringBuilder resultBuilder = new StringBuilder();
        File file = new File(String.format("%s/%s", path, filePath));
        try {
            for (Object data : yaml.loadAll(new FileInputStream(file))) {
                JSONObject jsonObject = new JSONObject((Map<String, Object>) data);
                switch (jsonObject.get("kind").toString()) {
                    case "C7NHelmRelease":
                        handleC7nHelmRelease(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "Ingress":
                        handleIngress(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "Service":
                        handleService(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "C7nCertification":
                        handleC7nCertification(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "ConfigMap":
                        handleConfigMap(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "Secret":
                        handleSecret(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    default:
                        break;
                }
            }
            return resultBuilder.toString();
        } catch (FileNotFoundException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    private void handleService(T t, String objectType, String operationType, StringBuilder resultBuilder, JSONObject jsonObject) {
        Yaml yaml3 = new Yaml();
        V1Service v1Service = yaml3.loadAs(jsonObject.toJSONString(), V1Service.class);
        if (objectType.equals("Service") && v1Service.getMetadata().getName().equals(((V1Service) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                v1Service = (V1Service) t;
            } else {
                return;
            }
        }
        Tag tag3 = new Tag(SVCTAG);
        resultBuilder.append("\n").append(getYamlObject(tag3).dump(v1Service).replace(SVCTAG, "---"));
    }

    private void handleIngress(T t, String objectType, String operationType, StringBuilder resultBuilder, JSONObject jsonObject) {
        Yaml yaml2 = new Yaml();
        V1beta1Ingress v1beta1Ingress = yaml2.loadAs(jsonObject.toJSONString(), V1beta1Ingress.class);
        if (objectType.equals("Ingress") && v1beta1Ingress.getMetadata().getName().equals(((V1beta1Ingress) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                v1beta1Ingress = (V1beta1Ingress) t;
            } else {
                return;
            }
        }
        Tag tag2 = new Tag(INGTAG);
        resultBuilder.append("\n").append(getYamlObject(tag2).dump(v1beta1Ingress).replace(INGTAG, "---"));
    }

    private void handleC7nHelmRelease(T t, String objectType, String operationType, StringBuilder resultBuilder, JSONObject jsonObject) {
        Yaml yaml1 = new Yaml();
        C7nHelmRelease c7nHelmRelease = yaml1.loadAs(jsonObject.toJSONString(), C7nHelmRelease.class);
        if (objectType.equals("C7NHelmRelease") && c7nHelmRelease.getMetadata().getName().equals(((C7nHelmRelease) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                c7nHelmRelease = (C7nHelmRelease) t;
            } else {
                return;
            }
        }
        Tag tag1 = new Tag(C7NTAG);
        resultBuilder.append("\n").append(getYamlObject(tag1).dump(c7nHelmRelease).replace(C7NTAG, "---"));
    }


    private void handleC7nCertification(T t, String objectType, String operationType, StringBuilder resultBuilder, JSONObject jsonObject) {
        Yaml yaml4 = new Yaml();
        C7nCertification c7nCertification = yaml4.loadAs(jsonObject.toJSONString(), C7nCertification.class);
        if (objectType.equals("C7nCertification") && c7nCertification.getMetadata().getName().equals(((C7nCertification) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                c7nCertification = (C7nCertification) t;
            } else {
                return;
            }
        }
        Tag tag1 = new Tag(CERTTAG);
        resultBuilder.append("\n").append(getYamlObject(tag1).dump(c7nCertification).replace(CERTTAG, "---"));
    }

    private void handleConfigMap(T t, String objectType, String operationType, StringBuilder resultBuilder, JSONObject jsonObject) {
        Yaml yaml5 = new Yaml();
        V1ConfigMap v1ConfigMap = yaml5.loadAs(jsonObject.toJSONString(), V1ConfigMap.class);
        if (objectType.equals("ConfigMap") && v1ConfigMap.getMetadata().getName().equals(((V1ConfigMap) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                v1ConfigMap = (V1ConfigMap) t;
            } else {
                return;
            }
        }
        Tag tag1 = new Tag(CONFIGMAPTAG);
        resultBuilder.append("\n").append(getYamlObject(tag1).dump(v1ConfigMap).replace(CONFIGMAPTAG, "---"));
    }

    private void handleSecret(T t, String objectType, String operationType, StringBuilder resultBuilder,
                              JSONObject jsonObject) {
        Yaml yaml6 = new Yaml();
        V1Secret v1Secret = yaml6.loadAs(jsonObject.toJSONString(), V1Secret.class);
        if (objectType.equals("Secret") && v1Secret.getMetadata().getName()
                .equals(((V1Secret) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                v1Secret = (V1Secret) t;
            } else {
                return;
            }
        }
        Tag tag1 = new Tag(SECRET);
        resultBuilder.append("\n").append(getYamlObject(tag1).dump(v1Secret).replace(SECRET, "---"));
    }
}
