package io.choerodon.devops.api.vo;

/**
 * Created by Sheep on 2019/7/26.
 */
public class PipeRequestVO {

    private String podName;
    private String containerName;
    private String pipeID;
    private String namespace;
    private String instanceId;
    private Boolean previous;
    private Boolean downloadLog;


    public PipeRequestVO(String pipeID, Boolean downloadLog) {
        this.pipeID = pipeID;
        this.downloadLog = downloadLog;
    }

    public PipeRequestVO(String podName, String containerName, String pipeID, String namespace, String instanceId, Boolean previous) {
        this.podName = podName;
        this.containerName = containerName;
        this.pipeID = pipeID;
        this.namespace = namespace;
        this.instanceId = instanceId;
        this.previous = previous;
    }

    public Boolean getDownloadLog() {
        return downloadLog;
    }

    public void setDownloadLog(Boolean downloadLog) {
        this.downloadLog = downloadLog;
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

    public void setPrevious(Boolean previous) {
        this.previous = previous;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
