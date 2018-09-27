package io.choerodon.devops.api.dto;

import java.util.List;

public class PipelineFrequencyDTO {

    private List<String> createDates;
    private List<Long> pipelineSuccessFrequency;
    private List<Long> pipelineFailFrequency;
    private List<Long> pipelineFrequencys;

    public List<String> getCreateDates() {
        return createDates;
    }

    public void setCreateDates(List<String> createDates) {
        this.createDates = createDates;
    }

    public List<Long> getPipelineSuccessFrequency() {
        return pipelineSuccessFrequency;
    }

    public void setPipelineSuccessFrequency(List<Long> pipelineSuccessFrequency) {
        this.pipelineSuccessFrequency = pipelineSuccessFrequency;
    }

    public List<Long> getPipelineFailFrequency() {
        return pipelineFailFrequency;
    }

    public void setPipelineFailFrequency(List<Long> pipelineFailFrequency) {
        this.pipelineFailFrequency = pipelineFailFrequency;
    }

    public List<Long> getPipelineFrequencys() {
        return pipelineFrequencys;
    }

    public void setPipelineFrequencys(List<Long> pipelineFrequencys) {
        this.pipelineFrequencys = pipelineFrequencys;
    }
}
