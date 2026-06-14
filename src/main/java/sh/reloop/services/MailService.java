package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.models.Models.SendMailResponse;

import java.util.Map;

public class MailService {
    private final ReloopClient client;

    public MailService(ReloopClient client) {
        this.client = client;
    }

    public SendMailResponse send(Map<String, Object> parameters) {
        return client.fetch("POST", "/api/mail/v1/send", parameters, SendMailResponse.class);
    }
}
