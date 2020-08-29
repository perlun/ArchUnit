package com.tngtech.archunit.core.domain;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

import static com.tngtech.archunit.testutil.Assertions.assertThat;

// FIXME: How classes are resolved depending on the configured ClassResolver is part of the import context.
//        Thus this test should actually go into the importer package, not the domain package.
//        To match other open PRs I propose the name `ClassFileImporterAnnotationsTest`
//        and the location next to `ClassFileImporterTest`
public class TransitiveAnnotationsTest {

    @Test
    public void meta_annotations_are_imported() {
        JavaClass javaClass = new ClassFileImporter().importClass(BaseClass.class);
        JavaAnnotation<JavaClass> someAnnotation = javaClass.getAnnotationOfType(SomeAnnotation.class.getName());
        JavaAnnotation<JavaClass> someMetaAnnotation = someAnnotation.getRawType()
                .getAnnotationOfType(SomeMetaAnnotation.class.getName());
        JavaAnnotation<JavaClass> someMetaMetaAnnotation = someMetaAnnotation.getRawType()
                .getAnnotationOfType(SomeMetaMetaAnnotation.class.getName());

        assertThat(someMetaMetaAnnotation.getRawType()).matches(SomeMetaMetaAnnotation.class);
    }

    @Test
    public void parameters_of_meta_annotation_are_imported() {
        JavaClass javaClass = new ClassFileImporter().importClass(BaseClass.class);
        JavaAnnotation<JavaClass> someAnnotation = javaClass.getAnnotationOfType(SomeAnnotation.class.getName());
        JavaAnnotation<JavaClass> metaAnnotationWithParameters = someAnnotation.getRawType()
                .getAnnotationOfType(MetaAnnotationWithParameters.class.getName());

        assertThatParametersOfMetaAnnotationAreImported(metaAnnotationWithParameters);
    }

    @Test
    public void meta_annotations_of_class_members_are_imported() {
        JavaClass javaClass = new ClassFileImporter().importClass(ClassWithMetaAnnotatedMember.class);
        JavaField metaAnnotatedMember = javaClass.getField("metaAnnotatedMember");
        JavaAnnotation<JavaField> someAnnotation = metaAnnotatedMember
                .getAnnotationOfType(SomeAnnotation.class.getName());
        JavaAnnotation<JavaClass> metaAnnotationWithParameters = someAnnotation.getRawType()
                .getAnnotationOfType(MetaAnnotationWithParameters.class.getName());

        assertThatParametersOfMetaAnnotationAreImported(metaAnnotationWithParameters);
    }

    @SuppressWarnings("unchecked")
    private void assertThatParametersOfMetaAnnotationAreImported(final JavaAnnotation<JavaClass> metaAnnotationWithParameters) {
        JavaEnumConstant someEnum = (JavaEnumConstant) metaAnnotationWithParameters.get("someEnum").get();
        assertThat(someEnum.name()).isEqualTo(SomeEnum.CONSTANT.toString());
        assertThat(someEnum.getDeclaringClass()).matches(SomeEnum.class);

        JavaAnnotation<JavaClass> parameterAnnotation = (JavaAnnotation<JavaClass>) metaAnnotationWithParameters
                .get("parameterAnnotation").get();

        assertThat(parameterAnnotation.getRawType()).matches(ParameterAnnotation.class);
        JavaClass someClass = (JavaClass) parameterAnnotation.get("value").get();
        assertThat(someClass).matches(SomeClass.class);
    }

    private @interface MetaAnnotationWithParameters {
        SomeEnum someEnum();

        ParameterAnnotation parameterAnnotation();
    }

    private @interface SomeMetaMetaAnnotation {
    }

    @SomeMetaMetaAnnotation
    private @interface SomeMetaAnnotation {
    }

    @MetaAnnotationWithParameters(
            someEnum = SomeEnum.CONSTANT,
            parameterAnnotation = @ParameterAnnotation(SomeClass.class)
    )
    @SomeMetaAnnotation
    private @interface SomeAnnotation {
    }

    @SomeAnnotation
    private @interface BaseClass {
    }

    private enum SomeEnum {
        CONSTANT,
        VARIABLE
    }

    private @interface ParameterAnnotation {
        Class<?> value();
    }

    private static class SomeClass {
    }

    @SuppressWarnings("unused")
    private static class ClassWithMetaAnnotatedMember {
        @SomeAnnotation
        int metaAnnotatedMember;
    }
}
