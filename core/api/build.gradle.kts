import groovy.json.JsonOutput
import groovy.json.JsonSlurper

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

// core/api/openapi.json is the verbatim spec fetched from kluvs-api (full
// paths + components.schemas) — the regenerate-api-models.yml workflow writes
// it as-is, with no filtering, so the committed file always matches the
// canonical contract exactly.
//
// We don't generate directly from it, though. OpenAPI Generator's
// InlineModelResolver walks every path's requestBody/responses and promotes
// any inline (non-$ref) schema it finds into its own auto-named model — e.g.
// "ClubResponse", "InlineResponse200" — regardless of the "models" filter
// below. Since this module is models-only (no API client) and the epic's
// scope boundary is explicitly "named components.schemas, not inline
// envelopes", those promoted models are pure noise (this is exactly what
// produced ~80 unwanted files the first time this was wired up).
//
// The fix: derive a build-only copy of the spec with "paths" zeroed out to
// an empty object before handing it to the generator. It can't be removed
// entirely — OpenAPI's spec validator requires the "paths" key to exist,
// just not contain anything — but an empty object gives InlineModelResolver
// nothing to walk, so nothing gets promoted. This holds automatically as new
// entity schemas are added to components.schemas over time; there's no
// allow/deny list to maintain here.
val schemaOnlySpec = layout.buildDirectory.file("openapi/schema-only.json")

val stripPathsFromSpec by tasks.registering {
    val inputFile = file("openapi.json")
    val outputFile = schemaOnlySpec

    inputs.file(inputFile)
    outputs.file(outputFile)

    doLast {
        @Suppress("UNCHECKED_CAST")
        val spec = JsonSlurper().parse(inputFile) as MutableMap<String, Any?>
        spec["paths"] = emptyMap<String, Any?>()
        val target = outputFile.get().asFile
        target.parentFile.mkdirs()
        target.writeText(JsonOutput.toJson(spec))
    }
}

// openApiGenerate { } below only configures the task's settings — it does not
// register task dependencies, so stripPathsFromSpec must be wired in here.
tasks.named("openApiGenerate") {
    dependsOn(stripPathsFromSpec)
}

val generatedModelPackagePath = "com/ivangarzab/kluvs/api/models"

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
    inputSpec.set(schemaOnlySpec.map { it.asFile.path })
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.path)
    packageName.set("com.ivangarzab.kluvs.api")
    modelPackage.set("com.ivangarzab.kluvs.api.models")
    globalProperties.set(
        mapOf(
            // "" means "generate every model the generator can see" — safe now
            // that paths are stripped, since the only models left are the
            // named components.schemas entities.
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
// present in the source — so a model renamed/removed from components.schemas
// gets its old .kt file cleaned up automatically, scoped only to this one
// folder. This is the task to actually run after a spec update; it pulls in
// openApiGenerate as a dependency so a single invocation does both steps.
val syncGeneratedApiModels by tasks.registering(Sync::class) {
    dependsOn(tasks.named("openApiGenerate"))
    from(layout.buildDirectory.dir("generated/openapi/src/commonMain/kotlin/$generatedModelPackagePath"))
    into("src/commonMain/kotlin/$generatedModelPackagePath")
}

tasks.named("openApiGenerate") {
    finalizedBy(syncGeneratedApiModels)
}
