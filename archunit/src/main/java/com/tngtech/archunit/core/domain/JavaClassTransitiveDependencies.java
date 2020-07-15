/*
 * Copyright 2014-2020 TNG Technology Consulting GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tngtech.archunit.core.domain;

import com.tngtech.archunit.base.Predicate;

import java.util.HashSet;
import java.util.Set;

class JavaClassTransitiveDependencies {
    private JavaClassTransitiveDependencies() {
    }

    static Set<Dependency> findTransitiveDependenciesFrom(JavaClass javaClass, Predicate<JavaClass> shouldRecurse) {
        Set<Dependency> transitiveDependencies = new HashSet<>();
        Set<JavaClass> analyzedClasses = new HashSet<>();  // to avoid infinite recursion for cyclic dependencies
        addTransitiveDependenciesFrom(javaClass, shouldRecurse, transitiveDependencies, analyzedClasses);
        return transitiveDependencies;
    }

    private static void addTransitiveDependenciesFrom(
            JavaClass javaClass,
            Predicate<JavaClass> shouldRecurse,
            Set<Dependency> transitiveDependencies,
            Set<JavaClass> analyzedClasses) {

        analyzedClasses.add(javaClass);  // currently being analyzed
        Set<JavaClass> targetClassesToRecurse = new HashSet<>();
        for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
            transitiveDependencies.add(dependency);
            JavaClass targetClass = dependency.getTargetClass();
            JavaClass targetClassToRecurse = targetClass.isArray() ? targetClass.getComponentType() : targetClass;
            if (shouldRecurse.apply(targetClassToRecurse)) {
                targetClassesToRecurse.add(targetClassToRecurse);
            }
        }
        for (JavaClass targetClass : targetClassesToRecurse) {
            if (!analyzedClasses.contains(targetClass)) {
                addTransitiveDependenciesFrom(targetClass, shouldRecurse, transitiveDependencies, analyzedClasses);
            }
        }
    }
}
