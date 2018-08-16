package io.choerodon.devops.domain.application.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
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
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.infra.common.util.SkipNullRepresenterUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;

public class ObjectOperation<T> {

    private String C7NTAG = "!!io.choerodon.devops.domain.application.valueobject.C7nHelmRelease";
    private String INGTAG = "!!io.kubernetes.client.models.V1beta1Ingress";
    private String SVCTAG = "!!io.kubernetes.client.models.V1Service";

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
    public void operationEnvGitlabFile(String fileCode, Integer gitlabEnvProjectId, String operationType, Long userId, Long objectId, String objectType, Long envId, String filePath) {
        GitlabRepository gitlabRepository = ApplicationContextHelper.getSpringFactory().getBean(GitlabRepository.class);
        Tag tag = new Tag(type.getClass().toString());
        Yaml yaml = getYamlObject(tag);
        String content = yaml.dump(type).replace("!<" + tag.getValue() + ">", "---");
        String path = fileCode + ".yaml";
        if (operationType.equals("create")) {
            gitlabRepository.createFile(gitlabEnvProjectId, path, content,
                    "ADD FILE", TypeUtil.objToInteger(userId));
        } else {
            DevopsEnvFileResourceRepository devopsEnvFileResourceRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceRepository.class);
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(envId, objectId, objectType);
            gitlabRepository.updateFile(gitlabEnvProjectId, devopsEnvFileResourceE.getFilePath(), getUpdateContent(type, devopsEnvFileResourceE.getFilePath(), objectType, filePath, operationType),
                    "UPDATE FILE", TypeUtil.objToInteger(userId));
        }
    }

    private Yaml getYamlObject(Tag tag) {
        SkipNullRepresenterUtil skipNullRepresenter = new SkipNullRepresenterUtil();
        skipNullRepresenter.addClassTag(type.getClass(), tag);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(skipNullRepresenter, options);
        return yaml;
    }


    private String getUpdateContent(T t, String filePath, String objectType, String path, String operationType) {
        Yaml yaml = new Yaml();
        String result = "";
        File file = new File(path + "/" + filePath);
        try {
            for (Object data : yaml.loadAll(new FileInputStream(file))) {
                JSONObject jsonObject = new JSONObject((Map<String, Object>) data);
                String type = jsonObject.get("kind").toString();
                switch (type) {
                    case "C7NHelmRelease":
                        Yaml yaml1 = new Yaml();
                        C7nHelmRelease c7nHelmRelease = yaml1.loadAs(jsonObject.toJSONString(), C7nHelmRelease.class);
                        if (objectType.equals("C7NHelmRelease") && c7nHelmRelease.getMetadata().getName().equals(((C7nHelmRelease) t).getMetadata().getName())) {
                            if (operationType.equals("update")) {
                                c7nHelmRelease = (C7nHelmRelease) t;
                            } else {
                                break;
                            }
                        }
                        Tag tag1 = new Tag(C7NTAG);
                        result = result + "\n" + getYamlObject(tag1).dump(c7nHelmRelease).replace(C7NTAG, "---");
                        break;
                    case "Ingress":
                        Yaml yaml2 = new Yaml();
                        V1beta1Ingress v1beta1Ingress = yaml2.loadAs(jsonObject.toJSONString(), V1beta1Ingress.class);
                        if (objectType.equals("Ingress") && v1beta1Ingress.getMetadata().getName().equals(((V1beta1Ingress) t).getMetadata().getName())) {
                            if (operationType.equals("update")) {
                                v1beta1Ingress = (V1beta1Ingress) t;
                            } else {
                                break;
                            }
                        }
                        Tag tag2 = new Tag(INGTAG);
                        result = result + "\n" + getYamlObject(tag2).dump(v1beta1Ingress).replace(INGTAG, "---");
                        break;
                    case "Service":
                        Yaml yaml3 = new Yaml();
                        V1Service v1Service = yaml3.loadAs(jsonObject.toJSONString(), V1Service.class);
                        if (objectType.equals("Service") && v1Service.getMetadata().getName().equals(((V1beta1Ingress) t).getMetadata().getName())) {
                            if (operationType.equals("update")) {
                                v1Service = (V1Service) t;
                            } else {
                                break;
                            }
                        }
                        Tag tag3 = new Tag(SVCTAG);
                        result = result + "\n" + getYamlObject(tag3).dump(v1Service).replace(SVCTAG, "---");
                        break;
                }
            }
            return result;
        } catch (FileNotFoundException e) {
            throw new CommonException(e.getMessage());
        }
    }

}
