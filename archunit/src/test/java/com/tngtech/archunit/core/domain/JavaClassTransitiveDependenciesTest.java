package com.tngtech.archunit.core.domain;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

import static com.tngtech.archunit.testutil.Assertions.assertThatDependencies;

public class JavaClassTransitiveDependenciesTest {
    private static final String PACKAGE = JavaClassTransitiveDependenciesTest.class.getPackage().getName();

    static class AcyclicGraph {
        static class A {
            B b;
            C c;
        }

        static class B {
            Integer i;
        }

        static class C {
            D d;
        }

        static class D {
            String s;
        }
    }

    @Test
    public void findsTransitiveDependenciesInAcyclicGraph() {
        Class<?> a = AcyclicGraph.A.class;
        Class<?> b = AcyclicGraph.B.class;
        Class<?> c = AcyclicGraph.C.class;
        Class<?> d = AcyclicGraph.D.class;
        JavaClasses classes = new ClassFileImporter().importClasses(a, b, c, d);

        assertThatDependencies(classes.get(a).getTransitiveDependenciesFromSelfWithinPackage(PACKAGE))
                .contain(a, Object.class)
                .contain(a, b)
                    .contain(b, Object.class)
                    .contain(b, Integer.class)
                .contain(a, c)
                    .contain(c, Object.class)
                    .contain(c, d)
                        .contain(d, Object.class)
                        .contain(d, String.class)
                .containOnlyTargetClasses(b, c, d, Object.class, Integer.class, String.class);

        assertThatDependencies(classes.get(b).getTransitiveDependenciesFromSelfWithinPackage(PACKAGE))
                .contain(b, Object.class)
                .contain(b, Integer.class)
                .containOnlyTargetClasses(Object.class, Integer.class);

        assertThatDependencies(classes.get(c).getTransitiveDependenciesFromSelfWithinPackage(PACKAGE))
                .contain(c, Object.class)
                .contain(c, d)
                    .contain(d, Object.class)
                    .contain(d, String.class)
                .containOnlyTargetClasses(d, Object.class, String.class);
    }


    static class CyclicGraph {
        static class A {
            B b;
            C c;
            D d;
        }

        static class B {
            Integer i;
        }

        static class C {
            A a;
        }

        static class D {
            E e;
        }

        static class E {
            A a;
            String s;
        }
    }

    @Test
    public void findsTransitiveDependenciesInCyclicGraph() {
        Class<?> a = CyclicGraph.A.class;
        Class<?> b = CyclicGraph.B.class;
        Class<?> c = CyclicGraph.C.class;
        Class<?> d = CyclicGraph.D.class;
        Class<?> e = CyclicGraph.E.class;
        JavaClasses classes = new ClassFileImporter().importClasses(a, b, c, d, e);

        assertThatDependencies(classes.get(a).getTransitiveDependenciesFromSelfWithinPackage(PACKAGE))
                .contain(a, Object.class)
                .contain(a, b)
                    .contain(b, Object.class)
                    .contain(b, Integer.class)
                .contain(a, c)
                    .contain(c, Object.class)
                    .contain(c, a)
                .contain(a, d)
                    .contain(d, Object.class)
                    .contain(d, e)
                        .contain(e, Object.class)
                        .contain(e, a)
                        .contain(e, String.class)
                .containOnlyTargetClasses(a, b, c, d, e, Object.class, Integer.class, String.class);

        assertThatDependencies(classes.get(c).getTransitiveDependenciesFromSelfWithinPackage(PACKAGE))
                .contain(c, Object.class)
                .contain(c, a)
                    .contain(a, Object.class)
                    .contain(a, b)
                        .contain(b, Object.class)
                        .contain(b, Integer.class)
                    .contain(a, c)
                    .contain(a, d)
                        .contain(d, Object.class)
                        .contain(d, e)
                            .contain(e, Object.class)
                            .contain(e, a)
                            .contain(e, String.class)
                .containOnlyTargetClasses(a, b, c, d, e, Object.class, Integer.class, String.class);

        assertThatDependencies(classes.get(d).getTransitiveDependenciesFromSelfWithinPackage(PACKAGE))
                .contain(d, Object.class)
                .contain(d, e)
                    .contain(e, Object.class)
                    .contain(e, a)
                        .contain(a, Object.class)
                        .contain(a, b)
                            .contain(b, Object.class)
                            .contain(b, Integer.class)
                        .contain(a, c)
                            .contain(c, Object.class)
                            .contain(c, a)
                        .contain(a, d)
                    .contain(e, String.class)
                .containOnlyTargetClasses(a, b, c, d, e, Object.class, Integer.class, String.class);
    }
}
