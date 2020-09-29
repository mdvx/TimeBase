package deltix.qsrv.solgen;

import deltix.qsrv.solgen.base.SampleFactory;
import deltix.qsrv.solgen.base.SampleFactoryProviderBase;
import deltix.qsrv.solgen.cpp.CppSampleFactory;
import deltix.qsrv.solgen.java.JavaSampleFactory;
import deltix.qsrv.solgen.net.NetSampleFactory;
import deltix.qsrv.solgen.python.PythonSampleFactory;

public class SampleFactoryProvider implements SampleFactoryProviderBase {

    private static final SampleFactoryProvider INSTANCE = new SampleFactoryProvider();

    public static SampleFactoryProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public SampleFactory createForJava() {
        return new JavaSampleFactory();
    }

    @Override
    public SampleFactory createForNET() {
        return new NetSampleFactory();
    }

    @Override
    public SampleFactory createForPython() {
        return new PythonSampleFactory();
    }

    @Override
    public SampleFactory createForCpp() {
        return new CppSampleFactory();
    }

    @Override
    public SampleFactory createForGo() {
        return null;
    }
}
