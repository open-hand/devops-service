package io.choerodon.devops.infra.feign.operator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;

import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import retrofit2.Response;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.sonar.QualityGate;
import io.choerodon.devops.api.vo.sonar.QualityGateCondition;
import io.choerodon.devops.api.vo.sonar.SonarComponent;
import io.choerodon.devops.api.vo.sonar.SonarProjectSearchPageResult;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.util.JsonHelper;
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

    public void deleteQualityGate(String name) {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        RetrofitCallExceptionParse.executeCall(sonarClient.deleteQualityGate(data), ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_DELETE, Void.class);
    }

    public QualityGate gateShow(String name) {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        try {
            Response<ResponseBody> execute = sonarClient.gateShow(data).execute();
            if (execute == null) {
                throw new CommonException(ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_SHOW_GET, "");
            }
            if (execute.raw().code() == 404) {
                return null;
            } else {
                if (!execute.isSuccessful()) {
                    Optional.ofNullable(execute.errorBody()).ifPresent(v -> {
                        throw new CommonException(ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_SHOW_GET, execute.errorBody().toString());
                    });
                    throw new CommonException(ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_SHOW_GET, execute.raw().code());
                }
                if (ObjectUtils.isEmpty(execute.body())) {
                    throw new CommonException(ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_SHOW_GET, "");
                }
                return JsonHelper.unmarshalByJackson(execute.body().string(), QualityGate.class);
            }
        } catch (Exception e) {
            throw new CommonException(ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_SHOW_GET, e.getMessage());
        }
    }

    public SonarProjectSearchPageResult searchProjects(String sonarProjectKey) {
        Map<String, String> data = new HashMap<>();
        data.put("ps", "500");
        data.put("q", sonarProjectKey);
        return RetrofitCallExceptionParse.executeCall(sonarClient.searchProjects(data), ExceptionConstants.SonarCode.DEVOPS_SONAR_PROJECTS_SEARCH, SonarProjectSearchPageResult.class);
    }

    public void createProject(String code, String sonarProjectKey) {
        Map<String, String> data = new HashMap<>();
        data.put("project", sonarProjectKey);
        data.put("name", code);
        RetrofitCallExceptionParse.executeCall(sonarClient.createProject(data), ExceptionConstants.SonarCode.DEVOPS_SONAR_PROJECTS_SEARCH, Void.class);
    }

    public SonarComponent getSonarQualityGateResultDetail(String sonarProjectKey) {
        //初始化查询参数
        Map<String, String> queryContentMap = new HashMap<>();
        queryContentMap.put("additionalFields", "metrics,periods");
        queryContentMap.put("component", sonarProjectKey);
        queryContentMap.put("metricKeys", "quality_gate_details");

        //根据project-key查询sonarqube项目内容
        return RetrofitCallExceptionParse.executeCall(sonarClient.getSonarQualityGateResultDetail(queryContentMap), ExceptionConstants.SonarCode.Devops_SONAR_QUALITY_GATE_DETAILS_GET, SonarComponent.class);
    }

    public void bindQualityGate(String gateId, String sonarProjectKey) {
        Map<String, String> data = new HashMap<>();
        data.put("gateId", gateId);
        data.put("projectKey", sonarProjectKey);
        RetrofitCallExceptionParse.executeCall(sonarClient.bindQualityGate(data), ExceptionConstants.SonarCode.DEVOPS_SONAR_QUALITY_GATE_BIND, SonarComponent.class);
    }
}
