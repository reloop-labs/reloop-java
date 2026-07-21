package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.InboxModels.CreateMailboxParams;
import sh.reloop.models.InboxModels.CreateMailboxResponse;
import sh.reloop.models.InboxModels.InboxSuccessResponse;
import sh.reloop.models.InboxModels.Mailbox;
import sh.reloop.models.InboxModels.MailboxDetail;
import sh.reloop.models.InboxModels.UpdateMailboxParams;
import sh.reloop.validation.Validators;

/** Manages inbox mailboxes. */
public class InboxMailboxesService {
    private static final String MAILBOXES_V1 = "/api/inbox/v1/mailboxes";

    private final ReloopClient client;

    public InboxMailboxesService(ReloopClient client) {
        this.client = client;
    }

    public Mailbox[] list() {
        return client.request("GET", MAILBOXES_V1 + "/list", null, Mailbox[].class);
    }

    public MailboxDetail get(String id) {
        String mailboxId = Validators.requireMailboxId(id, "id");
        return client.request("GET", MAILBOXES_V1 + "/" + mailboxId, null, MailboxDetail.class);
    }

    public CreateMailboxResponse create(CreateMailboxParams params) {
        if (params == null) {
            params = new CreateMailboxParams();
        }
        String domainId = Validators.requireNonEmptyString(params.domainId, "domainId");
        String email = Validators.requireNonEmptyString(params.email, "email");

        CreateMailboxParams body = new CreateMailboxParams();
        body.domainId = domainId;
        body.email = email;
        body.password = params.password;
        body.quota = params.quota;
        body.displayName = params.displayName;

        return client.request("POST", MAILBOXES_V1 + "/create", body, CreateMailboxResponse.class);
    }

    public InboxSuccessResponse update(String id, UpdateMailboxParams params) {
        String mailboxId = Validators.requireMailboxId(id, "id");
        if (params == null
                || (params.displayName == null && params.status == null && params.quota == null)) {
            throw new ReloopValidationException(
                    "update requires at least one of displayName, status, or quota.", "params");
        }
        if (params.status != null) {
            Validators.requireMailboxStatus(params.status, "status");
        }
        return client.request("PATCH", MAILBOXES_V1 + "/" + mailboxId, params, InboxSuccessResponse.class);
    }

    public InboxSuccessResponse delete(String id) {
        String mailboxId = Validators.requireMailboxId(id, "id");
        return client.request("DELETE", MAILBOXES_V1 + "/" + mailboxId, null, InboxSuccessResponse.class);
    }
}
