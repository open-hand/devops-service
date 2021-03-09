package io.choerodon.devops.api.vo.pipeline;

import java.util.List;


/**
 * Created by wangxiang on 2021/3/5
 */
public class PipelineExecuteVO {
    private String pipelineName;
    private List<ExecuteDetailVO> executeDetailVOS;

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public List<ExecuteDetailVO> getExecuteDetailVOS() {
        return executeDetailVOS;
    }

    public void setExecuteDetailVOS(List<ExecuteDetailVO> executeDetailVOS) {
        this.executeDetailVOS = executeDetailVOS;
    }
}
