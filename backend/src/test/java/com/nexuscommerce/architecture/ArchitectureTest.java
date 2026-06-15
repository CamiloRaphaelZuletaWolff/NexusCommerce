package com.nexuscommerce.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;

/** Enforces the modular-monolith package boundaries (CLAUDE.md conventions). */
@AnalyzeClasses(packages = "com.nexuscommerce", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule constructor_injection_only =
            fields().should().notBeAnnotatedWith(Autowired.class)
                    .because("constructor injection keeps dependencies explicit and testable");

    @ArchTest
    static final ArchRule controllers_live_in_api_packages =
            classes().that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..api..");

    @ArchTest
    static final ArchRule controllers_do_not_use_repositories =
            noClasses().that().resideInAPackage("..api..")
                    .should().dependOnClassesThat().areAssignableTo(Repository.class)
                    .because("controllers must go through services, never repositories directly");

    @ArchTest
    static final ArchRule repositories_are_named_repository =
            classes().that().areAssignableTo(Repository.class)
                    .should().haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule services_do_not_depend_on_controllers =
            noClasses().that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule domain_does_not_depend_on_outer_layers =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("..api..", "..service..", "..repository..")
                    .because("domain entities must not depend on web, service or persistence layers");
}
