package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.MiscConstants.*;

import java.io.IOException;
import java.util.*;

import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.WorkloadBaseCreateOrUpdateVO;
import io.choerodon.devops.api.vo.WorkloadBaseVO;
import io.choerodon.devops.api.vo.workload.WorkLoad;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.util.*;

@Service
public class WorkloadServiceImpl implements WorkloadService {

    private static final String METADATA = "metadata";
    private static final String KIND = "kind";
    private static final String MASTER = "master";
    private static final String WORK_LOAD = "Workload";

    private static final Map<String, String> RESOURCE_FILE_TEMPLATE_PATH_MAP;

    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Autowired
    private PermissionHelper permissionHelper;

    @Autowired
    private UserAttrService userAttrService;

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;

    @Autowired
    @Lazy
    private DevopsDeploymentService devopsDeploymentService;

    @Autowired
    @Lazy
    private DevopsStatefulSetService devopsStatefulSetService;

    @Autowired
    @Lazy
    private DevopsJobService devopsJobService;

    @Autowired
    @Lazy
    private DevopsDaemonSetService devopsDaemonSetService;

    @Autowired
    @Lazy
    private DevopsCronJobService devopsCronJobService;

    @Autowired
    @Lazy
    private DevopsWorkloadResourceContentService devopsWorkloadResourceContentService;

    @Autowired
    @Lazy
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Autowired
    private Map<String, WorkLoad> workLoadMap;

    static {
        Map<String, String> filePathMap = new HashMap<>();
        filePathMap.put(ResourceType.DEPLOYMENT.getType(), "deploy-%s.yaml");
        filePathMap.put(ResourceType.JOB.getType(), "job-%s.yaml");
        filePathMap.put(ResourceType.CRON_JOB.getType(), "cj-%s.yaml");
        filePathMap.put(ResourceType.DAEMONSET.getType(), "ds-%s.yaml");
        filePathMap.put(ResourceType.STATEFULSET.getType(), "sts-%s.yaml");
        RESOURCE_FILE_TEMPLATE_PATH_MAP = Collections.unmodifiableMap(filePathMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdate(Long projectId, WorkloadBaseCreateOrUpdateVO workloadBaseCreateOrUpdateVO, MultipartFile multipartFile, ResourceType resourceType) {
        WorkloadBaseVO workloadBaseVO = processKeyEncrypt(workloadBaseCreateOrUpdateVO);
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, workloadBaseVO.getEnvId());

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        String resourceFilePath = "";

        String content = workloadBaseVO.getContent();
        if (multipartFile != null) {
            try {
                content = new String(multipartFile.getBytes());
            } catch (IOException e) {
                throw new CommonException("error.read.multipart.file");
            }
        }

        Yaml yaml = new Yaml();
        List<Object> objects = new ArrayList<>();

        //处理每个k8s资源对象
        Iterable<Object> workLoads = yaml.loadAll(content);
        int size = Iterables.size(workLoads);
        if (size != 1) {
            throw new CommonException("error.workload.size", resourceType.getType());
        }

        for (Object data : yaml.loadAll(content)) {
            Map<String, Object> datas = (Map<String, Object>) data;

            String kind = (String) datas.get(KIND);

            LinkedHashMap metadata = (LinkedHashMap) datas.get(METADATA);
            //校验yaml文件内资源属性是否合法,并返回资源的name
            String name = checkResource(metadata, kind, resourceType.getType());

            // 前面对资源进行了数量校验，因此只会循环一次，resourceFilePath也只会被设置一次
            resourceFilePath = String.format(RESOURCE_FILE_TEMPLATE_PATH_MAP.get(resourceType.getType()), name);
            objects.add(datas);
            handleWorkLoad(projectId, workloadBaseVO.getEnvId(), FileUtil.getYaml().dump(data), kind, name, workloadBaseVO.getOperateType(), workloadBaseVO.getResourceId(), DetailsHelper.getUserDetails().getUserId());
        }

        if (CREATE_TYPE.equals(workloadBaseVO.getOperateType())) {
            gitlabServiceClientOperator.createFile(devopsEnvironmentDTO.getGitlabEnvProjectId().intValue(), resourceFilePath, FileUtil.getYaml().dumpAll(objects.iterator()),
                    "ADD FILE", TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        } else {
            //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String gitOpsPath = clusterConnectionHandler.handDevopsEnvGitRepository(
                    devopsEnvironmentDTO.getProjectId(),
                    devopsEnvironmentDTO.getCode(),
                    devopsEnvironmentDTO.getId(),
                    devopsEnvironmentDTO.getEnvIdRsa(),
                    devopsEnvironmentDTO.getType(),
                    devopsEnvironmentDTO.getClusterCode());

            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "master",
                    resourceFilePath)) {
                throw new CommonException("error.fileResource.not.exist");
            }
            //获取更新内容
            ResourceConvertToYamlHandler<Object> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            // TODO 这里的get(0)似乎意味着不支持多个资源的更新
            String updateContent = resourceConvertToYamlHandler.getUpdateContent(objects.get(0), false, null, resourceFilePath, resourceType.getType(), gitOpsPath, CommandType.UPDATE.getType());
            gitlabServiceClientOperator.updateFile(devopsEnvironmentDTO.getGitlabEnvProjectId().intValue(), resourceFilePath, updateContent, "UPDATE FILE", TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }

    }

    @Override
    public Long getWorkloadId(Long envId, String workloadName, String type) {
        WorkLoad workLoad = workLoadMap.get(io.choerodon.devops.infra.util.StringUtils.toLowerCaseFirstOne(type) + WORK_LOAD);
        return workLoad.getWorkloadId(envId, workloadName);
    }

    @Override
    @Transactional
    public void delete(Long projectId, Long id, ResourceType resourceType) {
        Long envId;
        String resourceName;
        switch (resourceType) {
            case DEPLOYMENT:
                DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.selectByPrimaryKey(id);
                if (devopsDeploymentDTO == null) {
                    return;
                }
                envId = devopsDeploymentDTO.getEnvId();
                resourceName = devopsDeploymentDTO.getName();
                break;
            case STATEFULSET:
                DevopsStatefulSetDTO devopsStatefulSetDTO = devopsStatefulSetService.selectByPrimaryKey(id);
                if (devopsStatefulSetDTO == null) {
                    return;
                }
                envId = devopsStatefulSetDTO.getEnvId();
                resourceName = devopsStatefulSetDTO.getName();
                break;
            case JOB:
                DevopsJobDTO devopsJobDTO = devopsJobService.selectByPrimaryKey(id);
                if (devopsJobDTO == null) {
                    return;
                }
                envId = devopsJobDTO.getEnvId();
                resourceName = devopsJobDTO.getName();
                break;
            case DAEMONSET:
                DevopsDaemonSetDTO devopsDaemonSetDTO = devopsDaemonSetService.selectByPrimaryKey(id);
                if (devopsDaemonSetDTO == null) {
                    return;
                }
                envId = devopsDaemonSetDTO.getEnvId();
                resourceName = devopsDaemonSetDTO.getName();
                break;
            case CRON_JOB:
                DevopsCronJobDTO devopsCronJobDTO = devopsCronJobService.selectByPrimaryKey(id);
                if (devopsCronJobDTO == null) {
                    return;
                }
                envId = devopsCronJobDTO.getEnvId();
                resourceName = devopsCronJobDTO.getName();
                break;
            default:
                throw new CommonException("error.workload.resource.not.supported", resourceType.getType());
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        handleWorkLoad(null, null, null, resourceType.getType(), resourceName, DELETE_TYPE, id, null);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String gitOpsPath = clusterConnectionHandler.handDevopsEnvGitRepository(
                devopsEnvironmentDTO.getProjectId(),
                devopsEnvironmentDTO.getCode(),
                devopsEnvironmentDTO.getId(),
                devopsEnvironmentDTO.getEnvIdRsa(),
                devopsEnvironmentDTO.getType(),
                devopsEnvironmentDTO.getClusterCode());

        String resourceFileName = String.format(RESOURCE_FILE_TEMPLATE_PATH_MAP.get(resourceType.getType()), resourceName);

        // 查询该对象所在文件中是否含有其它对象
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(devopsEnvironmentDTO.getId(), id, resourceType.getType());
        if (devopsEnvFileResourceDTO == null) {
            deleteWorkload(resourceType.getType(), id);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER, resourceFileName)) {
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        resourceFileName,
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
            return;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                deleteWorkload(resourceType.getType(), id);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                return;
            }
        }
        List<DevopsEnvFileResourceDTO> devopsEnvFileResourceDTOS = devopsEnvFileResourceService
                .baseQueryByEnvIdAndPath(devopsEnvironmentDTO.getId(), devopsEnvFileResourceDTO.getFilePath());

        // 如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsEnvFileResourceDTOS.size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER, devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        devopsEnvFileResourceDTO.getFilePath(), "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
        } else {
            ResourceConvertToYamlHandler<Object> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
            Integer gitlabEnvProjectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
            resourceConvertToYamlHandler.operationEnvGitlabFile(null, gitlabEnvProjectId, DELETE_TYPE, userAttrDTO.getGitlabUserId(), id,
                    resourceType.getType(), null, false, devopsEnvironmentDTO.getId(), gitOpsPath);
        }
    }

    private WorkloadBaseVO processKeyEncrypt(WorkloadBaseCreateOrUpdateVO workloadBaseCreateOrUpdateVO) {
        // TODO 待hzero兼容 ModelAttribute 注解后删除
        WorkloadBaseVO decryptedWorkloadBaseVO = ConvertUtils.convertObject(workloadBaseCreateOrUpdateVO, WorkloadBaseVO.class);
        decryptedWorkloadBaseVO.setEnvId(KeyDecryptHelper.decryptValue(workloadBaseCreateOrUpdateVO.getEnvId()));
        decryptedWorkloadBaseVO.setResourceId(KeyDecryptHelper.decryptValue(workloadBaseCreateOrUpdateVO.getResourceId()));
        return decryptedWorkloadBaseVO;
    }

    private void handleWorkLoad(Long projectId, Long envId, String content, String kind, String name, String operateType, Long resourceId, Long userId) {
        if (CREATE_TYPE.equals(operateType)) {
            createWorkload(projectId, envId, content, kind, operateType, name, userId);
        } else if (UPDATE_TYPE.equals(operateType)) {
            updateWorkLoad(kind, name, content, resourceId, userId);
        } else {
            //自定义资源关联command
            DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(kind, DELETE_TYPE, userId);
            devopsEnvCommandDTO.setObjectId(resourceId);
            devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

            //更新自定义资源关联的最新command
            updateWorkLoadCommandId(kind, resourceId, devopsEnvCommandDTO.getId());
        }
    }


    private String checkResource(LinkedHashMap metadata, String kind, String resourceType) {
        if (kind == null) {
            throw new CommonException("error.workload.resource.kind.not.found");
        }
        // 禁止创建不同资源
        if (!resourceType.equals(kind)) {
            throw new CommonException("error.workload.resource.kind.mismatch");
        }
        if (metadata == null) {
            throw new CommonException("error.workload.resource.metadata.not.found");
        }

        Object name = metadata.get("name");
        if (StringUtils.isEmpty(name)) {
            throw new CommonException("error.workload.resource.name.not.found");
        }
        return name.toString();
    }

    private void checkWorkloadExist(String type, Long envId, String name) {
        WorkLoad workLoad = workLoadMap.get(io.choerodon.devops.infra.util.StringUtils.toLowerCaseFirstOne(type) + WORK_LOAD);
        workLoad.checkWorkloadExist(envId, name);
    }

    private void createWorkload(Long projectId, Long envId, String content, String resourceType, String operateType, String name, Long userId) {
        //校验新增的类型是否已存在
        checkWorkloadExist(resourceType, envId, name);

        //自定义资源关联command
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(resourceType, operateType, userId);
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
        Long workLoadId = null;
        WorkLoad workLoad = workLoadMap.get(io.choerodon.devops.infra.util.StringUtils.toLowerCaseFirstOne(resourceType) + WORK_LOAD);
        workLoadId = workLoad.createWorkload(name, projectId, envId, devopsEnvCommandDTO.getId());
        devopsWorkloadResourceContentService.create(resourceType, workLoadId, content);

        devopsEnvCommandDTO.setObjectId(workLoadId);
        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
    }

    private void updateWorkLoad(String resourceType, String name, String content, Long resourceId, Long userId) {
        //自定义资源关联command
        DevopsEnvCommandDTO devopsEnvCommandDTO = initDevopsEnvCommandDTO(resourceType, UPDATE_TYPE, userId);
        WorkLoad workLoad = workLoadMap.get(io.choerodon.devops.infra.util.StringUtils.toLowerCaseFirstOne(resourceType) + WORK_LOAD);
        workLoad.updateWorkLoad(devopsEnvCommandDTO, name, resourceId);
        devopsWorkloadResourceContentService.update(resourceType, resourceId, content);
    }


    private void updateWorkLoadCommandId(String kind, Long resourceId, Long commandId) {
        WorkLoad workLoad = workLoadMap.get(io.choerodon.devops.infra.util.StringUtils.toLowerCaseFirstOne(kind) + WORK_LOAD);
        workLoad.updateWorkLoadCommandId(resourceId, commandId);
    }

    private void deleteWorkload(String type, Long resourceId) {
        WorkLoad workLoad = workLoadMap.get(io.choerodon.devops.infra.util.StringUtils.toLowerCaseFirstOne(type) + WORK_LOAD);
        workLoad.deleteWorkload(resourceId);
    }

    private void checkMetadataInfo(String nowName, String oldName) {
        if (!nowName.equals(oldName)) {
            throw new CommonException("error.workload.resource.name.modify");
        }
    }

    public void updateDeployment(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId) {
        DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.selectByPrimaryKey(resourceId);
        checkMetadataInfo(newName, devopsDeploymentDTO.getName());
        devopsEnvCommandDTO.setObjectId(devopsDeploymentDTO.getId());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        //更新资源关联的最新command
        devopsDeploymentDTO.setCommandId(devopsEnvCommandDTO.getId());
        devopsDeploymentService.baseUpdate(devopsDeploymentDTO);
    }


    public void updateStatefulSet(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId) {
        DevopsStatefulSetDTO devopsStatefulSetDTO = devopsStatefulSetService.selectByPrimaryKey(resourceId);
        checkMetadataInfo(newName, devopsStatefulSetDTO.getName());
        devopsEnvCommandDTO.setObjectId(devopsStatefulSetDTO.getId());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        //更新资源关联的最新command
        devopsStatefulSetDTO.setCommandId(devopsEnvCommandDTO.getId());
        devopsStatefulSetService.baseUpdate(devopsStatefulSetDTO);
    }


    public void updateJob(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId) {
        DevopsJobDTO devopsJobDTO = devopsJobService.selectByPrimaryKey(resourceId);
        checkMetadataInfo(newName, devopsJobDTO.getName());
        devopsEnvCommandDTO.setObjectId(devopsJobDTO.getId());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        //更新资源关联的最新command
        devopsJobDTO.setCommandId(devopsEnvCommandDTO.getId());
        devopsJobService.baseUpdate(devopsJobDTO);
    }

    public void updateDaemonSet(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId) {
        DevopsDaemonSetDTO daemonSetDTO = devopsDaemonSetService.selectByPrimaryKey(resourceId);
        checkMetadataInfo(newName, daemonSetDTO.getName());
        devopsEnvCommandDTO.setObjectId(daemonSetDTO.getId());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        //更新资源关联的最新command
        daemonSetDTO.setCommandId(devopsEnvCommandDTO.getId());
        devopsDaemonSetService.baseUpdate(daemonSetDTO);
    }

    public void updateCronJob(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId) {
        DevopsCronJobDTO devopsCronJobDTO = devopsCronJobService.selectByPrimaryKey(resourceId);
        checkMetadataInfo(newName, devopsCronJobDTO.getName());
        devopsEnvCommandDTO.setObjectId(devopsCronJobDTO.getId());
        devopsEnvCommandDTO = devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);

        //更新资源关联的最新command
        devopsCronJobDTO.setCommandId(devopsEnvCommandDTO.getId());
        devopsCronJobService.baseUpdate(devopsCronJobDTO);
    }

    public static DevopsEnvCommandDTO initDevopsEnvCommandDTO(String resourceType, String operateType, Long userId) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        switch (operateType) {
            case CREATE_TYPE:
                devopsEnvCommandDTO.setCommandType(CommandType.CREATE.getType());
                break;
            case UPDATE_TYPE:
                devopsEnvCommandDTO.setCommandType(CommandType.UPDATE.getType());
                break;
            default:
                devopsEnvCommandDTO.setCommandType(CommandType.DELETE.getType());
                break;
        }

        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsEnvCommandDTO.setObject(resourceType.toLowerCase());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandDTO;
    }
}
