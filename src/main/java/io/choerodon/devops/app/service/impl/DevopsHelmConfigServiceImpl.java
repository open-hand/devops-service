package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsHelmConfigVO;
import io.choerodon.devops.app.service.DevopsHelmConfigService;
import io.choerodon.devops.infra.dto.DevopsHelmConfigDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHelmConfigMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsHelmConfigServiceImpl implements DevopsHelmConfigService {

    @Autowired
    private DevopsHelmConfigMapper devopsHelmConfigMapper;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    public List<DevopsHelmConfigVO> listHelmConfig(Long projectId) {

        List<DevopsHelmConfigVO> devopsHelmConfigVOList = new ArrayList<>();

        DevopsHelmConfigVO devopsHelmConfigVO1 = new DevopsHelmConfigVO();
        devopsHelmConfigVO1.setUrl("http://www.example.com/org/projects/");
        devopsHelmConfigVO1.setUsername("username");
        devopsHelmConfigVO1.setPassword("password");
        devopsHelmConfigVO1.setName("测试仓库1");
        devopsHelmConfigVO1.setRepoPrivate(true);
        devopsHelmConfigVO1.setRepoDefault(true);
        devopsHelmConfigVO1.setResourceType("organization");
        devopsHelmConfigVO1.setCreationDate(new Date());
        devopsHelmConfigVO1.setCreatorImageUrl("http://minio.c7n.devops.hand-china.com/iam-service/0/CHOERODON-MINIO/54d21810ba514c87966d28579e65a9ec@src=http___5b0988e595225.cdn.sohucs.com_images_20200424_7c24b1d510b14d0599d69f6c4052867d.jpeg&refer=http___5b0988e595225.cdn.sohucs.jfif");
        devopsHelmConfigVO1.setCreatorLoginName("25147");
        devopsHelmConfigVO1.setCreatorRealName("周扒皮");

        DevopsHelmConfigVO devopsHelmConfigVO2 = new DevopsHelmConfigVO();
        devopsHelmConfigVO2.setUrl("http://www.example.com/org/projects/");
        devopsHelmConfigVO2.setUsername("username");
        devopsHelmConfigVO2.setPassword("password");
        devopsHelmConfigVO2.setName("测试仓库1");
        devopsHelmConfigVO2.setRepoPrivate(false);
        devopsHelmConfigVO2.setRepoDefault(false);
        devopsHelmConfigVO2.setResourceType("project");
        devopsHelmConfigVO2.setCreationDate(new Date());
        devopsHelmConfigVO2.setCreatorImageUrl("http://minio.c7n.devops.hand-china.com/iam-service/0/CHOERODON-MINIO/54d21810ba514c87966d28579e65a9ec@src=http___5b0988e595225.cdn.sohucs.com_images_20200424_7c24b1d510b14d0599d69f6c4052867d.jpeg&refer=http___5b0988e595225.cdn.sohucs.jfif");
        devopsHelmConfigVO2.setCreatorLoginName("25147");
        devopsHelmConfigVO2.setCreatorRealName("周扒皮");

        devopsHelmConfigVOList.add(devopsHelmConfigVO1);
        devopsHelmConfigVOList.add(devopsHelmConfigVO2);
        List<DevopsHelmConfigDTO> devopsHelmConfigDTOS = new ArrayList<>();

        // 查询项目层设置helm仓库
        DevopsHelmConfigDTO helmConfigSearchDTOOnProject = new DevopsHelmConfigDTO();
        helmConfigSearchDTOOnProject.setResourceId(projectId);
        helmConfigSearchDTOOnProject.setResourceType(ResourceLevel.PROJECT.value());
        List<DevopsHelmConfigDTO> devopsHelmConfigDTOListOnProject = devopsHelmConfigMapper.select(helmConfigSearchDTOOnProject);
        devopsHelmConfigDTOS.addAll(devopsHelmConfigDTOListOnProject);
        DevopsHelmConfigDTO defaultDevopsHelmConfigDTOOnProject = null;
        if (devopsHelmConfigDTOS.size() != 0) {
            for (DevopsHelmConfigDTO devopsHelmConfigDTO : devopsHelmConfigDTOS) {
                if (Boolean.TRUE.equals(devopsHelmConfigDTO.getRepoDefault())) {
                    defaultDevopsHelmConfigDTOOnProject = devopsHelmConfigDTO;
                }
            }
        }
        devopsHelmConfigDTOS.remove(defaultDevopsHelmConfigDTOOnProject);
        devopsHelmConfigDTOS = devopsHelmConfigDTOS.stream().sorted(Comparator.comparing(DevopsHelmConfigDTO::getCreationDate, (i, j) -> {
            if (i.before(j)) {
                return -1;
            } else if (i.equals(j)) {
                return 0;
            }
            return 1;
        }).reversed()).collect(Collectors.toList());

        // 查询组织层helm仓库
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId, false, false, false);
        DevopsHelmConfigDTO helmConfigSearchDTOOnOrganization = new DevopsHelmConfigDTO();
        helmConfigSearchDTOOnOrganization.setResourceId(projectDTO.getOrganizationId());
        helmConfigSearchDTOOnOrganization.setResourceType(ResourceLevel.ORGANIZATION.value());
        helmConfigSearchDTOOnOrganization.setRepoDefault(true);
        DevopsHelmConfigDTO devopsHelmConfigDTOtOnOrganization = devopsHelmConfigMapper.selectOne(helmConfigSearchDTOOnOrganization);
        if (devopsHelmConfigDTOtOnOrganization != null) {
            devopsHelmConfigDTOS.add(0, devopsHelmConfigDTOtOnOrganization);
        } else {
            // 如果组织层的仓库为空，查询平台默认
            DevopsHelmConfigDTO helmConfigSearchDTOOnSite = new DevopsHelmConfigDTO();
            helmConfigSearchDTOOnSite.setResourceType(ResourceLevel.SITE.value());
            helmConfigSearchDTOOnSite.setRepoDefault(true);
            DevopsHelmConfigDTO devopsHelmConfigDTOListOnSite = devopsHelmConfigMapper.selectOne(helmConfigSearchDTOOnSite);
            if (devopsHelmConfigDTOListOnSite == null) {
                throw new CommonException("error.helm.config.site.exist");
            }
            devopsHelmConfigDTOS.add(0, devopsHelmConfigDTOListOnSite);
        }

        if (defaultDevopsHelmConfigDTOOnProject != null) {
            devopsHelmConfigDTOS.add(0, defaultDevopsHelmConfigDTOOnProject);
        }

        List<DevopsHelmConfigVO> devopsHelmConfigVOS = ConvertUtils.convertList(devopsHelmConfigDTOS, DevopsHelmConfigVO.class);

        Set<Long> creatorIds = devopsHelmConfigDTOS.stream().map(DevopsHelmConfigDTO::getCreatedBy).collect(Collectors.toSet());
        List<IamUserDTO> iamUserDTOList = baseServiceClientOperator.listUsersByIds(new ArrayList<>(creatorIds));
        Map<Long, IamUserDTO> iamUserDTOMap = iamUserDTOList.stream().collect(Collectors.toMap(IamUserDTO::getId, Function.identity()));

        devopsHelmConfigVOS.forEach(c -> {
            IamUserDTO creator = iamUserDTOMap.get(c.getCreatedBy());
            if (creator != null) {
                c.setCreatorImageUrl(creator.getImageUrl());
                c.setCreatorLoginName(creator.getLoginName());
                c.setCreatorRealName(creator.getRealName());
            }
        });
        return devopsHelmConfigVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DevopsHelmConfigVO createDevopsHelmConfigOnProjectLevel(Long projectId, DevopsHelmConfigVO devopsHelmConfigVO) {
        DevopsHelmConfigDTO devopsHelmConfigDTO = ConvertUtils.convertObject(devopsHelmConfigVO, DevopsHelmConfigDTO.class);
        devopsHelmConfigDTO.setResourceType(ResourceLevel.PROJECT.value());
        devopsHelmConfigDTO.setResourceId(projectId);

        DevopsHelmConfigDTO result = MapperUtil.resultJudgedInsertSelective(devopsHelmConfigMapper, devopsHelmConfigDTO, "error.helm.config.insert");
        return ConvertUtils.convertObject(result, DevopsHelmConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DevopsHelmConfigVO updateDevopsHelmConfigOnProjectLevel(Long projectId, DevopsHelmConfigVO devopsHelmConfigVO) {
        DevopsHelmConfigDTO devopsHelmConfigDTO = ConvertUtils.convertObject(devopsHelmConfigVO, DevopsHelmConfigDTO.class);
        devopsHelmConfigDTO.setResourceType(ResourceLevel.PROJECT.value());
        devopsHelmConfigDTO.setResourceId(projectId);

        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHelmConfigMapper, devopsHelmConfigDTO, "error.helm.config.update");

        return ConvertUtils.convertObject(devopsHelmConfigDTO, DevopsHelmConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevopsHelmConfig(Long projectId, Long helmConfigId) {
        DevopsHelmConfigDTO devopsHelmConfigDTO = new DevopsHelmConfigDTO();
        devopsHelmConfigDTO.setResourceId(projectId);
        devopsHelmConfigDTO.setResourceType(ResourceLevel.PROJECT.value());
        devopsHelmConfigDTO.setId(helmConfigId);
        devopsHelmConfigMapper.delete(devopsHelmConfigDTO);
    }

    @Override
    public DevopsHelmConfigVO queryDevopsHelmConfig(Long projectId, Long helmConfigId) {
        DevopsHelmConfigDTO devopsHelmConfigSearchDTO = new DevopsHelmConfigDTO();
        devopsHelmConfigSearchDTO.setResourceType(ResourceLevel.PROJECT.value());
        devopsHelmConfigSearchDTO.setResourceId(projectId);
        devopsHelmConfigSearchDTO.setId(helmConfigId);

        DevopsHelmConfigDTO devopsHelmConfigDTO = devopsHelmConfigMapper.selectOne(devopsHelmConfigSearchDTO);

        return ConvertUtils.convertObject(devopsHelmConfigDTO, DevopsHelmConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultDevopsHelmConfig(Long projectId, Long helmConfigId) {
        DevopsHelmConfigDTO devopsHelmConfigDTO = devopsHelmConfigMapper.selectByPrimaryKey(helmConfigId);

        // 先将项目层的所有仓库是否为默认置为false
        devopsHelmConfigMapper.updateAllHelmConfigRepoDefaultToFalse(projectId);

        // 如果helm默认仓库仍是项目层，那么将指定的仓库设置为默认仓库
        if (!ResourceLevel.SITE.value().equals(devopsHelmConfigDTO.getResourceType()) && !ResourceLevel.ORGANIZATION.value().equals(devopsHelmConfigDTO.getResourceType())) {
            devopsHelmConfigMapper.updateHelmConfigRepoDefaultToTrue(projectId, helmConfigId);
        }
    }

    @Override
    public DevopsHelmConfigDTO queryDefaultDevopsHelmConfigByLevel(String resourceType) {
        DevopsHelmConfigDTO devopsHelmConfigSearchDTO = new DevopsHelmConfigDTO();
        devopsHelmConfigSearchDTO.setRepoDefault(true);
        devopsHelmConfigSearchDTO.setResourceType(resourceType);
        return devopsHelmConfigMapper.selectOne(devopsHelmConfigSearchDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDevopsHelmConfig(DevopsHelmConfigDTO devopsHelmConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsHelmConfigMapper, devopsHelmConfigDTO, "error.helm.config.insert");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDevopsHelmConfig(DevopsHelmConfigDTO devopsHelmConfigDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHelmConfigMapper,devopsHelmConfigDTO,"error.helm.config.update");
    }
}
