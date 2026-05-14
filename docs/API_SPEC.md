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
- Use reusable bearer authentication under `components.securitySchemes`.
- Do not expose JPA entities directly in schemas.
- Use DTO-shaped request and response schemas.
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
                $ref: "#/components/schemas/AuthTokenResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: The credentials are invalid.

  /auth/register:
    post:
      tags:
        - Auth
      summary: Register an admin or staff user.
      description: Creates a user account for administrators or property staff only.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/RegisterRequest"
      responses:
        "201":
          description: User registration succeeded.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/UserResponseEnvelope"
        "400":
          description: The request body is invalid.
        "409":
          description: The email address is already registered.

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
              $ref: "#/components/schemas/CloseCashBalanceRequest"
      responses:
        "201":
          description: Monthly cash balance was closed.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/CashBalanceResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The property was not found.
        "409":
          description: The property month has already been closed.

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
        - $ref: "#/components/parameters/Page"
        - $ref: "#/components/parameters/Size"
      responses:
        "200":
          description: Expenses were successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ExpenseListResponseEnvelope"
        "400":
          description: Query parameters are invalid.
        "401":
          description: Authentication is missing or invalid.
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
              $ref: "#/components/schemas/CreateExpenseRequest"
      responses:
        "201":
          description: Expense was created.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ExpenseResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The property was not found.

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
          description: Expense was deleted.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The expense was not found.
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
              $ref: "#/components/schemas/UpdateExpenseRequest"
      responses:
        "200":
          description: Expense was updated.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ExpenseResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The expense was not found.

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
        - $ref: "#/components/parameters/Page"
        - $ref: "#/components/parameters/Size"
      responses:
        "200":
          description: Invoices were successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/InvoiceListResponseEnvelope"
        "400":
          description: Query parameters are invalid.
        "401":
          description: Authentication is missing or invalid.

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
              $ref: "#/components/schemas/GenerateMonthlyInvoicesRequest"
      responses:
        "201":
          description: Monthly invoice generation completed.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/GeneratedInvoicesResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The property was not found.
        "409":
          description: An invoice already exists for the same unit and billing month.

  /invoices/{invoiceId}:
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
                $ref: "#/components/schemas/InvoiceResponseEnvelope"
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The invoice was not found.

  /invoices/{invoiceId}/payments:
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
                $ref: "#/components/schemas/PaymentListResponseEnvelope"
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The invoice was not found.
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
              $ref: "#/components/schemas/CreatePaymentRequest"
      responses:
        "201":
          description: Payment was recorded.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PaymentResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The invoice was not found.

  /properties:
    get:
      tags:
        - Properties
      summary: List properties.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/Page"
        - $ref: "#/components/parameters/Size"
        - $ref: "#/components/parameters/Search"
      responses:
        "200":
          description: Properties were successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PropertyListResponseEnvelope"
        "401":
          description: Authentication is missing or invalid.
    post:
      tags:
        - Properties
      summary: Create a property.
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/CreatePropertyRequest"
      responses:
        "201":
          description: Property was created.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PropertyResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.

  /properties/{propertyId}:
    delete:
      tags:
        - Properties
      summary: Deactivate a property.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyId"
      responses:
        "204":
          description: Property was deactivated.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The property was not found.
    get:
      tags:
        - Properties
      summary: Get property detail.
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
                $ref: "#/components/schemas/PropertyResponseEnvelope"
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The property was not found.
    patch:
      tags:
        - Properties
      summary: Update a property.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/UpdatePropertyRequest"
      responses:
        "200":
          description: Property was updated.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PropertyResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The property was not found.

  /properties/{propertyId}/units:
    get:
      tags:
        - Units
      summary: List units by property.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/PropertyId"
        - $ref: "#/components/parameters/Page"
        - $ref: "#/components/parameters/Size"
      responses:
        "200":
          description: Units were successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/UnitListResponseEnvelope"
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The property was not found.
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
              $ref: "#/components/schemas/CreateUnitRequest"
      responses:
        "201":
          description: Unit was created.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/UnitResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The property was not found.
        "409":
          description: Unit number already exists inside the property.

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
                $ref: "#/components/schemas/CashFlowReportResponseEnvelope"
        "400":
          description: Query parameters are invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The property was not found.

  /tenants:
    get:
      tags:
        - Tenants
      summary: List tenants.
      description: Returns tenant data records. Tenants do not have login accounts in MVP.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/Page"
        - $ref: "#/components/parameters/Size"
        - $ref: "#/components/parameters/Search"
      responses:
        "200":
          description: Tenants were successfully retrieved.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/TenantListResponseEnvelope"
        "401":
          description: Authentication is missing or invalid.
    post:
      tags:
        - Tenants
      summary: Create a tenant.
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/CreateTenantRequest"
      responses:
        "201":
          description: Tenant was created.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/TenantResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.

  /tenants/{tenantId}:
    get:
      tags:
        - Tenants
      summary: Get tenant detail.
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
                $ref: "#/components/schemas/TenantResponseEnvelope"
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The tenant was not found.
    patch:
      tags:
        - Tenants
      summary: Update a tenant.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/TenantId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/UpdateTenantRequest"
      responses:
        "200":
          description: Tenant was updated.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/TenantResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The tenant was not found.

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
              $ref: "#/components/schemas/MoveOutTenantRequest"
      responses:
        "200":
          description: Tenant assignment was closed.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/TenantAssignmentResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The assignment was not found.

  /units/{unitId}:
    delete:
      tags:
        - Units
      summary: Deactivate a unit.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/UnitId"
      responses:
        "204":
          description: Unit was deactivated.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The unit was not found.
    get:
      tags:
        - Units
      summary: Get unit detail.
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
                $ref: "#/components/schemas/UnitResponseEnvelope"
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The unit was not found.
    patch:
      tags:
        - Units
      summary: Update a unit.
      security:
        - BearerAuth: []
      parameters:
        - $ref: "#/components/parameters/UnitId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/UpdateUnitRequest"
      responses:
        "200":
          description: Unit was updated.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/UnitResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The unit was not found.
        "409":
          description: Unit number already exists inside the property.

  /units/{unitId}/active-tenant:
    get:
      tags:
        - Tenant Assignments
      summary: Get the active tenant for a unit.
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
                $ref: "#/components/schemas/TenantAssignmentResponseEnvelope"
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The unit or active assignment was not found.

  /units/{unitId}/tenant-assignments:
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
                $ref: "#/components/schemas/TenantAssignmentListResponseEnvelope"
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The unit was not found.
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
              $ref: "#/components/schemas/AssignTenantRequest"
      responses:
        "201":
          description: Tenant was assigned to the unit.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/TenantAssignmentResponseEnvelope"
        "400":
          description: The request body is invalid.
        "401":
          description: Authentication is missing or invalid.
        "404":
          description: The unit or tenant was not found.
        "409":
          description: The unit already has an active tenant assignment.

components:
  parameters:
    AssignmentId:
      name: assignmentId
      in: path
      required: true
      description: UUID of the tenant assignment.
      schema:
        type: string
        format: uuid
    ExpenseId:
      name: expenseId
      in: path
      required: true
      description: UUID of the property expense.
      schema:
        type: string
        format: uuid
    InvoiceId:
      name: invoiceId
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
    Page:
      name: page
      in: query
      required: false
      description: One-based page number.
      schema:
        type: integer
        minimum: 1
        default: 1
    PropertyId:
      name: propertyId
      in: path
      required: true
      description: UUID of the property.
      schema:
        type: string
        format: uuid
    PropertyIdOptionalQuery:
      name: propertyId
      in: query
      required: false
      description: UUID of the property.
      schema:
        type: string
        format: uuid
    PropertyIdQuery:
      name: propertyId
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
    Size:
      name: size
      in: query
      required: false
      description: Number of items per page.
      schema:
        type: integer
        minimum: 1
        maximum: 100
        default: 10
    TenantId:
      name: tenantId
      in: path
      required: true
      description: UUID of the tenant.
      schema:
        type: string
        format: uuid
    TenantIdQuery:
      name: tenantId
      in: query
      required: false
      description: UUID of the tenant.
      schema:
        type: string
        format: uuid
    UnitId:
      name: unitId
      in: path
      required: true
      description: UUID of the unit.
      schema:
        type: string
        format: uuid
    UnitIdQuery:
      name: unitId
      in: query
      required: false
      description: UUID of the unit.
      schema:
        type: string
        format: uuid

  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    ApiMeta:
      type: object
      properties:
        page:
          type: integer
          example: 1
        size:
          type: integer
          example: 10
        totalItems:
          type: integer
          example: 100
        totalPages:
          type: integer
          example: 10
      required:
        - page
        - size
        - totalItems
        - totalPages

    ApiValidationError:
      type: object
      properties:
        field:
          type: string
          example: name
        message:
          type: string
          example: must not be blank
      required:
        - field
        - message

    ErrorResponse:
      type: object
      properties:
        timestamp:
          type: string
          format: date-time
          example: "2026-05-14T10:00:00Z"
        status:
          type: integer
          example: 400
        error:
          type: string
          example: Bad Request
        message:
          type: string
          example: Validation failed.
        path:
          type: string
          example: /api/v1/properties
        details:
          type: array
          items:
            $ref: "#/components/schemas/ApiValidationError"
      required:
        - timestamp
        - status
        - error
        - message
        - path

    AssignTenantRequest:
      type: object
      properties:
        tenantId:
          type: string
          format: uuid
        startDate:
          type: string
          format: date
          example: "2026-05-01"
      required:
        - tenantId
        - startDate

    AuthTokenResponse:
      type: object
      properties:
        accessToken:
          type: string
          example: jwt-token
        tokenType:
          type: string
          example: Bearer
      required:
        - accessToken
        - tokenType

    AuthTokenResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/AuthTokenResponse"

    CashBalanceResponse:
      type: object
      properties:
        propertyId:
          type: string
          format: uuid
        month:
          type: string
          format: date
          example: "2026-05-01"
        openingBalance:
          type: number
          format: double
          example: 5000000
        totalIncome:
          type: number
          format: double
          example: 15000000
        totalExpense:
          type: number
          format: double
          example: 4000000
        closingBalance:
          type: number
          format: double
          example: 16000000
      required:
        - propertyId
        - month
        - openingBalance
        - totalIncome
        - totalExpense
        - closingBalance

    CashBalanceResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/CashBalanceResponse"

    CashFlowReportResponse:
      type: object
      properties:
        propertyId:
          type: string
          format: uuid
        month:
          type: string
          example: "2026-05"
        totalIncome:
          type: number
          format: double
          example: 15000000
        totalExpense:
          type: number
          format: double
          example: 4000000
        netSaving:
          type: number
          format: double
          example: 11000000
      required:
        - propertyId
        - month
        - totalIncome
        - totalExpense
        - netSaving

    CashFlowReportResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/CashFlowReportResponse"

    CloseCashBalanceRequest:
      type: object
      properties:
        propertyId:
          type: string
          format: uuid
        month:
          type: string
          format: date
          description: First day of the month.
          example: "2026-05-01"
      required:
        - propertyId
        - month

    CreateExpenseRequest:
      type: object
      properties:
        propertyId:
          type: string
          format: uuid
        expenseDate:
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
        - propertyId
        - expenseDate
        - category
        - amount

    CreatePaymentRequest:
      type: object
      properties:
        amount:
          type: number
          format: double
          minimum: 0.01
          example: 750000
        paymentDate:
          type: string
          format: date
          example: "2026-05-08"
        paymentMethod:
          type: string
          minLength: 1
          maxLength: 50
          example: bank_transfer
        referenceNumber:
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
        - paymentDate
        - paymentMethod

    CreatePropertyRequest:
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

    CreateTenantRequest:
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
          maxLength: 30
          example: "08123456789"
        email:
          type: string
          nullable: true
          format: email
          maxLength: 150
          example: budi@example.com
      required:
        - name

    CreateUnitRequest:
      type: object
      properties:
        unitNumber:
          type: string
          minLength: 1
          maxLength: 50
          example: A-101
        monthlyFee:
          type: number
          format: double
          minimum: 0.01
          example: 750000
        dueDay:
          type: integer
          minimum: 1
          maximum: 28
          example: 10
      required:
        - unitNumber
        - monthlyFee
        - dueDay

    ExpenseListResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessListResponse"
        - type: object
          properties:
            data:
              type: array
              items:
                $ref: "#/components/schemas/ExpenseResponse"

    ExpenseResponse:
      allOf:
        - $ref: "#/components/schemas/CreateExpenseRequest"
        - type: object
          properties:
            id:
              type: string
              format: uuid
      required:
        - id

    ExpenseResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/ExpenseResponse"

    GenerateMonthlyInvoicesRequest:
      type: object
      properties:
        propertyId:
          type: string
          format: uuid
        billingMonth:
          type: string
          format: date
          description: First day of the billing month.
          example: "2026-05-01"
      required:
        - propertyId
        - billingMonth

    GeneratedInvoicesResponse:
      type: object
      properties:
        billingMonth:
          type: string
          format: date
          example: "2026-05-01"
        generated:
          type: array
          items:
            $ref: "#/components/schemas/InvoiceResponse"
        skipped:
          type: array
          items:
            $ref: "#/components/schemas/SkippedInvoiceResponse"
      required:
        - billingMonth
        - generated
        - skipped

    GeneratedInvoicesResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/GeneratedInvoicesResponse"

    InvoiceListResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessListResponse"
        - type: object
          properties:
            data:
              type: array
              items:
                $ref: "#/components/schemas/InvoiceResponse"

    InvoiceResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        unitId:
          type: string
          format: uuid
        tenantId:
          type: string
          format: uuid
        billingMonth:
          type: string
          format: date
          example: "2026-05-01"
        invoiceNumber:
          type: string
          example: INV-202605-A101
        amount:
          type: number
          format: double
          example: 750000
        dueDate:
          type: string
          format: date
          example: "2026-05-10"
        status:
          $ref: "#/components/schemas/InvoiceStatus"
      required:
        - id
        - unitId
        - tenantId
        - billingMonth
        - invoiceNumber
        - amount
        - dueDate
        - status

    InvoiceResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/InvoiceResponse"

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

    MoveOutTenantRequest:
      type: object
      properties:
        endDate:
          type: string
          format: date
          example: "2026-05-31"
      required:
        - endDate

    PaymentListResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessListResponse"
        - type: object
          properties:
            data:
              type: array
              items:
                $ref: "#/components/schemas/PaymentResponse"

    PaymentResponse:
      allOf:
        - $ref: "#/components/schemas/CreatePaymentRequest"
        - type: object
          properties:
            id:
              type: string
              format: uuid
            invoiceId:
              type: string
              format: uuid
            invoiceStatus:
              $ref: "#/components/schemas/InvoiceStatus"
      required:
        - id
        - invoiceId
        - invoiceStatus

    PaymentResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/PaymentResponse"

    PropertyListResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessListResponse"
        - type: object
          properties:
            data:
              type: array
              items:
                $ref: "#/components/schemas/PropertyResponse"

    PropertyResponse:
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
        createdAt:
          type: string
          format: date-time
        updatedAt:
          type: string
          format: date-time
      required:
        - id
        - name
        - active
        - createdAt
        - updatedAt

    PropertyResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/PropertyResponse"

    RegisterRequest:
      type: object
      properties:
        name:
          type: string
          minLength: 1
          maxLength: 150
          example: Admin User
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
        - name
        - email
        - password

    SkippedInvoiceResponse:
      type: object
      properties:
        unitId:
          type: string
          format: uuid
        reason:
          type: string
          example: Unit has no active tenant.
      required:
        - unitId
        - reason

    SuccessListResponse:
      type: object
      properties:
        data:
          type: array
          items:
            type: object
        message:
          type: string
          example: Success
        meta:
          $ref: "#/components/schemas/ApiMeta"
      required:
        - data
        - message
        - meta

    SuccessResponse:
      type: object
      properties:
        data:
          type: object
        message:
          type: string
          example: Success
      required:
        - data
        - message

    TenantAssignmentListResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessListResponse"
        - type: object
          properties:
            data:
              type: array
              items:
                $ref: "#/components/schemas/TenantAssignmentResponse"

    TenantAssignmentResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        unitId:
          type: string
          format: uuid
        tenantId:
          type: string
          format: uuid
        startDate:
          type: string
          format: date
          example: "2026-05-01"
        endDate:
          type: string
          format: date
          nullable: true
          example: null
        active:
          type: boolean
          example: true
      required:
        - id
        - unitId
        - tenantId
        - startDate
        - active

    TenantAssignmentResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/TenantAssignmentResponse"

    TenantListResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessListResponse"
        - type: object
          properties:
            data:
              type: array
              items:
                $ref: "#/components/schemas/TenantResponse"

    TenantResponse:
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

    TenantResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/TenantResponse"

    UnitListResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessListResponse"
        - type: object
          properties:
            data:
              type: array
              items:
                $ref: "#/components/schemas/UnitResponse"

    UnitResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        propertyId:
          type: string
          format: uuid
        unitNumber:
          type: string
          example: A-101
        monthlyFee:
          type: number
          format: double
          example: 750000
        dueDay:
          type: integer
          example: 10
        active:
          type: boolean
          example: true
      required:
        - id
        - propertyId
        - unitNumber
        - monthlyFee
        - dueDay
        - active

    UnitResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/UnitResponse"

    UpdateExpenseRequest:
      allOf:
        - $ref: "#/components/schemas/CreateExpenseRequest"

    UpdatePropertyRequest:
      allOf:
        - $ref: "#/components/schemas/CreatePropertyRequest"

    UpdateTenantRequest:
      allOf:
        - $ref: "#/components/schemas/CreateTenantRequest"

    UpdateUnitRequest:
      allOf:
        - $ref: "#/components/schemas/CreateUnitRequest"

    UserResponse:
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

    UserResponseEnvelope:
      allOf:
        - $ref: "#/components/schemas/SuccessResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/UserResponse"
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
