package io.choerodon.devops.infra.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import io.choerodon.core.swagger.ChoerodonRouteData;
import io.choerodon.swagger.annotation.ChoerodonExtraData;
import io.choerodon.swagger.swagger.extra.ExtraData;
import io.choerodon.swagger.swagger.extra.ExtraDataManager;

/**
 * 建议项目上，每个服务自定义一个 ExtraDataManager 的实现类，使用配置的形式注入服务配置，便于开发人员本地开发。
 *
 * name：表示路由唯一ID，一般用服务简码表示
 * path：表示路由前缀，也可用服务简码表示，前端调用服务API时需加上路由前缀
 * serviceId：服务名称，网关通过路由前缀匹配到服务后，将基于Ribbon请求该服务
 * packages: 指定扫描API的包，多个可用逗号分隔，可为空
 *
 * @author zmf
 * @since 20-5-18
 */
@ChoerodonExtraData
public class DevOpsExtraDataManager implements ExtraDataManager {
    @Autowired
    private org.springframework.core.env.Environment environment;

    @Override
    public ExtraData getData() {
        ChoerodonRouteData choerodonRouteData = new ChoerodonRouteData();
        choerodonRouteData.setName(environment.getProperty("hzero.service.current.name", "devops"));
        choerodonRouteData.setPath(environment.getProperty("hzero.service.current.path", "/devops/**"));
        choerodonRouteData.setServiceId(environment.getProperty("hzero.service.current.service-name", "devops-service"));
        choerodonRouteData.setPackages("io.choerodon.devops");
        extraData.put(ExtraData.ZUUL_ROUTE_DATA, choerodonRouteData);
        return extraData;
    }
}
