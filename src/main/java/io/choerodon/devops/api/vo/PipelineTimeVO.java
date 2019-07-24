package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

public class PipelineTimeVO {
    List<String> pipelineTime;
    List<String> refs;
    List<String> versions;
    List<Date> createDates;


    public List<String> getPipelineTime() {
        return pipelineTime;
    }

    public void setPipelineTime(List<String> pipelineTime) {
        this.pipelineTime = pipelineTime;
    }

    public List<String> getRefs() {
        return refs;
    }

    public void setRefs(List<String> refs) {
        this.refs = refs;
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public List<Date> getCreateDates() {
        return createDates;
    }

    public void setCreateDates(List<Date> createDates) {
        this.createDates = createDates;
    }
}
