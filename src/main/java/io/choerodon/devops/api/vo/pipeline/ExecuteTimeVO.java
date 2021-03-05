package io.choerodon.devops.api.vo.pipeline;

import java.util.Date;
import java.util.List;


/**
 * Created by wangxiang on 2021/3/5
 */
public class ExecuteTimeVO {
    private List<Date> creationDates;
    private List<PipelineExecuteVO> pipelineExecuteVOS;

    public List<Date> getCreationDates() {
        return creationDates;
    }

    public void setCreationDates(List<Date> creationDates) {
        this.creationDates = creationDates;
    }

    public List<PipelineExecuteVO> getPipelineExecuteVOS() {
        return pipelineExecuteVOS;
    }

    public void setPipelineExecuteVOS(List<PipelineExecuteVO> pipelineExecuteVOS) {
        this.pipelineExecuteVOS = pipelineExecuteVOS;
    }
}
