package deltix.qsrv.solgen.cpp;

import deltix.qsrv.solgen.CodegenUtils;
import deltix.qsrv.solgen.SolgenUtils;
import deltix.qsrv.solgen.StreamMetaData;
import deltix.qsrv.solgen.base.Project;
import deltix.qsrv.solgen.base.Property;
import deltix.qsrv.solgen.base.Source;
import deltix.qsrv.solgen.base.StringSource;

import java.util.*;

public class WriteStreamSample extends CppSample {

    static final List<Property> PROPERTIES = Collections.unmodifiableList(Arrays.asList(CppSampleFactory.STREAM_KEY));

    private static final String CODEC_INCLUDE = "cpp.codec.include";
    private static final String CODEC_CLASS_NAME = "cpp.codec.className";

    private static final String MESSAGES_DEFINITION_PROP = "cpp.WriteStream.messages";
    private static final String MESSAGES_SEND_PROP = "cpp.WriteStream.sendMessages";

    private static final String SAMPLE_NAME = "write-stream";
    private static final String SCRIPT_NAME = SAMPLE_NAME + ".cpp";
    private static final String TEMPLATE = SAMPLE_NAME + ".cpp-template";

    private final Source source;

    public WriteStreamSample(Properties properties) {
        this(properties.getProperty(CppSampleFactory.TB_URL.getName()),
            properties.getProperty(CppSampleFactory.STREAM_KEY.getName())
        );
    }

    public WriteStreamSample(String tbUrl, String stream) {
        super("writestream", stream);
        Map<String, String> params = new HashMap<>();
        params.put(CppSampleFactory.TB_URL.getName(), tbUrl);
        params.put(CppSampleFactory.STREAM_KEY.getName(), stream);

        StreamMetaData metaData = CodegenUtils.getStreamMetadata(tbUrl, stream);

        params.put(
            CODEC_INCLUDE,
            "#include \"codecs/" +
            metaData.getNameSpace() + "/" +
            CppCodecGenerator.getStreamCodecName(metaData.getNameSpace(), null) +
            ".h\""
        );
        params.put(
            CODEC_CLASS_NAME,
            CppCodecGenerator.getStreamEncoder(metaData.getNameSpace(), metaData.getNameSpace())
        );
        params.put(MESSAGES_DEFINITION_PROP, CppCodegenUtils.messagesDefinitions(metaData, 2));
        params.put(MESSAGES_SEND_PROP, CppCodegenUtils.sendMessages(metaData));

        source = new StringSource(
            SCRIPT_NAME,
            SolgenUtils.readTemplateFromClassPath(this.getClass().getPackage(), TEMPLATE, params)
        );

    }

    @Override
    public void addToProject(Project project) {
        super.addToProject(project);

        project.addSource(source);
    }

}
