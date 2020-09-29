package deltix.qsrv.hf.tickdb.http;

import deltix.qsrv.QSHome;
import deltix.qsrv.comm.cat.StartConfiguration;
import deltix.qsrv.hf.pub.md.UHFJAXBContext;
import deltix.qsrv.hf.tickdb.TDBRunner;
import deltix.qsrv.hf.tickdb.comm.server.TomcatServer;
import deltix.qsrv.hf.tickdb.pub.StreamOptions;
import deltix.qsrv.testsetup.TickDBCreator;
import deltix.util.JUnitCategories;
import deltix.util.io.Home;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
@Category(JUnitCategories.TickDBFast.class)
public class SecuredBaseTest {

    protected static String         SECURITY_DIR        = Home.getPath("testdata/tickdb/security");
    protected static String         TB_LOCATION         = Home.getPath("temp/resttdbsecurity/tickdb");
    protected static String         CONFIG_LOCATION     = Home.getPath("temp/resttdbsecurity/config");
    protected static String         SECURITY_FILE_NAME  = "uac-file-security.xml";
    protected static String         SECURITY_RULES_NAME = "uac-access-rules.xml";

    protected static String         ADMIN_USER          = "admin";
    protected static String         ADMIN_PASS          = "admin";

    protected static TDBRunner      runner;
    protected static Marshaller     marshaller;
    protected static String         TB_HOST             = "localhost";
    protected static java.net.URL   URL;

    protected static Marshaller     global;

    @BeforeClass
    public static void start() throws Throwable {
        FileUtils.deleteDirectory(new File(TB_LOCATION).getParentFile());

        FileUtils.copyFile(new File(SECURITY_DIR + File.separator + SECURITY_FILE_NAME),
            new File(CONFIG_LOCATION + File.separator + SECURITY_FILE_NAME));
        FileUtils.copyFile(new File(SECURITY_DIR + File.separator + SECURITY_RULES_NAME),
            new File(CONFIG_LOCATION + File.separator + SECURITY_RULES_NAME));

        QSHome.set(new File(TB_LOCATION).getParentFile().getAbsolutePath());

        StartConfiguration configuration = StartConfiguration.create(true, false, false);
        configuration.quantServer.setProperty("security", "FILE");
        configuration.quantServer.setProperty("security.config", SECURITY_FILE_NAME);

        runner = new TDBRunner(true, true, TB_LOCATION, new TomcatServer(configuration));
        runner.user = ADMIN_USER;
        runner.pass = ADMIN_PASS;
        runner.startup();

        TickDBCreator.createBarsStream(runner.getServerDb(), TickDBCreator.BARS_STREAM_KEY);

        marshaller = TBJAXBContext.createMarshaller();
        global = UHFJAXBContext.createMarshaller();
        URL = new URL("http://" + TB_HOST + ":" + runner.getPort() + "/tb/xml");
    }

    public static URL   getPath(String path) throws MalformedURLException {
        return new URL("http://localhost:" + runner.getPort() + "/" + path);
    }

    public static void         createStream(String key, StreamOptions options) throws JAXBException, IOException {
        CreateStreamRequest request = new CreateStreamRequest();
        request.key = key;
        request.options = new StreamDef(options);

        StringWriter writer = new StringWriter();
        global.marshal(options.getMetaData(), writer);
        request.options.metadata = writer.getBuffer().toString();

        TestXmlQueries.query(URL, ADMIN_USER, ADMIN_PASS, request);
    }

    @AfterClass
    public static void stop() throws Throwable {
        runner.shutdown();
        runner = null;
    }
}
