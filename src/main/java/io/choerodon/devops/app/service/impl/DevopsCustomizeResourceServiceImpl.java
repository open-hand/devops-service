package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.util.*;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCustomizeResourceDTO;
import io.choerodon.devops.api.vo.DevopsCustomizeResourceReqDTO;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.app.service.DevopsCustomizeResourceService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.util.*;
import io.choerodon.devops.infra.enums.CommandStatus;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.enums.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

/**
 * Created by Sheep on 2019/6/26.
 */

@Service
public class DevopsCustomizeResourceServiceImpl implements DevopsCustomizeResourceService {

    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String CHOERODON_IO_RESOURCE = "choerodon.io/resource";
    public static final String METADATA = "metadata";
    public static final String CUSTOM = "custom";
    public static final String LABELS = "labels";
    public static final String KIND = "kind";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private List<String> RESOURCETYPE = Arrays.asList(ResourceType.SERVICE.getType(), ResourceType.INGRESS.getType(), ResourceType.CONFIGMAP.getType(), ResourceType.SECRET.getType(), ResourceType.C7NHELMRELEASE.getType(), ResourceType.CERTIFICATE.getType());


    @Autowired
    private IamRepository iamRepository;

    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;

    @Autowired
    private DevopsCustomizeResourceRepository devopsCustomizeResourceRepository;

    @Autowired
    private DevopsCustomizeResourceContentRepository devopsCustomizeResourceContentRepository;

    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;

    @Autowired
    private EnvUtil envUtil;

    @Autowired
    private UserAttrRepository userAttrRepository;

    @Autowired
    private GitlabRepository gitlabRepository;

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdateResource(Long projectId, DevopsCustomizeResourceReqDTO devopsCustomizeResourceReqDTO, MultipartFile contentFile) {

        String content = devopsCustomizeResourceReqDTO.getContent();

        String resourceFilePath = String.format("custom-%s.yaml", GenerateUUID.generateUUID().substring(0, 5));

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsCustomizeResourceReqDTO.getEnvId());

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        ProjectVO projectE = iamRepository.queryIamProject(projectId);

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);


        String path = String.format("tmp%s%s%s%s%sresource", FILE_SEPARATOR, projectE.getCode(), FILE_SEPARATOR, devopsEnvironmentE.getCode(), FILE_SEPARATOR);

        if (contentFile != null) {
            content = FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, contentFile)));
            FileUtil.deleteDirectory(new File(path));
        }

        Yaml yaml = new Yaml();
        List<Object> objects = new ArrayList<>();

        //处理每个k8s资源对象
        try {
            for (Object data : yaml.loadAll(content)) {

                Map<String, Object> datas = (Map<String, Object>) data;

                Object kind = datas.get(KIND);

                LinkedHashMap metadata = (LinkedHashMap) datas.get(METADATA);
                //校验yaml文件内资源属性是否合法,并返回资源的name
                String name = CheckResource(metadata, kind);

                //添加自定义标签
                LinkedHashMap labels = (LinkedHashMap) metadata.get(LABELS);

                if (labels == null) {
                    labels = new LinkedHashMap();
                }
                labels.put(CHOERODON_IO_RESOURCE, CUSTOM);
                metadata.put(LABELS, labels);
                datas.put(METADATA, metadata);
                objects.add(datas);

            HandleCoustomizeResource(projectId, devopsCustomizeResourceReqDTO.getEnvId(), FileUtil.getYaml().dump(datas), kind.toString(), name, devopsCustomizeResourceReqDTO.getType(), devopsCustomizeResourceReqDTO.getResourceId(), resourceFilePath, null);

            }
        }catch (Exception e) {
            throw e;
        }
        if (devopsCustomizeResourceReqDTO.getType().equals(CREATE)) {
            gitlabRepository.createFile(devopsEnvironmentE.getGitlabEnvProjectId().intValue(), resourceFilePath, FileUtil.getYaml().dumpAll(objects.iterator()),
                    "ADD FILE", TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        } else {
            //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
            String gitOpsPath = envUtil.handDevopsEnvGitRepository(devopsEnvironmentE.getProjectE().getId(), devopsEnvironmentE.getCode(), devopsEnvironmentE.getEnvIdRsa());

            DevopsCustomizeResourceE devopsCustomizeResourceE = devopsCustomizeResourceRepository.query(devopsCustomizeResourceReqDTO.getResourceId());
            if (!gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    devopsCustomizeResourceE.getFilePath())) {
                throw new CommonException("error.fileResource.not.exist");
            }
            //获取更新内容
            ResourceConvertToYamlHandler resourceConvertToYamlHandler = new ResourceConvertToYamlHandler();
            String updateContent = resourceConvertToYamlHandler.getUpdateContent(objects.get(0), false, null, devopsCustomizeResourceE.getFilePath(), ResourceType.CUSTOM.getType(), gitOpsPath, CommandType.UPDATE.getType());
            gitlabRepository.updateFile(devopsEnvironmentE.getGitlabEnvProjectId().intValue(), devopsCustomizeResourceE.getFilePath(), updateContent, "UPDATE FILE", TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }

    }

    @Override
    public void createOrUpdateResourceByGitOps(String type, DevopsCustomizeResourceE devopsCustomizeResourceE, Long envId, Long userId) {

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsCustomizeResourceE.getEnvId());
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        HandleCoustomizeResource(devopsCustomizeResourceE.getProjectId(), devopsCustomizeResourceE.getEnvId(), devopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getContent(), devopsCustomizeResourceE.getK8sKind(), devopsCustomizeResourceE.getName(), type, devopsCustomizeResourceE.getId(), devopsCustomizeResourceE.getFilePath(), userId);
    }


    @Override
    public void deleteResource(Long resourceId) {

        DevopsCustomizeResourceE devopsCustomizeResourceE = devopsCustomizeResourceRepository.query(resourceId);

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsCustomizeResourceE.getEnvId());

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);

        HandleCoustomizeResource(null, null, null, null, null, DELETE, resourceId, null, null);

        //判断当前容器目录下是否存在环境对应的gitops文件目录，不存在则克隆
        String gitOpsPath = envUtil.handDevopsEnvGitRepository(devopsEnvironmentE.getProjectE().getId(), devopsEnvironmentE.getCode(), devopsEnvironmentE.getEnvIdRsa());

        //判断gitops库里面是否有该文件，没有文件直接删除对象
        if (!gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                devopsCustomizeResourceE.getFilePath())) {
            devopsCustomizeResourceRepository.delete(resourceId);
            devopsCustomizeResourceContentRepository.delete(devopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getId());
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                    .queryByEnvIdAndResource(devopsEnvironmentE.getId(), resourceId, ObjectType.CUSTOM.getType());
            if (devopsEnvFileResourceE != null) {
                devopsEnvFileResourceRepository.deleteFileResource(devopsEnvFileResourceE.getId());
            }
            return;
        }

        List<DevopsCustomizeResourceE> devopsCustomizeResourceES = devopsCustomizeResourceRepository.listByEnvAndFilePath(devopsEnvironmentE.getId(), devopsCustomizeResourceE.getFilePath());

        //如果对象所在文件只有一个对象，则直接删除文件,否则把对象从文件中去掉，更新文件
        if (devopsCustomizeResourceES.size() == 1) {
            if (devopsCustomizeResourceES.get(0).getId().equals(resourceId)) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        devopsCustomizeResourceE.getFilePath(),
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            } else {
                devopsCustomizeResourceRepository.delete(resourceId);
                devopsCustomizeResourceContentRepository.delete(devopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getId());
            }
        } else {
            //获取更新内容
            DevopsCustomizeResourceContentE devopsCustomizeResourceContentE = devopsCustomizeResourceContentRepository.query(devopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getId());
            ResourceConvertToYamlHandler resourceConvertToYamlHandler = new ResourceConvertToYamlHandler();
            String updateContent = resourceConvertToYamlHandler.getUpdateContent(FileUtil.getYaml().load(devopsCustomizeResourceContentE.getContent()), false, null, devopsCustomizeResourceE.getFilePath(), ResourceType.CUSTOM.getType(), gitOpsPath, CommandType.DELETE.getType());
            gitlabRepository.updateFile(devopsEnvironmentE.getGitlabEnvProjectId().intValue(), devopsCustomizeResourceE.getFilePath(), updateContent, "UPDATE FILE", TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        }
    }


    @Override
    public DevopsCustomizeResourceDTO getDevopsCustomizeResourceDetail(Long resourceId) {
        DevopsCustomizeResourceE devopsCustomizeResourceE = devopsCustomizeResourceRepository.queryDetail(resourceId);
        return ConvertHelper.convert(devopsCustomizeResourceE, DevopsCustomizeResourceDTO.class);
    }


    @Override
    public PageInfo<DevopsCustomizeResourceDTO> pageResources(Long envId, PageRequest pageRequest, String params) {
        PageInfo<DevopsCustomizeResourceE> devopsCustomizeResourceEPageInfo = devopsCustomizeResourceRepository.pageDevopsCustomizeResourceE(envId, pageRequest, params);
        List<Long> connectedEnvList = envUtil.getConnectedEnvList();
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList();
        PageInfo<DevopsCustomizeResourceDTO> devopsCustomizeResourceDTOPageInfo = ConvertPageHelper.convertPageInfo(devopsCustomizeResourceEPageInfo, DevopsCustomizeResourceDTO.class);
        devopsCustomizeResourceDTOPageInfo.getList().forEach(devopsCustomizeResourceDTO -> {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                devopsCustomizeResourceDTO.setEnvStatus(true);
            }
        });
        return devopsCustomizeResourceDTOPageInfo;
    }


    @Override
    public void deleteResourceByGitOps(Long resourceId) {

        DevopsCustomizeResourceE devopsCustomizeResourceE = devopsCustomizeResourceRepository.query(resourceId);

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsCustomizeResourceE.getEnvId());
        envUtil.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        devopsEnvCommandRepository.listByObjectAll(ObjectType.CUSTOM.getType(), resourceId).forEach(devopsEnvCommandE -> devopsEnvCommandRepository.deleteCommandById(devopsEnvCommandE));
        devopsCustomizeResourceRepository.delete(resourceId);
        devopsCustomizeResourceContentRepository.delete(devopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getId());
    }


    private void HandleCoustomizeResource(Long projectId, Long envId, String content, String kind, String name, String type, Long resourceId, String filePath, Long userId) {

        if (CREATE.equals(type)) {

            //校验新增的类型是否已存在
            devopsCustomizeResourceRepository.checkExist(envId, kind, name);

            //创建自定义资源的yaml文件内容
            DevopsCustomizeResourceContentE devopsCustomizeResourceContentE = new DevopsCustomizeResourceContentE();
            devopsCustomizeResourceContentE.setContent(content);

            //自定义资源关联command
            DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(type, userId);
            devopsEnvCommandE = devopsEnvCommandRepository.create(devopsEnvCommandE);

            //创建自定义资源
            DevopsCustomizeResourceE devopsCustomizeResourceE = new DevopsCustomizeResourceE(projectId, envId, devopsCustomizeResourceContentRepository.create(devopsCustomizeResourceContentE).getId(), devopsEnvCommandE.getId(), kind, name, filePath, userId);
            devopsCustomizeResourceE = devopsCustomizeResourceRepository.create(devopsCustomizeResourceE);

            devopsEnvCommandE.setObjectId(devopsCustomizeResourceE.getId());
            devopsEnvCommandRepository.update(devopsEnvCommandE);
        } else if (UPDATE.equals(type)) {
            DevopsCustomizeResourceE devopsCustomizeResourceE = devopsCustomizeResourceRepository.query(resourceId);
            devopsCustomizeResourceE.setLastUpdateBy(userId);
            if (!kind.equals(devopsCustomizeResourceE.getK8sKind())) {
                throw new CommonException("error.custom.resource.kind.modify");
            }
            if (!name.equals(devopsCustomizeResourceE.getName())) {
                throw new CommonException("error.custom.resource.name.modify");
            }

            //更新自定义资源的yaml文件内容
            DevopsCustomizeResourceContentE devopsCustomizeResourceContentE = devopsCustomizeResourceContentRepository.query(devopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getId());
            devopsCustomizeResourceContentE.setContent(content);
            devopsCustomizeResourceContentRepository.update(devopsCustomizeResourceContentE);

            //自定义资源关联command
            DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(type, userId);
            devopsEnvCommandE.setObjectId(devopsCustomizeResourceE.getId());
            devopsEnvCommandE = devopsEnvCommandRepository.create(devopsEnvCommandE);


            //更新自定义资源关联的最新command
            devopsCustomizeResourceE.setDevopsEnvCommandE(new DevopsEnvCommandE(devopsEnvCommandE.getId()));
            devopsCustomizeResourceRepository.update(devopsCustomizeResourceE);

        } else {
            DevopsCustomizeResourceE devopsCustomizeResourceE = devopsCustomizeResourceRepository.query(resourceId);

            //自定义资源关联command
            DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(DELETE, userId);
            devopsEnvCommandE.setObjectId(resourceId);
            devopsEnvCommandE = devopsEnvCommandRepository.create(devopsEnvCommandE);

            //更新自定义资源关联的最新command
            devopsCustomizeResourceE.setDevopsEnvCommandE(new DevopsEnvCommandE(devopsEnvCommandE.getId()));
            devopsCustomizeResourceRepository.update(devopsCustomizeResourceE);
        }
    }


    private DevopsEnvCommandE initDevopsEnvCommandE(String type, Long userId) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        switch (type) {
            case CREATE:
                devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
                break;
            case UPDATE:
                devopsEnvCommandE.setCommandType(CommandType.UPDATE.getType());
                break;
            default:
                devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
                break;
        }
        devopsEnvCommandE.setCreatedBy(userId);
        devopsEnvCommandE.setObject(ObjectType.CUSTOM.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        return devopsEnvCommandE;
    }


    private String CheckResource(LinkedHashMap metadata, Object kind) {
        if (kind == null) {
            throw new CommonException("custom.resource.kind.not.found");
        }
        //禁止创建平台已有的资源
        if (RESOURCETYPE.contains(kind.toString())) {
            throw new CommonException("error.kind.is.forbidden");
        }
        if (metadata == null) {
            throw new CommonException("custom.resource.metadata.not.found");
        }

        Object name = metadata.get("name");
        if (name == null) {
            throw new CommonException("custom.resource.name.not.found");
        }
        return name.toString();
    }


}
