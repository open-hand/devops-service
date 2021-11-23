package io.choerodon.devops.api.vo;

/**
 * Created by Sheep on 2019/7/26.
 */
public class PipeRequestVO {

    private String podName;
    private String containerName;
    private String pipeID;
    private String namespace;
    private Boolean previous;


    public PipeRequestVO(String podName, String containerName, String pipeID, String namespace, Boolean previous) {
        this.podName = podName;
        this.containerName = containerName;
        this.pipeID = pipeID;
        this.namespace = namespace;
        this.previous = previous;
    }


    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getPipeID() {
        return pipeID;
    }

    public void setPipeID(String pipeID) {
        this.pipeID = pipeID;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Boolean getPrevious() {
        return previous;
    }

    public PipeRequestVO setPrevious(Boolean previous) {
        this.previous = previous;
        return this;
    }
}
