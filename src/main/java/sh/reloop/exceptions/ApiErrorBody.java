package sh.reloop.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Decoded Reloop API error payload. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiErrorBody {
    public String message;
    public String why;
    /** How to resolve the error. The API returns this as "fix". */
    public String fix;
    public String link;
    /** @deprecated Use {@link #fix}. Mirrors fix; also filled from a legacy "tip" field. */
    @Deprecated
    public String tip;

    @com.fasterxml.jackson.annotation.JsonSetter("fix")
    void setFix(String value) {
        this.fix = value;
        this.tip = value;
    }

    @com.fasterxml.jackson.annotation.JsonSetter("tip")
    void setTip(String value) {
        if (this.fix == null) {
            this.fix = value;
        }
        this.tip = this.fix;
    }
}
