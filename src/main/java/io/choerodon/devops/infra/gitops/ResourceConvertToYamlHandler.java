package io.choerodon.devops.infra.gitops;

import static io.choerodon.devops.infra.constant.ExceptionConstants.GitopsCode.DEVOPS_FILE_RESOURCE_NOT_EXIST;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import io.kubernetes.client.models.V1beta1Ingress;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.kubernetes.C7nCertification;
import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.JsonYamlConversionUtil;
import io.choerodon.devops.infra.util.SkipNullRepresenterUtil;
import io.choerodon.devops.infra.util.TypeUtil;

public class ResourceConvertToYamlHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceConvertToYamlHandler.class);

    public static final String UPDATE = "update";
    private static final String C7NTAG = "!!io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease";
    private static final String V1_INGTAG = "!!io.kubernetes.client.openapi.models.V1Ingress";
    private static final String V1_BETA1_INGTAG = "!!io.kubernetes.client.models.V1beta1Ingress";
    private static final String SVCTAG = "!!io.kubernetes.client.openapi.models.V1Service";
    private static final String CERTTAG = "!!io.choerodon.devops.api.vo.kubernetes.C7nCertification";
    private static final String CONFIGMAPTAG = "!!io.kubernetes.client.openapi.models.V1ConfigMap";
    private static final String SECRET = "!!io.kubernetes.client.openapi.models.V1Secret";
    private static final String ENDPOINTS = "!!io.kubernetes.client.openapi.models.V1Endpoints";
    private static final String DEPLOYMENT = "!!io.kubernetes.client.openapi.models.V1Deployment";
    private static final List<String> WORKLOAD_RESOURCE_TYPE = new ArrayList<>();

    @Value(value = "${devops.deploy.enableDeleteBlankLine:true}")
    private Boolean enableDeleteBlankLine;

    static {
        WORKLOAD_RESOURCE_TYPE.add(ResourceType.DEPLOYMENT.getType());
        WORKLOAD_RESOURCE_TYPE.add(ResourceType.STATEFULSET.getType());
        WORKLOAD_RESOURCE_TYPE.add(ResourceType.JOB.getType());
        WORKLOAD_RESOURCE_TYPE.add(ResourceType.CRON_JOB.getType());
        WORKLOAD_RESOURCE_TYPE.add(ResourceType.DAEMONSET.getType());
    }

    private T type;

    public T getType() {
        return type;
    }

    public void setType(T type) {
        this.type = type;
    }

    public String getCreationResourceContentForBatchDeployment() {
        Tag tag = new Tag(type.getClass().toString());
        Yaml yaml = getYamlObject(tag, true);
        return yaml.dump(type).replace("!<" + tag.getValue() + ">", "---");
    }

    /**
     * operate files in GitLab
     *
     * @param fileCode           file's code
     * @param gitlabEnvProjectId Environment corresponding GitLab project ID
     * @param operationType      operation type
     * @param userId             GitLab user ID
     * @param filePath           环境库在本地的目录
     * @return 返回修改后的文件的sha值
     */
    public void operationEnvGitlabFile(String fileCode, Integer gitlabEnvProjectId, String operationType,
                                       Long userId, Long objectId, String objectType, V1Endpoints v1Endpoints, Boolean deleteCert, Long envId, String filePath) {
        GitlabServiceClientOperator gitlabServiceClientOperator = ApplicationContextHelper.getSpringFactory().getBean(GitlabServiceClientOperator.class);
        Tag tag = new Tag(type.getClass().toString());
        Yaml yaml = getYamlObject(tag, true);
        String endpointContent = null;
        if (v1Endpoints != null) {
            Yaml newYaml = getYamlObject(new Tag(v1Endpoints.getClass().toString()), true);
            endpointContent = newYaml.dump(v1Endpoints).replace(ENDPOINTS, "---");
        }
        String content = "";
        if (type instanceof V1PersistentVolume || type instanceof V1PersistentVolumeClaim) {
            JSON json = new JSON();
            String jsonStr = json.serialize(type);
            try {
                content = JsonYamlConversionUtil.json2yaml(jsonStr);
            } catch (IOException e) {
                throw new CommonException("devops.dump.pv.or.pvc.to.yaml", e);
            }
        } else {
            content = yaml.dump(type).replace("!<" + tag.getValue() + ">", "---");
            if (endpointContent != null) {
                content = content + "\n" + endpointContent;
            }
        }
        if (operationType.equals("create")) {
            String path = fileCode + ".yaml";
            gitlabServiceClientOperator.createFile(gitlabEnvProjectId, path, content,
                    String.format("【CREATE】%s", path), TypeUtil.objToInteger(userId));

        } else {
            DevopsEnvFileResourceService devopsEnvFileResourceService = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceService.class);
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService.baseQueryByEnvIdAndResourceId(envId, objectId, objectType);
            if (devopsEnvFileResourceDTO == null) {
                throw new CommonException(DEVOPS_FILE_RESOURCE_NOT_EXIST);
            }
            gitlabServiceClientOperator.updateFile(gitlabEnvProjectId, devopsEnvFileResourceDTO.getFilePath(), getUpdateContent(type, deleteCert,
                            endpointContent, devopsEnvFileResourceDTO.getFilePath(), objectType, filePath, operationType),
                    String.format("【UPDATE】 %s", devopsEnvFileResourceDTO.getFilePath()), TypeUtil.objToInteger(userId), "master");
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

    /**
     * 获取文件内的更新内容
     *
     * @param t             资源对象
     * @param deleteCert    是否删除证书
     * @param content       更新的资源的content
     * @param filePath      资源文件在环境库中的相对路径
     * @param objectType    对象类型
     * @param path          本地环境库的目录路径
     * @param operationType 操作类型 create/update
     * @return 指定文件操作之后的内容
     */
    public String getUpdateContent(T t, Boolean deleteCert, String content, String filePath, String
            objectType, String path, String operationType) {
        Yaml yaml = new Yaml();
        StringBuilder resultBuilder = new StringBuilder();
        // 获取要更新的资源所在的文件
        File file = new File(String.format("%s/%s", path, filePath));
        try {
            // 读取文件内的所有资源对象，没有更新的资源对象进行保留，更新的进行代替
            for (Object data : yaml.loadAll(new FileInputStream(file))) {
                // TODO 加上Yaml文件校验
                JSONObject jsonObject = new JSONObject((Map<String, Object>) data);
                switch (jsonObject.get("kind").toString()) {
                    case "C7NHelmRelease":
                        handleC7nHelmRelease(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "Ingress":
                        handleIngress(t, deleteCert, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "Service":
                        handleService(t, content, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "Certificate":
                        handleC7nCertification(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "ConfigMap":
                        handleConfigMap(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "Secret":
                        handleSecret(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "PersistentVolume":
                        handlePV(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "PersistentVolumeClaim":
                        // 这里不需要对遗留在自定义资源中的PVC做兼容判断，因为自定义资源中PVC的objectType是'custom'
                        handlePVC(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    case "Endpoints":
                        // 忽视掉Endpoints
                        break;
                    case "Deployment":
                    case "StatefulSet":
                    case "Job":
                    case "CronJob":
                    case "DaemonSet":
                        handleWorkload(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                    default:
                        handleCustom(t, objectType, operationType, resultBuilder, jsonObject);
                        break;
                }
            }
            String result = resultBuilder.toString();
            // 1. 判断是否开启删除gitops文件空行，没有开启则不处理。
            if (Boolean.FALSE.equals(enableDeleteBlankLine)) {
                LOGGER.info(">>>>>>>>>>>>>>>>return default gitops yaml <<<<<<<<<<<<<<<<<<<");
                return result;
            }
            // 2. 如果开启，则删除空行后返回
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(">>>>>>>>>>>>>>>>Old result yaml is {} <<<<<<<<<<<<<<<<<<<", result);
            }
            String replacedResult = result.replaceAll("((\\r\\n)|\\n)[\\s\\t ]*(\\1)+", "$1");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(">>>>>>>>>>>>>>>>ReplacedResult yaml is {} <<<<<<<<<<<<<<<<<<<", replacedResult);
            }
            return replacedResult;
        } catch (FileNotFoundException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    private void handleWorkload(T t, String objectType, String operationType, StringBuilder resultBuilder, JSONObject jsonObject) {
        Yaml yaml7 = new Yaml();
        Object workloadResource = yaml7.load(jsonObject.toJSONString());
        if (WORKLOAD_RESOURCE_TYPE.contains(objectType)) {
            String oldResourceName = ((LinkedHashMap) (((Map<String, Object>) workloadResource).get("metadata"))).get("name").toString();
            String newResourceName = ((LinkedHashMap) (((Map<String, Object>) t).get("metadata"))).get("name").toString();
            if (oldResourceName.equals(newResourceName)) {
                if (operationType.equals(UPDATE)) {
                    workloadResource = t;
                } else {
                    return;
                }
            }
        }
        resultBuilder.append("---").append("\n").append(getYamlObject(null, false).dump(workloadResource)).append("\n");
    }

    private void handleService(T t, String content, String objectType, String operationType, StringBuilder
            resultBuilder, JSONObject jsonObject) {
        Yaml yaml3 = new Yaml();
        V1Service v1Service = yaml3.loadAs(jsonObject.toJSONString(), V1Service.class);
        V1Service newV1Service;
        if (objectType.equals(ResourceType.SERVICE.getType()) && v1Service.getMetadata().getName().equals(((V1Service) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                newV1Service = (V1Service) t;
            } else {
                return;
            }
        } else {
            // 如果不是修改的这个对象，保留这个对象
            newV1Service = v1Service;
        }
        Tag tag3 = new Tag(SVCTAG);
        resultBuilder.append("\n").append(getYamlObject(tag3, true).dump(newV1Service).replace(SVCTAG, "---"));
        if (content != null) {
            resultBuilder.append("\n").append(content);
        }
    }

    private void handleIngress(T t, Boolean deleteCert, String objectType, String operationType, StringBuilder
            resultBuilder, JSONObject jsonObject) {
        if (t instanceof V1Ingress) {
            Yaml yaml2 = new Yaml();
            V1Ingress v1Ingress = yaml2.loadAs(jsonObject.toJSONString(), V1Ingress.class);
            V1Ingress newV1Ingress;

            // 如果这个Ingress对象是被修改的对象
            if (objectType.equals(ResourceType.INGRESS.getType()) && v1Ingress.getMetadata().getName().equals(((V1Ingress) t).getMetadata().getName())) {
                if (operationType.equals(UPDATE)) {
                    newV1Ingress = (V1Ingress) t;
                    if (!deleteCert) {
                        if (newV1Ingress.getSpec().getTls() != null && !newV1Ingress.getSpec().getTls().isEmpty()) {
                            newV1Ingress.getSpec().setTls(newV1Ingress.getSpec().getTls());
                        } else {
                            newV1Ingress.getSpec().setTls(v1Ingress.getSpec().getTls());
                        }
                    }
                } else {
                    // 如果是 DELETE 直接返回
                    return;
                }
            } else {
                // 如果不是，进行保留
                newV1Ingress = v1Ingress;
            }
            Tag tag2 = new Tag(V1_INGTAG);
            resultBuilder.append("\n").append(getYamlObject(tag2, true).dump(newV1Ingress).replace(V1_INGTAG, "---"));
        } else {
            Yaml yaml2 = new Yaml();
            V1beta1Ingress v1beta1Ingress = yaml2.loadAs(jsonObject.toJSONString(), V1beta1Ingress.class);
            V1beta1Ingress newV1Ingress;

            // 如果这个Ingress对象是被修改的对象
            if (objectType.equals(ResourceType.INGRESS.getType()) && v1beta1Ingress.getMetadata().getName().equals(((V1beta1Ingress) t).getMetadata().getName())) {
                if (operationType.equals(UPDATE)) {
                    newV1Ingress = (V1beta1Ingress) t;
                    if (!deleteCert) {
                        if (newV1Ingress.getSpec().getTls() != null && !newV1Ingress.getSpec().getTls().isEmpty()) {
                            newV1Ingress.getSpec().setTls(newV1Ingress.getSpec().getTls());
                        } else {
                            newV1Ingress.getSpec().setTls(v1beta1Ingress.getSpec().getTls());
                        }
                    }
                } else {
                    // 如果是 DELETE 直接返回
                    return;
                }
            } else {
                // 如果不是，进行保留
                newV1Ingress = v1beta1Ingress;
            }
            Tag tag2 = new Tag(V1_BETA1_INGTAG);
            resultBuilder.append("\n").append(getYamlObject(tag2, true).dump(newV1Ingress).replace(V1_BETA1_INGTAG, "---"));
        }
    }

    private void handleC7nHelmRelease(T t, String objectType, String operationType, StringBuilder
            resultBuilder, JSONObject jsonObject) {
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


    private void handleC7nCertification(T t, String objectType, String operationType, StringBuilder
            resultBuilder, JSONObject jsonObject) {
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
        V1ConfigMap newV1ConfigMap;
        if (objectType.equals(ResourceType.CONFIGMAP.getType()) && v1ConfigMap.getMetadata().getName().equals(((V1ConfigMap) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                newV1ConfigMap = (V1ConfigMap) t;
                newV1ConfigMap.getMetadata().setAnnotations(v1ConfigMap.getMetadata().getAnnotations());
            } else {
                return;
            }
        } else {
            // 修改的如果不是这个对象，保留这个对象
            newV1ConfigMap = v1ConfigMap;
        }
        Tag tag1 = new Tag(CONFIGMAPTAG);
        resultBuilder.append("\n").append(getYamlObject(tag1, true).dump(newV1ConfigMap).replace(CONFIGMAPTAG, "---"));
    }

    private void handleSecret(T t, String objectType, String operationType, StringBuilder resultBuilder,
                              JSONObject jsonObject) {
        JSON json = new JSON();
        V1Secret v1Secret = json.deserialize(jsonObject.toJSONString(), V1Secret.class);
        V1Secret newV1Secret;
        if (objectType.equals(ResourceType.SECRET.getType()) && v1Secret.getMetadata().getName()
                .equals(((V1Secret) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                newV1Secret = (V1Secret) t;
                newV1Secret.getMetadata().setAnnotations(v1Secret.getMetadata().getAnnotations());
            } else {
                return;
            }
        } else {
            // 修改的如果不是这个对象，保留这个对象
            newV1Secret = v1Secret;
        }
        Tag tag1 = new Tag(SECRET);
        resultBuilder.append("\n").append(getYamlObject(tag1, true).dump(newV1Secret).replace(SECRET, "---"));
    }

    private void handlePV(T t, String objectType, String operationType, StringBuilder resultBuilder,
                          JSONObject jsonObject) {
        JSON json = new JSON();
        V1PersistentVolume v1PersistentVolume = json.deserialize(jsonObject.toJSONString(), V1PersistentVolume.class);
        V1PersistentVolume newPv;
        if (objectType.equals(ResourceType.PERSISTENT_VOLUME.getType()) && v1PersistentVolume.getMetadata().getName().equals(((V1PersistentVolume) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                // 不允许更新
                throw new CommonException(GitOpsObjectError.PERSISTENT_VOLUME_UNMODIFIED.getError(), v1PersistentVolume.getMetadata().getName());
            } else {
                // 删除任由此对象内容丢失
                return;
            }
        } else {
            // 修改的如果不是这个对象，保留这个对象
            newPv = v1PersistentVolume;
        }
        String jsonStr = json.serialize(newPv);
        try {
            resultBuilder.append("\n").append(JsonYamlConversionUtil.json2yaml(jsonStr));
        } catch (IOException e) {
            throw new CommonException("devops.dump.pv.or.pvc.to.yaml", e);
        }
    }


    private void handlePVC(T t, String objectType, String operationType, StringBuilder resultBuilder,
                           JSONObject jsonObject) {
        JSON json = new JSON();
        V1PersistentVolumeClaim v1PersistentVolumeClaim = json.deserialize(jsonObject.toJSONString(), V1PersistentVolumeClaim.class);
        V1PersistentVolumeClaim newPvc;
        if (objectType.equals(ResourceType.PERSISTENT_VOLUME_CLAIM.getType()) && v1PersistentVolumeClaim.getMetadata().getName().equals(((V1PersistentVolumeClaim) t).getMetadata().getName())) {
            if (operationType.equals(UPDATE)) {
                // 不允许更新
                throw new CommonException(GitOpsObjectError.PERSISTENT_VOLUME_CLAIM_UNMODIFIED.getError(), v1PersistentVolumeClaim.getMetadata().getName());
            } else {
                // 删除任由此对象内容丢失
                return;
            }
        } else {
            // 修改的如果不是这个对象，保留这个对象
            newPvc = v1PersistentVolumeClaim;
        }
        String jsonStr = json.serialize(newPvc);
        try {
            resultBuilder.append("\n").append(JsonYamlConversionUtil.json2yaml(jsonStr));
        } catch (IOException e) {
            throw new CommonException("devops.dump.pv.or.pvc.to.yaml", e);
        }
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
