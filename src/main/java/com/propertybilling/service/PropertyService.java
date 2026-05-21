package com.propertybilling.service;

import com.propertybilling.dto.property.PropertyCreateRequest;
import com.propertybilling.dto.property.PropertyIndexElement;
import com.propertybilling.dto.property.PropertyIndexResponse;
import com.propertybilling.dto.property.PropertyStatus;
import com.propertybilling.dto.property.queryresult.PropertyIndexQueryResult;
import com.propertybilling.entity.Property;
import com.propertybilling.helper.SearchPatternHelper;
import com.propertybilling.repository.PropertyRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for property management.
 */
public class PropertyService {

	private final PropertyRepository propertyRepository;

	/**
	 * Creates an active property.
	 *
	 * @param request property data to persist
	 */
	public void createProperty(PropertyCreateRequest request) {
		OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);
		propertyRepository.save(new Property(
				UUID.randomUUID(),
				request.name(),
				request.address(),
				true,
				timestamp,
				timestamp
		));
	}

	/**
	 * Lists properties using optional text search and offset pagination.
	 *
	 * @param offset number of matching properties to skip
	 * @param limit maximum number of properties to return
	 * @param search optional text matched against name and address
	 * @param status optional active-state filter
	 * @return property index response
	 */
	public PropertyIndexResponse listProperties(int offset, int limit, String search, String status) {
		Optional<PropertyStatus> propertyStatus = PropertyStatus.fromQueryValue(status);

		if (hasUnsupportedStatus(status, propertyStatus)) {
			return emptyIndexResponse();
		}

		Boolean active = propertyStatus.map(PropertyStatus::isActive).orElse(null);
		List<PropertyIndexElement> properties = propertyRepository.findIndex(
				SearchPatternHelper.containsPattern(search),
				active,
				PageRequest.of(offset / limit, limit)
		)
				.stream()
				.map(this::toIndexElement)
				.toList();

		return new PropertyIndexResponse(properties.size(), properties);
	}

	private boolean hasUnsupportedStatus(String status, Optional<PropertyStatus> propertyStatus) {
		return status != null && !status.isBlank() && propertyStatus.isEmpty();
	}

	private PropertyIndexResponse emptyIndexResponse() {
		return new PropertyIndexResponse(0, List.of());
	}

	private PropertyIndexElement toIndexElement(PropertyIndexQueryResult property) {
		return new PropertyIndexElement(
				property.id(),
				property.name(),
				property.address(),
				property.active()
		);
	}
}
