package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.InboxModels.BatchThreadsParams;
import sh.reloop.models.InboxModels.InboxSuccessResponse;
import sh.reloop.models.InboxModels.ListThreadsParams;
import sh.reloop.models.InboxModels.MessageAttachment;
import sh.reloop.models.InboxModels.SetThreadReadParams;
import sh.reloop.models.InboxModels.SetThreadStarParams;
import sh.reloop.models.InboxModels.Thread;
import sh.reloop.models.InboxModels.ThreadBatchResponse;
import sh.reloop.models.InboxModels.ThreadDetail;
import sh.reloop.models.InboxModels.UpdateThreadParams;
import sh.reloop.validation.Validators;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Manages inbox threads. */
public class InboxThreadsService {
    private static final String THREADS_V1 = "/api/inbox/v1/threads";
    private static final int BATCH_IDS_MAX = 100;

    private final ReloopClient client;

    public InboxThreadsService(ReloopClient client) {
        this.client = client;
    }

    public Thread[] list(ListThreadsParams params) {
        Map<String, String> query = buildListThreadsQuery(params);
        return client.request("GET", THREADS_V1, null, query, Thread[].class);
    }

    public ThreadBatchResponse batch(BatchThreadsParams params) {
        if (params == null) {
            params = new BatchThreadsParams();
        }
        List<String> ids = Validators.requireInboxIdArray(params.ids, "ids", BATCH_IDS_MAX);
        Validators.requireThreadBatchAction(params.action);
        BatchThreadsParams body = new BatchThreadsParams();
        body.ids = ids;
        body.action = params.action;
        return client.request("POST", THREADS_V1 + "/batch", body, ThreadBatchResponse.class);
    }

    public ThreadDetail get(String id) {
        String threadId = Validators.requireThreadId(id, "id");
        return client.request("GET", THREADS_V1 + "/" + threadId, null, ThreadDetail.class);
    }

    public MessageAttachment getAttachment(String id, String attachmentId) {
        String threadId = Validators.requireThreadId(id, "id");
        String attachment = Validators.requireInboxAttachmentId(attachmentId, "attachmentId");
        return client.request(
                "GET", THREADS_V1 + "/" + threadId + "/attachments/" + attachment, null, MessageAttachment.class);
    }

    public InboxSuccessResponse update(String id, UpdateThreadParams params) {
        String threadId = Validators.requireThreadId(id, "id");
        if (params == null
                || (params.isRead == null && params.isStarred == null && params.isImportant == null
                && params.isPinned == null && params.status == null)) {
            throw new ReloopValidationException(
                    "update requires at least one of isRead, isStarred, isImportant, isPinned, or status.",
                    "params");
        }
        if (params.status != null) {
            Validators.requireThreadStatus(params.status, "status");
        }
        return client.request("PATCH", THREADS_V1 + "/" + threadId, params, InboxSuccessResponse.class);
    }

    public InboxSuccessResponse setRead(String id, SetThreadReadParams params) {
        String threadId = Validators.requireThreadId(id, "id");
        if (params == null) {
            params = new SetThreadReadParams();
        }
        SetThreadReadParams body = new SetThreadReadParams(params.isRead);
        return client.request("PATCH", THREADS_V1 + "/" + threadId + "/read", body, InboxSuccessResponse.class);
    }

    public InboxSuccessResponse setStar(String id, SetThreadStarParams params) {
        String threadId = Validators.requireThreadId(id, "id");
        if (params == null) {
            params = new SetThreadStarParams();
        }
        SetThreadStarParams body = new SetThreadStarParams(params.isStarred);
        return client.request("PATCH", THREADS_V1 + "/" + threadId + "/star", body, InboxSuccessResponse.class);
    }

    public InboxSuccessResponse archive(String id) {
        String threadId = Validators.requireThreadId(id, "id");
        return client.request("POST", THREADS_V1 + "/" + threadId + "/archive", null, InboxSuccessResponse.class);
    }

    public InboxSuccessResponse trash(String id) {
        String threadId = Validators.requireThreadId(id, "id");
        return client.request("POST", THREADS_V1 + "/" + threadId + "/trash", null, InboxSuccessResponse.class);
    }

    public InboxSuccessResponse restore(String id) {
        String threadId = Validators.requireThreadId(id, "id");
        return client.request("POST", THREADS_V1 + "/" + threadId + "/restore", null, InboxSuccessResponse.class);
    }

    public InboxSuccessResponse delete(String id) {
        String threadId = Validators.requireThreadId(id, "id");
        return client.request("DELETE", THREADS_V1 + "/" + threadId, null, InboxSuccessResponse.class);
    }

    private static Map<String, String> buildListThreadsQuery(ListThreadsParams params) {
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
        if (params.folder != null) {
            query.put("folder", params.folder);
        }
        if (params.q != null) {
            query.put("q", params.q);
        }
        if (params.isPinned != null) {
            query.put("isPinned", Boolean.toString(params.isPinned));
        }
        if (params.filter != null) {
            Validators.requireThreadFilter(params.filter, "filter");
            query.put("filter", params.filter);
        }
        return query;
    }
}
