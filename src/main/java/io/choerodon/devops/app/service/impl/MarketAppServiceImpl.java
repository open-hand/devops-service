package io.choerodon.devops.app.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.api.vo.ApplicationImportInternalVO;
import io.choerodon.devops.api.vo.MarketApplicationImportVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.AppServiceImportPayload;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.MarketAppService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.util.AppServiceUtils;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by wangxiang on 2021/3/2
 */
@Service
public class MarketAppServiceImpl implements MarketAppService {
    private static final String NORMAL = "normal";

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    private AppServiceService appServiceService;

    @Autowired
    private UserAttrService userAttrService;

    @Autowired
    private AppServiceUtils appServiceUtils;

    @Autowired
    private DevopsProjectService devopsProjectService;

    @Autowired
    private TransactionalProducer producer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Saga(code = SagaTopicCodeConstants.DEVOPS_IMPORT_MARKET_APPLICATION_SERVICE,
            description = "Devops创建应用服务", inputSchema = "{}")
    public void importAppService(Long projectId, List<ApplicationImportInternalVO> applicationImportInternalVOS) {
        applicationImportInternalVOS.forEach(marketApplicationImportVO -> {
            // 校验code是否存在
            appServiceUtils.checkCodeExist(marketApplicationImportVO.getAppCode());
        });
        List<AppServiceImportPayload> appServiceImportPayloads = appServiceService.createAppService(projectId, applicationImportInternalVOS);
        //发送saga
        if (CollectionUtils.isEmpty(applicationImportInternalVOS)) {
            return;
        }
        //2. saga执行git仓库的数据
        appServiceImportPayloads.forEach(payload -> producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_IMPORT_MARKET_APPLICATION_SERVICE)
                        .withPayloadAndSerialize(payload)
                        .withRefId(String.valueOf(payload.getAppServiceId()))
                        .withSourceId(projectId),
                builder -> {
                }));
    }
}
