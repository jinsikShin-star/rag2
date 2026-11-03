package io.github.ragportfolio.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HexagonalArchitectureTest {
    private static final JavaClasses PRODUCTION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("io.github.ragportfolio");

    @Test void importsAllFourProductionModules() {
        for (String name : new String[]{"io.github.ragportfolio.domain.Actor", "io.github.ragportfolio.application.QuestionService",
                "io.github.ragportfolio.adapters.http.AnswerController", "io.github.ragportfolio.bootstrap.RagApplication"}) {
            assertTrue(PRODUCTION.contain(name), "Missing production class: " + name);
        }
    }

    @Test void domainDependsOnlyOnJdkAndItself() {
        classes().that().resideInAPackage("..domain..").should().onlyDependOnClassesThat()
                .resideInAnyPackage("java..", "io.github.ragportfolio.domain..").check(PRODUCTION);
    }

    @Test void applicationDependsOnlyOnDomainAndJdk() {
        classes().that().resideInAPackage("..application..").should().onlyDependOnClassesThat()
                .resideInAnyPackage("java..", "io.github.ragportfolio.application..", "io.github.ragportfolio.domain..")
                .check(PRODUCTION);
    }

    @Test void adaptersCannotDependOnBootstrap() {
        noClasses().that().resideInAPackage("..adapters..").should().dependOnClassesThat()
                .resideInAPackage("..bootstrap..").check(PRODUCTION);
    }

    @Test void productionModulesHaveNoDependencyCycles() {
        slices().matching("io.github.ragportfolio.(*)..").should().beFreeOfCycles().check(PRODUCTION);
    }
}
