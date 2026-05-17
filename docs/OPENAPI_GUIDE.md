# OpenAPI Guide

This project uses contract-first API development.

Before implementing a controller endpoint, define or update the OpenAPI contract
and keep `docs/API_SPEC.md` aligned with the public API behavior.

## Contract File Shape

When a static OpenAPI file is added, use `openapi.yml` at the project root.

Use this top-level order:

```yaml
openapi: 3.0.3
info:
  title: Property Billing API
  version: 0.1.0
  description: API for apartment and housing monthly fee tracking.

servers:
  - url: http://localhost:8080/api/v1
    description: Local development server.

tags: []
paths: {}
components:
  parameters: {}
  securitySchemes: {}
  schemas: {}
```

This mirrors the reference OpenAPI style used by the existing service examples:
clear tags, path operations, reusable parameters, reusable schemas, and a bearer
authentication scheme.

## Path Style

Put `/api/v1` in `servers.url`.

Paths must be relative to that server URL:

```yaml
paths:
  /properties:
    get:
      summary: List properties.
```

Do not write paths like this in `openapi.yml`:

```yaml
paths:
  /api/v1/properties:
    get:
      summary: List properties.
```

## Operation Style

Every operation must define:

- `tags`
- `summary`
- `description` when business behavior is not obvious
- `security` for protected endpoints
- `parameters` for path and query parameters
- `requestBody` for create, update, and action endpoints
- `responses`

Use endpoint-specific success responses and reusable standard responses for
common no-content and error outcomes:

```yaml
responses:
  "201":
    description: Property was created.
    content:
      application/json:
        schema:
          $ref: "#/components/schemas/PropertyShowResponse"
  "400":
    $ref: "#/components/responses/BadRequest"
  "401":
    $ref: "#/components/responses/Unauthorized"
  "404":
    $ref: "#/components/responses/NotFound"
```

Define reusable standard responses under `components.responses` so the meaning
of `204`, `400`, `401`, `403`, `404`, `409`, and `500` stays consistent across
the contract.

Example:

```yaml
/properties:
  post:
    tags:
      - Properties
    summary: Create a property.
    description: Creates a property that can contain multiple units.
    security:
      - BearerAuth: []
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: "#/components/schemas/PropertyCreateRequest"
    responses:
      "201":
        description: Property was created.
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/PropertyShowResponse"
      "400":
        description: The request body is invalid.
      "401":
        description: Authentication is missing or invalid.
```

## Reusable Parameters

Common path and query parameters must live under `components.parameters`.

Example:

```yaml
components:
  parameters:
    PropertyId:
      name: propertyId
      in: path
      required: true
      description: UUID of the property.
      schema:
        type: string
        format: uuid
    Page:
      name: page
      in: query
      required: false
      description: One-based page number.
      schema:
        type: integer
        minimum: 1
        default: 1
```

Use `$ref` from operations:

```yaml
parameters:
  - $ref: "#/components/parameters/PropertyId"
  - $ref: "#/components/parameters/Page"
```

## Security Scheme

JWT authentication must use a reusable bearer scheme.

```yaml
components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

Protected operations must include:

```yaml
security:
  - BearerAuth: []
```

Auth endpoints such as login and register do not require bearer security.

## Schema Style

Schemas must represent DTOs, not JPA entities.

Use:

- `{Resource}CreateRequest`
- `{Resource}UpdateRequest`
- `{Resource}IndexElement` for one item in a list response
- `{Resource}IndexResponse` for list responses
- `{Resource}ShowResponse` for detail responses

Keep the route vocabulary aligned with the schema vocabulary:

```text
Index  = list
Show   = detail
Create = create
Update = update
Delete = delete or deactivate
```

Example:

```yaml
components:
  schemas:
    PropertyCreateRequest:
      type: object
      properties:
        name:
          type: string
          minLength: 1
          maxLength: 150
          example: Green Residence
        address:
          type: string
          nullable: true
          maxLength: 500
          example: Bekasi
      required:
        - name
```

## Response Shape

Return resource-shaped payloads directly. Do not wrap successful responses in
generic `data`, `message`, or `status` envelopes.

Single-resource responses use the resource schema directly:

```json
{
  "id": "2b6ff716-7208-4ab3-8de3-535fa4cda5f6",
  "name": "Green Residence",
  "address": "Bekasi",
  "active": true
}
```

List responses use a resource-specific top-level key:

```json
{
  "count": 1,
  "properties": [
    {
      "id": "2b6ff716-7208-4ab3-8de3-535fa4cda5f6",
      "name": "Green Residence",
      "active": true
    }
  ]
}
```

Common error statuses should be reusable responses under `components.responses`,
matching the shared status-code descriptions used across the contract. Do not
wrap `400`, `401`, `403`, `404`, `409`, or `500` in a project-specific generic
error envelope unless a later implementation has a concrete response body that
must be documented.

Every `IndexResponse` must include `count`, equal to the number of items
returned in that response array.

Use `offset` and `limit` pagination for broad collections that can grow large.
`offset` defaults to `0`; `limit` defaults to `100` and must not exceed `100`.
Do not add pagination reflexively to narrow nested histories when the current
use case does not need it.

## Type Rules

Use these OpenAPI types consistently:

| Java/domain type | OpenAPI type |
|---|---|
| `UUID` | `type: string`, `format: uuid` |
| `BigDecimal` money | `type: number`, `format: double` |
| `LocalDate` | `type: string`, `format: date` |
| `OffsetDateTime` or `Instant` | `type: string`, `format: date-time` |
| Enum | `type: string` with `enum` |

Money must remain `BigDecimal` in Java implementation even though OpenAPI
represents it as a number.

## Status Code Rules

| Status | Usage |
|---|---|
| 200 | Successful read, update, or action that returns a body |
| 201 | Successful creation |
| 204 | Successful deletion or deactivation with no response body |
| 400 | Validation error or malformed request |
| 401 | Missing or invalid authentication |
| 403 | Authenticated but not allowed |
| 404 | Resource not found |
| 409 | Duplicate data or conflicting business state |
| 500 | Unexpected server error |

## Validation Documentation

Validation rules must be visible in request schemas using OpenAPI keywords.

Examples:

```yaml
name:
  type: string
  minLength: 1
  maxLength: 150
monthlyFee:
  type: number
  format: double
  minimum: 0.01
dueDay:
  type: integer
  minimum: 1
  maximum: 28
```

## Tag List

Use these tags:

- Auth
- Cash Balances
- Invoices
- Payments
- Properties
- Property Expenses
- Reports
- Tenant Assignments
- Tenants
- Units

## Tenant Login Rule

Tenant login is not part of MVP.

Do not define tenant login endpoints in MVP.

For MVP:

- Only admin and staff users can login.
- Tenants are managed as data records.
- Tenants do not have application accounts.

Future tenant login must use secure credentials such as email, phone,
invitation-based signup, or secure access code.

Do not use property name and unit name alone as login credentials.

## Update Checklist

When public API behavior changes, update:

- `docs/API_SPEC.md`
- `openapi.yml` when it exists
- OpenAPI annotations when using generated Swagger
- Controller and integration tests

Public API behavior includes:

- Endpoint path
- HTTP method
- Request body
- Response body
- Query parameter
- Path parameter
- Validation rule
- Error response
- Status code
