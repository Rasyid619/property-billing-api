package com.propertybilling.service;

import com.propertybilling.dto.tenant.TenantCreateRequest;
import com.propertybilling.dto.tenant.TenantIndexElement;
import com.propertybilling.dto.tenant.TenantIndexResponse;
import com.propertybilling.dto.tenant.TenantShowResponse;
import com.propertybilling.dto.tenant.TenantUpdateRequest;
import com.propertybilling.dto.tenant.queryresult.TenantIndexQueryResult;
import com.propertybilling.entity.Tenant;
import com.propertybilling.exception.TenantContactConflictException;
import com.propertybilling.exception.TenantNotFoundException;
import com.propertybilling.helper.SearchPatternHelper;
import com.propertybilling.repository.TenantRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

		return new TenantIndexResponse(tenants);
	}

	/**
	 * Returns one tenant data record by ID.
	 *
	 * @param tenantId tenant identifier
	 * @return tenant detail response
	 * @throws TenantNotFoundException when no tenant exists for the ID
	 */
	public TenantShowResponse getTenant(UUID tenantId) {
		return tenantRepository.findById(tenantId)
				.map(this::toShowResponse)
				.orElseThrow(TenantNotFoundException::new);
	}

	/**
	 * Replaces one tenant data record using a row lock.
	 *
	 * @param tenantId tenant identifier
	 * @param request updated tenant data
	 * @throws TenantNotFoundException when no tenant exists for the ID
	 * @throws TenantContactConflictException when another tenant already uses the phone or email
	 */
	@Transactional
	public void updateTenant(UUID tenantId, TenantUpdateRequest request) {
		Tenant tenant = tenantRepository.findByIdForUpdate(tenantId)
				.orElseThrow(TenantNotFoundException::new);

		if (hasDuplicatePhone(request.phone(), tenantId) || hasDuplicateEmail(request.email(), tenantId)) {
			throw new TenantContactConflictException();
		}

		try {
			tenant.update(request.name(), request.phone(), request.email());
			tenantRepository.save(tenant);
		} catch (DataIntegrityViolationException exception) {
			throw new TenantContactConflictException();
		}
	}

	private boolean hasDuplicatePhone(String phone) {
		return phone != null && tenantRepository.existsByPhone(phone);
	}

	private boolean hasDuplicateEmail(String email) {
		return email != null && tenantRepository.existsByEmail(email);
	}

	private boolean hasDuplicatePhone(String phone, UUID tenantId) {
		return phone != null && tenantRepository.existsByPhoneAndIdNot(phone, tenantId);
	}

	private boolean hasDuplicateEmail(String email, UUID tenantId) {
		return email != null && tenantRepository.existsByEmailAndIdNot(email, tenantId);
	}

	private TenantIndexElement toIndexElement(TenantIndexQueryResult tenant) {
		return new TenantIndexElement(
				tenant.id(),
				tenant.name(),
				tenant.phone(),
				tenant.email()
		);
	}

	private TenantShowResponse toShowResponse(Tenant tenant) {
		return new TenantShowResponse(
				tenant.getId(),
				tenant.getName(),
				tenant.getPhone(),
				tenant.getEmail()
		);
	}
}
