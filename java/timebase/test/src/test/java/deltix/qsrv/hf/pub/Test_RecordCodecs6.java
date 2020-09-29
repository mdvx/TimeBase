package deltix.qsrv.hf.pub;

import deltix.qsrv.hf.pub.codec.BoundDecoder;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.codec.FixedBoundEncoder;
import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.tickdb.TDBRunner;
import deltix.qsrv.hf.tickdb.http.BaseTest;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.pub.query.InstrumentMessageSource;
import deltix.qsrv.hf.tickdb.testframework.TestAllTypesMessage;
import deltix.qsrv.hf.tickdb.testframework.TestAllTypesStreamCreator;
import deltix.qsrv.hf.tickdb.util.ZIPUtil;
import deltix.timebase.api.messages.*;
import deltix.timebase.api.messages.securities.*;
import deltix.timebase.messages.*;
import deltix.timebase.messages.InstrumentMessage;
import deltix.timebase.messages.SchemaElement;
import deltix.util.JUnitCategories;
import deltix.util.collections.generated.*;
import deltix.util.io.Home;

import deltix.util.memory.MemoryDataInput;
import deltix.util.memory.MemoryDataOutput;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import static deltix.qsrv.hf.pub.Test_RecordCodecsBase.getRCD;
import static org.junit.Assert.*;

/**
 * Tests new message format
 */
@Category(JUnitCategories.TickDBCodecs.class)
public class Test_RecordCodecs6  extends BaseTest {

    private static final File DIR = Home.getFile("/testdata/tickdb/misc/");
    private static final File ZIP = new File(DIR, "enum_95_test.zip");

    private static final float DELTA = (float) 0.000001;

    private CodecFactory factory;

    private void setUpComp () {
        factory = CodecFactory.newCompiledCachingFactory();
    }

    private void setUpIntp () {
        factory = CodecFactory.newInterpretingCachingFactory();
    }


    static final TypeLoader CL = TypeLoaderImpl.DEFAULT_INSTANCE;

    private static final deltix.qsrv.hf.pub.md.RecordClassDescriptor rcdHiddenBooleanAtByte = new deltix.qsrv.hf.pub.md.RecordClassDescriptor(
            (deltix.qsrv.hf.pub.Test_RecordCodecs6.HiddenBooleanAtByte.class).getName(),
            null,
            false,
            null,
            new deltix.qsrv.hf.pub.md.NonStaticDataField("byteLikeBoolean1", null, new deltix.qsrv.hf.pub.md.BooleanDataType(true)),
            new deltix.qsrv.hf.pub.md.NonStaticDataField("byteLikeBoolean2", null, new deltix.qsrv.hf.pub.md.BooleanDataType(true))
    );

    public static final class HiddenBooleanAtByte extends InstrumentMessage {
        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        protected byte byteLikeBoolean1;
        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        private byte byteLikeBoolean2;

        public boolean getByteLikeBoolean1 () {
            return byteLikeBoolean1 == 1;
        }

        public void setByteLikeBoolean1 (boolean value) {
            this.byteLikeBoolean1 = (byte) (value ? 1 : 0);
        }

        public void nullifyByteLikeBoolean1 () {
            this.byteLikeBoolean1 = - 1;
        }

        public boolean hasByteLikeBoolean1 () {
            return this.byteLikeBoolean1 != - 1;
        }

        public boolean getByteLikeBoolean2 () {
            return byteLikeBoolean2 == 1;
        }

        public void setByteLikeBoolean2 (boolean value) {
            this.byteLikeBoolean2 = (byte) (value ? 1 : 0);
        }

        public void nullifyByteLikeBoolean2 () {
            this.byteLikeBoolean2 = - 1;
        }

        public boolean hasByteLikeBoolean2 () {
            return this.byteLikeBoolean2 != - 1;
        }


        @Override
        public boolean equals (Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HiddenBooleanAtByte that = (HiddenBooleanAtByte) o;

            if (getByteLikeBoolean1() != that.getByteLikeBoolean1()) return false;
            return getByteLikeBoolean2() == that.getByteLikeBoolean2();

        }

        @Override
        public String toString () {
            return "hiddenBooleanAtByte{" +
                    "byteLikeBoolean1=" + byteLikeBoolean1 +
                    ", byteLikeBoolean2=" + byteLikeBoolean2 +
                    '}';
        }
    }

    private static final deltix.qsrv.hf.pub.md.RecordClassDescriptor rcdAllProtectedNumbers = new deltix.qsrv.hf.pub.md.RecordClassDescriptor(
            (deltix.qsrv.hf.pub.Test_RecordCodecs6.AllProtectedNumbers.class).getName(),
            null,
            false,
            null,
            new deltix.qsrv.hf.pub.md.NonStaticDataField("shortValue", null, new deltix.qsrv.hf.pub.md.IntegerDataType("INT16", true)),
            new deltix.qsrv.hf.pub.md.NonStaticDataField("intValue", null, new deltix.qsrv.hf.pub.md.IntegerDataType("INT32", true)),
            new deltix.qsrv.hf.pub.md.NonStaticDataField("longValue", null, new deltix.qsrv.hf.pub.md.IntegerDataType("INT64", true)),

            new deltix.qsrv.hf.pub.md.NonStaticDataField("floatValue", null, new deltix.qsrv.hf.pub.md.FloatDataType("IEEE32", true)),
            new deltix.qsrv.hf.pub.md.NonStaticDataField("doubleValue", null, new deltix.qsrv.hf.pub.md.FloatDataType("IEEE64", true))
    );

    public static final class AllProtectedNumbers extends InstrumentMessage {
        protected short shortValue;
        protected int intValue;
        protected long longValue;

        protected float floatValue;
        protected double doubleValue;

        public short getShortValue () {
            return shortValue;
        }

        public void setShortValue (short shortValue) {
            this.shortValue = shortValue;
        }

        public void nullifyShortValue () {
            this.shortValue = - 32768;
        }

        public boolean hasShortValue () {
            return this.shortValue == - 32768;
        }

        public int getIntValue () {
            return intValue;
        }

        public void setIntValue (int intValue) {
            this.intValue = intValue;
        }

        public void nullifyIntValue () {
            this.intValue = - 2147483648;
        }

        public boolean hasIntValue () {
            return this.intValue == - 2147483648;
        }

        public long getLongValue () {
            return longValue;
        }

        public void setLongValue (long longValue) {
            this.longValue = longValue;
        }

        public void nullifyLongValue () {
            this.longValue = - 9223372036854775808L;
        }

        public boolean hasLongValue () {
            return this.longValue == - 9223372036854775808L;
        }

        public float getFloatValue () {
            return floatValue;
        }

        public void setFloatValue (float floatValue) {
            this.floatValue = floatValue;
        }

        public void nullifyFloatValue () {
            this.floatValue = 0.0f / 0.0f;
        }

        public boolean hasFloatValue () {
            return this.floatValue == 0.0f / 0.0f;
        }

        public double getDoubleValue () {
            return doubleValue;
        }


        public void setDoubleValue (double doubleValue) {
            this.doubleValue = doubleValue;
        }

        public void nullifyDoubleValue () {
            this.doubleValue = 0.0d / 0.0;
        }

        public boolean hasDoubleValue () {
            return this.doubleValue == 0.0d / 0.0;
        }
    }

    private static final deltix.qsrv.hf.pub.md.RecordClassDescriptor rcdAllProtectedStrings = new deltix.qsrv.hf.pub.md.RecordClassDescriptor(
            (Test_RecordCodecs6.AllProtectedStrings.class).getName(),
            null,
            false,
            null,
            new deltix.qsrv.hf.pub.md.NonStaticDataField("string", null, new deltix.qsrv.hf.pub.md.VarcharDataType("UTF8", true, false)),
            new deltix.qsrv.hf.pub.md.NonStaticDataField("charSequence", null, new deltix.qsrv.hf.pub.md.VarcharDataType("UTF8", true, false))
    );

    public static final class AllProtectedStrings extends InstrumentMessage {
        protected String string;
        protected CharSequence charSequence;


        public String getString () {
            return string;
        }

        public void setString (String string) {
            this.string = string;
        }

        public void nullifyString () {
            this.string = null;
        }

        public boolean hasString () {
            return this.string == null;
        }

        public CharSequence getCharSequence () {
            return charSequence;
        }

        public void setCharSequence (CharSequence charSequence) {
            this.charSequence = charSequence;
        }

        public void nullifyCharSequence () {
            this.charSequence = null;
        }

        public boolean hasCharSequence () {
            return this.charSequence == null;
        }

        @Override
        public boolean equals (Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AllProtectedStrings that = (AllProtectedStrings) o;

            if (getString() != null ? ! getString().equals(that.getString()) : that.getString() != null) return false;
            return getCharSequence() != null ? getCharSequence().equals(that.getCharSequence()) : that.getCharSequence() == null;

        }

        @Override
        public int hashCode () {
            int result = getString() != null ? getString().hashCode() : 0;
            result = 31 * result + (getCharSequence() != null ? getCharSequence().hashCode() : 0);
            return result;
        }

        @Override
        public String toString () {
            return "AllNullifyProtected{" +
                    "string='" + string + '\'' +
                    ", charSequence=" + charSequence +
                    '}';
        }
    }


    private static final deltix.qsrv.hf.pub.md.RecordClassDescriptor rcdAllTypeFields = new deltix.qsrv.hf.pub.md.RecordClassDescriptor(
            (Test_RecordCodecs6.AllTypeFields.class).getName(),
            null,
            false,
            null,
            new deltix.qsrv.hf.pub.md.StaticDataField("int1", "Test Statics", new IntegerDataType("INT32", true), 123),
            new deltix.qsrv.hf.pub.md.NonStaticDataField("int2", "Test NonStatics", new deltix.qsrv.hf.pub.md.IntegerDataType("INT32", false)),
            new deltix.qsrv.hf.pub.md.NonStaticDataField("int3", "Test NonStatics", new deltix.qsrv.hf.pub.md.IntegerDataType("INT32", true)),
            new deltix.qsrv.hf.pub.md.StaticDataField("int4", "Test Statics", new IntegerDataType("INT32", false), 2465)
    );

    public static final class AllTypeFields extends InstrumentMessage {
        protected int int1;
        protected int int2;
        protected int int3;
        protected int int4;

        public int getInt1 () {
            return int1;
        }

        public void setInt1 (int int1) {
            this.int1 = int1;
        }

        public void nullifyInt1 () {
            this.int1 = - 2147483648;
        }

        public boolean hasInt1 () {
            return this.int1 == - 2147483648;
        }

        public int getInt2 () {
            return int2;
        }

        public void setInt2 (int int2) {
            this.int2 = int2;
        }

        public void nullifyInt2 () {
            this.int2 = - 2147483648;
        }

        public boolean hasInt2 () {
            return this.int2 == - 2147483648;
        }

        public int getInt3 () {
            return int3;
        }

        public void setInt3 (int int3) {
            this.int3 = int3;
        }

        public void nullifyInt3 () {
            this.int3 = - 2147483648;
        }

        public boolean hasInt3 () {
            return this.int3 == - 2147483648;
        }

        public int getInt4 () {
            return int4;
        }

        public void setInt4 (int int4) {
            this.int4 = int4;
        }

        public void nullifyInt4 () {
            this.int4 = - 2147483648;
        }

        public boolean hasInt4 () {
            return this.int4 == - 2147483648;
        }

        @Override
        public boolean equals (Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AllTypeFields that = (AllTypeFields) o;

            if (getInt2() != that.getInt2()) return false;
            return getInt3() == that.getInt3();

        }

        @Override
        public int hashCode () {
            int result = getInt2();
            result = 31 * result + getInt3();
            return result;
        }

        @Override
        public String toString () {
            return "AllTypeFields{" +
                    "int2=" + int2 +
                    ", int3=" + int3 +
                    '}';
        }
    }

    private static final deltix.qsrv.hf.pub.md.RecordClassDescriptor rcdNotFullClass = new deltix.qsrv.hf.pub.md.RecordClassDescriptor(
            (Test_RecordCodecs6.NotFullClass.class).getName(),
            null,
            false,
            null,
            new deltix.qsrv.hf.pub.md.NonStaticDataField("string", null, new deltix.qsrv.hf.pub.md.VarcharDataType("UTF8", true, false)),
            new deltix.qsrv.hf.pub.md.NonStaticDataField("int1", null, new deltix.qsrv.hf.pub.md.IntegerDataType("INT32", false)),
            new deltix.qsrv.hf.pub.md.NonStaticDataField("int2", null, new deltix.qsrv.hf.pub.md.IntegerDataType("INT32", false)),
            new deltix.qsrv.hf.pub.md.NonStaticDataField("charSequence", null, new deltix.qsrv.hf.pub.md.VarcharDataType("UTF8", true, false))

    );

    public static final class NotFullClass extends InstrumentMessage {
        protected int int1;
        protected int int2;


        public int getInt2 () {
            return int2;
        }

        public void setInt2 (int int2) {
            this.int2 = int2;
        }

        public void nullifyInt2 () {
            this.int2 = - 2147483648;
        }

        public boolean hasInt2 () {
            return this.int2 == - 2147483648;
        }

        public int getInt1 () {
            return int1;
        }

        public void setInt1 (int int1) {
            this.int1 = int1;
        }

        public void nullifyInt1 () {
            this.int1 = - 2147483648;
        }

        public boolean hasInt1 () {
            return this.int1 == - 2147483648;
        }

        @Override
        public boolean equals (Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NotFullClass that = (NotFullClass) o;

            if (getInt1() != that.getInt1()) return false;
            return getInt2() == that.getInt2();

        }

        @Override
        public int hashCode () {
            int result = getInt1();
            result = 31 * result + getInt2();
            return result;
        }

        @Override
        public String toString () {
            return "NotFullClass{" +
                    "int1=" + int1 +
                    ", int2=" + int2 +
                    '}';
        }
    }

    @SchemaElement (
            name = "EnumClass"
    )
    public static final class EnumClass extends InstrumentMessage {
        @SchemaType (
                isNullable = false
        )
        public CurrencyType currencyType;
        @SchemaType (
                isNullable = false
        )
        public RollingType rollingType;

        public CurrencyType getCurrencyType () {
            return currencyType;
        }

        public void setCurrencyType (CurrencyType currencyType) {
            this.currencyType = currencyType;
        }

        public RollingType getRollingType () {
            return rollingType;
        }

        public void setRollingType (RollingType rollingType) {
            this.rollingType = rollingType;
        }

        @Override
        public boolean equals (Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EnumClass enumClass = (EnumClass) o;

            if (getCurrencyType() != enumClass.getCurrencyType()) return false;
            return getRollingType() == enumClass.getRollingType();

        }

        @Override
        public int hashCode () {
            int result = getCurrencyType() != null ? getCurrencyType().hashCode() : 0;
            result = 31 * result + (getRollingType() != null ? getRollingType().hashCode() : 0);
            return result;
        }

        @Override
        public String toString () {
            return "EnumClass{" +
                    "currencyType=" + currencyType +
                    ", rollingType=" + rollingType +
                    '}';
        }
    }

    private static final deltix.qsrv.hf.pub.md.RecordClassDescriptor CUSTOM_CLASS = new deltix.qsrv.hf.pub.md.RecordClassDescriptor(
            "EnumClass",
            null,
            false,
            null,
            new deltix.qsrv.hf.pub.md.NonStaticDataField("currencyType", null, new deltix.qsrv.hf.pub.md.EnumDataType(false, new EnumClassDescriptor(CurrencyType.class))),
            new deltix.qsrv.hf.pub.md.NonStaticDataField("rollingType", null, new deltix.qsrv.hf.pub.md.EnumDataType(false, new EnumClassDescriptor(RollingType.class)))
    );


    private static String createDB (String zipFileName) throws IOException, InterruptedException {
        File folder = new File(TDBRunner.getTemporaryLocation());
        //BasicIOUtil.deleteFileOrDir(folder);
        //folder.mkdirs();

        FileInputStream is = new FileInputStream(zipFileName);
        ZIPUtil.extractZipStream(is, folder);
        is.close();

        return folder.getAbsolutePath();
    }

    @org.junit.Test
    public void testOldEnumSchema () throws Throwable {
        String path = createDB(ZIP.getAbsolutePath());
        try (DXTickDB db = TickDBFactory.create(path)) {
            db.open(true);
            DXTickStream stream = db.getStream("enum_test");
            long time = TimeConstants.TIMESTAMP_UNKNOWN;
            MappingTypeLoader mappingTypeLoader = new MappingTypeLoader();
            mappingTypeLoader.bind("EnumClass", EnumClass.class);


            SelectionOptions options = new SelectionOptions();
            options.typeLoader = mappingTypeLoader;
            try (InstrumentMessageSource cursor =
                         stream.select(time, options)) {
                int count = 0;
                while (cursor.next() && count++ < 100) {
                    InstrumentMessage message = cursor.getMessage();
                    assertNotNull(message);
                }
            }

        } catch (Throwable e) {
            fail(e.getMessage());
        }

    }


    @org.junit.Test
    public void testNotFullClassIntp () {
        setUpIntp();
        Test_RecordCodecs6.NotFullClass inMsg = getNotFullClass();
        testBoundRoundTrip(inMsg, rcdNotFullClass);
    }

    @org.junit.Test
    public void testNotFullClassComp () {
        setUpComp();
        Test_RecordCodecs6.NotFullClass inMsg = getNotFullClass();
        testBoundRoundTrip(inMsg, rcdNotFullClass);
    }

    private Test_RecordCodecs6.NotFullClass getNotFullClass () {
        Test_RecordCodecs6.NotFullClass msg = new Test_RecordCodecs6.NotFullClass();
        msg.setInt2(25554);
        msg.setInt1(- 4545);
        return msg;
    }


    @org.junit.Test
    public void testAllTypeFieldsIntp () {
        setUpIntp();
        Test_RecordCodecs6.AllTypeFields inMsg = getAllTypeFields();
        testBoundRoundTrip(inMsg, rcdAllTypeFields);
    }

    @org.junit.Test
    public void testAllTypeFieldsComp () {
        setUpComp();
        Test_RecordCodecs6.AllTypeFields inMsg = getAllTypeFields();
        testBoundRoundTrip(inMsg, rcdAllTypeFields);
    }

    private Test_RecordCodecs6.AllTypeFields getAllTypeFields () {
        Test_RecordCodecs6.AllTypeFields msg = new Test_RecordCodecs6.AllTypeFields();
        msg.setInt2(25554);
        msg.setInt3(- 2147483648);
        msg.setInt1(123);
        msg.setInt4(2465);
        return msg;
    }

    @org.junit.Test
    public void testByteLikeBooleanIntp () {
        setUpIntp();
        Test_RecordCodecs6.HiddenBooleanAtByte inMsg = getHiddenBooleanAtByte();
        testBoundRoundTrip(inMsg, rcdHiddenBooleanAtByte);
    }

    @org.junit.Test
    public void testByteLikeBooleanComp () {
        setUpComp();
        Test_RecordCodecs6.HiddenBooleanAtByte inMsg = getHiddenBooleanAtByte();
        testBoundRoundTrip(inMsg, rcdHiddenBooleanAtByte);
    }

    private Test_RecordCodecs6.HiddenBooleanAtByte getHiddenBooleanAtByte () {
        Test_RecordCodecs6.HiddenBooleanAtByte msg = new Test_RecordCodecs6.HiddenBooleanAtByte();
        msg.setByteLikeBoolean1(false);
        msg.nullifyByteLikeBoolean2();
        return msg;
    }


    @org.junit.Test
    public void testNullifyStringsIntp () {
        setUpIntp();
        Test_RecordCodecs6.AllProtectedStrings inMsg = getAllProtectedStrings();
        testBoundRoundTrip(inMsg, rcdAllProtectedStrings);
    }

    @org.junit.Test
    public void testNullifyStringsComp () {
        setUpComp();
        Test_RecordCodecs6.AllProtectedStrings inMsg = getAllProtectedStrings();
        testBoundRoundTrip(inMsg, rcdAllProtectedStrings);
    }

    private Test_RecordCodecs6.AllProtectedStrings getAllProtectedStrings () {
        Test_RecordCodecs6.AllProtectedStrings msg = new Test_RecordCodecs6.AllProtectedStrings();
        (msg).setString("Hi Kolia !!!");
        (msg).setCharSequence("How are you Gene ???");
        return msg;
    }


    @org.junit.Test
    public void testProtectedNumbersIntp () {
        setUpIntp();
        Test_RecordCodecs6.AllProtectedNumbers inMsg = getAllProtectedNumbersMessage();
        testBoundRoundTrip(inMsg, rcdAllProtectedNumbers);
    }

    @org.junit.Test
    public void testProtectedNumbersComp () {
        setUpComp();
        Test_RecordCodecs6.AllProtectedNumbers inMsg = getAllProtectedNumbersMessage();
        testBoundRoundTrip(inMsg, rcdAllProtectedNumbers);
    }

    private Test_RecordCodecs6.AllProtectedNumbers getAllProtectedNumbersMessage () {
        Test_RecordCodecs6.AllProtectedNumbers msg = new Test_RecordCodecs6.AllProtectedNumbers();
        (msg).setShortValue((short) - 2555);
        (msg).setIntValue(- 2144649);
        (msg).setLongValue(- 140737455329L);
        (msg).setFloatValue((float) 5042.8894);
        (msg).setDoubleValue(9999546.1457);
        return msg;
    }


    private void testBoundRoundTrip (Object inMsg, RecordClassDescriptor rcd) {
        if (inMsg instanceof Test_RecordCodecs6.AllProtectedStrings) {
            Test_RecordCodecs6.AllProtectedStrings outMsg = (Test_RecordCodecs6.AllProtectedStrings) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((Test_RecordCodecs6.AllProtectedStrings) inMsg, outMsg);
        } else if (inMsg instanceof Test_RecordCodecs6.AllProtectedNumbers) {
            Test_RecordCodecs6.AllProtectedNumbers outMsg = (Test_RecordCodecs6.AllProtectedNumbers) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((Test_RecordCodecs6.AllProtectedNumbers) inMsg, outMsg);
        } else if (inMsg instanceof Test_RecordCodecs6.HiddenBooleanAtByte) {
            Test_RecordCodecs6.HiddenBooleanAtByte outMsg = (Test_RecordCodecs6.HiddenBooleanAtByte) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((Test_RecordCodecs6.HiddenBooleanAtByte) inMsg, outMsg);
        } else if (inMsg instanceof Test_RecordCodecs6.AllTypeFields) {
            Test_RecordCodecs6.AllTypeFields outMsg = (Test_RecordCodecs6.AllTypeFields) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((Test_RecordCodecs6.AllTypeFields) inMsg, outMsg);
        } else if (inMsg instanceof Test_RecordCodecs6.NotFullClass) {
            Test_RecordCodecs6.NotFullClass outMsg = (Test_RecordCodecs6.NotFullClass) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((Test_RecordCodecs6.NotFullClass) inMsg, outMsg);
        } else if (inMsg instanceof Test_RecordCodecs6.TestAllClassMessage) {
            Test_RecordCodecs6.TestAllClassMessage outMsg = (TestAllClassMessage) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((TestAllClassMessage) inMsg, outMsg);
        } else if (inMsg instanceof ClassA) {
            Test_RecordCodecs6.ClassA outMsg = (ClassA) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((ClassA) inMsg, outMsg);
        } else if (inMsg instanceof TestClassWithInterfaceField) {
            TestClassWithInterfaceField outMsg = (TestClassWithInterfaceField) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((TestClassWithInterfaceField) inMsg, outMsg);
        } else if (inMsg instanceof AllArraysTypes) {
            AllArraysTypes outMsg = (AllArraysTypes) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((AllArraysTypes) inMsg, outMsg);
        } else if (inMsg instanceof MinMaxFieldsClass) {
            MinMaxFieldsClass out = (MinMaxFieldsClass) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((MinMaxFieldsClass) inMsg, out);
        } else if (inMsg instanceof CaseInsensitiveClass) {
            CaseInsensitiveClass out = (CaseInsensitiveClass) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((CaseInsensitiveClass) inMsg, out);
        } else if (inMsg instanceof L2Message) {
            L2Message out = (L2Message) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((L2Message) inMsg, out);
        } else if (inMsg instanceof TestByteAsBoolean) {
            TestByteAsBoolean out = (TestByteAsBoolean) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((TestByteAsBoolean) inMsg, out);
        } else if (inMsg instanceof TestNewFormatBooleanClass) {
            TestNewFormatBooleanClass out = (TestNewFormatBooleanClass) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((TestNewFormatBooleanClass) inMsg, out);
        } else if (inMsg instanceof TestNewFormatPublicField) {
            TestNewFormatPublicField out = (TestNewFormatPublicField) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((TestNewFormatPublicField) inMsg, out);
        } else if (inMsg instanceof TestClassWithEnums) {
            TestClassWithEnums out = (TestClassWithEnums) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((TestClassWithEnums) inMsg, out);
        } else if (inMsg instanceof TestClassWithEnumsCharSequence) {
            TestClassWithEnumsCharSequence out = (TestClassWithEnumsCharSequence) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((TestClassWithEnumsCharSequence) inMsg, out);
        } else if (inMsg instanceof TestClassWithEnumsNumber) {
            TestClassWithEnumsNumber out = (TestClassWithEnumsNumber) boundRoundTrip(inMsg, rcd);
            checkRoundTrip((TestClassWithEnumsNumber) inMsg, out);
        }
    }

    private Object boundRoundTrip (Object inMsg, RecordClassDescriptor cd) {
        FixedBoundEncoder benc =
                factory.createFixedBoundEncoder(CL, cd);

        MemoryDataOutput out = new MemoryDataOutput();

        benc.encode(inMsg, out);

        MemoryDataInput in = new MemoryDataInput(out);

        Object outMsg = boundDecode(in, cd);

        return (outMsg);
    }

    private Object boundDecode (
            MemoryDataInput in,
            RecordClassDescriptor cd
    ) {
        BoundDecoder bdec =
                factory.createFixedBoundDecoder(CL, cd);

        return (bdec.decode(in));
    }

    private void checkRoundTrip (Test_RecordCodecs6.AllProtectedNumbers inMsg, Test_RecordCodecs6.AllProtectedNumbers outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getShortValue(), outMsg.getShortValue());
        assertEquals(inMsg.getIntValue(), outMsg.getIntValue());
        assertEquals(inMsg.getLongValue(), outMsg.getLongValue());
        assertEquals(inMsg.getFloatValue(), outMsg.getFloatValue(), DELTA);
        assertEquals(inMsg.getDoubleValue(), outMsg.getDoubleValue(), DELTA);
    }

    private void checkRoundTrip (Test_RecordCodecs6.MinMaxFieldsClass inMsg, Test_RecordCodecs6.MinMaxFieldsClass outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.int8, outMsg.int8);
        assertEquals(inMsg.int8_s, outMsg.int8_s);

        assertEquals(inMsg.ieee32, outMsg.ieee32, DELTA);
        assertEquals(inMsg.ieee32_s, outMsg.ieee32_s, DELTA);

        if (inMsg.ieee32_array != null && outMsg.ieee32_array != null)
            assertArrayEquals(inMsg.ieee32_array.toFloatArray(), outMsg.ieee32_array.toFloatArray(), DELTA);

        if (inMsg.int8_array != null && outMsg.int8_array != null)
            assertArrayEquals(inMsg.int8_array.toIntArray(), outMsg.int8_array.toIntArray());

    }

    private void checkRoundTrip (Test_RecordCodecs6.AllProtectedStrings inMsg, Test_RecordCodecs6.AllProtectedStrings outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getString(), outMsg.getString());
        assertEquals(inMsg.getCharSequence().toString(), outMsg.getCharSequence().toString());
    }

    private void checkRoundTrip (Test_RecordCodecs6.HiddenBooleanAtByte inMsg, Test_RecordCodecs6.HiddenBooleanAtByte outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getByteLikeBoolean1(), outMsg.getByteLikeBoolean1());
        assertEquals(inMsg.getByteLikeBoolean2(), outMsg.getByteLikeBoolean2());
    }

    private void checkRoundTrip (Test_RecordCodecs6.AllTypeFields inMsg, Test_RecordCodecs6.AllTypeFields outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getInt2(), outMsg.getInt2());
        assertEquals(inMsg.getInt3(), outMsg.getInt3());
        assertEquals(inMsg.getInt1(), outMsg.getInt1());
        assertEquals(inMsg.getInt4(), outMsg.getInt4());
    }

    private void checkRoundTrip (Test_RecordCodecs6.NotFullClass inMsg, Test_RecordCodecs6.NotFullClass outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getInt1(), outMsg.getInt1());
        assertEquals(inMsg.getInt2(), outMsg.getInt2());
    }

    private void checkRoundTrip (Test_RecordCodecs6.ClassA inMsg, Test_RecordCodecs6.ClassA outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        if (inMsg.nestedClassA != null && outMsg.nestedClassA != null) {
            assertEquals(inMsg.nestedClassA.getInt1(), outMsg.nestedClassA.getInt1());
            assertEquals(inMsg.nestedClassA.getBool1(), outMsg.nestedClassA.getBool1());
        }
        assertEquals(inMsg.nestedClassA_n.getInt1(), outMsg.nestedClassA_n.getInt1());
        assertEquals(inMsg.nestedClassA_n.getBool1(), outMsg.nestedClassA_n.getBool1());
    }

    private void checkRoundTrip (Test_RecordCodecs6.TestAllClassMessage inMsg, Test_RecordCodecs6.TestAllClassMessage outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        inMsg.allTypesMessage_n.assertEquals("", outMsg.allTypesMessage_n);
        inMsg.allTypesMessage_n1.assertEquals("", outMsg.allTypesMessage_n1);

        if (inMsg.allTypesMessage != null && outMsg.allTypesMessage != null)
            inMsg.allTypesMessage.assertEquals("", outMsg.allTypesMessage);

        if (inMsg.allTypesMessage1 != null && outMsg.allTypesMessage1 != null)
            inMsg.allTypesMessage1.assertEquals("", outMsg.allTypesMessage1);
    }


    private void checkRoundTrip (Test_RecordCodecs6.TestClassWithInterfaceField inMsg, Test_RecordCodecs6.TestClassWithInterfaceField outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        if (inMsg.intr1 instanceof ClassB) {
            assertTrue(outMsg.intr1 instanceof ClassB);
            checkRoundTrip((ClassB) inMsg.intr1, (ClassB) outMsg.intr1);
        } else if (inMsg.intr1 instanceof ClassBB) {
            assertTrue(outMsg.intr1 instanceof ClassBB);
            checkRoundTrip((ClassBB) inMsg.intr1, (ClassBB) outMsg.intr1);
        } else
            fail("expected types: ClassB, ClassBB.");

        if (inMsg.intr2 instanceof ClassB) {
            assertTrue(outMsg.intr2 instanceof ClassB);
            checkRoundTrip((ClassB) inMsg.intr2, (ClassB) outMsg.intr2);
        } else if (inMsg.intr2 instanceof ClassBB) {
            assertTrue(outMsg.intr2 instanceof ClassBB);
            checkRoundTrip((ClassBB) inMsg.intr2, (ClassBB) outMsg.intr2);
        } else
            fail("expected types: ClassB, ClassBB.");
    }

    private void checkRoundTrip (Test_RecordCodecs6.ClassB inMsg, Test_RecordCodecs6.ClassB outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.int1, outMsg.int1);
        assertEquals(inMsg.int2, outMsg.int2);
    }

    private void checkRoundTrip (Test_RecordCodecs6.ClassBB inMsg, Test_RecordCodecs6.ClassBB outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.enum1, outMsg.enum1);
        assertEquals(inMsg.enum2, outMsg.enum2);
        assertEquals(inMsg.enum3, outMsg.enum3);
    }

    private void checkRoundTrip (Test_RecordCodecs6.AllArraysTypes inMsg, Test_RecordCodecs6.AllArraysTypes outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.booleanArray1, outMsg.booleanArray1);
        assertEquals(inMsg.booleanArray2, outMsg.booleanArray2);

        assertEquals(inMsg.doubleArray1, outMsg.doubleArray1);
        assertEquals(inMsg.doubleArray2, outMsg.doubleArray2);
        assertEquals(inMsg.doubleArray3, outMsg.doubleArray3);

        assertEquals(inMsg.byteArray1, outMsg.byteArray1);

        assertEquals(inMsg.floatArray1, outMsg.floatArray1);
        assertEquals(inMsg.floatArray2, outMsg.floatArray2);
        assertEquals(inMsg.floatArray3, outMsg.floatArray3);

        assertEquals(inMsg.longArray1, outMsg.longArray1);
        assertEquals(inMsg.longArray2, outMsg.longArray2);
        assertEquals(inMsg.longArray3, outMsg.longArray3);

        assertEquals(inMsg.intArray1, outMsg.intArray1);
        assertEquals(inMsg.intArray2, outMsg.intArray2);
        assertEquals(inMsg.intArray3, outMsg.intArray3);

        assertEquals(inMsg.shortArray1, outMsg.shortArray1);
        assertEquals(inMsg.shortArray2, outMsg.shortArray2);
        assertEquals(inMsg.shortArray3, outMsg.shortArray3);

        if (inMsg.objArray1 == null)
            assertNull(outMsg.objArray1);
        else {
            assertEquals(inMsg.objArray1.size(), outMsg.objArray1.size());
            for (int i = 0; i < inMsg.objArray1.size(); i++) {
                checkRoundTrip(inMsg.objArray1.get(i), outMsg.objArray1.get(i));
            }
        }

        if (inMsg.objArray2 == null)
            assertNull(outMsg.objArray2);
        else {
            assertEquals(inMsg.objArray2.size(), outMsg.objArray2.size());
            for (int i = 0; i < inMsg.objArray2.size(); i++) {
                checkRoundTrip(inMsg.objArray2.get(i), outMsg.objArray2.get(i));
            }
        }

        if (inMsg.intrArray1 == null)
            assertNull(outMsg.intrArray1);
        else {
            assertEquals(inMsg.intrArray1.size(), outMsg.intrArray1.size());
            for (int i = 0; i < inMsg.intrArray1.size(); i++) {
                if (inMsg.intrArray1.get(i) instanceof ClassB) {
                    assertTrue(outMsg.intrArray1.get(i) instanceof ClassB);
                    checkRoundTrip((ClassB) inMsg.intrArray1.get(i), (ClassB) outMsg.intrArray1.get(i));
                } else if (inMsg.intrArray1.get(i) instanceof ClassBB) {
                    assertTrue(outMsg.intrArray1.get(i) instanceof ClassBB);
                    checkRoundTrip((ClassBB) inMsg.intrArray1.get(i), (ClassBB) outMsg.intrArray1.get(i));
                } else
                    fail("expected types: ClassB, ClassBB.");
            }
        }

        if (inMsg.intrArray2 == null)
            assertNull(outMsg.intrArray2);
        else {
            assertEquals(inMsg.intrArray2.size(), outMsg.intrArray2.size());
            for (int i = 0; i < inMsg.intrArray2.size(); i++) {
                if (inMsg.intrArray2.get(i) instanceof ClassB) {
                    assertTrue(outMsg.intrArray2.get(i) instanceof ClassB);
                    checkRoundTrip((ClassB) inMsg.intrArray2.get(i), (ClassB) outMsg.intrArray2.get(i));
                } else if (inMsg.intrArray2.get(i) instanceof ClassBB) {
                    assertTrue(outMsg.intrArray2.get(i) instanceof ClassBB);
                    checkRoundTrip((ClassBB) inMsg.intrArray2.get(i), (ClassBB) outMsg.intrArray2.get(i));
                } else
                    fail("expected types: ClassB, ClassBB.");
            }
        }
    }

    private void checkRoundTrip (Test_RecordCodecs6.NestedClassA inMsg, Test_RecordCodecs6.NestedClassA outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.bool1, outMsg.bool1);
        assertEquals(inMsg.int1, outMsg.int1);
    }

    @SchemaElement (
            title = "Test class with All types and new message format"
    )
    public static final class TestAllClassMessage extends InstrumentMessage {
        @SchemaType (
                isNullable = false
        )
        public TestAllTypesMessage allTypesMessage_n;

        public TestAllTypesMessage allTypesMessage;

        @SchemaType (
                isNullable = false
        )
        public TestAllTypesMessage allTypesMessage_n1;

        public TestAllTypesMessage allTypesMessage1;


        private static TestAllTypesStreamCreator creator = new TestAllTypesStreamCreator(null);
        private static Random random = new Random();

        void setNulls () {
            allTypesMessage_n = new TestAllTypesMessage();
            allTypesMessage = null;
            allTypesMessage_n1 = new TestAllTypesMessage();
            allTypesMessage1 = null;
            creator.fillMessage(random.nextInt(100) + 1, random.nextInt(100) + 1, allTypesMessage_n);
            creator.fillMessage(random.nextInt(100) + 1, random.nextInt(100) + 1, allTypesMessage_n1);
        }

        void setAll () {
            allTypesMessage_n = new TestAllTypesMessage();
            allTypesMessage = new TestAllTypesMessage();
            allTypesMessage_n1 = new TestAllTypesMessage();
            allTypesMessage1 = new TestAllTypesMessage();
            creator.fillMessage(random.nextInt(100) + 1, random.nextInt(100) + 1, allTypesMessage_n);
            creator.fillMessage(random.nextInt(100) + 1, random.nextInt(100) + 1, allTypesMessage_n1);
            creator.fillMessage(random.nextInt(100) + 1, random.nextInt(100) + 1, allTypesMessage);
            creator.fillMessage(random.nextInt(100) + 1, random.nextInt(100) + 1, allTypesMessage1);
        }

    }

    @Test
    public void testAllTypesIntp () throws Exception {
        setUpIntp();
        testAllTypeFields();
    }

    @Test
    public void testAllTypesComp () throws Exception {
        setUpComp();
        testAllTypeFields();
    }

    private void testAllTypeFields () throws Exception {
        final RecordClassDescriptor rcd = getRCD(TestAllClassMessage.class);
        final TestAllClassMessage msg = new TestAllClassMessage();

        for (int i = 0; i < 100; i++) {
            switch (i % 10) {
                case 0:
                    msg.setNulls();
                    break;
                default:
                    msg.setAll();
                    break;
            }
            testBoundRoundTrip(msg, rcd);
        }
    }


    public static class NestedClassA {
        private int int1;
        private byte bool1;

        @SchemaElement
        public int getInt1 () {
            return int1;
        }

        public void setInt1 (int int1) {
            this.int1 = int1;
        }

        public void nullifyInt1 () {
            this.int1 = IntegerDataType.INT32_NULL;
        }

        public boolean hasInt1 () {
            return this.int1 != IntegerDataType.INT32_NULL;
        }

        @SchemaElement
        @SchemaType (
                dataType = SchemaDataType.BOOLEAN
        )
        public boolean getBool1 () {
            return bool1 == 1;
        }

        public void setBool1 (boolean value) {
            this.bool1 = (byte) (value ? 1 : 0);
        }

        public void nullifyBool1 () {
            this.bool1 = TypeConstants.BOOLEAN_NULL;
        }

        public boolean hasBool1 () {
            return bool1 != TypeConstants.BOOLEAN_NULL;
        }

        @Override
        public String toString () {
            return "NestedClassA{" +
                    "int1=" + int1 +
                    ", bool1=" + bool1 +
                    '}';
        }
    }

    public interface InterfaceB {
    }

    @SchemaElement (
            title = "Message class with class field and new SchemaType decorator"
    )
    public static class ClassA extends InstrumentMessage {

        public NestedClassA nestedClassA;

        @SchemaType (
                isNullable = false
        )
        public NestedClassA nestedClassA_n;

        public ClassA () {
            nestedClassA_n = new NestedClassA();
        }

        @Override
        public String toString () {
            return "ClassA{" +
                    "nestedClassA=" + nestedClassA +
                    ", nestedClassA_n=" + nestedClassA_n +
                    '}';
        }
    }

    public enum TestEnum {RED, BLUE, GREEN, YELLOW}

    public static class ClassBB implements InterfaceB {

        public ClassBB () {
            enum1 = TestEnum.RED;
        }

        private TestEnum enum1;
        private TestEnum enum2;
        private TestEnum enum3;

        public void setEnum1 (TestEnum enum1) {
            this.enum1 = enum1;
        }

        @SchemaElement
        @SchemaType (
                isNullable = false,
                dataType = SchemaDataType.ENUM,
                nestedTypes = {TestEnum.class}
        )
        public TestEnum getEnum1 () {
            return this.enum1;
        }

        public void nullifyEnum1 () {
            this.enum1 = null;
        }

        public boolean hasEnum1 () {
            return this.enum1 == null;
        }


        public void setEnum2 (TestEnum enum2) {
            this.enum2 = enum2;
        }

        @SchemaElement
        @SchemaType (
                dataType = SchemaDataType.ENUM,
                nestedTypes = {TestEnum.class}
        )
        public TestEnum getEnum2 () {
            return this.enum2;
        }

        public void nullifyEnum2 () {
            this.enum2 = null;
        }

        public boolean hasEnum2 () {
            return this.enum2 == null;
        }


        public void setEnum3 (TestEnum enum3) {
            this.enum3 = enum3;
        }

        @SchemaElement
        public TestEnum getEnum3 () {
            return this.enum3;
        }

        public void nullifyEnum3 () {
            this.enum3 = null;
        }

        public boolean hasEnum3 () {
            return this.enum3 == null;
        }

        @Override
        public String toString () {
            return "ClassAA{" +
                    "enum1=" + enum1 +
                    ", enum2=" + enum2 +
                    ", enum3=" + enum3 +
                    '}';
        }
    }

    public static class ClassB implements InterfaceB {
        public int int1;
        public int int2;

        @Override
        public String toString () {
            return "ClassB{" +
                    "int1=" + int1 +
                    ", int2=" + int2 +
                    '}';
        }
    }

    @Test
    public void testNewSchemaClassFiledIntp () throws Exception {
        setUpIntp();
        testNewSchemaClassFiled();
    }

    @Test
    public void testNewSchemaClassFiledComp () throws Exception {
        setUpComp();
        testNewSchemaClassFiled();
    }

    public void testNewSchemaClassFiled () throws Exception {
        final RecordClassDescriptor rcd = getRCD(ClassA.class);
        final ClassA msg = new ClassA();

        for (int i = 0; i < 100; i++) {
            switch (i % 10) {
                case 0:
                    msg.setSymbol("s1");
                    msg.nestedClassA_n = new NestedClassA();
                    msg.nestedClassA_n.setBool1(true);
                    msg.nestedClassA_n.setInt1(i);
                    break;
                default:
                    msg.setSymbol("s2");
                    msg.nestedClassA = new NestedClassA();
                    msg.nestedClassA.nullifyBool1();
                    msg.nestedClassA.setInt1(i);
                    msg.nestedClassA_n = new NestedClassA();
                    msg.nestedClassA_n.setBool1(false);
                    msg.nestedClassA_n.setInt1(i);
                    break;
            }
            testBoundRoundTrip(msg, rcd);
        }
    }


    public static class TestClassWithInterfaceField extends InstrumentMessage {
        @SchemaType (
                isNullable = false,
                dataType = SchemaDataType.OBJECT,
                nestedTypes = {ClassB.class, ClassBB.class}
        )
        public InterfaceB intr1;
        @SchemaType (
                dataType = SchemaDataType.OBJECT,
                nestedTypes = {ClassB.class, ClassBB.class}
        )
        public InterfaceB intr2;

        @Override
        public String toString () {
            return "TestClassWithInterfaceField{" +
                    "intr1=" + intr1 +
                    ", intr2=" + intr2 +
                    '}';
        }
    }

    @Test
    public void testNewSchemaClassInterfaceFiledIntp () throws Exception {
        setUpIntp();
        testNewSchemaClassInterfaceFiled();
    }

    @Test
    public void testNewSchemaClassInterfaceFiledComp () throws Exception {
        setUpComp();
        testNewSchemaClassInterfaceFiled();
    }

    private void testNewSchemaClassInterfaceFiled () throws Exception {
        final RecordClassDescriptor rcd = getRCD(TestClassWithInterfaceField.class);
        final TestClassWithInterfaceField msg = new TestClassWithInterfaceField();
        ClassB classB;
        ClassBB classBB;
        for (int i = 0; i < 100; i++) {
            switch (i % 10) {
                case 1:
                    msg.intr1 = new ClassB();
                    classB = (ClassB) msg.intr1;
                    classB.int1 = 20 + i;
                    classB.int1 = 10 + i;
                    msg.intr2 = new ClassBB();
                    classBB = (ClassBB) msg.intr2;
                    classBB.enum1 = TestEnum.BLUE;
                    classBB.nullifyEnum2();
                    classBB.nullifyEnum3();
                    break;

                default:
                    msg.intr1 = new ClassB();
                    classB = (ClassB) msg.intr1;
                    classB.int1 = 20 + i;
                    classB.int1 = 10 + i;
                    msg.intr2 = new ClassBB();
                    classBB = (ClassBB) msg.intr2;
                    classBB.setEnum1(TestEnum.BLUE);
                    classBB.setEnum2(TestEnum.RED);
                    classBB.setEnum3(TestEnum.YELLOW);
                    break;
            }
            testBoundRoundTrip(msg, rcd);
        }
    }

    public static class AllArraysTypes extends InstrumentMessage {


        @SchemaArrayType (
                isNullable = false
        )
        public ObjectArrayList<NestedClassA> objArray1;
        public ObjectArrayList<NestedClassA> objArray2;

        @SchemaArrayType (
                isNullable = false,
                elementTypes = {ClassB.class, ClassBB.class}
        )
        public ObjectArrayList<InterfaceB> intrArray1;
        @SchemaArrayType (
                elementTypes = {ClassB.class, ClassBB.class}
        )
        public ObjectArrayList<InterfaceB> intrArray2;


        @SchemaArrayType (
                isNullable = false,
                isElementNullable = false,
                elementDataType = SchemaDataType.BOOLEAN
        )
        public BooleanArrayList booleanArray1;
        @SchemaArrayType (
                isNullable = false,
                elementEncoding = "INT8",
                elementDataType = SchemaDataType.INTEGER
        )
        public ByteArrayList byteArray1;
        @SchemaArrayType (
                isNullable = false,
                elementEncoding = "INT16",
                elementDataType = SchemaDataType.INTEGER
        )
        public ShortArrayList shortArray1;
        @SchemaArrayType (
                isNullable = false,
                elementEncoding = "INT32",
                elementDataType = SchemaDataType.INTEGER
        )
        public IntegerArrayList intArray1;
        @SchemaArrayType (
                isNullable = false,
                elementEncoding = "INT64",
                elementDataType = SchemaDataType.INTEGER
        )
        public LongArrayList longArray1;
        @SchemaArrayType (
                isNullable = false,
                elementEncoding = "IEEE64",
                elementDataType = SchemaDataType.FLOAT
        )
        public DoubleArrayList doubleArray1;
        @SchemaArrayType (
                isNullable = false,
                elementEncoding = "IEEE32",
                elementDataType = SchemaDataType.FLOAT
        )
        public FloatArrayList floatArray1;

        @SchemaArrayType (
                isNullable = false,
                isElementNullable = false
        )
        public BooleanArrayList booleanArray2;
        @SchemaArrayType (
                isNullable = false
        )
        public ShortArrayList shortArray2;
        @SchemaArrayType (
                isNullable = false
        )
        public IntegerArrayList intArray2;
        @SchemaArrayType (
                isNullable = false
        )
        public LongArrayList longArray2;
        @SchemaArrayType (
                isNullable = false
        )
        public DoubleArrayList doubleArray2;
        @SchemaArrayType (
                isNullable = false
        )
        public FloatArrayList floatArray2;

        public ShortArrayList shortArray3;
        public IntegerArrayList intArray3;
        public LongArrayList longArray3;
        public DoubleArrayList doubleArray3;
        public FloatArrayList floatArray3;


        void setValues (int count) {
            objArray1 = new ObjectArrayList<>();
            objArray2 = new ObjectArrayList<>();
            for (int i = 0; i < count; i++) {
                objArray1.add(new NestedClassA());
                objArray2.add(new NestedClassA());
            }

            intrArray1 = new ObjectArrayList<>();
            intrArray2 = new ObjectArrayList<>();
            for (int i = 0; i < count; i++) {
                if (i % 2 == 0) {
                    intrArray1.add(new ClassB());
                    intrArray2.add(new ClassB());
                } else {
                    intrArray1.add(new ClassBB());
                    intrArray2.add(new ClassBB());
                }
            }

            booleanArray1 = new BooleanArrayList();
            byteArray1 = new ByteArrayList();
            shortArray1 = new ShortArrayList();
            intArray1 = new IntegerArrayList();
            longArray1 = new LongArrayList();
            doubleArray1 = new DoubleArrayList();
            floatArray1 = new FloatArrayList();


            booleanArray2 = new BooleanArrayList();
            shortArray2 = new ShortArrayList();
            intArray2 = new IntegerArrayList();
            longArray2 = new LongArrayList();
            doubleArray2 = new DoubleArrayList();
            floatArray2 = new FloatArrayList();

            shortArray3 = new ShortArrayList();
            intArray3 = new IntegerArrayList();
            longArray3 = new LongArrayList();
            doubleArray3 = new DoubleArrayList();
            floatArray3 = new FloatArrayList();
        }

        void setNulls () {
            objArray1 = new ObjectArrayList<>();
            objArray2 = null;

            intrArray1 = new ObjectArrayList<>();
            intrArray2 = null;

            booleanArray1 = new BooleanArrayList();
            byteArray1 = new ByteArrayList();
            shortArray1 = new ShortArrayList();
            intArray1 = new IntegerArrayList();
            longArray1 = new LongArrayList();
            doubleArray1 = new DoubleArrayList();
            floatArray1 = new FloatArrayList();


            booleanArray2 = new BooleanArrayList();
            shortArray2 = new ShortArrayList();
            intArray2 = new IntegerArrayList();
            longArray2 = new LongArrayList();
            doubleArray2 = new DoubleArrayList();
            floatArray2 = new FloatArrayList();

            shortArray3 = null;
            intArray3 = null;
            longArray3 = null;
            doubleArray3 = null;
            floatArray3 = null;
        }

        @Override
        public String toString () {
            return "AllArraysTypes{" +
                    "objArray1=" + objArray1 +
                    ", objArray2=" + objArray2 +
                    ", intrArray1=" + intrArray1 +
                    ", intrArray2=" + intrArray2 +
                    ", booleanArray1=" + booleanArray1 +
                    ", byteArray1=" + byteArray1 +
                    ", shortArray1=" + shortArray1 +
                    ", intArray1=" + intArray1 +
                    ", longArray1=" + longArray1 +
                    ", doubleArray1=" + doubleArray1 +
                    ", floatArray1=" + floatArray1 +
                    ", booleanArray2=" + booleanArray2 +
                    ", shortArray2=" + shortArray2 +
                    ", intArray2=" + intArray2 +
                    ", longArray2=" + longArray2 +
                    ", doubleArray2=" + doubleArray2 +
                    ", floatArray2=" + floatArray2 +
                    ", shortArray3=" + shortArray3 +
                    ", intArray3=" + intArray3 +
                    ", longArray3=" + longArray3 +
                    ", doubleArray3=" + doubleArray3 +
                    ", floatArray3=" + floatArray3 +
                    '}';
        }
    }


    @Test
    public void testAllArraysTypesIntp () throws Exception {
        setUpIntp();
        testAllArraysTypes();
    }

    @Test
    public void testAllArraysTypesComp () throws Exception {
        setUpComp();
        testAllArraysTypes();
    }

    private void testAllArraysTypes () throws Exception {
        final RecordClassDescriptor rcd = getRCD(AllArraysTypes.class);
        final AllArraysTypes msg = new AllArraysTypes();
        for (int i = 0; i < 50; i++) {
            switch (i % 10) {
                case 0:
                    msg.setNulls();
                    break;
                default:
                    msg.setValues(i + i);
                    break;
            }
            testBoundRoundTrip(msg, rcd);
        }
    }


    public static class MinMaxFieldsClass extends InstrumentMessage {

        @SchemaType (
                encoding = "INT8",
                dataType = SchemaDataType.INTEGER
        )
        public int int8_s;

        @SchemaType (
                encoding = "INT8",
                dataType = SchemaDataType.INTEGER,
                minimum = "-5",
                maximum = "5"
        )
        public int int8;


        @SchemaType (
                encoding = "IEEE32",
                dataType = SchemaDataType.FLOAT
        )
        public float ieee32_s;

        @SchemaType (
                encoding = "IEEE32",
                dataType = SchemaDataType.FLOAT,
                minimum = "-5.0",
                maximum = "5.0"
        )
        public float ieee32;


        @SchemaArrayType (
                elementEncoding = "INT8",
                elementMinimum = "-5",
                elementMaximum = "5"
        )
        public IntegerArrayList int8_array;


        @SchemaArrayType (
                elementEncoding = "IEEE32",
                elementMinimum = "-5.0",
                elementMaximum = "5.0"
        )
        public FloatArrayList ieee32_array;

        @Override
        public String toString () {
            return "MinMaxFieldsClass{" +
                    "int8_s=" + int8_s +
                    ", int8=" + int8 +
                    ", ieee32_s=" + ieee32_s +
                    ", ieee32=" + ieee32 +
                    ", int8_array=" + int8_array +
                    ", ieee32_array=" + ieee32_array +
                    '}';
        }
    }

    @Test
    public void testMinMaxFieldsClassIntp () throws Exception {
        setUpIntp();
        testMinMaxFieldsClass();
    }

    @Test
    public void testMinMaxFieldsClassComp () throws Exception {
        setUpComp();
        testMinMaxFieldsClass();
    }

    private void testMinMaxFieldsClass () throws Exception {
        final RecordClassDescriptor rcd = getRCD(MinMaxFieldsClass.class);
        final MinMaxFieldsClass msg = new MinMaxFieldsClass();

        msg.int8_s = 120;
        msg.int8 = 5;
        msg.ieee32_s = (float) 120.0;
        msg.ieee32 = (float) 5.0;
        msg.int8_array = new IntegerArrayList();
        msg.int8_array.add(5);
        msg.ieee32_array = new FloatArrayList();
        msg.ieee32_array.add((float) 5.0);
        testBoundRoundTrip(msg, rcd);

        try {
            msg.int8 = 6;
            testBoundRoundTrip(msg, rcd);
            fail("cannot set value " + msg.int8 + " to public int deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.int8 field");
        } catch (IllegalArgumentException ex) {
            assertEquals("public int deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.int8 == 6", ex.getMessage());
        }

        try {
            msg.int8 = - 99;
            testBoundRoundTrip(msg, rcd);
            fail("cannot set value " + msg.int8 + " to public int deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.int8 field");
        } catch (IllegalArgumentException ex) {
            assertEquals("public int deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.int8 == -99", ex.getMessage());
        }

        try {
            msg.int8 = 1;
            msg.ieee32 = (float) 9.3;
            testBoundRoundTrip(msg, rcd);
            fail("cannot set value " + msg.int8 + " to public float deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.ieee32 field");
        } catch (IllegalArgumentException ex) {
            assertEquals("public float deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.ieee32 == 9.3", ex.getMessage());
        }

        try {
            msg.ieee32 = (float) - 98.3;
            testBoundRoundTrip(msg, rcd);
            fail("cannot set value " + msg.int8 + " to public float deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.ieee32 field");
        } catch (IllegalArgumentException ex) {
            assertEquals("public float deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.ieee32 == -98.3", ex.getMessage());
        }

        try {
            msg.ieee32 = (float) - 2.3;
            msg.int8_array.add(99);
            testBoundRoundTrip(msg, rcd);
            fail("cannot set value " + msg.int8_array + " to deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.int8_array field");
        } catch (IllegalArgumentException ex) {
            assertEquals("deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.int8_array == 99", ex.getMessage());
        }

        try {
            msg.int8_array.clear();
            msg.int8_array.add(- 66);
            testBoundRoundTrip(msg, rcd);
            fail("cannot set value " + msg.int8_array + " to deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.int8_array field");
        } catch (IllegalArgumentException ex) {
            assertEquals("deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.int8_array == -66", ex.getMessage());
        }

        try {
            msg.int8_array.clear();
            msg.ieee32_array.add((float) - 65.9);
            testBoundRoundTrip(msg, rcd);
            fail("cannot set value " + msg.ieee32_array + " to deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.ieee32_array field");
        } catch (IllegalArgumentException ex) {
            assertEquals("deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.ieee32_array == -65.9", ex.getMessage());
        }

        try {
            msg.ieee32_array.clear();
            msg.ieee32_array.add((float) 5.1);
            testBoundRoundTrip(msg, rcd);
            fail("cannot set value " + msg.ieee32_array + " to deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.ieee32_array field");
        } catch (IllegalArgumentException ex) {
            assertEquals("deltix.qsrv.hf.pub.Test_RecordCodecs6$MinMaxFieldsClass.ieee32_array == 5.1", ex.getMessage());
        }

    }


    @SchemaElement (
            title = "class for test Integer encoding"
    )
    public static class AllIntegerEncoding extends InstrumentMessage {

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "INT8"
        )
        public byte int8;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "INT16"
        )
        public short int16;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "INT32"
        )
        public int int31;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "INT48"
        )
        public long int48;


        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "INT64"
        )
        public long int64;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "SIGNED(8)"
        )
        public byte signed8;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "SIGNED(16)"
        )
        public short signed16;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "SIGNED(32)"
        )
        public int signed32;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "SIGNED(48)"
        )
        public long signed48;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "SIGNED(64)"
        )
        public long signed64;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "PUINT30"
        )
        public int puint30;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "PUINT61"
        )
        public long puint61;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "UNSIGNED(30)"
        )
        public int unsigned30;

        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "UNSIGNED(61)"
        )
        public long unsigned61;


        @SchemaType (
                dataType = SchemaDataType.INTEGER,
                encoding = "PINTERVAL"
        )
        public int pinterval;

        @Override
        public String toString () {
            return "TestAllIntegerEncoding{" +
                    "int8=" + int8 +
                    ", int16=" + int16 +
                    ", int31=" + int31 +
                    ", int48=" + int48 +
                    ", int64=" + int64 +
                    ", signed8=" + signed8 +
                    ", signed16=" + signed16 +
                    ", signed32=" + signed32 +
                    ", signed48=" + signed48 +
                    ", signed64=" + signed64 +
                    ", puint30=" + puint30 +
                    ", puint61=" + puint61 +
                    ", unsigned30=" + unsigned30 +
                    ", unsigned61=" + unsigned61 +
                    ", pinterval=" + pinterval +
                    '}';
        }
    }


    @Test
    public void testAllIntegerEncoding () throws Throwable {
        final RecordClassDescriptor rcd = getRCD(AllIntegerEncoding.class);
        assertNotNull(rcd);

        for (DataField df : rcd.getFields()) {
            DataType dt = df.getType();
            assertTrue(dt instanceof IntegerDataType);
            IntegerDataType idt = (IntegerDataType) dt;
            switch (idt.getEncoding()) {
                case "INT8":
                case "SIGNED(8)":
                    assertEquals(1, idt.getSize());
                    break;
                case "INT16":
                case "SIGNED(16)":
                    assertEquals(2, idt.getSize());
                    break;
                case "INT32":
                case "SIGNED(32)":
                    assertEquals(4, idt.getSize());
                    break;
                case "INT48":
                case "SIGNED(48)":
                    assertEquals(6, idt.getSize());
                    break;
                case "INT64":
                case "SIGNED(64)":
                    assertEquals(8, idt.getSize());
                    break;
                case "PUINT30":
                case "UNSIGNED(30)":
                    assertEquals(100, idt.getSize());
                    break;
                case "PUINT61":
                case "UNSIGNED(61)":
                    assertEquals(101, idt.getSize());
                    break;
                case "PINTERVAL":
                    assertEquals(102, idt.getSize());
                    break;
                default:
                    fail("incorrect encoding for Integer data type!");
                    break;
            }
        }
    }

    @SchemaElement (
            title = "test class for Float encoding"
    )
    public static class AllFloatEncpding extends InstrumentMessage {

        @SchemaType (
                dataType = SchemaDataType.FLOAT,
                encoding = "IEEE32"
        )
        public float ieee32;

        @SchemaType (
                dataType = SchemaDataType.FLOAT,
                encoding = "IEEE64"
        )
        public double ieee64;

        @SchemaType (
                dataType = SchemaDataType.FLOAT,
                encoding = "BINARY(32)"
        )
        public float binary32;

        @SchemaType (
                dataType = SchemaDataType.FLOAT,
                encoding = "BINARY(64)"
        )
        public double binary64;

        @SchemaType (
                dataType = SchemaDataType.FLOAT,
                encoding = "DECIMAL"
        )
        public double decimal;

        @SchemaType (
                dataType = SchemaDataType.FLOAT,
                encoding = "DECIMAL(2)"
        )
        public double decimal2;

        @Override
        public String toString () {
            return "AllFloatEncpding{" +
                    "ieee32=" + ieee32 +
                    ", ieee64=" + ieee64 +
                    ", binary32=" + binary32 +
                    ", binary64=" + binary64 +
                    ", decimal=" + decimal +
                    ", decimal2=" + decimal2 +
                    '}';
        }
    }

    @Test
    public void testAllFloatEncoding () throws Throwable {
        final RecordClassDescriptor rcd = getRCD(AllFloatEncpding.class);
        assertNotNull(rcd);
        for (DataField df : rcd.getFields()) {
            DataType dt = df.getType();
            assertTrue(dt instanceof FloatDataType);
            FloatDataType fdt = (FloatDataType) dt;
            switch (fdt.getEncoding()) {
                case "IEEE32":
                case "BINARY(32)":
                    assertEquals(100, fdt.getScale());
                    break;
                case "IEEE64":
                case "BINARY(64)":
                    assertEquals(101, fdt.getScale());
                    break;
                case "DECIMAL":
                    assertEquals(102, fdt.getScale());
                    break;

                default:
                    assertEquals(FloatDataType.parseEncodingScaled(fdt.getEncoding()), fdt.getScale());
                    break;
            }
        }
    }


    public static class TestIntrospectorClass extends InstrumentMessage {
        @SchemaType (
                encoding = "INT8",
                minimum = "3",
                maximum = "9",
                isNullable = false
        )
        public short int8;

        public short int16;

        @SchemaType (
                encoding = "IEEE32",
                minimum = "-5.0",
                maximum = "5.0"
        )
        public double ieee32;

        public double ieee64;

        public long int64;

        @SchemaType (
                encoding = "ALPHANUMERIC(10)",
                dataType = SchemaDataType.VARCHAR
        )
        public long alphanumeric;

        @SchemaType (
                encoding = "UTF8"
        )
        public String utf8;

        @SchemaType (
                encoding = "ALPHANUMERIC(10)"
        )
        public String alphanumericString;

        public LongArrayList int64Array;

        @SchemaArrayType (
                elementEncoding = "INT16",
                elementMaximum = "99",
                elementMinimum = "1",
                isNullable = false
        )
        public IntegerArrayList int16Array;

        @SchemaArrayType (
                elementEncoding = "INT8",
                elementMaximum = "99",
                elementMinimum = "18",
                isElementNullable = false,
                isNullable = false
        )
        public IntegerArrayList int8Array;

        public DoubleArrayList ieee64Array;

        @SchemaArrayType (
                isElementNullable = false,
                elementEncoding = "IEEE32",
                elementMinimum = "-99.1",
                elementMaximum = "98.9",
                isNullable = false
        )
        public DoubleArrayList ieee32Array;
    }

    @Test
    public void testIntrospector () throws Throwable {
        final RecordClassDescriptor rcd = getRCD(TestIntrospectorClass.class);
        assertNotNull(rcd);

        DataField df;
        DataType dt;

        final DataField[] dfs = rcd.getFields();
        assertNotNull(dfs);
        assertEquals(13, dfs.length);

        //int8 field
        df = rcd.getField("int8");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof IntegerDataType);
        assertEquals("INT8", (dt).getEncoding());
        assertEquals(3, ((IntegerDataType) dt).getMin().intValue());
        assertEquals(9, ((IntegerDataType) dt).getMax().intValue());
        assertFalse(dt.isNullable());

        //int16 field
        df = rcd.getField("int16");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof IntegerDataType);
        assertEquals("INT16", (dt).getEncoding());
        assertNull(((IntegerDataType) dt).getMin());
        assertNull(((IntegerDataType) dt).getMax());

        //ieee32 field
        df = rcd.getField("ieee32");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof FloatDataType);
        assertEquals("IEEE32", (dt).getEncoding());
        assertEquals(- 5.0, ((FloatDataType) dt).getMin().doubleValue(), DELTA);
        assertEquals(5.0, ((FloatDataType) dt).getMax().doubleValue(), DELTA);

        //ieee64 field
        df = rcd.getField("ieee64");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof FloatDataType);
        assertEquals("IEEE64", (dt).getEncoding());
        assertNull(((FloatDataType) dt).getMin());
        assertNull(((FloatDataType) dt).getMax());

        //int64 field
        df = rcd.getField("int64");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof IntegerDataType);
        assertEquals("INT64", (dt).getEncoding());
        assertNull(((IntegerDataType) dt).getMin());
        assertNull(((IntegerDataType) dt).getMax());

        //alphanumeric field
        df = rcd.getField("alphanumeric");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof VarcharDataType);
        assertEquals("ALPHANUMERIC(10)", (dt).getEncoding());

        //utf8 field
        df = rcd.getField("utf8");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof VarcharDataType);
        assertEquals("UTF8", (dt).getEncoding());

        //alphanumericString
        df = rcd.getField("alphanumericString");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof VarcharDataType);
        assertEquals("ALPHANUMERIC(10)", (dt).getEncoding());

        //int64Array field
        df = rcd.getField("int64Array");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof ArrayDataType);
        assertNull(dt.getEncoding());
        assertTrue(dt.isNullable());
        dt = ((ArrayDataType) dt).getElementDataType();
        assertTrue(dt instanceof IntegerDataType);
        assertEquals("INT64", (dt).getEncoding());
        assertNull(((IntegerDataType) dt).getMin());
        assertNull(((IntegerDataType) dt).getMax());
        assertTrue(dt.isNullable());

        //int16Array field
        df = rcd.getField("int16Array");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof ArrayDataType);
        assertNull(dt.getEncoding());
        assertFalse(dt.isNullable());
        dt = ((ArrayDataType) dt).getElementDataType();
        assertTrue(dt instanceof IntegerDataType);
        assertEquals("INT16", (dt).getEncoding());
        assertEquals(1, ((IntegerDataType) dt).getMin().intValue());
        assertEquals(99, ((IntegerDataType) dt).getMax().intValue());
        assertTrue(dt.isNullable());

        //int8Array field
        df = rcd.getField("int8Array");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof ArrayDataType);
        assertNull(dt.getEncoding());
        assertFalse(dt.isNullable());
        dt = ((ArrayDataType) dt).getElementDataType();
        assertTrue(dt instanceof IntegerDataType);
        assertEquals("INT8", (dt).getEncoding());
        assertEquals(18, ((IntegerDataType) dt).getMin().intValue());
        assertEquals(99, ((IntegerDataType) dt).getMax().intValue());
        assertFalse(dt.isNullable());

        //ieee64Array field
        df = rcd.getField("ieee64Array");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof ArrayDataType);
        assertNull(dt.getEncoding());
        assertTrue(dt.isNullable());
        dt = ((ArrayDataType) dt).getElementDataType();
        assertTrue(dt instanceof FloatDataType);
        assertEquals("IEEE64", (dt).getEncoding());
        assertNull(((FloatDataType) dt).getMin());
        assertNull(((FloatDataType) dt).getMax());
        assertTrue(dt.isNullable());

        //ieee32Array field
        df = rcd.getField("ieee32Array");
        assertNotNull(df);
        dt = df.getType();
        assertNotNull(dt);
        assertTrue(dt instanceof ArrayDataType);
        assertNull(dt.getEncoding());
        assertFalse(dt.isNullable());
        dt = ((ArrayDataType) dt).getElementDataType();
        assertTrue(dt instanceof FloatDataType);
        assertEquals("IEEE32", (dt).getEncoding());
        assertEquals(- 99.1, ((FloatDataType) dt).getMin().doubleValue(), DELTA);
        assertEquals(98.9, ((FloatDataType) dt).getMax().doubleValue(), DELTA);
        assertFalse(dt.isNullable());
    }


    public static final RecordClassDescriptor rcdCaseInsensitiveClass = new RecordClassDescriptor(
            CaseInsensitiveClass.class.getName(),
            null,
            false,
            null,
            new NonStaticDataField("helloMessage", "real name at class: helloMESSAGE", new VarcharDataType("UTF8", true, true)),
            new NonStaticDataField("size", "real name at class: sIzE", new IntegerDataType("INT32", true)),
            new NonStaticDataField("isLive", "real name at class: IsLiVe", new IntegerDataType("INT32", true)),
            new NonStaticDataField("byteLikeBoolean1", "real name at class: bYtELikEBOOleAn1", new BooleanDataType(true))

    );

    public static class CaseInsensitiveClass extends InstrumentMessage {

        public String helloMESSAGE;

        protected int sIzE;

        private int IsLiVe = - 2147483648;

        protected byte bYtELikEBOOleAn1;


        public int getSiZE () {
            return this.sIzE;
        }

        public void sETsizE (int size) {
            this.sIzE = size;
        }

        public void nuLLiFySizE () {
            this.sIzE = IntegerDataType.INT32_NULL;
        }

        public boolean HASSizE () {
            return this.sIzE != IntegerDataType.INT32_NULL;
        }


        public boolean GeTByteLikeBOOleAn1 () {
            return bYtELikEBOOleAn1 == 1;
        }

        public void sEtBytELikeBooLEan1 (boolean value) {
            this.bYtELikEBOOleAn1 = (byte) (value ? 1 : 0);
        }

        public void nuLLifyByteLIkeBOOlean1 () {
            this.bYtELikEBOOleAn1 = - 1;
        }

        public boolean hAsBYteLikEbooLean1 () {
            return this.bYtELikEBOOleAn1 != - 1;
        }

        @Override
        public String toString () {
            return "CaseInsensitiveClass{" +
                    "helloMESSAGE='" + helloMESSAGE + '\'' +
                    ", sIzE=" + sIzE +
                    ", IsLiVe=" + IsLiVe +
                    ", bYtELikEBOOleAn1=" + bYtELikEBOOleAn1 +
                    '}';
        }
    }

    private void checkRoundTrip (CaseInsensitiveClass inMsg, CaseInsensitiveClass outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.helloMESSAGE, outMsg.helloMESSAGE);

        assertEquals(inMsg.getSiZE(), outMsg.getSiZE());
        assertEquals(inMsg.HASSizE(), outMsg.HASSizE());

        assertEquals(inMsg.GeTByteLikeBOOleAn1(), outMsg.GeTByteLikeBOOleAn1());
        assertEquals(inMsg.hAsBYteLikEbooLean1(), outMsg.hAsBYteLikEbooLean1());

        assertEquals(inMsg.IsLiVe, outMsg.IsLiVe);
    }

    @Test
    public void testCaseInsensitiveClassComp () {
        setUpComp();
        testCaseInsensitiveClass();
    }


    @Test
    public void testCaseInsensitiveClassIntp () {
        setUpIntp();
        testCaseInsensitiveClass();
    }


    private void testCaseInsensitiveClass () {
        final CaseInsensitiveClass msg = new CaseInsensitiveClass();
        for (int i = 0; i < 100; i++) {
            switch (i % 7) {
                case 0:
                    msg.helloMESSAGE = null;
                    msg.nuLLifyByteLIkeBOOlean1();
                    msg.nuLLiFySizE();
                    msg.IsLiVe = - 2147483648;
                    break;
                default:
                    msg.helloMESSAGE = "Hi, Kolya!" + i;
                    msg.sETsizE(i);
                    msg.sEtBytELikeBooLEan1(i % 2 == 0);
                    msg.IsLiVe = - 2147483648;
                    break;
            }
            testBoundRoundTrip(msg, rcdCaseInsensitiveClass);
        }
    }

    @Test
    public void testManagerWithNewFormatComp () throws Exception {
        setUpComp();
        final RecordClassDescriptor rcd = Introspector.createEmptyMessageIntrospector().introspectRecordClass(L2Message.class);
        final L2Message msg = new L2Message();
        for (int i = 0; i < 100; i++) {
            switch (i % 7) {
                case 0:
                    msg.nullifyActions();
                    break;
                default:
                    final ObjectArrayList<Level2ActionInfo> actions = new ObjectArrayList<>(i);
                    for (int j = 0; j < i; j++) {
                        Level2Action action = new Level2Action();
                        action.setAction(BookUpdateAction.UPDATE);
                        action.setSize(i + j);
                        action.setPrice(j - i);
                        action.setIsAsk(j % (i + 1) == 0);
                        action.setNumOfOrders(j);

                        actions.add(action);
                    }

                    msg.setActions(actions);

                    break;
            }
            msg.setMaxDepth((short) i);
            testBoundRoundTrip(msg, rcd);
        }
    }

    private void checkRoundTrip (L2Message inMsg, L2Message outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getExchangeId(), outMsg.getExchangeId());
        assertEquals(inMsg.getMaxDepth(), outMsg.getMaxDepth());
        assertEquals(inMsg.getExchangeId(), outMsg.getExchangeId());
        assertEquals(inMsg.getCurrencyCode(), outMsg.getCurrencyCode());
        assertEquals(inMsg.getSourceId(), outMsg.getSourceId());
        assertEquals(inMsg.getSequenceNumber(), outMsg.getSequenceNumber());
        assertEquals(inMsg.isImplied(), outMsg.isImplied());
        assertEquals(inMsg.isSnapshot(), outMsg.isSnapshot());

        if (inMsg.hasActions()) {
            ObjectArrayList<Level2ActionInfo> inActions = inMsg.getActions();
            ObjectArrayList<Level2ActionInfo> outActions = outMsg.getActions();
            assertEquals(inActions.size(), outActions.size());
            for (int i = 0; i < inActions.size(); i++) {
                Level2ActionInfo a1 = inActions.get(i);
                Level2ActionInfo a2 = outActions.get(i);

                assertEquals(a1.isAsk(), a2.isAsk());
                assertEquals(a1.getNumOfOrders(), a2.getNumOfOrders());
                assertEquals(a1.getAction(), a2.getAction());
                assertEquals(a1.getLevel(), a2.getLevel());
                assertEquals(a1.getPrice(), a2.getPrice(), DELTA);
                assertEquals(a1.getSize(), a2.getSize(), DELTA);
            }
        } else
            assertFalse(outMsg.hasActions());
    }


    public static class TestByteAsBoolean extends InstrumentMessage {

        protected byte isLast;
        protected byte isask;
        protected byte hasByte;
        protected byte flag;

        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        @SchemaElement
        public boolean isLaSt () {
            return isLast == 1;
        }

        public void setIsLAst (boolean value) {
            this.isLast = (byte) (value ? 1 : 0);
        }

        public void nuLLifyIsLasT () {
            this.isLast = - 1;
        }

        public boolean hasISLast () {
            return this.isLast != - 1;
        }

        ///////////////////////////////////
        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        @SchemaElement
        public boolean ISASK () {
            return isask == 1;
        }

        public void setisask (boolean value) {
            this.isask = (byte) (value ? 1 : 0);
        }

        public void nuLLifYisask () {
            this.isask = - 1;
        }

        public boolean hasisaSk () {
            return this.isask != - 1;
        }

        ////////////////////////////////
        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        @SchemaElement
        public boolean getFLAG () {
            return flag == 1;
        }

        public void setflAg (boolean value) {
            this.flag = (byte) (value ? 1 : 0);
        }

        public void nuLLifYFlag () {
            this.flag = - 1;
        }

        public boolean hasflag () {
            return this.flag != - 1;
        }

        ///////////////////////////////////

        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        @SchemaElement
        public boolean HasByte () {
            return hasByte == 1;
        }

        public void setHAsByte (boolean value) {
            this.hasByte = (byte) (value ? 1 : 0);
        }

        public void nuLLifYHASByte () {
            this.hasByte = - 1;
        }

        public boolean hasHASByte () {
            return this.hasByte != - 1;
        }

        ///////////////////////////////////
        @Override
        public String toString () {
            return "TestByteAsBoolean{" +
                    "isLast=" + isLast +
                    ", isask=" + isask +
                    ", hasByte=" + hasByte +
                    ", flag=" + flag +
                    '}';
        }
    }

    @Test
    public void testByteAsBooleanComp () throws Exception {
        setUpComp();
        testByteAsBoolean();
    }

    @Test
    public void testByteAsBooleanIntp () throws Exception {
        setUpIntp();
        testByteAsBoolean();
    }

    private void testByteAsBoolean () throws Exception {
        final RecordClassDescriptor rcd = Introspector.createEmptyMessageIntrospector().introspectRecordClass(TestByteAsBoolean.class);
        final TestByteAsBoolean msg = new TestByteAsBoolean();
        for (int i = 0; i < 100; i++) {
            switch (i % 7) {
                case 0:
                    msg.nuLLifyIsLasT();
                    msg.nuLLifYHASByte();
                    msg.nuLLifYisask();
                    msg.nuLLifYFlag();
                    break;
                default:
                    msg.setflAg(i % 2 == 0);
                    msg.setisask(i % 2 == 0);
                    msg.setHAsByte(i % 2 == 0);
                    msg.setIsLAst(i % 2 == 0);
                    break;
            }

            testBoundRoundTrip(msg, rcd);
        }
    }

    private void checkRoundTrip (TestByteAsBoolean inMsg, TestByteAsBoolean outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.flag, outMsg.flag);
        assertEquals(inMsg.hasByte, outMsg.hasByte);
        assertEquals(inMsg.isask, outMsg.isask);
        assertEquals(inMsg.isLast, outMsg.isLast);
    }

    public static class TestNewFormatBooleanClass extends InstrumentMessage {
        private byte last;

        @SchemaElement (name = "last")
        public byte getLast () {
            return last;
        }

        public void setLast (byte last) {
            this.last = last;
        }

        private byte ask;

        @SchemaElement (name = "ask")
        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        public boolean isAsk () {
            return this.ask == 1;
        }

        public void setAsk (boolean value) {
            this.ask = (byte) (value ? 1 : 0);
        }

        public void nullifyAsk () {
            this.ask = - 1;
        }

        public boolean hasAsk () {
            return this.ask != - 1;
        }

        @Override
        public String toString () {
            return "TestNewFormatBooleanClass{" +
                    "last=" + last +
                    ", ask=" + ask +
                    '}';
        }
    }


    public static final RecordClassDescriptor rcdTestNewFormatBooleanClass = new RecordClassDescriptor(
            TestNewFormatBooleanClass.class.getName(),
            null,
            false,
            null,
            new NonStaticDataField("last", null, new BooleanDataType(true)),
            new NonStaticDataField("ask", null, new BooleanDataType(true))
    );


    @Test
    public void testNewFormatBooleanClassIntp () throws Exception {
        setUpIntp();
        testNewFormatBooleanClass();
    }


    @Test
    public void testNewFormatBooleanClassComp () throws Exception {
        setUpComp();
        testNewFormatBooleanClass();
    }

    private void testNewFormatBooleanClass() throws Exception {
        final TestNewFormatBooleanClass msg = new TestNewFormatBooleanClass();
        for (int i = 0; i < 100; i++) {
            switch (i % 5) {
                case 0:
                    msg.setLast((byte) - 1);
                    msg.nullifyAsk();
                    break;
                case 1:
                    msg.setLast((byte) 0);
                    msg.setAsk(false);
                    break;
                default:
                    msg.setLast((byte) 1);
                    msg.setAsk(true);
                    break;
            }
            testBoundRoundTrip(msg, rcdTestNewFormatBooleanClass);
        }
    }

    private void checkRoundTrip (TestNewFormatBooleanClass inMsg, TestNewFormatBooleanClass outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.ask, outMsg.ask);
        assertEquals(inMsg.last, outMsg.last);
    }

    private void checkRoundTrip (TestNewFormatPublicField inMsg, TestNewFormatPublicField outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.ask, outMsg.ask);
        assertEquals(inMsg.last, outMsg.last);
    }

    public static class TestNewFormatPublicField {
        @SchemaElement (name = "last")
        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        public byte last;

        @SchemaElement (name = "ask")
        public boolean ask;

        @Override
        public String toString () {
            return "TestNewFormatPublicField{" +
                    "last=" + last +
                    ", ask=" + ask +
                    '}';
        }
    }

    public static final RecordClassDescriptor rcdTestNewFormatPublicField = new RecordClassDescriptor(
            TestNewFormatPublicField.class.getName(),
            null,
            false,
            null,
            new NonStaticDataField("last", null, new BooleanDataType(true)),
            new NonStaticDataField("ask", null, new BooleanDataType(false))
    );


    @Test
    public void testNewFormatPublicFieldBooleanClassIntp () throws Exception {
        setUpIntp();
        testNewFormatPublicFieldBooleanClass();
    }

    @Test
    public void testNewFormatPublicFieldBooleanClassComp () throws Exception {
        setUpComp();
        testNewFormatPublicFieldBooleanClass();
    }

    private void testNewFormatPublicFieldBooleanClass() throws Exception {
        final TestNewFormatPublicField msg = new TestNewFormatPublicField();
        for (int i = 0; i < 100; i++) {
            switch (i % 5) {
                case 0:
                    msg.last = ((byte) - 1);
                    msg.ask = false;
                    break;
                case 1:
                    msg.last = ((byte) 0);
                    msg.ask = (false);
                    break;
                default:
                    msg.last = ((byte) 1);
                    msg.ask = (true);
                    break;
            }
            testBoundRoundTrip(msg, rcdTestNewFormatPublicField);
        }
    }



    public enum TestMappingEnum {
        VALUE_A,    // value 0
        VALUE_B,    // value 1
        VALUE_C,    // value 2
        VALUE_D     // value 3
    }

    final static EnumClassDescriptor ecdCorrectSchema = new EnumClassDescriptor(
            TestMappingEnum.class.getName(),
            "Correct enum schema",
            "VALUE_A", "VALUE_B", "VALUE_C", "VALUE_D"
    );

    final static EnumClassDescriptor ecdIncorrectSchemaSequence = new EnumClassDescriptor(
            TestMappingEnum.class.getName(),
            "Incorrect enum schema",
            false,
            new EnumValue("VALUE_A", 2),
            new EnumValue("VALUE_B", 3),
            new EnumValue("VALUE_C", 4),
            new EnumValue("VALUE_D", 0)
    );

    final static EnumClassDescriptor ecdNotFullSchema = new EnumClassDescriptor(
            TestMappingEnum.class.getName(),
            "Schema without VALUE_B and VALUE_D",
            false,
            new EnumValue("VALUE_A", 2),
            new EnumValue("VALUE_C", 4)
    );

    final static EnumClassDescriptor ecdNotFullClass = new EnumClassDescriptor(
            TestMappingEnum.class.getName(),
            "Schema contains more elements than class",
            false,
            new EnumValue("VALUE_A", 2),
            new EnumValue("VALUE_B", 3),
            new EnumValue("VALUE_C", 4),
            new EnumValue("VALUE_D", 0),
            new EnumValue("VALUE_E", 1),
            new EnumValue("VALUE_I", 5)
    );

    public static class TestClassWithEnums extends InstrumentMessage {
        // isNullable = false
        public TestMappingEnum enum1;

        public TestMappingEnum enum2;

        // isNullable = false
        private TestMappingEnum enum3;

        private TestMappingEnum enum4;

        public TestMappingEnum getEnum3 () {
            return enum3;
        }

        public void setEnum3 (TestMappingEnum enum3) {
            this.enum3 = enum3;
        }

        public TestMappingEnum getEnum4 () {
            return enum4;
        }

        public void setEnum4 (TestMappingEnum enum4) {
            this.enum4 = enum4;
        }

        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("TestClassWithEnums{");
            sb.append("enum1=").append(enum1);
            sb.append(", enum2=").append(enum2);
            sb.append(", enum3=").append(enum3);
            sb.append(", enum4=").append(enum4);
            sb.append('}');
            return sb.toString();
        }
    }
    public static class TestClassWithEnumsNumber extends InstrumentMessage {
        // isNullable = false
        public long enum1;

        public long enum2;

        // isNullable = false
        private long enum3;

        private long enum4;

        public long getEnum3 () {
            return enum3;
        }

        public void setEnum3 (long enum3) {
            this.enum3 = enum3;
        }

        public long getEnum4 () {
            return enum4;
        }

        public void setEnum4 (long enum4) {
            this.enum4 = enum4;
        }

        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("TestClassWithEnums{");
            sb.append("enum1=").append(enum1);
            sb.append(", enum2=").append(enum2);
            sb.append(", enum3=").append(enum3);
            sb.append(", enum4=").append(enum4);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class TestClassWithEnumsCharSequence extends InstrumentMessage {
        // isNullable = false
        public CharSequence enum1;

        public CharSequence enum2;

        // isNullable = false
        private CharSequence enum3;

        private CharSequence enum4;

        public CharSequence getEnum3 () {
            return enum3;
        }

        public void setEnum3 (CharSequence enum3) {
            this.enum3 = enum3;
        }

        public CharSequence getEnum4 () {
            return enum4;
        }

        public void setEnum4 (CharSequence enum4) {
            this.enum4 = enum4;
        }

        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("TestClassWithEnumsCharSequence{");
            sb.append("enum1=").append(enum1);
            sb.append(", enum2=").append(enum2);
            sb.append(", enum3=").append(enum3);
            sb.append(", enum4=").append(enum4);
            sb.append('}');
            return sb.toString();
        }
    }

    public static RecordClassDescriptor getRCDTestClassWithEnums (String className, EnumClassDescriptor ecd) {
        return new RecordClassDescriptor(
                className,
                "title",
                false,
                null,
                new NonStaticDataField("enum1", null, new EnumDataType(false, ecd)),
                new NonStaticDataField("enum2", null, new EnumDataType(true, ecd)),
                new NonStaticDataField("enum3", null, new EnumDataType(false, ecd)),
                new NonStaticDataField("enum4", null, new EnumDataType(true, ecd))
        );
    }
    private void checkRoundTrip (TestClassWithEnums inMsg, TestClassWithEnums outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.enum1, outMsg.enum1);
        assertEquals(inMsg.enum2, outMsg.enum2);
        assertEquals(inMsg.enum3, outMsg.enum3);
        assertEquals(inMsg.enum4, outMsg.enum4);
    }

    private void checkRoundTrip (TestClassWithEnumsCharSequence inMsg, TestClassWithEnumsCharSequence outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.enum1, outMsg.enum1);
        assertEquals(inMsg.enum2, outMsg.enum2);
        assertEquals(inMsg.enum3, outMsg.enum3);
        assertEquals(inMsg.enum4, outMsg.enum4);
    }

    private void checkRoundTrip (TestClassWithEnumsNumber inMsg, TestClassWithEnumsNumber outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.enum1, outMsg.enum1);
        assertEquals(inMsg.enum2, outMsg.enum2);
        assertEquals(inMsg.enum3, outMsg.enum3);
        assertEquals(inMsg.enum4, outMsg.enum4);
    }

    @Test
    public void testEnumMappingCorrectMappingComp () {
        setUpComp();
        final RecordClassDescriptor rcd1 = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdCorrectSchema);
        final RecordClassDescriptor rcd2 = getRCDTestClassWithEnums(TestClassWithEnumsCharSequence.class.getName(), ecdCorrectSchema);
        testEnumRoundtrip(rcd1, rcd2);
    }

    @Test
    public void testEnumMappingCorrectMappingIntp () {
        setUpIntp();
        final RecordClassDescriptor rcd1 = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdCorrectSchema);
        final RecordClassDescriptor rcd2 = getRCDTestClassWithEnums(TestClassWithEnumsCharSequence.class.getName(), ecdCorrectSchema);
        testEnumRoundtrip(rcd1, rcd2);
    }


    @Test
    public void testEnumMappingIncorrectMappingSequenceComp () {
        setUpComp();
        final RecordClassDescriptor rcd1 = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdIncorrectSchemaSequence);
        final RecordClassDescriptor rcd2 = getRCDTestClassWithEnums(TestClassWithEnumsCharSequence.class.getName(), ecdIncorrectSchemaSequence);
        testEnumRoundtrip(rcd1, rcd2);
    }

    @Test
    public void testEnumMappingIncorrectMappingSequenceIntp () {
        setUpIntp();
        final RecordClassDescriptor rcd1 = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdIncorrectSchemaSequence);
        final RecordClassDescriptor rcd2 = getRCDTestClassWithEnums(TestClassWithEnumsCharSequence.class.getName(), ecdIncorrectSchemaSequence);
        testEnumRoundtrip(rcd1, rcd2);
    }


    @Test
    public void testEnumMappingNotFullEnumSchemaComp () {
        setUpComp();
        final RecordClassDescriptor rcd1 = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdNotFullSchema);
        final RecordClassDescriptor rcd2 = getRCDTestClassWithEnums(TestClassWithEnumsCharSequence.class.getName(), ecdNotFullSchema);
        testNotFullClassWritingValuesFromSchemaRoundtrip(rcd1, rcd2);
    }



    @Test
    public void testEnumMappingNotFullEnumSchemaIntp () {
        setUpIntp();
        final RecordClassDescriptor rcd1 = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdNotFullSchema);
        final RecordClassDescriptor rcd2 = getRCDTestClassWithEnums(TestClassWithEnumsCharSequence.class.getName(), ecdNotFullSchema);
        testNotFullClassWritingValuesFromSchemaRoundtrip(rcd1, rcd2);
    }

    private void testNotFullClassWritingValuesFromSchemaRoundtrip(RecordClassDescriptor rcd1, RecordClassDescriptor rcd2) {
        final TestClassWithEnums msg1 = new TestClassWithEnums();
        final TestClassWithEnumsCharSequence msg2 = new TestClassWithEnumsCharSequence();
        for (int i = 0; i < 100; i++) {
            switch (i % 6) {
                case 0:
                    msg1.enum2 = null;
                    msg1.enum4 = null;

                    msg2.enum2 = null;
                    msg2.enum4 = null;

                    msg1.enum1 = TestMappingEnum.VALUE_A;
                    msg1.enum3 = TestMappingEnum.VALUE_A;

                    msg2.enum1 = "VALUE_C";
                    msg2.enum3 = "VALUE_C";
                    break;

                default:
                    msg1.enum1 = i % 2 == 0 ? TestMappingEnum.VALUE_A : TestMappingEnum.VALUE_C;
                    msg1.enum2 = i % 2 == 0 ? TestMappingEnum.VALUE_A : TestMappingEnum.VALUE_C;
                    msg1.enum3 = i % 2 == 0 ? TestMappingEnum.VALUE_C : TestMappingEnum.VALUE_A;
                    msg1.enum4 = i % 2 == 0 ? TestMappingEnum.VALUE_A : TestMappingEnum.VALUE_A;

                    msg2.enum1 = i % 2 == 0 ? "VALUE_A" : "VALUE_C";
                    msg2.enum2 = i % 2 == 0 ? "VALUE_A" : "VALUE_C";
                    msg2.enum3 = i % 2 == 0 ? "VALUE_C" : "VALUE_A";
                    msg2.enum4 = i % 2 == 0 ? "VALUE_C" : "VALUE_A";
                    break;

            }
            testBoundRoundTrip(msg1, rcd1);
            testBoundRoundTrip(msg2, rcd2);
        }
    }

    @Test
    public void testNotFullEnumClassWritingWithExceptionComp() {
        setUpComp();
        testNotFullEnumClassWritingWithException();
    }

    @Test
    public void testNotFullEnumClassWritingWithExceptionIntp() {
        setUpIntp();
        testNotFullEnumClassWritingWithException();
    }

    private void testNotFullEnumClassWritingWithException() {
        final RecordClassDescriptor rcd = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdNotFullSchema);
        final TestClassWithEnums msg = new TestClassWithEnums();
        msg.enum1 = TestMappingEnum.VALUE_B;
        try {
            boundRoundTrip(msg, rcd);
            fail("Cannot write enuma values that doesn't exists in schema");
        } catch (IllegalArgumentException ex) {
            assertEquals("value is absent in schema: enum1 == VALUE_B", ex.getMessage());
        }
        msg.enum1 = TestMappingEnum.VALUE_A;
        msg.enum3 = TestMappingEnum.VALUE_D;
        try {
            boundRoundTrip(msg, rcd);
            fail("Cannot write enuma values that doesn't exists in schema");
        } catch (IllegalArgumentException ex) {
            assertEquals("value is absent in schema: enum3 == VALUE_D", ex.getMessage());
        }
    }

    @Test
    public void testEnumMappingNotFullEnumClassDecoderExceptionComp () {
        setUpComp();
        testEnumMappingNotFullEnumClassDecoderException();
    }

    @Test
    public void testEnumMappingNotFullEnumClassDecoderExceptionIntp () {
        setUpIntp();
        testEnumMappingNotFullEnumClassDecoderException();
    }

    private void testEnumMappingNotFullEnumClassDecoderException() {
        final RecordClassDescriptor rcd = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdNotFullClass);
        final TestClassWithEnums msg = new TestClassWithEnums();
        msg.enum1 = TestMappingEnum.VALUE_A;
        msg.enum2 = TestMappingEnum.VALUE_A;
        msg.enum3 = TestMappingEnum.VALUE_A;
        msg.enum4 = TestMappingEnum.VALUE_A;
        try {
            boundRoundTrip(msg, rcd);
            fail("Cannot decode enum into class that doesn't contains all schema values.");
        } catch (IllegalArgumentException ex) {
            assertEquals("Class " + TestMappingEnum.class.getName() + " must contains all schema values for decoding!", ex.getMessage());
        } catch (RuntimeException ex) {
            assertEquals("java.lang.IllegalArgumentException: Class " + TestMappingEnum.class.getName() + " must contains all schema values for decoding!", ex.getMessage());
        }
    }

    private void testEnumRoundtrip (RecordClassDescriptor rcd1, RecordClassDescriptor rcd2) {
        final TestClassWithEnums msg1 = new TestClassWithEnums();
        final TestClassWithEnumsCharSequence msg2 = new TestClassWithEnumsCharSequence();
        for (int i = 0; i < 100; i++) {
            switch (i % 6) {
                case 0:
                    msg1.enum2 = null;
                    msg1.enum4 = null;

                    msg2.enum2 = null;
                    msg2.enum4 = null;

                    msg1.enum1 = TestMappingEnum.VALUE_D;
                    msg1.enum3 = TestMappingEnum.VALUE_D;

                    msg2.enum1 = "VALUE_D";
                    msg2.enum3 = "VALUE_D";
                    break;

                case 1:
                    msg1.enum1 = i % 2 == 0 ? TestMappingEnum.VALUE_A : TestMappingEnum.VALUE_B;
                    msg1.enum2 = i % 2 == 0 ? TestMappingEnum.VALUE_B : TestMappingEnum.VALUE_C;
                    msg1.enum3 = i % 2 == 0 ? TestMappingEnum.VALUE_C : TestMappingEnum.VALUE_D;
                    msg1.enum4 = i % 2 == 0 ? TestMappingEnum.VALUE_D : TestMappingEnum.VALUE_A;

                    msg2.enum1 = i % 2 == 0 ? "VALUE_A" : "VALUE_B";
                    msg2.enum2 = i % 2 == 0 ? "VALUE_B" : "VALUE_C";
                    msg2.enum3 = i % 2 == 0 ? "VALUE_C" : "VALUE_D";
                    msg2.enum4 = i % 2 == 0 ? "VALUE_D" : "VALUE_A";
                    break;
                case 2:
                    msg1.enum1 = i % 2 == 0 ? TestMappingEnum.VALUE_B : TestMappingEnum.VALUE_C;
                    msg1.enum2 = i % 2 == 0 ? TestMappingEnum.VALUE_C : TestMappingEnum.VALUE_D;
                    msg1.enum3 = i % 2 == 0 ? TestMappingEnum.VALUE_D : TestMappingEnum.VALUE_A;
                    msg1.enum4 = i % 2 == 0 ? TestMappingEnum.VALUE_A : TestMappingEnum.VALUE_B;

                    msg2.enum1 = i % 2 == 0 ? "VALUE_B" : "VALUE_C";
                    msg2.enum2 = i % 2 == 0 ? "VALUE_C" : "VALUE_D";
                    msg2.enum3 = i % 2 == 0 ? "VALUE_D" : "VALUE_A";
                    msg2.enum4 = i % 2 == 0 ? "VALUE_A" : "VALUE_B";
                    break;
                case 3:
                    msg1.enum1 = i % 2 == 0 ? TestMappingEnum.VALUE_C : TestMappingEnum.VALUE_D;
                    msg1.enum2 = i % 2 == 0 ? TestMappingEnum.VALUE_D : TestMappingEnum.VALUE_A;
                    msg1.enum3 = i % 2 == 0 ? TestMappingEnum.VALUE_A : TestMappingEnum.VALUE_B;
                    msg1.enum4 = i % 2 == 0 ? TestMappingEnum.VALUE_B : TestMappingEnum.VALUE_C;

                    msg2.enum1 = i % 2 == 0 ? "VALUE_C" : "VALUE_D";
                    msg2.enum2 = i % 2 == 0 ? "VALUE_D" : "VALUE_A";
                    msg2.enum3 = i % 2 == 0 ? "VALUE_A" : "VALUE_B";
                    msg2.enum4 = i % 2 == 0 ? "VALUE_B" : "VALUE_C";
                    break;

                default:
                    msg1.enum1 = i % 2 == 0 ? TestMappingEnum.VALUE_D : TestMappingEnum.VALUE_A;
                    msg1.enum2 = i % 2 == 0 ? TestMappingEnum.VALUE_A : TestMappingEnum.VALUE_B;
                    msg1.enum3 = i % 2 == 0 ? TestMappingEnum.VALUE_B : TestMappingEnum.VALUE_C;
                    msg1.enum4 = i % 2 == 0 ? TestMappingEnum.VALUE_C : TestMappingEnum.VALUE_D;

                    msg2.enum1 = i % 2 == 0 ? "VALUE_D" : "VALUE_A";
                    msg2.enum2 = i % 2 == 0 ? "VALUE_A" : "VALUE_B";
                    msg2.enum3 = i % 2 == 0 ? "VALUE_B" : "VALUE_C";
                    msg2.enum4 = i % 2 == 0 ? "VALUE_C" : "VALUE_D";
                    break;
            }
            testBoundRoundTrip(msg1, rcd1);
            testBoundRoundTrip(msg2, rcd2);
        }
    }

    @Test
    public void testEnumIntegerMappingComp() {
        setUpComp();
        testEnumIntegerMapping();
    }

    @Test
    public void testEnumIntegerMappingIntp() {
        setUpIntp();
        testEnumIntegerMapping();
    }

    private void testEnumIntegerMapping() {
        final RecordClassDescriptor rcd = getRCDTestClassWithEnums(TestClassWithEnumsNumber.class.getName(), ecdCorrectSchema);
        final TestClassWithEnumsNumber msg = new TestClassWithEnumsNumber();
        for (int i = 0; i <  100; i++) {
            msg.enum1 =  i % 2 == 0 ? 2 : 1;
            msg.enum2 =  i % 2 == 0 ? 0 : 3;
            msg.enum3 =  i % 2 == 0 ? 2 : 0;
            msg.enum4 =  i % 2 == 0 ? 3 : 0;

            testBoundRoundTrip(msg, rcd);
        }
    }

    @Test
    public void testEnumMappingReadEnumValueWithIncorrectValueFromTBDComp () {
        setUpComp();
        testReadEnumValueWithIncorrectValueFromTBD();
    }

    @Test
    public void testEnumMappingReadEnumValueWithIncorrectValueFromTBDIntp() {
        setUpIntp();
        testReadEnumValueWithIncorrectValueFromTBD();
    }


    private void testReadEnumValueWithIncorrectValueFromTBD () {
        final TestClassWithEnumsCharSequence inMsg = new TestClassWithEnumsCharSequence();
        final RecordClassDescriptor rcdBigEnum = getRCDTestClassWithEnums(TestClassWithEnumsCharSequence.class.getName(), ecdNotFullClass);
        final RecordClassDescriptor rcdSmallEnum = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdNotFullSchema);

        inMsg.enum1 = "VALUE_I"; // value = 5
        inMsg.enum3 = "VALUE_I"; // value = 5

        FixedBoundEncoder benc =
                factory.createFixedBoundEncoder(CL, rcdBigEnum);

        MemoryDataOutput out = new MemoryDataOutput();

        benc.encode(inMsg, out);

        MemoryDataInput in = new MemoryDataInput(out);

        BoundDecoder bdec =
                factory.createFixedBoundDecoder(CL, rcdSmallEnum);
        try {
            bdec.decode(in);
            fail("Cannot successfully read incorrect value from Timabase!");
        } catch (IllegalArgumentException ex) {
            assertEquals("value is out of range 5", ex.getMessage());
        } catch (RuntimeException ex) {
            assertEquals("value is out of range 5", ex.getCause().getMessage());
        }

        inMsg.enum1 = "VALUE_B"; // value = 3
        inMsg.enum3 = "VALUE_B"; // value = 3

        out = new MemoryDataOutput();
        benc.encode(inMsg, out);
        in = new MemoryDataInput(out);

        try {
            bdec.decode(in);
            fail("Cannot successfully read incorrect value from Timabase!");
        } catch (IllegalArgumentException ex) {
            assertEquals("value is out of range 3", ex.getMessage());
        } catch (RuntimeException ex) {
            assertEquals("value is out of range 3", ex.getCause().getMessage());
        }
    }


    @Test
    public void testEnumMappingWithLongValuesComp() {
        setUpComp();
        testEnumMappingWithLongValues();
    }

    @Test
    public void testEnumMappingWithLongValuesIntp() {
        setUpIntp();
        testEnumMappingWithLongValues();
    }


    private void testEnumMappingWithLongValues() {
        long v1 = 2147483850L;
        long v2 = 2147483851L;
        final RecordClassDescriptor rcd = getRCDTestClassWithEnums(TestClassWithEnumsNumber.class.getName(), rcdEnumLongValues);
        final TestClassWithEnumsNumber msg = new TestClassWithEnumsNumber();
        for (int i = 0; i <  100; i++) {
            msg.enum1 =  i % 2 == 0 ? v2 : v1;
            msg.enum2 =  i % 2 == 0 ? (-1) : v2;
            msg.enum3 =  i % 2 == 0 ? v1 : v2;
            msg.enum4 =  i % 2 == 0 ? v1 : (-1);

            testBoundRoundTrip(msg, rcd);
        }
    }

    private static final EnumClassDescriptor rcdEnumLongValues = new EnumClassDescriptor(
            TestMappingEnum.class.getName(),
            "Schema with VERY BIG ENUM VALUES",
            false,
            new EnumValue("VALUE_A", 2147483850L),
            new EnumValue("VALUE_C", 2147483851L)
    );

    @Test
    public void testEnumMappingWithLongValuesWithExceptionComp() {
        setUpComp();
        testEnumMappingWithLongValuesWithException();
    }

    @Test
    public void testEnumMappingWithLongValuesWithExceptionIntrp() {
        setUpIntp();
        testEnumMappingWithLongValuesWithException();
    }

    private void testEnumMappingWithLongValuesWithException() {
        TestMappingEnum v1 = TestMappingEnum.VALUE_A;
        TestMappingEnum v2 = TestMappingEnum.VALUE_C;
        final RecordClassDescriptor rcd = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), rcdEnumLongValues);
        final TestClassWithEnums msg = new TestClassWithEnums();
        for (int i = 0; i <  100; i++) {
            msg.enum1 =  i % 2 == 0 ? v2 : v1;
            msg.enum2 =  i % 2 == 0 ? (null) : v2;
            msg.enum3 =  i % 2 == 0 ? v1 : v2;
            msg.enum4 =  i % 2 == 0 ? v1 : (null);

            testBoundRoundTrip(msg, rcd);
        }
    }


    public static class TestEnumMessage extends InstrumentMessage {
        public long enumValue1;
        public long enumValue2;

        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("TestEnumMessage{");
            sb.append("enumValue1=").append(enumValue1);
            sb.append(", enumValue2=").append(enumValue2);
            sb.append('}');
            return sb.toString();
        }
    }

    private static final EnumClassDescriptor rcdEnumLongValues1 = new EnumClassDescriptor(
            "TestEnumClass",
            "Schema with VERY BIG ENUM VALUES",
            false,
            new EnumValue("VALUE_A", 2147483850L),
            new EnumValue("VALUE_C", 2147483851L)
    );

    public static final RecordClassDescriptor rcdTestEnumMessage = new RecordClassDescriptor(
            TestEnumMessage.class.getName(),
            null,
            false,
            null,
            new NonStaticDataField("enumValue1", null, new EnumDataType(true, rcdEnumLongValues1)),
            new NonStaticDataField("enumValue2", null, new EnumDataType(false, rcdEnumLongValues1))
    );

    @Test
    public void testEnumNumbersComp() {
        setUpComp();
        testEnumNumbers();
    }
    @Test
    public void testEnumNumbersIntp() {
        setUpIntp();
        testEnumNumbers();
    }

    private void testEnumNumbers() {
        TestEnumMessage inMsg = new TestEnumMessage();
        inMsg.enumValue1 = 2147483850L;
        inMsg.enumValue2 = 2147483851L;

        FixedBoundEncoder benc =
                factory.createFixedBoundEncoder(CL, rcdTestEnumMessage);

        MemoryDataOutput out = new MemoryDataOutput();

        benc.encode(inMsg, out);

        MemoryDataInput in = new MemoryDataInput(out);

        BoundDecoder bdec =
                factory.createFixedBoundDecoder(CL, rcdTestEnumMessage);
        TestEnumMessage outMsg = (TestEnumMessage) bdec.decode(in);
        assertEquals(inMsg.toString(), outMsg.toString());
    }


    final static EnumClassDescriptor ecdShortValues = new EnumClassDescriptor(
            TestMappingEnum.class.getName(),
            "Incorrect enum schema",
            false,
            new EnumValue("VALUE_A", 502),
            new EnumValue("VALUE_B", 503),
            new EnumValue("VALUE_C", 504),
            new EnumValue("VALUE_D", 505)
    );

    @Test
    public void testEnumMappingShortValueComp () {
        setUpComp();
        final RecordClassDescriptor rcd1 = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdShortValues);
        final RecordClassDescriptor rcd2 = getRCDTestClassWithEnums(TestClassWithEnumsCharSequence.class.getName(), ecdShortValues);
        testEnumRoundtrip(rcd1, rcd2);
    }

    @Test
    public void testEnumMappingShortValueIntp () {
        setUpIntp();
        final RecordClassDescriptor rcd1 = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdShortValues);
        final RecordClassDescriptor rcd2 = getRCDTestClassWithEnums(TestClassWithEnumsCharSequence.class.getName(), ecdShortValues);
        testEnumRoundtrip(rcd1, rcd2);
    }

    final static EnumClassDescriptor ecdIntValues = new EnumClassDescriptor(
            TestMappingEnum.class.getName(),
            "Incorrect enum schema",
            false,
            new EnumValue("VALUE_A", 51000),
            new EnumValue("VALUE_B", 53000),
            new EnumValue("VALUE_C", 54000),
            new EnumValue("VALUE_D", 55000)
    );

    @Test
    public void testEnumMappingIntValueComp () {
        setUpComp();
        final RecordClassDescriptor rcd1 = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdIntValues);
        final RecordClassDescriptor rcd2 = getRCDTestClassWithEnums(TestClassWithEnumsCharSequence.class.getName(), ecdIntValues);
        testEnumRoundtrip(rcd1, rcd2);
    }

    @Test
    public void testEnumMappingUntValueIntp () {
        setUpIntp();
        final RecordClassDescriptor rcd1 = getRCDTestClassWithEnums(TestClassWithEnums.class.getName(), ecdIntValues);
        final RecordClassDescriptor rcd2 = getRCDTestClassWithEnums(TestClassWithEnumsCharSequence.class.getName(), ecdIntValues);
        testEnumRoundtrip(rcd1, rcd2);
    }

}
