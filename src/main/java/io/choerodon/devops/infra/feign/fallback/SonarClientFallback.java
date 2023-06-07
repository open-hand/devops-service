package io.choerodon.devops.infra.feign.fallback;

import java.util.Map;

import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.http.QueryMap;

import io.choerodon.devops.api.vo.sonar.*;
import io.choerodon.devops.infra.feign.SonarClient;

/**
 * Created by Sheep on 2019/5/6.
 */
@Component
public class SonarClientFallback implements SonarClient {

    @Override
    public Call<SonarComponent> getSonarComponent(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<SonarComponent> getSonarAnalysisDate(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<SonarAnalyses> getAnalyses(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Bug> getBugs(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Vulnerability> getVulnerability(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Bug> getNewBugs(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Vulnerability> getNewVulnerability(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<SonarTables> getSonarTables(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Void> updateVisibility(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Void> updateDefaultVisibility(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Void> addGroupToTemplate(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Void> removeGroupFromTemplate(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Projects> listProject() {
        return null;
    }

    @Override
    public Call<Void> getUser() {
        return null;
    }

    @Override
    public Call<ResponseBody> createToken(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<Void> revokeToken(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<ResponseBody> listToken(@QueryMap Map<String, String> data) {
        return null;
    }

    @Override
    public Call<ResponseBody> batchQueryMeasures(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<ResponseBody> createQualityGate(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<ResponseBody> createQualityGateCondition(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<ResponseBody> deleteQualityGateCondition(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<ResponseBody> deleteQualityGate(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<ResponseBody> searchProjects(Map<String, String> data) {
        return null;
    }

    @Override
    public Call<ResponseBody> createProject(Map<String, String> data) {
        return null;
    }

    @Override
    public Call<ResponseBody> listMeasures(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<ResponseBody> gateShow(Map<String, String> data) {
        return null;
    }

    @Override
    public Call<ResponseBody> bindQualityGate(Map<String, String> data) {
        return null;
    }

    @Override
    public Call<ResponseBody> getUser(Map<String, String> data) {
        return null;
    }

    @Override
    public Call<ResponseBody> createUser(Map<String, Object> data) {
        return null;
    }

    @Override
    public Call<ResponseBody> addUserPermission(Map<String, Object> data) {
        return null;
    }

    @Override
    public Call<ResponseBody> listIssue(Map<String, String> maps) {
        return null;
    }

    @Override
    public Call<ResponseBody> listWebhooks() {
        return null;
    }

    @Override
    public Call<ResponseBody> updateWebhook(Map<String, Object> data) {
        return null;
    }

    @Override
    public Call<ResponseBody> createWebhook(Map<String, Object> data) {
        return null;
    }
}
