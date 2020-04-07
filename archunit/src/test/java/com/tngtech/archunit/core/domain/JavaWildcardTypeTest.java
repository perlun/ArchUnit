package com.tngtech.archunit.core.domain;

import java.util.List;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

import static com.tngtech.archunit.testutil.Assertions.assertThat;

public class JavaWildcardTypeTest {
    @Test
    public void toString_unbounded() {
        JavaWildcardType wildcardType = importWildcardTypeOf(Unbounded.class);

        assertThat(wildcardType.toString())
                .contains(JavaWildcardType.class.getSimpleName())
                .contains("?")
                .doesNotContain("extends")
                .doesNotContain("super");
    }

    @Test
    public void toString_upper_bounded() {
        JavaWildcardType wildcardType = importWildcardTypeOf(UpperBounded.class);

        assertThat(wildcardType.toString())
                .contains(JavaWildcardType.class.getSimpleName())
                .contains("? extends java.lang.String")
                .doesNotContain("super");
    }

    @Test
    public void toString_lower_bounded() {
        JavaWildcardType wildcardType = importWildcardTypeOf(LowerBounded.class);

        assertThat(wildcardType.toString())
                .contains(JavaWildcardType.class.getSimpleName())
                .contains("? super java.lang.String")
                .doesNotContain("extends");
    }

    private JavaWildcardType importWildcardTypeOf(Class<?> clazz) {
        JavaType listType = new ClassFileImporter().importClass(clazz).getTypeParameters().get(0)
                .getUpperBounds().get(0);
        JavaType wildcardType = ((JavaParameterizedType) listType).getActualTypeArguments().get(0);
        return (JavaWildcardType) wildcardType;
    }

    @SuppressWarnings("unused")
    private static class Unbounded<T extends List<?>> {
    }

    @SuppressWarnings("unused")
    private static class UpperBounded<T extends List<? extends String>> {
    }

    @SuppressWarnings("unused")
    private static class LowerBounded<T extends List<? super String>> {
    }
}
