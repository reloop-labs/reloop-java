package sh.reloop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public final class InboxModels {
    private InboxModels() {}

    public static final class MailboxStatus {
        public static final String ACTIVE = "active";
        public static final String DISABLED = "disabled";

        private MailboxStatus() {}
    }

    public static final class ThreadStatus {
        public static final String ACTIVE = "active";
        public static final String ARCHIVED = "archived";
        public static final String CLOSED = "closed";
        public static final String TRASH = "trash";

        private ThreadStatus() {}
    }

    public static final class ThreadFilter {
        public static final String PRIMARY = "primary";
        public static final String ALERTS = "alerts";
        public static final String PERSON = "person";
        public static final String TAG = "tag";

        private ThreadFilter() {}
    }

    public static final class ThreadBatchAction {
        public static final String ARCHIVE = "archive";
        public static final String TRASH = "trash";
        public static final String RESTORE = "restore";
        public static final String STAR = "star";
        public static final String UNSTAR = "unstar";
        public static final String READ = "read";
        public static final String UNREAD = "unread";
        public static final String IMPORTANT = "important";
        public static final String UNIMPORTANT = "unimportant";
        public static final String SPAM = "spam";
        public static final String UNSPAM = "unspam";
        public static final String PIN = "pin";
        public static final String UNPIN = "unpin";

        private ThreadBatchAction() {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InboxSuccessResponse {
        public boolean success;
        public String id;
        public String message;
        public Boolean isRead;
        public Boolean isStarred;
        public Boolean isSpam;
        public Boolean isImportant;
        public Boolean isPinned;
        public String status;
        public String deletedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SendEmailResponse {
        public boolean success;
        public String messageId;
        public String status;
        public String timestamp;
        public String id;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SendEmailOrPendingResponse {
        public boolean success;
        public String messageId;
        public String status;
        public String timestamp;
        public String id;
        public Boolean pending;
        public String sendAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThreadBatchResponse {
        public boolean success;
        public List<String> ids;
        public String action;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageAttachment {
        public String id;
        public String filename;
        public String contentType;
        public int size;
        public String storagePath;
        public String contentDisposition;
        public String contentId;
        public String createdAt;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AttachmentInput {
        public String content;
        public String filename;
        public String path;
        @JsonProperty("content_type")
        public String contentType;
        @JsonProperty("content_id")
        public String contentId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Mailbox {
        public String id;
        public String email;
        public String quota;
        public String status;
        public String displayName;
        public String createdAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MailboxDetail extends Mailbox {
        public String domainId;
        public String updatedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateMailboxResponse {
        public String id;
        public String email;
        public String status;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateMailboxParams {
        public String domainId;
        public String email;
        public String password;
        public String quota;
        public String displayName;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateMailboxParams {
        public String displayName;
        public String status;
        public String quota;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageAttachmentItem {
        public String id;
        public String inboundEmailId;
        public String filename;
        public String contentType;
        public int size;
        public String storagePath;
        public String contentDisposition;
        public String contentId;
        public String createdAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        public String id;
        public String mailboxId;
        public String organizationId;
        public String fromEmail;
        public String fromName;
        public List<String> toEmails;
        public List<String> ccEmails;
        public List<String> bccEmails;
        public String replyTo;
        public String subject;
        public String textBody;
        public String htmlBody;
        public String snippet;
        public int size;
        public String status;
        public boolean isRead;
        public boolean isStarred;
        public boolean isSpam;
        public Double spamScore;
        public String messageId;
        public String threadId;
        public String inReplyTo;
        public List<String> references;
        public Map<String, String> headers;
        public String date;
        public String createdAt;
        public List<MessageAttachmentItem> attachments;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageRaw {
        public String id;
        public String messageId;
        public String raw;
    }

    public static class ListMessagesParams {
        public String mailboxId;
        public Integer limit;
        public Integer offset;
        public String q;
        public Boolean isSpam;
    }

    public static class ListSentMessagesParams {
        public String mailboxId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BatchMessagesParams {
        public List<String> ids;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateMessageParams {
        public Boolean isRead;
        public Boolean isStarred;
        public Boolean isSpam;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SetMessageReadParams {
        public boolean isRead;

        public SetMessageReadParams() {}

        public SetMessageReadParams(boolean isRead) {
            this.isRead = isRead;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SetMessageStarParams {
        public boolean isStarred;

        public SetMessageStarParams() {}

        public SetMessageStarParams(boolean isStarred) {
            this.isStarred = isStarred;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SendMessageParams {
        public String mailboxId;
        public Object to;
        public String subject;
        public String text;
        public String html;
        public Object cc;
        public Object bcc;
        public List<AttachmentInput> attachments;
        public String scheduledAt;
        public Double undoWindowSeconds;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ComposeMessageParams {
        public String text;
        public String html;
        public Object cc;
        public Object bcc;
        public List<AttachmentInput> attachments;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ForwardMessageParams extends ComposeMessageParams {
        public Object to;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThreadLabel {
        public String id;
        public String name;
        public String color;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Thread {
        public String id;
        public String mailboxId;
        public String organizationId;
        public String subject;
        public String lastMessagePreview;
        public String lastMessageAt;
        public String status;
        public int messageCount;
        public List<String> participants;
        public boolean isRead;
        public boolean isStarred;
        public Boolean isImportant;
        public Boolean isPinned;
        public String pinnedAt;
        public List<ThreadLabel> labels;
        public String deletedAt;
        public String createdAt;
        public String updatedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThreadMessage {
        public String id;
        public String threadId;
        public String direction;
        public String inboundEmailId;
        public String emailLogId;
        public String fromEmail;
        public String fromName;
        public String subject;
        public String preview;
        public String messageAt;
        public String rfc822MessageId;
        public String inReplyTo;
        public String createdAt;
        public Object email;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThreadDetail extends Thread {
        public List<ThreadMessage> messages;
    }

    public static class ListThreadsParams {
        public String mailboxId;
        public Integer limit;
        public Integer offset;
        public String folder;
        public String q;
        public Boolean isPinned;
        public String filter;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BatchThreadsParams {
        public List<String> ids;
        public String action;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateThreadParams {
        public Boolean isRead;
        public Boolean isStarred;
        public Boolean isImportant;
        public Boolean isPinned;
        public String status;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SetThreadReadParams {
        public boolean isRead;

        public SetThreadReadParams() {}

        public SetThreadReadParams(boolean isRead) {
            this.isRead = isRead;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SetThreadStarParams {
        public boolean isStarred;

        public SetThreadStarParams() {}

        public SetThreadStarParams(boolean isStarred) {
            this.isStarred = isStarred;
        }
    }
}
