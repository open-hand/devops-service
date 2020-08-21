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
    description = "A node selector requirement is a selector that contains values, a key, and an operator that relates the key and values."
)
public class V1NodeSelectorRequirement {
    public static final String SERIALIZED_NAME_KEY = "key";
    @SerializedName("key")
    private String key;
    public static final String SERIALIZED_NAME_OPERATOR = "operator";
    @SerializedName("operator")
    private String operator;
    public static final String SERIALIZED_NAME_VALUES = "values";
    @SerializedName("values")
    private List<String> values = null;

    public V1NodeSelectorRequirement() {
    }

    public V1NodeSelectorRequirement key(String key) {
        this.key = key;
        return this;
    }

    @ApiModelProperty(
        required = true,
        value = "The label key that the selector applies to."
    )
    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public V1NodeSelectorRequirement operator(String operator) {
        this.operator = operator;
        return this;
    }

    @ApiModelProperty(
        required = true,
        value = "Represents a key's relationship to a set of values. Valid operators are In, NotIn, Exists, DoesNotExist. Gt, and Lt."
    )
    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public V1NodeSelectorRequirement values(List<String> values) {
        this.values = values;
        return this;
    }

    public V1NodeSelectorRequirement addValuesItem(String valuesItem) {
        if (this.values == null) {
            this.values = new ArrayList();
        }

        this.values.add(valuesItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("An array of string values. If the operator is In or NotIn, the values array must be non-empty. If the operator is Exists or DoesNotExist, the values array must be empty. If the operator is Gt or Lt, the values array must have a single element, which will be interpreted as an integer. This array is replaced during a strategic merge patch.")
    public List<String> getValues() {
        return this.values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1NodeSelectorRequirement v1NodeSelectorRequirement = (V1NodeSelectorRequirement)o;
            return Objects.equals(this.key, v1NodeSelectorRequirement.key) && Objects.equals(this.operator, v1NodeSelectorRequirement.operator) && Objects.equals(this.values, v1NodeSelectorRequirement.values);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.key, this.operator, this.values});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1NodeSelectorRequirement {\n");
        sb.append("    key: ").append(this.toIndentedString(this.key)).append("\n");
        sb.append("    operator: ").append(this.toIndentedString(this.operator)).append("\n");
        sb.append("    values: ").append(this.toIndentedString(this.values)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
