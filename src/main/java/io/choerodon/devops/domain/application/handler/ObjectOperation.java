package io.choerodon.devops.domain.application.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.infra.common.util.SkipNullRepresenterUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.ResourceType;
import io.kubernetes.client.models.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

public class ObjectOperation<T> {

    public static final String UPDATE = "update";
    private static final String C7NTAG = "!!io.choerodon.devops.domain.application.valueobject.C7nHelmRelease";
    private static final String INGTAG = "!!io.kubernetes.client.models.V1beta1Ingress";
    private static final String SVCTAG = "!!io.kubernetes.client.models.V1Service";
    private static final String CERTTAG = "!!io.choerodon.devops.domain.application.valueobject.C7nCertification";
    private static final String CONFIGMAPTAG = "!!io.kubernetes.client.models.V1ConfigMap";
    private static final String SECRET = "!!io.kubernetes.client.models.V1Secret";
    private static final String ENDPOINTS = "!!io.kubernetes.client.models.V1Endpoints";

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
                                       Long userId, Long objectId, String objectType, V1Endpoints v1Endpoints, Boolean deleteCert, Long envId, String filePath) {
        GitlabRepository gitlabRepository = ApplicationContextHelper.getSpringFactory().getBean(GitlabRepository.class);
        Tag tag = new Tag(type.getClass().toString());
        Yaml yaml = getYamlObject(tag, true);
        String endpointContent = null;
        if (v1Endpoints != null) {
            Yaml newYaml = getYamlObject(new Tag(v1Endpoints.getClass().toString()), true);
            endpointContent = newYaml.dump(v1Endpoints).replace(ENDPOINTS, "---");
        }
        String content = yaml.dump(type).replace("!<" + tag.getValue() + ">", "---");
        if (endpointContent != null) {
            content = content + "\n" + endpointContent;
        }
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
            gitlabRepository.updateFile(gitlabEnvProjectId, devopsEnvFileResourceE.getFilePath(), getUpdateContent(type, deleteCert,
                    endpointContent, devopsEnvFileResourceE.getFilePath(), objectType, filePath, operationType),
                    "UPDATE FILE", TypeUtil.objToInteger(userId));
        }
    }

    private Yaml getYamlObject(Tag tag, Boolean isTag) {
        SkipNullRepresenterUtil skipNullRepresenter = null;
        if (isTag) {
            skipNullRepresenter = new SkipNullRepresenterUtil();
            skipNullRepresenter.addClassTag(type.getClass(), tag);
        }
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowReadOnlyProperties(true);
        return skipNullRepresenter == null ? new Yaml(options) : new Yaml(skipNullRepresenter, options);
    }

    public String getUpdateContent(T t, Boolean deleteCert, String content, String filePath, String objectType, String path, String operationType) {
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
                        handleIngress(t, deleteCert, objectType, operationType, resultBuilder, jsonObject);
                        break;
//                    case "Service":
//                        handleService(t, content, objectType, operationType, resultBuilder, jsonObject);
//                        break;
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
                        handleCustom(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                }
            }
            return resultBuilder.toString();
        } catch (FileNotFoundException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    private void handleService(T t, String content, String objectType, String operationType, StringBuilder resultBuilder, JSONObject jsonObject) {
        Yaml yaml3 = new Yaml();
        V1Service v1Service = yaml3.loadAs(jsonObject.toJSONString(), V1Service.class);
        V1Service newV1Service = new V1Service();
        if (objectType.equals(ResourceType.SERVICE.getType()) && v1Service.getMetadata().getName().equals(((V1Service) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                Map<String, String> oldAnnotations = v1Service.getMetadata().getAnnotations();
                newV1Service = (V1Service) t;
                Map<String, String> newAnnotations = newV1Service.getMetadata().getAnnotations();
                oldAnnotations.forEach((key, value) -> {
                    if (!key.equals("choerodon.io/network-service-instances") && !key.equals("choerodon.io/network-service-app")) {
                        newAnnotations.put(key, value);
                    }
                });
                newV1Service.getMetadata().setAnnotations(newAnnotations);
            } else {
                return;
            }
        }
        Tag tag3 = new Tag(SVCTAG);
        resultBuilder.append("\n").append(getYamlObject(tag3, true).dump(newV1Service).replace(SVCTAG, "---"));
        if (content != null) {
            resultBuilder.append("\n").append(content);
        }
    }

    private void handleIngress(T t, Boolean deleteCert, String objectType, String operationType, StringBuilder resultBuilder, JSONObject jsonObject) {
        Yaml yaml2 = new Yaml();
        V1beta1Ingress v1beta1Ingress = yaml2.loadAs(jsonObject.toJSONString(), V1beta1Ingress.class);
        V1beta1Ingress newV1beta1Ingress = new V1beta1Ingress();

        if (objectType.equals(ResourceType.INGRESS.getType()) && v1beta1Ingress.getMetadata().getName().equals(((V1beta1Ingress) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                newV1beta1Ingress = (V1beta1Ingress) t;
                newV1beta1Ingress.getMetadata().setAnnotations(v1beta1Ingress.getMetadata().getAnnotations());
                if (!deleteCert) {
                    if(!newV1beta1Ingress.getSpec().getTls().isEmpty()) {
                        newV1beta1Ingress.getSpec().setTls(newV1beta1Ingress.getSpec().getTls());
                    }else {
                        newV1beta1Ingress.getSpec().setTls(v1beta1Ingress.getSpec().getTls());
                    }
                }
            } else {
                return;
            }
        }
        Tag tag2 = new Tag(INGTAG);
        resultBuilder.append("\n").append(getYamlObject(tag2, true).dump(newV1beta1Ingress).replace(INGTAG, "---"));
    }

    private void handleC7nHelmRelease(T t, String objectType, String operationType, StringBuilder resultBuilder, JSONObject jsonObject) {
        Yaml yaml1 = new Yaml();
        C7nHelmRelease c7nHelmRelease = yaml1.loadAs(jsonObject.toJSONString(), C7nHelmRelease.class);
        if (objectType.equals(ResourceType.C7NHELMRELEASE.getType()) && c7nHelmRelease.getMetadata().getName().equals(((C7nHelmRelease) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                c7nHelmRelease = (C7nHelmRelease) t;
            } else {
                return;
            }
        }
        Tag tag1 = new Tag(C7NTAG);
        resultBuilder.append("\n").append(getYamlObject(tag1, true).dump(c7nHelmRelease).replace(C7NTAG, "---"));
    }


    private void handleC7nCertification(T t, String objectType, String operationType, StringBuilder resultBuilder, JSONObject jsonObject) {
        Yaml yaml4 = new Yaml();
        C7nCertification c7nCertification = yaml4.loadAs(jsonObject.toJSONString(), C7nCertification.class);
        if (objectType.equals(ResourceType.CERTIFICATE.getType()) && c7nCertification.getMetadata().getName().equals(((C7nCertification) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                c7nCertification = (C7nCertification) t;
            } else {
                return;
            }
        }
        Tag tag1 = new Tag(CERTTAG);
        resultBuilder.append("\n").append(getYamlObject(tag1, true).dump(c7nCertification).replace(CERTTAG, "---"));
    }

    private void handleConfigMap(T t, String objectType, String operationType, StringBuilder resultBuilder, JSONObject jsonObject) {
        Yaml yaml5 = new Yaml();
        V1ConfigMap v1ConfigMap = yaml5.loadAs(jsonObject.toJSONString(), V1ConfigMap.class);
        V1ConfigMap newV1ConfigMap = new V1ConfigMap();
        if (objectType.equals(ResourceType.CONFIGMAP.getType()) && v1ConfigMap.getMetadata().getName().equals(((V1ConfigMap) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                newV1ConfigMap = (V1ConfigMap) t;
                newV1ConfigMap.getMetadata().setAnnotations(v1ConfigMap.getMetadata().getAnnotations());
            } else {
                return;
            }
        }
        Tag tag1 = new Tag(CONFIGMAPTAG);
        resultBuilder.append("\n").append(getYamlObject(tag1, true).dump(newV1ConfigMap).replace(CONFIGMAPTAG, "---"));
    }

    private void handleSecret(T t, String objectType, String operationType, StringBuilder resultBuilder,
                              JSONObject jsonObject) {
        Yaml yaml6 = new Yaml();
        V1Secret v1Secret = yaml6.loadAs(jsonObject.toJSONString(), V1Secret.class);
        V1Secret newV1Secret = new V1Secret();
        if (objectType.equals(ResourceType.SECRET.getType()) && v1Secret.getMetadata().getName()
                .equals(((V1Secret) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                newV1Secret = (V1Secret) t;
                newV1Secret.getMetadata().setAnnotations(v1Secret.getMetadata().getAnnotations());
            } else {
                return;
            }
        }
        Tag tag1 = new Tag(SECRET);
        resultBuilder.append("\n").append(getYamlObject(tag1, true).dump(newV1Secret).replace(SECRET, "---"));
    }

    private void handleCustom(T t, String objectType, String operationType, StringBuilder resultBuilder,
                              JSONObject jsonObject) {
        Yaml yaml7 = new Yaml();
        Object customResource = yaml7.load(jsonObject.toJSONString());
        if (objectType.equals(ResourceType.CUSTOM.getType())) {
            String oldResourceName = ((LinkedHashMap) (((Map<String, Object>) customResource).get("metadata"))).get("name").toString();
            String newResourceName = ((LinkedHashMap) (((Map<String, Object>) t).get("metadata"))).get("name").toString();
            if (oldResourceName.equals(newResourceName)) {
                if (operationType.equals(UPDATE)) {
                    customResource = t;
                } else {
                    return;
                }
            }
        }
        resultBuilder.append("---").append("\n").append(getYamlObject(null, false).dump(customResource)).append("\n");
    }
}
