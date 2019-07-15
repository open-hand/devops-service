package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.DevopsDeployValueVO;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.app.service.DevopsDeployValueService;
import io.choerodon.devops.domain.application.repository.DevopsDeployValueRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.PipelineAppDeployRepository;
import io.choerodon.devops.infra.dto.DevopsDeployValueDTO;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsDeployValueMapper;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.BeanUtils;
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
    private DevopsDeployValueRepository valueRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvironmentRepository devopsEnviromentRepository;
    @Autowired
    private PipelineAppDeployRepository appDeployRepository;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private DevopsDeployValueMapper valueMapper;

    @Override
    public DevopsDeployValueVO createOrUpdate(Long projectId, DevopsDeployValueVO pipelineValueDTO) {

        FileUtil.checkYamlFormat(pipelineValueDTO.getValue());

        DevopsDeployValueE pipelineValueE = ConvertHelper.convert(pipelineValueDTO, DevopsDeployValueE.class);
        pipelineValueE.setProjectId(projectId);
        pipelineValueE = valueRepository.baseCreateOrUpdate(pipelineValueE);
        return ConvertHelper.convert(pipelineValueE, DevopsDeployValueVO.class);
    }

    @Override
    public void delete(Long projectId, Long valueId) {
        valueRepository.baseDelete(valueId);
    }

    @Override
    public PageInfo<DevopsDeployValueVO> listByOptions(Long projectId, Long appId, Long envId, PageRequest
            pageRequest, String params) {
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        List<Long> connectedEnvList = envUtil.getConnectedEnvList();
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList();
        Long userId = null;
        if (!iamRepository.isProjectOwner(DetailsHelper.getUserDetails().getUserId(), projectE)) {
            userId = DetailsHelper.getUserDetails().getUserId();
        }
        PageInfo<DevopsDeployValueVO> valueDTOS = ConvertPageHelper.convertPageInfo(valueRepository.basePageByOptions(projectId, appId, envId, userId, pageRequest, params), DevopsDeployValueVO.class);
        PageInfo<DevopsDeployValueVO> page = new PageInfo<>();
        BeanUtils.copyProperties(valueDTOS, page);
        page.setList(valueDTOS.getList().stream().peek(t -> {
            UserE userE = iamRepository.queryUserByUserId(t.getCreateBy());
            t.setCreateUserName(userE.getLoginName());
            t.setCreateUserUrl(userE.getImageUrl());
            t.setCreateUserRealName(userE.getRealName());
            DevopsEnvironmentE devopsEnvironmentE = devopsEnviromentRepository.baseQueryById(t.getEnvId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                t.setEnvStatus(true);
            }
        }).collect(Collectors.toList()));
        return page;
    }

    @Override
    public DevopsDeployValueVO queryById(Long projectId, Long valueId) {
        DevopsDeployValueVO valueDTO = ConvertHelper.convert(valueRepository.baseQueryById(valueId), DevopsDeployValueVO.class);
        valueDTO.setIndex(checkDelete(projectId, valueId));
        return valueDTO;
    }

    @Override
    public void checkName(Long projectId, String name) {
        valueRepository.baseCheckName(projectId, name);
    }

    @Override
    public List<DevopsDeployValueVO> queryByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        return ConvertHelper.convertList(valueRepository.baseQueryByAppIdAndEnvId(projectId, appId, envId), DevopsDeployValueVO.class);
    }

    @Override
    public Boolean checkDelete(Long projectId, Long valueId) {
        List<PipelineAppDeployE> appDeployEList = appDeployRepository.queryByValueId(valueId);
        if (appDeployEList == null || appDeployEList.isEmpty()) {
            List<ApplicationInstanceE> instanceEList = applicationInstanceRepository.listByValueId(valueId);
            if (instanceEList == null || instanceEList.isEmpty()) {
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
        PageInfo<DevopsDeployValueDTO> devopsAutoDeployDOS = PageHelper
                .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> valueMapper.listByOptions(projectId, appId, envId, userId, searchParamMap, paramMap));
        return devopsAutoDeployDOS;
    }

    @Override
    public DevopsDeployValueDTO baseCreateOrUpdate(DevopsDeployValueDTO devopsDeployValueDTO) {
        if (devopsDeployValueDTO.getId() == null) {
            if (valueMapper.insert(devopsDeployValueDTO) != 1) {
                throw new CommonException("error.insert.pipeline.value");
            }
        } else {
            devopsDeployValueDTO.setObjectVersionNumber(valueMapper.selectByPrimaryKey(devopsDeployValueDTO).getObjectVersionNumber());
            if (valueMapper.updateByPrimaryKeySelective(devopsDeployValueDTO) != 1) {
                throw new CommonException("error.update.pipeline.value");
            }
            devopsDeployValueDTO.setObjectVersionNumber(null);
        }
        return valueMapper.selectByPrimaryKey(devopsDeployValueDTO);
    }

    @Override
    public void baseDelete(Long valueId) {
        DevopsDeployValueDTO pipelineValueDO = new DevopsDeployValueDTO();
        pipelineValueDO.setId(valueId);
        valueMapper.deleteByPrimaryKey(pipelineValueDO);
    }

    @Override
    public DevopsDeployValueDTO baseQueryById(Long valueId) {
        DevopsDeployValueDTO pipelineValueDO = new DevopsDeployValueDTO();
        pipelineValueDO.setId(valueId);
        return valueMapper.selectByPrimaryKey(pipelineValueDO);
    }

    @Override
    public void baseCheckName(Long projectId, String name) {
        DevopsDeployValueDTO pipelineValueDO = new DevopsDeployValueDTO();
        pipelineValueDO.setProjectId(projectId);
        pipelineValueDO.setName(name);
        if (valueMapper.select(pipelineValueDO).size() > 0) {
            throw new CommonException("error.devops.pipeline.value.name.exit");
        }
    }

    @Override
    public List<DevopsDeployValueDTO> baseQueryByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        DevopsDeployValueDTO valueDO = new DevopsDeployValueDTO();
        valueDO.setProjectId(projectId);
        valueDO.setAppId(appId);
        valueDO.setEnvId(envId);
        return valueMapper.select(valueDO);
    }
}
