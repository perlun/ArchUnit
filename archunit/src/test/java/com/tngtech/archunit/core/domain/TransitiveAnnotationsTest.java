package com.tngtech.archunit.core.domain;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

import static com.tngtech.archunit.core.domain.TestUtils.importClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

public class TransitiveAnnotationsTest {

    @Test
    public void meta_annotations_are_imported() {
        JavaClass javaClass = new ClassFileImporter().importClass(BaseClass.class);
        JavaAnnotation<JavaClass> someAnnotation = javaClass.getAnnotationOfType(SomeAnnotation.class.getName());
        JavaAnnotation<JavaClass> someMetaAnnotation = someAnnotation.getRawType()
                .getAnnotationOfType(SomeMetaAnnotation.class.getName());
        JavaAnnotation<JavaClass> someMetaMetaAnnotation = someMetaAnnotation.getRawType()
                .getAnnotationOfType(SomeMetaMetaAnnotation.class.getName());
    }

    @Test
    public void meta_annotations_are_correctly_verified() {
        JavaClasses javaClasses = importClasses(BaseClass.class);
        classes().should()
                .beMetaAnnotatedWith(SomeMetaAnnotation.class)
                .andShould()
                .beMetaAnnotatedWith(SomeMetaMetaAnnotation.class)
                .check(javaClasses);
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
        JavaAnnotation<JavaField> someAnnotation =  metaAnnotatedMember
                .getAnnotationOfType(SomeAnnotation.class.getName());
        JavaAnnotation<JavaClass> metaAnnotationWithParameters = someAnnotation.getRawType()
                .getAnnotationOfType(MetaAnnotationWithParameters.class.getName());

        assertThatParametersOfMetaAnnotationAreImported(metaAnnotationWithParameters);
    }

    private void assertThatParametersOfMetaAnnotationAreImported(final JavaAnnotation<JavaClass> metaAnnotationWithParameters) {
        assertThat(metaAnnotationWithParameters.get("someEnum").isPresent()).isTrue();
        JavaEnumConstant someEnum = (JavaEnumConstant) metaAnnotationWithParameters.get("someEnum").get();
        assertThat(someEnum.name()).isEqualTo(SomeEnum.CONSTANT.toString());

        assertThat(metaAnnotationWithParameters.get("parameterAnnotation").isPresent()).isTrue();
        JavaAnnotation<JavaClass> parameterAnnotation = (JavaAnnotation<JavaClass>) metaAnnotationWithParameters
                .get("parameterAnnotation").get();

        assertThat(parameterAnnotation.get("value").isPresent()).isTrue();
        JavaClass someClass = (JavaClass) parameterAnnotation.get("value").get();
        assertThat(someClass.getName()).isEqualTo(SomeClass.class.getName());
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
    private @interface BaseClass{
    }

    private enum SomeEnum {
        CONSTANT,
        VARIABLE;
    }

    private @interface ParameterAnnotation {
        Class<?> value();
    }

    private class SomeClass {
    }

    private class ClassWithMetaAnnotatedMember {
        @SomeAnnotation
        int metaAnnotatedMember;
    }
}