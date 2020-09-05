package com.tngtech.archunit.core.domain;

import com.tngtech.archunit.core.domain.properties.HasAnnotations;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.testutil.Assertions.assertThat;
import static com.tngtech.archunit.testutil.Assertions.assertThatAnnotation;
import static com.tngtech.java.junit.dataprovider.DataProviders.testForEach;

// FIXME: How classes are resolved depending on the configured ClassResolver is part of the import context.
//        Thus this test should actually go into the importer package, not the domain package.
//        To match other open PRs I propose the name `ClassFileImporterAnnotationsTest`
//        and the location next to `ClassFileImporterTest`
@RunWith(DataProviderRunner.class)
public class TransitiveAnnotationsTest {

    @Test
    public void meta_annotation_types_are_transitively_imported() {
        JavaClass javaClass = new ClassFileImporter().importClass(MetaAnnotatedClass.class);
        JavaAnnotation<JavaClass> someAnnotation = javaClass.getAnnotationOfType(SomeAnnotation.class.getName());
        JavaAnnotation<JavaClass> someMetaAnnotation = someAnnotation.getRawType()
                .getAnnotationOfType(SomeMetaAnnotation.class.getName());
        JavaAnnotation<JavaClass> someMetaMetaAnnotation = someMetaAnnotation.getRawType()
                .getAnnotationOfType(SomeMetaMetaAnnotation.class.getName());
        JavaAnnotation<JavaClass> someMetaMetaMetaAnnotation = someMetaMetaAnnotation.getRawType()
                .getAnnotationOfType(SomeMetaMetaMetaAnnotationWithParameters.class.getName());

        assertThat(someMetaMetaMetaAnnotation.getRawType()).matches(SomeMetaMetaMetaAnnotationWithParameters.class);
    }

    @DataProvider
    public static Object[][] elementsAnnotatedWithSomeAnnotation() {
        return testForEach(
                new ClassFileImporter().importClass(MetaAnnotatedClass.class),
                new ClassFileImporter().importClass(ClassWithMetaAnnotatedField.class).getField("metaAnnotatedField"),
                new ClassFileImporter().importClass(ClassWithMetaAnnotatedMethod.class).getMethod("metaAnnotatedMethod"),
                new ClassFileImporter().importClass(ClassWithMetaAnnotatedConstructor.class).getConstructor()
        );
    }

    @Test
    @UseDataProvider("elementsAnnotatedWithSomeAnnotation")
    public void parameters_of_meta_annotations_are_transitively_imported(HasAnnotations<?> annotatedWithSomeAnnotation) {
        JavaAnnotation<?> someAnnotation = annotatedWithSomeAnnotation
                .getAnnotationOfType(SomeAnnotation.class.getName());
        JavaAnnotation<?> metaAnnotationWithParameters = someAnnotation.getRawType()
                .getAnnotationOfType(MetaAnnotationWithParameters.class.getName());

        assertThatAnnotation(metaAnnotationWithParameters).hasEnumProperty("someEnum", SomeEnum.CONSTANT);
        assertThatAnnotation(metaAnnotationWithParameters).hasAnnotationProperty("parameterAnnotation", ParameterAnnotation.class)
                .withClassProperty("value", SomeAnnotationParameterType.class);

        JavaAnnotation<JavaClass> metaMetaMetaAnnotation = someAnnotation
                .getRawType().getAnnotationOfType(SomeMetaAnnotation.class.getName())
                .getRawType().getAnnotationOfType(SomeMetaMetaAnnotation.class.getName())
                .getRawType().getAnnotationOfType(SomeMetaMetaMetaAnnotationWithParameters.class.getName());

        assertThatAnnotation(metaMetaMetaAnnotation)
                .hasClassProperty("classParam", SomeMetaMetaMetaAnnotationClassParameter.class)
                .hasEnumProperty("enumParam", SomeMetaMetaMetaAnnotationEnumParameter.VALUE)
                .hasAnnotationProperty("annotationParam", SomeMetaMetaMetaParameterAnnotation.class)
                .withClassProperty("value", SomeMetaMetaMetaParameterAnnotationClassParameter.class);
    }

    private @interface MetaAnnotationWithParameters {
        SomeEnum someEnum();

        ParameterAnnotation parameterAnnotation();
    }

    private @interface SomeMetaMetaMetaAnnotationWithParameters {
        Class<?> classParam();

        SomeMetaMetaMetaAnnotationEnumParameter enumParam();

        SomeMetaMetaMetaParameterAnnotation annotationParam();
    }

    @SomeMetaMetaMetaAnnotationWithParameters(
            classParam = SomeMetaMetaMetaAnnotationClassParameter.class,
            enumParam = SomeMetaMetaMetaAnnotationEnumParameter.VALUE,
            annotationParam = @SomeMetaMetaMetaParameterAnnotation(SomeMetaMetaMetaParameterAnnotationClassParameter.class)
    )
    private @interface SomeMetaMetaAnnotation {
    }

    @SomeMetaMetaAnnotation
    private @interface SomeMetaAnnotation {
    }

    @MetaAnnotationWithParameters(
            someEnum = SomeEnum.CONSTANT,
            parameterAnnotation = @ParameterAnnotation(SomeAnnotationParameterType.class)
    )
    @SomeMetaAnnotation
    private @interface SomeAnnotation {
    }

    private enum SomeEnum {
        CONSTANT,
        VARIABLE
    }

    private @interface ParameterAnnotation {
        Class<?> value();
    }

    private static class SomeAnnotationParameterType {
    }

    @SomeAnnotation
    private static class MetaAnnotatedClass {
    }

    @SuppressWarnings("unused")
    private static class ClassWithMetaAnnotatedField {
        @SomeAnnotation
        int metaAnnotatedField;
    }

    @SuppressWarnings("unused")
    private static class ClassWithMetaAnnotatedMethod {
        @SomeAnnotation
        void metaAnnotatedMethod() {
        }
    }

    @SuppressWarnings("unused")
    private static class ClassWithMetaAnnotatedConstructor {
        @SomeAnnotation
        ClassWithMetaAnnotatedConstructor() {
        }
    }

    private static class SomeMetaMetaMetaAnnotationClassParameter {
    }

    private enum SomeMetaMetaMetaAnnotationEnumParameter {
        VALUE
    }

    private @interface SomeMetaMetaMetaParameterAnnotation {
        Class<?> value();
    }

    private static class SomeMetaMetaMetaParameterAnnotationClassParameter {
    }
}
