package deltix.qsrv.hf.pub;


import deltix.qsrv.hf.pub.codec.*;
import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.tickdb.testframework.TestAllTypesMessagePrivate;
import deltix.timebase.api.messages.*;
import deltix.timebase.api.messages.service.*;
import deltix.timebase.messages.SchemaElement;
import deltix.timebase.messages.*;
import deltix.timebase.messages.service.*;
import deltix.timebase.api.messages.calendar.*;
import deltix.timebase.api.messages.securities.*;
import deltix.timebase.messages.service.*;
import deltix.timebase.api.messages.trade.*;
import deltix.timebase.api.messages.universal.*;
import deltix.util.JUnitCategories;
import deltix.util.collections.generated.ByteArrayList;
import deltix.util.collections.generated.IntegerArrayList;
import deltix.util.collections.generated.ObjectArrayList;
import deltix.util.lang.Util;
import deltix.util.memory.MemoryDataInput;
import deltix.util.memory.MemoryDataOutput;
import net.sourceforge.stripes.util.ResolverUtil;
import org.junit.ComparisonFailure;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests new message format
 */
@Category(JUnitCategories.TickDBCodecs.class)
public class Test_RecordCodecs8 {

    private static final float DELTA = (float) 0.000001;

    private CodecFactory factory;

    private void setUpComp () {
        factory = CodecFactory.newCompiledCachingFactory();
    }

    private void setUpIntp () {
        factory = CodecFactory.newInterpretingCachingFactory();
    }

    private final static TypeLoader CL = TypeLoaderImpl.DEFAULT_INSTANCE;

    public enum TestEnum {
        RED, BLACK, WHITE, GREEN
    }

    public static final EnumClassDescriptor rcdTestEnum = new EnumClassDescriptor(
            TestEnum.class.getName(),
            "Test enum for using in overriding tests",
            "RED", "BLACK", "WHITE", "GREEN"
    );

    public static final RecordClassDescriptor rcdTestPublicClassNotStatic = new RecordClassDescriptor(
            TestPublicClass.class.getName(),
            "Not overriding class",
            false,
            null,
            new NonStaticDataField("isAsk", "original public field that override in parent ", new BooleanDataType(false)),
            new NonStaticDataField("isLast", "original public field that override in parent", new BooleanDataType(true)),
            new NonStaticDataField("isLast2", "original public field that override in parent", new BooleanDataType(false)),
            new NonStaticDataField("int32", "original public field that override in parent", new IntegerDataType("INT32", true)),
            new NonStaticDataField("testEnum", "original public field that override in parent", new EnumDataType(true, rcdTestEnum)),

            new NonStaticDataField("isAsk_n", "original public field that NOT override in parent ", new BooleanDataType(false)),
            new NonStaticDataField("isLast_n", "original public field that NOT override in parent", new BooleanDataType(true)),
            new NonStaticDataField("isLast2_n", "original public field that NOT override in parent", new BooleanDataType(false)),
            new NonStaticDataField("int32_n", "original public field that NOT override in parent", new IntegerDataType("INT32", true)),
            new NonStaticDataField("testEnum_n", "original public field that NOT override in parent", new EnumDataType(true, rcdTestEnum))
    );

    public static class TestPublicClass extends InstrumentMessage {
        public boolean isAsk;

        public byte isLast;

        public byte isLast2;

        public int int32;

        public TestEnum testEnum;


        public boolean isAsk_n;

        public byte isLast_n;

        public byte isLast2_n;

        public int int32_n;

        public TestEnum testEnum_n;

        @Override
        public String toString () {
            return "TestPublicClass{" +
                    "isAsk=" + isAsk +
                    ", isLast=" + isLast +
                    ", isLast2=" + isLast2 +
                    ", int32=" + int32 +
                    ", testEnum=" + testEnum +
                    ", isAsk_n=" + isAsk_n +
                    ", isLast_n=" + isLast_n +
                    ", isLast2_n=" + isLast2_n +
                    ", int32_n=" + int32_n +
                    ", testEnum_n=" + testEnum_n +
                    '}';
        }
    }

    public static class TestOverrideClass extends TestPublicClass {
        @SchemaElement (name = "isAsk")
        public boolean isAsk;

        @SchemaElement (name = "isLast")
        public byte isLast;

        @SchemaElement (name = "int32")
        public int int32;

        @SchemaElement (name = "testEnum")
        public CharSequence testEnum;

        @SchemaElement (name = "isLast2")
        public byte isLast2;


        @Override
        public String toString () {
            return "TestOverrideClass{" +
                    "isAsk=" + isAsk +
                    ", isLast=" + isLast +
                    ", int32=" + int32 +
                    ", testEnum=" + testEnum +
                    ", isLast2=" + isLast2 +
                    '}';
        }
    }


    private Object boundRoundTrip (Object inMsg, RecordClassDescriptor cd, TypeLoader loader) {
        FixedBoundEncoder benc =
                factory.createFixedBoundEncoder(loader, cd);

        MemoryDataOutput out = new MemoryDataOutput();

        benc.encode(inMsg, out);

        MemoryDataInput in = new MemoryDataInput(out);

        Object outMsg = boundDecode(in, cd, loader);

        return (outMsg);
    }

    private Object boundDecode (
            MemoryDataInput in,
            RecordClassDescriptor cd,
            TypeLoader loader
    ) {
        BoundDecoder bdec =
                factory.createFixedBoundDecoder(loader, cd);

        return (bdec.decode(in));
    }


    private void testBoundRoundTrip (Object inMsg, RecordClassDescriptor rcd, TypeLoader loader) {
        if (inMsg instanceof TestOverrideClass) {
            TestOverrideClass outMsg = (TestOverrideClass) boundRoundTrip(inMsg, rcd, loader);
            checkRoundTrip((TestOverrideClass) inMsg, outMsg);
        } else if (inMsg instanceof TestBooleansProperties) {
            TestBooleansProperties outMsg = (TestBooleansProperties) boundRoundTrip(inMsg, rcd, loader);
            checkRoundTrip((TestBooleansProperties) inMsg, outMsg);
        } else if (inMsg instanceof AllStaticTypesClass) {
            AllStaticTypesClass outMsg = (AllStaticTypesClass) boundRoundTrip(inMsg, rcd, loader);
            checkRoundTrip((AllStaticTypesClass) inMsg, outMsg);
        } else if (inMsg instanceof TestAllTypesMessagePrivate) {
            TestAllTypesMessagePrivate outMsg = (TestAllTypesMessagePrivate) boundRoundTrip(inMsg, rcd, loader);
            checkRoundTrip((TestAllTypesMessagePrivate) inMsg, outMsg);
        } else if (inMsg instanceof TestCodecBinding) {
            TestCodecBinding outMsg = (TestCodecBinding) boundRoundTrip(inMsg, rcd, loader);
            checkRoundTrip((TestCodecBinding) inMsg, outMsg);
        } else if (inMsg instanceof TestClassForIntrospection) {
            TestClassForIntrospection outMsg = (TestClassForIntrospection) boundRoundTrip(inMsg, rcd, loader);
            checkRoundTrip((TestClassForIntrospection) inMsg, outMsg);
        } else if (inMsg instanceof NotCompileClass) {
            NotCompileClass outMsg = (NotCompileClass) boundRoundTrip(inMsg, rcd, loader);
            checkRoundTrip((NotCompileClass) inMsg, outMsg);
        } else if (inMsg instanceof PackageHeader) {
            PackageHeader outMsg = (PackageHeader) boundRoundTrip(inMsg, rcd, loader);
            checkRoundTrip((PackageHeader) inMsg, outMsg);
        } else if (inMsg instanceof BaseEntry) {
            BaseEntry outMsg = (BaseEntry)  boundRoundTrip(inMsg, rcd, loader);
            checkBaseEntry((BaseEntry) inMsg, outMsg);
        } else if (inMsg instanceof BarMsg) {
            BarMsg outMsg = (BarMsg)  boundRoundTrip(inMsg, rcd, loader);
            checkBaseEntry((BarMsg) inMsg, outMsg);
        } else if (inMsg instanceof BarMessage) {
            BarMessage outMsg = (BarMessage)  boundRoundTrip(inMsg, rcd, loader);
            checkBaseEntry((BarMessage) inMsg, outMsg);
        }

    }

    @org.junit.Test
    public void testOverridePublicFiledsInCustomClassIntp () {
        setUpIntp();
        testOverridePublicFiledsInCustomClass();
    }

    @org.junit.Test
    public void testOverridePublicFiledsInCustomClassComp () {
        setUpComp();
        testOverridePublicFiledsInCustomClass();
    }

    private void testOverridePublicFiledsInCustomClass () {
        final TestOverrideClass msg = new TestOverrideClass();
        final SimpleTypeLoader typeLoader = new SimpleTypeLoader(rcdTestPublicClassNotStatic.getName(), TestOverrideClass.class);
        for (int i = 0; i < 1000; i++) {
            switch (i % 7) {
                case 0:
                    msg.int32 = IntegerDataType.INT32_NULL;
                    msg.int32_n = IntegerDataType.INT32_NULL;

                    msg.isAsk = false;
                    msg.isAsk_n = false;

                    msg.isLast = - 1;
                    msg.isLast_n = - 1;

                    msg.isLast2 = 0;
                    msg.isLast2_n = 1;

                    msg.testEnum = null;
                    msg.testEnum_n = null;
                    break;
                default:
                    msg.int32 = i;
                    msg.int32_n = i;

                    msg.isAsk = i % 2 == 0;
                    msg.isAsk_n = i % 2 != 0;

                    msg.isLast2 = (byte) (i % 2);
                    msg.isLast2_n = (byte) (i % 2);

                    msg.isLast = (byte) (i % 2);
                    msg.isLast_n = (byte) (i % 2);

                    msg.testEnum = msg.isAsk ? "BLACK" : "WHITE";
                    msg.testEnum_n = msg.isAsk_n ? TestEnum.RED : TestEnum.GREEN;
                    break;
            }
            testBoundRoundTrip(msg, rcdTestPublicClassNotStatic, typeLoader);
        }
    }


    private void checkRoundTrip (TestOverrideClass inMsg, TestOverrideClass outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.int32, outMsg.int32);
        assertEquals(inMsg.int32_n, outMsg.int32_n);
        assertEquals(inMsg.isAsk, outMsg.isAsk);
        assertEquals(inMsg.isAsk_n, outMsg.isAsk_n);
        assertEquals(inMsg.isLast2, outMsg.isLast2);
        assertEquals(inMsg.isLast2_n, outMsg.isLast2_n);
        assertEquals(inMsg.isLast, outMsg.isLast);
        assertEquals(inMsg.isLast_n, outMsg.isLast_n);
        assertEquals(inMsg.testEnum, outMsg.testEnum);
        assertEquals(inMsg.testEnum_n, outMsg.testEnum_n);
    }


    public static final RecordClassDescriptor rcdTestBooleansProperties = new RecordClassDescriptor(
            TestBooleansProperties.class.getName(),
            "test class with all configuration of boolean properties",
            false,
            null,
            new NonStaticDataField("bool1", null, new BooleanDataType(false)),
            new NonStaticDataField("bool2", null, new BooleanDataType(false)),
            new NonStaticDataField("bool3", null, new BooleanDataType(true)),
            new NonStaticDataField("bool4", null, new BooleanDataType(true)),
            new NonStaticDataField("bool5", null, new BooleanDataType(true)),
            new NonStaticDataField("bool6", null, new BooleanDataType(false)),
            new NonStaticDataField("bool7", null, new BooleanDataType(false)),
            new NonStaticDataField("bool8", null, new BooleanDataType(true)),
            new NonStaticDataField("bool9", null, new BooleanDataType(true)),
            new NonStaticDataField("bool10", null, new BooleanDataType(false)),
            new NonStaticDataField("bool11", null, new BooleanDataType(false)),
            new NonStaticDataField("bool12", null, new BooleanDataType(false))
    );

    public static class TestBooleansProperties extends InstrumentMessage {
        private boolean bool1;

        @SchemaElement
        public boolean isBool1 () {
            return bool1;
        }

        public void setBool1 (boolean bool1) {
            this.bool1 = bool1;
        }


        private boolean bool2;

        @SchemaElement
        public boolean isBool2 () {
            return bool2;
        }

        public void setBool2 (boolean bool2) {
            this.bool2 = bool2;
        }

        public boolean hasBool2 () {
            return true;
        }

        public void nullifyBool2 () {
        }


        private byte bool3;

        @SchemaElement
        public byte getBool3 () {
            return bool3;
        }

        public void setBool3 (byte bool3) {
            this.bool3 = bool3;
        }


        private byte bool4;

        @SchemaElement
        public byte isBool4 () {
            return bool4;
        }

        public void setBool4 (byte bool4) {
            this.bool4 = bool4;
        }

        public boolean hasBool4 () {
            return this.bool4 != - 1;
        }

        public void nullifyBool4 () {
            this.bool4 = - 1;
        }


        private byte bool5;

        @SchemaElement
        public boolean isBool5 () {
            return bool5 == 1;
        }

        public void setBool5 (boolean value) {
            this.bool5 = (byte) (value ? 1 : 0);
        }

        public boolean hasBool5 () {
            return this.bool5 != - 1;
        }

        public void nullifyBool5 () {
            this.bool5 = - 1;
        }


        private byte bool6;

        @SchemaElement
        public boolean getBool6 () {
            return bool6 == 1;
        }

        public void setBool6 (byte bool6) {
            this.bool6 = bool6;
        }


        private byte bool7;

        @SchemaElement
        public byte getBool7 () {
            return bool7;
        }

        public void setBool7 (boolean value) {
            bool7 = (byte) (value ? 1 : 0);
        }


        private byte bool8;

        @SchemaElement
        public boolean getBool8 () {
            return bool8 == 1;
        }

        public void setBool8 (byte value) {
            this.bool8 = value;
        }

        public boolean hasBool8 () {
            return bool8 != - 1;
        }

        public void nullifyBool8 () {
            this.bool8 = - 1;
        }


        private byte bool9;

        @SchemaElement
        public byte getBool9 () {
            return bool9;
        }

        public void setBool9 (boolean value) {
            this.bool9 = (byte) (value ? 1 : 0);
        }

        public boolean hasBool9 () {
            return this.bool9 != - 1;
        }

        public void nullifyBool9 () {
            this.bool9 = - 1;
        }


        private boolean bool10;

        @SchemaElement
        public byte getBool10 () {
            return bool10 ? (byte) 1 : (byte) 0;
        }

        public void setBool10 (byte value) {
            this.bool10 = value == 1;
        }


        private boolean bool11;

        @SchemaElement
        public byte getBool11 () {
            return bool11 ? (byte) 1 : (byte) 0;
        }

        public void setBool11 (boolean value) {
            this.bool11 = value;
        }


        private boolean bool12;

        @SchemaElement
        public boolean isBool12 () {
            return bool12;
        }

        public void setBool12 (byte value) {
            this.bool12 = value == 1;
        }

        @Override
        public String toString () {
            return "TestBooleansProperties{" +
                    "bool1=" + bool1 +
                    ", bool2=" + bool2 +
                    ", bool3=" + bool3 +
                    ", bool4=" + bool4 +
                    ", bool5=" + bool5 +
                    ", bool6=" + bool6 +
                    ", bool7=" + bool7 +
                    ", bool8=" + bool8 +
                    ", bool9=" + bool9 +
                    ", bool10=" + bool10 +
                    ", bool11=" + bool11 +
                    ", bool12=" + bool12 +
                    '}';
        }
    }

    private void checkRoundTrip (TestBooleansProperties inMsg, TestBooleansProperties outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.bool1, outMsg.bool1);
        assertEquals(inMsg.bool2, outMsg.bool2);
        assertEquals(inMsg.bool3, outMsg.bool3);
        assertEquals(inMsg.bool4, outMsg.bool4);
        assertEquals(inMsg.bool5, outMsg.bool5);
        assertEquals(inMsg.bool6, outMsg.bool6);
        assertEquals(inMsg.bool7, outMsg.bool7);
        assertEquals(inMsg.bool8, outMsg.bool8);
        assertEquals(inMsg.bool9, outMsg.bool9);
        assertEquals(inMsg.bool10, outMsg.bool10);
        assertEquals(inMsg.bool11, outMsg.bool11);
        assertEquals(inMsg.bool12, outMsg.bool12);
    }


    @Test
    public void testAllBooleanPropertiesIntp () {
        setUpIntp();
        testAllBooleanProperties();
    }

    @Test
    public void testAllBooleanPropertiesComp () {
        setUpComp();
        testAllBooleanProperties();
    }

    private void testAllBooleanProperties () {
        final TestBooleansProperties msg = new TestBooleansProperties();

        for (int i = 0; i < 1000; i++) {
            switch (i % 7) {
                case 0: //set false
                    msg.bool1 = false;
                    msg.bool2 = false;
                    msg.bool3 = 0;
                    msg.bool4 = 0;
                    msg.bool5 = 0;
                    msg.bool6 = 0;
                    msg.bool7 = 0;
                    msg.bool8 = 0;
                    msg.bool9 = 0;
                    msg.bool10 = false;
                    msg.bool11 = false;
                    msg.bool12 = false;
                    break;

                case 1: //set true
                    msg.bool1 = true;
                    msg.bool2 = true;
                    msg.bool3 = 1;
                    msg.bool4 = 1;
                    msg.bool5 = 1;
                    msg.bool6 = 1;
                    msg.bool7 = 1;
                    msg.bool8 = 1;
                    msg.bool9 = 1;
                    msg.bool10 = true;
                    msg.bool11 = true;
                    msg.bool12 = true;
                    break;
                default: //set null or false
                    msg.bool1 = false;
                    msg.bool2 = false;
                    msg.bool3 = - 1;
                    msg.bool4 = - 1;
                    msg.bool5 = - 1;
                    msg.bool6 = 0; //not nullable
                    msg.bool7 = 0; //not nullable
                    msg.bool8 = - 1;
                    msg.bool9 = - 1;
                    msg.bool10 = false;
                    msg.bool11 = false;
                    msg.bool12 = false;
                    break;
            }
            testBoundRoundTrip(msg, rcdTestBooleansProperties, CL);
        }
    }


    public static final RecordClassDescriptor rcdClassB = new RecordClassDescriptor(
            ClassB.class.getName(),
            null,
            false,
            null,
            new NonStaticDataField("integer1", null, new IntegerDataType("INT32", true)),
            new NonStaticDataField("integer2", null, new IntegerDataType("INT32", true))
    );

    public static final RecordClassDescriptor rcdClassA = new RecordClassDescriptor(
            ClassA.class.getName(),
            null,
            false,
            null,
            new NonStaticDataField("int64", null, new IntegerDataType("INT64", true)),
            new NonStaticDataField("name", null, new VarcharDataType("UTF8", true, true)),
            new NonStaticDataField("enum1", null, new EnumDataType(true, rcdTestEnum)),
            new NonStaticDataField("enum2", null, new EnumDataType(true, rcdTestEnum))
    );

    public static final RecordClassDescriptor rcdCustomClassA = new RecordClassDescriptor(
            CustomClassA.class.getName(),
            null,
            false,
            null,
            new NonStaticDataField("inpt1", null, new ClassDataType(true, rcdClassA, rcdClassB)),
            new NonStaticDataField("inpt2", null, new ClassDataType(true, rcdClassA, rcdClassB)),
            new NonStaticDataField("inpt3", null, new ClassDataType(true, rcdClassA, rcdClassB)),

            new NonStaticDataField("listInpt1", null, new ArrayDataType(true, new ClassDataType(true, rcdClassA, rcdClassB))),
            new NonStaticDataField("listInpt2", null, new ArrayDataType(true, new ClassDataType(true, rcdClassA, rcdClassB))),
            new NonStaticDataField("listInpt3", null, new ArrayDataType(true, new ClassDataType(true, rcdClassA, rcdClassB))),

            new NonStaticDataField("int32_1", null, new IntegerDataType("INT32", true)),
            new NonStaticDataField("int32_2", null, new IntegerDataType("INT32", true))
    );

    public static class CustomClassA extends InstrumentMessage {
        public CustomInterface inpt1;

        protected CustomInterface inpt2;

        public CustomInterface getInpt2 () {
            return inpt2;
        }

        public void setInpt2 (CustomInterface inpt2) {
            this.inpt2 = inpt2;
        }

        protected CustomInterface inpt3;

        public CustomInterface getInpt3 () {
            return inpt3;
        }

        public void setInpt3 (CustomInterface inpt3) {
            this.inpt3 = inpt3;
        }

        public boolean hasInpt3 () {
            return this.inpt3 != null;
        }

        public void nullifyInpt3 () {
            this.inpt3 = null;
        }

        public ObjectArrayList<CustomInterface> listInpt1;

        protected ObjectArrayList<CustomInterface> listInpt2;

        public ObjectArrayList<CustomInterface> getListInpt2 () {
            return listInpt2;
        }

        public void setListInpt2 (ObjectArrayList<CustomInterface> listInpt2) {
            this.listInpt2 = listInpt2;
        }

        protected ObjectArrayList<CustomInterface> listInpt3;

        public ObjectArrayList<CustomInterface> getListInpt3 () {
            return listInpt3;
        }

        public void setListInpt3 (ObjectArrayList<CustomInterface> listInpt3) {
            this.listInpt3 = listInpt3;
        }

        public boolean hasListInpt3 () {
            return this.listInpt3 != null;
        }

        public void nullifyListInpt3 () {
            this.listInpt3 = null;
        }

        public int int32_1;
        public int int32_2;

        @Override
        public String toString () {
            return "CustomClassA{" +
                    "inpt1=" + inpt1 +
                    ", inpt2=" + inpt2 +
                    ", inpt3=" + inpt3 +
                    ", listInpt1=" + listInpt1 +
                    ", listInpt2=" + listInpt2 +
                    ", listInpt3=" + listInpt3 +
                    ", int32_1=" + int32_1 +
                    ", int32_2=" + int32_2 +
                    '}';
        }
    }


    public interface CustomInterface {
    }


    public static class ClassA implements CustomInterface {

        public long int64;

        private String name;

        @SchemaElement (name = "name")
        public String getName () {
            return name;
        }

        public void setName (String name) {
            this.name = name;
        }

        public TestEnum enum1;

        protected TestEnum enum2;

        @SchemaElement (name = "enum2")
        public TestEnum getEnum2 () {
            return enum2;
        }

        public void setEnum2 (TestEnum enum2) {
            this.enum2 = enum2;
        }

        @Override
        public String toString () {
            return "ClassA{" +
                    "int64=" + int64 +
                    ", name='" + name + '\'' +
                    ", enum1=" + enum1 +
                    ", enum2=" + enum2 +
                    '}';
        }
    }


    public static class ClassAOverride extends ClassA {
        @SchemaElement (name = "enum2")
        @SchemaType (
                dataType = SchemaDataType.ENUM,
                isNullable = false,
                nestedTypes = {TestEnum.class}
        )
        public CharSequence getEnumAsString () {
            return enum2.toString();
        }

        public void setEnum2Override (CharSequence enum2) {
            this.enum2 = TestEnum.valueOf((String) enum2);
        }

        @Override
        public String toString () {
            return "ClassAOverride{} " + super.toString();
        }
    }


    public static class ClassB implements CustomInterface {
        public ClassB (int integer1, int integer2) {
            this.integer1 = integer1;
            this.integer2 = integer2;
        }

        public ClassB () {
        }

        public int integer1;
        private int integer2;

        @SchemaElement (name = "integer2")
        public int getInteger2 () {
            return integer2;
        }

        public void setInteger2 (int integer2) {
            this.integer2 = integer2;
        }

        @Override
        public String toString () {
            return "ClassB{" +
                    "integer1=" + integer1 +
                    ", integer2=" + integer2 +
                    '}';
        }
    }


    private ObjectArrayList<CustomInterface> getListCustomInterfaceMessage (int i) {
        final ObjectArrayList<CustomInterface> msg = new ObjectArrayList<>(i);
        for (int j = 0; j < i; j++) {
            msg.add(getCustomInterfaceMessage(j));
        }
        return msg;
    }


    private CustomInterface getCustomInterfaceMessage (int i) {
        if (i % 2 == 0) {
            final ClassAOverride msg = new ClassAOverride();
            msg.enum1 = (i % 2 == 0) ? TestEnum.RED : TestEnum.GREEN;
            msg.enum2 = (i % 2 == 0) ? TestEnum.BLACK : TestEnum.WHITE;
            msg.int64 = i;
            msg.setName("HI All! #" + i);
            return msg;
        } else {
            final ClassB msg = new ClassB();
            msg.integer1 = i;
            msg.integer2 = 2 * i;
            return msg;
        }
    }


    public static class UnsupportedBooleanProperties extends InstrumentMessage {
        private byte bool1;

        public boolean getBool1 () {
            return this.bool1 == 1;
        }

        public void setBool1 (boolean value) {
            this.bool1 = (byte) (value ? 1 : 0);
        }
    }

    public static final RecordClassDescriptor rcdUnsupportedBooleanProperties1 = new RecordClassDescriptor(
            UnsupportedBooleanProperties.class.getName(),
            null,
            false,
            null,
            new NonStaticDataField("bool1", null, new BooleanDataType(true))
    );

    public static class UnsupportedBooleanPropertiesForTest extends InstrumentMessage {
        private byte bool1;

        public byte getBool1 () {
            return bool1;
        }

        public void setBool1 (byte bool1) {
            this.bool1 = bool1;
        }
    }


    @Test
    public void testUnsupportedBooleanPropertiesIntp () {
        setUpIntp();
        final UnsupportedBooleanProperties inMsg = new UnsupportedBooleanProperties();
        try {
            FixedBoundEncoder benc = factory.createFixedBoundEncoder(TypeLoaderImpl.DEFAULT_INSTANCE, rcdUnsupportedBooleanProperties1);
            MemoryDataOutput out = new MemoryDataOutput();
            benc.encode(inMsg, out);
            fail("Missing NULLABLE value when using getter & setter that return / set boolean type without HASER & NULLIFIER!");
        } catch (RuntimeException ex) {
            assertEquals("Can`t read NULLABLE BOOLEAN using getter: \"getBool1()\" that return boolean.class without HASER method.", ex.getMessage());
        }


        try {
            final UnsupportedBooleanPropertiesForTest testMsg = new UnsupportedBooleanPropertiesForTest();
            testMsg.setBool1(BooleanDataType.NULL);

            SimpleTypeLoader simpleTypeLoader = new SimpleTypeLoader(rcdUnsupportedBooleanProperties1.getName(), UnsupportedBooleanPropertiesForTest.class);
            FixedBoundEncoder benc = factory.createFixedBoundEncoder(simpleTypeLoader, rcdUnsupportedBooleanProperties1);
            MemoryDataOutput out = new MemoryDataOutput();
            benc.encode(testMsg, out);

            BoundDecoder bdec = factory.createFixedBoundDecoder(TypeLoaderImpl.DEFAULT_INSTANCE, rcdUnsupportedBooleanProperties1);
            MemoryDataInput in = new MemoryDataInput(out);
            bdec.decode(in);
            fail("Missing NULLABLE value when using getter & setter that return / set boolean type without HASER & NULLIFIER!");
        } catch (RuntimeException ex) {
            assertEquals("Can`t write NULLABLE BOOLEAN using setter: \"setBool1()\" that parameter type = boolean.class without NULLIFIER method.", ex.getMessage());
        }
    }

    @Test
    public void testUnsupportedBooleanPropertiesComp () {
        setUpComp();
        try {
            factory.createFixedBoundEncoder(TypeLoaderImpl.DEFAULT_INSTANCE, rcdUnsupportedBooleanProperties1);
            fail("Missing NULLABLE value when using getter & setter that return / set boolean type without HASER & NULLIFIER!");
        } catch (RuntimeException ex) {
            assertEquals("java.lang.UnsupportedOperationException: Can`t read NULLABLE BOOLEAN using getter: \"getBool1()\" that return boolean.class without HASER method.", ex.getMessage());
        }


        try {
            factory.createFixedBoundDecoder(TypeLoaderImpl.DEFAULT_INSTANCE, rcdUnsupportedBooleanProperties1);
            fail("Missing NULLABLE value when using getter & setter that return / set boolean type without HASER & NULLIFIER!");
        } catch (RuntimeException ex) {
            assertEquals("java.lang.UnsupportedOperationException: Can`t write NULLABLE BOOLEAN using setter: \"setBool1()\" that parameter type = boolean.class without NULLIFIER method.", ex.getMessage());
        }
    }


    private static class UnsupportedPrivateClass extends InstrumentMessage {
        private byte bool1;

        public byte getBool1 () {
            return bool1;
        }

        public void setBool1 (byte bool1) {
            this.bool1 = bool1;
        }
    }


    @Test
    public void testUnsupportedPrivateClassIntp () {
        setUpIntp();
        final UnsupportedBooleanProperties inMsg = new UnsupportedBooleanProperties();
        SimpleTypeLoader loader = new SimpleTypeLoader(rcdUnsupportedBooleanProperties1.getName(), UnsupportedPrivateClass.class);
        try {
            FixedBoundEncoder benc = factory.createFixedBoundEncoder(loader, rcdUnsupportedBooleanProperties1);
            MemoryDataOutput out = new MemoryDataOutput();
            benc.encode(inMsg, out);
            fail("FAIL: Cannot processing PRIVATE classes!!!!");
        } catch (RuntimeException ex) {
            assertEquals("Failed to bind descriptor " + rcdUnsupportedBooleanProperties1.getName(), ex.getMessage());
        }


        try {
            final UnsupportedBooleanPropertiesForTest testMsg = new UnsupportedBooleanPropertiesForTest();
            testMsg.setBool1(BooleanDataType.NULL);

            SimpleTypeLoader simpleTypeLoader = new SimpleTypeLoader(rcdUnsupportedBooleanProperties1.getName(), UnsupportedBooleanPropertiesForTest.class);
            FixedBoundEncoder benc = factory.createFixedBoundEncoder(simpleTypeLoader, rcdUnsupportedBooleanProperties1);
            MemoryDataOutput out = new MemoryDataOutput();
            benc.encode(testMsg, out);

            BoundDecoder bdec = factory.createFixedBoundDecoder(loader, rcdUnsupportedBooleanProperties1);
            MemoryDataInput in = new MemoryDataInput(out);
            bdec.decode(in);
            fail("FAIL: Cannot processing PRIVATE classes!!!!");
        } catch (RuntimeException ex) {
            assertEquals("Failed to bind descriptor " + rcdUnsupportedBooleanProperties1.getName(), ex.getMessage());
        }
    }


    @Test
    public void testUnsupportedPrivateClassComp () {
        setUpComp();
        SimpleTypeLoader loader = new SimpleTypeLoader(rcdUnsupportedBooleanProperties1.getName(), UnsupportedPrivateClass.class);
        try {
            factory.createFixedBoundEncoder(loader, rcdUnsupportedBooleanProperties1);
            fail("FAIL: Cannot processing PRIVATE classes!!!!");
        } catch (RuntimeException ex) {
            assertEquals("Failed to bind descriptor " + rcdUnsupportedBooleanProperties1.getName(), ex.getMessage());
        }
        try {
            factory.createFixedBoundDecoder(loader, rcdUnsupportedBooleanProperties1);
            fail("FAIL: Cannot processing PRIVATE classes!!!!");
        } catch (RuntimeException ex) {
            assertEquals("Failed to bind descriptor " + rcdUnsupportedBooleanProperties1.getName(), ex.getMessage());
        }
    }

    final RecordClassDescriptor rcdAllStaticTypesClass = new RecordClassDescriptor(
            AllStaticTypesClass.class.getName(),
            null,
            false,
            null,
            new StaticDataField("binary", null, new BinaryDataType(true, BinaryDataType.MIN_COMPRESSION), null),

            new StaticDataField("bool1", null, new BooleanDataType(false), true),
            new StaticDataField("bool2", null, new BooleanDataType(false), true),
            new StaticDataField("bool3", null, new BooleanDataType(true), null),
            new StaticDataField("bool4", null, new BooleanDataType(true), true),
            new StaticDataField("bool5", null, new BooleanDataType(true), null),

            new StaticDataField("aChar", null, new CharDataType(true), 'A'),
            new StaticDataField("timestamp1", null, new DateTimeDataType(true), "2016-12-05 10:14:28.123"),
            new StaticDataField("ieee32", null, new FloatDataType("IEEE32", true), 9.9F),
            new StaticDataField("ieee64", null, new FloatDataType("IEEE64", true), 10.1),
            new StaticDataField("int32", null, new IntegerDataType("INT32", true), 99),
            new StaticDataField("timeOfDay", null, new TimeOfDayDataType(true), 0),

            new StaticDataField("name", null, new VarcharDataType("UTF8", true, true), "HI, ALL!"),
            new StaticDataField("classB", null, new ClassDataType(true, rcdClassB), null)
    );


    public static class AllStaticTypesClass extends InstrumentMessage {
        private ByteArrayList binary;

        private byte bool1; //not nullable
        private boolean bool2; //not nullable
        private byte bool3;  //nullable byte getter&setter
        private byte bool4; //nullable boolean get&set + has & nullify
        private byte bool5; //nullable boolean get&set + has & nullify

        private char aChar;

        private long timestamp1;
        private float ieee32;
        private double ieee64;

        private int int32;
        private int timeOfDay;

        private String name;

        private ClassB classB;


        public byte getBool3 () {
            return bool3;
        }

        public void setBool3 (byte value) {
            this.bool3 = value;
        }

        public boolean isBool5 () {
            return bool5 == 1;
        }

        public void setBool5 (boolean value) {
            bool5 = (byte) (value ? 1 : 0);
        }

        public boolean hasBool5 () {
            return bool5 != - 1;
        }

        public void nullifyBool5 () {
            bool5 = - 1;
        }


        public boolean isBool4 () {
            return bool4 == 1;
        }

        public void setBool4 (boolean value) {
            bool4 = (byte) (value ? 1 : 0);
        }

        public boolean hasBool4 () {
            return bool4 != - 1;
        }

        public void nullifyBool4 () {
            bool4 = - 1;
        }

        public ByteArrayList getBinary () {
            return binary;
        }

        public void setBinary (ByteArrayList binary) {
            this.binary = binary;
        }

        public byte getBool1 () {
            return bool1;
        }

        public void setBool1 (byte bool1) {
            this.bool1 = bool1;
        }

        public boolean isBool2 () {
            return bool2;
        }

        public void setBool2 (boolean bool2) {
            this.bool2 = bool2;
        }

        public char getaChar () {
            return aChar;
        }

        public void setaChar (char aChar) {
            this.aChar = aChar;
        }

        public long getTimestamp1 () {
            return timestamp1;
        }

        public void setTimestamp1 (long timestamp1) {
            this.timestamp1 = timestamp1;
        }

        public float getIeee32 () {
            return ieee32;
        }

        public void setIeee32 (float ieee32) {
            this.ieee32 = ieee32;
        }

        public double getIeee64 () {
            return ieee64;
        }

        public void setIeee64 (double ieee64) {
            this.ieee64 = ieee64;
        }

        public int getInt32 () {
            return int32;
        }

        public void setInt32 (int int32) {
            this.int32 = int32;
        }

        public int getTimeOfDay () {
            return timeOfDay;
        }

        public void setTimeOfDay (int timeOfDay) {
            this.timeOfDay = timeOfDay;
        }

        public String getName () {
            return name;
        }

        public void setName (String name) {
            this.name = name;
        }

        public ClassB getClassB () {
            return classB;
        }

        public void setClassB (ClassB classB) {
            this.classB = classB;
        }

        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("AllStaticTypesClass{");
            sb.append(" ");
            sb.append(binary);
            sb.append(" ");
            sb.append(bool1);
            sb.append(" ");
            sb.append(bool2);
            sb.append(" ");
            sb.append(bool3);
            sb.append(" ");
            sb.append(bool4);
            sb.append(" ");
            sb.append(bool5);
            sb.append(" ");
            sb.append(aChar);
            sb.append(" ");
            sb.append(timestamp1);
            sb.append(" ");
            sb.append(ieee32);
            sb.append(" ");
            sb.append(ieee64);
            sb.append(" ");
            sb.append(int32);
            sb.append(" ");
            sb.append(timeOfDay);
            sb.append(" ");
            sb.append(name);
            sb.append(" ");
            sb.append(classB);
            sb.append('}');
            return sb.toString();
        }
    }


    @Test
    public void testAllStaticTypesClassIntp () {
        setUpIntp();
        testAllStaticTypesClass();
    }

    @Test
    public void testAllStaticTypesClassComp () {
        setUpComp();
        testAllStaticTypesClass();
    }

    private void testAllStaticTypesClass () {
        final AllStaticTypesClass msg = new AllStaticTypesClass();
        msg.setBinary(null);
        msg.setBool1((byte) 1);
        msg.setBool2(true);
        msg.setBool3((byte) - 1);
        msg.setBool4(true);
        msg.nullifyBool5();
        msg.setaChar('A');
        msg.setTimestamp1(1480932868123L); // "2016-12-05 10:14:28.123"
        msg.setIeee32(9.9F);
        msg.setIeee64(10.1);
        msg.setInt32(99);
        msg.setTimeOfDay(0);
        msg.setName("HI, ALL!");
        msg.setClassB(null);
        testBoundRoundTrip(msg, rcdAllStaticTypesClass, CL);
    }

    private void checkRoundTrip (AllStaticTypesClass inMsg, AllStaticTypesClass outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.aChar, outMsg.aChar);
        assertEquals(inMsg.binary, outMsg.binary);
        assertEquals(inMsg.bool1, outMsg.bool1);
        assertEquals(inMsg.bool2, outMsg.bool2);
        assertEquals(inMsg.bool3, outMsg.bool3);
        assertEquals(inMsg.bool4, outMsg.bool4);
        assertEquals(inMsg.bool5, outMsg.bool5);
        assertEquals(inMsg.timestamp1, outMsg.timestamp1);
        assertEquals(inMsg.ieee32, outMsg.ieee32, 0.001);
        assertEquals(inMsg.ieee64, outMsg.ieee64, 0.001);
        assertEquals(inMsg.int32, outMsg.int32);
        assertEquals(inMsg.timeOfDay, outMsg.timeOfDay);
        assertEquals(inMsg.name, outMsg.name);
        assertEquals(inMsg.classB, outMsg.classB);
    }

    @Test
    public void testAllTypesPrivateComp () {
        setUpComp();
        testAllPrivateTypes();
    }

    @Test
    public void testAllTypesPrivateIntp () {
        setUpIntp();
        testAllPrivateTypes();
    }


    private void testAllPrivateTypes () {
        RecordClassDescriptor rcd = TestAllTypesMessagePrivate.getClassDescriptor();
        final TestAllTypesMessagePrivate msg = new TestAllTypesMessagePrivate();

        for (int s = 0; s <= 100; s++) {
            for (int e = 0; e < 2; e++) {

                msg.fillMessage(e, s);
                testBoundRoundTrip(msg, rcd, CL);
            }
        }
    }

    private void checkRoundTrip (TestAllTypesMessagePrivate inMsg, TestAllTypesMessagePrivate outMsg) {
        inMsg.assertEquals("Equals with out message ", outMsg);
    }

    private static final RecordClassDescriptor rcdTestCodecBinding = new RecordClassDescriptor(
            TestCodecBinding.class.getName(),
            "test schema for testing binding",
            false,
            null,
            new NonStaticDataField("bool1", "title", new BooleanDataType(false)),
            new NonStaticDataField("bool2", "title", new BooleanDataType(false)),
            new NonStaticDataField("bool3", "title", new BooleanDataType(true)),

            new NonStaticDataField("bool4", "title", new BooleanDataType(false)),
            new NonStaticDataField("bool5", "title", new BooleanDataType(true)),
            new NonStaticDataField("bool6", "title", new BooleanDataType(true)),
            new NonStaticDataField("name1", "title", new VarcharDataType("UTF8", true, true)),

            new NonStaticDataField("bool7", "title", new BooleanDataType(true)),
            new NonStaticDataField("bool8", "title", new BooleanDataType(false)),
            new NonStaticDataField("bool9", "title", new BooleanDataType(true)),
            new NonStaticDataField("name2", "title", new VarcharDataType("UTF8", true, true)),

            new NonStaticDataField("bool10", "title", new BooleanDataType(false)),
            new NonStaticDataField("bool11", "title", new BooleanDataType(true)),
            new NonStaticDataField("bool12", "title", new BooleanDataType(false)),
            new NonStaticDataField("name3", "title", new VarcharDataType("UTF8", true, true)),

            new NonStaticDataField("bool13", "title", new BooleanDataType(false)),
            new NonStaticDataField("bool14", "title", new BooleanDataType(true)),
            new NonStaticDataField("bool15", "title", new BooleanDataType(false)),
            new NonStaticDataField("name4", "title", new VarcharDataType("UTF8", true, true))
    );


    public static class TestCodecBinding extends InstrumentMessage {
        // PUBLIC FIELDS
        @SchemaElement (name = "bool1")
        public boolean bool1;

        @SchemaElement (name = "bool2")
        public boolean booleanField;

        //bool3 in schema
        public byte bool3;


        // PROPERTIES WITH SchemaElement.name, where (fieldName from getter)  == SE.name
        private boolean bool4;

        @SchemaElement (name = "bool4")
        public boolean isBool4 () {
            return bool4;
        }

        public void setBool4 (boolean bool4) {
            this.bool4 = bool4;
        }

        private byte bool5;

        @SchemaElement (name = "bool5")
        public boolean getBool5 () {
            return bool5 == 1;
        }

        public void setBool5 (boolean bool5) {
            this.bool5 = bool5 ? (byte) 1 : (byte) 0;
        }

        public boolean hasBool5 () {
            return this.bool5 != - 1;
        }

        public void nullifyBool5 () {
            this.bool5 = - 1;
        }

        private byte bool6;

        @SchemaElement (name = "bool6")
        public byte bool6 () {
            return bool6;
        }

        public void setBool6 (byte bool6) {
            this.bool6 = bool6;
        }

        private String name1;

        @SchemaElement (name = "name1")
        public String getName1 () {
            return name1;
        }

        public void setName1 (String name1) {
            this.name1 = name1;
        }

        // PROPERTIES WITH SchemaElement.name, where SE.name != (fieldName from getter)
        private byte canExecute;

        @SchemaElement (name = "bool7")
        public byte canExecute () {
            return canExecute;
        }

        public void setCanExecute (byte canExecute) {
            this.canExecute = canExecute;
        }

        private boolean ask;

        @SchemaElement (name = "bool8")
        public boolean isAsk () {
            return ask;
        }

        public void setAsk (boolean ask) {
            this.ask = ask;
        }

        private byte byteAsBool;

        @SchemaElement (name = "bool9")
        public byte getByteAsBool () {
            return byteAsBool;
        }

        public void setByteAsBool (byte byteAsBool) {
            this.byteAsBool = byteAsBool;
        }

        private String stringField;

        @SchemaElement (name = "name2")
        public String getStringField () {
            return stringField;
        }

        public void setStringField (String stringField) {
            this.stringField = stringField;
        }

        // PROPERTIES WITH EMPTY SchemaElement.name
        private boolean bool10;

        @SchemaElement (title = "bool10 field in schema")
        public boolean isBool10 () {
            return bool10;
        }

        public void setBool10 (boolean bool10) {
            this.bool10 = bool10;
        }

        private byte bool11;

        @SchemaElement (title = "bool11 field in schema")
        public byte getBool11 () {
            return bool11;
        }

        public void setBool11 (byte bool11) {
            this.bool11 = bool11;
        }

        private boolean bool12;

        @SchemaElement (title = "bool12 field in schema")
        public boolean bool12 () {
            return bool12;
        }

        public void setBool12 (boolean bool12) {
            this.bool12 = bool12;
        }

        private String name3;

        @SchemaElement (title = "name3 field in schema")
        public String getName3 () {
            return name3;
        }

        public void setName3 (String name3) {
            this.name3 = name3;
        }

        // PROPERTIES WITH only JavaBean notation
        private boolean bool13;

        public boolean isBool13 () {
            return bool13;
        }

        public void setBool13 (boolean bool13) {
            this.bool13 = bool13;
        }

        private byte bool14;

        public byte getBool14 () {
            return bool14;
        }

        public void setBool14 (byte bool14) {
            this.bool14 = bool14;
        }

        private boolean bool15;

        public boolean bool15 () {
            return bool15;
        }

        public void setBool15 (boolean bool15) {
            this.bool15 = bool15;
        }

        private String name4;

        public String getName4 () {
            return name4;
        }

        public void setName4 (String name4) {
            this.name4 = name4;
        }


        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("TestCodecBinding{");
            sb.append("ask=").append(ask);
            sb.append(", bool1=").append(bool1);
            sb.append(", booleanField=").append(booleanField);
            sb.append(", bool3=").append(bool3);
            sb.append(", bool4=").append(bool4);
            sb.append(", bool5=").append(bool5);
            sb.append(", bool6=").append(bool6);
            sb.append(", name1='").append(name1).append('\'');
            sb.append(", canExecute=").append(canExecute);
            sb.append(", byteAsBool=").append(byteAsBool);
            sb.append(", stringField='").append(stringField).append('\'');
            sb.append(", bool10=").append(bool10);
            sb.append(", bool11=").append(bool11);
            sb.append(", bool12=").append(bool12);
            sb.append(", name3='").append(name3).append('\'');
            sb.append(", bool13=").append(bool13);
            sb.append(", bool14=").append(bool14);
            sb.append(", bool15=").append(bool15);
            sb.append(", name4='").append(name4).append('\'');
            sb.append('}');
            return sb.toString();
        }

        public void setTrue (int index) {
            ask = true;
            bool1 = true;
            booleanField = true;
            bool3 = 1;
            bool4 = true;
            bool5 = 1;
            bool6 = 1;
            name1 = "True name1 string #" + index;
            canExecute = 1;
            byteAsBool = 1;
            stringField = "True name2 string #" + index;
            bool10 = true;
            bool11 = 1;
            bool12 = true;
            name3 = "True name3 string #" + index;
            bool13 = true;
            bool14 = 1;
            bool15 = true;
            name4 = "True name4 string #" + index;
        }

        public void setFalse (int index) {
            ask = false;
            bool1 = false;
            booleanField = false;
            bool3 = 0;
            bool4 = false;
            bool5 = 0;
            bool6 = 0;
            name1 = "False name1 string #" + index;
            canExecute = 0;
            byteAsBool = 0;
            stringField = "False name2 string #" + index;
            bool10 = false;
            bool11 = 0;
            bool12 = false;
            name3 = "False name3 string #" + index;
            bool13 = false;
            bool14 = 0;
            bool15 = false;
            name4 = "False name4 string #" + index;
        }

        public void setNulls (int index) {
            ask = (index % 2 == 0);
            bool1 = (index % 2 == 0);
            booleanField = (index % 2 == 0);
            bool3 = - 1;
            bool4 = (index % 2 == 0);
            bool5 = - 1;
            bool6 = - 1;
            name1 = null;
            canExecute = - 1;
            byteAsBool = - 1;
            stringField = null;
            bool10 = (index % 2 == 0);
            bool11 = - 1;
            bool12 = (index % 2 == 0);
            name3 = null;
            bool13 = (index % 2 == 0);
            bool14 = - 1;
            bool15 = (index % 2 == 0);
            name4 = null;
        }
    }


    @Test
    public void testCodecBindingComp () {
        setUpComp();
        testCodecBinding();
    }

    @Test
    public void testCodecBindingIntp () {
        setUpIntp();
        testCodecBinding();
    }


    private void testCodecBinding () {
        final TestCodecBinding msg = new TestCodecBinding();
        for (int i = 0; i < 100; i++) {
            switch (i % 5) {
                case 0: //setNulls
                    msg.setNulls(i);
                    break;
                case 1: //set True;
                    msg.setTrue(i);
                    break;
                default: //set false
                    msg.setFalse(i);
                    break;
            }
            testBoundRoundTrip(msg, rcdTestCodecBinding, CL);
        }
    }

    private void checkRoundTrip (TestCodecBinding inMsg, TestCodecBinding outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.bool1, outMsg.bool1);
        assertEquals(inMsg.booleanField, outMsg.booleanField);
        assertEquals(inMsg.bool3, outMsg.bool3);
        assertEquals(inMsg.bool4, outMsg.bool4);
        assertEquals(inMsg.bool5, outMsg.bool5);
        assertEquals(inMsg.bool6, outMsg.bool6);
        assertEquals(inMsg.canExecute, outMsg.canExecute);
        assertEquals(inMsg.ask, outMsg.ask);
        assertEquals(inMsg.byteAsBool, outMsg.byteAsBool);
        assertEquals(inMsg.bool10, outMsg.bool10);
        assertEquals(inMsg.bool11, outMsg.bool11);
        assertEquals(inMsg.bool12, outMsg.bool12);
        assertEquals(inMsg.bool13, outMsg.bool13);
        assertEquals(inMsg.bool14, outMsg.bool14);
        assertEquals(inMsg.bool15, outMsg.bool15);

        assertEquals(inMsg.name1, outMsg.name1);
        assertEquals(inMsg.stringField, outMsg.stringField);
        assertEquals(inMsg.name3, outMsg.name3);
        assertEquals(inMsg.name4, outMsg.name4);

    }


    public static class TestClassForIntrospection extends InstrumentMessage {
        private byte aByte;

        public byte getaByte () {
            return aByte;
        }

        public void setaByte (byte aByte) {
            this.aByte = aByte;
        }

        private MemberOfTestClassForIntrospection member;

        public MemberOfTestClassForIntrospection getMember () {
            return member;
        }

        public void setMember (MemberOfTestClassForIntrospection member) {
            this.member = member;
        }

        private ObjectArrayList<MemberOfTestClassForIntrospection> listOfMembers;

        public ObjectArrayList<MemberOfTestClassForIntrospection> getListOfMembers () {
            return listOfMembers;
        }

        public void setListOfMembers (ObjectArrayList<MemberOfTestClassForIntrospection> listOfMembers) {
            this.listOfMembers = listOfMembers;
        }

        private IntegerArrayList ints;

        public IntegerArrayList getInts () {
            return ints;
        }

        public void setInts (IntegerArrayList ints) {
            this.ints = ints;
        }


        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("TestClassForIntrospection{");
            sb.append("aByte=").append(aByte);
            sb.append(", member=").append(member);
            sb.append(", listOfMembers=").append(listOfMembers);
            sb.append(", ints=").append(ints);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class MemberOfTestClassForIntrospection {
        private boolean canExecute;

        public boolean canExecute () {
            return canExecute;
        }

        public void setCanExecute (boolean canExecute) {
            this.canExecute = canExecute;
        }

        private byte isAsk;

        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        public byte isAsk () {
            return isAsk;
        }

        public void setIsAsk (byte isAsk) {
            this.isAsk = isAsk;
        }

        private boolean off;

        public boolean isOff () {
            return off;
        }

        public void setOff (boolean off) {
            this.off = off;
        }


        private boolean flag;

        public boolean getFlag () {
            return flag;
        }

        public void setFlag (boolean flag) {
            this.flag = flag;
        }

        private boolean getty;

        public boolean getty () {
            return getty;
        }

        public void setGetty (boolean getty) {
            this.getty = getty;
        }

        private byte me;

        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        public boolean getMe () {
            return me == 1;
        }

        public void setMe (boolean value) {
            this.me = (byte) (value ? 1 : 0);
        }

        public boolean hasMe () {
            return this.me != - 1;
        }

        public void nullifyMe () {
            this.me = - 1;
        }


        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("MemberOfTestClassForIntrospection{");
            sb.append("canExecute=").append(canExecute);
            sb.append(", isAsk=").append(isAsk);
            sb.append(", off=").append(off);
            sb.append(", flag=").append(flag);
            sb.append(", getty=").append(getty);
            sb.append(", me=").append(me);
            sb.append('}');
            return sb.toString();
        }
    }

    @Test
    public void testSchemaCreationIntrospectorFullMode1 () throws Exception {
        RecordClassDescriptor rcdTestClassForIntrospection = Introspector.createCustomIntrospector ().introspectRecordClass(TestClassForIntrospection.class);

        assertEquals(TestClassForIntrospection.class.getName(), rcdTestClassForIntrospection.getName());
        assertEquals(4, rcdTestClassForIntrospection.getFields().length);

        DataField df = rcdTestClassForIntrospection.getField("aByte");
        assertNotNull(df);
        assertTrue(df instanceof NonStaticDataField);
        assertTrue(df.getType() instanceof IntegerDataType);
        assertEquals("INT8", df.getType().getEncoding());


        df = rcdTestClassForIntrospection.getField("ints");
        assertNotNull(df);
        assertTrue(df instanceof NonStaticDataField);
        assertTrue(df.getType() instanceof ArrayDataType);
        DataType dt = ((ArrayDataType) df.getType()).getElementDataType();
        assertTrue(dt instanceof IntegerDataType);
        assertEquals("INT32", dt.getEncoding());


        df = rcdTestClassForIntrospection.getField("member");
        assertNotNull(df);
        assertTrue(df instanceof NonStaticDataField);
        assertTrue(df.getType() instanceof ClassDataType);
        assertEquals(1, ((ClassDataType) df.getType()).getDescriptors().length);
        RecordClassDescriptor rcdMemberOfTestClassForIntrospection = ((ClassDataType) df.getType()).getFixedDescriptor();
        assertEqualsMemberOfTestClassForIntrospection(rcdMemberOfTestClassForIntrospection);

        df = rcdTestClassForIntrospection.getField("listOfMembers");
        assertNotNull(df);
        assertTrue(df instanceof NonStaticDataField);
        assertTrue(df.getType() instanceof ArrayDataType);
        dt = ((ArrayDataType) df.getType()).getElementDataType();
        assertTrue(dt instanceof ClassDataType);
        assertEquals(1, ((ClassDataType) dt).getDescriptors().length);
        assertTrue(rcdMemberOfTestClassForIntrospection == ((ClassDataType) dt).getFixedDescriptor());
    }

    private void assertEqualsMemberOfTestClassForIntrospection (RecordClassDescriptor rcdMemberOfTestClassForIntrospection) {
        assertEquals(MemberOfTestClassForIntrospection.class.getName(), rcdMemberOfTestClassForIntrospection.getName());
        assertEquals(6, rcdMemberOfTestClassForIntrospection.getFields().length);

        DataField df = rcdMemberOfTestClassForIntrospection.getField("canExecute");
        assertNotNull(df);
        assertTrue(df.getType() instanceof BooleanDataType);
        assertFalse(df.getType().isNullable());

        df = rcdMemberOfTestClassForIntrospection.getField("isAsk");
        assertNotNull(df);
        assertTrue(df.getType() instanceof BooleanDataType);
        assertTrue(df.getType().isNullable());

        df = rcdMemberOfTestClassForIntrospection.getField("off");
        assertNotNull(df);
        assertTrue(df.getType() instanceof BooleanDataType);
        assertFalse(df.getType().isNullable());

        df = rcdMemberOfTestClassForIntrospection.getField("flag");
        assertNotNull(df);
        assertTrue(df.getType() instanceof BooleanDataType);
        assertFalse(df.getType().isNullable());

        df = rcdMemberOfTestClassForIntrospection.getField("getty");
        assertNotNull(df);
        assertTrue(df.getType() instanceof BooleanDataType);
        assertFalse(df.getType().isNullable());

        df = rcdMemberOfTestClassForIntrospection.getField("me");
        assertNotNull(df);
        assertTrue(df.getType() instanceof BooleanDataType);
        assertTrue(df.getType().isNullable());
    }

    @Test
    public void testClassForIntrospectionComp () throws Exception {
        setUpComp();
        testClassForIntrospection();
    }

    @Test
    public void testClassForIntrospectionIntp () throws Exception {
        setUpIntp();
        testClassForIntrospection();
    }

    private void testClassForIntrospection () throws Exception {
        final TestClassForIntrospection msg = new TestClassForIntrospection();
        RecordClassDescriptor rcd = Introspector.createCustomIntrospector().introspectRecordClass(TestClassForIntrospection.class);
        for (int i = 0; i < 100; i++) {
            switch (i % 5) {
                case 0: //setNull
                    msg.listOfMembers = null;
                    msg.member = null;
                    msg.aByte = IntegerDataType.INT8_NULL;
                    msg.ints = null;
                    break;
                default:
                    msg.listOfMembers = new ObjectArrayList<>(i + 5);
                    for (int j = 0; j < i; j++)
                        msg.listOfMembers.add(generateMemberOfTestClassForIntrospection(j));

                    msg.member = generateMemberOfTestClassForIntrospection(i + 2);
                    msg.aByte = (byte) i;
                    msg.ints = new IntegerArrayList(i + 5);
                    for (int j = 0; j < i; j++)
                        msg.ints.add(j);
                    break;
            }
            testBoundRoundTrip(msg, rcd, CL);
        }
    }

    private MemberOfTestClassForIntrospection generateMemberOfTestClassForIntrospection (int index) {
        final MemberOfTestClassForIntrospection out = new MemberOfTestClassForIntrospection();
        switch (index % 4) {
            case 0:
                out.canExecute = (index % 2 == 0);
                out.isAsk = - 1;
                out.off = (index % 2 == 0);
                out.flag = (index % 2 == 0);
                out.getty = (index % 2 == 0);
                out.me = - 1;
                break;
            case 1:
                out.canExecute = true;
                out.isAsk = 1;
                out.off = true;
                out.flag = true;
                out.getty = true;
                out.me = 1;
                break;
            default:
                out.canExecute = false;
                out.isAsk = 0;
                out.off = false;
                out.flag = false;
                out.getty = false;
                out.me = 0;
                break;
        }
        return out;
    }

    private void checkRoundTrip (TestClassForIntrospection inMsg, TestClassForIntrospection outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.aByte, outMsg.aByte);
        assertEquals(inMsg.ints, outMsg.ints);

        if (inMsg.member == null) {
            assertNull(outMsg.member);
        } else {
            assertNotNull(outMsg.member);
            checkRoundTrip(inMsg.member, outMsg.member);
        }

        if (inMsg.listOfMembers == null) {
            assertNull(outMsg.listOfMembers);
        } else {
            assertNotNull(outMsg.listOfMembers);
            assertEquals(inMsg.listOfMembers.size(), outMsg.listOfMembers.size());
            for (int i = 0; i < inMsg.listOfMembers.size(); i++)
                checkRoundTrip(inMsg.listOfMembers.get(i), outMsg.listOfMembers.get(i));
        }
    }

    private void checkRoundTrip (MemberOfTestClassForIntrospection inMsg, MemberOfTestClassForIntrospection outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.canExecute, outMsg.canExecute);
        assertEquals(inMsg.isAsk, outMsg.isAsk);
        assertEquals(inMsg.off, outMsg.off);
        assertEquals(inMsg.flag, outMsg.flag);
        assertEquals(inMsg.getty, outMsg.getty);
        assertEquals(inMsg.me, outMsg.me);
    }


    public static class IncorrectClassForIntrospectorFullMode extends InstrumentMessage {
        private boolean ask;

        public boolean isAsk () {
            return ask;
        }

        private byte byteAsBool;

        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        public byte getByteAsBool () {
            return byteAsBool;
        }

        private boolean canExecute;

        public boolean canExecute () {
            return canExecute;
        }

        private int integer;

        public int getInteger () {
            return integer;
        }


        private String string;

        public String gOtString () {
            return string;
        }

        public void setString (String string) {
            this.string = string;
        }
    }

    @Test
    public void testIncorrectClassForIntrospectorFullMode () throws Exception {
        RecordClassDescriptor rcd = Introspector.createCustomIntrospector().introspectRecordClass(IncorrectClassForIntrospectorFullMode.class);
        assertEquals(IncorrectClassForIntrospectorFullMode.class.getName(), rcd.getName());
        assertEquals(0, rcd.getFields().length);
    }


    public static class DogMessage extends InstrumentMessage {
        private String name;

        public String getName () {
            return name;
        }

        public void setDogName (String name) {
            this.name = name;
        }

        private byte legsCount;

        public byte getLegsCount () {
            return legsCount;
        }

        public void setLegsCount (byte legsCount) {
            this.legsCount = legsCount;
        }

        private TestEnum color;

        @SchemaType (dataType = SchemaDataType.ENUM, nestedTypes = {TestEnum.class})
        public TestEnum getDogColor () {
            return color;
        }

        public void setDogColor (TestEnum color) {
            this.color = color;
        }


        private byte isInvalid;

        @SchemaType (dataType = SchemaDataType.BOOLEAN, isNullable = false)
        public byte getIsInvalid () {
            return isInvalid;
        }

        public void setIsInvalid (byte isInvalid) {
            this.isInvalid = isInvalid;
        }
    }

    @Test
    public void testIntrospectedCustomClass () throws Exception {
        RecordClassDescriptor rcd = Introspector.createCustomIntrospector().introspectRecordClass(DogMessage.class);
        assertEquals(DogMessage.class.getName(), rcd.getName());
        assertEquals(3, rcd.getFields().length);

        DataField df = rcd.getField("dogColor");
        assertNotNull(df);
        assertTrue(df.getType() instanceof EnumDataType);

        df = rcd.getField("isInvalid");
        assertNotNull(df);
        assertTrue(df.getType() instanceof BooleanDataType);
        assertFalse(df.getType().isNullable());

        df = rcd.getField("legsCount");
        assertNotNull(df);
        assertTrue(df.getType() instanceof IntegerDataType);
        assertEquals("INT8", df.getType().getEncoding());
    }

    public static class CustomClassWithRepeatingFields extends InstrumentMessage {
        private byte ty;
        private boolean getty;
        private boolean isTy;

        public boolean getty () {
            return getty;
        }

        public void setGetty (boolean getty) {
            this.getty = getty;
        }

        @SchemaType (dataType = SchemaDataType.BOOLEAN)
        public byte getTy () {
            return ty;
        }

        public boolean isTy () {
            return isTy;
        }

        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("CustomClassWithRepeatingFields{");
            sb.append("getty=").append(getty);
            sb.append(", ty=").append(ty);
            sb.append(", isTy=").append(isTy);
            sb.append('}');
            return sb.toString();
        }
    }

    @Test
    public void testCustomClassWithRepeatingFields () {
        Introspector introspector = Introspector.createCustomIntrospector();
        try {
            introspector.introspectRecordClass(CustomClassWithRepeatingFields.class);
        } catch (Introspector.IntrospectionException ex) {
            assertEquals("Field \"getTy\" can not be repeated in schema!".toLowerCase(), ex.getMessage().toLowerCase());
        }
        try {
            introspector.introspectRecordClass(CustomClass1.class);
        } catch (Introspector.IntrospectionException ex) {
            assertEquals("Field \"isAsk\" can not be repeated in schema!", ex.getMessage());
        }
    }

    public static class CustomClass1 extends InstrumentMessage {
        public boolean isAsk;

        public boolean isAsk () {
            return isAsk;
        }

        public void setIsAsk (boolean ask) {
            isAsk = ask;
        }
    }

    private static final Class[] ALL_MESSAGES = new Class[] {
            CalendarDateMessage.class,
            CalendarMessage.class,
            deltix.timebase.api.messages.calendar.TradingSession.class,
            CustomAttribute.class,
            OrderCancelRejectEvent.class,
            OrderNewEvent.class,
            OrderPendingCancelEvent.class,
            OrderPendingNewEvent.class,
            OrderPendingReplaceEvent.class,
            OrderStatusRequest.class,
            OrderTradeReportEvent.class,
            AbstractFuture.class,
            Bond.class,
            ContinuousFuture.class,
            CFD.class,
            Currency.class,
            CustomInstrument.class,
            Equity.class,
            ETF.class,
            Exchange.class,
            ExchangeTradedSynthetic.class,
            Future.class,
            GenericInstrument.class,
            Index.class,
            Option.class,
            PriceFormat.class,
            deltix.timebase.api.messages.securities.TradingSession.class,
            AdjustmentFactorMessage.class,
            BinaryMessage.class,
            ConnectionStatusChangeMessage.class,
            DataLossMessage.class,
            ErrorMessage.class,
            EventMessage.class,
            MarketOnCloseMessage.class,
            MarketOnOpenMessage.class,
            MarketStatusMessage.class,
            MetaDataChangeMessage.class,
            PacketEndMessage.class,
            RealTimeStartMessage.class,
            deltix.timebase.api.messages.service.SecurityDefinitionMessage.class,
            deltix.timebase.api.messages.service.SecurityFeedStatusMessage.class,
            SourceStatusMessage.class,
            StreamTruncatedMessage.class,
            SystemMessage.class,
            BaseEntry.class,
            BasePriceEntry.class,
            BookResetEntry.class,
            L1Entry.class,
            L2EntryNew.class,
            L2EntryUpdate.class,
            L3EntryNew.class,
            L3EntryUpdate.class,
            PackageHeader.class,
            TradeEntry.class,
            BarMessage.class,
            BestBidOfferMessage.class,
            DacMessage.class,
            L2Message.class,
            L2SnapshotMessage.class,
            Level2Action.class,
            Level2Message.class,
            MarketMessage.class,
            TradeMessage.class
    };

    @Test
    public void testClasses() throws Introspector.IntrospectionException {
        ResolverUtil<InstrumentMessage> resolver = new ResolverUtil<InstrumentMessage>();
        resolver.findImplementations(InstrumentMessage.class, "deltix.timebase");
        Set<Class<? extends InstrumentMessage>> classes = resolver.getClasses();

        for (Class<? extends InstrumentMessage> clazz : classes) {
            Introspector.createEmptyMessageIntrospector().introspectRecordClass(clazz);
        }
    }

    @Ignore
    public void testAllSchemaElements () throws Exception {
        final ObjectArrayList<Class> classes = new ObjectArrayList<>(ALL_MESSAGES.length);
        for (int i = 0; i < ALL_MESSAGES.length; i++) {
            if (classes.contains(ALL_MESSAGES[i]))
                fail("Please don`t duplicate input data!");
            else
                classes.add(ALL_MESSAGES[i]);
        }

        RecordClassDescriptor base;
        RecordClassDescriptor all;

        for (Class cls : classes) {
//            System.out.println("introspecting class: " + cls.getName());
            if (InstrumentMessage.class.isAssignableFrom(cls)) {
                base = Introspector.createEmptyMessageIntrospector().introspectRecordClass(cls);
                all = Introspector.createCustomIntrospector().introspectRecordClass(cls);
            } else {
                base = Introspector.createEmptyMessageIntrospector().introspectMemberClass("test introspection", cls);
                all = Introspector.createCustomIntrospector().introspectMemberClass("test introspection", cls);
            }

            assertNotNull(base);
            assertNotNull(all);

            // compare fields

            assertEquals("class: " + cls.getName(), all.getFields().length, base.getFields().length);

            for (DataField df1 : base.getFields()) {
                boolean contains = false;
                for (DataField df2 : all.getFields()) {
                    if (compareDataFields(df1, df2)) {
                        contains = true;
                        break;
                    }
                }
                assertTrue(contains);
            }
        }
    }

    private boolean compareDataFields (DataField df1, DataField df2) {
        return (Util.xequals(df1.getName(), df2.getName()) &&
                ((df1 instanceof NonStaticDataField && df2 instanceof NonStaticDataField) || (df1 instanceof StaticDataField && df2 instanceof StaticDataField)) &&
                (df1.getType().getCode() == df2.getType().getCode()));
    }


    public static class TestBindingProperties extends InstrumentMessage {
        private boolean ask;

        public boolean isAsk (boolean ask) {  //invalid getter  (have input parameter)
            return ask;
        }

        public void setAsk (boolean ask) {
            this.ask = ask;
        }

        private boolean last;

        public int isLast () { //invalid getter  return type for BooleanDataType
            return last ? 1 : 0;
        }

        public void setLast (boolean last) {
            this.last = last;
        }

        private boolean integer;

        public boolean getInteger () { //invalid getter return type for IntegerDataType
            return integer;
        }

        public void setInteger (boolean integer) {
            this.integer = integer;
        }

        private boolean ask2;

        public boolean isAsk2 () {
            return ask2;
        }

        public void setAsk2 (int ask2) { // invalid setter type for BooleanDataType
            this.ask2 = ask2 % 2 == 0;
        }

        private boolean ask3;

        public boolean isAsk3 () {
            return ask3;
        }

        public void setAsk3 (boolean ask3, String arg) { //invalid param count
            this.ask3 = ask3;
        }

        private int integer2;

        public int getInteger2 () {
            return integer2;
        }

        public void setInteger2 (boolean integer2) { //invalid type for IntegerDataType
            this.integer2 = integer2 ? 1 : 0;
        }

        private int count;

        public int getCount () {
            return count;
        }

        public void setCount (int count, int count2) { //invalid parameter count
            this.count = count + count2;
        }
    }

    @Test
    public void testBinding () throws Exception {
        final RecordClassDescriptor rcd = new RecordClassDescriptor(TestBindingProperties.class.getName(), null, false, null,
                new NonStaticDataField("ask", null, new BooleanDataType(false)),
                new NonStaticDataField("last", null, new BooleanDataType(false)),
                new NonStaticDataField("integer", null, new IntegerDataType("INT32", true)),
                new NonStaticDataField("ask2", null, new BooleanDataType(false)),
                new NonStaticDataField("ask3", null, new BooleanDataType(false)),
                new NonStaticDataField("integer2", null, new IntegerDataType("INT32", true)),
                new NonStaticDataField("count", null, new IntegerDataType("INT32", true))
        );
        FieldLayout layout = new FieldLayout(rcd, rcd.getField("ask"));

        layout.bind(TestBindingProperties.class); // incorrect getter (have 1 input parameter)
        assertFalse(layout.isBound());

        layout = new FieldLayout(rcd, rcd.getField("last"));
        layout.bind(TestBindingProperties.class); // incorrect getter (return nonBoolean type)
        assertFalse(layout.isBound());

        layout = new FieldLayout(rcd, rcd.getField("integer"));
        layout.bind(TestBindingProperties.class); // incorrect getter (return nonInteger type)
        assertFalse(layout.isBound());

        layout = new FieldLayout(rcd, rcd.getField("ask2"));
        layout.bind(TestBindingProperties.class); // incorrect setter (param type != boolean or byte type)
        assertFalse(layout.isBound());

        layout = new FieldLayout(rcd, rcd.getField("ask3"));
        layout.bind(TestBindingProperties.class); // incorrect setter (more than 1 param count)
        assertFalse(layout.isBound());

        layout = new FieldLayout(rcd, rcd.getField("integer2"));
        layout.bind(TestBindingProperties.class); // incorrect setter (param type != integer)
        assertFalse(layout.isBound());

        layout = new FieldLayout(rcd, rcd.getField("count"));
        layout.bind(TestBindingProperties.class); // incorrect setter (more than 1 param count)
        assertFalse(layout.isBound());
    }

    public static class TestBindingSmartProperties extends InstrumentMessage {
        private int integer1;

        public int getInteger1 () {
            return integer1;
        }

        public void setInteger1 (int integer1) {
            this.integer1 = integer1;
        }

        public void nullifyInteger1 () {
            this.integer1 = IntegerDataType.INT32_NULL;
        }

        public boolean hasInteger1 (int integer1) { //incorrect haser (input param != 0)
            return this.integer1 != IntegerDataType.INT32_NULL;
        }


        private int int2;

        public int getInt2 () {
            return int2;
        }

        public void setInt2 (int int2) {
            this.int2 = int2;
        }

        public void nullifyInt2 () {
            this.int2 = IntegerDataType.INT32_NULL;
        }

        public void hasInt2 () {  // incorrect haser (return VOID type)
            //
        }

        private int int3;

        public int getInt3 () {
            return int3;
        }

        public void setInt3 (int int3) {
            this.int3 = int3;
        }

        public void nullifyInt3 (int intNull) { //incorrect nullifier (input params count != 0)
            this.int3 = intNull;
        }

        public boolean hasInt3 () {
            return this.int3 != IntegerDataType.INT32_NULL;
        }
    }


    @Test
    public void testBindingSmartProperties () throws Exception {
        final RecordClassDescriptor rcd = new RecordClassDescriptor(TestBindingSmartProperties.class.getName(), null, false, null,
                new NonStaticDataField("integer1", null, new IntegerDataType("INT32", true)),
                new NonStaticDataField("int2", null, new IntegerDataType("INT32", true)),
                new NonStaticDataField("int3", null, new IntegerDataType("INT32", true))
        );
        FieldLayout layout = new FieldLayout(rcd, rcd.getField("integer1"));

        layout.bind(TestBindingSmartProperties.class); // incorrect haser (have 1 input parameter)
        assertTrue(layout.isBound());
        assertTrue(layout.hasAccessMethods());
        assertFalse(layout.hasSmartProperties());
        assertTrue(layout.hasNullifier());
        assertFalse(layout.hasHaser());

        layout = new FieldLayout(rcd, rcd.getField("int2"));
        layout.bind(TestBindingSmartProperties.class); // incorrect haser (return not boolean type)
        assertTrue(layout.isBound());
        assertTrue(layout.hasAccessMethods());
        assertFalse(layout.hasSmartProperties());
        assertTrue(layout.hasNullifier());
        assertFalse(layout.hasHaser());


        layout = new FieldLayout(rcd, rcd.getField("int3"));
        layout.bind(TestBindingSmartProperties.class); // incorrect nullifier (have 1 input parameter)
        assertTrue(layout.isBound());
        assertTrue(layout.isBound());
        assertTrue(layout.hasAccessMethods());
        assertFalse(layout.hasSmartProperties());
        assertFalse(layout.hasNullifier());
        assertTrue(layout.hasHaser());
    }


    public static class TestBindingPublicField extends InstrumentMessage {
        public int int1;

        @SchemaElement (name = "int2")
        public int count;
    }

    @Test
    public void testBindingPublicFields () throws Exception {
        final RecordClassDescriptor rcd = new RecordClassDescriptor(TestBindingPublicField.class.getName(), null, false, null,
                new NonStaticDataField("int1", null, new IntegerDataType("INT32", true)),
                new NonStaticDataField("int2", null, new IntegerDataType("INT32", true))
        );
        FieldLayout layout = new FieldLayout(rcd, rcd.getField("int1"));

        layout.bind(TestBindingPublicField.class); // incorrect haser (have 1 input parameter)
        assertTrue(layout.isBound());
        assertFalse(layout.hasAccessMethods());
        assertNotNull(layout.getJavaField());

        layout = new FieldLayout(rcd, rcd.getField("int2"));
        layout.bind(TestBindingPublicField.class); // incorrect haser (return not boolean type)
        assertTrue(layout.isBound());
        assertFalse(layout.hasSmartProperties());
        assertNotNull(layout.getJavaField());
    }

    public static class TestBindingPropertiesWithSchemaElement extends InstrumentMessage {
        private boolean bool1;

        @SchemaElement (name = "bool1")
        public boolean isAsk () {
            return bool1;
        }

        public void setIsAsk (boolean bool1) {
            this.bool1 = bool1;
        }

        private boolean bool2;

        @SchemaElement (name = "bool2")
        public boolean isLast () {
            return bool2;
        }

        public void setIsLast (boolean bool2) {
            this.bool2 = bool2;
        }

        private boolean bool3;

        @SchemaElement
        public boolean isBool3 () {
            return bool3;
        }

        public void setBool3 (boolean bool3) {
            this.bool3 = bool3;
        }

        private boolean bool4;

        @SchemaElement
        public boolean isBool4 () {
            return bool4;
        }

        public void setIsBool4 (boolean bool4) {
            this.bool4 = bool4;
        }


        private boolean bool5;

        @SchemaElement
        public boolean getBool5 () {
            return bool5;
        }

        public void setBool5 (boolean bool5) {
            this.bool5 = bool5;
        }

        private boolean bool6;

        public boolean isBool6 () {
            return bool6;
        }

        public void setBool6 (boolean bool6) {
            this.bool6 = bool6;
        }

        private boolean bool7;

        public boolean isBool7 () {
            return bool7;
        }

        public void setIsBool7 (boolean bool7) {
            this.bool7 = bool7;
        }

        private boolean bool8;

        public boolean getBool8 () {
            return bool8;
        }

        public void setBool8 (boolean bool8) {
            this.bool8 = bool8;
        }

        private int size;

        @SchemaElement (name = "int1")
        public int getSize () {
            return size;
        }

        public void setSize (int size) {
            this.size = size;
        }

        private int int2;

        @SchemaElement
        public int getInt2 () {
            return int2;
        }

        public void setInt2 (int int2) {
            this.int2 = int2;
        }

        private int int3;

        public int getInt3 () {
            return int3;
        }

        public void setInt3 (int int3) {
            this.int3 = int3;
        }
    }


    @Test
    public void testBindingPropertiesAllTypes () throws Exception {
        final RecordClassDescriptor rcd = new RecordClassDescriptor(TestBindingPropertiesWithSchemaElement.class.getName(), null, false, null,
                new NonStaticDataField("bool1", null, new BooleanDataType(false)),
                new NonStaticDataField("bool2", null, new BooleanDataType(false)),
                new NonStaticDataField("bool3", null, new BooleanDataType(false)),
                new NonStaticDataField("bool4", null, new BooleanDataType(false)),
                new NonStaticDataField("bool5", null, new BooleanDataType(false)),
                new NonStaticDataField("bool6", null, new BooleanDataType(false)),
                new NonStaticDataField("isBool7", null, new BooleanDataType(false)),
                new NonStaticDataField("bool8", null, new BooleanDataType(false)),

                new NonStaticDataField("int1", null, new IntegerDataType("INT32", true)),
                new NonStaticDataField("int2", null, new IntegerDataType("INT32", true)),
                new NonStaticDataField("int3", null, new IntegerDataType("INT32", true))
        );


        for (DataField field : rcd.getFields()) {
            FieldLayout layout = new FieldLayout(rcd, field);
            layout.bind(TestBindingPropertiesWithSchemaElement.class);
            assertTrue(layout.isBound());
            assertNull(layout.getJavaField());
            assertTrue(layout.hasAccessMethods());
            assertFalse(layout.hasSmartProperties());
        }
    }


    @Test // very slow test, because it compile codec for each standard message type
    public void testCodecOnAllMessagesComp () throws Exception {
        setUpComp();
        testCodecOnAllMessages();
    }


    @Test
    public void testCodecOnAllMessagesIntp () throws Exception {
        setUpIntp();
        testCodecOnAllMessages();
    }


    private void testCodecOnAllMessages () throws Exception {
        final ObjectArrayList<Class> classes = new ObjectArrayList<>(ALL_MESSAGES.length);
        for (int i = 0; i < ALL_MESSAGES.length; i++) {
            if (classes.contains(ALL_MESSAGES[i]))
                fail("Please don`t duplicate input data!");
            else
                classes.add(ALL_MESSAGES[i]);
        }

        RecordClassDescriptor rcd;
        for (Class cls : classes) {
            if (InstrumentMessage.class.isAssignableFrom(cls)) {
                rcd = Introspector.createEmptyMessageIntrospector().introspectRecordClass(cls);
            } else {
                rcd = Introspector.createEmptyMessageIntrospector().introspectMemberClass("test introspection", cls);
            }
            factory.createFixedBoundEncoder(CL, rcd);
            factory.createFixedBoundDecoder(CL, rcd);
        }
    }

    @Test
    public void testNotCompiledNamesComp() {
        setUpComp();
        testNotCompiledNames();
    }

    @Test
    public void testNotCompiledNamesIntp() {
        setUpIntp();
        testNotCompiledNames();
    }



    private void testNotCompiledNames() {
        final NotCompileClass msg = new NotCompileClass();
        final SimpleTypeLoader typeLoader = new SimpleTypeLoader(rcdContentClass.getName(), ContentClass.class, rcdNotCompileClass.getName(), NotCompileClass.class);
        for (int i = 0; i < 100; i ++) {
            switch (i % 2) {
                case 0:
                    msg.contentArray = null;
                    msg.contentField = null;
                    msg.contentProperty = null;
                    msg.contentPropertyArray = null;
                    break;
                default:
                    msg.contentArray = new ObjectArrayList<>();
                    msg.contentPropertyArray = new ObjectArrayList<>();

                    for (int  ii = 0; ii < i + 2; ii++) {
                        final ContentClass cls = new ContentClass();
                        cls.ieee64 = ii + i + 0.2;
                        cls.int32 = ii + i + 90;
                        msg.contentArray.add(cls);
                        msg.contentPropertyArray.add(cls);
                    }

                    final ContentClass cls = new ContentClass();
                    cls.ieee64 = i + 9.99;
                    cls.int32 = i + 1000;

                    msg.contentField = cls;
                    msg.contentProperty = cls;
                    break;
            }
            testBoundRoundTrip(msg, rcdNotCompileClass, typeLoader);
        }
    }

    @SchemaElement(
            name = "™Class+ %Name™",
            title = "not compile content class"
    )
    public static class ContentClass {
        @SchemaElement(name = "|0-0|")
        public double ieee64;

        private int int32;

        @SchemaElement(name = "0-0")
        public int getInt32 () {
            return int32;
        }

        public void setInt32 (int int32) {
            this.int32 = int32;
        }


        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("ContentClass{");
            sb.append("ieee64=").append(ieee64);
            sb.append(", int32=").append(int32);
            sb.append('}');
            return sb.toString();
        }
    }

    private static final RecordClassDescriptor rcdContentClass = new RecordClassDescriptor(
            "™Class+ %Name™",
            "not compile nested class",
            false,
            null,
            new NonStaticDataField("|0-0|", null, new FloatDataType("IEEE64", true)),
            new NonStaticDataField("0-0", null, new IntegerDataType("INT32", true))
    );

    @SchemaElement(
            name = "¤ ўЎ*SP0 s",
            title = "not compiled schema names"
    )
    public static class NotCompileClass extends InstrumentMessage {

        @SchemaElement(name = "091_qqq  ss filed")
        public ContentClass contentField;

        @SchemaElement(name = "poo{|To p    `Field")
        public ObjectArrayList<ContentClass> contentArray;

        private ContentClass contentProperty;

        private ObjectArrayList<ContentClass> contentPropertyArray;

        @SchemaElement(name = ".872")
        public ContentClass getContentProperty () {
            return contentProperty;
        }


        public void setContentProperty (ContentClass contentProperty) {
            this.contentProperty = contentProperty;
        }

        @SchemaElement(name = "0<<<<z")
        public ObjectArrayList<ContentClass> getContentPropertyArray () {
            return contentPropertyArray;
        }

        public void setContentPropertyArray (ObjectArrayList<ContentClass> contentPropertyArray) {
            this.contentPropertyArray = contentPropertyArray;
        }


        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("NotCompileClass{");
            sb.append("contentArray=").append(contentArray);
            sb.append(", contentField=").append(contentField);
            sb.append(", contentProperty=").append(contentProperty);
            sb.append(", contentPropertyArray=").append(contentPropertyArray);
            sb.append('}');
            return sb.toString();
        }
    }

    private static final RecordClassDescriptor rcdNotCompileClass = new RecordClassDescriptor(
            "¤ ўЎ*SP0 s",
            "not compiled schema names",
            false,
            null,
            new NonStaticDataField("091_qqq  ss filed", null, new ClassDataType(true, rcdContentClass)),
            new NonStaticDataField("poo{|To p    `Field", null, new ArrayDataType(true, new ClassDataType(true, rcdContentClass))),
            new NonStaticDataField(".872", null, new ClassDataType(true, rcdContentClass)),
            new NonStaticDataField("0<<<<z", null, new ArrayDataType(true, new ClassDataType(true, rcdContentClass)))
    );


    private void checkRoundTrip (NotCompileClass inMsg, NotCompileClass outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        if (inMsg.contentField == null)
            assertNull(outMsg.contentField);
        else
            checkRoundTrip(inMsg.contentField, outMsg.contentField);

        if (inMsg.contentProperty == null)
            assertNull(outMsg.contentProperty);
        else
            checkRoundTrip(inMsg.contentProperty, outMsg.contentProperty);

        if (inMsg.contentArray == null)
            assertNull(outMsg.contentArray);
        else {
            assertEquals(inMsg.contentArray.size(), outMsg.contentArray.size());
            for (int i = 0; i < inMsg.contentArray.size(); i++)
                checkRoundTrip(inMsg.contentArray.get(i), outMsg.contentArray.get(i));
        }

        if (inMsg.contentPropertyArray == null)
            assertNull(outMsg.contentPropertyArray);
        else {
            assertEquals(inMsg.contentPropertyArray.size(), outMsg.contentPropertyArray.size());
            for (int i = 0; i < inMsg.contentPropertyArray.size(); i++)
                checkRoundTrip(inMsg.contentPropertyArray.get(i), outMsg.contentPropertyArray.get(i));
        }
    }

    private void checkRoundTrip (ContentClass inMsg, ContentClass outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.int32, outMsg.int32);
        assertEquals(inMsg.ieee64, outMsg.ieee64, 0.001);
    }



    @Test
    public void testPackageHeaderComp() throws Exception {
        setUpComp();
        testPackageHeader();
    }

    @Test
    public void testPackageHeaderIntp() throws Exception {
        setUpIntp();
        testPackageHeader();
    }


    private void testPackageHeader() throws Exception {
        final RecordClassDescriptor rcdPackageHeader = Introspector.createEmptyMessageIntrospector().introspectRecordClass(PackageHeader.class);
        final PackageHeader msg = new PackageHeader();
        msg.setEntries(new ObjectArrayList<>());
        for (int i = 0; i < 100; i++) {

            msg.getEntries().clear();
            for (int j = 0; j < i + 9; j++)
                msg.getEntries().add(generateBaseEntry(j));
            switch (i % 4) {
                case 0:
                    msg.setPackageType(PackageType.INCREMENTAL_UPDATE);
                    break;
                case 1:
                    msg.setPackageType(PackageType.PERIODICAL_SNAPSHOT);
                    break;
                default:
                    msg.setPackageType(PackageType.VENDOR_SNAPSHOT);
                    break;
            }

            testBoundRoundTrip(msg, rcdPackageHeader, CL);
        }
    }

    private BaseEntryInfo generateBaseEntry(int i) {
        long time = System.currentTimeMillis();
        BaseEntryInfo msg;
        switch (i % 9) {
            case 0:
                msg = new TradeEntry();
                setBaseEntry((BaseEntry) msg, i + 90);
                TradeEntry m0 = (TradeEntry) msg;
                if (i *3 % 2 == 0) {
                    m0.nullifyPrice();
                    m0.nullifySize();
                    m0.nullifyCondition();
                    m0.nullifyTradeType();
                    m0.nullifySellerNumberOfOrders();
                    m0.nullifyBuyerNumberOfOrders();
                    m0.nullifySellerOrderId();
                    m0.nullifyBuyerOrderId();
                    m0.nullifySellerParticipantId();
                    m0.nullifyBuyerParticipantId();
                    m0.nullifySide();
                    m0.nullifyMatchId();
                } else {
                    m0.setPrice(time / 23);
                    m0.setSize(time/ 23);
                    m0.setCondition("Hi Alexey! +" + time);
                    m0.setTradeType(time / 2 == 0 ? TradeType.CANCELLATION : TradeType.AUCTION_CLEARING_PRICE);
                    m0.setSide(time / 2 == 0 ? AggressorSide.BUY : AggressorSide.SELL);
                    m0.setSellerNumberOfOrders(time / 2);
                    m0.setBuyerNumberOfOrders(time / 3);

                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < (time / (time - 1000)); j++)
                        sb.append((byte) j);

                    m0.setSellerOrderId(sb.toString());
                    m0.setBuyerOrderId(sb.toString());
                    m0.setSellerParticipantId(sb.toString());
                    m0.setBuyerParticipantId(sb.toString());
                    m0.setMatchId(sb.toString());

//                    for (int j = 0; j < (time / (time - 1000)); j++) {
//                        m0.getSellerOrderId().add((byte) j);
//                        m0.getBuyerOrderId().add((byte) j);
//                        m0.getSellerParticipantId().add((byte) j);
//                        m0.getBuyerParticipantId().add((byte) j);
//                        m0.getMatchId().add((byte) j);
//                    }
                }
                break;

            case 1:
                msg = new L1Entry();
                setBasePriceEntry((BasePriceEntry) msg, i + 10);
                L1Entry m1 = (L1Entry) msg;
                m1.setSide((i % 2 == 0) ? QuoteSide.BID : QuoteSide.ASK);
                if (i *3 % 2 == 0) {
                    m1.nullifyIsNational();
                } else {
                    m1.setIsNational(time % 2 == 0);
                }
                break;

            case 2:
                msg = new L2EntryNew();
                setBasePriceEntry((BasePriceEntry) msg, i + 20);
                L2EntryNew m2 = (L2EntryNew) msg;
                m2.setSide((i % 2 == 0) ? QuoteSide.BID : QuoteSide.ASK);
                m2.setLevel((short) i);
                break;

            case 3:
                msg = new L2EntryUpdate();
                setBasePriceEntry((BasePriceEntry) msg, i + 30);
                L2EntryUpdate m3 = (L2EntryUpdate) msg;
                if (time % 2 == 0)
                    m3.setSide((i % 2 == 0) ? QuoteSide.BID : QuoteSide.ASK);
                else
                    m3.nullifySide();
                m3.setAction((i % 2 == 0) ? BookUpdateAction.UPDATE : BookUpdateAction.DELETE);
                m3.setLevel((short) i);
                break;

            case 4:
                msg = new L3EntryNew();
                setBasePriceEntry((BasePriceEntry) msg, i + 40);
                L3EntryNew m4 = (L3EntryNew) msg;
                m4.setSide((i % 2 == 0) ? QuoteSide.BID : QuoteSide.ASK);
                if (time % 2 == 0)
                    m4.setInsertType((i % 2 == 0) ? InsertType.ADD_BACK : InsertType.ADD_FRONT);
                else
                    m4.nullifyInsertType();
                break;

            case 5:
                msg = new L3EntryUpdate();
                setBasePriceEntry((BasePriceEntry) msg, i + 50);
                L3EntryUpdate m5 = (L3EntryUpdate) msg;
                if (time % 2 == 0)
                    m5.setSide((i % 2 == 0) ? QuoteSide.BID : QuoteSide.ASK);
                else
                    m5.nullifySide();
                m5.setAction((i % 2 == 0) ? QuoteUpdateAction.CANCEL : QuoteUpdateAction.MODIFY);

                break;


            default:
                msg = new BookResetEntry();
                setBaseEntry((BaseEntry) msg, i + 80);
                BookResetEntry m6 = (BookResetEntry) msg;
                m6.setSide((i % 2 == 0) ? QuoteSide.BID : QuoteSide.ASK);
                m6.setModelType((i % 2 == 0) ? DataModelType.LEVEL_THREE : DataModelType.LEVEL_TWO);
                break;
        }
        return msg;
    }


    private void setBasePriceEntry(BasePriceEntryInterface msg, int i) {
        setBaseEntry((BaseEntry) msg, i + 1);

        if (i % 3 == 0) {
            msg.nullifyPrice();
            msg.nullifySize();
            msg.nullifyNumberOfOrders();
            msg.nullifyParticipantId();
            msg.nullifyQuoteId();
        } else {
            msg.setPrice(i);
            msg.setSize(i + i);

            msg.setNumberOfOrders(i + 898);

            StringBuilder pid = new StringBuilder();
            StringBuilder qid = new StringBuilder();

            for (int j = 0; j < i + 10; j++) {
                pid.append((byte) (i + j));
                qid.append((byte) (i + 2 * j));
            }

            msg.setParticipantId(pid);
            msg.setQuoteId(qid);
        }

    }

    private void setBaseEntry(BaseEntry msg, int i) {
        if (i % 3 == 0) {
            msg.nullifyIsImplied();
            msg.nullifyContractId();
            msg.nullifyExchangeId();
        } else {
            msg.setIsImplied(i % 2 == 0);
            msg.setContractId(codec.encodeToLong("NNY" + i));
            msg.setExchangeId(codec.encodeToLong(("NNY" + (i + 2))));
        }
    }

    AlphanumericCodec codec = new AlphanumericCodec(10);

    private void checkRoundTrip (PackageHeader inMsg, PackageHeader outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getPackageType(), outMsg.getPackageType());

        assertEquals(inMsg.getEntries().size(), outMsg.getEntries().size());

        for (int i = 0; i < inMsg.getEntries().size(); i++) {
            BaseEntryInfo in = inMsg.getEntries().get(i);
            BaseEntryInfo out = outMsg.getEntries().get(i);

            if (in instanceof TradeEntry) {
                assertTrue(out instanceof TradeEntry);
                checkRoundTrip ((TradeEntry) in, (TradeEntry) out);

            } else if (in instanceof L1Entry) {
                assertTrue(out instanceof L1Entry);
                checkRoundTrip ((L1Entry) in, (L1Entry) out);

            } else if (in instanceof L2EntryNew) {
                assertTrue(out instanceof L2EntryNew);
                checkRoundTrip ((L2EntryNew) in, (L2EntryNew) out);

            } else if (in instanceof L2EntryUpdate) {
                assertTrue(out instanceof L2EntryUpdate);
                checkRoundTrip ((L2EntryUpdate) in, (L2EntryUpdate) out);

            } else if (in instanceof L3EntryNew) {
                assertTrue(out instanceof L3EntryNew);
                checkRoundTrip ((L3EntryNew) in, (L3EntryNew) out);

            } else if (in instanceof L3EntryUpdate) {
                assertTrue(out instanceof L3EntryUpdate);
                checkRoundTrip ((L3EntryUpdate) in, (L3EntryUpdate) out);

            } else if (in instanceof BookResetEntry) {
                assertTrue(out instanceof BookResetEntry);
                checkRoundTrip ((BookResetEntry) in, (BookResetEntry) out);

            } else fail("Unsupported entry type: " + in.getClass().getName());
        }

    }

    private void checkRoundTrip (BookResetEntry inMsg, BookResetEntry outMsg) {
        checkBaseEntry(inMsg, outMsg);
        assertEquals(inMsg.toString(), outMsg.toString());

        if (inMsg.hasSide())
            assertEquals(inMsg.getSide(), outMsg.getSide());
        else
            assertFalse(outMsg.hasSide());

        if (inMsg.hasModelType())
            assertEquals(inMsg.getModelType(), outMsg.getModelType());
        else
            assertFalse(outMsg.hasModelType());
    }


    private void checkRoundTrip (L3EntryUpdate inMsg, L3EntryUpdate outMsg) {
        checkBasePriceEntry(inMsg, outMsg);
        assertEquals(inMsg.toString(), outMsg.toString());

        if (inMsg.hasSide())
            assertEquals(inMsg.getSide(), outMsg.getSide());
        else
            assertFalse(outMsg.hasSide());

        if (inMsg.hasAction())
            assertEquals(inMsg.getAction(), outMsg.getAction());
        else
            assertFalse(outMsg.hasAction());
    }

    private void checkRoundTrip (L3EntryNew inMsg, L3EntryNew outMsg) {
        checkBasePriceEntry(inMsg, outMsg);
        assertEquals(inMsg.toString(), outMsg.toString());

        if (inMsg.hasSide())
            assertEquals(inMsg.getSide(), outMsg.getSide());
        else
            assertFalse(outMsg.hasSide());

        if (inMsg.hasInsertType())
            assertEquals(inMsg.getInsertType(), outMsg.getInsertType());
        else
            assertFalse(outMsg.hasInsertType());
    }

    private void checkRoundTrip (L2EntryUpdate inMsg, L2EntryUpdate outMsg) {
        checkBasePriceEntry(inMsg, outMsg);
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getLevel(), outMsg.getLevel());

        if (inMsg.hasSide())
            assertEquals(inMsg.getSide(), outMsg.getSide());
        else
            assertFalse(outMsg.hasSide());

        if (inMsg.hasAction())
            assertEquals(inMsg.getAction(), outMsg.getAction());
        else
            assertFalse(outMsg.hasAction());
    }

    private void checkRoundTrip (L2EntryNew inMsg, L2EntryNew outMsg) {
        checkBasePriceEntry(inMsg, outMsg);
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getLevel(), outMsg.getLevel());

        if (inMsg.hasSide())
            assertEquals(inMsg.getSide(), outMsg.getSide());
        else
            assertFalse(outMsg.hasSide());
    }

    private void checkRoundTrip (L1Entry inMsg, L1Entry outMsg) {
        checkBasePriceEntry(inMsg, outMsg);
        assertEquals(inMsg.toString(), outMsg.toString());

        if (inMsg.hasIsNational())
            assertEquals(inMsg.isNational(), outMsg.isNational());
        else
            assertFalse(outMsg.hasIsNational());

        if (inMsg.hasSide())
            assertEquals(inMsg.getSide(), outMsg.getSide());
        else
            assertFalse(outMsg.hasSide());
    }

    private void checkBasePriceEntry (BasePriceEntry inMsg, BasePriceEntry outMsg) {
        checkBaseEntry(inMsg, outMsg);
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getPrice(), outMsg.getPrice(), DELTA);
        assertEquals(inMsg.getSize(), outMsg.getSize(), DELTA);
        assertEquals(inMsg.getNumberOfOrders(), outMsg.getNumberOfOrders());

        if (inMsg.hasQuoteId()) {
            checkEquals(inMsg.getQuoteId(), outMsg.getQuoteId());
        } else
            assertFalse(outMsg.hasQuoteId());

        if (inMsg.hasParticipantId())
            checkEquals(inMsg.getParticipantId(), outMsg.getParticipantId());
        else
            assertFalse(outMsg.hasParticipantId());
    }

    private void checkBaseEntry (BaseEntry inMsg, BaseEntry outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        if (inMsg.hasIsImplied())
            assertEquals(inMsg.hasIsImplied(), outMsg.hasIsImplied());
        else
            assertFalse(outMsg.hasIsImplied());

        assertEquals(inMsg.getExchangeId(), outMsg.getExchangeId());
        assertEquals(inMsg.getContractId(), outMsg.getContractId());
    }


    private void checkRoundTrip (TradeEntry inMsg, TradeEntry outMsg) {
        checkBaseEntry(inMsg, outMsg);
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getPrice(), outMsg.getPrice(), DELTA);
        assertEquals(inMsg.getSize(), outMsg.getSize(), DELTA);

        if (inMsg.hasCondition())
            assertEquals(inMsg.getCondition().toString(), outMsg.getCondition().toString());
        else
            assertFalse(outMsg.hasCondition());


        if (inMsg.hasTradeType())
            assertEquals(inMsg.getTradeType(), outMsg.getTradeType());
        else
            assertFalse(outMsg.hasTradeType());

        assertEquals(inMsg.getSellerNumberOfOrders(), outMsg.getSellerNumberOfOrders());
        assertEquals(inMsg.getBuyerNumberOfOrders(), outMsg.getBuyerNumberOfOrders());


        if (inMsg.hasSellerOrderId())
            checkEquals(inMsg.getSellerOrderId(), outMsg.getSellerOrderId());
        else
            assertFalse(outMsg.hasSellerOrderId());

        if (inMsg.hasBuyerOrderId())
            checkEquals(inMsg.getBuyerOrderId(), outMsg.getBuyerOrderId());
        else
            assertFalse(outMsg.hasBuyerOrderId());

        if (inMsg.hasSellerParticipantId())
            checkEquals(inMsg.getSellerParticipantId(), outMsg.getSellerParticipantId());
        else
            assertFalse(outMsg.hasSellerParticipantId());

        if (inMsg.hasBuyerParticipantId())
            checkEquals(inMsg.getBuyerParticipantId(), outMsg.getBuyerParticipantId());
        else
            assertFalse(outMsg.hasBuyerParticipantId());

        if (inMsg.hasSide())
            assertEquals(inMsg.getSide(), outMsg.getSide());
        else
            assertFalse(outMsg.hasSide());

        if (inMsg.hasMatchId())
            checkEquals(inMsg.getMatchId(), outMsg.getMatchId());
        else
            assertFalse(outMsg.hasMatchId());
    }

    public static void checkEquals(CharSequence expected, CharSequence actual) {
        if (Util.compare(expected, actual, false) != 0)
            throw new ComparisonFailure("", expected.toString(), actual.toString());
    }

    public static final RecordClassDescriptor rcdMarketWithStaticCurrency = new RecordClassDescriptor(
            MarketMessage.class.getName(),
            "static currency code with int64 encoding",
            false,
            null,
            new StaticDataField("originalTimestamp", null, new DateTimeDataType(true), null),
            new StaticDataField("currencyCode", null, new IntegerDataType("INT64", true), 840),
            new StaticDataField("sequenceNumber", null, new IntegerDataType("INT64", true), null),
            new StaticDataField("sourceId", null, new VarcharDataType("ALPHANUMERIC(10)", true, true), null)
    );

    public static final RecordClassDescriptor rcdBarWithStaticCurrency = new RecordClassDescriptor(
            BarMessage.class.getName(),
            "static currency code with int64 encoding",
            false,
            rcdMarketWithStaticCurrency,
            new StaticDataField("exchangeId", null, new VarcharDataType("ALPHANUMERIC(10)", true, false), null),
            new NonStaticDataField("close", null, new FloatDataType("DECIMAL", true)),
            new NonStaticDataField("open", null, new FloatDataType("DECIMAL", true), "close"),
            new NonStaticDataField("high", null, new FloatDataType("DECIMAL", true), "close"),
            new NonStaticDataField("low", null, new FloatDataType("DECIMAL", true), "close"),
            new NonStaticDataField("volume", null, new FloatDataType("DECIMAL", true))
    );

    private void checkBaseEntry (BarMessage inMsg, BarMessage outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.getOriginalTimestamp(), outMsg.getOriginalTimestamp());
        assertEquals(inMsg.getCurrencyCode(), outMsg.getCurrencyCode());
        assertEquals(inMsg.getSequenceNumber(), outMsg.getSequenceNumber());
        assertEquals(inMsg.getSourceId(), outMsg.getSourceId());
        assertEquals(inMsg.getExchangeId(), outMsg.getExchangeId());
        assertEquals(inMsg.getClose(), outMsg.getClose(), DELTA);
        assertEquals(inMsg.getOpen(), outMsg.getOpen(), DELTA);
        assertEquals(inMsg.getHigh(), outMsg.getHigh(), DELTA);
        assertEquals(inMsg.getLow(), outMsg.getLow(), DELTA);
        assertEquals(inMsg.getVolume(), outMsg.getVolume(), DELTA);
    }

    private void checkBaseEntry (BarMsg inMsg, BarMsg outMsg) {
        assertEquals(inMsg.toString(), outMsg.toString());

        assertEquals(inMsg.originalTimestamp, outMsg.originalTimestamp);
        assertEquals(inMsg.currencyCode, outMsg.currencyCode);
        assertEquals(inMsg.sequenceNumber, outMsg.sequenceNumber);
        assertEquals(inMsg.sourceId, outMsg.sourceId);
        assertEquals(inMsg.exchangeId, outMsg.exchangeId);
        assertEquals(inMsg.close, outMsg.close, DELTA);
        assertEquals(inMsg.open, outMsg.open, DELTA);
        assertEquals(inMsg.high, outMsg.high, DELTA);
        assertEquals(inMsg.low, outMsg.low, DELTA);
        assertEquals(inMsg.volume, outMsg.volume, DELTA);
    }

    public static class MarketMsg extends InstrumentMessage {
        public long originalTimestamp;
        public short currencyCode;
        public long sequenceNumber;
        public long sourceId;

        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("MarketMsg{");
            sb.append("currencyCode=").append(currencyCode);
            sb.append(", originalTimestamp=").append(originalTimestamp);
            sb.append(", sequenceNumber=").append(sequenceNumber);
            sb.append(", sourceId=").append(sourceId);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class BarMsg extends MarketMsg {
        public double open;
        public double high;
        public double low;
        public double close;
        public double volume;
        public long exchangeId;

        @Override
        public String toString () {
            final StringBuffer sb = new StringBuffer("BarMsg{");
            sb.append("close=").append(close);
            sb.append(", open=").append(open);
            sb.append(", high=").append(high);
            sb.append(", low=").append(low);
            sb.append(", volume=").append(volume);
            sb.append(", exchangeId=").append(exchangeId);
            sb.append('}');
            sb.append(super.toString());
            return sb.toString();
        }
    }

    @Test
    public void testStaticCastNewFormatComp() {
        setUpComp();
        testStaticCastNewFormat();
    }

    @Test
    public void testStaticCastNewFormatIntp() {
        setUpIntp();
        testStaticCastNewFormat();
    }

    private void testStaticCastNewFormat() {
        final BarMessage msg = new BarMessage();
        msg.setCurrencyCode((short) 840);
        msg.setExchangeId(Long.MIN_VALUE);
        msg.setOriginalTimestamp(Long.MIN_VALUE);
        msg.setSequenceNumber(Long.MIN_VALUE);
        msg.setSourceId(Long.MIN_VALUE);
        msg.setOpen(90.0);
        msg.setClose(100.0);
        testBoundRoundTrip(msg, rcdBarWithStaticCurrency, CL);
    }

    @Test
    public void testStaticCastPOJOFormatComp() {
        setUpComp();
        testStaticCastPOJOFormat();
    }

    @Test
    public void testStaticCastPOJOFormatIntp() {
        setUpIntp();
        testStaticCastPOJOFormat();
    }

    private void testStaticCastPOJOFormat() {
        final SimpleTypeLoader typeLoader = new SimpleTypeLoader(rcdBarWithStaticCurrency.getName(), BarMsg.class);
        final BarMsg msg = new BarMsg();
        msg.currencyCode = (short) 840;
        msg.open = 90.0;
        msg.close = 100.0;
        msg.exchangeId = (Long.MIN_VALUE);
        msg.originalTimestamp = (Long.MIN_VALUE);
        msg.sequenceNumber = (Long.MIN_VALUE);
        msg.sourceId = (Long.MIN_VALUE);
        testBoundRoundTrip(msg, rcdBarWithStaticCurrency, typeLoader);
    }




//    @Test
//    public void printSchemaDifference () throws Introspector.IntrospectionException {
//        final Class<?>[] standardMessages = new Class[] {
//                BarMessage.class,

//                TradeMessage.class,
//                BestBidOfferMessage.class,
//                Level2Message.class,
//                L2SnapshotMessage.class
//        };
//
//        char[] chars = new char[55];
//
//        for (Class cls : standardMessages) {
//            RecordClassDescriptor rcdIntr = Introspector.createEmptyMessageIntrospector().introspectRecordClass(cls);
//            ClassDescriptor rcdSCH = Introspector.createMessageIntrospector().getClassDescriptor(cls);
//
//            System.out.println(cls.getName());
//            System.out.println("Introspector       |        StreamConfigurationHelper");
//            System.out.println("------------------------------------------------------");
//            assertNotNull(rcdIntr);
//            assertNotNull(rcdSCH);
//
//            DataField[] dfMin, dfMax;
//            boolean changes = rcdIntr.getFields().length < ((RecordClassDescriptor) rcdSCH).getFields().length;
//            if (changes) {
//                dfMin = rcdIntr.getFields();
//                dfMax = ((RecordClassDescriptor) rcdSCH).getFields();
//            } else {
//                dfMax = rcdIntr.getFields();
//                dfMin = ((RecordClassDescriptor) rcdSCH).getFields();
//            }
//            Arrays.sort(dfMax, ((o1, o2) -> o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase())));
//            Arrays.sort(dfMin, ((o1, o2) -> o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase())));
//
//            for (int i = 0; i < dfMin.length; i++) {
//                int middle = chars.length / 2;
//                for (int j = 0; j < chars.length; j++)
//                    chars[j] = (j == (middle - 8)) ? '|' : ' ';
//
//                DataField df1 = changes ? dfMin[i] : dfMax[i];
//                DataField df2 = changes ? dfMax[i] : dfMin[i];
//                System.arraycopy(df1.getName().toCharArray(), 0, chars, 0, df1.getName().length());
//                System.arraycopy(df2.getName().toCharArray(), 0, chars, middle + 1, df2.getName().length());
//                System.out.println(chars);
//            }
//            for (int i = dfMin.length; i < dfMax.length; i++) {
//                if (changes) {
//                    int middle = chars.length / 2;
//                    for (int j = 0; j < chars.length; j++)
//                        chars[j] = (j == (middle - 8)) ? '|' : ' ';
//                    System.arraycopy(dfMax[i].getName().toCharArray(), 0, chars, middle + 1, dfMax[i].getName().length());
//                    System.out.println(chars);
//                } else {
//                    System.out.println(dfMax[i].getName());
//                }
//            }
//
//            System.out.println("--------------------------------------\n\n");
//        }
//    }


}
