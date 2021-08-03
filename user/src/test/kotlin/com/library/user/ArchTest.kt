package com.library.user

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ArchTest {

    @Test
    fun servicesAndRepositoriesShouldNotDependOnWebLayer() {

        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.library.user")

        noClasses()
            .that()
            .resideInAnyPackage("com.library.user.service..")
            .or()
            .resideInAnyPackage("com.library.user.repository..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..com.library.user.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses)
    }
}
