//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.kubernetes.client.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

@ApiModel(
    description = "PodDNSConfigOption defines DNS resolver options of a pod."
)
public class V1PodDNSConfigOption {
    public static final String SERIALIZED_NAME_NAME = "name";
    @SerializedName("name")
    private String name;
    public static final String SERIALIZED_NAME_VALUE = "value";
    @SerializedName("value")
    private String value;

    public V1PodDNSConfigOption() {
    }

    public V1PodDNSConfigOption name(String name) {
        this.name = name;
        return this;
    }

    @Nullable
    @ApiModelProperty("Required.")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public V1PodDNSConfigOption value(String value) {
        this.value = value;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1PodDNSConfigOption v1PodDNSConfigOption = (V1PodDNSConfigOption)o;
            return Objects.equals(this.name, v1PodDNSConfigOption.name) && Objects.equals(this.value, v1PodDNSConfigOption.value);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name, this.value});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1PodDNSConfigOption {\n");
        sb.append("    name: ").append(this.toIndentedString(this.name)).append("\n");
        sb.append("    value: ").append(this.toIndentedString(this.value)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
