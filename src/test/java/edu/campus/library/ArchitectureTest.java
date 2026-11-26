package edu.campus.library;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/** Layer rules (ADR-0001). Dependencies point inward: ui/infra -> service -> domain. */
@AnalyzeClasses(packages = "edu.campus.library", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule layers = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .withOptionalLayers(true) // layers may still be empty early in the project (M1)
            .layer("App").definedBy("edu.campus.library")
            .layer("UI").definedBy("edu.campus.library.ui..")
            .layer("Infra").definedBy("edu.campus.library.infra..")
            .layer("Service").definedBy("edu.campus.library.service..")
            .layer("Domain").definedBy("edu.campus.library.domain..")
            .whereLayer("App").mayNotBeAccessedByAnyLayer()
            .whereLayer("UI").mayOnlyBeAccessedByLayers("App")
            .whereLayer("Infra").mayOnlyBeAccessedByLayers("App")
            .whereLayer("Service").mayOnlyBeAccessedByLayers("App", "UI", "Infra")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("App", "UI", "Infra", "Service");

    @ArchTest
    static final ArchRule domainIsFreeOfIo = noClasses()
            .that().resideInAPackage("edu.campus.library.domain..")
            .should().dependOnClassesThat().resideInAnyPackage("java.nio.file..", "java.sql..")
            .because("the domain must not know about storage")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule uiUsesOnlyTheFacade = noClasses()
            .that().resideInAPackage("edu.campus.library.ui..")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
            .because("the UI talks to LibraryFacade, not to storage")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule noCycles = slices()
            .matching("edu.campus.library.(*)..")
            .should().beFreeOfCycles()
            .allowEmptyShould(true);
}
