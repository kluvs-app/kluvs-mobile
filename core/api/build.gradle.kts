plugins {
    id("kluvs.kmp.library")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.openapiGenerator)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Generated models are annotated @Serializable / @SerialName (see
            // configOptions below), so this module needs kotlinx-serialization
            // on its own compile classpath, same as :core:model and :core:network.
            implementation(libs.kotlinx.serialization)
        }
    }
}

val generatedModelPackagePath = "com/ivangarzab/kluvs/api/models"

// Generates from the full spec (paths included), not just components.schemas.
// Request/response envelopes that are only ever defined inline under "paths"
// (e.g. Server's GET oneOf, Session's PUT response variants) get auto-named
// models too (e.g. ServerGet200ResponseOneOfDto) — uglier names than the
// named entity schemas, but verified field-for-field accurate against the
// real spec, and the only way to get those envelope shapes without backend
// changes to promote them to named schemas. Consume the specific per-branch
// class relevant to each known call site (services already know which
// branch applies); don't rely on the ambiguous merged oneOf parent class.
//
// The "kotlin"/"multiplatform" library always writes to
// <outputDir>/src/commonMain/kotlin/<package>/... — it can't be pointed
// directly at the final models folder without doubling that path. So we
// generate into a throwaway build/ directory here, then sync just the model
// files into source control via the syncGeneratedApiModels task below.
// Generating straight into committed source previously meant any cleanup
// step (e.g. cleanupOutput) was scoped to the whole module root and could
// wipe build.gradle.kts / openapi.json alongside it — this keeps destructive
// operations confined to build/, never the committed tree directly.
openApiGenerate {
    generatorName.set("kotlin")
    library.set("multiplatform")
    inputSpec.set("$projectDir/openapi.json")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.path)
    // Safe to wipe wholesale on every run now that outputDir is a throwaway
    // build/ directory, not the module root.
    cleanupOutput.set(true)
    packageName.set("com.ivangarzab.kluvs.api")
    modelPackage.set("com.ivangarzab.kluvs.api.models")
    // Suffixes every generated class with "Dto" (Book -> BookDto, Club -> ClubDto, ...),
    // matching the existing hand-written wire-DTO naming convention. This also means
    // generated types never collide with same-named domain models in :core:model, so
    // mapper files can import both without aliasing.
    modelNameSuffix.set("Dto")
    globalProperties.set(
        mapOf(
            "models" to "",
            "apis" to "false",
            "modelDocs" to "false",
            "modelTests" to "false"
        )
    )
    configOptions.set(
        mapOf(
            "serializationLibrary" to "kotlinx_serialization",
            // Only "string" or "kotlinx-datetime" are valid for the multiplatform
            // library. We want raw strings — date parsing already lives in the
            // :core:data mapper layer (DateParsingUtils) and stays there.
            "dateLibrary" to "string"
        )
    )
}

// Copies generated models from build/ into the committed source tree. Sync
// (unlike Copy) deletes anything already in the destination that's no longer
// present in the source — so a model renamed/removed from the spec gets its
// old .kt file cleaned up automatically, scoped only to this one folder.
// This is the task to actually run after a spec update; it pulls in
// openApiGenerate as a dependency so a single invocation does both steps.
val syncGeneratedApiModels by tasks.registering(Sync::class) {
    dependsOn(tasks.named("openApiGenerate"))
    from(layout.buildDirectory.dir("generated/openapi/src/commonMain/kotlin/$generatedModelPackagePath"))
    into("src/commonMain/kotlin/$generatedModelPackagePath")
}

tasks.named("openApiGenerate") {
    finalizedBy(syncGeneratedApiModels)
}
