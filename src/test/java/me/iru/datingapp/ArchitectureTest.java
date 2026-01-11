package me.iru.datingapp;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@DisplayName("Architecture Tests")
class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("me.iru.datingapp");
    }

    // ========== Annotation Tests ==========

    @Test
    @DisplayName("Controllers should be annotated with @RestController or @Controller")
    void testControllersShouldBeAnnotatedWithRestControllerOrController() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..controller..")
                .and().haveSimpleNameEndingWith("Controller")
                .should().beAnnotatedWith(RestController.class)
                .orShould().beAnnotatedWith(Controller.class)
                .because("All controller classes should be properly annotated for Spring MVC");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Services should be annotated with @Service")
    void testServicesShouldBeAnnotatedWithService() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..service..")
                .and().haveSimpleNameEndingWith("Service")
                .should().beAnnotatedWith(Service.class)
                .because("All service classes should be Spring-managed beans");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repositories should be interfaces")
    void testRepositoriesShouldBeInterfaces() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..repository..")
                .and().haveSimpleNameEndingWith("Repository")
                .should().beInterfaces()
                .because("JPA repositories should be interfaces extending JpaRepository");

        rule.check(importedClasses);
    }

    // ========== Package Organization Tests ==========

    @Test
    @DisplayName("Entities should be in entity package")
    void testEntitiesShouldBeInEntityPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideInAPackage("..entity..")
                .because("Entity classes should be organized in the entity package");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("DTOs should be in dto package")
    void testDtosShouldBeInDtoPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Dto")
                .should().resideInAPackage("..dto..")
                .because("DTOs should be organized in the dto package");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Mappers should be in mapper package")
    void testMappersShouldBeInMapperPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Mapper")
                .and().areNotMemberClasses()
                .should().resideInAPackage("..mapper..")
                .because("Mapper classes should be organized in the mapper package");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Exceptions should be in exception package")
    void testExceptionsShouldBeInExceptionPackage() {
        ArchRule rule = classes()
                .that().areAssignableTo(Exception.class)
                .and().resideInAPackage("me.iru.datingapp..")
                .should().resideInAPackage("..exception..")
                .because("Custom exceptions should be organized in the exception package");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Configuration classes should be in config package")
    void testConfigurationClassesShouldBeInConfigPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.context.annotation.Configuration")
                .should().resideInAPackage("..config..")
                .because("Configuration classes should be organized in the config package");

        rule.check(importedClasses);
    }

    // ========== Dependency Rules ==========

    @Test
    @DisplayName("Services should not depend on Controllers")
    void testServicesShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..service..")
                .should().dependOnClassesThat().resideInAnyPackage("..controller..")
                .because("Services should not have dependencies on the controller layer");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controllers can only access Services")
    void testControllersCanOnlyAccessServices() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..controller..")
                .should().onlyAccessClassesThat()
                .resideInAnyPackage(
                        "..controller..",
                        "..service..",
                        "..dto..",
                        "..entity..",
                        "..exception..",
                        "java..",
                        "org.springframework..",
                        "org.slf4j..",
                        "io.swagger..",
                        "jakarta..",
                        "lombok.."
                )
                .because("Controllers should only interact with services and DTOs, not repositories directly");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repositories should only be accessed by Services or DAOs")
    void testRepositoriesShouldOnlyBeAccessedByServices() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..repository..")
                .should().onlyBeAccessed().byAnyPackage(
                        "..service..",
                        "..dao..",
                        "..repository.."
                )
                .because("Repositories should only be injected into service layer");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Entity classes should not depend on Service layer")
    void testEntitiesShouldNotDependOnServices() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..entity..")
                .should().dependOnClassesThat().resideInAnyPackage("..service..")
                .because("Entities should be plain data objects without business logic dependencies");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("DTO classes should not depend on Service layer")
    void testDtosShouldNotDependOnServices() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..dto..")
                .should().dependOnClassesThat().resideInAnyPackage("..service..")
                .because("DTOs should be simple data transfer objects");

        rule.check(importedClasses);
    }

    // ========== Cyclic Dependencies ==========

    @Test
    @DisplayName("No cyclic dependencies between packages")
    void testNoCyclicDependencies() {
        slices().matching("me.iru.datingapp.(*)..")
                .should().beFreeOfCycles()
                .because("Cyclic dependencies make the codebase hard to maintain")
                .check(importedClasses);
    }

    // ========== Layered Architecture ==========

    @Test
    @DisplayName("Layered architecture should be respected")
    void testLayeredArchitectureShouldBeRespected() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controllers").definedBy("..controller..")
                .layer("Services").definedBy("..service..")
                .layer("Repositories").definedBy("..repository..")
                .layer("DAOs").definedBy("..dao..")
                .layer("Entities").definedBy("..entity..")
                .layer("DTOs").definedBy("..dto..")

                .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
                .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers", "Services")
                .whereLayer("Repositories").mayOnlyBeAccessedByLayers("Services", "DAOs")
                .whereLayer("DAOs").mayOnlyBeAccessedByLayers("Services")

                .because("The layered architecture should be respected to maintain separation of concerns")
                .check(importedClasses);
    }

    // ========== Naming Conventions ==========

    @Test
    @DisplayName("Naming convention: Classes in service package should end with 'Service'")
    void testServiceNamingConvention() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("Service")
                .because("Service classes should follow consistent naming conventions");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Naming convention: Classes in controller package should end with 'Controller'")
    void testControllerNamingConvention() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..controller..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("Controller")
                .because("Controller classes should follow consistent naming conventions");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Naming convention: Classes in repository package should end with 'Repository'")
    void testRepositoryNamingConvention() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .should().haveSimpleNameEndingWith("Repository")
                .because("Repository interfaces should follow consistent naming conventions");

        rule.check(importedClasses);
    }
}

