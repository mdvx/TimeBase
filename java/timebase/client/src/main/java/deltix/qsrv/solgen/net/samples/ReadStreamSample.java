package deltix.qsrv.solgen.net.samples;

import deltix.anvil.util.StringUtil;
import deltix.qsrv.solgen.SolgenUtils;
import deltix.qsrv.solgen.base.*;
import deltix.qsrv.solgen.net.NetSampleFactory;

import java.util.*;

public class ReadStreamSample implements Sample {

    public static final Property STREAM_KEY = PropertyFactory.create(
            "timebase.stream",
            "TimeBase stream key.",
            true,
            StringUtil::isNotEmpty
    );
    public static final List<Property> PROPERTIES = Collections.unmodifiableList(Collections.singletonList(STREAM_KEY));

    private static final String READ_STREAM_TEMPLATE = "ReadStream.cs-template";

    private final Source readStreamSource;

    public ReadStreamSample(Properties properties) {
        this(properties.getProperty(NetSampleFactory.TB_URL.getName()),
                properties.getProperty(STREAM_KEY.getName()));
    }

    public ReadStreamSample(String tbUrl, String key) {
        Map<String, String> params = new HashMap<>();
        params.put(NetSampleFactory.TB_URL.getName(), tbUrl);
        params.put(STREAM_KEY.getName(), key);

        readStreamSource = new StringSource("ReadStream.cs",
                SolgenUtils.readTemplateFromClassPath(this.getClass().getPackage(), READ_STREAM_TEMPLATE, params));
    }

    @Override
    public void addToProject(Project project) {
        project.addSource(readStreamSource);
    }
}
