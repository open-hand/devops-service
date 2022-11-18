package io.choerodon.devops.infra.feign.operator;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.devops.api.vo.sonar.QualityGate;
import io.choerodon.devops.api.vo.sonar.QualityGateCondition;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.util.RetrofitCallExceptionParse;

@Component
public class SonarClientOperator {

    @Value("${services.sonarqube.url:}")
    private String sonarqubeUrl;
    @Value("${services.sonarqube.username:}")
    private String userName;
    @Value("${services.sonarqube.password:}")
    private String password;

    private static final String SONAR = "sonar";

    private static SonarClient sonarClient;

    @PostConstruct
    public void init() {
        sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
    }


    public QualityGate createQualityGate(String name) {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        return RetrofitCallExceptionParse.executeCall(sonarClient.createQualityGate(data), ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_CREATE, QualityGate.class);
    }

    public QualityGateCondition createQualityGateCondition(String gateId, String metric, String op, String error) {
        Map<String, String> data = new HashMap<>();
        data.put("gateId", gateId);
        data.put("metric", metric);
        data.put("op", op);
        data.put("error", error);
        return RetrofitCallExceptionParse.executeCall(sonarClient.createQualityGateCondition(data), ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_CONDITION_CREATE, QualityGateCondition.class);
    }

    public void deleteQualityGateCondition(String id) {
        Map<String, String> data = new HashMap<>();
        data.put("id", id);
        RetrofitCallExceptionParse.executeCall(sonarClient.deleteQualityGateCondition(data), ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_CONDITION_DELETE, Void.class);
    }

    public void deleteQualityGate(String id) {
        Map<String, String> data = new HashMap<>();
        data.put("id", id);
        RetrofitCallExceptionParse.executeCall(sonarClient.deleteQualityGate(data), ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_DELETE, Void.class);
    }
}
