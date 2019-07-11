package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCustomizeResourceService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.domain.application.entity.DevopsCustomizeResourceContentE;
import io.choerodon.devops.domain.application.entity.DevopsCustomizeResourceE;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsCustomizeResourceContentRepository;
import io.choerodon.devops.domain.application.repository.DevopsCustomizeResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.app.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.ResourceType;
import io.kubernetes.client.models.V1Endpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/1.
 */

@Service
public class HandlerCustomResourceServiceImpl implements HandlerObjectFileRelationsService<DevopsCustomizeResourceE> {

    public static final String UPDATE = "update";
    public static final String CREATE = "create";
    private static final String GIT_SUFFIX = "/.git";
    @Autowired
    private DevopsCustomizeResourceRepository devopsCustomizeResourceRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private DevopsCustomizeResourceContentRepository devopsCustomizeResourceContentRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsCustomizeResourceService devopsCustomizeResourceService;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync, List<DevopsCustomizeResourceE> devopsCustomizeResourceES, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<DevopsCustomizeResourceE> beforeDevopsCustomResource = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(ResourceType.CUSTOM.getType()))
                .map(devopsEnvFileResourceE -> {
                    DevopsCustomizeResourceE devopsCustomizeResourceE = devopsCustomizeResourceRepository
                            .query(devopsEnvFileResourceE.getResourceId());
                    if (devopsCustomizeResourceE == null) {
                        devopsEnvFileResourceRepository
                                .deleteByEnvIdAndResource(envId, devopsEnvFileResourceE.getResourceId(), ResourceType.CUSTOM.getType());
                        return null;
                    }
                    return devopsCustomizeResourceE;
                }).collect(Collectors.toList());

        //比较已存在实例和新增要处理的configMap,获取新增configMap，更新configMap，删除configMap
        List<DevopsCustomizeResourceE> addDevopsCustomizeResourceE = new ArrayList<>();
        List<DevopsCustomizeResourceE> updateDevopsCustomizeResourceE = new ArrayList<>();
        devopsCustomizeResourceES.stream().forEach(devopsCustomizeResourceE -> {
            if (beforeDevopsCustomResource.contains(devopsCustomizeResourceE)) {
                updateDevopsCustomizeResourceE.add(devopsCustomizeResourceE);
                beforeDevopsCustomResource.remove(devopsCustomizeResourceE);
            } else {
                addDevopsCustomizeResourceE.add(devopsCustomizeResourceE);
            }
        });

        //新增configMap
        addDevopsCustomResource(objectPath, projectId, envId, addDevopsCustomizeResourceE, path, userId);
        //更新configMap
        updateDevopsCustomResource(objectPath, projectId, envId, updateDevopsCustomizeResourceE, path, userId);
        //删除configMap,和文件对象关联关系
        beforeDevopsCustomResource.forEach(devopsCustomizeResourceE -> {
            DevopsCustomizeResourceE oldDevopsCustomizeResourceE = devopsCustomizeResourceRepository.queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceE.getK8sKind(), devopsCustomizeResourceE.getName());
            if (oldDevopsCustomizeResourceE != null) {
                devopsCustomizeResourceService.deleteResourceByGitOps(oldDevopsCustomizeResourceE.getId());
                devopsEnvFileResourceRepository
                        .deleteByEnvIdAndResource(envId, oldDevopsCustomizeResourceE.getId(), ResourceType.CUSTOM.getType());
            }
        });
    }


    private void updateDevopsCustomResource(Map<String, String> objectPath, Long projectId, Long envId, List<DevopsCustomizeResourceE> updateDevopsCustomizeResourceE, String path, Long userId) {
        updateDevopsCustomizeResourceE.stream()
                .forEach(devopsCustomizeResourceE -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(devopsCustomizeResourceE.hashCode()));
                        DevopsCustomizeResourceE oldDevopsCustomizeResourceE = devopsCustomizeResourceRepository
                                .queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceE.getK8sKind(), devopsCustomizeResourceE.getName());

                        //判断自定义资源是否发生了改变
                        DevopsCustomizeResourceContentE devopsCustomizeResourceContentE = devopsCustomizeResourceContentRepository.query(oldDevopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getId());
                        Boolean isNotChange = devopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getContent().equals(devopsCustomizeResourceContentE.getContent());
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(oldDevopsCustomizeResourceE.getDevopsEnvCommandE().getId());

                        //发生改变走处理改变自定义资源的逻辑
                        if (!isNotChange) {
                            oldDevopsCustomizeResourceE.setProjectId(projectId);
                            oldDevopsCustomizeResourceE.setDevopsCustomizeResourceContentE(new DevopsCustomizeResourceContentE(oldDevopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getId(), devopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getContent()));
                            devopsCustomizeResourceService.createOrUpdateResourceByGitOps(UPDATE, oldDevopsCustomizeResourceE, userId, envId);
                            DevopsCustomizeResourceE newDevopsCustomizeResourceE = devopsCustomizeResourceRepository
                                    .queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceE.getK8sKind(), devopsCustomizeResourceE.getName());
                            devopsEnvCommandE = devopsEnvCommandRepository.query(newDevopsCustomizeResourceE.getDevopsEnvCommandE().getId());
                        }

                        //没发生改变,更新commit记录，更新文件对应关系记录
                        devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandRepository.update(devopsEnvCommandE);
                        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                                .queryByEnvIdAndResource(envId, oldDevopsCustomizeResourceE.getId(), ResourceType.CUSTOM.getType());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath,
                                envId,
                                devopsEnvFileResourceE,
                                devopsCustomizeResourceE.hashCode(), oldDevopsCustomizeResourceE.getId(), ResourceType.CUSTOM.getType());

                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }

    private void addDevopsCustomResource(Map<String, String> objectPath, Long projectId, Long envId, List<DevopsCustomizeResourceE> addDevopsCustomizeResourceES, String path, Long userId) {
        addDevopsCustomizeResourceES.stream()
                .forEach(devopsCustomizeResourceE -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(devopsCustomizeResourceE.hashCode()));
                        DevopsCustomizeResourceE oldDevopsCustomizeResourceE = devopsCustomizeResourceRepository.queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceE.getK8sKind(), devopsCustomizeResourceE.getName());

                        //初始化configMap参数,创建时判断configMap是否存在，存在则直接创建文件对象关联关系
                        if (oldDevopsCustomizeResourceE == null) {
                            devopsCustomizeResourceE.setProjectId(projectId);
                            devopsCustomizeResourceService.createOrUpdateResourceByGitOps(CREATE, devopsCustomizeResourceE, userId, envId);
                            oldDevopsCustomizeResourceE = devopsCustomizeResourceRepository.queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceE.getK8sKind(), devopsCustomizeResourceE.getName());
                        }
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(oldDevopsCustomizeResourceE.getDevopsEnvCommandE().getId());
                        devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandRepository.update(devopsEnvCommandE);

                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, devopsCustomizeResourceE.hashCode(), oldDevopsCustomizeResourceE.getId(),
                                ResourceType.CUSTOM.getType());

                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }
}
