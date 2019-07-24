package io.choerodon.devops.api.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Creator: Runge
 * Date: 2018/4/18
 * Time: 20:59
 * Description:
 */
public class EnvInstanceVO {
    private Long envId;
    private List<EnvVersionVO> envVersions;

    public EnvInstanceVO() {
    }

    public EnvInstanceVO(Long envId) {
        this.envId = envId;
        this.envVersions = new ArrayList<>();
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public List<EnvVersionVO> getEnvVersions() {
        return envVersions;
    }

    public void setEnvVersions(List<EnvVersionVO> envVersions) {
        this.envVersions = envVersions;
    }

    public EnvVersionVO queryLastEnvVersionVO() {
        return envVersions.get(envVersions.size() - 1);
    }

    public void addEnvVersionDTOS(EnvVersionVO envVersionVO) {
        this.envVersions.add(envVersionVO);
    }
}
