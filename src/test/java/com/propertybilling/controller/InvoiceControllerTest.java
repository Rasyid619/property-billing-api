package com.propertybilling.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.propertybilling.config.SecurityConfig;
import com.propertybilling.constant.InvoiceStatus;
import com.propertybilling.constant.PaymentMethod;
import com.propertybilling.dto.invoice.InvoiceIndexElement;
import com.propertybilling.dto.invoice.InvoiceIndexResponse;
import com.propertybilling.dto.invoice.InvoiceShowResponse;
import com.propertybilling.dto.payment.PaymentCreateRequest;
import com.propertybilling.dto.payment.PaymentIndexElement;
import com.propertybilling.dto.payment.PaymentIndexResponse;
import com.propertybilling.entity.User;
import com.propertybilling.exception.GlobalExceptionHandler;
import com.propertybilling.exception.InvoiceGenerationConflictException;
import com.propertybilling.exception.InvoiceNotFoundException;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.InvoiceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InvoiceController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
/*
 * Web-layer tests for invoice endpoints.
 */
class InvoiceControllerTest {

	private final MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private InvoiceService invoiceService;

	@Autowired
	InvoiceControllerTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void generateMonthlyReturnsCreated() throws Exception {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

		mockMvc.perform(post("/api/v1/invoices/generate-monthly")
						.header("Authorization", "Bearer access-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "billing_month": "2026-05-01"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(invoiceService, times(1)).generateMonthlyInvoices(Mockito.argThat(request ->
				propertyId.equals(request.propertyId())
						&& LocalDate.parse("2026-05-01").equals(request.billingMonth())
		));
	}

	@Test
	void generateMonthlyRejectsBillingMonthThatIsNotFirstDay() throws Exception {
		mockMvc.perform(post("/api/v1/invoices/generate-monthly")
						.header("Authorization", "Bearer access-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "billing_month": "2026-05-02"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, invoiceService);
	}

	@Test
	void generateMonthlyReturnsNotFoundWhenPropertyDoesNotExist() throws Exception {
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		Mockito.doThrow(new PropertyNotFoundException())
				.when(invoiceService)
				.generateMonthlyInvoices(Mockito.any());

		mockMvc.perform(post("/api/v1/invoices/generate-monthly")
						.header("Authorization", "Bearer access-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000999",
								  "billing_month": "2026-05-01"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(invoiceService, times(1)).generateMonthlyInvoices(Mockito.any());
	}

	@Test
	void generateMonthlyReturnsConflictWhenGenerationConflicts() throws Exception {
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		Mockito.doThrow(new InvoiceGenerationConflictException())
				.when(invoiceService)
				.generateMonthlyInvoices(Mockito.any());

		mockMvc.perform(post("/api/v1/invoices/generate-monthly")
						.header("Authorization", "Bearer access-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "property_id": "00000000-0000-0000-0000-000000000101",
								  "billing_month": "2026-05-01"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(content().string(""));

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(invoiceService, times(1)).generateMonthlyInvoices(Mockito.any());
	}

	@Test
	void returnsInvoices() throws Exception {
		UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
		UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		when(invoiceService.listInvoices(propertyId, unitId, tenantId, "2026-05", "unpaid", 0, 100))
				.thenReturn(new InvoiceIndexResponse(
						1,
						List.of(new InvoiceIndexElement(
								UUID.fromString("00000000-0000-0000-0000-000000000401"),
								unitId,
								tenantId,
								LocalDate.parse("2026-05-01"),
								"INV-202605-A101",
								new BigDecimal("750000.00"),
								LocalDate.parse("2026-05-10"),
								"unpaid"
						))
				));

		mockMvc.perform(get("/api/v1/invoices")
						.header("Authorization", "Bearer access-token")
						.param("property_id", "00000000-0000-0000-0000-000000000101")
						.param("unit_id", "00000000-0000-0000-0000-000000000201")
						.param("tenant_id", "00000000-0000-0000-0000-000000000301")
						.param("month", "2026-05")
						.param("status", "unpaid"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.invoices[0].id").value("00000000-0000-0000-0000-000000000401"))
				.andExpect(jsonPath("$.invoices[0].unit_id").value("00000000-0000-0000-0000-000000000201"))
				.andExpect(jsonPath("$.invoices[0].tenant_id").value("00000000-0000-0000-0000-000000000301"))
				.andExpect(jsonPath("$.invoices[0].billing_month").value("2026-05-01"))
				.andExpect(jsonPath("$.invoices[0].invoice_number").value("INV-202605-A101"))
				.andExpect(jsonPath("$.invoices[0].amount").value(750000.00))
				.andExpect(jsonPath("$.invoices[0].due_date").value("2026-05-10"))
				.andExpect(jsonPath("$.invoices[0].status").value("unpaid"))
				.andExpect(jsonPath("$.invoices[0].created_at").doesNotExist())
				.andExpect(jsonPath("$.invoices[0].updated_at").doesNotExist());

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(invoiceService, times(1)).listInvoices(propertyId, unitId, tenantId, "2026-05", "unpaid", 0, 100);
	}

	@Test
	void rejectsInvalidMonth() throws Exception {
		mockMvc.perform(get("/api/v1/invoices")
						.header("Authorization", "Bearer access-token")
						.param("month", "2026-13"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, invoiceService);
	}

	@Test
	void rejectsInvalidStatus() throws Exception {
		mockMvc.perform(get("/api/v1/invoices")
						.header("Authorization", "Bearer access-token")
						.param("status", "archived"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, invoiceService);
	}

	@Test
	void rejectsInvalidLimit() throws Exception {
		mockMvc.perform(get("/api/v1/invoices")
						.header("Authorization", "Bearer access-token")
						.param("limit", "0"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, invoiceService);
	}

	@Test
	void returnsInvoiceDetail() throws Exception {
		UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
		UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
		UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		when(invoiceService.getInvoice(invoiceId)).thenReturn(new InvoiceShowResponse(
				invoiceId,
				unitId,
				tenantId,
				LocalDate.parse("2026-05-01"),
				"INV-202605-A101",
				new BigDecimal("750000.00"),
				LocalDate.parse("2026-05-10"),
				"unpaid"
		));

		mockMvc.perform(get("/api/v1/invoices/00000000-0000-0000-0000-000000000401")
						.header("Authorization", "Bearer access-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000401"))
				.andExpect(jsonPath("$.unit_id").value("00000000-0000-0000-0000-000000000201"))
				.andExpect(jsonPath("$.tenant_id").value("00000000-0000-0000-0000-000000000301"))
				.andExpect(jsonPath("$.billing_month").value("2026-05-01"))
				.andExpect(jsonPath("$.invoice_number").value("INV-202605-A101"))
				.andExpect(jsonPath("$.amount").value(750000.00))
				.andExpect(jsonPath("$.due_date").value("2026-05-10"))
				.andExpect(jsonPath("$.status").value("unpaid"))
				.andExpect(jsonPath("$.created_at").doesNotExist())
				.andExpect(jsonPath("$.updated_at").doesNotExist());

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(invoiceService, times(1)).getInvoice(invoiceId);
	}

	@Test
	void returnsNotFoundWhenInvoiceDoesNotExist() throws Exception {
		UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000999");
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		when(invoiceService.getInvoice(invoiceId)).thenThrow(new InvoiceNotFoundException());

		mockMvc.perform(get("/api/v1/invoices/00000000-0000-0000-0000-000000000999")
						.header("Authorization", "Bearer access-token"))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(invoiceService, times(1)).getInvoice(invoiceId);
	}

	@Test
	void returnsInvoicePayments() throws Exception {
		UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		when(invoiceService.listPayments(invoiceId)).thenReturn(new PaymentIndexResponse(
				1,
				List.of(new PaymentIndexElement(
						UUID.fromString("00000000-0000-0000-0000-000000000501"),
						invoiceId,
						new BigDecimal("750000.00"),
						LocalDate.parse("2026-05-08"),
						PaymentMethod.BANK_TRANSFER,
						"BCA-123456",
						"Paid by tenant",
						InvoiceStatus.PAID
				))
		));

		mockMvc.perform(get("/api/v1/invoices/00000000-0000-0000-0000-000000000401/payments")
						.header("Authorization", "Bearer access-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.payments[0].id").value("00000000-0000-0000-0000-000000000501"))
				.andExpect(jsonPath("$.payments[0].invoice_id").value("00000000-0000-0000-0000-000000000401"))
				.andExpect(jsonPath("$.payments[0].amount").value(750000.00))
				.andExpect(jsonPath("$.payments[0].payment_date").value("2026-05-08"))
				.andExpect(jsonPath("$.payments[0].payment_method").value("bank_transfer"))
				.andExpect(jsonPath("$.payments[0].reference_number").value("BCA-123456"))
				.andExpect(jsonPath("$.payments[0].note").value("Paid by tenant"))
				.andExpect(jsonPath("$.payments[0].invoice_status").value("paid"))
				.andExpect(jsonPath("$.payments[0].created_at").doesNotExist())
				.andExpect(jsonPath("$.payments[0].updated_at").doesNotExist());

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(invoiceService, times(1)).listPayments(invoiceId);
	}

	@Test
	void returnsNotFoundWhenInvoicePaymentsInvoiceDoesNotExist() throws Exception {
		UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000999");
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		when(invoiceService.listPayments(invoiceId)).thenThrow(new InvoiceNotFoundException());

		mockMvc.perform(get("/api/v1/invoices/00000000-0000-0000-0000-000000000999/payments")
						.header("Authorization", "Bearer access-token"))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(invoiceService, times(1)).listPayments(invoiceId);
	}

	@Test
	void recordPaymentReturnsCreated() throws Exception {
		UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000401");
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());

		mockMvc.perform(post("/api/v1/invoices/00000000-0000-0000-0000-000000000401/payments")
						.header("Authorization", "Bearer access-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": 750000.00,
								  "payment_date": "2026-05-08",
								  "payment_method": "bank_transfer",
								  "reference_number": "BCA-123456",
								  "note": "Paid by tenant"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(invoiceService, times(1)).recordPayment(Mockito.eq(invoiceId), Mockito.argThat(request ->
				new BigDecimal("750000.00").compareTo(request.amount()) == 0
						&& LocalDate.parse("2026-05-08").equals(request.paymentDate())
						&& PaymentMethod.BANK_TRANSFER.equals(request.paymentMethod())
						&& "BCA-123456".equals(request.referenceNumber())
						&& "Paid by tenant".equals(request.note())
		));
	}

	@Test
	void recordPaymentRejectsZeroAmount() throws Exception {
		mockMvc.perform(post("/api/v1/invoices/00000000-0000-0000-0000-000000000401/payments")
						.header("Authorization", "Bearer access-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": 0,
								  "payment_date": "2026-05-08",
								  "payment_method": "bank_transfer"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, invoiceService);
	}

	@Test
	void recordPaymentRejectsNegativeAmount() throws Exception {
		mockMvc.perform(post("/api/v1/invoices/00000000-0000-0000-0000-000000000401/payments")
						.header("Authorization", "Bearer access-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": -1,
								  "payment_date": "2026-05-08",
								  "payment_method": "bank_transfer"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, invoiceService);
	}

	@Test
	void recordPaymentRejectsUnsupportedPaymentMethod() throws Exception {
		mockMvc.perform(post("/api/v1/invoices/00000000-0000-0000-0000-000000000401/payments")
						.header("Authorization", "Bearer access-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": 750000.00,
								  "payment_date": "2026-05-08",
								  "payment_method": "crypto"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));

		verifyNoInteractions(authService, invoiceService);
	}

	@Test
	void recordPaymentReturnsNotFoundWhenInvoiceDoesNotExist() throws Exception {
		UUID invoiceId = UUID.fromString("00000000-0000-0000-0000-000000000999");
		when(authService.authenticateAccessToken("Bearer access-token")).thenReturn(buildUser());
		Mockito.doThrow(new InvoiceNotFoundException())
				.when(invoiceService)
				.recordPayment(Mockito.eq(invoiceId), Mockito.any(PaymentCreateRequest.class));

		mockMvc.perform(post("/api/v1/invoices/00000000-0000-0000-0000-000000000999/payments")
						.header("Authorization", "Bearer access-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": 750000.00,
								  "payment_date": "2026-05-08",
								  "payment_method": "bank_transfer"
								}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(content().string(""));

		verify(authService, times(1)).authenticateAccessToken("Bearer access-token");
		verify(invoiceService, times(1)).recordPayment(Mockito.eq(invoiceId), Mockito.any(PaymentCreateRequest.class));
	}

	private User buildUser() {
		return new User(
				UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"Admin User",
				"admin@example.com",
				"password-hash",
				"admin"
		);
	}
}
