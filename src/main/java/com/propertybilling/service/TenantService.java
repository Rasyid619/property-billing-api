package com.propertybilling.service;

import com.propertybilling.dto.tenant.TenantIndexElement;
import com.propertybilling.dto.tenant.TenantIndexResponse;
import com.propertybilling.dto.tenant.queryresult.TenantIndexQueryResult;
import com.propertybilling.helper.SearchPatternHelper;
import com.propertybilling.repository.TenantRepository;
import java.util.List;
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

	private TenantIndexElement toIndexElement(TenantIndexQueryResult tenant) {
		return new TenantIndexElement(
				tenant.id(),
				tenant.name(),
				tenant.phone(),
				tenant.email()
		);
	}
}
