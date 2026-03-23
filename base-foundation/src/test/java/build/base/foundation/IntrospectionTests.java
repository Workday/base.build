package build.base.foundation;

import build.base.foundation.example.AbstractPerson;
import build.base.foundation.example.ConcretePerson;
import build.base.foundation.tuple.Tuple;
import static build.base.foundation.Introspection.getParameterType;
import static build.base.foundation.Introspection.parameterTypes;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link Introspection}.
 *
 * @author brian.oliver
 * @since Jul-2018
 */
class IntrospectionTests {

    /**
     * Ensure all declared {@link Field}s of an inherited {@link Class} are returned.
     */
    @Test
    void shouldGetAllDeclaredFieldsFromAClass() {
        assertThat(Introspection.getAllDeclaredFields(ConcretePerson.class)
            .filter(method -> AbstractPerson.class.isAssignableFrom(method.getDeclaringClass())))
            .hasSize(6);
    }

    /**
     * Ensure all declared {@link Method}s of an inherited {@link Class} are returned.
     */
    @Test
    void shouldGetAllDeclaredMethodsFromAClass() {
        assertThat(Introspection.getAllDeclaredMethods(ConcretePerson.class)
            .filter(method -> AbstractPerson.class.isAssignableFrom(method.getDeclaringClass())))
            .hasSize(9);
    }

    /**
     * Ensure all declared {@link Method}s of an interface {@link Class} are returned.
     */
    @Test
    void shouldGetAllDeclaredMethodsAnFromInterface() {
        assertThat(Introspection.getAllDeclaredMethods(Tuple.class))
            .hasSize(2);
    }

    /**
     * Ensure all declared {@link Method}s of a {@link Class} implementing an interface {@link Class} are returned.
     */
    @Test
    void shouldGetAllDeclaredMethodsFromAClassImplementingAnInterface() {
        assertThat(Introspection
            .getAllDeclaredMethods(AllDeclaredMethodsFromClassImplementingInterfaceTestInterface.Impl.class)
            .filter(method -> !method.getDeclaringClass().equals(Object.class)))
            .hasSize(4);
    }

    /**
     * Ensure all declared {@link Field}s of an abstract {@link Class} are returned.
     */
    @Test
    void shouldGetAllDeclaredFieldsFromAnAbstractClass() {
        assertThat(Introspection.getAllDeclaredFields(AbstractPerson.class))
            .hasSize(5);
    }

    /**
     * Ensure all declared {@link Method}s of an abstract {@link Class} are returned.
     */
    @Test
    void shouldGetAllMethodsFromAnAbstractClass() {
        assertThat(Introspection.getAllDeclaredMethods(AbstractPerson.class)
            .filter(method -> !method.getDeclaringClass().equals(Object.class)))
            .hasSize(7);
    }

    /**
     * Ensure descriptions of various {@link Class}es are returned.
     */
    @Test
    void shouldDescribeClasses() {
        assertThat(Introspection.describe((Class<?>) null))
            .isEqualTo("null");

        assertThat(Introspection.describe(Object.class))
            .isEqualTo("Object");

        assertThat(Introspection.describe(Object[].class))
            .isEqualTo("Object[]");

        assertThat(Introspection.describe(ProcessBuilder.Redirect.class))
            .isEqualTo("ProcessBuilder$Redirect");

        assertThat(Introspection.describe(ProcessBuilder.Redirect[].class))
            .isEqualTo("ProcessBuilder$Redirect[]");

        assertThat(Introspection.describe(ProcessBuilder.Redirect.Type.class))
            .isEqualTo("ProcessBuilder$Redirect$Type");

        assertThat(Introspection.describe(ProcessBuilder.Redirect.Type[].class))
            .isEqualTo("ProcessBuilder$Redirect$Type[]");
    }

    /**
     * Ensure descriptions of various {@link Method} together with their {@link Parameter}s are returned.
     */
    @Test
    void shouldDescribeMethods()
        throws NoSuchMethodException {

        assertThat(Introspection.describe((Method) null))
            .isEqualTo("null");

        assertThat(Introspection.describe(Object.class.getMethod("toString")))
            .isEqualTo("String toString()");

        assertThat(Introspection.describe(Object.class.getMethod("wait", long.class, int.class)))
            .isEqualTo("void wait(long arg0, int arg1)");

        assertThat(Introspection.describe(Class.class.getMethod("getConstructor", Class[].class)))
            .isEqualTo("Constructor getConstructor(Class<?>[] arg0)");

        assertThat(Introspection.describe(Thread.class.getMethod("sleep", long.class, int.class)))
            .isEqualTo("void sleep(long arg0, int arg1)");
    }

    /**
     * Ensure descriptions of various {@link Constructor}s together with their {@link Parameter}s are returned.
     */
    @Test
    void shouldDescribeConstructors()
        throws NoSuchMethodException {
        assertThat(Introspection.describe((Constructor<?>) null))
            .isEqualTo("null");

        assertThat(Introspection.describe(String.class.getConstructor()))
            .isEqualTo("String()");

        assertThat(Introspection.describe(String.class.getConstructor(String.class)))
            .isEqualTo("String(String arg0)");

        assertThat(Introspection.describe(String.class.getConstructor(char[].class)))
            .isEqualTo("String(char[] arg0)");

        assertThat(Introspection.describe(String.class.getConstructor(char[].class, int.class, int.class)))
            .isEqualTo("String(char[] arg0, int arg1, int arg2)");
    }

    /**
     * Ensure all declared {@link Annotation}s by type are returned.
     */
    @Test
    void shouldGetAllDeclaredAnnotationsByType() {
        assertThat(Introspection.getAllDeclaredAnnotationsByType(Object.class, Description.class))
            .isEmpty();

        assertThat(Introspection.getAllDeclaredAnnotationsByType(BaseClass.class, Description.class)
            .map(Object::toString))
            .containsExactly("@build.base.foundation.IntrospectionTests.Description(\"The BaseClass Description\")");

        assertThat(Introspection.getAllDeclaredAnnotationsByType(BaseInterface.class, Description.class)
            .map(Object::toString))
            .containsExactly(
                "@build.base.foundation.IntrospectionTests.Description(\"The BaseInterface Description\")");

        assertThat(Introspection.getAllDeclaredAnnotationsByType(Singleton.class, Description.class)
            .map(Object::toString))
            .containsExactly(
                "@build.base.foundation.IntrospectionTests.Description(\"The Singleton Description\")",
                "@build.base.foundation.IntrospectionTests.Description(\"The Repeated Description\")",
                "@build.base.foundation.IntrospectionTests.Description(\"The BaseInterface Description\")");

        assertThat(Introspection.getAllDeclaredAnnotationsByType(InheritedSingleton.class, Description.class)
            .map(Object::toString))
            .containsExactly(
                "@build.base.foundation.IntrospectionTests.Description(\"The InheritedSingleton Description\")",
                "@build.base.foundation.IntrospectionTests.Description(\"The BaseInterface Description\")",
                "@build.base.foundation.IntrospectionTests.Description(\"The BaseClass Description\")");

        assertThat(Introspection.getAllDeclaredAnnotationsByType(ExtendedInterface.class, Description.class)
            .map(Object::toString))
            .containsExactly(
                "@build.base.foundation.IntrospectionTests.Description(\"The ExtendedInterface Description\")",
                "@build.base.foundation.IntrospectionTests.Description(\"Description 1\")",
                "@build.base.foundation.IntrospectionTests.Description(\"Description 2\")",
                "@build.base.foundation.IntrospectionTests.Description(\"The BaseInterface Description\")");

        assertThat(Introspection.getAllDeclaredAnnotationsByType(ExtendedInheritedSingleton.class, Description.class)
            .map(Object::toString))
            .containsExactly(
                "@build.base.foundation.IntrospectionTests.Description(\"The ExtendedInheritedSingleton Description\")",
                "@build.base.foundation.IntrospectionTests.Description(\"The ExtendedInterface Description\")",
                "@build.base.foundation.IntrospectionTests.Description(\"Description 1\")",
                "@build.base.foundation.IntrospectionTests.Description(\"Description 2\")",
                "@build.base.foundation.IntrospectionTests.Description(\"The InheritedSingleton Description\")",
                "@build.base.foundation.IntrospectionTests.Description(\"The BaseInterface Description\")",
                "@build.base.foundation.IntrospectionTests.Description(\"The BaseClass Description\")");
    }

    /**
     * Ensure an {@link Annotation} can be located by type.
     */
    @Test
    void shouldFindSpecifiedAnnotationByType() {
        assertThat(Introspection.findAnnotation(ClassWithDirectInheritedAnnotation.class, Description.class))
            .isPresent();

        assertThat(Introspection.findAnnotation(ClassWithMetaAnnotation.class, Description.class)
            .map(Description::value))
            .isEqualTo(Optional.of("MetaAnnotation"));

        assertThat(Introspection.findAnnotation(ClassWithInheritedMetaAnnotation.class, Description.class)
            .map(Description::value))
            .isEqualTo(Optional.of("MetaAnnotation"));
    }

    /**
     * Ensure primitives are boxed correctly.
     *
     * @param primitiveClass The primitive class to box
     * @param boxedClass     The expected boxed class
     */
    @ParameterizedTest
    @MethodSource("getBoxingUnboxingArguments")
    void shouldBoxPrimitives(final Class<?> primitiveClass, final Class<?> boxedClass) {
        assertThat(Introspection.getBoxedClassIfPrimitive(primitiveClass))
            .isEqualTo(Optional.of(boxedClass));

        assertThat(Introspection.getBoxedClass(primitiveClass))
            .isEqualTo(boxedClass);
    }

    /**
     * Ensure non primitives are handled correctly when attempting to box.
     */
    @Test
    void shouldNotBoxNonPrimitives() {
        assertThat(Introspection.getBoxedClassIfPrimitive(Object.class))
            .isEmpty();

        assertThat(Introspection.getBoxedClass(Object.class))
            .isEqualTo(Object.class);
    }

    /**
     * Ensure null is handled correctly when attempting to box.
     */
    @Test
    void shouldNotBoxNull() {
        assertThat(Introspection.getBoxedClassIfPrimitive(null))
            .isEmpty();

        assertThat(Introspection.getBoxedClass(null))
            .isNull();
    }

    /**
     * Ensure boxed primitive classes are unboxed correctly.
     *
     * @param primitiveClass The expected primitive class
     * @param boxedClass     The boxed class to unbox
     */
    @ParameterizedTest
    @MethodSource("getBoxingUnboxingArguments")
    void shouldUnboxBoxedPrimitives(final Class<?> primitiveClass, final Class<?> boxedClass) {

        assertThat(Introspection.getUnboxedClassIfBoxed(boxedClass))
            .isEqualTo(Optional.of(primitiveClass));

        assertThat(Introspection.getUnboxedClass(boxedClass))
            .isEqualTo(primitiveClass);
    }

    /**
     * Ensure classes that aren't boxed primitives are handled correctly when attempting to unbox.
     */
    @Test
    void shouldNotUnboxNonBoxedClasses() {
        assertThat(Introspection.getUnboxedClassIfBoxed(Object.class))
            .isEmpty();

        assertThat(Introspection.getUnboxedClass(Object.class))
            .isEqualTo(Object.class);
    }

    /**
     * Ensure null is handled correctly when attempting to unbox.
     */
    @Test
    void shouldNotUnboxNull() {
        assertThat(Introspection.getUnboxedClassIfBoxed(null))
            .isEmpty();

        assertThat(Introspection.getUnboxedClass(null))
            .isNull();
    }

    /**
     * Ensure the default value for a primitive and {@link Object} types is returned.
     */
    @Test
    void shouldObtainDefaultValue() {
        assertThat(Introspection.getDefaultValue(int.class))
            .isEqualTo(0);

        assertThat(Introspection.getDefaultValue(Integer.class))
            .isEqualTo(0);

        assertThat(Introspection.getDefaultValue(long.class))
            .isEqualTo(0L);

        assertThat(Introspection.getDefaultValue(Long.class))
            .isEqualTo(0L);

        assertThat(Introspection.getDefaultValue(short.class))
            .isEqualTo((short) 0);

        assertThat(Introspection.getDefaultValue(Short.class))
            .isEqualTo((short) 0);

        assertThat(Introspection.getDefaultValue(byte.class))
            .isEqualTo((byte) 0);

        assertThat(Introspection.getDefaultValue(Byte.class))
            .isEqualTo((byte) 0);

        assertThat(Introspection.getDefaultValue(char.class))
            .isEqualTo('\u0000');

        assertThat(Introspection.getDefaultValue(Character.class))
            .isEqualTo('\u0000');

        assertThat(Introspection.getDefaultValue(float.class))
            .isEqualTo(0.0f);

        assertThat(Introspection.getDefaultValue(Float.class))
            .isEqualTo(0.0f);

        assertThat(Introspection.getDefaultValue(double.class))
            .isEqualTo(0.0d);

        assertThat(Introspection.getDefaultValue(Double.class))
            .isEqualTo(0.0d);

        assertThat(Introspection.getDefaultValue(boolean.class))
            .isFalse();

        assertThat(Introspection.getDefaultValue(Boolean.class))
            .isFalse();

        assertThat(Introspection.getDefaultValue(void.class))
            .isNull();

        assertThat(Introspection.getDefaultValue(Object.class))
            .isNull();

        assertThat(Introspection.getDefaultValue(Class.class))
            .isNull();
    }

    /**
     * Ensure {@link Introspection#getParameterType(Class, Class)} returns the correct values.
     */
    @Test
    void shouldRetrieveParameterType() {
        class ParameterizedClass<T> {

        }

        abstract class AbstractParameterizedClass<T> {

        }

        abstract class AbstractChildOfAbstractParameterizedClass<T>
            extends AbstractParameterizedClass<T> {

        }

        class ClassImplementingIntegerParameterizedInterface
            implements ParameterizedInterface<Integer> {

        }

        class ClassExtendingStringAbstractParameterizedClass
            extends AbstractParameterizedClass<String> {

        }

        class ClassExtendingFloatAbstractChildOfAbstractParameterizedClass
            extends AbstractChildOfAbstractParameterizedClass<Float> {

        }

        class ClassExtendingConcreteLongParameterizedClass
            extends ParameterizedClass<Long> {

        }

        class ClassWithMultipleParameterizedInterfacesAndAbstractClass
            extends AbstractParameterizedClass<String>
            implements ParameterizedInterface<Integer>, AnotherParameterizedInterface<Long> {

        }

        class ClassWithWildcardType
            implements ParameterizedInterface<List<?>> {

        }

        class ClassWithMultipleTypes
            implements MultiParameterizedInterface<Boolean, Integer, Long> {

        }

        assertThat(
            getParameterType(ClassImplementingIntegerParameterizedInterface.class, ParameterizedInterface.class))
            .isEqualTo(Optional.of(Integer.class));

        assertThat(
            getParameterType(ClassExtendingStringAbstractParameterizedClass.class, AbstractParameterizedClass.class))
            .isEqualTo(Optional.of(String.class));

        assertThat(
            getParameterType(
                ClassExtendingFloatAbstractChildOfAbstractParameterizedClass.class,
                AbstractChildOfAbstractParameterizedClass.class))
            .isEqualTo(Optional.of(Float.class));

        assertThat(getParameterType(ClassExtendingConcreteLongParameterizedClass.class, ParameterizedClass.class))
            .isEqualTo(Optional.of(Long.class));

        assertThat(getParameterType(ClassWithWildcardType.class, ParameterizedInterface.class)
            .map(Type::getTypeName))
            .isEqualTo(Optional.of("java.util.List<?>"));

        assertThat(
            getParameterType(
                ClassWithMultipleParameterizedInterfacesAndAbstractClass.class,
                AbstractParameterizedClass.class))
            .isEqualTo(Optional.of(String.class));

        assertThat(
            getParameterType(
                ClassWithMultipleParameterizedInterfacesAndAbstractClass.class,
                ParameterizedInterface.class))
            .isEqualTo(Optional.of(Integer.class));

        assertThat(
            getParameterType(
                ClassWithMultipleParameterizedInterfacesAndAbstractClass.class,
                AnotherParameterizedInterface.class))
            .isEqualTo(Optional.of(Long.class));

        assertThat(getParameterType(ClassWithMultipleTypes.class))
            .isEmpty();

        assertThat(getParameterType(ClassWithMultipleTypes.class, MultiParameterizedInterface.class))
            .isEqualTo(Optional.of(Boolean.class));

        assertThat(parameterTypes(ClassWithMultipleTypes.class, MultiParameterizedInterface.class))
            .containsExactly(Boolean.class, Integer.class, Long.class);

        assertThat(parameterTypes(ClassWithMultipleTypes.class, MultiParameterizedInterface.class))
            .containsExactly(Boolean.class, Integer.class, Long.class);

        // Works with non-parameterized classes
        assertThat(parameterTypes(String.class)).isEmpty();
        assertThat(getParameterType(String.class)).isEmpty();
        assertThat(getParameterType(String.class, Comparable.class)).isEqualTo(Optional.of(String.class));
    }

    /**
     * Ensure the primitive types can be obtained.
     */
    @Test
    void shouldObtainPrimitiveTypes() {
        Introspection.primitives()
            .forEach(primitive -> assertThat(primitive.isPrimitive())
                .isTrue());
    }

    /**
     * Ensure the boxed types can be obtained.
     */
    @Test
    void shouldObtainBoxedTypes() {
        Introspection.boxed()
            .forEach(boxed -> assertThat(Introspection
                .getUnboxedClass(boxed)
                .isPrimitive())
                .isTrue());
    }

    /**
     * Argument provider for boxing and unboxing tests.
     */
    static Stream<Arguments> getBoxingUnboxingArguments() {
        return Stream.of(
            Arguments.of(int.class, Integer.class),
            Arguments.of(long.class, Long.class),
            Arguments.of(short.class, Short.class),
            Arguments.of(byte.class, Byte.class),
            Arguments.of(char.class, Character.class),
            Arguments.of(float.class, Float.class),
            Arguments.of(double.class, Double.class),
            Arguments.of(boolean.class, Boolean.class),
            Arguments.of(void.class, Void.class));
    }

    /**
     * An annotation to provide a description.
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Repeatable(Descriptions.class)
    @interface Description {

        String value() default "";
    }

    /**
     * An annotation to represented zero or more {@link Repeatable} {@link Description}s.
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Descriptions {

        Description[] value();
    }

    /**
     * An meta annotation to provide a description.
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Description("MetaAnnotation")
    @interface MetaDescription {

        String value() default "";
    }

    @Description("The BaseClass Description")
    static abstract class BaseClass {

    }

    @Description("The BaseInterface Description")
    interface BaseInterface {

    }

    static class ClassWithDirectInheritedAnnotation
        implements BaseInterface {

    }

    @MetaDescription("ClassWithMetaAnnotation")
    static class ClassWithMetaAnnotation {

    }

    static class ClassWithInheritedMetaAnnotation
        extends ClassWithMetaAnnotation {

    }

    @Description("The Singleton Description")
    @Description("The Repeated Description")
    static class Singleton
        implements BaseInterface {

    }

    @Description("The InheritedSingleton Description")
    static class InheritedSingleton
        extends BaseClass
        implements BaseInterface {

    }

    @Description("The ExtendedInterface Description")
    @Descriptions({ @Description("Description 1"), @Description("Description 2") })
    interface ExtendedInterface
        extends BaseInterface {

    }

    @Description("The ExtendedInheritedSingleton Description")
    static class ExtendedInheritedSingleton
        extends InheritedSingleton
        implements ExtendedInterface {

    }

    interface AllDeclaredMethodsFromClassImplementingInterfaceTestInterface {

        default void MethodOne() {
        }

        default void MethodTwo() {
        }

        default void MethodThree() {
        }

        class Impl
            implements AllDeclaredMethodsFromClassImplementingInterfaceTestInterface {

            @Override
            public void MethodThree() {

            }
        }
    }

    interface ParameterizedInterface<T> {

    }

    interface MultiParameterizedInterface<T, U, V> {

    }

    interface AnotherParameterizedInterface<T> {

    }
}
