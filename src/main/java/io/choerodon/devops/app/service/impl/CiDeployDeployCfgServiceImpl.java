package io.choerodon.devops.app.service.impl;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.api.vo.DevopsDeployGroupAppConfigVO;
import io.choerodon.devops.api.vo.DevopsDeployGroupContainerConfigVO;
import io.choerodon.devops.api.vo.pipeline.CiDeployDeployCfgVO;
import io.choerodon.devops.app.service.CiDeployDeployCfgService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiDeployDeployCfgDTO;
import io.choerodon.devops.infra.mapper.CiDeployDeployCfgMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * CI deployment部署任务配置表(CiDeployDeployCfg)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-07 10:15:32
 */
@Service
public class CiDeployDeployCfgServiceImpl implements CiDeployDeployCfgService {

    private static final String DEVOPS_DEPLOYMENT_CONFIG_SAVE = "devops.deployment.config.save";
    private static final String DEVOPS_DEPLOYMENT_CONFIG_UPDATE = "devops.deployment.config.update";


    @Autowired
    private CiDeployDeployCfgMapper ciDeployDeployCfgMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiDeployDeployCfgDTO ciDeployDeployCfgDTO) {
        MapperUtil.resultJudgedInsertSelective(ciDeployDeployCfgMapper, ciDeployDeployCfgDTO, DEVOPS_DEPLOYMENT_CONFIG_SAVE);
    }

    @Override
    public CiDeployDeployCfgVO queryConfigVoById(Long configId) {
        CiDeployDeployCfgVO ciDeployDeployCfgVO = ConvertUtils.convertObject(queryConfigById(configId), CiDeployDeployCfgVO.class);
        if (ciDeployDeployCfgVO.getAppConfigJson() != null) {
            ciDeployDeployCfgVO.setAppConfig(JsonHelper.unmarshalByJackson(ciDeployDeployCfgVO.getAppConfigJson(), DevopsDeployGroupAppConfigVO.class));
        }
        if (ciDeployDeployCfgVO.getContainerConfigJson() != null) {
            ciDeployDeployCfgVO.setContainerConfig(JsonHelper.unmarshalByJackson(ciDeployDeployCfgVO.getContainerConfigJson(), new TypeReference<List<DevopsDeployGroupContainerConfigVO>>() {
            }));
        }
        return ciDeployDeployCfgVO;
    }

    @Override
    public CiDeployDeployCfgDTO queryConfigById(Long id) {
        return ciDeployDeployCfgMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAppIdAndDeployType(Long id, Long appId, String deployType) {
        CiDeployDeployCfgDTO ciDeployDeployCfgDTO = ciDeployDeployCfgMapper.selectByPrimaryKey(id);

        ciDeployDeployCfgDTO.setAppId(appId);
        ciDeployDeployCfgDTO.setDeployType(deployType);
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(ciDeployDeployCfgMapper, ciDeployDeployCfgDTO, DEVOPS_DEPLOYMENT_CONFIG_UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfigByPipelineId(Long ciPipelineId) {
        Assert.notNull(ciPipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        CiDeployDeployCfgDTO ciDeployDeployCfgDTO = new CiDeployDeployCfgDTO();
        ciDeployDeployCfgDTO.setCiPipelineId(ciPipelineId);
        ciDeployDeployCfgMapper.delete(ciDeployDeployCfgDTO);

    }
}

