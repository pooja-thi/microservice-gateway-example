package com.library.publication

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ArchTest {

    @Test
    fun servicesAndRepositoriesShouldNotDependOnWebLayer() {

        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.library.publication")

        noClasses()
            .that()
            .resideInAnyPackage("com.library.publication.service..")
            .or()
            .resideInAnyPackage("com.library.publication.repository..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..com.library.publication.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses)
    }
}
