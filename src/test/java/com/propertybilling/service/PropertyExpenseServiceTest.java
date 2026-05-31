package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.propertybilling.constant.ExpenseCategory;
import com.propertybilling.dto.expense.ExpenseCreateRequest;
import com.propertybilling.dto.expense.ExpenseIndexResponse;
import com.propertybilling.dto.expense.ExpenseUpdateRequest;
import com.propertybilling.dto.expense.queryresult.ExpenseIndexQueryResult;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.PropertyExpense;
import com.propertybilling.exception.PropertyExpenseNotFoundException;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.PropertyExpenseRepository;
import com.propertybilling.repository.PropertyRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/*
 * Unit tests for property expense retrieval workflows.
 */
class PropertyExpenseServiceTest {

	private final PropertyRepository propertyRepository = Mockito.mock(PropertyRepository.class);
	private final PropertyExpenseRepository propertyExpenseRepository = Mockito.mock(PropertyExpenseRepository.class);
	private final PropertyExpenseService propertyExpenseService = new PropertyExpenseService(
			propertyRepository,
			propertyExpenseRepository
	);

	@Nested
	class CreateExpense {

		@Test
		void createsExpense() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			Property property = new Property(propertyId, "Green Residence", null, true);
			when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

			propertyExpenseService.createExpense(new ExpenseCreateRequest(
					propertyId,
					LocalDate.parse("2026-05-12"),
					ExpenseCategory.CLEANING,
					new BigDecimal("750000.00"),
					"Monthly cleaning fee"
			));

			ArgumentCaptor<PropertyExpense> expenseCaptor = ArgumentCaptor.forClass(PropertyExpense.class);
			verify(propertyRepository, times(1)).findById(propertyId);
			verify(propertyExpenseRepository, times(1)).save(expenseCaptor.capture());
			PropertyExpense expense = expenseCaptor.getValue();
			assertThat(expense.getId()).isNotNull();
			assertThat(expense.getProperty()).isEqualTo(property);
			assertThat(expense.getExpenseDate()).isEqualTo(LocalDate.parse("2026-05-12"));
			assertThat(expense.getCategory()).isEqualTo("cleaning");
			assertThat(expense.getAmount()).isEqualTo("750000.00");
			assertThat(expense.getDescription()).isEqualTo("Monthly cleaning fee");
			assertThat(expense.getReceiptUrl()).isNull();
		}

		@Test
		void throwsNotFoundWhenPropertyDoesNotExist() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> propertyExpenseService.createExpense(new ExpenseCreateRequest(
					propertyId,
					LocalDate.parse("2026-05-12"),
					ExpenseCategory.CLEANING,
					new BigDecimal("750000.00"),
					"Monthly cleaning fee"
			))).isInstanceOf(PropertyNotFoundException.class);

			verify(propertyRepository, times(1)).findById(propertyId);
			verify(propertyExpenseRepository, never()).save(any());
		}
	}

	@Nested
	class UpdateExpense {

		@Test
		void updatesExpense() {
			UUID expenseId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID originalPropertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			UUID updatedPropertyId = UUID.fromString("00000000-0000-0000-0000-000000000102");
			Property originalProperty = new Property(originalPropertyId, "Green Residence", null, true);
			Property updatedProperty = new Property(updatedPropertyId, "Blue Terrace", null, true);
			PropertyExpense expense = new PropertyExpense(
					expenseId,
					originalProperty,
					LocalDate.parse("2026-05-12"),
					"cleaning",
					"750000.00",
					"Monthly cleaning fee",
					null
			);
			when(propertyExpenseRepository.findByIdForUpdate(expenseId)).thenReturn(Optional.of(expense));
			when(propertyRepository.findById(updatedPropertyId)).thenReturn(Optional.of(updatedProperty));

			propertyExpenseService.updateExpense(expenseId, new ExpenseUpdateRequest(
					updatedPropertyId,
					LocalDate.parse("2026-05-15"),
					ExpenseCategory.REPAIR,
					new BigDecimal("1250000.00"),
					"Gate repair"
			));

			verify(propertyExpenseRepository, times(1)).findByIdForUpdate(expenseId);
			verify(propertyRepository, times(1)).findById(updatedPropertyId);
			verify(propertyExpenseRepository, times(1)).save(expense);
			assertThat(expense.getProperty()).isEqualTo(updatedProperty);
			assertThat(expense.getExpenseDate()).isEqualTo(LocalDate.parse("2026-05-15"));
			assertThat(expense.getCategory()).isEqualTo("repair");
			assertThat(expense.getAmount()).isEqualTo("1250000.00");
			assertThat(expense.getDescription()).isEqualTo("Gate repair");
		}

		@Test
		void throwsNotFoundWhenExpenseDoesNotExist() {
			UUID expenseId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyExpenseRepository.findByIdForUpdate(expenseId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> propertyExpenseService.updateExpense(expenseId, new ExpenseUpdateRequest(
					propertyId,
					LocalDate.parse("2026-05-15"),
					ExpenseCategory.REPAIR,
					new BigDecimal("1250000.00"),
					"Gate repair"
			))).isInstanceOf(PropertyExpenseNotFoundException.class);

			verify(propertyExpenseRepository, times(1)).findByIdForUpdate(expenseId);
			verify(propertyRepository, never()).findById(any());
			verify(propertyExpenseRepository, never()).save(any());
		}

		@Test
		void throwsNotFoundWhenPropertyDoesNotExist() {
			UUID expenseId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID originalPropertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			UUID missingPropertyId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			Property originalProperty = new Property(originalPropertyId, "Green Residence", null, true);
			PropertyExpense expense = new PropertyExpense(
					expenseId,
					originalProperty,
					LocalDate.parse("2026-05-12"),
					"cleaning",
					"750000.00",
					"Monthly cleaning fee",
					null
			);
			when(propertyExpenseRepository.findByIdForUpdate(expenseId)).thenReturn(Optional.of(expense));
			when(propertyRepository.findById(missingPropertyId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> propertyExpenseService.updateExpense(expenseId, new ExpenseUpdateRequest(
					missingPropertyId,
					LocalDate.parse("2026-05-15"),
					ExpenseCategory.REPAIR,
					new BigDecimal("1250000.00"),
					"Gate repair"
			))).isInstanceOf(PropertyNotFoundException.class);

			verify(propertyExpenseRepository, times(1)).findByIdForUpdate(expenseId);
			verify(propertyRepository, times(1)).findById(missingPropertyId);
			verify(propertyExpenseRepository, never()).save(any());
			assertThat(expense.getProperty()).isEqualTo(originalProperty);
			assertThat(expense.getExpenseDate()).isEqualTo(LocalDate.parse("2026-05-12"));
			assertThat(expense.getCategory()).isEqualTo("cleaning");
			assertThat(expense.getAmount()).isEqualTo("750000.00");
			assertThat(expense.getDescription()).isEqualTo("Monthly cleaning fee");
		}
	}

	@Nested
	class ListExpenses {

		@Test
		void listsExpensesForPropertyAndMonth() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			UUID expenseId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			when(propertyExpenseRepository.findIndex(
					propertyId,
					LocalDate.parse("2026-05-01"),
					LocalDate.parse("2026-06-01"),
					0,
					100
			)).thenReturn(List.of(buildExpense(
					expenseId,
					propertyId,
					LocalDate.parse("2026-05-12"),
					"cleaning",
					"750000.00",
					"Monthly cleaning fee"
			)));

			ExpenseIndexResponse response = propertyExpenseService.listExpenses(propertyId, "2026-05", 0, 100);

			assertThat(response.count()).isEqualTo(1);
			assertThat(response.expenses()).hasSize(1);
			assertThat(response.expenses().getFirst().id()).isEqualTo(expenseId);
			assertThat(response.expenses().getFirst().propertyId()).isEqualTo(propertyId);
			assertThat(response.expenses().getFirst().expenseDate()).isEqualTo(LocalDate.parse("2026-05-12"));
			assertThat(response.expenses().getFirst().category()).isEqualTo("cleaning");
			assertThat(response.expenses().getFirst().amount()).isEqualByComparingTo("750000.00");
			assertThat(response.expenses().getFirst().description()).isEqualTo("Monthly cleaning fee");
			verify(propertyExpenseRepository, times(1)).findIndex(
					propertyId,
					LocalDate.parse("2026-05-01"),
					LocalDate.parse("2026-06-01"),
					0,
					100
			);
		}

		@Test
		void listsExpensesWithoutMonthFilter() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyExpenseRepository.findIndex(propertyId, null, null, 200, 100)).thenReturn(List.of());

			ExpenseIndexResponse response = propertyExpenseService.listExpenses(propertyId, null, 200, 100);

			assertThat(response.count()).isZero();
			assertThat(response.expenses()).isEmpty();
			verify(propertyExpenseRepository, times(1)).findIndex(propertyId, null, null, 200, 100);
		}
	}

	private ExpenseIndexQueryResult buildExpense(
			UUID id,
			UUID propertyId,
			LocalDate expenseDate,
			String category,
			String amount,
			String description
	) {
		return new ExpenseIndexQueryResult() {
			@Override
			public UUID getId() {
				return id;
			}

			@Override
			public UUID getPropertyId() {
				return propertyId;
			}

			@Override
			public LocalDate getExpenseDate() {
				return expenseDate;
			}

			@Override
			public String getCategory() {
				return category;
			}

			@Override
			public String getAmount() {
				return amount;
			}

			@Override
			public String getDescription() {
				return description;
			}
		};
	}
}
