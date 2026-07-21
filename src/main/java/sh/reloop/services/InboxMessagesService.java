package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.InboxModels.BatchMessagesParams;
import sh.reloop.models.InboxModels.ComposeMessageParams;
import sh.reloop.models.InboxModels.ForwardMessageParams;
import sh.reloop.models.InboxModels.InboxSuccessResponse;
import sh.reloop.models.InboxModels.ListMessagesParams;
import sh.reloop.models.InboxModels.ListSentMessagesParams;
import sh.reloop.models.InboxModels.Message;
import sh.reloop.models.InboxModels.MessageAttachment;
import sh.reloop.models.InboxModels.MessageRaw;
import sh.reloop.models.InboxModels.SendEmailOrPendingResponse;
import sh.reloop.models.InboxModels.SendEmailResponse;
import sh.reloop.models.InboxModels.SendMessageParams;
import sh.reloop.models.InboxModels.SetMessageReadParams;
import sh.reloop.models.InboxModels.SetMessageStarParams;
import sh.reloop.models.InboxModels.UpdateMessageParams;
import sh.reloop.validation.Validators;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Manages inbox messages. */
public class InboxMessagesService {
    private static final String MESSAGES_V1 = "/api/inbox/v1/messages";
    private static final int BATCH_IDS_MAX = 100;

    private final ReloopClient client;

    public InboxMessagesService(ReloopClient client) {
        this.client = client;
    }

    public Message[] list(ListMessagesParams params) {
        Map<String, String> query = buildListMessagesQuery(params);
        return client.request("GET", MESSAGES_V1, null, query, Message[].class);
    }

    public Message[] listSent(ListSentMessagesParams params) {
        Map<String, String> query = new LinkedHashMap<>();
        if (params != null && params.mailboxId != null) {
            query.put("mailboxId", params.mailboxId);
        }
        return client.request("GET", MESSAGES_V1 + "/sent", null, query, Message[].class);
    }

    public Message get(String id) {
        String messageId = Validators.requireMessageId(id, "id");
        return client.request("GET", MESSAGES_V1 + "/" + messageId, null, Message.class);
    }

    public Message[] batch(BatchMessagesParams params) {
        if (params == null) {
            params = new BatchMessagesParams();
        }
        List<String> ids = Validators.requireInboxIdArray(params.ids, "ids", BATCH_IDS_MAX);
        BatchMessagesParams body = new BatchMessagesParams();
        body.ids = ids;
        return client.request("POST", MESSAGES_V1 + "/batch", body, Message[].class);
    }

    public MessageRaw getRaw(String id) {
        String messageId = Validators.requireMessageId(id, "id");
        return client.request("GET", MESSAGES_V1 + "/" + messageId + "/raw", null, MessageRaw.class);
    }

    public MessageAttachment getAttachment(String id, String attachmentId) {
        String messageId = Validators.requireMessageId(id, "id");
        String attachment = Validators.requireInboxAttachmentId(attachmentId, "attachmentId");
        return client.request(
                "GET", MESSAGES_V1 + "/" + messageId + "/attachments/" + attachment, null, MessageAttachment.class);
    }

    public InboxSuccessResponse update(String id, UpdateMessageParams params) {
        String messageId = Validators.requireMessageId(id, "id");
        if (params == null
                || (params.isRead == null && params.isStarred == null && params.isSpam == null)) {
            throw new ReloopValidationException(
                    "update requires at least one of isRead, isStarred, or isSpam.", "params");
        }
        return client.request("PATCH", MESSAGES_V1 + "/" + messageId, params, InboxSuccessResponse.class);
    }

    public InboxSuccessResponse setRead(String id, SetMessageReadParams params) {
        String messageId = Validators.requireMessageId(id, "id");
        if (params == null) {
            params = new SetMessageReadParams();
        }
        SetMessageReadParams body = new SetMessageReadParams(params.isRead);
        return client.request("PATCH", MESSAGES_V1 + "/" + messageId + "/read", body, InboxSuccessResponse.class);
    }

    public InboxSuccessResponse setStar(String id, SetMessageStarParams params) {
        String messageId = Validators.requireMessageId(id, "id");
        if (params == null) {
            params = new SetMessageStarParams();
        }
        SetMessageStarParams body = new SetMessageStarParams(params.isStarred);
        return client.request("PATCH", MESSAGES_V1 + "/" + messageId + "/star", body, InboxSuccessResponse.class);
    }

    public InboxSuccessResponse delete(String id) {
        String messageId = Validators.requireMessageId(id, "id");
        return client.request("DELETE", MESSAGES_V1 + "/" + messageId, null, InboxSuccessResponse.class);
    }

    public SendEmailOrPendingResponse send(SendMessageParams params) {
        if (params == null) {
            params = new SendMessageParams();
        }
        String mailboxId = Validators.requireNonEmptyString(params.mailboxId, "mailboxId");
        Object to = Validators.requireRecipient(params.to, "to");
        String subject = Validators.requireNonEmptyString(params.subject, "subject");
        if (params.undoWindowSeconds != null) {
            Validators.requireFiniteNumber(params.undoWindowSeconds, "undoWindowSeconds");
        }

        SendMessageParams body = new SendMessageParams();
        body.mailboxId = mailboxId;
        body.to = to;
        body.subject = subject;
        body.text = params.text;
        body.html = params.html;
        body.scheduledAt = params.scheduledAt;
        body.undoWindowSeconds = params.undoWindowSeconds;
        body.attachments = params.attachments;
        if (params.cc != null) {
            body.cc = Validators.requireRecipient(params.cc, "cc");
        }
        if (params.bcc != null) {
            body.bcc = Validators.requireRecipient(params.bcc, "bcc");
        }

        return client.request("POST", MESSAGES_V1 + "/send", body, SendEmailOrPendingResponse.class);
    }

    public InboxSuccessResponse cancelPending(String id) {
        String messageId = Validators.requireMessageId(id, "id");
        return client.request(
                "POST", MESSAGES_V1 + "/pending/" + messageId + "/cancel", null, InboxSuccessResponse.class);
    }

    public SendEmailResponse reply(String id, ComposeMessageParams params) {
        String messageId = Validators.requireMessageId(id, "id");
        Map<String, Object> body = buildComposeBody(params);
        return client.request("POST", MESSAGES_V1 + "/" + messageId + "/reply", body, SendEmailResponse.class);
    }

    public SendEmailResponse replyAll(String id, ComposeMessageParams params) {
        String messageId = Validators.requireMessageId(id, "id");
        Map<String, Object> body = buildComposeBody(params);
        return client.request("POST", MESSAGES_V1 + "/" + messageId + "/reply-all", body, SendEmailResponse.class);
    }

    public SendEmailResponse forward(String id, ForwardMessageParams params) {
        String messageId = Validators.requireMessageId(id, "id");
        if (params == null) {
            params = new ForwardMessageParams();
        }
        Object to = Validators.requireRecipient(params.to, "to");
        Map<String, Object> body = buildComposeBody(params);
        body.put("to", to);
        return client.request("POST", MESSAGES_V1 + "/" + messageId + "/forward", body, SendEmailResponse.class);
    }

    private static Map<String, String> buildListMessagesQuery(ListMessagesParams params) {
        Map<String, String> query = new LinkedHashMap<>();
        if (params == null) {
            return query;
        }
        if (params.mailboxId != null) {
            query.put("mailboxId", params.mailboxId);
        }
        if (params.limit != null) {
            Validators.requireInboxLimit(params.limit, "limit");
            query.put("limit", Integer.toString(params.limit));
        }
        if (params.offset != null) {
            Validators.requireInboxOffset(params.offset, "offset");
            query.put("offset", Integer.toString(params.offset));
        }
        if (params.q != null) {
            query.put("q", params.q);
        }
        if (params.isSpam != null) {
            query.put("isSpam", Boolean.toString(params.isSpam));
        }
        return query;
    }

    private static Map<String, Object> buildComposeBody(ComposeMessageParams params) {
        if (params == null) {
            params = new ComposeMessageParams();
        }
        Validators.requireComposeBody(params.text, params.html);

        Map<String, Object> body = new LinkedHashMap<>();
        if (params.text != null) {
            body.put("text", params.text);
        }
        if (params.html != null) {
            body.put("html", params.html);
        }
        if (params.cc != null) {
            body.put("cc", Validators.requireRecipient(params.cc, "cc"));
        }
        if (params.bcc != null) {
            body.put("bcc", Validators.requireRecipient(params.bcc, "bcc"));
        }
        if (params.attachments != null) {
            body.put("attachments", params.attachments);
        }
        return body;
    }
}
