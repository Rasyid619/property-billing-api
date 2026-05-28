# API Specification

This document defines the OpenAPI contract style for Property Billing API.

The project should use an OpenAPI 3.0.3 contract with the same structure as the
reference service OpenAPI file:

1. `openapi`
2. `info`
3. `servers`
4. `tags`
5. `paths`
6. `components`

When the project adds a static OpenAPI file, use `openapi.yml` at the project
root and keep this document aligned with it.

## Contract Rules

- Use `/api/v1` as the server base URL.
- Keep paths relative to the server URL, for example `/properties`.
- Define tags before paths.
- Sort tags, paths, parameters, responses, and schemas alphabetically where practical.
- Use reusable schemas under `components.schemas`.
- Use reusable query parameters under `components.parameters`.
- Use reusable standard responses under `components.responses`.
- Use reusable bearer authentication under `components.securitySchemes`.
- Keep standard error responses description-based unless a concrete endpoint response body needs to be documented.
- Add `count` to every index response so clients can read the returned array size directly.
- Use `offset` and `limit` pagination for broad collections that can grow large; default `limit` is `100`.
- Keep non-auth mutation responses lean: use `201` for create and `204` for update, delete, or action endpoints without response bodies.
- Auth endpoints may return token payloads directly, using `access_token` and `refresh_token` fields to make token roles explicit.
- Do not expose JPA entities directly in schemas.
- Use DTO-shaped request and response schemas.
- Use `snake_case` for all public request, response, and parameter field names.
- Use `Index` and `Show` schema families for list and detail responses.
- Use resource-first schema names such as `PropertyCreateRequest`, `PropertyIndexElement`, `PropertyShowResponse`, and `PropertyUpdateRequest`.
- Use `string` with `format: uuid` for IDs.
- Use `number` with `format: double` for money fields.
- Use `string` with `format: date` for dates.
- Use `string` with `format: date-time` for timestamps.
- Tenant login is not part of MVP.

## OpenAPI Foundation

```yaml
openapi: 3.0.3
info:
  title: Property Billing API
  version: 0.1.0
  description: API for apartment and housing monthly fee tracking.

servers:
  - url: http://localhost:8080/api/v1
    description: Local development server.

tags:
  - name: Auth
    description: Operations for admin and staff authentication.
  - name: Cash Balances
    description: Operations for monthly closing balances.
  - name: Invoices
    description: Operations for monthly invoice generation and retrieval.
  - name: Monitoring
    description: Operations for application monitoring and metrics.
  - name: Payments
    description: Operations for invoice payment recording.
  - name: Properties
    description: Operations for property management.
  - name: Property Expenses
    description: Operations for property expense tracking.
  - name: Reports
    description: Operations for cash-flow reporting.
  - name: Tenant Assignments
    description: Operations for unit tenant assignment history.
  - name: Tenants
    description: Operations for tenant data management.
  - name: Units
    description: Operations for unit management.

paths:
  /actuator/health:
    get:
      tags:
        - Monitoring
      summary: Check application health.
      description: Returns Spring Boot Actuator health status for local monitoring.
      servers:
        - url: http://localhost:8080
          description: Local application management server.
      responses:
        "200":
          description: Application is healthy.
          content:
            application/json:
              schema:
                type: object
                properties:
                  status:
                    type: string
                    example: UP

  /actuator/prometheus:
    get:
      tags:
        - Monitoring
      summary: Export Prometheus metrics.
      description: Returns application metrics in Prometheus text exposition format for local monitoring.
      servers:
        - url: http://localhost:8080
          description: Local application management server.
      responses:
        "200":
          description: Prometheus metrics were exported.
          content:
            text/plain:
              schema:
                type: string
                example: "# HELP jvm_info JVM version info"

  /auth/login:
    post:
      tags:
        - Auth
      summary: Login an admin or staff user.
      description: Returns a JWT access token for valid admin or staff credentials. Tenant login is not supported in MVP.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/LoginRequest"
      responses:
        "200":
          description: Login succeeded.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/AuthTokenResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"

  /auth/me:
    get:
      tags:
        - Auth
      summary: Get the authenticated user.
      description: Returns the currently authenticated admin or staff user.
      security:
        - BearerAuth: []
      responses:
        "200":
          description: Authenticated user was successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/AuthMeResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"

  /auth/refresh:
    post:
      tags:
        - Auth
      summary: Refresh an access token.
      description: Returns a new access token when a valid refresh token is provided.
      security: []
      parameters:
        - $ref: "#/components/parameters/RefreshTokenAuthorization"
      responses:
        "200":
          description: Access token was refreshed.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/AccessTokenResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"

  /cash-balances/close-month:
    post:
      tags:
        - Cash Balances
      summary: Close a monthly cash balance.
      description: Calculates and stores the opening balance, income, expenses, and closing balance for a property month.
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/CashBalanceCloseMonthRequest"
      responses:
        "201":
          description: Monthly cash balance was closed.
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
        "409":
          $ref: "#/components/responses/Conflict"

  /expenses:
    get:
      tags:
        - Property Expenses
      summary: List property expenses.
      description: Returns property expenses filtered by property and optional month.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyIdQuery"
        - $ref: "#/components/parameters/MonthQuery"
        - $ref: "#/components/parameters/Offset"
        - $ref: "#/components/parameters/Limit"
      responses:
        "200":
          description: Expenses were successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ExpenseIndexResponse"
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
    post:
      tags:
        - Property Expenses
      summary: Create a property expense.
      description: Records money spent for a property.
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/ExpenseCreateRequest"
      responses:
        "201":
          description: Expense was created.
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"

  /expenses/{expenseId}:
    delete:
      tags:
        - Property Expenses
      summary: Delete a property expense.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/ExpenseId"
      responses:
        "204":
          $ref: "#/components/responses/NoContent"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
    patch:
      tags:
        - Property Expenses
      summary: Update a property expense.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/ExpenseId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/ExpenseUpdateRequest"
      responses:
        "204":
          $ref: "#/components/responses/NoContent"
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"

  /invoices:
    get:
      tags:
        - Invoices
      summary: List invoices.
      description: Returns invoices filtered by property, unit, tenant, month, or status.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyIdOptionalQuery"
        - $ref: "#/components/parameters/UnitIdQuery"
        - $ref: "#/components/parameters/TenantIdQuery"
        - $ref: "#/components/parameters/MonthQuery"
        - $ref: "#/components/parameters/InvoiceStatusQuery"
        - $ref: "#/components/parameters/Offset"
        - $ref: "#/components/parameters/Limit"
      responses:
        "200":
          description: Invoices were successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/InvoiceIndexResponse"
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"

  /invoices/generate-monthly:
    post:
      tags:
        - Invoices
      summary: Generate monthly invoices.
      description: Generates one invoice per active unit with an active tenant for the requested property and billing month.
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/InvoiceGenerateMonthlyRequest"
      responses:
        "201":
          description: Monthly invoice generation completed.
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
        "409":
          $ref: "#/components/responses/Conflict"

  /invoices/{invoice_id}:
    get:
      tags:
        - Invoices
      summary: Get invoice detail.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/InvoiceId"
      responses:
        "200":
          description: Invoice detail was successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/InvoiceShowResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"

  /invoices/{invoice_id}/payments:
    get:
      tags:
        - Payments
      summary: List invoice payments.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/InvoiceId"
      responses:
        "200":
          description: Payments were successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PaymentIndexResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
    post:
      tags:
        - Payments
      summary: Record an invoice payment.
      description: Records payment and recalculates the invoice status.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/InvoiceId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/PaymentCreateRequest"
      responses:
        "201":
          description: Payment was recorded.
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"

  /properties:
    get:
      tags:
        - Properties
      summary: List properties.
      description: Returns properties filtered by optional search text and status.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/Offset"
        - $ref: "#/components/parameters/Limit"
        - $ref: "#/components/parameters/Search"
        - $ref: "#/components/parameters/Status"
      responses:
        "200":
          description: Properties were successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PropertyIndexResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"
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
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"

  /properties/{property_id}:
    delete:
      tags:
        - Properties
      summary: Deactivate a property.
      description: Marks a property inactive so it is not used for new workflows.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyId"
      responses:
        "204":
          $ref: "#/components/responses/NoContent"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
    get:
      tags:
        - Properties
      summary: Get property detail.
      description: Returns one property by ID.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyId"
      responses:
        "200":
          description: Property detail was successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PropertyShowResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
    put:
      tags:
        - Properties
      summary: Update a property.
      description: Replaces the name and address of an existing property.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/PropertyUpdateRequest"
      responses:
        "204":
          $ref: "#/components/responses/NoContent"
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"

  /properties/{property_id}/activate:
    post:
      tags:
        - Properties
      summary: Activate a property.
      description: Marks a property active so it can be used for new workflows.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyId"
      responses:
        "204":
          $ref: "#/components/responses/NoContent"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"

  /properties/{property_id}/units:
    get:
      tags:
        - Units
      summary: List units by property.
      description: Returns units for one property filtered by optional active status.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyId"
        - $ref: "#/components/parameters/Offset"
        - $ref: "#/components/parameters/Limit"
        - $ref: "#/components/parameters/Status"
      responses:
        "200":
          description: Units were successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/UnitIndexResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
    post:
      tags:
        - Units
      summary: Create a unit.
      description: Creates a unit inside a property. Unit and tenant are separate resources.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/UnitCreateRequest"
      responses:
        "201":
          description: Unit was created.
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
        "409":
          $ref: "#/components/responses/Conflict"

  /reports/cash-flow:
    get:
      tags:
        - Reports
      summary: Get a cash-flow report.
      description: Returns total income, total expenses, and net saving for a property month.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyIdQuery"
        - $ref: "#/components/parameters/MonthQueryRequired"
      responses:
        "200":
          description: Cash-flow report was successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/CashFlowReportResponse"
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"

  /tenants:
    get:
      tags:
        - Tenants
      summary: List tenants.
      description: Returns tenant data records filtered by optional search text. Tenants do not have login accounts in MVP.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/Offset"
        - $ref: "#/components/parameters/Limit"
        - $ref: "#/components/parameters/Search"
      responses:
        "200":
          description: Tenants were successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/TenantIndexResponse"
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
    post:
      tags:
        - Tenants
      summary: Create a tenant.
      description: Creates a tenant data record only. Tenants do not have login accounts in MVP.
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/TenantCreateRequest"
      responses:
        "201":
          description: Tenant was created.
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "409":
          $ref: "#/components/responses/Conflict"

  /tenants/{tenant_id}:
    get:
      tags:
        - Tenants
      summary: Get tenant detail.
      description: Returns one tenant data record. Tenants do not have login accounts in MVP.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/TenantId"
      responses:
        "200":
          description: Tenant detail was successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/TenantShowResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
    patch:
      tags:
        - Tenants
      summary: Update a tenant.
      description: Replaces one tenant data record's name, phone, and email. Duplicate phone or email values are rejected.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/TenantId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/TenantUpdateRequest"
      responses:
        "204":
          $ref: "#/components/responses/NoContent"
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
        "409":
          $ref: "#/components/responses/Conflict"

  /unit-tenant-assignments/{assignmentId}/move-out:
    patch:
      tags:
        - Tenant Assignments
      summary: Move out a tenant from a unit.
      description: Closes an active tenant assignment and preserves assignment history.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/AssignmentId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/TenantAssignmentMoveOutRequest"
      responses:
        "204":
          $ref: "#/components/responses/NoContent"
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"

  /units/{unit_id}:
    delete:
      tags:
        - Units
      summary: Deactivate a unit.
      description: Marks one unit inactive while keeping existing records available for history.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/UnitId"
      responses:
        "204":
          $ref: "#/components/responses/NoContent"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
    get:
      tags:
        - Units
      summary: Get unit detail.
      description: Returns one unit with its owning property, monthly fee, due day, and active state.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/UnitId"
      responses:
        "200":
          description: Unit detail was successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/UnitShowResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
    put:
      tags:
        - Units
      summary: Update a unit.
      description: Replaces the editable fields of one unit while keeping the owning property unchanged.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/UnitId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/UnitUpdateRequest"
      responses:
        "204":
          $ref: "#/components/responses/NoContent"
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
        "409":
          $ref: "#/components/responses/Conflict"

  /units/{unit_id}/activate:
    post:
      tags:
        - Units
      summary: Activate a unit.
      description: Marks one inactive unit active again so it can participate in future workflows.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/UnitId"
      responses:
        "204":
          $ref: "#/components/responses/NoContent"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"

  /units/{unit_id}/active-tenant:
    get:
      tags:
        - Tenant Assignments
      summary: Get the active tenant for a unit.
      description: Returns the active tenant assignment for a unit.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/UnitId"
      responses:
        "200":
          description: Active tenant assignment was successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/TenantAssignmentShowResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"

  /units/{unit_id}/tenant-assignments:
    get:
      tags:
        - Tenant Assignments
      summary: List tenant assignment history for a unit.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/UnitId"
      responses:
        "200":
          description: Assignment history was successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/TenantAssignmentIndexResponse"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
    post:
      tags:
        - Tenant Assignments
      summary: Assign a tenant to a unit.
      description: Creates an active tenant assignment. A unit can only have one active tenant assignment.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/UnitId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/TenantAssignmentCreateRequest"
      responses:
        "201":
          description: Tenant was assigned to the unit.
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/NotFound"
        "409":
          $ref: "#/components/responses/Conflict"

components:
  parameters:
    RefreshTokenAuthorization:
      name: Authorization
      in: header
      required: true
      description: Refresh token used to obtain a new access token.
      schema:
        type: string
        example: Bearer refresh-token
    AssignmentId:
      name: assignment_id
      in: path
      required: true
      description: UUID of the tenant assignment.
      schema:
        type: string
        format: uuid
    ExpenseId:
      name: expense_id
      in: path
      required: true
      description: UUID of the property expense.
      schema:
        type: string
        format: uuid
    InvoiceId:
      name: invoice_id
      in: path
      required: true
      description: UUID of the invoice.
      schema:
        type: string
        format: uuid
    InvoiceStatusQuery:
      name: status
      in: query
      required: false
      description: Invoice status filter.
      schema:
        $ref: "#/components/schemas/InvoiceStatus"
    MonthQuery:
      name: month
      in: query
      required: false
      description: Month in YYYY-MM format.
      schema:
        type: string
        pattern: "^\\d{4}-\\d{2}$"
        example: "2026-05"
    MonthQueryRequired:
      name: month
      in: query
      required: true
      description: Month in YYYY-MM format.
      schema:
        type: string
        pattern: "^\\d{4}-\\d{2}$"
        example: "2026-05"
    Limit:
      name: limit
      in: query
      required: false
      description: Maximum number of items to return.
      schema:
        type: integer
        minimum: 1
        maximum: 100
        default: 100
    PropertyId:
      name: property_id
      in: path
      required: true
      description: UUID of the property.
      schema:
        type: string
        format: uuid
    PropertyIdOptionalQuery:
      name: property_id
      in: query
      required: false
      description: UUID of the property.
      schema:
        type: string
        format: uuid
    PropertyIdQuery:
      name: property_id
      in: query
      required: true
      description: UUID of the property.
      schema:
        type: string
        format: uuid
    Search:
      name: search
      in: query
      required: false
      description: Search keyword.
      schema:
        type: string
        maxLength: 100
    Status:
      name: status
      in: query
      required: false
      description: Active or inactive status filter. Omit this parameter or pass null to include all records.
      schema:
        $ref: "#/components/schemas/Status"
    Offset:
      name: offset
      in: query
      required: false
      description: Number of items to skip before returning results.
      schema:
        type: integer
        minimum: 0
        default: 0
    TenantId:
      name: tenant_id
      in: path
      required: true
      description: UUID of the tenant.
      schema:
        type: string
        format: uuid
    TenantIdQuery:
      name: tenant_id
      in: query
      required: false
      description: UUID of the tenant.
      schema:
        type: string
        format: uuid
    UnitId:
      name: unit_id
      in: path
      required: true
      description: UUID of the unit.
      schema:
        type: string
        format: uuid
    UnitIdQuery:
      name: unit_id
      in: query
      required: false
      description: UUID of the unit.
      schema:
        type: string
        format: uuid
  responses:
    BadRequest:
      description: The request is invalid.
    Conflict:
      description: The request could not be completed due to a conflict with the current state of the resource.
    Forbidden:
      description: Access is forbidden.
    InternalServerError:
      description: The server encountered an unexpected condition that prevented it from fulfilling the request.
    NoContent:
      description: The request was successful and no content was returned.
    NotFound:
      description: The requested resource was not found.
    Unauthorized:
      description: Authentication is required and has failed.

  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:



    TenantAssignmentCreateRequest:
      type: object
      properties:
        tenant_id:
          type: string
          format: uuid
          description: UUID of the tenant to assign to the unit.
        start_date:
          type: string
          format: date
          description: First date of the tenant assignment.
          example: "2026-05-01"
      required:
        - tenant_id
        - start_date

    AccessTokenResponse:
      type: object
      properties:
        access_token:
          type: string
          description: JWT access token.
          example: access-token
      required:
        - access_token

    AuthTokenResponse:
      allOf:
        - $ref: "#/components/schemas/AccessTokenResponse"
        - type: object
          properties:
            refresh_token:
              type: string
              description: JWT refresh token.
              example: refresh-token
          required:
            - refresh_token

    AuthMeResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        name:
          type: string
          example: Admin User
        email:
          type: string
          format: email
          example: admin@example.com
        role:
          type: string
          enum:
            - admin
            - staff
          example: admin
      required:
        - id
        - name
        - email
        - role


    CashFlowReportResponse:
      type: object
      properties:
        property_id:
          type: string
          format: uuid
        month:
          type: string
          example: "2026-05"
        total_income:
          type: number
          format: double
          example: 15000000
        total_expense:
          type: number
          format: double
          example: 4000000
        net_saving:
          type: number
          format: double
          example: 11000000
      required:
        - property_id
        - month
        - total_income
        - total_expense
        - net_saving


    CashBalanceCloseMonthRequest:
      type: object
      properties:
        property_id:
          type: string
          format: uuid
        month:
          type: string
          format: date
          description: First day of the month.
          example: "2026-05-01"
      required:
        - property_id
        - month

    ExpenseCreateRequest:
      type: object
      properties:
        property_id:
          type: string
          format: uuid
        expense_date:
          type: string
          format: date
          example: "2026-05-12"
        category:
          type: string
          minLength: 1
          maxLength: 100
          example: cleaning
        amount:
          type: number
          format: double
          minimum: 0.01
          example: 750000
        description:
          type: string
          nullable: true
          maxLength: 500
          example: Monthly cleaning fee
      required:
        - property_id
        - expense_date
        - category
        - amount

    PaymentCreateRequest:
      type: object
      properties:
        amount:
          type: number
          format: double
          minimum: 0.01
          example: 750000
        payment_date:
          type: string
          format: date
          example: "2026-05-08"
        payment_method:
          type: string
          minLength: 1
          maxLength: 50
          example: bank_transfer
        reference_number:
          type: string
          nullable: true
          maxLength: 100
          example: BCA-123456
        note:
          type: string
          nullable: true
          maxLength: 500
          example: Paid by tenant
      required:
        - amount
        - payment_date
        - payment_method

    PropertyCreateRequest:
      type: object
      properties:
        name:
          type: string
          minLength: 1
          maxLength: 150
          pattern: '.*\S.*'
          example: Green Residence
        address:
          type: string
          nullable: true
          maxLength: 500
          example: Bekasi
      required:
        - name

    TenantCreateRequest:
      type: object
      properties:
        name:
          type: string
          minLength: 1
          maxLength: 150
          example: Budi
        phone:
          type: string
          nullable: true
          description: Optional phone number. Must be unique when provided.
          maxLength: 30
          example: "08123456789"
        email:
          type: string
          nullable: true
          description: Optional email address. Must be unique when provided.
          format: email
          maxLength: 150
          example: budi@example.com
      required:
        - name

    UnitCreateRequest:
      type: object
      properties:
        unit_number:
          type: string
          description: Unit number unique inside the property.
          minLength: 1
          maxLength: 50
          example: A-101
        monthly_fee:
          type: string
          description: Monthly fee decimal string greater than zero.
          pattern: "^(?!0+(?:\\.0+)?$)\\d+(?:\\.\\d{1,2})?$"
          example: "750000.00"
        due_day:
          type: integer
          description: Day of month when payment is due.
          minimum: 1
          maximum: 28
          example: 10
      required:
        - unit_number
        - monthly_fee
        - due_day

    ExpenseIndexResponse:
      type: object
      properties:
        count:
          type: integer
          minimum: 0
          description: Number of items returned in `expenses`.
          example: 1
        expenses:
          type: array
          items:
            $ref: "#/components/schemas/ExpenseIndexElement"
      required:
        - count
        - expenses

    ExpenseIndexElement:
      allOf:
        - $ref: "#/components/schemas/ExpenseShowResponse"

    ExpenseShowResponse:
      allOf:
        - $ref: "#/components/schemas/ExpenseCreateRequest"
        - type: object
          properties:
            id:
              type: string
              format: uuid
      required:
        - id


    InvoiceGenerateMonthlyRequest:
      type: object
      properties:
        property_id:
          type: string
          format: uuid
        billing_month:
          type: string
          format: date
          description: First day of the billing month.
          example: "2026-05-01"
      required:
        - property_id
        - billing_month

    InvoiceIndexResponse:
      type: object
      properties:
        count:
          type: integer
          minimum: 0
          description: Number of items returned in `invoices`.
          example: 1
        invoices:
          type: array
          items:
            $ref: "#/components/schemas/InvoiceIndexElement"
      required:
        - count
        - invoices

    InvoiceIndexElement:
      allOf:
        - $ref: "#/components/schemas/InvoiceShowResponse"

    InvoiceShowResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        unit_id:
          type: string
          format: uuid
        tenant_id:
          type: string
          format: uuid
        billing_month:
          type: string
          format: date
          example: "2026-05-01"
        invoice_number:
          type: string
          example: INV-202605-A101
        amount:
          type: number
          format: double
          example: 750000
        due_date:
          type: string
          format: date
          example: "2026-05-10"
        status:
          $ref: "#/components/schemas/InvoiceStatus"
      required:
        - id
        - unit_id
        - tenant_id
        - billing_month
        - invoice_number
        - amount
        - due_date
        - status


    InvoiceStatus:
      type: string
      enum:
        - unpaid
        - partial
        - paid
        - overdue
        - cancelled
      example: unpaid

    LoginRequest:
      type: object
      properties:
        email:
          type: string
          format: email
          example: admin@example.com
        password:
          type: string
          format: password
          minLength: 8
          example: password123
      required:
        - email
        - password

    TenantAssignmentMoveOutRequest:
      type: object
      properties:
        end_date:
          type: string
          format: date
          example: "2026-05-31"
      required:
        - end_date

    PaymentIndexResponse:
      type: object
      properties:
        count:
          type: integer
          minimum: 0
          description: Number of items returned in `payments`.
          example: 1
        payments:
          type: array
          items:
            $ref: "#/components/schemas/PaymentIndexElement"
      required:
        - count
        - payments

    PaymentIndexElement:
      allOf:
        - $ref: "#/components/schemas/PaymentShowResponse"

    PaymentShowResponse:
      allOf:
        - $ref: "#/components/schemas/PaymentCreateRequest"
        - type: object
          properties:
            id:
              type: string
              format: uuid
            invoice_id:
              type: string
              format: uuid
            invoice_status:
              $ref: "#/components/schemas/InvoiceStatus"
      required:
        - id
        - invoice_id
        - invoice_status


    PropertyIndexResponse:
      type: object
      properties:
        count:
          type: integer
          minimum: 0
          description: Number of items returned in `properties`.
          example: 1
        properties:
          type: array
          items:
            $ref: "#/components/schemas/PropertyIndexElement"
      required:
        - count
        - properties

    PropertyIndexElement:
      allOf:
        - $ref: "#/components/schemas/PropertyShowResponse"

    PropertyShowResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        name:
          type: string
          example: Green Residence
        address:
          type: string
          nullable: true
          example: Bekasi
        active:
          type: boolean
          example: true
      required:
        - id
        - name
        - active

    Status:
      type: string
      enum:
        - active
        - inactive
        - "null"
      example: active


    TenantAssignmentIndexResponse:
      type: object
      properties:
        count:
          type: integer
          minimum: 0
          description: Number of items returned in `tenant_assignments`.
          example: 1
        tenant_assignments:
          type: array
          items:
            $ref: "#/components/schemas/TenantAssignmentIndexElement"
      required:
        - count
        - tenant_assignments

    TenantAssignmentIndexElement:
      allOf:
        - $ref: "#/components/schemas/TenantAssignmentShowResponse"

    TenantAssignmentShowResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        unit_id:
          type: string
          format: uuid
        tenant_id:
          type: string
          format: uuid
        start_date:
          type: string
          format: date
          example: "2026-05-01"
        end_date:
          type: string
          format: date
          nullable: true
          example: null
        active:
          type: boolean
          example: true
      required:
        - id
        - unit_id
        - tenant_id
        - start_date
        - active


    TenantIndexResponse:
      type: object
      properties:
        count:
          type: integer
          minimum: 0
          description: Number of items returned in `tenants`.
          example: 1
        tenants:
          type: array
          items:
            $ref: "#/components/schemas/TenantIndexElement"
      required:
        - count
        - tenants

    TenantIndexElement:
      allOf:
        - $ref: "#/components/schemas/TenantShowResponse"

    TenantShowResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        name:
          type: string
          example: Budi
        phone:
          type: string
          nullable: true
          example: "08123456789"
        email:
          type: string
          nullable: true
          format: email
          example: budi@example.com
      required:
        - id
        - name


    UnitIndexResponse:
      type: object
      properties:
        count:
          type: integer
          minimum: 0
          description: Number of items returned in `units`.
          example: 1
        units:
          type: array
          items:
            $ref: "#/components/schemas/UnitIndexElement"
      required:
        - count
        - units

    UnitIndexElement:
      allOf:
        - $ref: "#/components/schemas/UnitShowResponse"

    UnitShowResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        property_id:
          type: string
          format: uuid
        unit_number:
          type: string
          example: A-101
        monthly_fee:
          type: number
          format: double
          example: 750000
        due_day:
          type: integer
          example: 10
        active:
          type: boolean
          example: true
      required:
        - id
        - property_id
        - unit_number
        - monthly_fee
        - due_day
        - active


    ExpenseUpdateRequest:
      allOf:
        - $ref: "#/components/schemas/ExpenseCreateRequest"

    PropertyUpdateRequest:
      allOf:
        - $ref: "#/components/schemas/PropertyCreateRequest"

    TenantUpdateRequest:
      allOf:
        - $ref: "#/components/schemas/TenantCreateRequest"

    UnitUpdateRequest:
      allOf:
        - $ref: "#/components/schemas/UnitCreateRequest"

```

## Required Error Responses

Every secured endpoint must document:

- `401` for missing or invalid JWT.
- `403` when authenticated users are not allowed to perform an action.

Every endpoint that loads a resource by ID must document:

- `404` when the resource does not exist.

Every endpoint that creates or updates data must document:

- `400` for validation errors.
- `409` for duplicate or conflicting business state when applicable.

## Tenant Login Rule

Do not define tenant login endpoints in MVP.

For MVP:

- Only admin and staff users can login.
- Tenants are data records only.
- Tenant credentials must not be based on property name and unit name.
