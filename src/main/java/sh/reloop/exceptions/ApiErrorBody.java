package sh.reloop.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Decoded Reloop API error payload. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiErrorBody {
    public String message;
    public String why;
    public String tip;
    public String link;
}
