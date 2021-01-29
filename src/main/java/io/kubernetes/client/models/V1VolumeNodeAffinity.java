//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.kubernetes.client.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nullable;
import java.util.Objects;

@ApiModel(
        description = "VolumeNodeAffinity defines constraints that limit what nodes this volume can be accessed from."
)
public class V1VolumeNodeAffinity {
    public static final String SERIALIZED_NAME_REQUIRED = "required";
    @SerializedName("required")
    private V1NodeSelector required;

    public V1VolumeNodeAffinity() {
    }

    public V1VolumeNodeAffinity required(V1NodeSelector required) {
        this.required = required;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1NodeSelector getRequired() {
        return this.required;
    }

    public void setRequired(V1NodeSelector required) {
        this.required = required;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1VolumeNodeAffinity v1VolumeNodeAffinity = (V1VolumeNodeAffinity) o;
            return Objects.equals(this.required, v1VolumeNodeAffinity.required);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(new Object[]{this.required});
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1VolumeNodeAffinity {\n");
        sb.append("    required: ").append(this.toIndentedString(this.required)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
