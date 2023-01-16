package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.DevopsAnsibleCommandConstants.*;
import static io.choerodon.devops.infra.enums.DevopsMiddlewareTypeEnum.MySQL;
import static io.choerodon.devops.infra.enums.DevopsMiddlewareTypeEnum.Redis;
import static io.choerodon.devops.infra.enums.deploy.MiddlewareDeployModeEnum.*;
import static io.choerodon.devops.infra.enums.host.HostCommandEnum.DEPLOY_MIDDLEWARE;
import static io.choerodon.devops.infra.enums.host.HostInstanceType.MIDDLEWARE_MYSQL;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.IOUtils;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.validator.AppServiceInstanceValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.host.MiddlewareHostCommandVO;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.enums.deploy.OperationTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
import io.choerodon.devops.infra.enums.host.HostInstanceType;
import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;
import io.choerodon.devops.infra.mapper.DevopsMiddlewareMapper;
import io.choerodon.devops.infra.util.*;

@Service
public class DevopsMiddlewareServiceImpl implements DevopsMiddlewareService {

    private static final String REDIS_STANDALONE_PERSISTENCE_TEMPLATE = "persistence:\n" +
            "  existingClaim: %s";
    private static final String REDIS_SENTINEL_MATCHLABELS_TEMPLATE = "matchLabels:\n%s";

    private static final String REDIS_CONFIGMAP_TEMPLATE = "configmap: |-\n%s";

    private static final String REDIS_MATCHLABELS_TEMPLATE = "    %s: %s\n";

    private static final String REDIS_CONFIGMAP_VALUE_TEMPLATE = "  %s %s\n";

    private static final String REDIS_INSTALL_LOG_PATH = "/tmp/redis-install.log";

    private static final String MYSQL_STANDALONE_PERSISTENCE_TEMPLATE = "    existingClaim: %s";

    private static final String MYSQL_CONFIGMAP_VALUE_TEMPLATE = "    %s=%s\n";

    private static final String MYSQL_INSTALL_LOG_PATH = "/tmp/mysql-install.log";

    private static final String SHELL_HEADER = "set -e\n" +
            "if [ -d \"/tmp/middleware\" ]; then\n" +
            "    rm -rf /tmp/middleware\n" +
            "fi\n" +
            "git clone https://gitee.com/open-hand/middleware.git /tmp/middleware\n";

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsMiddlewareServiceImpl.class);

    private static final String REDIS_SENTINEL_VALUE_TEMPLATE;

    private static final String REDIS_STANDALONE_VALUE_TEMPLATE;

    private static final String MYSQL_STANDALONE_VALUE_TEMPLATE;

    public static final Map<String, String> MODE_MAP = new HashMap<>();

    static {
        MODE_MAP.put(STANDALONE.getValue(), "单机模式");
        MODE_MAP.put(SENTINEL.getValue(), "哨兵模式");
        MODE_MAP.put(MASTER_SLAVE.getValue(), "主备模式");

        try (InputStream redisSentinelInputStream = DevopsMiddlewareServiceImpl.class.getResourceAsStream("/template/middleware/redis/redis-sentinel-value-template.yaml")) {
            REDIS_SENTINEL_VALUE_TEMPLATE = IOUtils.toString(redisSentinelInputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CommonException("devops.middleware.value.template");
        }
        try (InputStream redisStandaloneInputStream = DevopsMiddlewareServiceImpl.class.getResourceAsStream("/template/middleware/redis/redis-standalone-value-template.yaml")) {
            REDIS_STANDALONE_VALUE_TEMPLATE = IOUtils.toString(redisStandaloneInputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CommonException("devops.middleware.value.template");
        }
        try (InputStream mysqlStandaloneInputStream = DevopsMiddlewareServiceImpl.class.getResourceAsStream("/template/middleware/mysql/mysql-standalone-value-template.yaml")) {
            MYSQL_STANDALONE_VALUE_TEMPLATE = IOUtils.toString(mysqlStandaloneInputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CommonException("devops.middleware.value.template");
        }
    }

    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private DevopsHostMapper devopsHostMapper;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsMiddlewareMapper devopsMiddlewareMapper;
    @Autowired
    private EncryptService encryptService;
    @Autowired
    private DevopsPvcService devopsPvcService;
    @Autowired
    private KeySocketSendHelper webSocketHelper;
    @Autowired
    private DevopsHostCommandService devopsHostCommandService;
    @Autowired
    private DevopsHostAppInstanceService devopsHostAppInstanceService;
    @Autowired
    private DevopsHostAppService devopsHostAppService;

    /**
     * 中间件的环境部署逻辑和市场应用的部署逻辑完全一样，只是需要提前构造values
     *
     * @param projectId
     * @param middlewareRedisEnvDeployVO
     * @return
     */
    @Override
    public AppServiceInstanceVO envDeployForRedis(Long projectId, MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO) {

        // 根据部署模式以及版本查询部署部署对象id和市场服务id
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(Redis.getType(), middlewareRedisEnvDeployVO.getMode(), middlewareRedisEnvDeployVO.getVersion());

        middlewareRedisEnvDeployVO.setMarketDeployObjectId(middlewareServiceReleaseInfo.getId());
        middlewareRedisEnvDeployVO.setMarketAppServiceId(middlewareServiceReleaseInfo.getMarketServiceId());

        if (STANDALONE.getValue().equals(middlewareRedisEnvDeployVO.getMode())) {
            middlewareRedisEnvDeployVO.setValues(generateRedisStandaloneValues(middlewareRedisEnvDeployVO));
        }

        if (SENTINEL.getValue().equals(middlewareRedisEnvDeployVO.getMode())) {
            middlewareRedisEnvDeployVO.setValues(generateRedisSentinelValues(middlewareRedisEnvDeployVO));
        }

        MarketInstanceCreationRequestVO marketInstanceCreationRequestVO = ConvertUtils.convertObject(middlewareRedisEnvDeployVO, MarketInstanceCreationRequestVO.class);
        marketInstanceCreationRequestVO.setApplicationType(AppSourceType.MIDDLEWARE.getValue());
        marketInstanceCreationRequestVO.setOperationType(OperationTypeEnum.BASE_COMPONENT.value());
        marketInstanceCreationRequestVO.setInstanceName(marketInstanceCreationRequestVO.getAppCode());

        return appServiceInstanceService.createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO, true);
    }

    @Override
    public AppServiceInstanceVO envDeployForMySql(Long projectId, MiddlewareMySqlEnvDeployVO middlewareMySqlEnvDeployVO) {
        // 根据部署模式以及版本查询部署部署对象id和市场服务id
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(MySQL.getType(), STANDALONE.getValue(), middlewareMySqlEnvDeployVO.getVersion());

        middlewareMySqlEnvDeployVO.setMarketDeployObjectId(middlewareServiceReleaseInfo.getId());
        middlewareMySqlEnvDeployVO.setMarketAppServiceId(middlewareServiceReleaseInfo.getMarketServiceId());

        middlewareMySqlEnvDeployVO.setValues(generateMysqlStandaloneValues(middlewareMySqlEnvDeployVO));

        MarketInstanceCreationRequestVO marketInstanceCreationRequestVO = ConvertUtils.convertObject(middlewareMySqlEnvDeployVO, MarketInstanceCreationRequestVO.class);
        marketInstanceCreationRequestVO.setApplicationType(AppSourceType.MIDDLEWARE.getValue());
        marketInstanceCreationRequestVO.setOperationType(OperationTypeEnum.BASE_COMPONENT.value());
        marketInstanceCreationRequestVO.setInstanceName(marketInstanceCreationRequestVO.getAppCode());

        return appServiceInstanceService.createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO, true);
    }

    @Override
    public AppServiceInstanceVO updateMiddlewareInstance(Long projectId, MarketInstanceCreationRequestVO marketInstanceCreationRequestVO) {
        return appServiceInstanceService.createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO, true);
    }

    @Override
    public MiddlewareRedisEnvDeployVO queryRedisConfig(Long projectId, Long appServiceInstanceId, Long marketDeployObjectId) {
        MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO = new MiddlewareRedisEnvDeployVO();
        InstanceValueVO instanceValueVO = appServiceInstanceService.queryUpgradeValueForMarketInstance(projectId, appServiceInstanceId, marketDeployObjectId);
        Yaml yaml = new Yaml();
        Map<String, Object> values = yaml.loadAs(instanceValueVO.getYaml(), Map.class);

        middlewareRedisEnvDeployVO.setPassword((String) values.get("password"));

        middlewareRedisEnvDeployVO.setSysctlImage((Boolean) ((Map) values.get("sysctlImage")).get("enabled"));

        Map sentinelConfig = (Map) values.get("sentinel");
        if (sentinelConfig != null) {
            middlewareRedisEnvDeployVO.setMode(SENTINEL.getValue());
            middlewareRedisEnvDeployVO.setSlaveCount((Integer) ((Map) values.get("cluster")).get("slaveCount"));
            middlewareRedisEnvDeployVO.setPvLabels((Map<String, String>) ((Map) values.get("slave")).get("matchLabels"));
            middlewareRedisEnvDeployVO.setConfiguration(getConfigMap((String) values.get("configmap")));
        } else {
            middlewareRedisEnvDeployVO.setMode(STANDALONE.getValue());
            if (values.get("persistence") != null) {
                middlewareRedisEnvDeployVO.setPvcName((String) ((Map) values.get("persistence")).get("existingClaim"));
            }
            middlewareRedisEnvDeployVO.setConfiguration(getConfigMap((String) values.get("configmap")));
        }

        return middlewareRedisEnvDeployVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hostDeployForRedis(Long projectId, MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO) {
        checkMiddlewareNameAndCode(projectId, middlewareRedisHostDeployVO.getAppName(), middlewareRedisHostDeployVO.getAppCode(), Redis.getType());

        List<DevopsHostDTO> devopsHostDTOList = devopsHostMapper.listByProjectIdAndIds(projectId, middlewareRedisHostDeployVO.getHostIds());

        if (SENTINEL.getValue().equals(middlewareRedisHostDeployVO.getMode())) {
            CommonExAssertUtil.assertTrue(middlewareRedisHostDeployVO.getHostIds().size() >= 3, "devops.host.size.less.than.3");
            CommonExAssertUtil.assertTrue(devopsHostDTOList.size() >= 3, "devops.host.size.less.than.3");
        }

        // 根据部署模式以及版本查询部署部署对象id和市场服务id
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(Redis.getType(), middlewareRedisHostDeployVO.getMode(), middlewareRedisHostDeployVO.getVersion());

        DevopsHostDTO devopsHostDTOForConnection = devopsHostDTOList.get(0);

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setDeployObjectId(middlewareServiceReleaseInfo.getId());
        deploySourceVO.setType(AppSourceType.PLATFORM_PRESET.getValue());
        deploySourceVO.setProjectName(projectDTO.getName());

        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.BASE_COMPONENT,
                null,
                DeployModeEnum.HOST,
                devopsHostDTOForConnection.getId(),
                devopsHostDTOForConnection.getName(),
                CommandStatus.SUCCESS.getStatus(),
                DeployObjectTypeEnum.MIDDLEWARE,
                middlewareRedisHostDeployVO.getAppCode(),
                middlewareRedisHostDeployVO.getVersion() + "-" + middlewareRedisHostDeployVO.getMode(),
                null,
                null,
                null,
                deploySourceVO);

        String deployShell;
        try {
            MiddlewareInventoryVO middlewareInventoryVO = calculateGeneralInventoryValue(devopsHostDTOList, Redis);
            // 安装ansible、git初始化文件
            deployShell = preProcessShell()
                    + generatePrivateKey(devopsHostDTOList)
                    + generateHostConfiguration(middlewareInventoryVO, Redis)
                    + generateRedisConfiguration(middlewareRedisHostDeployVO.getPassword(), middlewareRedisHostDeployVO.getConfiguration())
                    + generateInstallRedis();

            Map<String, String> middlewareConfig = new HashMap<>();
            middlewareConfig.put("version", middlewareRedisHostDeployVO.getVersion());
            middlewareConfig.put("mode", middlewareRedisHostDeployVO.getMode());

            DevopsHostAppDTO devopsHostAppDTO = new DevopsHostAppDTO(
                    projectId,
                    devopsHostDTOForConnection.getId(),
                    middlewareRedisHostDeployVO.getAppName(),
                    middlewareRedisHostDeployVO.getAppCode(),
                    RdupmTypeEnum.MIDDLEWARE.value(),
                    OperationTypeEnum.BASE_COMPONENT.value()
            );

            devopsHostAppService.baseCreate(devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_MIDDLEWARE_INSTANCE_FAILED);

            DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
                    devopsHostDTOForConnection.getId(),
                    devopsHostAppDTO.getId(),
                    middlewareRedisHostDeployVO.getAppCode(),
                    AppSourceType.MIDDLEWARE.getValue(),
                    JsonHelper.marshalByJackson(middlewareConfig),
                    null,
                    null,
                    null,
                    null,
                    null);
            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);

            // 保存中间件信息
            Map<String, Object> redisConfigurationToSave = new HashMap<>();
            redisConfigurationToSave.put("configuration", middlewareRedisHostDeployVO.getConfiguration());
            redisConfigurationToSave.put("password", middlewareRedisHostDeployVO.getPassword());
            DevopsMiddlewareDTO devopsMiddlewareDTO = saveMiddlewareInfo(projectId,
                    devopsHostAppInstanceDTO.getId(),
                    middlewareRedisHostDeployVO.getAppCode(),
                    Redis.getType(),
                    middlewareRedisHostDeployVO.getMode(),
                    middlewareRedisHostDeployVO.getVersion(),
                    devopsHostDTOList.stream().map(h -> String.valueOf(h.getId())).collect(Collectors.joining(",")),
                    JsonHelper.marshalByJackson(redisConfigurationToSave));

            DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
            devopsHostCommandDTO.setHostId(devopsHostDTOForConnection.getId());
            devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
            devopsHostCommandDTO.setCommandType(HostCommandEnum.DEPLOY_MIDDLEWARE.value());
            devopsHostCommandDTO.setInstanceType(HostInstanceType.MIDDLEWARE_REDIS.value());
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
            devopsHostCommandService.baseCreate(devopsHostCommandDTO);

            MiddlewareHostCommandVO middlewareHostCommandVO = new MiddlewareHostCommandVO();
            middlewareHostCommandVO.setMiddlewareType(Redis.getType());
            middlewareHostCommandVO.setMode(middlewareRedisHostDeployVO.getMode());
            middlewareHostCommandVO.setShell(deployShell);
            middlewareHostCommandVO.setInstanceId(String.valueOf(devopsHostAppInstanceDTO.getId()));
            middlewareHostCommandVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
            middlewareHostCommandVO.setName(middlewareRedisHostDeployVO.getAppCode());

            HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
            hostAgentMsgVO.setHostId(String.valueOf(devopsHostDTOForConnection.getId()));
            hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_MIDDLEWARE.value());
            hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
            hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(middlewareHostCommandVO));

            webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + devopsHostDTOForConnection.getId(),
                    String.format(DevopsHostConstants.MIDDLEWARE_INSTANCE, devopsHostDTOForConnection.getId(), devopsMiddlewareDTO.getId()),
                    JsonHelper.marshalByJackson(hostAgentMsgVO));
            LOGGER.info("deploy Middleware Redis,mode:{} version:{} projectId:{}", middlewareRedisHostDeployVO.getMode(), middlewareRedisHostDeployVO.getVersion(), projectId);
        } catch (Exception e) {
            devopsDeployRecordService.saveFailRecord(
                    projectId,
                    DeployType.BASE_COMPONENT,
                    null,
                    DeployModeEnum.HOST,
                    devopsHostDTOForConnection.getId(),
                    devopsHostDTOForConnection.getName(),
                    CommandStatus.FAILED.getStatus(),
                    DeployObjectTypeEnum.MIDDLEWARE,
                    middlewareRedisHostDeployVO.getAppCode(),
                    middlewareRedisHostDeployVO.getVersion() + "-" + middlewareRedisHostDeployVO.getMode(),
                    null,
                    deploySourceVO,
                    DetailsHelper.getUserDetails().getUserId(),
                    e.getMessage());
            throw new CommonException(e.getMessage());
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hostDeployForMySql(Long projectId, MiddlewareMySqlHostDeployVO middlewareMySqlHostDeployVO) {
        // master-slave模式，节点数量必须为2，且需要设置virtualIp
        if (MASTER_SLAVE.getValue().equals(middlewareMySqlHostDeployVO.getMode()) && middlewareMySqlHostDeployVO.getHostIds().size() != 2) {
            if (middlewareMySqlHostDeployVO.getHostIds().size() != 2) {
                throw new CommonException("devops.mysql.master-slave.host.count");
            }
            if (!GitOpsConstants.IP_REG_PATTERN.matcher(middlewareMySqlHostDeployVO.getVirtualIp()).matches()) {
                throw new CommonException("devops.virtual.ip.invalid");
            }
        }

        checkMiddlewareNameAndCode(projectId, middlewareMySqlHostDeployVO.getAppName(), middlewareMySqlHostDeployVO.getAppCode(), MySQL.getType());

        List<DevopsHostDTO> devopsHostDTOList = devopsHostMapper.listByProjectIdAndIds(projectId, middlewareMySqlHostDeployVO.getHostIds());

        convertMySQLConfiguration(middlewareMySqlHostDeployVO, devopsHostDTOList.stream().collect(Collectors.toMap(DevopsHostDTO::getId, Function.identity())));

        // 根据部署模式以及版本查询部署部署对象id和市场服务id
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(MySQL.getType(), middlewareMySqlHostDeployVO.getMode(), middlewareMySqlHostDeployVO.getVersion());

        DevopsHostDTO devopsHostDTOForConnection = devopsHostDTOList.get(0);

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setDeployObjectId(middlewareServiceReleaseInfo.getId());
        deploySourceVO.setType(AppSourceType.PLATFORM_PRESET.getValue());
        deploySourceVO.setProjectName(projectDTO.getName());

        devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.BASE_COMPONENT,
                null,
                DeployModeEnum.HOST,
                devopsHostDTOForConnection.getId(),
                devopsHostDTOForConnection.getName(),
                CommandStatus.SUCCESS.getStatus(),
                DeployObjectTypeEnum.MIDDLEWARE,
                middlewareMySqlHostDeployVO.getAppCode(),
                middlewareMySqlHostDeployVO.getVersion() + "-" + middlewareMySqlHostDeployVO.getMode(),
                null,
                null,
                null,
                deploySourceVO);

        String deployShell;
        try {
            MiddlewareInventoryVO middlewareInventoryVO = calculateGeneralInventoryValue(devopsHostDTOList, MySQL);
            // 安装ansible、git初始化文件
            deployShell = preProcessShell()
                    + generatePrivateKey(devopsHostDTOList)
                    + generateHostConfiguration(middlewareInventoryVO, MySQL)
                    + generateMySqlConfiguration(middlewareMySqlHostDeployVO.getPassword(), middlewareMySqlHostDeployVO.getVirtualIp(), middlewareMySqlHostDeployVO.getConfiguration())
                    + generateInstallMySQL();

            Map<String, String> middlewareConfig = new HashMap<>();
            middlewareConfig.put("version", middlewareMySqlHostDeployVO.getVersion());
            middlewareConfig.put("mode", middlewareMySqlHostDeployVO.getMode());

            DevopsHostAppDTO devopsHostAppDTO = new DevopsHostAppDTO(
                    projectId,
                    devopsHostDTOForConnection.getId(),
                    middlewareMySqlHostDeployVO.getAppName(),
                    middlewareMySqlHostDeployVO.getAppCode(),
                    RdupmTypeEnum.MIDDLEWARE.value(),
                    OperationTypeEnum.BASE_COMPONENT.value()
            );

            devopsHostAppService.baseCreate(devopsHostAppDTO, DevopsHostConstants.ERROR_SAVE_MIDDLEWARE_INSTANCE_FAILED);

            DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO(projectId,
                    devopsHostDTOForConnection.getId(),
                    devopsHostAppDTO.getId(),
                    middlewareMySqlHostDeployVO.getAppCode(),
                    AppSourceType.MIDDLEWARE.getValue(),
                    JsonHelper.marshalByJackson(middlewareConfig),
                    null,
                    null,
                    null,
                    null,
                    null);
            devopsHostAppInstanceService.baseCreate(devopsHostAppInstanceDTO);

            // 保存中间件信息
            Map<String, Object> mysqlConfigurationToSave = new HashMap<>();
            mysqlConfigurationToSave.put("password", middlewareMySqlHostDeployVO.getPassword());
            mysqlConfigurationToSave.put("virtualIp", middlewareMySqlHostDeployVO.getVirtualIp());
            mysqlConfigurationToSave.put("configuration", middlewareMySqlHostDeployVO.getConfiguration());
            DevopsMiddlewareDTO devopsMiddlewareDTO = saveMiddlewareInfo(projectId,
                    devopsHostAppInstanceDTO.getId(),
                    middlewareMySqlHostDeployVO.getAppCode(),
                    MySQL.getType(),
                    middlewareMySqlHostDeployVO.getMode(),
                    middlewareMySqlHostDeployVO.getVersion(),
                    devopsHostDTOList.stream().map(h -> String.valueOf(h.getId())).collect(Collectors.joining(",")),
                    JsonHelper.marshalByJackson(mysqlConfigurationToSave));


            DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
            devopsHostCommandDTO.setHostId(devopsHostDTOForConnection.getId());
            devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
            devopsHostCommandDTO.setCommandType(DEPLOY_MIDDLEWARE.value());
            devopsHostCommandDTO.setInstanceType(MIDDLEWARE_MYSQL.value());
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
            devopsHostCommandService.baseCreate(devopsHostCommandDTO);

            MiddlewareHostCommandVO middlewareHostCommandVO = new MiddlewareHostCommandVO();
            middlewareHostCommandVO.setMiddlewareType(MySQL.getType());
            middlewareHostCommandVO.setMode(middlewareMySqlHostDeployVO.getMode());
            middlewareHostCommandVO.setShell(deployShell);
            middlewareHostCommandVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
            middlewareHostCommandVO.setInstanceId(String.valueOf(devopsHostAppInstanceDTO.getId()));
            middlewareHostCommandVO.setName(middlewareMySqlHostDeployVO.getAppCode());

            HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
            hostAgentMsgVO.setHostId(String.valueOf(devopsHostDTOForConnection.getId()));
            hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_MIDDLEWARE.value());
            hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
            hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(middlewareHostCommandVO));

            webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + devopsHostDTOForConnection.getId(),
                    String.format(DevopsHostConstants.MIDDLEWARE_INSTANCE, devopsHostDTOForConnection.getId(), devopsMiddlewareDTO.getId()),
                    JsonHelper.marshalByJackson(hostAgentMsgVO));
            LOGGER.info("deploy Middleware MySQL,mode:{} version:{} projectId:{}", middlewareMySqlHostDeployVO.getMode(), middlewareMySqlHostDeployVO.getVersion(), projectId);
        } catch (Exception e) {
            devopsDeployRecordService.saveFailRecord(
                    projectId,
                    DeployType.BASE_COMPONENT,
                    null,
                    DeployModeEnum.HOST,
                    devopsHostDTOForConnection.getId(),
                    devopsHostDTOForConnection.getName(),
                    CommandStatus.FAILED.getStatus(),
                    DeployObjectTypeEnum.MIDDLEWARE,
                    middlewareMySqlHostDeployVO.getAppCode(),
                    middlewareMySqlHostDeployVO.getVersion() + "-" + middlewareMySqlHostDeployVO.getMode(),
                    null,
                    deploySourceVO,
                    DetailsHelper.getUserDetails().getUserId(),
                    e.getMessage());
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public void uninstallMiddleware(Long projectId, DevopsHostAppInstanceDTO devopsHostAppInstanceDTO) {
        DevopsMiddlewareDTO middlewareDTOToSearch = new DevopsMiddlewareDTO();
        middlewareDTOToSearch.setInstanceId(devopsHostAppInstanceDTO.getId());
        DevopsMiddlewareDTO devopsMiddlewareDTO = devopsMiddlewareMapper.selectOne(middlewareDTOToSearch);
        DevopsHostDTO devopsHostDTOForConnection = null;
        Long hostIdForConnection = devopsHostAppInstanceDTO.getHostId();
        Set<Long> hostIds = Arrays.stream(devopsMiddlewareDTO.getHostIds().split(",")).map(Long::parseLong).collect(Collectors.toSet());
        List<DevopsHostDTO> devopsHostDTOList = devopsHostMapper.listByProjectIdAndIds(projectId, hostIds);
        for (DevopsHostDTO host : devopsHostDTOList) {
            if (host.getId().equals(hostIdForConnection)) {
                devopsHostDTOForConnection = host;
                break;
            }
        }
        if (devopsHostDTOForConnection == null) {
            return;
        }

        DevopsMiddlewareTypeEnum middlewareTypeEnum = DevopsMiddlewareTypeEnum.valueOf(devopsMiddlewareDTO.getType());

        MiddlewareInventoryVO middlewareInventoryVO = calculateGeneralInventoryValue(devopsHostDTOList, middlewareTypeEnum);

        Map<String, Object> stringObjectMap = JsonHelper.unmarshalByJackson(devopsMiddlewareDTO.getConfiguration(), new TypeReference<Map<String, Object>>() {
        });

        Supplier<String> generateInstallMiddlewareShell = devopsMiddlewareDTO.getType().equals(Redis.getType()) ?
                () -> generateRedisConfiguration((String) stringObjectMap.get("password"), (Map<String, String>) stringObjectMap.get("configuration")) :
                () -> generateMySqlConfiguration((String) stringObjectMap.get("password"), (String) stringObjectMap.get("virtualIp"), (Map<String, Map<String, String>>) stringObjectMap.get("configuration"));

        String uninstallShell;
        uninstallShell = generateShellHeader()
                + generateInstallMiddlewareShell.get()
                + generateHostConfiguration(middlewareInventoryVO, middlewareTypeEnum)
                + generateUninstallCommand(middlewareTypeEnum);

        DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
        devopsHostCommandDTO.setCommandType(HostCommandEnum.KILL_INSTANCE.value());
        devopsHostCommandDTO.setHostId(devopsHostAppInstanceDTO.getHostId());
        devopsHostCommandDTO.setInstanceType(HostResourceType.INSTANCE_PROCESS.value());
        devopsHostCommandDTO.setInstanceId(devopsHostAppInstanceDTO.getId());
        devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
        devopsHostCommandService.baseCreate(devopsHostCommandDTO);

        MiddlewareHostCommandVO middlewareHostCommandVO = new MiddlewareHostCommandVO();
        middlewareHostCommandVO.setInstanceId(String.valueOf(devopsHostAppInstanceDTO.getId()));
        middlewareHostCommandVO.setShell(uninstallShell);

        HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
        hostAgentMsgVO.setHostId(String.valueOf(hostIdForConnection));
        hostAgentMsgVO.setType(HostCommandEnum.KILL_MIDDLEWARE.value());
        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(middlewareHostCommandVO));
        hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));

        hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(middlewareHostCommandVO));

        webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + hostIdForConnection, DevopsHostConstants.GROUP + hostIdForConnection, JsonHelper.marshalByJackson(hostAgentMsgVO));
    }

    @Override
    public void deleteByInstanceId(Long instanceId) {
        devopsMiddlewareMapper.deleteByInstanceId(instanceId);
    }

    @Override
    public DevopsMiddlewareDTO queryByInstanceId(Long instanceId) {
        return devopsMiddlewareMapper.queryByInstanceId(instanceId);
    }

    @Override
    @Transactional
    public void updateHostInstance(Long projectId, MiddlewareHostInstanceVO middlewareHostInstanceVO) {
        DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = devopsHostAppInstanceService.baseQuery(middlewareHostInstanceVO.getInstanceId());
        CommonExAssertUtil.assertTrue(projectId.equals(devopsHostAppInstanceDTO.getProjectId()), "devops.operating.resource.in.other.project");
        DevopsHostAppDTO devopsHostAppDTO = devopsHostAppService.baseQuery(devopsHostAppInstanceDTO.getAppId());
//        devopsHostAppService.checkNameUniqueAndThrow(projectId, devopsHostAppInstanceDTO.getAppId(), middlewareHostInstanceVO.getAppName());
        devopsHostAppDTO.setName(middlewareHostInstanceVO.getAppName());
        devopsHostAppService.baseUpdate(devopsHostAppDTO);
    }

    @Override
    public DevopsMiddlewareDTO saveMiddlewareInfo(Long projectId, Long instanceId, String name, String type, String mode, String version, String hostIds, String configuration) {
        DevopsMiddlewareDTO devopsMiddlewareDTO = new DevopsMiddlewareDTO(
                projectId,
                instanceId,
                name,
                type,
                mode,
                version,
                hostIds,
                configuration
        );
        MapperUtil.resultJudgedInsertSelective(devopsMiddlewareMapper, devopsMiddlewareDTO, "devops.middleware.insert");
        return devopsMiddlewareDTO;
    }

    private MiddlewareInventoryVO calculateGeneralInventoryValue(List<DevopsHostDTO> devopsHostDTOList, DevopsMiddlewareTypeEnum middlewareTypeEnum) {
        MiddlewareInventoryVO middlewareInventoryVO = new MiddlewareInventoryVO(middlewareTypeEnum);
        for (DevopsHostDTO hostDTO : devopsHostDTOList) {
            // 如果内网ip不存在，使用公网ip
            String ip = hostDTO.getHostIp();
            // 如果内网端口不存在，使用公网端口
            Integer port = hostDTO.getSshPort();
            if (HostAuthType.ACCOUNTPASSWORD.value().equals(hostDTO.getAuthType())) {
                middlewareInventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL_PSW_TYPE, ip, ip, port, hostDTO.getUsername(), hostDTO.getPassword()))
                        .append(System.lineSeparator());
            } else {
                middlewareInventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL_PRIVATE_KEY_TYPE, ip, ip, port, hostDTO.getUsername(), String.format(PRIVATE_KEY_SAVE_PATH_TEMPLATE, hostDTO.getName())))
                        .append(System.lineSeparator());
            }
            // 设置chrony节点
            middlewareInventoryVO.getChrony().append(ip)
                    .append(System.lineSeparator());

            switch (middlewareTypeEnum) {
                case Redis:
                    middlewareInventoryVO.getRedis().append(ip)
                            .append(System.lineSeparator());
                    break;
                case MySQL:
                    middlewareInventoryVO.getMysql().append(ip)
                            .append(System.lineSeparator());
                    break;
                default:
                    throw new CommonException("devops.middleware.unsupported.type", middlewareTypeEnum.getType());
            }
        }
        return middlewareInventoryVO;
    }

    private String generateRedisStandaloneValues(MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO) {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("{{ password }}", middlewareRedisEnvDeployVO.getPassword());
        configuration.put("{{ usePassword }}", "true");

        if (!StringUtils.isEmpty(middlewareRedisEnvDeployVO.getPvcName())) {
            // 将对应pvc设为已使用状态
            devopsPvcService.setUsed(middlewareRedisEnvDeployVO.getEnvironmentId(), middlewareRedisEnvDeployVO.getPvcName());
            configuration.put("{{ persistence-enabled }}", "true");
            configuration.put("{{ persistence-info }}", String.format(REDIS_STANDALONE_PERSISTENCE_TEMPLATE, middlewareRedisEnvDeployVO.getPvcName()));
        } else {
            configuration.put("{{ persistence-enabled }}", "false");
            configuration.put("{{ persistence-info }}", "");
        }

        configuration.put("{{ sysctlImage-enabled }}", middlewareRedisEnvDeployVO.getSysctlImage().toString());
        configuration.put("{{ sysctlImage-mountHostSys }}", middlewareRedisEnvDeployVO.getSysctlImage().toString());

        if (!CollectionUtils.isEmpty(middlewareRedisEnvDeployVO.getConfiguration())) {
            StringBuilder configMapSb = new StringBuilder();
            middlewareRedisEnvDeployVO.getConfiguration().forEach((k, v) -> configMapSb.append(String.format(REDIS_CONFIGMAP_VALUE_TEMPLATE, k, v)));
            configuration.put("{{ configmap }}", String.format(REDIS_CONFIGMAP_TEMPLATE, configMapSb.toString()));
        }

        return FileUtil.replaceReturnString(REDIS_STANDALONE_VALUE_TEMPLATE, configuration);
    }

    private String generateRedisSentinelValues(MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO) {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("{{ password }}", middlewareRedisEnvDeployVO.getPassword());
        configuration.put("{{ usePassword }}", "true");

        configuration.put("{{ slaveCount }}", middlewareRedisEnvDeployVO.getSlaveCount().toString());

        if (!CollectionUtils.isEmpty(middlewareRedisEnvDeployVO.getPvLabels())) {
            configuration.put("{{ persistence-enabled }}", "true");
            StringBuilder stringBuilder = new StringBuilder();
            middlewareRedisEnvDeployVO.getPvLabels().forEach((k, v) -> stringBuilder.append(String.format(REDIS_MATCHLABELS_TEMPLATE, k, v)));
            configuration.put("{{ matchLabels }}", String.format(REDIS_SENTINEL_MATCHLABELS_TEMPLATE, stringBuilder.toString()));
        } else {
            configuration.put("{{ persistence-enabled }}", "false");
            configuration.put("{{ matchLabels }}", "");
        }

        configuration.put("{{ sysctlImage-enabled }}", middlewareRedisEnvDeployVO.getSysctlImage().toString());
        configuration.put("{{ sysctlImage-mountHostSys }}", middlewareRedisEnvDeployVO.getSysctlImage().toString());

        if (!CollectionUtils.isEmpty(middlewareRedisEnvDeployVO.getConfiguration())) {
            StringBuilder configMapSb = new StringBuilder();
            middlewareRedisEnvDeployVO.getConfiguration().forEach((k, v) -> configMapSb.append(String.format(REDIS_CONFIGMAP_VALUE_TEMPLATE, k, v)));
            configuration.put("{{ configmap }}", String.format(REDIS_CONFIGMAP_TEMPLATE, configMapSb.toString()));
        }

        return FileUtil.replaceReturnString(REDIS_SENTINEL_VALUE_TEMPLATE, configuration);
    }

    private String generateMysqlStandaloneValues(MiddlewareMySqlEnvDeployVO middlewareMySqlEnvDeployVO) {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("{{ password }}", middlewareMySqlEnvDeployVO.getPassword());

        if (!StringUtils.isEmpty(middlewareMySqlEnvDeployVO.getPvcName())) {
            // 将对应pvc设为已使用状态
            devopsPvcService.setUsed(middlewareMySqlEnvDeployVO.getEnvironmentId(), middlewareMySqlEnvDeployVO.getPvcName());
            configuration.put("{{ persistence-enabled }}", "true");
            configuration.put("{{ persistence-info }}", String.format(MYSQL_STANDALONE_PERSISTENCE_TEMPLATE, middlewareMySqlEnvDeployVO.getPvcName()));
        } else {
            configuration.put("{{ persistence-enabled }}", "false");
            configuration.put("{{ persistence-info }}", "");
        }

        if (!CollectionUtils.isEmpty(middlewareMySqlEnvDeployVO.getConfiguration())) {
            StringBuilder configMapSb = new StringBuilder();
            middlewareMySqlEnvDeployVO.getConfiguration().forEach((k, v) -> configMapSb.append(String.format(MYSQL_CONFIGMAP_VALUE_TEMPLATE, k, v)));
            configuration.put("{{ configmap }}", configMapSb.toString());
        }

        return FileUtil.replaceReturnString(MYSQL_STANDALONE_VALUE_TEMPLATE, configuration);
    }

    private static Map<String, String> getConfigMap(String config) {
        Map<String, String> configMap = new HashMap<>();
        String[] splitConfig = config.split("\n");
        Arrays.stream(splitConfig).filter(s -> {
            if (!s.trim().startsWith("#")) {
                return true;
            }
            return false;
        }).forEach(s -> {
            String[] keyValue = s.trim().split(" ");
            if (keyValue.length == 2) {
                configMap.put(keyValue[0], keyValue[1]);
            }
        });
        return configMap;
    }

    private void convertMySQLConfiguration(MiddlewareMySqlHostDeployVO middlewareMySqlHostDeployVO, Map<Long, DevopsHostDTO> devopsHostDTOMap) {
        Map<String, Map<String, String>> convertedConfiguration = new HashMap<>();
        middlewareMySqlHostDeployVO.getConfiguration().forEach((k, v) -> {
            String key = encryptService.decrypt(k);
            convertedConfiguration.put(devopsHostDTOMap.get(Long.parseLong(key)).getHostIp(), v);
        });
        middlewareMySqlHostDeployVO.setConfiguration(convertedConfiguration);
    }

    public void checkMiddlewareNameAndCode(Long projectId, String name, String code, String type) {
//        if (Boolean.FALSE.equals(devopsHostAppService.checkNameUnique(projectId, null, name))) {
//            throw new CommonException("devops.middleware.name.exists");
//        }

        AppServiceInstanceValidator.checkCode(code);
        CommonExAssertUtil.assertTrue(devopsMiddlewareMapper.checkCodeUnique(projectId, code, type) < 1, "devops.middleware.code.exists");
    }

    private String preProcessShell() {
        InputStream shellInputStream = DevopsClusterNodeServiceImpl.class.getResourceAsStream("/shell/pre-process.sh");
        Map<String, String> map = new HashMap<>();
        map.put("{{ git-clone }}", "if [ -d \"/tmp/middleware\" ]; then\n" +
                "    rm -rf /tmp/middleware\n" +
                "fi\n" +
                "git clone https://gitee.com/open-hand/middleware.git /tmp/middleware");
        return FileUtil.replaceReturnString(shellInputStream, map) + System.lineSeparator();
    }

    private String generatePrivateKey(List<DevopsHostDTO> devopsHostDTOList) {
        // 创建目录
        String mkdirCommand = "mkdir -p /tmp/ansible/ssh-key" + System.lineSeparator();
        // 创建密钥命令
        String generatePrivateKeyCommand = "";
        for (DevopsHostDTO devopsHostDTO : devopsHostDTOList) {
            if (HostAuthType.PUBLICKEY.value().equalsIgnoreCase(devopsHostDTO.getAuthType())) {
                generatePrivateKeyCommand += String.format(SAVE_PRIVATE_KEY_TEMPLATE, Base64Util.getBase64DecodedString(devopsHostDTO.getPassword()), String.format(PRIVATE_KEY_SAVE_PATH_TEMPLATE, devopsHostDTO.getName())) + System.lineSeparator();
            }
        }
        return mkdirCommand + generatePrivateKeyCommand;
    }

    private String generateHostConfiguration(MiddlewareInventoryVO middlewareInventoryVO, DevopsMiddlewareTypeEnum type) {
        return "cat <<EOF > /tmp/" + type.getType().toLowerCase() + "-inventory.ini" + System.lineSeparator() + middlewareInventoryVO.getInventoryConfiguration() + System.lineSeparator() + "EOF" + System.lineSeparator();
    }

    private String generateRedisConfiguration(String password, Map<String, String> configuration) {
        String command = "cat <<EOF > /tmp/redis-configuration.yml" + System.lineSeparator();
        Map<String, Map<String, String>> baseConfig = new HashMap<>();
        configuration.putIfAbsent("port", "6379");
        configuration.putIfAbsent("bind", "0.0.0.0");
        configuration.putIfAbsent("databases", "16");
        configuration.putIfAbsent("dir", "/var/lib/redis/data");
        configuration.putIfAbsent("logfile", "/var/log/redis/redis.log");
        configuration.putIfAbsent("requirepass", password);
        configuration.putIfAbsent("masterauth", password);
        configuration.putIfAbsent("appendonly", "yes");
        configuration.putIfAbsent("appendfsync", "everysec");
        configuration.putIfAbsent("no-appendfsync-on-rewrite", "yes");
        configuration.putIfAbsent("auto-aof-rewrite-percentage", "100");
        configuration.putIfAbsent("auto-aof-rewrite-min-size", "64mb");

        baseConfig.put("redis_base_config", configuration);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowReadOnlyProperties(true);
        Yaml yaml = new Yaml(options);
        String baseConfigInYaml = yaml.dump(baseConfig);
        Map<String, String> params = new HashMap<>();
        params.put("{{base-config}}", baseConfigInYaml);
        String redisConfiguration = FileUtil.replaceReturnString(DevopsMiddlewareServiceImpl.class.getResourceAsStream("/template/middleware/redis/redis-configuration.yml"), params);
        return command + redisConfiguration + System.lineSeparator() + "EOF" + System.lineSeparator();
    }

    private String generateMySqlConfiguration(String password, String virtualIp, Map<String, Map<String, String>> configurations) {
        StringBuilder command = new StringBuilder(MKDIR_FOR_MULTI_NODE_CONFIGURATION + System.lineSeparator());
        for (Map.Entry<String, Map<String, String>> entry : configurations.entrySet()) {

            String header = String.format("cat <<EOF >/tmp/middleware/host_vars/%s.yml", entry.getKey()) + System.lineSeparator();
            Map<String, String> mysqldConfiguration = entry.getValue();
            Map<String, Object> configuration = new HashMap<>();

            Map<String, String> authConfiguration = new HashMap<>();
            authConfiguration.put("replicationUser", "replicator");
            authConfiguration.put("replicationPassword", password);
            authConfiguration.putIfAbsent("rootPassword", password);

            mysqldConfiguration.putIfAbsent("datadir", "/var/lib/mysql");
            mysqldConfiguration.putIfAbsent("port", "3306");
            mysqldConfiguration.putIfAbsent("bind_address", "0.0.0.0");

            configuration.put("auth", authConfiguration);
            configuration.put("mysqld", mysqldConfiguration);
            configuration.put("lb_keepalived_virtual_ipaddress", virtualIp);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setAllowReadOnlyProperties(true);
            Yaml yaml = new Yaml(options);
            String baseConfigInYaml = yaml.dump(configuration);
            command.append(header).append(baseConfigInYaml).append(System.lineSeparator()).append("EOF").append(System.lineSeparator());
        }
        return command.toString();
    }

    private String generateInstallRedis() {
        return String.format(REDIS_ANSIBLE_COMMAND_TEMPLATE, REDIS_INSTALL_LOG_PATH) + System.lineSeparator();
    }

    private String generateInstallMySQL() {
        return String.format(MYSQL_ANSIBLE_COMMAND_TEMPLATE, MYSQL_INSTALL_LOG_PATH) + System.lineSeparator();
    }

    private String generateUninstallCommand(DevopsMiddlewareTypeEnum middlewareTypeEnum) {
        String inventoryIni;
        String uninstallCommand;
        String configuration = "";
        switch (middlewareTypeEnum) {
            case Redis:
                inventoryIni = "/tmp/redis-inventory.ini";
                uninstallCommand = REDIS_UNINSTALL_COMMAND;
                configuration = "-e @/tmp/redis-configuration.yml";
                break;
            case MySQL:
                inventoryIni = "/tmp/mysql-inventory.ini";
                uninstallCommand = MYSQL_UNINSTALL_COMMAND;
                break;
            default:
                throw new CommonException("devops.middleware.unsupported.type", middlewareTypeEnum.getType());
        }
        return String.format(UNINSTALL_MIDDLEWARE_ANSIBLE_COMMAND_TEMPLATE, inventoryIni, configuration, uninstallCommand, "/tmp/middleware-uninstall.log");
    }

    private String generateShellHeader() {
        return SHELL_HEADER;
    }
}
