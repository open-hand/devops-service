package io.choerodon.devops.app.eventhandler.pipeline.job;

import static io.choerodon.devops.infra.constant.ExceptionConstants.CiHostDeployCode.DEVOPS_HOST_DEPLOY_INFO_CREATE;
import static io.choerodon.devops.infra.constant.ExceptionConstants.CiJobCode.DEVOPS_JOB_CONFIG_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.PipelineCheckConstant.DEVOPS_JOB_ID_IS_NULL;
import static io.choerodon.devops.infra.constant.ResourceCheckConstant.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiHostDeployInfoVO;
import io.choerodon.devops.app.service.DevopsCiHostDeployInfoService;
import io.choerodon.devops.app.service.DevopsCiTplHostDeployInfoService;
import io.choerodon.devops.app.service.DevopsHostAppService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCiHostDeployInfoDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiTplHostDeployInfoCfgDTO;
import io.choerodon.devops.infra.dto.DevopsHostAppDTO;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsCiHostDeployInfoMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class HostDeployJobHandlerImpl extends AbstractJobHandler {
    @Autowired
    @Lazy
    private DevopsHostAppService devopsHostAppService;
    @Autowired
    private DevopsCiHostDeployInfoMapper devopsCiHostDeployInfoMapper;
    @Autowired
    private DevopsCiHostDeployInfoService devopsCiHostDeployInfoService;
    @Autowired
    private DevopsCiTplHostDeployInfoService devopsCiTplHostDeployInfoService;


    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.HOST_DEPLOY;
    }

    @Override
    protected void checkConfigInfo(Long projectId, DevopsCiJobVO devopsCiJobVO) {
        DevopsCiHostDeployInfoVO devopsCiHostDeployInfoVO = devopsCiJobVO.getDevopsCiHostDeployInfoVO();
        if (DeployTypeEnum.CREATE.value().equals(devopsCiHostDeployInfoVO.getDeployType())) {
            // 校验应用编码和应用名称
            devopsHostAppService.checkNameAndCodeUniqueAndThrow(projectId, null, devopsCiHostDeployInfoVO.getAppName(), devopsCiHostDeployInfoVO.getAppCode());
            devopsCiHostDeployInfoVO.setAppId(null);
        } else {
            if (devopsCiHostDeployInfoVO.getAppId() != null) {
                DevopsHostAppDTO devopsHostAppDTO = devopsHostAppService.baseQuery(devopsCiHostDeployInfoVO.getAppId());
                devopsCiHostDeployInfoVO.setHostId(devopsHostAppDTO.getHostId());
            }
        }
    }

    @Override
    public List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        Assert.notNull(devopsCiJobDTO, "Job can't be null");
        Assert.notNull(organizationId, DEVOPS_ORGANIZATION_ID_IS_NULL);
        Assert.notNull(projectId, DEVOPS_PROJECT_ID_IS_NULL);
        final Long jobId = devopsCiJobDTO.getId();
        Assert.notNull(jobId, DEVOPS_JOB_ID_IS_NULL);
        Assert.notNull(devopsCiJobDTO.getConfigId(), DEVOPS_JOB_CONFIG_ID_IS_NULL);
        List<String> result = new ArrayList<>();
        result.add(String.format("host_deploy %s %s", devopsCiJobDTO.getConfigId(), CiCommandTypeEnum.HOST_DEPLOY.value()));
        return result;
    }

    @Override
    protected Long saveConfig(Long ciPipelineId, DevopsCiJobVO devopsCiJobVO) {
        // 使用能够解密主键加密的json工具解密
        DevopsCiHostDeployInfoVO ciHostDeployInfoVO = devopsCiJobVO.getDevopsCiHostDeployInfoVO();
        // 创建主机应用，必须输入主机id
        if (DeployTypeEnum.CREATE.value().equals(ciHostDeployInfoVO.getDeployType()) && ciHostDeployInfoVO.getHostId() == null) {
            throw new CommonException(DEVOPS_HOST_ID_IS_NULL);
        }
        // 使用不进行主键加密的json工具再将json写入类, 用于在数据库存非加密数据
        DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO = ConvertUtils.convertObject(ciHostDeployInfoVO, DevopsCiHostDeployInfoDTO.class);
        if (ciHostDeployInfoVO.getJarDeploy() != null && !StringUtils.equals(ciHostDeployInfoVO.getHostDeployType(), RdupmTypeEnum.DOCKER.value())) {
            devopsCiHostDeployInfoDTO.setDeployJson(JsonHelper.marshalByJackson(ciHostDeployInfoVO.getJarDeploy()));
        }
        if (ciHostDeployInfoVO.getImageDeploy() != null && StringUtils.equals(ciHostDeployInfoVO.getHostDeployType(), RdupmTypeEnum.DOCKER.value())) {
            devopsCiHostDeployInfoDTO.setDeployJson(JsonHelper.marshalByJackson(ciHostDeployInfoVO.getImageDeploy()));
            devopsCiHostDeployInfoDTO.setDockerCommand(ciHostDeployInfoVO.getDockerCommand());
            devopsCiHostDeployInfoDTO.setKillCommand(null);
            devopsCiHostDeployInfoDTO.setPreCommand(null);
            devopsCiHostDeployInfoDTO.setRunCommand(null);
            devopsCiHostDeployInfoDTO.setPostCommand(null);
        }

        devopsCiHostDeployInfoDTO.setId(null);
        devopsCiHostDeployInfoDTO.setCiPipelineId(ciPipelineId);
        MapperUtil.resultJudgedInsert(devopsCiHostDeployInfoMapper, devopsCiHostDeployInfoDTO, DEVOPS_HOST_DEPLOY_INFO_CREATE);
        return devopsCiHostDeployInfoDTO.getId();
    }

    @Override
    public void deleteConfigByPipelineId(Long ciPipelineId) {
        Assert.notNull(ciPipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        DevopsCiHostDeployInfoDTO devopsCiHostDeployInfoDTO = new DevopsCiHostDeployInfoDTO();
        devopsCiHostDeployInfoDTO.setCiPipelineId(ciPipelineId);
        devopsCiHostDeployInfoMapper.delete(devopsCiHostDeployInfoDTO);
    }

    @Override
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        devopsCiJobVO.setDevopsCiHostDeployInfoVO(ConvertUtils.convertObject(devopsCiHostDeployInfoService.selectByPrimaryKey(devopsCiJobVO.getConfigId()), DevopsCiHostDeployInfoVO.class));
    }


    @Override
    public void fillJobTemplateConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        DevopsCiTplHostDeployInfoCfgDTO devopsCiTplHostDeployInfoCfgDTO = devopsCiTplHostDeployInfoService.selectByPrimaryKey(devopsCiJobVO.getConfigId());
        if (devopsCiTplHostDeployInfoCfgDTO == null) {
            devopsCiTplHostDeployInfoCfgDTO = new DevopsCiTplHostDeployInfoCfgDTO();
        }
        devopsCiJobVO.setDevopsCiHostDeployInfoVO(ConvertUtils.convertObject(devopsCiTplHostDeployInfoCfgDTO, DevopsCiHostDeployInfoVO.class));
    }
}
