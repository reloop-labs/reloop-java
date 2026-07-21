package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.models.MailModels.SendMailParams;
import sh.reloop.models.MailModels.SendMailResponse;
import sh.reloop.validation.Validators;

/** Sends transactional email. */
public class MailService {
    private final ReloopClient client;

    public MailService(ReloopClient client) {
        this.client = client;
    }

    public SendMailResponse send(SendMailParams params) {
        if (params == null) {
            params = new SendMailParams();
        }
        String from = Validators.requireMailString(params.from, "from");
        Object to = Validators.requireRecipient(params.to, "to");
        String subject = Validators.requireMailString(params.subject, "subject");

        SendMailParams body = new SendMailParams();
        body.from = from;
        body.to = to;
        body.subject = subject;
        body.cc = params.cc;
        body.bcc = params.bcc;
        body.text = params.text;
        body.html = params.html;
        body.replyTo = params.replyTo;
        body.scheduledAt = params.scheduledAt;
        body.headers = params.headers;
        body.channelId = params.channelId;
        body.attachments = params.attachments;
        body.tags = params.tags;
        body.template = params.template;
        body.threadId = params.threadId;

        return client.request("POST", "/api/mail/v1/send", body, SendMailResponse.class);
    }
}
