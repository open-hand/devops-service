package io.choerodon.devops.domain.application.valueobject;

public class Event {
    private Metadata metadata;
    private InvolvedObject involvedObject;
    private String message;

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public InvolvedObject getInvolvedObject() {
        return involvedObject;
    }

    public void setInvolvedObject(InvolvedObject involvedObject) {
        this.involvedObject = involvedObject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}