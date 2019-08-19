package io.choerodon.devops.api.vo;

/**
 * Created by Sheep on 2019/7/26.
 */
public class PipeRequestVO {

    private String podName;
    private String containerName;
    private String pipeID;
    private String namespace;


    public PipeRequestVO(String podName, String containerName, String pipeID, String namespace) {
        this.podName = podName;
        this.containerName = containerName;
        this.pipeID = pipeID;
        this.namespace = namespace;
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
}
