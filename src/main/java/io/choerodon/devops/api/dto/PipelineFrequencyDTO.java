package io.choerodon.devops.api.dto;

import java.util.List;

public class PipelineFrequencyDTO {

    List<String> createDates;
    List<Long> pipelineSuccessFrequency;
    List<Long> pipelineFailFrequency;
    List<Long> PipelineFrequencys;

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
        return PipelineFrequencys;
    }

    public void setPipelineFrequencys(List<Long> pipelineFrequencys) {
        PipelineFrequencys = pipelineFrequencys;
    }
}
