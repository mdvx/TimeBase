package deltix.qsrv.solgen.python;

import deltix.anvil.util.StringUtil;
import deltix.qsrv.solgen.CodegenUtils;
import deltix.qsrv.solgen.SolgenUtils;
import deltix.qsrv.solgen.StreamMetaData;
import deltix.qsrv.solgen.base.*;
import deltix.qsrv.solgen.java.JavaSampleFactory;

import java.util.*;

public class WriteStreamSample extends PythonSample {

    public static final Property STREAM_KEY = PropertyFactory.create(
        "timebase.stream",
        "TimeBase stream key.",
        true,
        StringUtil::isNotEmpty
    );

    static final List<Property> PROPERTIES = Collections.unmodifiableList(Arrays.asList(STREAM_KEY));

    private static final String MESSAGES_DEFINITION_PROP = "python.WriteStream.messages";
    private static final String MESSAGES_SEND_PROP = "python.WriteStream.sendMessages";

    private static final String SAMPLE_NAME = "WriteStream";
    private static final String SCRIPT_NAME = SAMPLE_NAME + ".py";
    private static final String TEMPLATE = SAMPLE_NAME + ".python-template";

    private final Source writeStreamSource;

    public WriteStreamSample(Properties properties) {
        this(properties.getProperty(PythonSampleFactory.TB_URL.getName()),
            properties.getProperty(STREAM_KEY.getName())
        );
    }

    public WriteStreamSample(String tbUrl, String stream) {
        Map<String, String> params = new HashMap<>();
        params.put(JavaSampleFactory.TB_URL.getName(), tbUrl);
        params.put(STREAM_KEY.getName(), stream);
        params.put(SAMPLE_NAME_PROP, SAMPLE_NAME);
        params.put(SCRIPT_NAME_PROP, SCRIPT_NAME);

        StreamMetaData metaData = CodegenUtils.getStreamMetadata(tbUrl, stream);

        params.put(MESSAGES_DEFINITION_PROP, PythonCodegenUtils.messagesDefinition(metaData, 1));
        params.put(MESSAGES_SEND_PROP, PythonCodegenUtils.messagesSend(metaData, 2));

        writeStreamSource = new StringSource(
            SCRIPT_NAME,
            SolgenUtils.readTemplateFromClassPath(this.getClass().getPackage(), TEMPLATE, params)
        );

        generateLaunchers(params);
    }

    @Override
    public void addToProject(Project project) {
        super.addToProject(project);

        project.addSource(writeStreamSource);
    }

}
