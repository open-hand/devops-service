package io.choerodon.devops.api.vo;

/**
 * @author: 25499
 * @date: 2019/11/5 14:31
 * @description:
 */
public class PvVO {
    private String prometheusPVC;
    private String grafanaPV;
    private String alertManagerPVC;

    public PvVO(String prometheusPVC, String grafanaPV, String alertManagerPVC) {
        this.prometheusPVC = prometheusPVC;
        this.grafanaPV = grafanaPV;
        this.alertManagerPVC = alertManagerPVC;
    }

    public PvVO() {
    }

    public String getPrometheusPVC() {
        return prometheusPVC;
    }

    public void setPrometheusPVC(String prometheusPVC) {
        this.prometheusPVC = prometheusPVC;
    }

    public String getGrafanaPV() {
        return grafanaPV;
    }

    public void setGrafanaPV(String grafanaPV) {
        this.grafanaPV = grafanaPV;
    }

    public String getAlertManagerPVC() {
        return alertManagerPVC;
    }

    public void setAlertManagerPVC(String alertManagerPVC) {
        this.alertManagerPVC = alertManagerPVC;
    }
}
