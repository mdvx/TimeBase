package deltix.qsrv.hf.pub;

import deltix.qsrv.hf.codec.ArrayTypeUtil;
import deltix.qsrv.hf.codec.cg.CodecGenerator;
import deltix.qsrv.hf.pub.codec.*;
import deltix.qsrv.hf.pub.md.*;
import deltix.timebase.api.messages.InstrumentType;
import deltix.timebase.messages.InstrumentMessage;
import deltix.util.collections.generated.*;
import deltix.util.io.BasicIOUtil;
import deltix.util.jcg.*;
import deltix.util.jcg.scg.SourceCodePrinter;
import deltix.util.lang.Util;
import deltix.util.memory.MemoryDataInput;
import deltix.util.memory.MemoryDataOutput;
import deltix.util.time.GMT;
import deltix.util.time.TimeFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static deltix.qsrv.hf.tickdb.lang.compiler.cg.QCGHelpers.CTXT;
import static java.lang.reflect.Modifier.*;

/**
 * Date: 8/1/11
 *
 * @author BazylevD
 */
public class CodecTestGen {
    private static final JAnnotation TEST_ANNOTATION = CTXT.annotation(Test.class);
    private static final JAnnotation OVERRIDE_ANNOTATION = CTXT.annotation(Override.class);
    private static final JExpr COMMA_STRING = CTXT.stringLiteral(",");
    private static final Object SKIP_VALUE = new Object();
    private static final String NOT_NULL = "NotNull";
    private static final String TO_STRING = "toString";
    private static final String EQUALS =  "equals";
    private JClass jclass;
    private final HashMap<String, JClassDefinition> recordClassMap = new HashMap<String, JClassDefinition>();
    private final HashMap<String, JExpr> rcdMap = new HashMap<String, JExpr>();
    private final static HashMap<String, EnumClassDescriptor> ecdMap = new HashMap<String, EnumClassDescriptor>();
    // generate .NET auto-property instead of fields
    private static boolean useProperty = false;

    private final static class JClassDefinition {
        private final JClass jclass;
        private final boolean isPublic;
        private final Class<?>[] types;
        private final String[] fieldNames;

        private JClassDefinition(JClass jclass, boolean isPublic, Class<?>[] types, String[] fieldNames) {
            this.jclass = jclass;
            this.isPublic = isPublic;
            this.types = types;
            this.fieldNames = fieldNames;
        }

        private Class<?> getFieldType(String fieldName) {
            for (int i = 0; i < fieldNames.length; i++) {
                if (fieldNames[i].equals(fieldName))
                    return types[i];
            }
            throw new IllegalArgumentException(fieldName + " field not found");
        }

        private String getFieldName(Class<?> fieldType) {
            for (int i = 0; i < fieldNames.length; i++) {
                if (types[i] == fieldType)
                    return fieldNames[i];
            }
            throw new IllegalArgumentException(fieldType.getName() + " type not found");
        }
    }

    public static void main(String[] args) throws IOException {
        final CodecTestGen gen = new CodecTestGen();
        gen.generateHostClass("deltix.qsrv.hf.pub.Test_RecordCodecs5");


        gen.testBooleanDataType();
        gen.testDataType(INTEGER_ENTRY, false);
        gen.testIntegerDataTypeBinding();
        gen.testIntegerDataTypeRange();
        gen.testDataType(FLOAT_ENTRY, false);
        gen.testDataType(STRING_ENTRY, false);
        gen.testEnumDataType(false);
        gen.testBitmaskDataType(false);
        gen.testDataType(DATETIME_ENTRY, false);
        gen.testDataType(TIMEOFDAY_ENTRY, false);
        gen.testDataType(CHAR_ENTRY, false);

        if (!useProperty) {
            gen.testEarlyBinding();
            gen.testEarlyBindingStatic();
            gen.testArrayOtherBindings();
        }

        final StringBuilder sb = new StringBuilder();
        String code = CodecGenerator.toString(gen.jclass);
        sb.append("package ").append(gen.jclass.packageName()).append(";\n");
        sb.append(code);

        //String code = toStringTemp(gen.jclass);
        if (args.length > 0) {
            BasicIOUtil.writeTextFile(args[0], sb.toString());
        } else
            System.out.println(sb);
    }

    private static String toStringTemp(JClass jclass) {
        final StringBuilder buf = new StringBuilder();
        final SourceCodePrinter p = new SourceCodePrinter(buf);

        try {
            p.print(jclass);
            return buf.toString();
        } catch (IOException iox) {
            throw new deltix.util.io.UncheckedIOException(iox);
        } catch (Exception iox) {
            System.out.println(buf);
            iox.printStackTrace();
            return null;
        }
    }

    private static String toString(Object obj) {
        final StringBuilder buf = new StringBuilder();
        final SourceCodePrinter p = new SourceCodePrinter(buf);

        try {
            p.print(obj);
            return buf.toString();
        } catch (IOException iox) {
            throw new deltix.util.io.UncheckedIOException(iox);
        } catch (Exception iox) {
            System.out.println(buf);
            iox.printStackTrace();
            return null;
        }
    }

    /*
     * All IntegerDataType encoding with standard binding
     * All IntegerDataType encoding with standard binding to nullable (.NET only)
     */

    private final static Class<?>[] INT_FIELD_TYPES = {
            byte.class,
            short.class,
            int.class,
            long.class,
            long.class,     // INT48
            int.class,      // PUINT30
            long.class,     // PUINT61
            int.class       // PINTERVAL
    };

    private final static Class<?>[] INT_FIELD_TYPES_NULLABLE =  null;

    private final static String[] INT_ENCODINGS = {
            IntegerDataType.ENCODING_INT8,
            IntegerDataType.ENCODING_INT16,
            IntegerDataType.ENCODING_INT32,
            IntegerDataType.ENCODING_INT64,
            IntegerDataType.ENCODING_INT48,
            IntegerDataType.ENCODING_PUINT30,
            IntegerDataType.ENCODING_PUINT61,
            IntegerDataType.ENCODING_PINTERVAL
    };

    private final static Object[] INT_NORMAL_VALUES = {
            (byte) 0x55,
            (short) -30001,
            2000000001,
            2000000000001L,
            0x7FFFFFFFFF0AL,
            0x3FFFFFFE,
            0x1FFFFFFFFFFFFFFEL,
            CTXT.staticVarRef(Integer.class, "MAX_VALUE")
    };

    private final static Object[] INT_NULL_VALUES =
            new Object[]{
                    CTXT.staticVarRef(IntegerDataType.class, "INT8_NULL"),
                    CTXT.staticVarRef(IntegerDataType.class, "INT16_NULL"),
                    CTXT.staticVarRef(IntegerDataType.class, "INT32_NULL"),
                    CTXT.staticVarRef(IntegerDataType.class, "INT64_NULL"),
                    CTXT.staticVarRef(IntegerDataType.class, "INT48_NULL"),
                    CTXT.staticVarRef(IntegerDataType.class, "PUINT30_NULL"),
                    CTXT.staticVarRef(IntegerDataType.class, "PUINT61_NULL"),
                    CTXT.staticVarRef(IntegerDataType.class, "PINTERVAL_NULL")
            };

    private final static Object[] INT_NULL_VALUES_4NET = null;

    private final static DataTypeEntry INTEGER_ENTRY = new DataTypeEntry(
            "Integer",
            IntegerDataType.class,
            INT_FIELD_TYPES,
            INT_FIELD_TYPES_NULLABLE,
            INT_ENCODINGS,
            INT_NORMAL_VALUES,
            INT_NULL_VALUES,
            INT_NULL_VALUES_4NET
    );

    private void testRoundTrip(JClass rclass, boolean isPublic, JMethod method, Object[] normal_values, Object[] null_values) {
        final JCompoundStatement body = CTXT.compStmt();
        method.body().add(body);

        String rcdName = getRCDName(rclass, true, false, null);
        JExpr rcdNullable = rcdMap.get(rcdName);
        assert rcdNullable != null;
        rcdName = getRCDName(rclass, false, false, null);
        JExpr rcdNonNullable = rcdMap.get(rcdName);
        assert rcdNonNullable != null;
        rcdName = getRCDName(rclass, true, true, null);
        JExpr rcdNullableStatic = rcdMap.get(rcdName);
        assert rcdNullableStatic != null;
        rcdName = getRCDName(rclass, false, true, null);
        JExpr rcdNonNullableStatic = rcdMap.get(rcdName);
        assert rcdNonNullableStatic != null;
        rcdName = getRCDName(rclass, true, true, "NV");
        JExpr rcdNullableStaticNV = rcdMap.get(rcdName);
        assert rcdNullableStaticNV != null;
        rcdName = getRCDName(rclass, true, true, "NV_4NET");
        JExpr rcdNullableStaticNV_4NET = rcdMap.get(rcdName);


        JExpr msg = body.addVar(0, rclass, "msg", CTXT.newExpr(rclass));

        assignFieldValues(body, rclass, msg, normal_values);
        // non-static nullable
        JExpr textExpr = getStringLiteral((isPublic ? "public" : "private") + " nullable - normal values");
        body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullable));
        // non-static non-nullable
        textExpr = getStringLiteral((isPublic ? "public" : "private") + " non-nullable - normal values");
        body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNonNullable));
        if (!useProperty || isPublic) {
            // static nullable
            textExpr = getStringLiteral((isPublic ? "public" : "private") + " nullable static - normal values");
            body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullableStatic));
            // static non-nullable
            textExpr = getStringLiteral((isPublic ? "public" : "private") + " non-nullable static - normal values");
            body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNonNullableStatic));
        }

        assignFieldValues(body, rclass, msg, null_values);
        // non-static nullable
        textExpr = getStringLiteral((isPublic ? "public" : "private") + " nullable - null values");
        body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullable));
        if (!useProperty || isPublic) {
            // static nullable (null values)
            textExpr = getStringLiteral((isPublic ? "public" : "private") + " nullable static - null values");
            body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullableStaticNV));
            if (rcdNullableStaticNV_4NET != null) {
                textExpr = getStringLiteral((isPublic ? "public" : "private") + " nullable static 4NET - null values");
                body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullableStaticNV_4NET));
            }
        }
    }

    private void createIntegerDataTypeRCD(JClass rclass, boolean isNullable) {
        final DataType[] dataTypes = new DataType[INT_ENCODINGS.length];
        for (int i = 0; i < INT_ENCODINGS.length; i++)
            dataTypes[i] = new IntegerDataType(INT_ENCODINGS[i], isNullable);


        final String name = getRCDName(rclass, isNullable, false, null);
        JInitMemberVariable rcd = jclass.addVar(PRIVATE | FINAL | STATIC, RecordClassDescriptor.class, name,
                genRCDInitializer(
                        rclass,
                        false,
                        dataTypes
                )
        );
        rcdMap.put(name, rcd.access());
    }

    private void createRCD(JClass rclass, boolean isNullable, Class<? extends DataType> dataTypeClass, String[] encodings) {
        final DataType[] dataTypes = new DataType[encodings.length];
        for (int i = 0; i < encodings.length; i++)
            dataTypes[i] = newDataType(dataTypeClass, encodings[i], isNullable);

        final String name = getRCDName(rclass, isNullable, false, null);
        JInitMemberVariable rcd = jclass.addVar(PRIVATE | FINAL | STATIC, RecordClassDescriptor.class, name,
                genRCDInitializer(
                        rclass,
                        false,
                        dataTypes
                )
        );
        rcdMap.put(name, rcd.access());
    }

    private void createArrayRCD(JClass rclass, boolean isNullable, DataType[] dataTypes, String postfix) {
        final DataType[] arrayDataTypes = new DataType[dataTypes.length];
        for (int i = 0; i < dataTypes.length; i++)
            arrayDataTypes[i] = dataTypes[i] != null ? new ArrayDataType(isNullable, dataTypes[i]) : null;

        final String name = getRCDName(rclass, isNullable, false, postfix);
        JInitMemberVariable rcd = jclass.addVar(PRIVATE | FINAL | STATIC, RecordClassDescriptor.class, name,
                genRCDInitializer(
                        rclass,
                        false,
                        arrayDataTypes
                )
        );
        rcdMap.put(name, rcd.access());
    }

    private void createStaticRCD(JClass rclass, boolean isNullable, Class<? extends DataType> dataTypeClass, String[] encodings, Object[] values, String postfix) {
        final String[] fields = recordClassMap.get(rclass.fullName()).fieldNames;
        if (fields == null)
            throw new IllegalArgumentException("Unknown class definition " + rclass.fullName());
        if (fields.length != values.length)
            throw new IllegalArgumentException("Values and fields length are different " + values.length + ", " + fields.length);
        if (encodings.length != values.length)
            throw new IllegalArgumentException();

        final DataType[] dataTypes = new DataType[encodings.length];
        for (int i = 0; i < encodings.length; i++)
            dataTypes[i] = newDataType(dataTypeClass, encodings[i], isNullable);

        final String name = getRCDName(rclass, isNullable, true, postfix);
        JInitMemberVariable rcd = jclass.addVar(PRIVATE | FINAL | STATIC, RecordClassDescriptor.class, name,
                genRCDInitializer(
                        rclass,
                        true,
                        dataTypes,
                        values
                )
        );
        rcdMap.put(name, rcd.access());
    }

    private void createIntegerDataTypeStaticRCD(JClass rclass, boolean isNullable, Object[] values, String postfix) {
        JInitVariable rcd = createIntegerDataTypeStaticRCD(jclass, rclass, isNullable, values, postfix);
        rcdMap.put(rcd.name(), ((JInitMemberVariable) rcd).access());
    }

    private JInitVariable createIntegerDataTypeStaticRCD(JVariableContainer body, JClass rclass, boolean isNullable, Object[] values, String postfix) {
        final String[] fields = recordClassMap.get(rclass.fullName()).fieldNames;
        if (fields == null)
            throw new IllegalArgumentException("Unknown class definition " + rclass.fullName());
        if (fields.length != values.length)
            throw new IllegalArgumentException("Values and fields length are different " + values.length + ", " + fields.length);
        if (INT_ENCODINGS.length != values.length)
            throw new IllegalArgumentException();


        final DataType[] dataTypes = new DataType[INT_ENCODINGS.length];
        for (int i = 0; i < INT_ENCODINGS.length; i++)
            dataTypes[i] = new IntegerDataType(INT_ENCODINGS[i], isNullable);

        final String name = getRCDName(rclass, isNullable, true, postfix);
        return body.addVar(PRIVATE | FINAL | STATIC, RecordClassDescriptor.class, name,
                genRCDInitializer(
                        rclass,
                        true,
                        dataTypes,
                        values
                )
        );
    }

    /*
     * All IntegerDataType encoding with non-standard binding
     */

    private final static Object[] INT_NULL_VALUES_SIGNED = INT_NULL_VALUES;

    private final static Object[] INT_NULL_VALUES_4SIGNED = null;

    public void testIntegerDataTypeBinding() {
        final JMethod methodImpl = jclass.addMethod(PRIVATE, void.class, "testIntegerDataTypeBinding");
        testIntegerDataTypeBinding(long.class, methodImpl);
        testIntegerDataTypeBinding(int.class, methodImpl);
        testIntegerDataTypeBinding(short.class, methodImpl);
        addCompIntpTests("testIntegerDataTypeBinding", methodImpl);
    }

    private void testIntegerDataTypeBinding(Class<?> clazz, JMethod methodImpl) {
        // populate field types
        final Class<?>[] types = new Class<?>[INT_FIELD_TYPES.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = MdUtil.getSize(clazz) >= MdUtil.getSize(INT_FIELD_TYPES[i]) ?
                    clazz :
                    INT_FIELD_TYPES[i];
        }

        JClass rClassPublic = addRecordClass(getName(clazz, true),
                true,
                INT_NULL_VALUES_4SIGNED,
                types
        );
        createIntegerDataTypeRCD(rClassPublic, true);
        createIntegerDataTypeRCD(rClassPublic, false);
        createIntegerDataTypeStaticRCD(rClassPublic, true, INT_NORMAL_VALUES, null);
        createIntegerDataTypeStaticRCD(rClassPublic, false, INT_NORMAL_VALUES, null);
        createIntegerDataTypeStaticRCD(rClassPublic, true, INT_NULL_VALUES_SIGNED, "NV");

        // private property is not supported
        final JClass rClassPrivate;
        if (!useProperty) {
            rClassPrivate = addRecordClass(getName(clazz, false),
                    true,
                    INT_NULL_VALUES_4SIGNED,
                    types
            );
            createIntegerDataTypeRCD(rClassPrivate, true);
            createIntegerDataTypeRCD(rClassPrivate, false);
            createIntegerDataTypeStaticRCD(rClassPrivate, true, INT_NORMAL_VALUES, null);
            createIntegerDataTypeStaticRCD(rClassPrivate, false, INT_NORMAL_VALUES, null);
            createIntegerDataTypeStaticRCD(rClassPrivate, true, INT_NULL_VALUES_SIGNED, "NV");
        } else
            rClassPrivate = null;

        testRoundTrip(rClassPublic, true, methodImpl, INT_NORMAL_VALUES, INT_NULL_VALUES_SIGNED);
        if (!useProperty)
            testRoundTrip(rClassPrivate, false, methodImpl, INT_NORMAL_VALUES, INT_NULL_VALUES_SIGNED);
        testNullViolation(rClassPublic, methodImpl, INT_NORMAL_VALUES, INT_NULL_VALUES_SIGNED, INT_ENCODINGS);
        if (!useProperty)
            testNullViolation(rClassPrivate, methodImpl, INT_NORMAL_VALUES, INT_NULL_VALUES_SIGNED, INT_ENCODINGS);
    }

    /*
     * All IntegerDataType encoding with unsigned binding (.NET only)
     */

    private final static Class<?>[] INT_FIELD_TYPES_UNSIGNED = {
            byte.class,
    };

    private final static Object[] INT_NORMAL_VALUES_UNSIGNED = {
            (byte) 0x55,
            (short) 30001,
            2000000001,
            2000000000001L,
            0x7FFFFFFFFF0AL,
            0x3FFFFFFE,
            0x1FFFFFFFFFFFFFFEL,
            CTXT.staticVarRef(Integer.class, "MAX_VALUE")
    };

    /*
     * natural IntegerDataType encoding limits with non-standard binding
     */

    public void testIntegerDataTypeRange() {
        final JMethod methodImpl = jclass.addMethod(PRIVATE, void.class, "testIntegerDataTypeRange");
        testIntegerDataTypeRange(long.class, methodImpl);
        testIntegerDataTypeRange(int.class, methodImpl);
        testIntegerDataTypeRange(short.class, methodImpl);
        addCompIntpTests("testIntegerDataTypeRange", methodImpl);
    }

    private void testIntegerDataTypeRange(Class<?> clazz, JMethod methodImpl) {
        // TODO: remove this!!!
        // populate field types
        final Class<?>[] types = new Class<?>[INT_FIELD_TYPES.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = MdUtil.getSize(clazz) >= MdUtil.getSize(INT_FIELD_TYPES[i]) ?
                    clazz :
                    INT_FIELD_TYPES[i];
        }

        String className = getName(clazz, true);
        JClassDefinition classDefinitionPublic = recordClassMap.get(jclass.fullName() + "." + className);
        final JClass rClassPublic;
        if (classDefinitionPublic != null)
            rClassPublic = classDefinitionPublic.jclass;
        else {

            rClassPublic = addRecordClass(className,
                    true,
                    INT_NULL_VALUES_4SIGNED,
                    types
            );
            createIntegerDataTypeRCD(rClassPublic, true);
            createIntegerDataTypeRCD(rClassPublic, false);
            createIntegerDataTypeStaticRCD(rClassPublic, true, INT_NORMAL_VALUES, null);
            createIntegerDataTypeStaticRCD(rClassPublic, false, INT_NORMAL_VALUES, null);
            createIntegerDataTypeStaticRCD(rClassPublic, true, INT_NULL_VALUES, "NV");
        }

        className = getName(clazz, false);
        JClassDefinition classDefinitionPrivate = recordClassMap.get(jclass.fullName() + "." + className);
        final Object[] lower_values, upper_values;
        if (clazz == long.class) {
            lower_values = INT_RANGE_LOWER_VALUES_4LONG;
            upper_values = INT_RANGE_UPPER_VALUES_4LONG;
        } else if (clazz == int.class) {
            lower_values = INT_RANGE_LOWER_VALUES_4INT;
            upper_values = INT_RANGE_UPPER_VALUES_4INT;
        } else if (clazz == short.class) {
            lower_values = INT_RANGE_LOWER_VALUES_4SHORT;
            upper_values = INT_RANGE_UPPER_VALUES_4SHORT;
        } else
            throw new IllegalStateException(String.valueOf(clazz));

        testIntegerDataTypeRangeImpl(classDefinitionPublic, methodImpl, lower_values, upper_values);
        if (!useProperty)
            testIntegerDataTypeRangeImpl(classDefinitionPrivate, methodImpl, lower_values, upper_values);
    }

    // violates the lower limit of encoding range
    private final static Object[] INT_RANGE_LOWER_VALUES_4LONG = {
            -129,
            -32769,
            -2147483649L,
            SKIP_VALUE,
            -140737488355329L,  // INT48
            -1,                 // PUINT30,
            -1,                 // PUINT61,
            -1                  // PINTERVAL
    };

    // violates the lower limit of encoding range
    private final static Object[] INT_RANGE_UPPER_VALUES_4LONG = {
            128,
            32768,
            2147483648L,
            SKIP_VALUE,
            140737488355328L, // INT48
            1073741823,
            2305843009213693951L,
            2147483648L
    };

    private final static Object[] INT_RANGE_LOWER_VALUES_4INT = {
            -129,
            -32769,
            SKIP_VALUE,
            SKIP_VALUE,
            SKIP_VALUE,         // INT48
            -1,                 // PUINT30,
            -1,                 // PUINT61,
            -1                  // PINTERVAL
    };

    private final static Object[] INT_RANGE_UPPER_VALUES_4INT = {
            128,
            SKIP_VALUE,
            SKIP_VALUE,
            SKIP_VALUE,
            SKIP_VALUE,         // INT48
            SKIP_VALUE,
            SKIP_VALUE,
            SKIP_VALUE
    };

    private final static Object[] INT_RANGE_LOWER_VALUES_4SHORT = {
            -129,
            SKIP_VALUE,
            SKIP_VALUE,
            SKIP_VALUE,
            SKIP_VALUE,         // INT48
            -1,                 // PUINT30,
            -1,                 // PUINT61,
            -1                  // PINTERVAL
    };

    private final static Object[] INT_RANGE_UPPER_VALUES_4SHORT = {
            128,
            SKIP_VALUE,
            SKIP_VALUE,
            SKIP_VALUE,
            SKIP_VALUE,         // INT48
            1073741823,
            SKIP_VALUE,
            SKIP_VALUE
    };

    private void testIntegerDataTypeRangeImpl(JClassDefinition classDefinition, JMethod method, Object[] lower_values, Object[] upper_values) {
        final JCompoundStatement body = CTXT.compStmt();
        method.body().add(body);

        final JClass rclass = classDefinition.jclass;
        final String[] fieldNames = classDefinition.fieldNames;
        String rcdName = getRCDName(rclass, true, false, null);
        JExpr rcdNullable = rcdMap.get(rcdName);
        assert rcdNullable != null;
        rcdName = getRCDName(rclass, false, false, null);
        JExpr rcdNonNullable = rcdMap.get(rcdName);
        assert rcdNonNullable != null;
/*      TODO: skip static check for a while
        rcdName = getRCDName(rclass, true, true, null);
        JExpr rcdNullableStatic = rcdMap.get(rcdName);
        assert rcdNullableStatic != null;
        rcdName = getRCDName(rclass, false, true, null);
        JExpr rcdNonNullableStatic = rcdMap.get(rcdName);
        assert rcdNonNullableStatic != null;
        rcdName = getRCDName(rclass, true, true, "NV");
        JExpr rcdNullableStaticNV = rcdMap.get(rcdName);
        assert rcdNullableStaticNV != null;
*/

        JExpr msg = body.addVar(0, rclass, "msg", CTXT.newExpr(rclass));

        // lower boundary violation
        assignFieldValues(body, rclass, msg, lower_values);
        final Object[] values = new Object[INT_NORMAL_VALUES.length];
        Arrays.fill(values, SKIP_VALUE);
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                values[i - 1] = INT_NORMAL_VALUES[i - 1];
                if (i > 1)
                    values[i - 2] = SKIP_VALUE;
            }

            final Object value = lower_values[i];
            assignFieldValues(body, rclass, msg, values);
            if (value != SKIP_VALUE) {
                // non-static nullable
                testRangeViolation(body, msg, rcdNullable, "low", INT_ENCODINGS[i], fieldNames[i], value);

                // non-static non-nullable
                testRangeViolation(body, msg, rcdNonNullable, "low", INT_ENCODINGS[i], fieldNames[i], value);
            }
        }

        // upper boundary violation
        assignFieldValues(body, rclass, msg, upper_values);
        Arrays.fill(values, SKIP_VALUE);
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                values[i - 1] = INT_NORMAL_VALUES[i - 1];
                if (i > 1)
                    values[i - 2] = SKIP_VALUE;
            }

            final Object value = upper_values[i];
            assignFieldValues(body, rclass, msg, values);
            if (value != SKIP_VALUE) {
                // non-static nullable
                testRangeViolation(body, msg, rcdNullable, "up", INT_ENCODINGS[i], fieldNames[i], value);

                // non-static non-nullable
                testRangeViolation(body, msg, rcdNonNullable, "up", INT_ENCODINGS[i], fieldNames[i], value);
            }
        }

        // TODO: test range limit for StaticDataField
    }

    private void testRangeViolation(JCompoundStatement body, JExpr msg, JExpr rcd, String postfix, String encoding, String fieldName, Object value) {
        final String title = encoding + " - " + postfix;
        final JTryStatement tryStmt = CTXT.tryStmt();
        tryStmt.tryStmt().add(jclass.callSuperMethod("boundEncode", msg, rcd));
        tryStmt.tryStmt().add(assertFail(title));
        final JCompoundStatement catchStmt = tryStmt.addCatch(IllegalArgumentException.class, "e");
        catchStmt.add(assertEquals(title,
                CTXT.staticCall(String.class, "format",
                        CTXT.stringLiteral("java.lang.IllegalArgumentException: %s == " + value),
                        jclass.thisVar().access().call("getFieldString", msg, CTXT.stringLiteral(fieldName))),
                tryStmt.catchVariable(IllegalArgumentException.class)));
        body.add(tryStmt);
    }

    private void testNullViolation(JClass rclass, JMethod method, Object[] normal_values, Object[] null_values, String[] encodings) {
        if (encodings.length != null_values.length)
            throw new IllegalArgumentException("encordings and null_values length are different " + encodings.length + ", " + null_values.length);

        final JCompoundStatement body = CTXT.compStmt();
        method.body().add(body);

        String rcdName = getRCDName(rclass, false, false, null);
        JExpr rcdNonNullable = rcdMap.get(rcdName);
        assert rcdNonNullable != null;
        JClassDefinition classDefinition = recordClassMap.get(rclass.fullName());
        assert classDefinition != null;
        final String[] fields = classDefinition.fieldNames;
        assert fields != null;
/*      TODO: skip static check for a while
        rcdName = getRCDName(rclass, true, true, null);
        JExpr rcdNullableStatic = rcdMap.get(rcdName);
        assert rcdNullableStatic != null;
        rcdName = getRCDName(rclass, false, true, null);
        JExpr rcdNonNullableStatic = rcdMap.get(rcdName);
        assert rcdNonNullableStatic != null;
        rcdName = getRCDName(rclass, true, true, "NV");
        JExpr rcdNullableStaticNV = rcdMap.get(rcdName);
        assert rcdNullableStaticNV != null;
*/

        JExpr msg = body.addVar(0, rclass, "msg", CTXT.newExpr(rclass));

        assignFieldValues(body, rclass, msg, null_values);
        final Object[] values = new Object[null_values.length];
        Arrays.fill(values, SKIP_VALUE);
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                values[i - 1] = normal_values[i - 1];
                if (i > 1)
                    values[i - 2] = SKIP_VALUE;
            }

            final Object value = null_values[i];
            assignFieldValues(body, rclass, msg, values);
            if (value != SKIP_VALUE && (value == null || !(value instanceof NonNullLiteral))) {
                final String title = (encodings[i] == null ? "" : encodings[i] + " - ") + "null violation - " + (classDefinition.isPublic ? "public" : "private");
                final JTryStatement tryStmt = CTXT.tryStmt();
                tryStmt.tryStmt().add(jclass.callSuperMethod("boundEncode", msg, rcdNonNullable));
                tryStmt.tryStmt().add(assertFail(title));
                final JCompoundStatement catchStmt = tryStmt.addCatch(IllegalArgumentException.class, "e");
                catchStmt.add(assertEquals(title,
                        "java.lang.IllegalArgumentException: '" + fields[i] + "' field is not nullable",
                        tryStmt.catchVariable(IllegalArgumentException.class)));
                body.add(tryStmt);
            }
        }
    }

    private void testConstraintViolation(JClass rclass, JMethod method, Object[] normal_values, Object[] bad_values, String[] encodings) {
        if (encodings.length != bad_values.length)
            throw new IllegalArgumentException("encordings and bad_values length are different " + encodings.length + ", " + bad_values.length);

        final JCompoundStatement body = CTXT.compStmt();
        method.body().add(body);

        String rcdName = getRCDName(rclass, false, false, null);
        JExpr rcdNonNullable = rcdMap.get(rcdName);
        assert rcdNonNullable != null;
        JClassDefinition classDefinition = recordClassMap.get(rclass.fullName());
        assert classDefinition != null;
        final String[] fields = classDefinition.fieldNames;
        assert fields != null;

        JExpr msg = body.addVar(0, rclass, "msg", CTXT.newExpr(rclass));

        assignFieldValues(body, rclass, msg, bad_values);
        final Object[] values = new Object[bad_values.length];
        Arrays.fill(values, SKIP_VALUE);
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                values[i - 1] = normal_values[i - 1];
                if (i > 1)
                    values[i - 2] = SKIP_VALUE;
            }

            final Object value = bad_values[i];
            assignFieldValues(body, rclass, msg, values);
            if (value != null ) {
                final String title = (encodings[i] == null ? "" : encodings[i] + " - ") + "contraint violation - " + (classDefinition.isPublic ? "public" : "private");
                final JTryStatement tryStmt = CTXT.tryStmt();
                tryStmt.tryStmt().add(jclass.callSuperMethod("boundEncode", msg, rcdNonNullable));
                tryStmt.tryStmt().add(assertFail(title));
                final JCompoundStatement catchStmt = tryStmt.addCatch(IllegalArgumentException.class, "e");
                final JExpr catchVar = tryStmt.catchVariable(IllegalArgumentException.class);
                final JExpr exprSimpleMsg = CTXT.stringLiteral(String.format("java.lang.IllegalArgumentException: %s == " +
                        ((value instanceof EnumLiteral) ? ((EnumLiteral) value).value.value : value),
                        classDefinition.fieldNames[i]));
                catchStmt.add(
                        CTXT.ifStmt(exprSimpleMsg.call(EQUALS, catchVar.call(TO_STRING)).not(),
                                assertEquals(title,
                                        CTXT.staticCall(String.class, "format",
                                                CTXT.stringLiteral("java.lang.IllegalArgumentException: %s == " + ((value instanceof EnumLiteral) ? ((EnumLiteral) value).value.value : value)),
                                                jclass.thisVar().access().call("getFieldString", msg, CTXT.stringLiteral(fields[i]))),
                                        catchVar).asStmt()));
                body.add(tryStmt);
            }
        }
    }

    /*
     * Test an exception on early binding
     */

    private final static Class<?>[] ALL_FIELD_TYPES =
            new Class<?>[]{
                    boolean.class,
                    char.class,
                    byte.class,
                    short.class,
                    int.class,
                    long.class,
                    float.class,
                    double.class,
                    String.class,
                    InstrumentType.class,
                    CharSequence.class
                    // ByteArrayList
            };

    private final static Class<?>[] INT_ALLOWED_FIELD_TYPES =
            new Class<?>[]{
                    byte.class,
                    short.class,
                    int.class,
                    long.class
            };

    private final static Class<?>[] FLOAT_ALLOWED_FIELD_TYPES = {
            float.class,
            double.class
    };

    private final static Class<?>[] STRING_ALLOWED_FIELD_TYPES =
            new Class<?>[]{
                    String.class,
                    CharSequence.class,
                    long.class};

    private final static Class<?>[] ENUM_ALLOWED_FIELD_TYPES =
            new Class<?>[]{
                    InstrumentType.class,
                    CharSequence.class,
                    byte.class,
                    short.class,
                    int.class,
                    long.class
            };

    private interface EarlyBindingStatus {
        /* returns:
         *   O - ok
         *   B - bad
         *   U - undersized
         */
        char check(DataType dataType, Class<?> fieldType);
    }

    public void testEarlyBinding() {
        JClass rClassPublic = addRecordClass("AllTypesPublic",
                true,
                ALL_FIELD_TYPES
        );

        JMethod method = addTestMethod("testEarlyBinding");
        final JTryStatement tryStmt = CTXT.tryStmt();
        method.body().add(tryStmt);
        final JCompoundStatement catchStmt = tryStmt.addCatch(NoSuchFieldException.class, "e");
        catchStmt.add(CTXT.newExpr(RuntimeException.class, tryStmt.catchVariable(NoSuchFieldException.class)).throwStmt());
        final JCompoundStatement body = tryStmt.tryStmt();


        final JClassDefinition classDefinition = recordClassMap.get(rClassPublic.fullName());

        // INTEGER
        testEarlyBinding(body, classDefinition, IntegerDataType.class, INT_ENCODINGS, new EarlyBindingStatus() {
            @Override
            public char check(DataType dataType, Class<?> fieldType) {
                if (dataType instanceof IntegerDataType) {
                    if (Util.indexOf(INT_ALLOWED_FIELD_TYPES, fieldType) == -1)
                        return 'B';
                    else {
                        return MdUtil.getSize(fieldType) < ((IntegerDataType) dataType).getNativeTypeSize() ?
                                'U' : 'O';
                    }
                } else
                    throw new IllegalArgumentException();
            }
        });

        // FLOAT
        testEarlyBinding(body, classDefinition, FloatDataType.class, FLOAT_ENCODINGS, new EarlyBindingStatus() {
            @Override
            public char check(DataType dataType, Class<?> fieldType) {
                if (dataType instanceof FloatDataType) {
                    if (Util.indexOf(FLOAT_ALLOWED_FIELD_TYPES, fieldType) == -1)
                        return 'B';
                    else {
                        final boolean isIEEE32 = FloatDataType.ENCODING_FIXED_FLOAT.equals(dataType.getEncoding());
                        return (fieldType == float.class && isIEEE32 || fieldType == double.class && !isIEEE32) ?
                                'O' : 'U';
                    }
                } else
                    throw new IllegalArgumentException();
            }
        });

        // VARCHAR
        testEarlyBinding(body, classDefinition, VarcharDataType.class, STRING_ENCODINGS, new EarlyBindingStatus() {
            @Override
            public char check(DataType dataType, Class<?> fieldType) {
                if (dataType instanceof VarcharDataType) {
                    if (Util.indexOf(STRING_ALLOWED_FIELD_TYPES, fieldType) == -1)
                        return 'B';
                    else {
                        final VarcharDataType sdt = (VarcharDataType) dataType;
                        return (fieldType == long.class && (sdt.getEncodingType() != VarcharDataType.ALPHANUMERIC || sdt.getLength() > 10)) ?
                                'U' : 'O';
                    }
                } else
                    throw new IllegalArgumentException();
            }
        });

        // ENUM
        createECD(InstrumentType.class, false);
        testEarlyBinding(body, classDefinition, EnumDataType.class, ENUM_ENCODINGS, new EarlyBindingStatus() {
            @Override
            public char check(DataType dataType, Class<?> fieldType) {
                if (dataType instanceof EnumDataType) {
                    if (Util.indexOf(ENUM_ALLOWED_FIELD_TYPES, fieldType) == -1)
                        return 'B';
                    else {
                        return (MdUtil.isIntegerType(fieldType) &&
                                MdUtil.getSize(fieldType) < ((EnumDataType) dataType).descriptor.computeStorageSize()) ?
                                'U' : 'O';
                    }
                } else
                    throw new IllegalArgumentException();
            }
        });
    }

    private void testEarlyBinding(JCompoundStatement body, JClassDefinition classDefinition, Class<? extends DataType> dataTypeClass, String[] encodings, EarlyBindingStatus delegate) {
        final String[] fieldNames = classDefinition.fieldNames;
        for (int i = 0; i < fieldNames.length; i++) {
            for (String encording : encodings) {
                DataType dataType = newDataType(dataTypeClass, encording, false);
                testEarlyBindingImpl(body, classDefinition.jclass, fieldNames[i], dataType, ALL_FIELD_TYPES[i], delegate);
            }
        }
    }

    private void testEarlyBindingImpl(JCompoundStatement body, JClass jclass, String fieldName, DataType dataType, Class<?> fieldType, EarlyBindingStatus delegate) {
        final char status = delegate.check(dataType, fieldType);
        if (status == 'O') {
            checkFieldBinding(body, jclass, fieldName, dataType, true);
        } else {
            final JTryStatement tryStmt = CTXT.tryStmt();
            body.add(tryStmt);
            checkFieldBinding(tryStmt.tryStmt(), jclass, fieldName, dataType, false);

            final JCompoundStatement catchStmt = tryStmt.addCatch(IllegalArgumentException.class, "e");
            catchStmt.add(assertEquals(null,
                    String.format("java.lang.IllegalArgumentException: %s cannot be bound to %s field",
                            status == 'B' ? trimAssemplyPrefix(dataType.getBaseName()) :
                                    dataType instanceof EnumDataType ?
                                            trimAssemplyPrefix(((EnumDataType) dataType).descriptor.getName()) :
                                            dataType.getEncoding(),
                            fieldType.getName()),
                    tryStmt.catchVariable(IllegalArgumentException.class)));
        }
    }

    /*
     * Test an exception on early binding with a static field
     */

    private final static Object[] INT_STATIC_VALUES_OK =
            new Object[]{
                    (byte) 0x55,           // byte3
                    (short) -30001,        // short4
                    2000000001,            // int5
                    2000000000001L,        // long6
            };


    private final static Object[] INT_STATIC_VALUES_BAD_HI =
            new Object[]{
                    Byte.MAX_VALUE + 1,             // byte3
                    Short.MAX_VALUE + 1,            // short4
                    (long) Integer.MAX_VALUE + 1,   // int5
                    null
            };

    private final static Object[] INT_STATIC_VALUES_BAD_LO =
            new Object[]{
                    Byte.MIN_VALUE - 1,             // byte3
                    Short.MIN_VALUE - 1,            // short4
                    (long) Integer.MIN_VALUE - 1,   // int5
                    null
            };

    private final static Object[] FLOAT_STATIC_VALUES_OK = {
            1.12f,
            1324.345,
    };

    private final static Object[] FLOAT_STATIC_VALUES_BAD_HI = {
            3.4028236E38,
            null
    };

    private final static Object[] FLOAT_STATIC_VALUES_BAD_LO = {
            -3.4028236E38,
            null
    };

    public void testEarlyBindingStatic() {
        final String fullClassName = jclass.fullName() + "." + "AllTypesPublic";
        JClassDefinition classDefinition = recordClassMap.get(fullClassName);
        if (classDefinition == null) {
            final JClass rclass = addRecordClass("AllTypesPublic",
                    true,
                    ALL_FIELD_TYPES
            );
            classDefinition = recordClassMap.get(rclass.fullName());
        }

        JMethod method = addTestMethod("testEarlyBindingStatic");
        final JTryStatement tryStmt = CTXT.tryStmt();
        method.body().add(tryStmt);
        final JCompoundStatement catchStmt = tryStmt.addCatch(NoSuchFieldException.class, "e");
        catchStmt.add(CTXT.newExpr(RuntimeException.class, tryStmt.catchVariable(NoSuchFieldException.class)).throwStmt());
        final JCompoundStatement body = tryStmt.tryStmt();

        // INTEGER
        DataType dataType = new IntegerDataType(IntegerDataType.ENCODING_INT64, true);
        for (int i = 0; i < INT_ALLOWED_FIELD_TYPES.length; i++) {
            final Class<?> fieldType = INT_ALLOWED_FIELD_TYPES[i];
            String fieldName = classDefinition.getFieldName(fieldType);
            //System.out.println(fieldName);

            testEarlyBindingImpl(body, classDefinition.jclass, fieldName, dataType, fieldType, INT_STATIC_VALUES_OK[i], true);
            testEarlyBindingImpl(body, classDefinition.jclass, fieldName, dataType, fieldType, INT_STATIC_VALUES_BAD_HI[i], false);
            testEarlyBindingImpl(body, classDefinition.jclass, fieldName, dataType, fieldType, INT_STATIC_VALUES_BAD_LO[i], false);
        }

        // FLOAT
        dataType = new FloatDataType(FloatDataType.ENCODING_FIXED_DOUBLE, true);
        for (int i = 0; i < FLOAT_ALLOWED_FIELD_TYPES.length; i++) {
            final Class<?> fieldType = FLOAT_ALLOWED_FIELD_TYPES[i];
            String fieldName = classDefinition.getFieldName(fieldType);
            //System.out.println(fieldName);

            testEarlyBindingImpl(body, classDefinition.jclass, fieldName, dataType, fieldType, FLOAT_STATIC_VALUES_OK[i], true);
            testEarlyBindingImpl(body, classDefinition.jclass, fieldName, dataType, fieldType, FLOAT_STATIC_VALUES_BAD_HI[i], false);
            testEarlyBindingImpl(body, classDefinition.jclass, fieldName, dataType, fieldType, FLOAT_STATIC_VALUES_BAD_LO[i], false);
        }
    }

    private void testEarlyBindingImpl(JCompoundStatement body, JClass jclass, String fieldName, DataType dataType, Class<?> fieldType, Object value, boolean isOk) {
        // no code is necessary
        if(value == null)
            return;


        if (isOk) {
            checkFieldBindingStatic(body, jclass, fieldName, dataType, value, true);
        } else {
            final JTryStatement tryStmt = CTXT.tryStmt();
            body.add(tryStmt);
            checkFieldBindingStatic(tryStmt.tryStmt(), jclass, fieldName, dataType, value, false);

            final JCompoundStatement catchStmt = tryStmt.addCatch(IllegalArgumentException.class, "e");
            catchStmt.add(assertEquals(null,
                    String.format("java.lang.IllegalArgumentException: Cannot store static value %s to %s",
                            String.valueOf(value),
                            fieldType.getName()),
                    tryStmt.catchVariable(IllegalArgumentException.class)));
        }
    }

    private static String trimAssemplyPrefix(String className) {
         return className;
    }

    private void checkFieldBinding(JCompoundStatement body, JClass jclass, String fieldName, DataType dataType, boolean isOk) {
        final JCompoundStatement subBody = CTXT.compStmt();
        body.add(subBody);

        // TODO: use typeArgument in FieldLayout constructor
        final JLocalVariable layout = subBody.addVar(FINAL, NonStaticFieldLayout.class,
                "layout",
                CTXT.newExpr(NonStaticFieldLayout.class,
                        CTXT.nullLiteral(),
                        CTXT.newExpr(NonStaticDataField.class,
                                CTXT.stringLiteral(fieldName),
                                CTXT.nullLiteral(),
                                getDataTypeExpr(dataType))

                ));

        subBody.add(layout.call("bind", CTXT.classLiteral(jclass)));

        if (isOk)
            subBody.add(CTXT.assertStmt(
                    CTXT.binExpr(layout.call("getJavaField"), "!=", CTXT.nullLiteral()),
                    null
            ));
        else
            subBody.add(assertFail(fieldName + " to " + dataType.getBaseName()));
    }

    private void checkFieldBindingStatic(JCompoundStatement body, JClass jclass, String fieldName, DataType dataType, Object value, boolean isOk) {
        final JCompoundStatement subBody = CTXT.compStmt();
        body.add(subBody);

        // TODO: use typeArgument in FieldLayout constructor
        final JLocalVariable layout = subBody.addVar(FINAL, StaticFieldLayout.class,
                "layout",
                CTXT.newExpr(StaticFieldLayout.class,
                        CTXT.nullLiteral(),
                        CTXT.newExpr(StaticDataField.class,
                                CTXT.stringLiteral(fieldName),
                                CTXT.nullLiteral(),
                                getDataTypeExpr(dataType),
                                getValueExpr4Static(value)
                        )
                ));

        subBody.add(layout.call("bind", CTXT.classLiteral(jclass)));

        if (isOk)
            subBody.add(CTXT.assertStmt(
                    CTXT.binExpr(layout.call("getJavaField"), "!=", CTXT.nullLiteral()),
                    null
            ));
        else
            subBody.add(assertFail(fieldName + " to " + dataType.getBaseName()));
    }

    /*
     * BooleanDataType binding for boolean and byte
     * TODO: sbyte, bool?
     */


    private final static Class<?>[] BOOLEAN_FIELD_TYPES = new Class<?>[]{boolean.class, byte.class};

    private final static String[] BOOL_ENCODINGS = new String[]{NOT_NULL, null};

    private final static Object[] BOOL_TRUE_VALUES =
            new Object[]{
                    true,
                    new ToStringLiteral((byte) 1, "true"),
            };

    private final static Object[] BOOL_FALSE_VALUES =
            new Object[]{
                    false,
                    new ToStringLiteral((byte) 0, "false"),
            };

    private final static Object[] BOOL_NULL_VALUES =
            new Object[]{
                    new NonNullLiteral(false),
                    new NullLiteral((byte) -1),
            };

    public void testBooleanDataType() {
        JClass rClassPublic = addRecordClass("AllBooleanPublic",
                true,
                BOOLEAN_FIELD_TYPES
        );

        createRCD(rClassPublic, true, BooleanDataType.class, BOOL_ENCODINGS);
        createRCD(rClassPublic, false, BooleanDataType.class, BOOL_ENCODINGS);
        createStaticRCD(rClassPublic, true, BooleanDataType.class, BOOL_ENCODINGS, BOOL_TRUE_VALUES, null);
        createStaticRCD(rClassPublic, true, BooleanDataType.class, BOOL_ENCODINGS, BOOL_FALSE_VALUES, "F");
        createStaticRCD(rClassPublic, false, BooleanDataType.class, BOOL_ENCODINGS, BOOL_TRUE_VALUES, null);
        createStaticRCD(rClassPublic, true, BooleanDataType.class, BOOL_ENCODINGS, BOOL_NULL_VALUES, "NV");

        JClass rClassPrivate = addRecordClass("AllBooleanPrivate",
                false,
                BOOLEAN_FIELD_TYPES
        );
        createRCD(rClassPrivate, true, BooleanDataType.class, BOOL_ENCODINGS);
        createRCD(rClassPrivate, false, BooleanDataType.class, BOOL_ENCODINGS);
        createStaticRCD(rClassPrivate, true, BooleanDataType.class, BOOL_ENCODINGS, BOOL_TRUE_VALUES, null);
        createStaticRCD(rClassPrivate, true, BooleanDataType.class, BOOL_ENCODINGS, BOOL_FALSE_VALUES, "F");
        createStaticRCD(rClassPrivate, false, BooleanDataType.class, BOOL_ENCODINGS, BOOL_TRUE_VALUES, null);
        createStaticRCD(rClassPrivate, true, BooleanDataType.class, BOOL_ENCODINGS, BOOL_NULL_VALUES, "NV");

        final JMethod methodImpl = jclass.addMethod(PRIVATE, void.class, "testBooleanDataType");
        testBooleanDataTypeImpl(rClassPublic, true, methodImpl);
        if (!useProperty)
            testBooleanDataTypeImpl(rClassPrivate, false, methodImpl);
        testNullViolation(rClassPublic, methodImpl, BOOL_TRUE_VALUES, BOOL_NULL_VALUES, BOOL_ENCODINGS);
        if (!useProperty)
            testNullViolation(rClassPrivate, methodImpl, BOOL_TRUE_VALUES, BOOL_NULL_VALUES, BOOL_ENCODINGS);

        addCompIntpTests("testBooleanDataType", methodImpl);
    }

    private JMethod testBooleanDataTypeImpl(JClass rclass, boolean isPublic, JMethod method) {
        final JCompoundStatement body = CTXT.compStmt();
        method.body().add(body);

        String rcdName = getRCDName(rclass, true, false, null);
        JExpr rcdNullable = rcdMap.get(rcdName);
        assert rcdNullable != null;
        rcdName = getRCDName(rclass, false, false, null);
        JExpr rcdNonNullable = rcdMap.get(rcdName);
        assert rcdNonNullable != null;
        rcdName = getRCDName(rclass, true, true, null);
        JExpr rcdNullableStatic = rcdMap.get(rcdName);
        assert rcdNullableStatic != null;
        rcdName = getRCDName(rclass, true, true, "F");
        JExpr rcdNullableStaticF = rcdMap.get(rcdName);
        assert rcdNullableStaticF != null;
        rcdName = getRCDName(rclass, false, true, null);
        JExpr rcdNonNullableStatic = rcdMap.get(rcdName);
        assert rcdNonNullableStatic != null;
        rcdName = getRCDName(rclass, true, true, "NV");
        JExpr rcdNullableStaticNV = rcdMap.get(rcdName);
        assert rcdNullableStaticNV != null;

        JExpr msg = body.addVar(0, rclass, "msg", CTXT.newExpr(rclass));

        assignFieldValues(body, rclass, msg, BOOL_TRUE_VALUES);
        // non-static nullable
        JExpr textExpr = getStringLiteral((isPublic ? "public" : "private") + " nullable - true values");
        body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullable));
        // non-static non-nullable
        textExpr = getStringLiteral((isPublic ? "public" : "private") + " non-nullable - true values");
        body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNonNullable));
        if (!useProperty) {
            // static nullable
            textExpr = getStringLiteral((isPublic ? "public" : "private") + " nullable static - true values");
            body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullableStatic));
            // static non-nullable
            textExpr = getStringLiteral((isPublic ? "public" : "private") + " non-nullable static - true values");
            body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNonNullableStatic));
        }

        assignFieldValues(body, rclass, msg, BOOL_FALSE_VALUES);
        // non-static nullable
        textExpr = getStringLiteral((isPublic ? "public" : "private") + " nullable - false values");
        body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullable));
        // non-static non-nullable
        textExpr = getStringLiteral((isPublic ? "public" : "private") + " non-nullable - false values");
        body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNonNullable));
        if (!useProperty) {
            // static nullable
            textExpr = getStringLiteral((isPublic ? "public" : "private") + " nullable static - false values");
            body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullableStaticF));
        }

        assignFieldValues(body, rclass, msg, BOOL_NULL_VALUES);
        // non-static nullable
        textExpr = getStringLiteral((isPublic ? "public" : "private") + " nullable - null values");
        body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullable));
        if (!useProperty) {
            // static nullable
            textExpr = getStringLiteral((isPublic ? "public" : "private") + " nullable static - null values");
            body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullableStaticNV));
        }

        return method;
    }

    /*
     * All FloatDataType encoding with standard binding
     */

    private final static Class<?>[] FLOAT_FIELD_TYPES = {
            float.class,    // IEEE32
            double.class,   // IEEE64
            double.class,   // DECIMAL
            double.class, // ???float.class,    // DECIMAL(2)
            double.class,   // DECIMAL(4)
            //double.class    // IEEE32 (oversized binding)
    };

    private final static Class<?>[] FLOAT_FIELD_TYPES_NULLABLE = null;

    // encoding
    private final static String[] FLOAT_ENCODINGS = {
            FloatDataType.ENCODING_FIXED_FLOAT,
            FloatDataType.ENCODING_FIXED_DOUBLE,
            FloatDataType.ENCODING_SCALE_AUTO,
            FloatDataType.getEncodingScaled(2),
            FloatDataType.getEncodingScaled(4),
            //FloatDataType.ENCODING_FIXED_FLOAT,
    };

    private final static Object[] FLOAT_NORMAL_VALUES = {
            23456.34f, // 123456.34f, // TODO: preciseness issue under .NET
            76456577.7632,
            76456577.7633,
            76456577.37,
            76456577.769,
            //123456.346
    };

    private final static Object[] FLOAT_NULL_VALUES = {
            Float.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            //Float.NaN,
    };

    private final static Object[] FLOAT_NULL_VALUES_4NET =  null;

    private final static DataTypeEntry FLOAT_ENTRY = new DataTypeEntry(
            "Float",
            FloatDataType.class,
            FLOAT_FIELD_TYPES,
            FLOAT_FIELD_TYPES_NULLABLE,
            FLOAT_ENCODINGS,
            FLOAT_NORMAL_VALUES,
            FLOAT_NULL_VALUES,
            FLOAT_NULL_VALUES_4NET
    );

    /*
     * All VarcharDataType encoding
     */

    private final static Class<?>[] STRING_FIELD_TYPES =
            new Class<?>[]{
                    // UTF8
                    String.class,
                    CharSequence.class,
                    // ALPHANUMERIC(100)
                    String.class,
                    CharSequence.class,
                    // ALPHANUMERIC(5)
                    long.class,
                    // ALPHANUMERIC(10)
                    long.class,
            };

    // encoding
    private final static String[] STRING_ENCODINGS = {
            VarcharDataType.ENCODING_INLINE_VARSIZE,
            VarcharDataType.ENCODING_INLINE_VARSIZE,
            VarcharDataType.getEncodingAlphanumeric(100),
            VarcharDataType.getEncodingAlphanumeric(100),
            VarcharDataType.getEncodingAlphanumeric(5),
            VarcharDataType.getEncodingAlphanumeric(10)
    };

    private final static class AlphanumericLiteral {
        private final int size;
        private final CharSequence value;

        private AlphanumericLiteral(int size, CharSequence value) {
            this.size = size;
            this.value = value;
        }
    }

    private final static Object[] STRING_NORMAL_VALUES = {
            "Hi Kolia !!!",
            "How are you Gene ???",
            "VERY VERY, LONG STRING !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_ ABCDEFGHIJKLM",
            "ANOTHER    LONG STRING !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_ ABCDEFGHIJKLM",
            new AlphanumericLiteral(5, "ABCDE"),
            new AlphanumericLiteral(10, "ABCDE12345")
    };

    private final static Object[] STRING_NULL_VALUES = {
            null,
            null,
            null,
            null,
            new AlphanumericLiteral(5, null),
            new AlphanumericLiteral(10, null)
    };

    private final static DataTypeEntry STRING_ENTRY = new DataTypeEntry(
            "Varchar",
            VarcharDataType.class,
            STRING_FIELD_TYPES,
            null,
            STRING_ENCODINGS,
            STRING_NORMAL_VALUES,
            STRING_NULL_VALUES,
            null
    );

    /*
     * All EnumDataType encoding
     */




    private final static Class<?>[] ENUM_FIELD_TYPES =
            new Class<?>[]{
                    InstrumentType.class,
                    CharSequence.class,
                    byte.class,
                    short.class,
                    int.class,
                    long.class,
            };

    private final static Class<?>[] ENUM_FIELD_TYPES_NULLABLE =  null;

    private final static EnumClassDescriptor ECD_TRAFFIC_LIGHTS = new EnumClassDescriptor(InstrumentType.class);
    private final static String ECD_TRAFFIC_LIGHTS_NAME = ECD_TRAFFIC_LIGHTS.getName();
    private final static EnumClassDescriptor ECD_VAR_ENUM = null;
    private final static String ECD_VAR_ENUM_NAME = ECD_VAR_ENUM != null ? ECD_VAR_ENUM.getName() : null;

    static {
        ecdMap.put(ECD_TRAFFIC_LIGHTS_NAME, ECD_TRAFFIC_LIGHTS);
        ecdMap.put(ECD_VAR_ENUM_NAME, ECD_VAR_ENUM);
    }

    // encoding
    private final static String[] ENUM_ENCODINGS =
            new String[]{
                    ECD_TRAFFIC_LIGHTS_NAME,
                    ECD_TRAFFIC_LIGHTS_NAME,
                    ECD_TRAFFIC_LIGHTS_NAME,
                    ECD_TRAFFIC_LIGHTS_NAME,
                    ECD_TRAFFIC_LIGHTS_NAME,
                    ECD_TRAFFIC_LIGHTS_NAME,
            };

    private final static class EnumLiteral {
        private final Class<?> clazz;
        private final EnumValue value;

        private EnumLiteral(Class<?> clazz, Enum value) {
            this.clazz = clazz;
            this.value = value != null ? new EnumValue(value.name(), value.ordinal()) : null;
        }

        private EnumLiteral(Class<?> clazz, String symbol, int value) {
            this.clazz = clazz;
            this.value = new EnumValue(symbol, value);
        }
    }

    private final static Object[] ENUM_NORMAL_VALUES =
            new Object[]{
                    InstrumentType.OPTION,
                    InstrumentType.FUTURE.toString(),
                    new EnumLiteral(byte.class, InstrumentType.BOND),
                    new EnumLiteral(short.class, InstrumentType.FUTURE),
                    new EnumLiteral(int.class, InstrumentType.OPTION),
                    new EnumLiteral(long.class, InstrumentType.FX),
            };

    private final static Object[] ENUM_NULL_VALUES =
            new Object[]{
                    null,
                    null,
                    new EnumLiteral(byte.class, null),
                    new EnumLiteral(short.class, null),
                    new EnumLiteral(int.class, null),
                    new EnumLiteral(long.class, null)
            };

    private final static Object[] ENUM_NULL_VALUES_4NET = null;

    private final static Object[] ENUM_BAD_VALUES =
            new Object[]{
                    null,
                    "BAD_VALUE",
                    (byte) 20,
                    (short) 21,
                    (int) 22,
                    (long) 0x2005,
            };

    private final static DataTypeEntry ENUM_ENTRY = new DataTypeEntry(
            "Enum",
            EnumDataType.class,
            ENUM_FIELD_TYPES,
            ENUM_FIELD_TYPES_NULLABLE,
            ENUM_ENCODINGS,
            ENUM_NORMAL_VALUES,
            ENUM_NULL_VALUES,
            ENUM_NULL_VALUES_4NET,
            ENUM_BAD_VALUES
    );

    public void testEnumDataType(boolean isNetNullable) {
        createECD(InstrumentType.class, false);

        testDataType(ENUM_ENTRY, isNetNullable);
    }

    /*
     * All EnumDataType (bitmask) encoding
     */

    private final static Class<?>[] BITMASK_FIELD_TYPES =
            new Class<?>[]{
                    int.class,
                    long.class
            };

    private final static Class<?>[] BITMASK_FIELD_TYPES_NULLABLE = null;

    private final static EnumClassDescriptor ECD_FIELDS_BITMASK = new EnumClassDescriptor("JavaFieldsBitmask", null, true,
            EnumValue.autoNumber(true, "field1", "field2", "field3", "field4", "field5"));
    private final static String ECD_FIELDS_BITMASK_NAME = ECD_FIELDS_BITMASK.getName();
    private final static EnumClassDescriptor ECD_ENUM_ASSEMBLIES_FLAGS = null;
    private final static String ECD_ENUM_ASSEMBLIES_FLAGS_NAME = ECD_ENUM_ASSEMBLIES_FLAGS != null ? ECD_ENUM_ASSEMBLIES_FLAGS.getName() : null;

    static {
        ecdMap.put(ECD_FIELDS_BITMASK_NAME, ECD_FIELDS_BITMASK);
        ecdMap.put(ECD_ENUM_ASSEMBLIES_FLAGS_NAME, ECD_ENUM_ASSEMBLIES_FLAGS);
    }

    private final static String[] BITMASK_ENCODINGS =
            new String[]{
                    ECD_FIELDS_BITMASK_NAME,
                    ECD_FIELDS_BITMASK_NAME
            };

    private final static Object[] BITMASK_NORMAL_VALUES =
            new Object[]{
                    new EnumLiteral(int.class, "field1", 1),
                    new EnumLiteral(long.class, "field2", 2),
            };

    private final static Object[] BITMASK_NULL_VALUES =
            new Object[]{
                    new NonNullLiteral(new EnumLiteral(int.class, "field1", 1)),
                    new NonNullLiteral(new EnumLiteral(long.class, "field2", 2)),
            };

    private final static Object[] BITMASK_NULL_VALUES_4NET = null;

    private final static Object[] BITMASK_BAD_VALUES =
            new Object[]{
                    (int) 0x21,
                    (long) 0x2005,
            };

    private final static DataTypeEntry BITMASK_ENTRY = new DataTypeEntry(
            "Bitmask",
            EnumDataType.class,
            BITMASK_FIELD_TYPES,
            BITMASK_FIELD_TYPES_NULLABLE,
            BITMASK_ENCODINGS,
            BITMASK_NORMAL_VALUES,
            BITMASK_NULL_VALUES,
            BITMASK_NULL_VALUES_4NET,
            BITMASK_BAD_VALUES
    );

    public void testBitmaskDataType(boolean isNetNullable) {
        createECD(ECD_FIELDS_BITMASK);

        testDataType(BITMASK_ENTRY, isNetNullable);
    }

    /*
     * ARRAY type: test non-standard bindings
     *  ByteArrayList BOOLEAN, INT8 + (ENUM; BITMASK)
     *  ShortArrayList INT8, INT16 + (ENUM; BITMASK)
     *  IntegerArrayList INTEGER(INT8, INT16, INT32, PUINT30, PINTERVAL); TIMEOFDAY; ENUM; BITMASK
     *  LongArrayList 10 encodings -TIMEOFDAY from above + INTEGER(INT48, INT64, PUINT61); VARCHAR ALPHANUMERIC (10); TIMESTAMP
     *  DoubleArrayList IEEE64, DECIMAL, DECIMAL(2)
     */

    private final static Class<?>[] ARRAY_FIELD_TYPES = {
            ByteArrayList.class,
            ShortArrayList.class,
            IntegerArrayList.class,
            LongArrayList.class,
            LongArrayList.class,
            DoubleArrayList.class,
    };

    private final static String[] ARRAY_POSTFIXES = {
            "INT8_INT48_IEEE64",
            "INT16_INT64_DECIMAL",
            "INT32_PUINT61_DECIMAL2",
            "PUINT30_VARCHAR",
            "PINTERVAL_TIMESTAMP",
            "TIMEOFDAY",
            "ENUM",
            "BITMASK"
    };

    private final static EnumDataType ENUM_DATA_TYPE = new EnumDataType(true,
            new EnumClassDescriptor(InstrumentType.class));
    private final static EnumDataType BITMASK_DATA_TYPE = new EnumDataType(true,
            ECD_FIELDS_BITMASK);


    private final static DataType[][] ARRAY_DATA_TYPES = {
            {
                    // TODO: byte/bool !!!
                    new IntegerDataType(IntegerDataType.ENCODING_INT8, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT8, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT8, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT8, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT48, true),
                    new FloatDataType(FloatDataType.ENCODING_FIXED_DOUBLE, true),
            },
            {
                    new IntegerDataType(IntegerDataType.ENCODING_INT8, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT16, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT16, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT16, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT64, true),
                    new FloatDataType(FloatDataType.ENCODING_SCALE_AUTO, true),
            },
            {
                    new IntegerDataType(IntegerDataType.ENCODING_INT8, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT16, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT32, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT32, true),
                    new IntegerDataType(IntegerDataType.ENCODING_PUINT61, true),
                    new FloatDataType(FloatDataType.getEncodingScaled(2), true),
            },
            {
                    new IntegerDataType(IntegerDataType.ENCODING_INT8, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT16, true),
                    new IntegerDataType(IntegerDataType.ENCODING_PUINT30, true),
                    new IntegerDataType(IntegerDataType.ENCODING_PUINT30, true),
                    new VarcharDataType(VarcharDataType.getEncodingAlphanumeric(10), true, false),
                    new FloatDataType(FloatDataType.ENCODING_FIXED_DOUBLE, true),
            },
            {       // PINTERVAL_TIMESTAMP
                    new IntegerDataType(IntegerDataType.ENCODING_INT8, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT16, true),
                    new IntegerDataType(IntegerDataType.ENCODING_PINTERVAL, true),
                    new IntegerDataType(IntegerDataType.ENCODING_PINTERVAL, true),
                    new DateTimeDataType(true),
                    new FloatDataType(FloatDataType.getEncodingScaled(2), true),
            },
            {       // TIMEOFDAY
                    new IntegerDataType(IntegerDataType.ENCODING_INT8, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT16, true),
                    new TimeOfDayDataType(true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT64, true),
                    new IntegerDataType(IntegerDataType.ENCODING_INT64, true),
                    new FloatDataType(FloatDataType.getEncodingScaled(2), true),
            },
            {       // ENUM
                    ENUM_DATA_TYPE,
                    ENUM_DATA_TYPE,
                    ENUM_DATA_TYPE,
                    ENUM_DATA_TYPE,
                    new IntegerDataType(IntegerDataType.ENCODING_INT64, true),
                    new FloatDataType(FloatDataType.getEncodingScaled(2), true),
            },
            {       // BITMASK
                    BITMASK_DATA_TYPE,
                    BITMASK_DATA_TYPE,
                    BITMASK_DATA_TYPE,
                    BITMASK_DATA_TYPE,
                    new IntegerDataType(IntegerDataType.ENCODING_INT64, true),
                    new FloatDataType(FloatDataType.getEncodingScaled(2), true),
            }
    };

    private final static Object[][] ARRAY_DATA_VALUES = {
            {
                    (byte) 0x50,
                    (byte) 0x51,
                    (byte) 0x52,
                    (byte) 0x53,
                    0x7FFFFFFFFFFAl, // INT48
                    127.459
            },
            {
                    SKIP_VALUE,
                    (short) 0x5F51,
                    (short) 0x5F52,
                    (short) 0x5F53,
                    0x7FFFFFFFFFFAFE3Fl, // INT64
                    127.459
            },
            {
                    SKIP_VALUE,
                    SKIP_VALUE,
                    0x5F521919,
                    0x5F52191A,
                    0x1FFFFFFFFFFFFFFAL, // PUINT61
                    127.45
            },
            {
                    SKIP_VALUE,
                    SKIP_VALUE,
                    0x3FFFFFFA, // PUINT30
                    0x3FFFFFFB, // PUINT30
                    new AlphanumericLiteral(10, "ABCDE12345"),  // VARCHAR ALPHANUMERIC (10)
                    SKIP_VALUE
            },
            {
                    SKIP_VALUE,
                    SKIP_VALUE,
                    0x7ffffffA, // PINTERVAL
                    0x7ffffffB, // PINTERVAL
                                // TIMESTAMP
                    new DateTimeLiteral(1314899629573L), // 2011-09-01 17:53:49.573 GMT
                    SKIP_VALUE
            },
            {       // TIMEOFDAY
                    SKIP_VALUE,
                    SKIP_VALUE,
                    new TimeOfDayLiteral(TimeFormatter.parseTimeOfDayMillis("22:39:57.956")),
                    SKIP_VALUE,
                    SKIP_VALUE,
                    SKIP_VALUE
            },
            {       // ENUM
                    new EnumLiteral(byte.class, InstrumentType.OPTION),
                    new EnumLiteral(byte.class, InstrumentType.FUTURE),
                    new EnumLiteral(byte.class, InstrumentType.BOND),
                    new EnumLiteral(byte.class, InstrumentType.FX),
                    SKIP_VALUE,
                    SKIP_VALUE
            },
            {       // BITMASK
                    new EnumLiteral(int.class, "field1", 1),
                    new EnumLiteral(int.class, "field2", 2),
                    new EnumLiteral(int.class, "field3", 4),
                    new EnumLiteral(int.class, "field4", 8),
                    SKIP_VALUE,
                    SKIP_VALUE
            }
    };

    public void testArrayOtherBindings() {
        final JClass rClassPublic = addRecordClass("ArrayOtherBindingsPublic",
                true,
                ARRAY_FIELD_TYPES
        );

        // TODO: ???
        createECD(InstrumentType.class, false);
        createECD(ECD_FIELDS_BITMASK);

        final JMethod initEmpty = rClassPublic.addMethod(PRIVATE, void.class, "initEmpty");
        final JCompoundStatement initEmptyBody = initEmpty.body();
        final JMethod initNulls = rClassPublic.addMethod(PRIVATE, void.class, "initNulls");
        final JCompoundStatement initNullsBody = initNulls.body();

        final JClassDefinition classDefinition = recordClassMap.get(rClassPublic.fullName());
        final String[] fields = classDefinition.fieldNames;
        for (String field : fields) {
            initEmptyBody.add(
                    rClassPublic.thisVar().access().field(field).assign(CTXT.newExpr(classDefinition.getFieldType(field)))
            );
            initNullsBody.add(
                    rClassPublic.thisVar().access().field(field).assign(CTXT.nullLiteral())
            );
        }

        final JMethod methodImpl = jclass.addMethod(PRIVATE, void.class, "testArrayOtherBindings");
        for (int i = 0; i < ARRAY_POSTFIXES.length; i++) {
            String postfix = ARRAY_POSTFIXES[i];
            createArrayRCD(rClassPublic, true, ARRAY_DATA_TYPES[i], postfix);
            testArrayOtherBindings(methodImpl, classDefinition, ARRAY_DATA_VALUES[i], postfix);
        }

        addCompIntpTests("testArrayOtherBindings", methodImpl);
    }

    private void testArrayOtherBindings(JMethod method, JClassDefinition rClassDefinition, Object[] values, String postfix) {
        JClass rclass = rClassDefinition.jclass;
        String rcdName = getRCDName(rclass, true, false, postfix);
        JExpr rcdNullable = rcdMap.get(rcdName);

        // create and initialize msg
        final JCompoundStatement body = CTXT.compStmt();
        method.body().add(body);
        JExpr msg = body.addVar(FINAL, rclass, "msg", CTXT.newExpr(rclass));

        body.add(msg.call("initEmpty"));
        final String[] fields = rClassDefinition.fieldNames;
        for (int i = 0; i < fields.length; i++) {
            if (values[i] == SKIP_VALUE)
                continue;

            final String field = fields[i];
            JExpr value = getValueExpr(values[i]);
            if (rClassDefinition.types[i] == ByteArrayList.class)
                value = value.cast(byte.class);
            else if (rClassDefinition.types[i] == ShortArrayList.class)
                value = value.cast(short.class);

            body.add(
                    msg.field(field).call("add", value));
        }

        // do encoding/decoding for one-element, empty and null ARRAY
        JExpr textExpr = getStringLiteral("public " + postfix + " one-value");
        body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullable));

        body.add(msg.call("initEmpty"));
        textExpr = getStringLiteral("public " + postfix + " empty");
        body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullable));

        body.add(msg.call("initNulls"));
        textExpr = getStringLiteral("public " + postfix + " null");
        body.add(jclass.callSuperMethod("testRcdBound", textExpr, msg, rcdNullable));
    }

    /*
     * All DateTimeDataType bindings
     */

    private final static Class<?>[] DATETIME_FIELD_TYPES =
            new Class<?>[]{
                    long.class
            };

    private final static Class<?>[] DATETIME_FIELD_TYPES_NULLABLE = null;

    private final static String[] DATETIME_ENCODINGS =
            new String[]{
                    null,
            };

    private final static class DateTimeLiteral {
        private final Object value;

        private DateTimeLiteral(Object value) {
            this.value = value;
        }
    }

    private final static Object[] DATETIME_NORMAL_VALUES =
            new Object[]{
                    new DateTimeLiteral(1314899629573L) // 2011-09-01 17:53:49.573 GMT
            };

    private final static Object[] DATETIME_NULL_VALUES =
            new Object[]{
                    new NonNullLiteral(new DateTimeLiteral(1314899629573L)),
            };

    private final static Object[] DATETIME_NULL_VALUES_4NET = null;

    private final static DataTypeEntry DATETIME_ENTRY = new DataTypeEntry(
            "DateTime",
            DateTimeDataType.class,
            DATETIME_FIELD_TYPES,
            DATETIME_FIELD_TYPES_NULLABLE,
            DATETIME_ENCODINGS,
            DATETIME_NORMAL_VALUES,
            DATETIME_NULL_VALUES,
            DATETIME_NULL_VALUES_4NET
    );

    /*
     * All TimeOfDayDataType bindings
     */

    private final static Class<?>[] TIMEOFDAY_FIELD_TYPES =
            new Class<?>[]{
                    int.class
            };

    private final static Class<?>[] TIMEOFDAY_FIELD_TYPES_NULLABLE = null;

    private final static String[] TIMEOFDAY_ENCODINGS = {null};

    private final static Object[] TIMEOFDAY_NULL_VALUES = {
            new NullLiteral(CTXT.staticVarRef(TimeOfDayDataType.class, "NULL"))
    };

    private final static class TimeOfDayLiteral {
        private final Object value;

        private TimeOfDayLiteral(Object value) {
            this.value = value;
        }
    }

    private final static Object[] TIMEOFDAY_NORMAL_VALUES = {
            new TimeOfDayLiteral(TimeFormatter.parseTimeOfDayMillis("22:39:57.955"))
    };

    private final static Object[] TIMEOFDAY_NULL_VALUES_4NET = null;

    private final static DataTypeEntry TIMEOFDAY_ENTRY = new DataTypeEntry(
            "TimeOfDay",
            TimeOfDayDataType.class,
            TIMEOFDAY_FIELD_TYPES,
            TIMEOFDAY_FIELD_TYPES_NULLABLE,
            TIMEOFDAY_ENCODINGS,
            TIMEOFDAY_NORMAL_VALUES,
            TIMEOFDAY_NULL_VALUES,
            TIMEOFDAY_NULL_VALUES_4NET
    );


    /*
     * All CharDataType bindings
     */

    private final static Class<?>[] CHAR_FIELD_TYPES = {char.class};

    private final static Class<?>[] CHAR_FIELD_TYPES_NULLABLE = null;

    private final static String[] CHAR_ENCODINGS = {null};

    private final static Object[] CHAR_NULL_VALUES = {
            new NullLiteral(CTXT.staticVarRef(CharDataType.class, "NULL"))
    };

    private final static Object[] CHAR_NORMAL_VALUES = {
            'Z'
    };

    private final static Object[] CHAR_NULL_VALUES_4NET = null;

    private final static DataTypeEntry CHAR_ENTRY = new DataTypeEntry(
            "Character",
            CharDataType.class,
            CHAR_FIELD_TYPES,
            CHAR_FIELD_TYPES_NULLABLE,
            CHAR_ENCODINGS,
            CHAR_NORMAL_VALUES,
            CHAR_NULL_VALUES,
            CHAR_NULL_VALUES_4NET
    );

    private void testDataType(DataTypeEntry entry, boolean isNetNullable) {
        final String testName = getTestName(entry.name, isNetNullable);
        final JMethod methodImpl = jclass.addMethod(PRIVATE, void.class, testName);

        testDataType(methodImpl, true, isNetNullable, entry);
        if (!useProperty)
            testDataType(methodImpl, false, isNetNullable, entry);

        addCompIntpTests(testName, methodImpl);
    }

    private void testDataType(JMethod methodImpl, boolean isPublic, boolean isNetNullable, DataTypeEntry entry) {
        final JClass rclass = addRecordClass(getName("All" + entry.name, isPublic, isNetNullable),
                isPublic,
                isNetNullable ? entry.nullValues : null,
                isNetNullable ? entry.fieldTypesNetNullable : entry.fieldTypes
        );
        createRCD(rclass, true, entry.dataTypeClass, entry.encodings);
        createRCD(rclass, false, entry.dataTypeClass, entry.encodings);
        createStaticRCD(rclass, true, entry.dataTypeClass, entry.encodings, entry.normalValues, null);
        createStaticRCD(rclass, false, entry.dataTypeClass, entry.encodings, entry.normalValues, null);
        createStaticRCD(rclass, true, entry.dataTypeClass, entry.encodings, entry.nullValues, "NV");

        testRoundTrip(rclass, isPublic, methodImpl, entry.normalValues, entry.nullValues);
        testNullViolation(rclass, methodImpl, entry.normalValues, entry.nullValues, entry.encodings);

        if (isNetNullable) {
            createStaticRCD(rclass, true, entry.dataTypeClass, entry.encodings, entry.nullValues4NET, "NV_4NET");
            testRoundTrip(rclass, isPublic, methodImpl, entry.normalValues, entry.nullValues4NET);
            testNullViolation(rclass, methodImpl, entry.normalValues, entry.nullValues4NET, entry.encodings);
        }

        if (entry.badValues != null)
            testConstraintViolation(rclass, methodImpl, entry.normalValues, entry.badValues, entry.encodings);
    }

    private void createECD(Class<?> enumType, boolean isBitmask) {
        final String name = enumType.getName();
        if (!rcdMap.containsKey(name)) {
            JExpr[] args;
            if (enumType.isEnum()) {
                final Object[] values = enumType.getEnumConstants();
                args = new JExpr[3 + values.length];
                int i = 0;
                args[i++] = CTXT.classLiteral(enumType).call("getName");
                args[i++] = CTXT.nullLiteral();
                args[i++] = CTXT.booleanLiteral(isBitmask);
                for (Object value : values) {
                    args[i++] = getStringLiteral(value.toString());
                }

            }
            else
                throw new IllegalArgumentException(enumType.getName());

            JInitMemberVariable rcd = jclass.addVar(PRIVATE | FINAL | STATIC, EnumClassDescriptor.class, enumType.getSimpleName(),
                    CTXT.newExpr(EnumClassDescriptor.class, args)
            );

            rcdMap.put(name, rcd.access());
        }
    }

    private void createECD(EnumClassDescriptor ecd) {
        final String name = ecd.getName();
        if (!rcdMap.containsKey(name)) {
            final EnumValue[] values = ecd.getValues();
            final JExpr[] args = new JExpr[3 + values.length];
            int i = 0;
            args[i++] = CTXT.stringLiteral(name);
            args[i++] = CTXT.nullLiteral();
            args[i++] = CTXT.booleanLiteral(ecd.isBitmask());
            for (EnumValue value : values) {
                args[i++] = CTXT.newExpr(EnumValue.class, getStringLiteral(value.symbol), CTXT.longLiteral(value.value));
            }

            JInitMemberVariable rcd = jclass.addVar(PRIVATE | FINAL | STATIC, EnumClassDescriptor.class, name,
                    CTXT.newExpr(EnumClassDescriptor.class, args)
            );

            rcdMap.put(name, rcd.access());
        }
    }


    private void generateHostClass(String className) {
        int idx = className.lastIndexOf('.');
        if (idx != -1)
            jclass = CTXT.newClass(PUBLIC | FINAL, className.substring(0, idx), className.substring(idx + 1), Test_RecordCodecsBase.class);
        else
            jclass = CTXT.newClass(PUBLIC | FINAL, null, className, Test_RecordCodecsBase.class);

        generateGetFieldString();
    }

    private JClass addRecordClass(String className, boolean isPublic, Class<?>... types) {
        return addRecordClass(className, isPublic, null, types);
    }

    private JClass addRecordClass(String className, boolean isPublic, Object[] null_values, Class<?>... types) {
        final String[] fields = new String[types.length];
        final JClass rclass = jclass.innerClass(
                (isPublic ? PUBLIC : PRIVATE) | FINAL |  STATIC,
                className,
                InstrumentMessage.class);
        for (int i = 0; i < types.length; i++) {
            final Class<?> type = types[i];
            fields[i] = getSimpleName(type) + (i + 1);
            if (useProperty)
                rclass.addProperty(PUBLIC, type, fields[i]);
            else
                rclass.addVar(PUBLIC, type, fields[i]);
        }

        recordClassMap.put(rclass.fullName(), new JClassDefinition(rclass, isPublic, types, fields));

        // toString and equals overrides
        if (fields.length > 0) {
            {
                final JMethod method;
                method = rclass.addMethod(PUBLIC, String.class, "toString");
                method.addAnnotation(OVERRIDE_ANNOTATION);

                final JExpr thisExpr = rclass.thisVar().access();
                if (fields.length == 1) {
                    method.body().add(CTXT.staticCall(String.class, "valueOf", thisExpr.field(fields[0])).returnStmt());
                } else {
                    JExpr sumExpr = thisExpr.field(fields[0]);
                    for (int i = 1; i < fields.length; i++) {
                        sumExpr = CTXT.binExpr(sumExpr, "+", COMMA_STRING);
                        sumExpr = CTXT.binExpr(sumExpr, "+", thisExpr.field(fields[i]));
                    }
                    method.body().add(sumExpr.returnStmt());
                }
            }
            {
                final JMethod method;
                method = rclass.addMethod(PUBLIC, boolean.class, "equals");
                method.addAnnotation(OVERRIDE_ANNOTATION);

                final JMethodArgument arg = method.addArg(0, Object.class, "o");
                method.body().add(CTXT.ifStmt(CTXT.instanceOf(arg, rclass).not(), CTXT.falseLiteral().returnStmt()));
                final JLocalVariable otherExpr = method.body().addVar(0, rclass, "other", arg.cast(rclass));

                JExpr thisExpr = rclass.thisVar().access();
                JExpr eqExpr = null;
                for (int i = 0; i < fields.length; i++) {
                    Class<?> type = types[i];
                    JExpr eq = genEquals(thisExpr.field(fields[i]), otherExpr.field(fields[i]), type);
                    final Object null_value = null_values != null ? null_values[i] : null;
                    if (null_value != null ){

                        eq = CTXT.binExpr(eq, "||",
                                CTXT.binExpr(
                                        genEquals(thisExpr.field(fields[i]), null_value, type), "&&",
                                        CTXT.binExpr(otherExpr.field(fields[i]), "==", CTXT.nullLiteral())));

                        // TODO: remove it, when setStaticFields be fixed
                        eq = CTXT.binExpr(eq, "||",
                                CTXT.binExpr(
                                        genEquals(otherExpr.field(fields[i]), null_value, type), "&&",
                                        CTXT.binExpr(thisExpr.field(fields[i]), "==", CTXT.nullLiteral())));
                    }

                    eqExpr = eqExpr != null ? CTXT.binExpr(eqExpr, "&&", eq) : eq;
                }
                method.body().add(eqExpr.returnStmt());
            }
        }

        return rclass;
    }

    private JExpr genEquals(JExpr f1, Object f2, Class<?> type) {
        final JExpr f2Expr = (f2 instanceof JExpr) ? (JExpr) f2 : getValueExpr(f2);
        final Class<?> baseType = type;

        if (type == String.class || type == CharSequence.class)
            return CTXT.staticCall(Util.class, "equals", f1, f2Expr);
        else if (baseType == float.class) {
            if (f2 instanceof Float && ((Float) f2).isNaN())
                return  CTXT.staticCall(Float.class, "isNaN", f1);
            else
                return CTXT.binExpr(CTXT.staticCall(Float.class, "compare", f1, f2Expr), "==", CTXT.intLiteral(0));
        } else if (baseType == double.class) {
            if (f2 instanceof Double && ((Double) f2).isNaN())
                return  CTXT.staticCall(Double.class, "isNaN", f1);
            else
                return  CTXT.binExpr(CTXT.staticCall(Double.class, "compare", f1, f2Expr), "==", CTXT.intLiteral(0));
        } else if (ArrayTypeUtil.isSupported(baseType))
            return CTXT.staticCall(Util.class, "xequals", f1, f2Expr);
        else
            return CTXT.binExpr(f1, "==", f2Expr);
    }

    private JExpr genRCDInitializer(JClass recordClass, boolean isStatic, DataType[] dataTypes, Object... values) {
        final String[] fields = recordClassMap.get(recordClass.fullName()).fieldNames;
        if (fields == null)
            throw new IllegalArgumentException("Unknown class definition " + recordClass.fullName());
        if (fields.length != dataTypes.length)
            throw new IllegalArgumentException("Types and fields length are different " + dataTypes.length + ", " + fields.length);
        if (values.length > 0 && values.length != dataTypes.length)
            throw new IllegalArgumentException("Types and values length are different " + dataTypes.length + ", " + values.length);

        final ArrayList<JExpr> args = new ArrayList<JExpr>(4 + dataTypes.length);
        int i = 0;
        args.add(CTXT.classLiteral(recordClass).call("getName"));
        args.add(CTXT.nullLiteral());
        args.add(CTXT.falseLiteral());
        args.add(CTXT.nullLiteral());
        for (int j = 0; j < dataTypes.length; j++) {
            final DataType dataType = dataTypes[j];
            if (dataType == null)
                continue;

            final JExpr typeExpr = getDataTypeExpr(dataType);
            if (isStatic)
                args.add(CTXT.newExpr(
                        StaticDataField.class,
                        getStringLiteral(fields[j]),
                        CTXT.nullLiteral(),
                        typeExpr,
                        getValueExpr4Static(values[j]))
                );
            else
                args.add(CTXT.newExpr(
                        NonStaticDataField.class,
                        getStringLiteral(fields[j]),
                        CTXT.nullLiteral(),
                        typeExpr
                ));
        }

        return CTXT.newExpr(RecordClassDescriptor.class, args.toArray(new JExpr[args.size()]));
    }

    private JExpr genRCDInitializer(JClass recordClass, DataField[] fields) {
        final JExpr[] args = new JExpr[4 + fields.length];
        int i = 0;
        args[i++] = CTXT.classLiteral(recordClass).call("getName");
        args[i++] = CTXT.nullLiteral();
        args[i++] = CTXT.falseLiteral();
        args[i++] = CTXT.nullLiteral();
        for (DataField field : fields) {
            final JExpr typeExpr = getDataTypeExpr(field.getType());
            // new NonStaticDataField("", null,  new deltix.qsrv.hf.pub.md.IntegerDataType("INT32", true)),
            if (field instanceof NonStaticDataField)
                args[i++] = CTXT.newExpr(
                        NonStaticDataField.class,
                        getStringLiteral(field.getName()),
                        getStringLiteral(field.getTitle()),
                        typeExpr,
                        getStringLiteral(((NonStaticDataField) field).getRelativeTo())
                );
            else {
                args[i++] = CTXT.newExpr(
                        StaticDataField.class,
                        getStringLiteral(field.getName()),
                        getStringLiteral(field.getTitle()),
                        typeExpr,
                        getStringLiteral(((StaticDataField) field).getStaticValue())
                );
            }
        }

        return CTXT.newExpr(RecordClassDescriptor.class, args);
    }

    private JExpr getDataTypeExpr(DataType dataType) {
        final Class clazz = dataType.getClass();
        final Constructor[] constructors = clazz.getConstructors();
        final Class<?>[] params = constructors[0].getParameterTypes();
        final ArrayList<JExpr> args2 = new ArrayList<JExpr>();
        int idx = 0;
        // encoding param
        if (params[idx] == String.class) {
            args2.add(CTXT.stringLiteral(dataType.getEncoding()));
            idx++;
        }
        // nullable
        if (params[idx] == boolean.class) {
            args2.add(CTXT.booleanLiteral(dataType.isNullable()));
            idx++;
        }
        // EnumDataType
        if (params.length > idx && params[idx] == EnumClassDescriptor.class) {
            final String ecdName = trimAssemplyPrefix(((EnumDataType) dataType).descriptor.getName());
            final JExpr ecd = rcdMap.get(ecdName);
            assert ecd != null;
            args2.add(ecd);
            idx++;
        }
        // VarcharDataType MultiLine
        if (params.length > idx && params[idx++] == boolean.class) {
            args2.add(CTXT.booleanLiteral(((VarcharDataType) dataType).isMultiLine()));
            //idx++;
        }

        // ARRAY type
        if (dataType instanceof ArrayDataType) {
            DataType underline  = ((ArrayDataType)dataType).getElementDataType();
            args2.add(getDataTypeExpr(underline));
            idx++;
        }

        return CTXT.newExpr(dataType.getClass(), args2.toArray(new JExpr[args2.size()]));
    }

    private void addCompIntpTests(String name, JMethod methodImpl) {
        JMethod method = addTestMethod(name + "Comp");
        JCompoundStatement body = method.body();
        body.add(jclass.callSuperMethod("setUpComp"));
        body.add(methodImpl.callThis());

        method = addTestMethod(name + "Intp");
        body = method.body();
        body.add(jclass.callSuperMethod("setUpIntp"));
        body.add(methodImpl.callThis());
    }

    private JMethod addTestMethod(String methodName) {
        final JMethod method = jclass.addMethod(PUBLIC, void.class, methodName);
        method.addAnnotation(TEST_ANNOTATION);
        return method;
    }

    private void assignFieldValues(JCompoundStatement body, JExpr msg, Object... values) {
        int len = values.length;
        if (len % 2 != 0)
            throw new IllegalArgumentException("broken name/value pattern");

        for (int i = 0; i < len; i += 2) {
            final Object value = values[i + 1];
            if (values[i] != SKIP_VALUE)
                body.add(msg.field((String) values[i]).assign(getValueExpr(value)));
        }
    }

    private void assignFieldValues(JCompoundStatement body, JClass jclass, JExpr msg, Object... values) {
        final JClassDefinition classDefinition = recordClassMap.get(jclass.fullName());
        final String[] fields = classDefinition.fieldNames;
        final Class<?>[] types = classDefinition.types;
        if (fields == null)
            throw new IllegalArgumentException("Unknown class definition " + jclass.fullName());
        if (fields.length != values.length)
            throw new IllegalArgumentException("Values and fields length are different " + values.length + ", " + fields.length);

        for (int i = 0; i < fields.length; i++) {
            if (values[i] != SKIP_VALUE) {
                body.add(msg.field(fields[i]).assign(getValueExpr(values[i])));
            }
        }
    }

    private static JExpr getValueExpr4Static(Object value) {
        if (value == SKIP_VALUE || value == null || value instanceof NullLiteral)
            return CTXT.nullLiteral();
        else if (value instanceof AlphanumericLiteral)
            return getStringLiteral(((AlphanumericLiteral) value).value);
        else if (value instanceof EnumLiteral) {
            final EnumLiteral el = (EnumLiteral) value;
            return getStringLiteral(el.value != null ? el.value.symbol : null);
        } else if (value instanceof NonNullLiteral)
            return getValueExpr4Static(((NonNullLiteral) value).value);
        else if (value instanceof DateTimeLiteral)
            return getValueExpr(GMT.formatDateTimeMillis((Long) ((DateTimeLiteral) value).value));
        else if (value instanceof TimeOfDayLiteral)
            return getValueExpr(TimeFormatter.formatTimeofDayMillis((Integer) ((TimeOfDayLiteral) value).value));
        else if (value instanceof ToStringLiteral)
            return getStringLiteral(((ToStringLiteral) value).str);
        else
            return getStringLiteral(value);
    }

    private static JExpr getStringLiteral(Object value) {
        if (value != null && (value instanceof JExpr)) {
            final String s = toString(value);
            if ("java.lang.Integer.MAX_VALUE".equals(s))
                return CTXT.stringLiteral("2147483647");
            else if ("deltix.qsrv.hf.pub.md.IntegerDataType.INT8_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.IntegerDataType.INT16_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.IntegerDataType.INT32_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.IntegerDataType.INT64_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.IntegerDataType.INT48_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.IntegerDataType.PUINT30_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.IntegerDataType.PUINT61_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.IntegerDataType.PINTERVAL_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.DataTypeNulls.UINT8_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.DataTypeNulls.UINT16_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.DataTypeNulls.UINT32_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.DataTypeNulls.UINT48_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.DataTypeNulls.UINT64_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.DataTypeNulls.INT8_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.DataTypeNulls.INT16_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.DataTypeNulls.INT32_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.DataTypeNulls.INT48_NULL".equals(s) ||
                    "deltix.qsrv.hf.pub.md.DataTypeNulls.INT64_NULL".equals(s))
                return CTXT.nullLiteral();
            else
                throw new IllegalArgumentException("unexpected expression " + s);
        }

        return value != null ? CTXT.stringLiteral(value.toString()) : CTXT.nullLiteral();
    }

    private static JExpr getValueExpr(Object value) {
        if (value == SKIP_VALUE)
            throw new IllegalArgumentException("SKIP_VALUE");
        else if (value == null)
            return CTXT.nullLiteral();
        else if (value instanceof String)
            return CTXT.stringLiteral((String) value);
        else if (value instanceof Number) {
            if (value instanceof Long)
                return CTXT.longLiteral((Long) value);
            else if (value instanceof Double)
                return CTXT.doubleLiteral((Double) value);
            else if (value instanceof Float)
                return CTXT.floatLiteral((Float) value);
            else if (value instanceof Integer)
                return CTXT.intLiteral((Integer) value);
            else if (value instanceof Short)
                return CTXT.intLiteral((Short) value);
            else
                return CTXT.intLiteral((Byte) value);
        } else if (value instanceof Boolean)
            return CTXT.booleanLiteral((Boolean) value);
        else if (value instanceof Character)
            return CTXT.charLiteral((Character) value);
        else if (value instanceof JExpr)
            return (JExpr) value;
        else if (value instanceof AlphanumericLiteral) {
            final AlphanumericLiteral an = (AlphanumericLiteral) value;
            return an.value == null ?
                    CTXT.staticVarRef(ExchangeCodec.class, "NULL") :
                    CTXT.longLiteral(new AlphanumericCodec(an.size).encodeToLong(an.value));
        } else if (value instanceof Enum)
            return CTXT.enumLiteral((Enum<?>) value);
        else if (value instanceof EnumLiteral) {
            final EnumLiteral el = (EnumLiteral) value;
            return getValueExpr(el.value != null ? el.value.value : EnumDataType.NULL).cast(el.clazz);
        } else if (value instanceof NonNullLiteral)
            return getValueExpr(((NonNullLiteral) value).value);
        else if (value instanceof NullLiteral)
            return getValueExpr(((NullLiteral) value).value);
        else if (value instanceof DateTimeLiteral)
            return getValueExpr(((DateTimeLiteral) value).value);
        else if (value instanceof TimeOfDayLiteral)
            return getValueExpr(((TimeOfDayLiteral) value).value);
        else if (value instanceof ToStringLiteral)
            return getValueExpr(((ToStringLiteral) value).value);
        else
            throw new IllegalArgumentException("value type is not supported " + value.getClass());
    }

    private JExpr encode(JCompoundStatement body, JExpr msg, JExpr rcd) {
        //MemoryDataOutput out = boundEncode(msg, cdMsgClassStringPublic);
        return body.addVar(0, MemoryDataOutput.class, "out", jclass.callSuperMethod("boundEncode", msg, rcd));
    }

    private void decodeFromOut(JCompoundStatement body, JExpr msg, JExpr rcd, JExpr out) {
        // in = new MemoryDataInput(out);
        JLocalVariable inVar = body.addVar(0, MemoryDataInput.class, "in", CTXT.newExpr(MemoryDataInput.class, out));
        decode(body, msg, rcd, inVar);
    }

    private void decode(JCompoundStatement body, JExpr msg, JExpr rcd, JExpr in) {
        //boundDecode(msg2, cdMsgClassStringPublicStatic, in);
        body.add(jclass.callSuperMethod("boundDecode", msg, rcd, in));
    }



    private static JExpr assertEquals(String text, JExpr msg, JExpr msg2) {
        return CTXT.staticCall(Assert.class, "assertEquals",
                getStringLiteral(text),
                msg.call(TO_STRING),
                msg2.call(TO_STRING)
        );
    }

    private static JExpr assertEquals(String text, String msg, JExpr msg2) {
        return CTXT.staticCall(Assert.class, "assertEquals",
                getStringLiteral(text),
                getStringLiteral(msg),
                msg2.call("toString")
        );
    }

    private static JExpr assertFail(String text) {
        return CTXT.staticCall(Assert.class, "fail", getStringLiteral(text));
    }

    private static JExpr getStringLiteral(String value) {
        return value != null ? CTXT.stringLiteral(value) : CTXT.nullLiteral();
    }

    private void generateGetFieldString() {
        final JMethod method = jclass.addMethod(PRIVATE, String.class, "getFieldString");
        final JMethodArgument argMsg = method.addArg(0, Object.class, "msg");
        final JMethodArgument argName = method.addArg(0, String.class, "name");

        final JTryStatement tryStmt = CTXT.tryStmt();
        method.body().add(tryStmt);
        tryStmt.tryStmt().add(argMsg.call("getClass").call("getField", argName).call(TO_STRING).returnStmt());
        final JCompoundStatement catchStmt = tryStmt.addCatch(NoSuchFieldException.class, "e");
        JExpr var = tryStmt.catchVariable(NoSuchFieldException.class);
        catchStmt.add(CTXT.newExpr(RuntimeException.class, var).throwStmt());
    }

    private static String getRCDName(JClass rclass, boolean isNullable, boolean isStatic, String postfix) {
        return "rcd" + rclass.name() + (isNullable ? "Nullable" : "NotNullable") + (isStatic ? "Static" : "") + (postfix != null ? postfix : "");
    }

    @SuppressWarnings("unchecked")
    private static <T extends DataType> T newDataType(Class<T> dataTypeClass, String encoding, boolean isNullable) {
        // optimize using reflection?
        if (dataTypeClass == IntegerDataType.class)
            return (T) new IntegerDataType(encoding, isNullable);
        else if (dataTypeClass == FloatDataType.class)
            return (T) new FloatDataType(encoding, isNullable);
        else if (dataTypeClass == VarcharDataType.class)
            return (T) new VarcharDataType(encoding, isNullable, false);
        else if (dataTypeClass == EnumDataType.class) {
            final EnumClassDescriptor ecd = ecdMap.get(encoding);
            if (ecd == null)
                throw new IllegalArgumentException(encoding);
            else
                return (T) new EnumDataType(isNullable, ecd);
        } else if (dataTypeClass == BooleanDataType.class)
            // suppress nullable on boolean field
            return (T) new BooleanDataType(isNullable && !NOT_NULL.equals(encoding));
        else if (dataTypeClass == DateTimeDataType.class)
            return (T) new DateTimeDataType(isNullable);
        else if (dataTypeClass == TimeOfDayDataType.class)
            return (T) new TimeOfDayDataType(isNullable);
        else if (dataTypeClass == CharDataType.class)
            return (T) new CharDataType(isNullable);
        else
            throw new IllegalArgumentException(dataTypeClass.getName());
    }

    private static String getSimpleName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    private static final StringBuilder sb = new StringBuilder();

    private static String getName(String prefix, boolean isPublic, boolean isNullable) {
        sb.setLength(0);
        sb.append(prefix);
        sb.append(isPublic ? "Public" : "Private");
        if (isNullable)
            sb.append("Nullable");
        return sb.toString();
    }

    private static String getName(Class<?> clazz, boolean isPublic) {
        sb.setLength(0);
        sb.append('A');
        sb.append(Util.toBoxed(clazz).getSimpleName());

        sb.append(isPublic ? "Public" : "Private");
        return sb.toString();
    }

    private static String getTestName(String typeName, boolean isNetNullable) {
        sb.setLength(0);
        sb.append("test").append(typeName).append("DataType");
        if (isNetNullable)
            sb.append("Nullable");

        return sb.toString();
    }

    private static class NonNullLiteral {
        private final Object value;

        private NonNullLiteral(Object value) {
            this.value = value;
        }
    }

    private static class NullLiteral {
        private final Object value;

        private NullLiteral(Object value) {
            this.value = value;
        }
    }

    // when string representation (for static value) differs from value.toString()
    private static class ToStringLiteral {
        private final Object value;
        private final String str;

        private ToStringLiteral(Object value, String str) {
            this.value = value;
            this.str = str;
        }
    }

    // contains all references necessary to generate test for a DataType
    private final static class  DataTypeEntry {
        private final String name;
        private final Class<? extends DataType> dataTypeClass;
        private final Class<?>[] fieldTypes;
        private final Class<?>[] fieldTypesNetNullable;
        private final String[] encodings;
        private final Object[] normalValues;
        private final Object[] nullValues;
        private final Object[] nullValues4NET;
        private final Object[] badValues;

        private DataTypeEntry(String name, Class<? extends DataType> dataTypeClass, Class<?>[] fieldTypes, Class<?>[] fieldTypesNetNullable, String[] encodings, Object[] normalValues, Object[] nullValues, Object[] nullValues4NET) {
            this(name, dataTypeClass, fieldTypes, fieldTypesNetNullable, encodings, normalValues, nullValues, nullValues4NET, null);
        }

        private DataTypeEntry(String name, Class<? extends DataType> dataTypeClass, Class<?>[] fieldTypes, Class<?>[] fieldTypesNetNullable, String[] encodings, Object[] normalValues, Object[] nullValues, Object[] nullValues4NET, Object[] badValues) {
            this.name = name;
            this.dataTypeClass = dataTypeClass;
            this.fieldTypes = fieldTypes;
            this.fieldTypesNetNullable = fieldTypesNetNullable;
            this.encodings = encodings;
            this.normalValues = normalValues;
            this.nullValues = nullValues;
            this.nullValues4NET = nullValues4NET;
            this.badValues = badValues;
        }
    }
}

// Hot TODO:
// +fix all tests
// *add binding validation for the rest of types
// +fix old junit tests
// enum/flag validation on encoding
// fix setStaticFields for nullables (correct nulls)
// integer/float range check ???
// enum/flag support long
// enum support type extension

// TODO:
// 1.Check early exception on field binding (+INTEGER)
// 1.1 Binding for .NET case
// 2. INTEGER limit check
// +3. INTEGER natural range check
// +3.1 INTEGER null check
// 3.1.1 for StaticDataField
// 3.2 INTEGER range limit for StaticDataField
// +3.3 INTEGER range validation for unsigned int (-123) !!!
// 3.4 Static oversized binding null-value-check

// 4. FLOAT
// +4.1 non-static null
// [x]4.2 support oversized binding!!!
// [x]4.3 natural range check
// +4.4. early binding exception

// +5. VARCHAR
// +5.1 early binding exception
// 5.2 charset and length limit check
// +5.3 System.Text.StringBuilder

// 6. ENUM
// +6.1 early binding exception (int->byte under-size)
// 6.2 check invalid integer entries on encoding
// issues: long support is not implemented;
// invalid value of string-bound field produce non-descriptive exception

// 7. BITMASK
// +7.1 support BITMASK in Java
// 7.2 check invalid integer entries on encoding

// 8. BINARY
// 8.1 remove byte[] type

// 9. TIMESTAMP

// TIMEOFDAY, CHAR, BINARY

// spec corrections
// Java float may be bound only to IEEE32
// Character set for ALPHANUMERIC

// didn't catch
// +NPE on private field under .NET
// +Compilation error in encoder for CharSequence under .NET
// fix FixedUnboundEncoderImpl.checkLimit: Test_RecordCodecs, line 623
// fix skip: Unimplemented for QBinaryType
// follow the rule, when truncated msg is read to non-nullable field:
//      java.lang.IllegalStateException: cannot write null to not nullable field
// fix Test_RecordCodecs5 under IKVM