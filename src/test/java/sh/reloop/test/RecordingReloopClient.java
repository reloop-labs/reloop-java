package sh.reloop.test;

import sh.reloop.ReloopClient;

import java.util.HashMap;
import java.util.Map;

public class RecordingReloopClient extends ReloopClient {
    public String lastMethod;
    public String lastPath;
    public Object lastBody;

    public RecordingReloopClient() {
        super("rl_test", "https://reloop.sh");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fetch(String method, String path, Object body, Class<T> responseType) {
        this.lastMethod = method;
        this.lastPath = path;
        this.lastBody = body;
        return null;
    }

    @Override
    public Map<String, Object> fetchMap(String method, String path, Object body) {
        this.lastMethod = method;
        this.lastPath = path;
        this.lastBody = body;
        return new HashMap<>();
    }
}
