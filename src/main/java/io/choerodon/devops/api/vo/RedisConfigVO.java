package io.choerodon.devops.api.vo;

import java.util.List;
import java.util.Map;

/**
 * Created by wangxiang on 2021/3/24
 */
public class RedisConfigVO {


    /**
     * redis 给默认值，可不填  usePassword
     */
    private String pass;
    /**
     * 启用内核参数修改 必选
     */
    private Boolean sysctlImage;

    private Map<String, String> configMap;

    /**
     * 非必选，可填可选此环境中的
     */
    private String pvcName;

    /**
     * 默认为3，最小为3
     */
    private Integer slaveCount;

    private List<String> pvLabels;

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public Boolean getSysctlImage() {
        return sysctlImage;
    }

    public void setSysctlImage(Boolean sysctlImage) {
        this.sysctlImage = sysctlImage;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public String getPvcName() {
        return pvcName;
    }

    public void setPvcName(String pvcName) {
        this.pvcName = pvcName;
    }

    public Integer getSlaveCount() {
        return slaveCount;
    }

    public void setSlaveCount(Integer slaveCount) {
        this.slaveCount = slaveCount;
    }

    public List<String> getPvLabels() {
        return pvLabels;
    }

    public void setPvLabels(List<String> pvLabels) {
        this.pvLabels = pvLabels;
    }
}
