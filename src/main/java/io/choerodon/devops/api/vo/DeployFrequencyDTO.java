package io.choerodon.devops.api.vo;

import java.util.List;

public class DeployFrequencyDTO {


    private List<String> creationDates;
    private List<Long> deployFrequencys;
    private List<Long> deploySuccessFrequency;
    private List<Long> deployFailFrequency;

    public List<String> getCreationDates() {
        return creationDates;
    }

    public void setCreationDates(List<String> creationDates) {
        this.creationDates = creationDates;
    }

    public List<Long> getDeployFrequencys() {
        return deployFrequencys;
    }

    public void setDeployFrequencys(List<Long> deployFrequencys) {
        this.deployFrequencys = deployFrequencys;
    }

    public List<Long> getDeploySuccessFrequency() {
        return deploySuccessFrequency;
    }

    public void setDeploySuccessFrequency(List<Long> deploySuccessFrequency) {
        this.deploySuccessFrequency = deploySuccessFrequency;
    }

    public List<Long> getDeployFailFrequency() {
        return deployFailFrequency;
    }

    public void setDeployFailFrequency(List<Long> deployFailFrequency) {
        this.deployFailFrequency = deployFailFrequency;
    }
}
