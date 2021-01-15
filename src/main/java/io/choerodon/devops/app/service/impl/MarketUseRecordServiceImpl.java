package io.choerodon.devops.app.service.impl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.market.MarketAppUseRecordDTO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.service.MarketUseRecordService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.AppServiceVersionMapper;

/**
 * Created by wangxiang on 2020/12/16
 */
@Service
public class MarketUseRecordServiceImpl implements MarketUseRecordService {


    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;

    @Autowired
    private AppServiceMapper appServiceMapper;

    @Autowired
    private AppServiceVersionMapper appServiceVersionMapper;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;


    @Override
    @Async
    public void saveMarketUseRecord(String purpose, Long projectId, DeploySourceVO deploySourceVO, Long userId) {
        MarketAppUseRecordDTO marketAppUseRecordDTO = new MarketAppUseRecordDTO();
        marketAppUseRecordDTO.setPurpose(purpose);
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(Objects.requireNonNull(projectId));
        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceClientOperator.queryDeployObject(Objects.requireNonNull(projectId), Objects.requireNonNull(deploySourceVO.getDeployObjectId()));

        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(marketServiceDeployObjectVO.getDevopsAppServiceId());
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionMapper.selectByPrimaryKey(marketServiceDeployObjectVO.getDevopsAppServiceVersionId());
        if (!Objects.isNull(appServiceDTO)) {
            marketAppUseRecordDTO.setAppServiceAndVersion(appServiceDTO.getName() + "(" + appServiceVersionDTO.getVersion() + ")");
            ProjectDTO sourceProject = baseServiceClientOperator.queryIamProjectById(Objects.requireNonNull(appServiceDTO.getProjectId()));
            marketAppUseRecordDTO.setAppServiceSource(sourceProject.getName());
        } else {
            ProjectDTO sourceProject = baseServiceClientOperator.queryIamProjectById(Objects.requireNonNull(projectId));
            marketAppUseRecordDTO.setAppServiceSource(sourceProject.getName());
        }
        marketAppUseRecordDTO.setUserOrg(projectDTO.getName());
        marketAppUseRecordDTO.setDeployObjectId(deploySourceVO.getDeployObjectId());
        //部署记录存一个部署人员id，用于通知订阅人员
        marketAppUseRecordDTO.setDeployUserId(userId);
        marketServiceClientOperator.createUseRecord(marketAppUseRecordDTO);
    }
}
