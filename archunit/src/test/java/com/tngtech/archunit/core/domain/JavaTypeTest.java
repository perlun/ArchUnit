package com.tngtech.archunit.core.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

import static com.tngtech.archunit.testutil.Assertions.assertThatType;
import static org.assertj.core.api.Assertions.assertThat;

public class JavaTypeTest {

    @Test
    public void erased_non_generic_class_is_the_class_itself() {
        class SimpleClass {
        }

        JavaType type = new ClassFileImporter().importClass(SimpleClass.class);

        assertThat(type.toErasure()).isEqualTo(type);
    }

    @Test
    public void erased_unbound_type_variable_is_java_lang_Object() {
        @SuppressWarnings("unused")
        class ClassWithUnboundTypeParameter<T> {
        }

        JavaType type = new ClassFileImporter().importClass(ClassWithUnboundTypeParameter.class).getTypeParameters().get(0);

        assertThatType(type.toErasure()).matches(Object.class);
    }

    @Test
    public void erased_type_variable_bound_by_single_class_is_this_class() {
        @SuppressWarnings("unused")
        class ClassWithBoundTypeParameterWithSingleClassBound<T extends Serializable> {
        }

        JavaType type = new ClassFileImporter().importClass(ClassWithBoundTypeParameterWithSingleClassBound.class).getTypeParameters().get(0);

        assertThatType(type.toErasure()).matches(Serializable.class);
    }

    @Test
    public void erased_type_variable_bound_by_single_generic_class_is_the_erasure_of_this_class() {
        @SuppressWarnings("unused")
        class ClassWithBoundTypeParameterWithSingleGenericClassBound<T extends List<String>> {
        }

        JavaType type = new ClassFileImporter().importClass(ClassWithBoundTypeParameterWithSingleGenericClassBound.class).getTypeParameters().get(0);

        assertThatType(type.toErasure()).matches(List.class);
    }

    @Test
    public void erased_type_variable_bound_by_multiple_generic_classes_and_interfaces_is_the_erasure_of_the_leftmost_bound() {
        @SuppressWarnings("unused")
        class ClassWithBoundTypeParameterWithMultipleGenericClassAndInterfaceBounds<T extends HashMap<String, String> & Iterable<String> & Serializable> {
        }

        JavaType type = new ClassFileImporter().importClass(ClassWithBoundTypeParameterWithMultipleGenericClassAndInterfaceBounds.class).getTypeParameters().get(0);

        assertThatType(type.toErasure()).matches(HashMap.class);
    }
}
