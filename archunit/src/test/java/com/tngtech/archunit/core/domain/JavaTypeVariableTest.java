package com.tngtech.archunit.core.domain;

import java.io.Serializable;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

import static com.tngtech.archunit.testutil.Assertions.assertThat;

public class JavaTypeVariableTest {
    @Test
    public void toString_unbounded() {
        JavaTypeVariable typeVariable = new ClassFileImporter().importClass(Unbounded.class).getTypeParameters().get(0);

        assertThat(typeVariable.toString())
                .contains(JavaTypeVariable.class.getSimpleName())
                .contains("NAME")
                .doesNotContain("extends");
    }

    @Test
    public void toString_upper_bounded_by_single_bound() {
        JavaTypeVariable typeVariable = new ClassFileImporter().importClass(BoundedBySingleBound.class).getTypeParameters().get(0);

        assertThat(typeVariable.toString())
                .contains(JavaTypeVariable.class.getSimpleName())
                .contains("NAME extends java.lang.String");
    }

    @Test
    public void toString_upper_bounded_by_multiple_bounds() {
        JavaTypeVariable typeVariable = new ClassFileImporter().importClass(BoundedByMultipleBounds.class).getTypeParameters().get(0);

        assertThat(typeVariable.toString())
                .contains(JavaTypeVariable.class.getSimpleName())
                .contains("NAME extends java.lang.String & java.io.Serializable");
    }

    @SuppressWarnings("unused")
    private static class Unbounded<NAME> {
    }

    @SuppressWarnings("unused")
    private static class BoundedBySingleBound<NAME extends String> {
    }

    @SuppressWarnings("unused")
    private static class BoundedByMultipleBounds<NAME extends String & Serializable> {
    }
}
