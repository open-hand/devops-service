

package com.cdancy.jenkins.rest.domain.job;

import java.util.List;

final class AutoValue_StageFlowNode extends StageFlowNode {
    private final String id;
    private final String name;
    private final String status;

    private final String parameterDescription;
    private final long startTimeMillis;
    private final long durationTimeMillis;
    private final List<Long> parentNodes;

    AutoValue_StageFlowNode(String id, String name, String status, String parameterDescription, long startTimeMillis, long durationTimeMillis, List<Long> parentNodes) {

        if (name == null) {
            throw new NullPointerException("Null name");
        } else {
            this.id = id;
            this.name = name;
            if (status == null) {
                throw new NullPointerException("Null status");
            } else {
                this.parameterDescription = parameterDescription;
                this.status = status;
                this.startTimeMillis = startTimeMillis;
                this.durationTimeMillis = durationTimeMillis;
                if (parentNodes == null) {
                    throw new NullPointerException("Null parentNodes");
                } else {
                    this.parentNodes = parentNodes;
                }
            }
        }
    }

    @Override
    public String id() {
        return id;
    }

    public String name() {
        return this.name;
    }

    public String status() {
        return this.status;
    }

    @Override
    public String parameterDescription() {
        return parameterDescription;
    }

    public long startTimeMillis() {
        return this.startTimeMillis;
    }

    public long durationTimeMillis() {
        return this.durationTimeMillis;
    }

    public List<Long> parentNodes() {
        return this.parentNodes;
    }

    public String toString() {
        return "StageFlowNode{name=" + this.name + ", status=" + this.status + ", startTimeMillis=" + this.startTimeMillis + ", durationTimeMillis=" + this.durationTimeMillis + ", parentNodes=" + this.parentNodes + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof StageFlowNode)) {
            return false;
        } else {
            StageFlowNode that = (StageFlowNode) o;
            return this.name.equals(that.name()) && this.status.equals(that.status()) && this.startTimeMillis == that.startTimeMillis() && this.durationTimeMillis == that.durationTimeMillis() && this.parentNodes.equals(that.parentNodes());
        }
    }

    public int hashCode() {
        int h$ = 1;
        h$ *= 1000003;
        h$ ^= this.name.hashCode();
        h$ *= 1000003;
        h$ ^= this.status.hashCode();
        h$ *= 1000003;
        h$ ^= (int) (this.startTimeMillis >>> 32 ^ this.startTimeMillis);
        h$ *= 1000003;
        h$ ^= (int) (this.durationTimeMillis >>> 32 ^ this.durationTimeMillis);
        h$ *= 1000003;
        h$ ^= this.parentNodes.hashCode();
        return h$;
    }
}
