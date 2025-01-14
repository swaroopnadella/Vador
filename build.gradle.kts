/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

import io.freefair.gradle.plugins.lombok.LombokExtension.LOMBOK_VERSION
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  java
  id(libs.plugins.detekt.pluginId) apply false
  alias(libs.plugins.lombok) apply false
  id("org.sonarqube") version "3.4.0.2513"
}
allprojects {
  apply(plugin = "vador.root-conventions")
}
val detektReportMerge by tasks.registering(ReportMergeTask::class) {
  output.set(rootProject.buildDir.resolve("reports/detekt/merge.xml"))
}
subprojects {
  apply(plugin = "vador.sub-conventions")
  tasks.withType<Detekt>().configureEach {
    ignoreFailures = true
    reports {
      xml.required by true
    }
  }
  plugins.withType<DetektPlugin> {
    tasks.withType<Detekt> detekt@{
      finalizedBy(detektReportMerge)
      detektReportMerge.configure {
        input.from(this@detekt.xmlReportFile)
      }
    }
  }
  val lombokForSonarQube: Configuration by configurations.creating
  dependencies {
    lombokForSonarQube("org.projectlombok:lombok:$LOMBOK_VERSION")
  }
  sonarqube {
    properties {
      property("sonar.projectName", name)
      property("sonar.sources", "src/main")
      property("sonar.tests", "src/test")
      property("sonar.java.libraries", lombokForSonarQube.files.last().toString())
      property("sonar.java.binaries", "build/classes")
    }
  }
}
sonarqube {
  properties {
    property("sonar.modules", subprojects.joinToString(",") { it.name })
    property("sonar.coverage.jacoco.xmlReportPaths", rootProject.buildDir.resolve("/build/reports/kover/report.xml"))
    property("detekt.sonar.kotlin.config.path", rootProject.buildDir.resolve("/detekt/detekt.yml"))
    property("sonar.kotlin.detekt.reportPaths", rootProject.buildDir.resolve("/build/reports/detekt/merge.xml"))
  }
}
afterEvaluate {
  tasks {
    check.configure {
      dependsOn(detektReportMerge)
    }
    sonarqube.configure { dependsOn(check) }
  }
}
