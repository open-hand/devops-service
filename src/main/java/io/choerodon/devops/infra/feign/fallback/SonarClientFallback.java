package io.choerodon.devops.infra.feign.fallback;

import java.util.Map;

import io.choerodon.devops.api.dto.sonar.Bug;
import io.choerodon.devops.api.dto.sonar.SonarAnalyses;
import io.choerodon.devops.api.dto.sonar.SonarComponent;
import io.choerodon.devops.api.dto.sonar.SonarTables;
import io.choerodon.devops.api.dto.sonar.Vulnerability;
import io.choerodon.devops.infra.feign.SonarClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

/**
 * Created by Sheep on 2019/5/6.
 */
public class SonarClientFallback implements SonarClient {

    @Override
    public Call<SonarComponent> getSonarComponet(Map<String, String> maps) {
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
    public void updateVisibility(Map<String, String> maps) {}

    @Override
    public void updateDefaultVisibility(Map<String, String> maps){}

    @Override
    public void addGroupToTemplate(Map<String, String> maps) {}

    @Override
    public void removeGroupFromTemplate(Map<String, String> maps) {}
}
