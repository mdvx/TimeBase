package deltix.qsrv.solgen.cpp;

import deltix.qsrv.solgen.CodegenUtils;
import deltix.qsrv.solgen.SolgenUtils;
import deltix.qsrv.solgen.StreamMetaData;
import deltix.qsrv.solgen.base.Project;
import deltix.qsrv.solgen.base.Property;
import deltix.qsrv.solgen.base.Source;
import deltix.qsrv.solgen.base.StringSource;

import java.util.*;

public class SpeedTestSample extends CppSample {

    static final List<Property> PROPERTIES = Collections.unmodifiableList(
        Arrays.asList(CppSampleFactory.STREAM_KEY, CppSampleFactory.NUM_THREADS)
    );

    private static final String CODEC_INCLUDE = "cpp.codec.include";
    private static final String CODEC_CLASS_NAME = "cpp.codec.className";

    private static final String SAMPLE_NAME = "speed-test";
    private static final String SCRIPT_NAME = SAMPLE_NAME + ".cpp";
    private static final String TEMPLATE = SAMPLE_NAME + ".cpp-template";

    private final Source source;

    public SpeedTestSample(Properties properties) {
        this(properties.getProperty(CppSampleFactory.TB_URL.getName()),
            properties.getProperty(CppSampleFactory.STREAM_KEY.getName()),
            properties.getProperty(CppSampleFactory.NUM_THREADS.getName())
        );
    }

    public SpeedTestSample(String tbUrl, String stream, String numThreads) {
        super("speedtest", stream);
        Map<String, String> params = new HashMap<>();
        params.put(CppSampleFactory.TB_URL.getName(), tbUrl);
        params.put(CppSampleFactory.STREAM_KEY.getName(), stream);
        params.put(CppSampleFactory.NUM_THREADS.getName(), numThreads);

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
            CppCodecGenerator.getStreamDecoder(metaData.getNameSpace(), metaData.getNameSpace())
        );

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
