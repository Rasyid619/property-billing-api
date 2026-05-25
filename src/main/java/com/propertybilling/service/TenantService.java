package com.propertybilling.service;

import com.propertybilling.dto.tenant.TenantCreateRequest;
import com.propertybilling.dto.tenant.TenantIndexElement;
import com.propertybilling.dto.tenant.TenantIndexResponse;
import com.propertybilling.dto.tenant.queryresult.TenantIndexQueryResult;
import com.propertybilling.entity.Tenant;
import com.propertybilling.exception.TenantContactConflictException;
import com.propertybilling.helper.SearchPatternHelper;
import com.propertybilling.repository.TenantRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for tenant management.
 */
public class TenantService {

	private final TenantRepository tenantRepository;

	/**
	 * Creates a tenant data record without creating a login account.
	 *
	 * @param request tenant data to persist
	 */
	public void createTenant(TenantCreateRequest request) {
		if (hasDuplicatePhone(request.phone()) || hasDuplicateEmail(request.email())) {
			throw new TenantContactConflictException();
		}

		try {
			tenantRepository.save(new Tenant(
					UUID.randomUUID(),
					request.name(),
					request.phone(),
					request.email()
			));
		} catch (DataIntegrityViolationException exception) {
			throw new TenantContactConflictException();
		}
	}

	/**
	 * Lists tenant data records using optional text search and offset pagination.
	 *
	 * @param offset number of matching tenants to skip
	 * @param limit maximum number of tenants to return
	 * @param search optional text matched against name, phone, and email
	 * @return tenant index response
	 */
	public TenantIndexResponse listTenants(int offset, int limit, String search) {
		List<TenantIndexElement> tenants = tenantRepository.findIndex(
				SearchPatternHelper.containsPattern(search),
				PageRequest.of(offset / limit, limit)
		)
				.stream()
				.map(this::toIndexElement)
				.toList();

		return new TenantIndexResponse(tenants.size(), tenants);
	}

	private boolean hasDuplicatePhone(String phone) {
		return phone != null && tenantRepository.existsByPhone(phone);
	}

	private boolean hasDuplicateEmail(String email) {
		return email != null && tenantRepository.existsByEmail(email);
	}

	private TenantIndexElement toIndexElement(TenantIndexQueryResult tenant) {
		return new TenantIndexElement(
				tenant.id(),
				tenant.name(),
				tenant.phone(),
				tenant.email()
		);
	}
}
