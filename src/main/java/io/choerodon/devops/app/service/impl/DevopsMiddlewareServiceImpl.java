package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.DevopsAnsibleCommandConstants.*;
import static io.choerodon.devops.infra.enums.DevopsMiddlewareTypeEnum.MYSQL;
import static io.choerodon.devops.infra.enums.DevopsMiddlewareTypeEnum.REDIS;
import static io.choerodon.devops.infra.enums.deploy.MiddlewareDeployModeEnum.*;
import static io.choerodon.devops.infra.enums.host.HostCommandEnum.DEPLOY_MIDDLEWARE;
import static io.choerodon.devops.infra.enums.host.HostInstanceType.MIDDLEWARE_MYSQL;
import static io.choerodon.devops.infra.enums.host.HostInstanceType.MIDDLEWARE_REDIS;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.AppServiceInstanceValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.host.HostAgentMsgVO;
import io.choerodon.devops.api.vo.host.MiddlewareDeployVO;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.DevopsHostConstants;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.dto.DevopsHostCommandDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.DevopsMiddlewareDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.enums.host.HostCommandEnum;
import io.choerodon.devops.infra.enums.host.HostCommandStatusEnum;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsMiddlewareServiceImpl.class);

    private static final String REDIS_SENTINEL_VALUE_TEMPLATE;

    private static final String REDIS_STANDALONE_VALUE_TEMPLATE;

    private static final String MYSQL_STANDALONE_VALUE_TEMPLATE;

    private static final String MIDDLEWARE_STATUS_SYNC_LOCK = "middleware-status-sync-lock";

    static {
        try (InputStream redisSentinelInputStream = DevopsMiddlewareServiceImpl.class.getResourceAsStream("/template/middleware/redis/redis-sentinel-value-template.yaml")) {
            REDIS_SENTINEL_VALUE_TEMPLATE = IOUtils.toString(redisSentinelInputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CommonException("error.middleware.value.template");
        }
        try (InputStream redisStandaloneInputStream = DevopsMiddlewareServiceImpl.class.getResourceAsStream("/template/middleware/redis/redis-standalone-value-template.yaml")) {
            REDIS_STANDALONE_VALUE_TEMPLATE = IOUtils.toString(redisStandaloneInputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CommonException("error.middleware.value.template");
        }
        try (InputStream mysqlStandaloneInputStream = DevopsMiddlewareServiceImpl.class.getResourceAsStream("/template/middleware/mysql/mysql-standalone-value-template.yaml")) {
            MYSQL_STANDALONE_VALUE_TEMPLATE = IOUtils.toString(mysqlStandaloneInputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CommonException("error.middleware.value.template");
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
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private EncryptService encryptService;
    @Autowired
    private DevopsPvcService devopsPvcService;
    @Autowired
    private KeySocketSendHelper webSocketHelper;
    @Autowired
    private DevopsHostCommandService devopsHostCommandService;

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
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(REDIS.getType(), middlewareRedisEnvDeployVO.getMode(), middlewareRedisEnvDeployVO.getVersion());

        middlewareRedisEnvDeployVO.setMarketDeployObjectId(middlewareServiceReleaseInfo.getId());
        middlewareRedisEnvDeployVO.setMarketAppServiceId(middlewareServiceReleaseInfo.getMarketServiceId());

        if (STANDALONE.getValue().equals(middlewareRedisEnvDeployVO.getMode())) {
            middlewareRedisEnvDeployVO.setValues(generateRedisStandaloneValues(middlewareRedisEnvDeployVO));
        }

        if (SENTINEL.getValue().equals(middlewareRedisEnvDeployVO.getMode())) {
            middlewareRedisEnvDeployVO.setValues(generateRedisSentinelValues(middlewareRedisEnvDeployVO));
        }

        MarketInstanceCreationRequestVO marketInstanceCreationRequestVO = ConvertUtils.convertObject(middlewareRedisEnvDeployVO, MarketInstanceCreationRequestVO.class);

        return appServiceInstanceService.createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO, true);
    }

    @Override
    public AppServiceInstanceVO envDeployForMySql(Long projectId, MiddlewareMySqlEnvDeployVO middlewareMySqlEnvDeployVO) {
        // 根据部署模式以及版本查询部署部署对象id和市场服务id
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(MYSQL.getType(), STANDALONE.getValue(), middlewareMySqlEnvDeployVO.getVersion());

        middlewareMySqlEnvDeployVO.setMarketDeployObjectId(middlewareServiceReleaseInfo.getId());
        middlewareMySqlEnvDeployVO.setMarketAppServiceId(middlewareServiceReleaseInfo.getMarketServiceId());

        middlewareMySqlEnvDeployVO.setValues(generateMysqlStandaloneValues(middlewareMySqlEnvDeployVO));

        MarketInstanceCreationRequestVO marketInstanceCreationRequestVO = ConvertUtils.convertObject(middlewareMySqlEnvDeployVO, MarketInstanceCreationRequestVO.class);
        return appServiceInstanceService.createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO, true);
    }

    @Override
    public AppServiceInstanceVO updateRedisInstance(Long projectId, MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO) {
        return appServiceInstanceService.createOrUpdateMarketInstance(projectId, ConvertUtils.convertObject(middlewareRedisEnvDeployVO, MarketInstanceCreationRequestVO.class), true);
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
        checkMiddlewareName(projectId, middlewareRedisHostDeployVO.getName(), REDIS.getType());

        List<DevopsHostDTO> devopsHostDTOList = devopsHostMapper.listByProjectIdAndIds(projectId, middlewareRedisHostDeployVO.getHostIds());

        if (SENTINEL.getValue().equals(middlewareRedisHostDeployVO.getMode())) {
            CommonExAssertUtil.assertTrue(middlewareRedisHostDeployVO.getHostIds().size() >= 3, "error.host.size.less.than.3");
            CommonExAssertUtil.assertTrue(devopsHostDTOList.size() >= 3, "error.host.size.less.than.3");
        }

        // 根据部署模式以及版本查询部署部署对象id和市场服务id
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(REDIS.getType(), middlewareRedisHostDeployVO.getMode(), middlewareRedisHostDeployVO.getVersion());

        DevopsHostDTO devopsHostDTOForConnection = devopsHostDTOList.get(0);

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setDeployObjectId(middlewareServiceReleaseInfo.getId());
        deploySourceVO.setType(AppSourceType.PLATFORM_PRESET.getValue());
        deploySourceVO.setProjectName(projectDTO.getName());

        Long recordId = devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.BASE_COMPONENT,
                null,
                DeployModeEnum.HOST,
                devopsHostDTOForConnection.getId(),
                devopsHostDTOForConnection.getName(),
                CommandStatus.OPERATING.getStatus(),
                DeployObjectTypeEnum.MIDDLEWARE,
                middlewareRedisHostDeployVO.getName(),
                middlewareRedisHostDeployVO.getVersion() + "-" + middlewareRedisHostDeployVO.getMode(),
                null,
                deploySourceVO);

        // 保存中间件信息
        DevopsMiddlewareDTO devopsMiddlewareDTO = saveMiddlewareInfo(projectId,
                middlewareRedisHostDeployVO.getName(),
                REDIS.getType(),
                middlewareRedisHostDeployVO.getMode(),
                middlewareRedisHostDeployVO.getVersion(),
                devopsHostDTOList.stream().map(h -> String.valueOf(h.getId())).collect(Collectors.joining(",")),
                JsonHelper.marshalByJackson(middlewareRedisHostDeployVO.getConfiguration()));

        String deployShell;
        try {
            MiddlewareInventoryVO middlewareInventoryVO = calculateGeneralInventoryValue(devopsHostDTOList, REDIS);
            // 安装ansible、git初始化文件
            deployShell = preProcessShell()
                    + generatePrivateKey(devopsHostDTOList)
                    + generateHostConfiguration(middlewareInventoryVO, REDIS)
                    + generateRedisConfiguration(middlewareRedisHostDeployVO)
                    + generateInstallRedis();

            DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
            devopsHostCommandDTO.setHostId(devopsHostDTOForConnection.getId());
            devopsHostCommandDTO.setInstanceId(devopsMiddlewareDTO.getId());
            devopsHostCommandDTO.setCommandType(DEPLOY_MIDDLEWARE.value());
            devopsHostCommandDTO.setInstanceType(MIDDLEWARE_REDIS.value());
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
            devopsHostCommandService.baseCreate(devopsHostCommandDTO);


            MiddlewareDeployVO middlewareDeployVO = new MiddlewareDeployVO();
            middlewareDeployVO.setMiddlewareType(REDIS.getType());
            middlewareDeployVO.setDeployShell(deployShell);
            middlewareDeployVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
            middlewareDeployVO.setRecordId(String.valueOf(recordId));

            HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
            hostAgentMsgVO.setHostId(String.valueOf(devopsHostDTOForConnection.getId()));
            hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_MIDDLEWARE.value());
            hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
            hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(middlewareDeployVO));

            webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + devopsHostDTOForConnection.getId(),
                    String.format(DevopsHostConstants.MIDDLEWARE_INSTANCE, devopsHostDTOForConnection.getId(), devopsMiddlewareDTO.getId()),
                    JsonHelper.marshalByJackson(hostAgentMsgVO));
            LOGGER.info("deploy Middleware Redis,mode:{} version:{} projectId:{}", middlewareRedisHostDeployVO.getMode(), middlewareRedisHostDeployVO.getVersion(), projectId);
        } catch (Exception e) {
            devopsDeployRecordService.updateRecord(recordId, CommandStatus.FAILED.getStatus(), e.getMessage());
            throw new CommonException(e.getMessage());
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hostDeployForMySql(Long projectId, MiddlewareMySqlHostDeployVO middlewareMySqlHostDeployVO) {
        // master-slave模式，节点数量必须为2，且需要设置virtualIp
        if (MASTER_SLAVE.getValue().equals(middlewareMySqlHostDeployVO.getMode()) && middlewareMySqlHostDeployVO.getHostIds().size() != 2) {
            if (middlewareMySqlHostDeployVO.getHostIds().size() != 2) {
                throw new CommonException("error.mysql.master-slave.host.count");
            }
            if (!GitOpsConstants.IP_REG_PATTERN.matcher(middlewareMySqlHostDeployVO.getVirtualIp()).matches()) {
                throw new CommonException("error.virtual.ip.invalid");
            }
        }

        checkMiddlewareName(projectId, middlewareMySqlHostDeployVO.getName(), MYSQL.getType());

        List<DevopsHostDTO> devopsHostDTOList = devopsHostMapper.listByProjectIdAndIds(projectId, middlewareMySqlHostDeployVO.getHostIds());

        convertMySQLConfiguration(middlewareMySqlHostDeployVO, devopsHostDTOList.stream().collect(Collectors.toMap(DevopsHostDTO::getId, Function.identity())));

        // 根据部署模式以及版本查询部署部署对象id和市场服务id
        MarketServiceDeployObjectVO middlewareServiceReleaseInfo = marketServiceClientOperator.getMiddlewareServiceReleaseInfo(MYSQL.getType(), middlewareMySqlHostDeployVO.getMode(), middlewareMySqlHostDeployVO.getVersion());

        DevopsHostDTO devopsHostDTOForConnection = devopsHostDTOList.get(0);

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        DeploySourceVO deploySourceVO = new DeploySourceVO();
        deploySourceVO.setDeployObjectId(middlewareServiceReleaseInfo.getId());
        deploySourceVO.setType(AppSourceType.PLATFORM_PRESET.getValue());
        deploySourceVO.setProjectName(projectDTO.getName());

        Long recordId = devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.BASE_COMPONENT,
                null,
                DeployModeEnum.HOST,
                devopsHostDTOForConnection.getId(),
                devopsHostDTOForConnection.getName(),
                CommandStatus.OPERATING.getStatus(),
                DeployObjectTypeEnum.MIDDLEWARE,
                middlewareMySqlHostDeployVO.getName(),
                middlewareMySqlHostDeployVO.getVersion() + "-" + middlewareMySqlHostDeployVO.getMode(),
                null,
                deploySourceVO);

        // 保存中间件信息
        DevopsMiddlewareDTO devopsMiddlewareDTO = saveMiddlewareInfo(projectId,
                middlewareMySqlHostDeployVO.getName(),
                MYSQL.getType(),
                middlewareMySqlHostDeployVO.getMode(),
                middlewareMySqlHostDeployVO.getVersion(),
                devopsHostDTOList.stream().map(h -> String.valueOf(h.getId())).collect(Collectors.joining(",")),
                JsonHelper.marshalByJackson(middlewareMySqlHostDeployVO.getConfiguration()));

        String deployShell;
        try {
            MiddlewareInventoryVO middlewareInventoryVO = calculateGeneralInventoryValue(devopsHostDTOList, MYSQL);
            // 安装ansible、git初始化文件
            deployShell = preProcessShell()
                    + generatePrivateKey(devopsHostDTOList)
                    + generateHostConfiguration(middlewareInventoryVO, MYSQL)
                    + generateMySqlConfiguration(middlewareMySqlHostDeployVO)
                    + generateInstallMySQL();

            DevopsHostCommandDTO devopsHostCommandDTO = new DevopsHostCommandDTO();
            devopsHostCommandDTO.setHostId(devopsHostDTOForConnection.getId());
            devopsHostCommandDTO.setInstanceId(devopsMiddlewareDTO.getId());
            devopsHostCommandDTO.setCommandType(DEPLOY_MIDDLEWARE.value());
            devopsHostCommandDTO.setInstanceType(MIDDLEWARE_MYSQL.value());
            devopsHostCommandDTO.setStatus(HostCommandStatusEnum.OPERATING.value());
            devopsHostCommandService.baseCreate(devopsHostCommandDTO);

            MiddlewareDeployVO middlewareDeployVO = new MiddlewareDeployVO();
            middlewareDeployVO.setMiddlewareType(MYSQL.getType());
            middlewareDeployVO.setDeployShell(deployShell);
            middlewareDeployVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
            middlewareDeployVO.setInstanceId(String.valueOf(devopsMiddlewareDTO.getId()));
            middlewareDeployVO.setRecordId(String.valueOf(recordId));

            HostAgentMsgVO hostAgentMsgVO = new HostAgentMsgVO();
            hostAgentMsgVO.setHostId(String.valueOf(devopsHostDTOForConnection.getId()));
            hostAgentMsgVO.setType(HostCommandEnum.DEPLOY_MIDDLEWARE.value());
            hostAgentMsgVO.setCommandId(String.valueOf(devopsHostCommandDTO.getId()));
            hostAgentMsgVO.setPayload(JsonHelper.marshalByJackson(middlewareDeployVO));

            webSocketHelper.sendByGroup(DevopsHostConstants.GROUP + devopsHostDTOForConnection.getId(),
                    String.format(DevopsHostConstants.MIDDLEWARE_INSTANCE, devopsHostDTOForConnection.getId(), devopsMiddlewareDTO.getId()),
                    JsonHelper.marshalByJackson(hostAgentMsgVO));
            LOGGER.info("deploy Middleware MySQL,mode:{} version:{} projectId:{}", middlewareMySqlHostDeployVO.getMode(), middlewareMySqlHostDeployVO.getVersion(), projectId);
        } catch (Exception e) {
            devopsDeployRecordService.updateRecord(recordId, CommandStatus.FAILED.getStatus(), e.getMessage());
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public DevopsMiddlewareDTO saveMiddlewareInfo(Long projectId, String name, String type, String mode, String version, String hostIds, String configuration) {
        DevopsMiddlewareDTO devopsMiddlewareDTO = new DevopsMiddlewareDTO(
                projectId,
                name,
                type,
                mode,
                version,
                hostIds,
                configuration
        );
        MapperUtil.resultJudgedInsertSelective(devopsMiddlewareMapper, devopsMiddlewareDTO, "error.middleware.insert");
        return devopsMiddlewareDTO;
    }

    @Override
    public void updateMiddlewareStatus() {
        // 添加redis锁，防止多个pod并发更新状态
        if (!Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(MIDDLEWARE_STATUS_SYNC_LOCK, "lock", 1, TimeUnit.MINUTES))) {
            return;
        }
        try {
            // 查处所有处于操作中的中间件
            DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO();
            devopsDeployRecordDTO.setDeployResult(CommandStatus.OPERATING.getStatus());
            devopsDeployRecordDTO.setDeployObjectType(DeployObjectTypeEnum.MIDDLEWARE.value());
            List<DevopsDeployRecordDTO> devopsDeployRecordDTOList = devopsDeployRecordService.baseList(devopsDeployRecordDTO);
            // 过滤出时间已经超过30分钟的中间件
            long currentTimeMillis = System.currentTimeMillis();
            List<DevopsDeployRecordDTO> timeoutRecords = devopsDeployRecordDTOList
                    .stream()
                    .filter(d -> currentTimeMillis - d.getCreationDate().getTime() > 1800000)
                    .peek(d -> {
                        d.setDeployResult(CommandStatus.FAILED.getStatus());
                        d.setErrorMessage("time out");
                    })
                    .collect(Collectors.toList());
            // 将中间件状态设置为超时
            timeoutRecords.forEach(d -> devopsDeployRecordService.updateRecord(d));
        } finally {
            stringRedisTemplate.delete(MIDDLEWARE_STATUS_SYNC_LOCK);
        }
    }

    private MiddlewareInventoryVO calculateGeneralInventoryValue(List<DevopsHostDTO> devopsHostDTOList, DevopsMiddlewareTypeEnum middlewareTypeEnum) {
        MiddlewareInventoryVO middlewareInventoryVO = new MiddlewareInventoryVO(middlewareTypeEnum);
        for (DevopsHostDTO hostDTO : devopsHostDTOList) {
            // 如果内网ip不存在，使用公网ip
            String ip = hostDTO.getHostIp();
            // 如果内网端口不存在，使用公网端口
            Integer port = hostDTO.getSshPort();
            if (HostAuthType.ACCOUNTPASSWORD.value().equals(hostDTO.getAuthType())) {
                middlewareInventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL_PASSWORD_TYPE, ip, ip, port, hostDTO.getUsername(), hostDTO.getPassword()))
                        .append(System.lineSeparator());
            } else {
                middlewareInventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL_PRIVATE_KEY_TYPE, ip, ip, port, hostDTO.getUsername(), String.format(PRIVATE_KEY_SAVE_PATH_TEMPLATE, hostDTO.getName())))
                        .append(System.lineSeparator());
            }
            // 设置chrony节点
            middlewareInventoryVO.getChrony().append(ip)
                    .append(System.lineSeparator());

            switch (middlewareTypeEnum) {
                case REDIS:
                    middlewareInventoryVO.getRedis().append(ip)
                            .append(System.lineSeparator());
                    break;
                case MYSQL:
                    middlewareInventoryVO.getMysql().append(ip)
                            .append(System.lineSeparator());
                    break;
                default:
                    throw new CommonException("error.middleware.unsupported.type", middlewareTypeEnum.getType());
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
            convertedConfiguration.put(devopsHostDTOMap.get(Long.parseLong(key)).getName(), v);
        });
        middlewareMySqlHostDeployVO.setConfiguration(convertedConfiguration);
    }

    public void checkMiddlewareName(Long projectId, String name, String type) {
        AppServiceInstanceValidator.checkName(name);
        CommonExAssertUtil.assertTrue(devopsMiddlewareMapper.checkNameUnique(projectId, name, type) < 1, "error.middleware.name.exists");
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

    private String generateRedisConfiguration(MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO) {
        String command = "cat <<EOF > /tmp/redis-configuration.yml" + System.lineSeparator();
        Map<String, String> configuration = middlewareRedisHostDeployVO.getConfiguration();
        Map<String, Map<String, String>> baseConfig = new HashMap<>();
        configuration.putIfAbsent("port", "6379");
        configuration.putIfAbsent("bind", "0.0.0.0");
        configuration.putIfAbsent("databases", "16");
        configuration.putIfAbsent("dir", "/var/lib/redis/data");
        configuration.putIfAbsent("logfile", "/var/log/redis/redis.log");
        configuration.putIfAbsent("requirepass", middlewareRedisHostDeployVO.getPassword());
        configuration.putIfAbsent("masterauth", middlewareRedisHostDeployVO.getPassword());
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

    private String generateMySqlConfiguration(MiddlewareMySqlHostDeployVO middlewareMySqlHostDeployVO) {
        StringBuilder command = new StringBuilder(MKDIR_FOR_MULTI_NODE_CONFIGURATION + System.lineSeparator());
        Map<String, Map<String, String>> configurations = middlewareMySqlHostDeployVO.getConfiguration();
        for (Map.Entry<String, Map<String, String>> entry : configurations.entrySet()) {

            String header = String.format("cat <<EOF >/tmp/middleware/host_vars/%s.yml", entry.getKey()) + System.lineSeparator();
            Map<String, String> mysqldConfiguration = entry.getValue();
            Map<String, Object> configuration = new HashMap<>();

            Map<String, String> authConfiguration = new HashMap<>();
            authConfiguration.put("replicationUser", "replicator");
            authConfiguration.put("replicationPassword", "Changeit!123");
            authConfiguration.putIfAbsent("rootPassword", middlewareMySqlHostDeployVO.getPassword());

            mysqldConfiguration.putIfAbsent("datadir", "/var/lib/mysql");
            mysqldConfiguration.putIfAbsent("port", "3306");
            mysqldConfiguration.putIfAbsent("bind_address", "0.0.0.0");

            configuration.put("auth", authConfiguration);
            configuration.put("mysqld", mysqldConfiguration);
            configuration.put("lb_keepalived_virtual_ipaddress", middlewareMySqlHostDeployVO.getVirtualIp());

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
}
