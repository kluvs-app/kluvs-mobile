# :core:api

Generated OpenAPI wire models for the Kluvs API.

## Purpose

100% generated Kotlin `@Serializable` DTOs (`com.ivangarzab.kluvs.api.models`, every class
suffixed `Dto`) built from the canonical spec in `kluvs-api`. **Never hand-edit these files** —
fix the spec upstream instead.

## How it's generated

- `openapi.json` in this module is the input spec (fetched, never hand-written).
- `openApiGenerate` (openapi-generator, `kotlin`/`multiplatform`) writes to `build/generated/openapi`,
  then `syncGeneratedApiModels` syncs the models into `src/commonMain/.../api/models`
  (removing models that left the spec). Run `:core:api:openApiGenerate` to do both.
- In practice this runs in CI: `.github/workflows/regenerate-api-models.yml` fetches the spec from
  `kluvs-api` on an `api-spec-updated` dispatch, regenerates, and opens a PR against `develop`.

## Dependencies

- No `project(...)` dependencies (only `kotlinx-serialization`).
- Consumed by `:core:data`, whose services and mappers translate these DTOs into `:core:model` types.
  Feature modules never see DTOs.
