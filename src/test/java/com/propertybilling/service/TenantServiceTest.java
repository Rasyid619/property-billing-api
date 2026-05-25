package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.propertybilling.dto.tenant.TenantCreateRequest;
import com.propertybilling.dto.tenant.TenantIndexResponse;
import com.propertybilling.dto.tenant.queryresult.TenantIndexQueryResult;
import com.propertybilling.entity.Tenant;
import com.propertybilling.repository.TenantRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;

/*
 * Unit tests for tenant business workflows.
 */
class TenantServiceTest {

	private final TenantRepository tenantRepository = Mockito.mock(TenantRepository.class);
	private final TenantService tenantService = new TenantService(tenantRepository);

	@Nested
	/*
	 * Service tests for creating tenants.
	 */
	class CreateTenant {

		@Test
		void createsTenantDataRecord() {
			ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);

			tenantService.createTenant(new TenantCreateRequest(
					"Budi",
					"08123456789",
					"budi@example.com"
			));

			verify(tenantRepository, times(1)).save(tenantCaptor.capture());
			assertThat(tenantCaptor.getValue().getId()).isNotNull();
			assertThat(tenantCaptor.getValue().getName()).isEqualTo("Budi");
			assertThat(tenantCaptor.getValue().getPhone()).isEqualTo("08123456789");
			assertThat(tenantCaptor.getValue().getEmail()).isEqualTo("budi@example.com");
			assertThat(tenantCaptor.getValue().getCreatedAt()).isNull();
			assertThat(tenantCaptor.getValue().getUpdatedAt()).isNull();
		}
	}

	@Nested
	/*
	 * Service tests for listing tenants.
	 */
	class IndexTenants {

		@Test
		void listsTenants() {
			TenantIndexQueryResult tenant = buildTenant(
					"00000000-0000-0000-0000-000000000301",
					"Budi",
					"08123456789",
					"budi@example.com"
			);
			when(tenantRepository.findIndex(Mockito.isNull(), Mockito.any(Pageable.class)))
					.thenReturn(List.of(tenant));

			TenantIndexResponse response = tenantService.listTenants(0, 100, null);

			assertThat(response.count()).isEqualTo(1);
			assertThat(response.tenants()).hasSize(1);
			assertThat(response.tenants().getFirst().id())
					.isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000301"));
			assertThat(response.tenants().getFirst().name()).isEqualTo("Budi");
			assertThat(response.tenants().getFirst().phone()).isEqualTo("08123456789");
			assertThat(response.tenants().getFirst().email()).isEqualTo("budi@example.com");
			verify(tenantRepository, times(1)).findIndex(Mockito.isNull(), Mockito.any(Pageable.class));
		}

		@Test
		void formatsSearchPatternBeforeListingTenants() {
			when(tenantRepository.findIndex(Mockito.eq("%budi%"), Mockito.any(Pageable.class)))
					.thenReturn(List.of());

			TenantIndexResponse response = tenantService.listTenants(25, 25, "  budi  ");

			assertThat(response.count()).isZero();
			assertThat(response.tenants()).isEmpty();
			verify(tenantRepository, times(1)).findIndex(Mockito.eq("%budi%"), Mockito.any(Pageable.class));
		}

		@Test
		void treatsBlankSearchAsNoSearch() {
			when(tenantRepository.findIndex(Mockito.isNull(), Mockito.any(Pageable.class)))
					.thenReturn(List.of());

			TenantIndexResponse response = tenantService.listTenants(0, 100, "   ");

			assertThat(response.count()).isZero();
			assertThat(response.tenants()).isEmpty();
			verify(tenantRepository, times(1)).findIndex(Mockito.isNull(), Mockito.any(Pageable.class));
		}

		@Test
		void passesPageRequestToRepository() {
			when(tenantRepository.findIndex(Mockito.isNull(), Mockito.any(Pageable.class)))
					.thenReturn(List.of());
			ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

			tenantService.listTenants(200, 100, null);

			verify(tenantRepository, times(1)).findIndex(Mockito.isNull(), pageableCaptor.capture());
			assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
			assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
		}
	}

	private TenantIndexQueryResult buildTenant(
			String id,
			String name,
			String phone,
			String email
	) {
		return new TenantIndexQueryResult(UUID.fromString(id), name, phone, email);
	}
}
