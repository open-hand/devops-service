package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_DEPLOY_REDIS;
import static io.choerodon.devops.infra.constant.DevopsAnsibleCommandConstants.*;
import static io.choerodon.devops.infra.enums.DevopsMiddlewareTypeEnum.REDIS;
import static org.hzero.core.util.StringPool.SLASH;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.schmizz.sshj.SSHClient;
import org.apache.commons.io.IOUtils;
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

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.validator.AppServiceInstanceValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.deploy.DeploySourceVO;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.DevopsMiddlewareRedisDeployPayload;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsMiddlewareService;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.DevopsMiddlewareDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.DeployModeEnum;
import io.choerodon.devops.infra.enums.deploy.DeployObjectTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;
import io.choerodon.devops.infra.mapper.DevopsMiddlewareMapper;
import io.choerodon.devops.infra.util.*;

@Service
public class DevopsMiddlewareServiceImpl implements DevopsMiddlewareService {

    private static final String STANDALONE_PERSISTENCE_TEMPLATE = "persistence:\n" +
            "  existingClaim: %s";
    private static final String SENTINEL_MATCHLABELS_TEMPLATE = "matchLabels:\n%s";

    private static final String CONFIGMAP_TEMPLATE = "configmap: |-\n%s";

    private static final String MATCHLABELS_TEMPLATE = "    %s: %s\n";

    private static final String CONFIGMAP_VALUE_TEMPLATE = "  %s %s\n";

    private static final String STANDALONE_MODE = "standalone";

    private static final String SENTINEL_MODE = "sentinel";

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsMiddlewareServiceImpl.class);

    private static final String REDIS_SENTINEL_VALUE_TEMPLATE;

    private static final String REDIS_STANDALONE_VALUE_TEMPLATE;

    private static final String MIDDLEWARE_STATUS_SYNC_LOCK = "middleware-status-sync-lock";

    static {
        InputStream redisSentinelInputStream = DevopsMiddlewareServiceImpl.class.getResourceAsStream("/template/redis-sentinel-value-template.yaml");
        InputStream redisStandaloneInputStream = DevopsMiddlewareServiceImpl.class.getResourceAsStream("/template/redis-standalone-value-template.yaml");
        try {
            REDIS_SENTINEL_VALUE_TEMPLATE = IOUtils.toString(redisSentinelInputStream, StandardCharsets.UTF_8);
            REDIS_STANDALONE_VALUE_TEMPLATE = IOUtils.toString(redisStandaloneInputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException("error.load.ci.sh");
        }
    }

    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private SshUtil sshUtil;
    @Autowired
    private DevopsHostMapper devopsHostMapper;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsMiddlewareMapper devopsMiddlewareMapper;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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

        if (STANDALONE_MODE.equals(middlewareRedisEnvDeployVO.getMode())) {
            middlewareRedisEnvDeployVO.setValues(generateRedisStandaloneValues(middlewareRedisEnvDeployVO));
        }

        if (SENTINEL_MODE.equals(middlewareRedisEnvDeployVO.getMode())) {
            middlewareRedisEnvDeployVO.setValues(generateRedisSentinelValues(middlewareRedisEnvDeployVO));
        }

        MarketInstanceCreationRequestVO marketInstanceCreationRequestVO = ConvertUtils.convertObject(middlewareRedisEnvDeployVO, MarketInstanceCreationRequestVO.class);

        return appServiceInstanceService.createOrUpdateMarketInstance(projectId, marketInstanceCreationRequestVO);
    }

    @Override
    public AppServiceInstanceVO updateRedisInstance(Long projectId, MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO) {
        return appServiceInstanceService.createOrUpdateMarketInstance(projectId, ConvertUtils.convertObject(middlewareRedisEnvDeployVO, MarketInstanceCreationRequestVO.class));
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
            middlewareRedisEnvDeployVO.setMode(SENTINEL_MODE);
            middlewareRedisEnvDeployVO.setSlaveCount((Integer) ((Map) values.get("cluster")).get("slaveCount"));
            middlewareRedisEnvDeployVO.setPvLabels((Map<String, String>) ((Map) values.get("slave")).get("matchLabels"));
            middlewareRedisEnvDeployVO.setConfiguration(getConfigMap((String) values.get("configmap")));
        } else {
            middlewareRedisEnvDeployVO.setMode(STANDALONE_MODE);
            if (values.get("persistence") != null) {
                middlewareRedisEnvDeployVO.setPvcName((String) ((Map) values.get("persistence")).get("existingClaim"));
            }
            middlewareRedisEnvDeployVO.setConfiguration(getConfigMap((String) values.get("configmap")));
        }

        return middlewareRedisEnvDeployVO;
    }

    @Override
    @Saga(code = DEVOPS_DEPLOY_REDIS,
            description = "主机部署redis中间件", inputSchemaClass = MiddlewareRedisHostDeployVO.class)
    @Transactional(rollbackFor = Exception.class)
    public void hostDeployForRedis(Long projectId, MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO) {
        checkMiddlewareName(projectId, middlewareRedisHostDeployVO.getName(), REDIS.getType());
        if (SENTINEL_MODE.equals(middlewareRedisHostDeployVO.getMode())) {
            CommonExAssertUtil.assertTrue(middlewareRedisHostDeployVO.getHostIds().size() >= 3, "error.host.size.less.than.3");
        }

        List<DevopsHostDTO> devopsHostDTOList = devopsHostMapper.listByProjectIdAndIds(projectId, middlewareRedisHostDeployVO.getHostIds());

        if (SENTINEL_MODE.equals(middlewareRedisHostDeployVO.getMode())) {
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

        Long deployRecordId = devopsDeployRecordService.saveRecord(
                projectId,
                DeployType.BASE_COMPONENT,
                null,
                DeployModeEnum.HOST,
                devopsHostDTOForConnection.getId(),
                devopsHostDTOForConnection.getName(),
                CommandStatus.OPERATING.getStatus(),
                DeployObjectTypeEnum.MIDDLEWARE,
                middlewareRedisHostDeployVO.getName(),
                middlewareRedisHostDeployVO.getVersion(),
                null,
                deploySourceVO);

        // 保存中间件信息
        saveMiddlewareInfo(projectId,
                middlewareRedisHostDeployVO.getName(),
                REDIS.getType(),
                middlewareRedisHostDeployVO.getMode(),
                middlewareRedisHostDeployVO.getVersion(),
                devopsHostDTOList.stream().map(h -> String.valueOf(h.getId())).collect(Collectors.joining(",")),
                JsonHelper.marshalByJackson(middlewareRedisHostDeployVO.getConfiguration()));

        DevopsMiddlewareRedisDeployPayload devopsMiddlewareRedisDeployPayload = new DevopsMiddlewareRedisDeployPayload();
        devopsMiddlewareRedisDeployPayload.setProjectId(projectId);
        devopsMiddlewareRedisDeployPayload.setMiddlewareRedisHostDeployVO(middlewareRedisHostDeployVO);
        devopsMiddlewareRedisDeployPayload.setDeploySourceVO(deploySourceVO);
        devopsMiddlewareRedisDeployPayload.setDeployRecordId(deployRecordId);

        producer.apply(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(projectId)
                        .withRefType("host")
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_DEPLOY_REDIS),
                builder -> builder
                        .withPayloadAndSerialize(devopsMiddlewareRedisDeployPayload)
                        .withRefId(deployRecordId.toString()));
    }

    @Override
    public void hostDeployForRedis(DevopsMiddlewareRedisDeployPayload devopsMiddlewareRedisDeployPayload) {
        MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO = devopsMiddlewareRedisDeployPayload.getMiddlewareRedisHostDeployVO();
        DevopsHostDTO devopsHostDTOForConnection = null;
        List<DevopsHostDTO> devopsHostDTOList = devopsHostMapper.listByProjectIdAndIds(devopsMiddlewareRedisDeployPayload.getProjectId(), middlewareRedisHostDeployVO.getHostIds());
        Optional<DevopsHostDTO> any = devopsHostDTOList.stream().filter(d -> !StringUtils.isEmpty(d.getHostIp()) && d.getSshPort() != null).findAny();
        if (any.isPresent()) {
            devopsHostDTOForConnection = any.get();
        }

        CommonExAssertUtil.assertTrue(devopsHostDTOForConnection != null, "error.host.ip");


        LOGGER.info("========================================");
        LOGGER.info("start to deploy Middleware Redis,mode:{} version:{} projectId:{}", middlewareRedisHostDeployVO.getMode(), middlewareRedisHostDeployVO.getVersion(), devopsMiddlewareRedisDeployPayload.getProjectId());

        HostConnectionVO hostConnectionVO = ConvertUtils.convertObject(devopsHostDTOForConnection, HostConnectionVO.class);
        hostConnectionVO.setHostPort(devopsHostDTOForConnection.getSshPort());
        SSHClient sshClient = new SSHClient();

        try {
            sshUtil.sshConnect(hostConnectionVO, sshClient);
            ExecResultInfoVO resultInfoVO;
            // 安装ansible、git初始化文件
            sshUtil.uploadPreProcessShell(sshClient, DeployObjectTypeEnum.MIDDLEWARE.value(), DeployObjectTypeEnum.MIDDLEWARE.value());
            resultInfoVO = sshUtil.execCommand(sshClient, String.format(BASH_COMMAND_TEMPLATE, PRE_KUBEADM_HA_SH));
            if (resultInfoVO != null && resultInfoVO.getExitCode() != 0) {
                throw new Exception(String.format("failed to initialize the environment on host: [ %s ],error is :%s <<<<<<<<<", sshClient.getRemoteHostname(), resultInfoVO.getStdErr()));
            }

            // 创建节点密钥文件
            generateAndUploadPrivateKey(sshClient, devopsHostDTOList);
            LOGGER.info("rsa file upload completed");

            MiddlewareInventoryVO middlewareInventoryVO = calculateGeneralInventoryValue(devopsHostDTOList, REDIS);

            // 上传节点配置文件
            generateAndUploadRedisHostConfiguration(sshClient, middlewareInventoryVO);
            LOGGER.info("node configuration file upload completed");

            // 生成并上传redis配置文件
            generateAndUploadRedisConfiguration(sshClient, middlewareRedisHostDeployVO);
            LOGGER.info("redis configuration file upload completed");

            // 安装redis
            resultInfoVO = executeInstallRedis(sshClient);
            LOGGER.info("the Redis installation command has finished");

            if (resultInfoVO.getExitCode() != 0) {
                throw new CommonException("failed to install redis");
            }

            devopsDeployRecordService.updateRecord(devopsMiddlewareRedisDeployPayload.getDeployRecordId(), CommandStatus.SUCCESS.getStatus());
            LOGGER.info("========================================");
            LOGGER.info("deploy Middleware Redis,mode:{} version:{} projectId:{}", middlewareRedisHostDeployVO.getMode(), middlewareRedisHostDeployVO.getVersion(), devopsMiddlewareRedisDeployPayload.getProjectId());
        } catch (Exception e) {
            devopsDeployRecordService.updateRecord(devopsMiddlewareRedisDeployPayload.getDeployRecordId(), CommandStatus.FAILED.getStatus());
            throw new CommonException(e.getMessage());
        } finally {
            sshUtil.closeSsh(sshClient, null);
        }
    }

    @Override
    public void saveMiddlewareInfo(Long projectId, String name, String type, String mode, String version, String hostIds, String configuration) {
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
                    .filter(d -> currentTimeMillis - d.getCreationDate().getTime() > 3600000)
                    .peek(d -> d.setDeployResult(CommandStatus.FAILED.getStatus()))
                    .collect(Collectors.toList());
            // 将中间件状态设置为超时
            timeoutRecords.forEach(d -> devopsDeployRecordService.updateRecord(d));
        } finally {
            stringRedisTemplate.delete(MIDDLEWARE_STATUS_SYNC_LOCK);
        }
    }

    private MiddlewareInventoryVO calculateGeneralInventoryValue(List<DevopsHostDTO> devopsHostDTOList, DevopsMiddlewareTypeEnum middlewareTypeEnum) {
        MiddlewareInventoryVO middlewareInventoryVO = new MiddlewareInventoryVO();
        for (DevopsHostDTO hostDTO : devopsHostDTOList) {
            if (HostAuthType.ACCOUNTPASSWORD.value().equals(hostDTO.getAuthType())) {
                middlewareInventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL_PASSWORD_TYPE, hostDTO.getName(), hostDTO.getPrivateIp(), hostDTO.getPrivatePort(), hostDTO.getUsername(), hostDTO.getPassword()))
                        .append(System.lineSeparator());
            } else {
                middlewareInventoryVO.getAll().append(String.format(INVENTORY_INI_TEMPLATE_FOR_ALL_PRIVATE_KEY_TYPE, hostDTO.getName(), hostDTO.getPrivateIp(), hostDTO.getPrivatePort(), hostDTO.getUsername(), String.format(PRIVATE_KEY_SAVE_PATH_TEMPLATE, hostDTO.getName())))
                        .append(System.lineSeparator());
            }
            // 设置chrony节点
            middlewareInventoryVO.getChrony().append(hostDTO.getName())
                    .append(System.lineSeparator());

            switch (middlewareTypeEnum) {
                case REDIS:
                    middlewareInventoryVO.getRedis().append(hostDTO.getName())
                            .append(System.lineSeparator());
                    break;
                default:
                    throw new CommonException("error.middleware.unsupported.type", middlewareTypeEnum.getType());
            }
        }
        return middlewareInventoryVO;
    }

    private void generateAndUploadRedisHostConfiguration(SSHClient sshClient, MiddlewareInventoryVO middlewareInventoryVO) {
        String inventory = generateRedisInventoryInI(middlewareInventoryVO);
        String filePath = String.format(ANSIBLE_CONFIG_BASE_DIR_TEMPLATE, REDIS.getType()) + SLASH + "middleware-inventory.ini";
        String targetFilePath = BASE_DIR + SLASH + "middleware-inventory.ini";
        FileUtil.saveDataToFile(filePath, inventory);
        sshUtil.uploadFile(sshClient, filePath, targetFilePath);
    }

    private void generateAndUploadRedisConfiguration(SSHClient ssh, MiddlewareRedisHostDeployVO middlewareRedisHostDeployVO) throws IOException {
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
        String redisConfiguration = FileUtil.replaceReturnString(DevopsMiddlewareServiceImpl.class.getResourceAsStream("/template/redis-configuration.yml"), params);
        sshUtil.execCommand(ssh, String.format(SAVE_REDIS_CONFIGURATION, redisConfiguration));
    }

    private void generateAndUploadPrivateKey(SSHClient ssh, List<DevopsHostDTO> devopsHostDTOList) throws IOException {
        // 创建目录
        sshUtil.execCommand(ssh, "mkdir -p /tmp/ansible/ssh-key");

        List<String> commands = new ArrayList<>();
        for (DevopsHostDTO devopsHostDTO : devopsHostDTOList) {
            if (HostAuthType.PUBLICKEY.value().equalsIgnoreCase(devopsHostDTO.getAuthType())) {
                commands.add(String.format(SAVE_PRIVATE_KEY_TEMPLATE, Base64Util.getBase64DecodedString(devopsHostDTO.getPassword()), String.format(PRIVATE_KEY_SAVE_PATH_TEMPLATE, devopsHostDTO.getName())));
            }
        }
        sshUtil.execCommands(ssh, commands);
    }

    private String generateRedisInventoryInI(MiddlewareInventoryVO middlewareInventoryVO) {
        Map<String, String> map = new HashMap<>();
        map.put("{{all}}", middlewareInventoryVO.getAll().toString());
        map.put("{{chrony}}", middlewareInventoryVO.getChrony().toString());
        map.put("{{redis}}", middlewareInventoryVO.getRedis().toString());
        InputStream inventoryIniInputStream = DevopsMiddlewareServiceImpl.class.getResourceAsStream("/template/middleware-inventory.ini");

        return FileUtil.replaceReturnString(inventoryIniInputStream, map);
    }

    private ExecResultInfoVO executeInstallRedis(SSHClient sshClient) throws IOException {
        return sshUtil.execCommand(sshClient, REDIS_ANSIBLE_COMMAND_TEMPLATE);
    }

    private String generateRedisStandaloneValues(MiddlewareRedisEnvDeployVO middlewareRedisEnvDeployVO) {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("{{ password }}", middlewareRedisEnvDeployVO.getPassword());
        configuration.put("{{ usePassword }}", "true");

        if (!StringUtils.isEmpty(middlewareRedisEnvDeployVO.getPvcName())) {
            configuration.put("{{ persistence-enabled }}", "true");
            configuration.put("{{ persistence-info }}", String.format(STANDALONE_PERSISTENCE_TEMPLATE, middlewareRedisEnvDeployVO.getPvcName()));
        } else {
            configuration.put("{{ persistence-enabled }}", "false");
            configuration.put("{{ persistence-info }}", "");
        }

        configuration.put("{{ sysctlImage-enabled }}", middlewareRedisEnvDeployVO.getSysctlImage().toString());
        configuration.put("{{ sysctlImage-mountHostSys }}", middlewareRedisEnvDeployVO.getSysctlImage().toString());

        if (!CollectionUtils.isEmpty(middlewareRedisEnvDeployVO.getConfiguration())) {
            StringBuilder configMapSb = new StringBuilder();
            middlewareRedisEnvDeployVO.getConfiguration().forEach((k, v) -> configMapSb.append(String.format(CONFIGMAP_VALUE_TEMPLATE, k, v)));
            configuration.put("{{ configmap }}", String.format(CONFIGMAP_TEMPLATE, configMapSb.toString()));
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
            middlewareRedisEnvDeployVO.getPvLabels().forEach((k, v) -> stringBuilder.append(String.format(MATCHLABELS_TEMPLATE, k, v)));
            configuration.put("{{ matchLabels }}", String.format(SENTINEL_MATCHLABELS_TEMPLATE, stringBuilder.toString()));
        } else {
            configuration.put("{{ persistence-enabled }}", "false");
            configuration.put("{{ matchLabels }}", "");
        }

        configuration.put("{{ sysctlImage-enabled }}", middlewareRedisEnvDeployVO.getSysctlImage().toString());
        configuration.put("{{ sysctlImage-mountHostSys }}", middlewareRedisEnvDeployVO.getSysctlImage().toString());

        if (!CollectionUtils.isEmpty(middlewareRedisEnvDeployVO.getConfiguration())) {
            StringBuilder configMapSb = new StringBuilder();
            middlewareRedisEnvDeployVO.getConfiguration().forEach((k, v) -> configMapSb.append(String.format(CONFIGMAP_VALUE_TEMPLATE, k, v)));
            configuration.put("{{ configmap }}", String.format(CONFIGMAP_TEMPLATE, configMapSb.toString()));
        }

        return FileUtil.replaceReturnString(REDIS_SENTINEL_VALUE_TEMPLATE, configuration);
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

    public void checkMiddlewareName(Long projectId, String name, String type) {
        AppServiceInstanceValidator.checkName(name);
        CommonExAssertUtil.assertTrue(devopsMiddlewareMapper.checkNameUnique(projectId, name, type) < 1, "error.middleware.name.exists");
    }
}
