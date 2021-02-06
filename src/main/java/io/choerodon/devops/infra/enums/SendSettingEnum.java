package io.choerodon.devops.infra.enums;


import java.util.HashMap;
import java.util.Map;

/**
 * User: Mr.Wang
 * Date: 2020/3/23
 */
public enum SendSettingEnum {
    CREATE_APPSERVICE("CREATE_APP_SERVICE"),
    ENABLE_APPSERVICE("ENABLEAPPSERVICE"),
    DISABLE_APPSERVICE("DISABLEAPPSERVICE"),
    DELETE_APPSERVICE("DELETEAPPSERVICE"),
    APPSERVICE_CREATIONFAILURE("APPSERVICECREATIONFAILURE"),
    CREATE_ENV("CREATE_ENV"),
    ENABLE_ENV("ENABLE_ENV"),
    DISABLE_ENV("DISABLE_ENV"),
    DELETE_ENV("DELETE_ENV"),
    CREATE_ENVFAILED("CREATE_ENV_FAILED"),
    UPDATE_ENV_PERMISSIONS("UPDATE_ENV_PERMISSIONS"),
    CREATE_CLUSTER("CREATE_CLUSTER"),
    ACTIVATE_CLUSTER("ACTIVATE_CLUSTER"),
    DELETE_CLUSTER("DELETE_CLUSTER"),
    CREATE_CLUSTERFAILED("CREATE_CLUSTER_FAILED"),
    RESOURCE_INSTALLFAILED("RESOURCE_INSTALL_FAILED"),
    GITLAB_CD_FAILURE("gitLabContinuousDeliveryFailure"),
    GITLAB_CD_SUCCESS("gitLabContinuousDeliverySuccess"),
    CREATE_APPSERVICE_VERSION("APP_SERVICE_CREATE_VERSION"),
    CREATE_RESOURCE_FAILED("CREATE_RESOURCE_FAILED"),
    CREATE_RESOURCE("CREATE_RESOURCE"),
    DELETE_RESOURCE("DELETE_RESOURCE"),
    PIPELINE_SUCCESS("PIPELINESUCCESS"),
    PIPELINE_FAILED("PIPELINEFAILED"),
    PIPELINE_PASS("PIPELINEPASS"),
    PIPELINE_STOP("PIPELINESTOP");


    private static Map<String, String> map = new HashMap<>();

    private String sendSettingCode;


    static {
        map.put(SendSettingEnum.CREATE_APPSERVICE.value(), AppService.CREATE);
        map.put(SendSettingEnum.ENABLE_APPSERVICE.value(), AppService.ENABLE);
        map.put(SendSettingEnum.DISABLE_APPSERVICE.value(), AppService.DISABLE);
        map.put(SendSettingEnum.DELETE_APPSERVICE.value(), AppService.DELETE);
        map.put(SendSettingEnum.APPSERVICE_CREATIONFAILURE.value(), AppService.CREATION_FAILURE);
        map.put(SendSettingEnum.CREATE_ENV.value(), ENV.CREATE);
        map.put(SendSettingEnum.ENABLE_ENV.value(), ENV.ENABLE);
        map.put(SendSettingEnum.DISABLE_ENV.value(), ENV.DISABLE);
        map.put(SendSettingEnum.DELETE_ENV.value(), ENV.DELETE);
        map.put(SendSettingEnum.CREATE_ENVFAILED.value(), ENV.CREATEENVFAILED);
        map.put(SendSettingEnum.UPDATE_ENV_PERMISSIONS.value(), ENV.UPDATE_ENV_PERMISSIONS);
        map.put(SendSettingEnum.CREATE_CLUSTER.value(), Cluster.CREATE);
        map.put(SendSettingEnum.ACTIVATE_CLUSTER.value(), Cluster.ACTIVITE);
        map.put(SendSettingEnum.DELETE_CLUSTER.value(), Cluster.DELETE);
        map.put(SendSettingEnum.CREATE_CLUSTERFAILED.value(), Cluster.CREATE_FAILED);
        map.put(SendSettingEnum.RESOURCE_INSTALLFAILED.value(), Resource.INSTALL_FAILED);
        map.put(SendSettingEnum.GITLAB_CD_FAILURE.value(), GitLabCD.FAILURE);
        map.put(SendSettingEnum.GITLAB_CD_SUCCESS.value(), GitLabCD.SUCCESS);
        map.put(SendSettingEnum.CREATE_APPSERVICE_VERSION.value(), AppService.CREATE_VERSION);
        map.put(SendSettingEnum.CREATE_RESOURCE_FAILED.value(), Resource.CREATE_FAILED);
        map.put(SendSettingEnum.CREATE_RESOURCE.value(), Resource.CREATE);
        map.put(SendSettingEnum.DELETE_RESOURCE.value(), Resource.DELETE);
        map.put(SendSettingEnum.PIPELINE_SUCCESS.value(), Pipeline.SUCCESS);
        map.put(SendSettingEnum.PIPELINE_FAILED.value(), Pipeline.FAILED);
        map.put(SendSettingEnum.PIPELINE_PASS.value(), Pipeline.PASS);
        map.put(SendSettingEnum.PIPELINE_STOP.value(), Pipeline.STOP);

    }

    SendSettingEnum(String sendSettingCode) {
        this.sendSettingCode = sendSettingCode;
    }

    public String value() {
        return this.sendSettingCode;
    }

    public static String getEventName(String sendSettingCode) {
        return map.get(sendSettingCode) == null ? "" : map.get(sendSettingCode);
    }

    private interface AppService {
        String CREATE = "创建应用服务";
        String ENABLE = "启用应用服务";
        String DISABLE = "停用应用服务";
        String DELETE = "删除应用服务";
        String CREATION_FAILURE = "创建应用服务失败";
        String CREATE_VERSION = "应用服务版本生成";
    }

    private interface ENV {
        String CREATE = "创建环境";
        String ENABLE = "启用环境";
        String DISABLE = "停用环境";
        String DELETE = "删除环境";
        String CREATEENVFAILED = "创建环境失败";
        String UPDATE_ENV_PERMISSIONS = "环境权限分配";
    }

    private interface Cluster {
        String CREATE = "创建集群";
        String ACTIVITE = "激活集群";
        String DELETE = "删除集群";
        String CREATE_FAILED = "创建集群失败";
    }

    private interface Resource {
        String INSTALL_FAILED = "组件安装失败";
        String CREATE = "资源创建";
        String CREATE_FAILED = "创建资源失败";
        String DELETE = "删除资源";
    }

    private interface GitLabCD {
        String FAILURE = "持续集成流水线失败";
        String SUCCESS = "持续集成流水线成功";
    }

    private interface Pipeline {
        String SUCCESS = "流水线执行成功";
        String FAILED = "流水线执行失败";
        String PASS = "流水线或签任务通过";
        String STOP = "流水线被终止";
    }

}
