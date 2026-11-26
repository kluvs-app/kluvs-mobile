import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

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

// Read only by the tryApiSpec task. Empty = the saved openapi.json; or a kluvs-api
// branch or commit SHA (e.g. "main", "fix/some-branch") to try that spec.
val apiSpecRef = ""

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
            // Don't set "serializationLibrary" to "kotlinx_serialization" here:
            // library = "multiplatform" already implies kotlinx serialization in the
            // generator's own template, so setting it explicitly on top causes the
            // template to emit @Serializable twice on every generated class.
            //
            // Only "string" or "kotlinx-datetime" are valid for the multiplatform
            // library. We want raw strings — date parsing already lives in the
            // :core:data mapper layer (DateParsingUtils) and stays there.
            "dateLibrary" to "string"
        )
    )
    // Unknown enum values: adds an unknown_default_open_api case to every generated
    // enum, and (via the templates below) a serializer that decodes any unrecognized
    // wire value to it instead of throwing, so shipped app versions survive enum values
    // the API adds later. Mappers in :core:data turn that case into a safe domain value.
    // It must go in additionalProperties: the plugin only forwards configOptions keys
    // the generator lists as CLI options, and the Kotlin generator clears that list,
    // so "enumUnknownDefaultCase" in configOptions is silently dropped.
    additionalProperties.set(
        mapOf(
            "enumUnknownDefaultCase" to "true"
        )
    )
    // Upstream's multiplatform templates add the unknown case but no fallback
    // serializer (only the jvm kotlinx_serialization branch has one). The copies in
    // templates/ add it; see the header comment in each file.
    templateDir.set("$projectDir/templates")
    // Free-form objects (schemas with `additionalProperties: true`, e.g. Member's
    // platform_metadata) default to `kotlin.collections.Map<String, kotlin.Any>`, but
    // kotlinx.serialization has no serializer for bare `Any` — that fails to compile.
    //
    // `typeMappings` can't fix this: the Kotlin generator's getPrimitiveType()
    // hardcodes a literal "object" return for free-form schemas, bypassing the
    // typeMapping table entirely (confirmed by decompiling openapi-generator's
    // DefaultCodegen — there's no map lookup on that code path, just a hardcoded
    // string literal). The only lever that's checked *before* that hardcoded
    // branch is schemaMapping, but only for $ref'd schemas — which is why
    // platform_metadata's properties were changed to `$ref: '#/components/schemas/JsonObject'`
    // in the spec (a placeholder schema that only exists as a schemaMapping target)
    // instead of leaving them as inline `additionalProperties: true`.
    //
    // Mapped to JsonObject (not Map<String, String>) to preserve the real,
    // arbitrary-JSON contract — collapsing values to String would misrepresent
    // the API and break parsing the moment a non-string value shows up.
    schemaMappings.set(
        mapOf(
            "JsonObject" to "kotlinx.serialization.json.JsonObject"
        )
    )
    importMappings.set(
        mapOf(
            "JsonObject" to "kotlinx.serialization.json.JsonObject"
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

// tryApiSpec: generates the committed models from the spec that apiSpecRef points at.
// It has its own generate task so that openApiGenerate (and the regen workflow, which
// runs it) never reads apiSpecRef.
val generateTryApiSpecModels by tasks.registering(GenerateTask::class) {
    // Same generator settings as openApiGenerate. Add any new setting from that block here too.
    val settings = openApiGenerate
    generatorName.set(settings.generatorName)
    library.set(settings.library)
    cleanupOutput.set(settings.cleanupOutput)
    packageName.set(settings.packageName)
    modelPackage.set(settings.modelPackage)
    modelNameSuffix.set(settings.modelNameSuffix)
    globalProperties.set(settings.globalProperties)
    configOptions.set(settings.configOptions)
    additionalProperties.set(settings.additionalProperties)
    templateDir.set(settings.templateDir)
    schemaMappings.set(settings.schemaMappings)
    importMappings.set(settings.importMappings)
    outputDir.set(layout.buildDirectory.dir("generated/openapi-try").get().asFile.path)

    val ref = apiSpecRef.trim()
    val message = if (ref.isEmpty()) {
        inputSpec.set(settings.inputSpec)
        "Generating API models from the saved core/api/openapi.json."
    } else {
        val url = "https://raw.githubusercontent.com/kluvs-app/kluvs-api/$ref/openapi.json"
        remoteInputSpec.set(url)
        "WARNING: generating API models from $url. Don't commit them; " +
            "set apiSpecRef back to \"\" and run tryApiSpec again to reset."
    }

    // A branch can move while its URL stays the same, so always generate again.
    outputs.upToDateWhen { false }
    outputs.cacheIf { false }
    // quiet level, so the message still shows with --quiet.
    doFirst { logger.quiet(message) }
}

val tryApiSpec by tasks.registering(Sync::class) {
    group = "api"
    description = "Generates the API models from the kluvs-api ref in apiSpecRef (empty = saved spec)."
    dependsOn(generateTryApiSpecModels)
    from(layout.buildDirectory.dir("generated/openapi-try/src/commonMain/kotlin/$generatedModelPackagePath"))
    into("src/commonMain/kotlin/$generatedModelPackagePath")
}
