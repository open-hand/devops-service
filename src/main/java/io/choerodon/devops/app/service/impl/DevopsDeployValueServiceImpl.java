package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsDeployValueVO;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsDeployValueService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.PipelineAppDeployService;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsDeployValueDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.PipelineAppServiceDeployDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDeployValueMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:01 2019/4/10
 * Description:
 */
@Service
public class DevopsDeployValueServiceImpl implements DevopsDeployValueService {
    private static final Gson gson = new Gson();
    @Autowired
    private DevopsDeployValueMapper devopsDeployValueMapper;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private PipelineAppDeployService pipelineAppDeployService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;

    @Override
    public DevopsDeployValueVO createOrUpdate(Long projectId, DevopsDeployValueVO devopsDeployValueVO) {

        FileUtil.checkYamlFormat(devopsDeployValueVO.getValue());

        DevopsDeployValueDTO devopsDeployValueDTO = ConvertUtils.convertObject(devopsDeployValueVO, DevopsDeployValueDTO.class);
        devopsDeployValueDTO.setProjectId(projectId);
        devopsDeployValueDTO = baseCreateOrUpdate(devopsDeployValueDTO);
        return ConvertUtils.convertObject(devopsDeployValueDTO, DevopsDeployValueVO.class);
    }

    @Override
    public void delete(Long projectId, Long valueId) {
        baseDelete(valueId);
    }

    @Override
    public PageInfo<DevopsDeployValueVO> pageByOptions(Long projectId, Long appId, Long envId, PageRequest pageRequest, String params) {
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
        List<Long> connectedEnvList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        Long userId = null;
        if (!iamServiceClientOperator.isProjectOwner(DetailsHelper.getUserDetails().getUserId(), projectDTO)) {
            userId = DetailsHelper.getUserDetails().getUserId();
        }
        PageInfo<DevopsDeployValueDTO> deployValueDTOPageInfo = basePageByOptions(projectId, appId, envId, userId, pageRequest, params);
        PageInfo<DevopsDeployValueVO> page = ConvertUtils.convertPage(deployValueDTOPageInfo, DevopsDeployValueVO.class);
        page.setList(deployValueDTOPageInfo.getList().stream().map(devopsDeployValueDTO -> {

            DevopsDeployValueVO devopsDeployValueVO = ConvertUtils.convertObject(devopsDeployValueDTO, DevopsDeployValueVO.class);
            IamUserDTO iamUserDTO = iamServiceClientOperator.queryUserByUserId(devopsDeployValueDTO.getCreatedBy());
            devopsDeployValueVO.setCreateUserName(iamUserDTO.getLoginName());
            devopsDeployValueVO.setCreateUserUrl(iamUserDTO.getImageUrl());
            devopsDeployValueVO.setCreateUserRealName(iamUserDTO.getRealName());
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsDeployValueDTO.getEnvId());
            if (connectedEnvList.contains(devopsEnvironmentDTO.getClusterId())
                    && updatedEnvList.contains(devopsEnvironmentDTO.getClusterId())) {
                devopsDeployValueVO.setEnvStatus(true);
            }
            return devopsDeployValueVO;
        }).collect(Collectors.toList()));
        return page;
    }

    @Override
    public DevopsDeployValueVO query(Long projectId, Long valueId) {
        DevopsDeployValueVO devopsDeployValueVO = ConvertUtils.convertObject(baseQueryById(valueId), DevopsDeployValueVO.class);
        devopsDeployValueVO.setIndex(checkDelete(projectId, valueId));
        return devopsDeployValueVO;
    }

    @Override
    public void checkName(Long projectId, String name) {
        baseCheckName(projectId, name);
    }

    @Override
    public List<DevopsDeployValueVO> listByEnvAndApp(Long projectId, Long appId, Long envId) {
        return ConvertUtils.convertList(baseQueryByAppIdAndEnvId(projectId, appId, envId), DevopsDeployValueVO.class);
    }

    @Override
    public Boolean checkDelete(Long projectId, Long valueId) {
        List<PipelineAppServiceDeployDTO> pipelineAppServiceDeployDTOS = pipelineAppDeployService.baseQueryByValueId(valueId);
        if (pipelineAppServiceDeployDTOS == null || pipelineAppServiceDeployDTOS.isEmpty()) {
            List<AppServiceInstanceDTO> appServiceInstanceDTOS = appServiceInstanceService.baseListByValueId(valueId);
            if (appServiceInstanceDTOS == null || appServiceInstanceDTOS.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PageInfo<DevopsDeployValueDTO> basePageByOptions(Long projectId, Long appId, Long envId, Long userId, PageRequest pageRequest, String params) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        PageInfo<DevopsDeployValueDTO> deployValueDTOPageInfo = PageHelper
                .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsDeployValueMapper.listByOptions(projectId, appId, envId, userId, searchParamMap, paramMap));
        return deployValueDTOPageInfo;
    }

    @Override
    public DevopsDeployValueDTO baseCreateOrUpdate(DevopsDeployValueDTO devopsDeployValueDTO) {
        if (devopsDeployValueDTO.getId() == null) {
            if (devopsDeployValueMapper.insert(devopsDeployValueDTO) != 1) {
                throw new CommonException("error.insert.pipeline.value");
            }
        } else {
            devopsDeployValueDTO.setObjectVersionNumber(devopsDeployValueMapper.selectByPrimaryKey(devopsDeployValueDTO).getObjectVersionNumber());
            if (devopsDeployValueMapper.updateByPrimaryKeySelective(devopsDeployValueDTO) != 1) {
                throw new CommonException("error.update.pipeline.value");
            }
            devopsDeployValueDTO.setObjectVersionNumber(null);
        }
        return devopsDeployValueMapper.selectByPrimaryKey(devopsDeployValueDTO);
    }

    @Override
    public void baseDelete(Long valueId) {
        DevopsDeployValueDTO devopsDeployValueDTO = new DevopsDeployValueDTO();
        devopsDeployValueDTO.setId(valueId);
        devopsDeployValueMapper.deleteByPrimaryKey(devopsDeployValueDTO);
    }

    @Override
    public DevopsDeployValueDTO baseQueryById(Long valueId) {
        DevopsDeployValueDTO devopsDeployValueDTO = new DevopsDeployValueDTO();
        devopsDeployValueDTO.setId(valueId);
        return devopsDeployValueMapper.selectByPrimaryKey(devopsDeployValueDTO);
    }

    @Override
    public void baseCheckName(Long projectId, String name) {
        DevopsDeployValueDTO devopsDeployValueDTO = new DevopsDeployValueDTO();
        devopsDeployValueDTO.setProjectId(projectId);
        devopsDeployValueDTO.setName(name);
        if (!devopsDeployValueMapper.select(devopsDeployValueDTO).isEmpty()) {
            throw new CommonException("error.devops.pipeline.value.name.exit");
        }
    }

    @Override
    public List<DevopsDeployValueDTO> baseQueryByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        DevopsDeployValueDTO devopsDeployValueDTO = new DevopsDeployValueDTO();
        devopsDeployValueDTO.setProjectId(projectId);
        devopsDeployValueDTO.setAppServiceId(appId);
        devopsDeployValueDTO.setEnvId(envId);
        return devopsDeployValueMapper.select(devopsDeployValueDTO);
    }
}
