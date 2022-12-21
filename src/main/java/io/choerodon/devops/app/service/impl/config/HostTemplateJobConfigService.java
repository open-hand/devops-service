package io.choerodon.devops.app.service.impl.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.utils.ConvertUtils;
import io.choerodon.devops.api.vo.template.CiTemplateJobVO;
import io.choerodon.devops.api.vo.template.CiTemplatePipelineVO;
import io.choerodon.devops.api.vo.template.CiTplHostDeployInfoCfgVO;
import io.choerodon.devops.infra.dto.CiTplHostDeployInfoCfgDTO;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.mapper.CiTplHostDeployInfoMapper;
import io.choerodon.devops.infra.utils.PipelineTemplateUtils;

@Service
public class HostTemplateJobConfigService extends TemplateJobConfigService {

    @Autowired
    private CiTplHostDeployInfoMapper ciTplHostDeployInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long baseInsert(CiTemplateJobVO ciTemplateJobVO) {
        CiTplHostDeployInfoCfgDTO ciTplHostDeployInfoCfgDTO
                = ConvertUtils.convertObject(ciTemplateJobVO.getDevopsCiHostDeployInfoVO(), CiTplHostDeployInfoCfgDTO.class);
        ciTplHostDeployInfoCfgDTO.setId(null);
        ciTplHostDeployInfoMapper.insertSelective(ciTplHostDeployInfoCfgDTO);
        return ciTplHostDeployInfoCfgDTO.getId();
    }

    @Override
    public void fillCdJobConfig(CiTemplateJobVO ciTemplateJobVO) {
        CiTplHostDeployInfoCfgVO ciTplHostDeployInfoCfgVO = fillHostDeployConfig(ciTemplateJobVO.getConfigId(), ciTemplateJobVO.getBuiltIn());
        ciTemplateJobVO.setDevopsCiHostDeployInfoVO(ciTplHostDeployInfoCfgVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(CiTemplateJobVO ciTemplateJobVO) {
        if (ciTemplateJobVO.getConfigId() == null) {
            return;
        }
        ciTplHostDeployInfoMapper.deleteByPrimaryKey(ciTemplateJobVO.getConfigId());
    }

    private CiTplHostDeployInfoCfgVO fillHostDeployConfig(Long configId, boolean builtIn) {
        if (configId == null && !builtIn) {
            return new CiTplHostDeployInfoCfgVO();
        }
        //如果是内置的则要看，nodejs则是其他，Java 则是主机
        CiTemplatePipelineVO ciTemplatePipelineVO = (CiTemplatePipelineVO) PipelineTemplateUtils.threadLocal.get();
        if (ciTemplatePipelineVO == null) {
            CiTplHostDeployInfoCfgVO ciTplHostDeployInfoCfgVO = new CiTplHostDeployInfoCfgVO();
            ciTplHostDeployInfoCfgVO.setHostDeployType(DeployObjectTypeEnum.JAR.value());
            return ciTplHostDeployInfoCfgVO;
        }
        PipelineTemplateUtils.threadLocal.remove();
        if (StringUtils.equalsIgnoreCase(ciTemplatePipelineVO.getName().trim(), "Java(Maven构建):构建+主机部署") && ciTemplatePipelineVO.getBuiltIn()) {
            CiTplHostDeployInfoCfgVO ciTplHostDeployInfoCfgVO = new CiTplHostDeployInfoCfgVO();
            ciTplHostDeployInfoCfgVO.setHostDeployType(DeployObjectTypeEnum.JAR.value());
            return ciTplHostDeployInfoCfgVO;
        } else if (StringUtils.equalsIgnoreCase(ciTemplatePipelineVO.getName().trim(), "Node.js(Npm构建):构建+主机部署") && ciTemplatePipelineVO.getBuiltIn()) {
            CiTplHostDeployInfoCfgVO ciTplHostDeployInfoCfgVO = new CiTplHostDeployInfoCfgVO();
            ciTplHostDeployInfoCfgVO.setHostDeployType(DeployObjectTypeEnum.OTHER.value());
            return ciTplHostDeployInfoCfgVO;
        } else {
            return ConvertUtils.convertObject(ciTplHostDeployInfoMapper.selectByPrimaryKey(configId), CiTplHostDeployInfoCfgVO.class);
        }
    }
}
