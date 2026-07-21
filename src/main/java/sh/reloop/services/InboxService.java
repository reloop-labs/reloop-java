package sh.reloop.services;

import sh.reloop.ReloopClient;

/** Inbox mailboxes, messages, and threads. */
public class InboxService {
    public final InboxMailboxesService mailboxes;
    public final InboxMessagesService messages;
    public final InboxThreadsService threads;

    public InboxService(ReloopClient client) {
        this.mailboxes = new InboxMailboxesService(client);
        this.messages = new InboxMessagesService(client);
        this.threads = new InboxThreadsService(client);
    }
}
