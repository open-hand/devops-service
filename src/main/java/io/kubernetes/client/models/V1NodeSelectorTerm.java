//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.kubernetes.client.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

@ApiModel(
    description = "A null or empty node selector term matches no objects. The requirements of them are ANDed. The TopologySelectorTerm type implements a subset of the NodeSelectorTerm."
)
public class V1NodeSelectorTerm {
    public static final String SERIALIZED_NAME_MATCH_EXPRESSIONS = "matchExpressions";
    @SerializedName("matchExpressions")
    private List<V1NodeSelectorRequirement> matchExpressions = null;
    public static final String SERIALIZED_NAME_MATCH_FIELDS = "matchFields";
    @SerializedName("matchFields")
    private List<V1NodeSelectorRequirement> matchFields = null;

    public V1NodeSelectorTerm() {
    }

    public V1NodeSelectorTerm matchExpressions(List<V1NodeSelectorRequirement> matchExpressions) {
        this.matchExpressions = matchExpressions;
        return this;
    }

    public V1NodeSelectorTerm addMatchExpressionsItem(V1NodeSelectorRequirement matchExpressionsItem) {
        if (this.matchExpressions == null) {
            this.matchExpressions = new ArrayList();
        }

        this.matchExpressions.add(matchExpressionsItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("A list of node selector requirements by node's labels.")
    public List<V1NodeSelectorRequirement> getMatchExpressions() {
        return this.matchExpressions;
    }

    public void setMatchExpressions(List<V1NodeSelectorRequirement> matchExpressions) {
        this.matchExpressions = matchExpressions;
    }

    public V1NodeSelectorTerm matchFields(List<V1NodeSelectorRequirement> matchFields) {
        this.matchFields = matchFields;
        return this;
    }

    public V1NodeSelectorTerm addMatchFieldsItem(V1NodeSelectorRequirement matchFieldsItem) {
        if (this.matchFields == null) {
            this.matchFields = new ArrayList<>();
        }

        this.matchFields.add(matchFieldsItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("A list of node selector requirements by node's fields.")
    public List<V1NodeSelectorRequirement> getMatchFields() {
        return this.matchFields;
    }

    public void setMatchFields(List<V1NodeSelectorRequirement> matchFields) {
        this.matchFields = matchFields;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1NodeSelectorTerm v1NodeSelectorTerm = (V1NodeSelectorTerm)o;
            return Objects.equals(this.matchExpressions, v1NodeSelectorTerm.matchExpressions) && Objects.equals(this.matchFields, v1NodeSelectorTerm.matchFields);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(new Object[]{this.matchExpressions, this.matchFields});
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1NodeSelectorTerm {\n");
        sb.append("    matchExpressions: ").append(this.toIndentedString(this.matchExpressions)).append("\n");
        sb.append("    matchFields: ").append(this.toIndentedString(this.matchFields)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
