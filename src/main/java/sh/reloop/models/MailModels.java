package sh.reloop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public final class MailModels {
    private MailModels() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SendMailTag {
        public String name;
        public String value;

        public SendMailTag() {}

        public SendMailTag(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SendMailAttachment {
        public Object content;
        public String filename;
        public String path;
        @JsonProperty("content_type")
        public String contentType;
        @JsonProperty("content_id")
        public String contentId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SendMailTemplate {
        public String id;
        public Map<String, Object> variables;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SendMailParams {
        public String from;
        public Object to;
        public String subject;
        public Object cc;
        public Object bcc;
        public String text;
        public String html;
        @JsonProperty("reply_to")
        public Object replyTo;
        @JsonProperty("scheduled_at")
        public String scheduledAt;
        public Map<String, String> headers;
        @JsonProperty("channel_id")
        public String channelId;
        public List<SendMailAttachment> attachments;
        public List<SendMailTag> tags;
        public SendMailTemplate template;
        @JsonProperty("thread_id")
        public String threadId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SendMailResponse {
        public boolean success;
        public String messageId;
        public String status;
        public String timestamp;
        public String id;
    }
}
