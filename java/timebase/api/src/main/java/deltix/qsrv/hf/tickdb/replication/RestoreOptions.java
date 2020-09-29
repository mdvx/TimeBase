package deltix.qsrv.hf.tickdb.replication;

import deltix.timebase.messages.IdentityKey;

/**
 *
 */
public class RestoreOptions extends CommonOptions {

    public RestoreOptions() {
    }

    public RestoreOptions(String name) {
        this.name = name;
    }

    public String name; // stream name
}
