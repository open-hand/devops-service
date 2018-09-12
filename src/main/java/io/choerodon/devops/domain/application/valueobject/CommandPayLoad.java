package io.choerodon.devops.domain.application.valueobject;

import  java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;

public class CommandPayLoad {
    List<DevopsEnvCommandE> devopsEnvCommandES;



    public CommandPayLoad( List<DevopsEnvCommandE> devopsEnvCommandES){

        this.devopsEnvCommandES = devopsEnvCommandES;
    }


    public List<DevopsEnvCommandE> getDevopsEnvCommandES() {
        return devopsEnvCommandES;
    }

    public void setDevopsEnvCommandES(List<DevopsEnvCommandE> devopsEnvCommandES) {
        this.devopsEnvCommandES = devopsEnvCommandES;
    }
}
