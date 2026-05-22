package com.propertybilling.service;

import com.propertybilling.dto.common.StatusFilter;
import com.propertybilling.dto.property.PropertyCreateRequest;
import com.propertybilling.dto.property.PropertyIndexElement;
import com.propertybilling.dto.property.PropertyIndexResponse;
import com.propertybilling.dto.property.PropertyShowResponse;
import com.propertybilling.dto.property.PropertyUpdateRequest;
import com.propertybilling.dto.property.queryresult.PropertyIndexQueryResult;
import com.propertybilling.entity.Property;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.helper.SearchPatternHelper;
import com.propertybilling.repository.PropertyRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
		propertyRepository.save(new Property(
				UUID.randomUUID(),
				request.name(),
				request.address(),
				true
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
		Optional<StatusFilter> statusFilter = StatusFilter.fromQueryValue(status);

		if (hasUnsupportedStatus(status, statusFilter)) {
			return emptyIndexResponse();
		}

		Boolean active = statusFilter.map(StatusFilter::isActive).orElse(null);
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

	/**
	 * Returns one property by ID.
	 *
	 * @param propertyId property identifier
	 * @return property detail response
	 * @throws PropertyNotFoundException when no property exists for the ID
	 */
	public PropertyShowResponse getProperty(UUID propertyId) {
		return propertyRepository.findById(propertyId)
				.map(this::toShowResponse)
				.orElseThrow(PropertyNotFoundException::new);
	}

	/**
	 * Marks one property inactive using a row lock.
	 *
	 * @param propertyId property identifier
	 * @throws PropertyNotFoundException when no property exists for the ID
	 */
	@Transactional
	public void deactivateProperty(UUID propertyId) {
		Property property = propertyRepository.findByIdForUpdate(propertyId)
				.orElseThrow(PropertyNotFoundException::new);

		property.deactivate();
		propertyRepository.save(property);
	}

	/**
	 * Marks one property active using a row lock.
	 *
	 * @param propertyId property identifier
	 * @throws PropertyNotFoundException when no property exists for the ID
	 */
	@Transactional
	public void activateProperty(UUID propertyId) {
		Property property = propertyRepository.findByIdForUpdate(propertyId)
				.orElseThrow(PropertyNotFoundException::new);

		property.activate();
		propertyRepository.save(property);
	}

	/**
	 * Updates the name and address of one property using a row lock.
	 *
	 * @param propertyId property identifier
	 * @param request new property data
	 * @throws PropertyNotFoundException when no property exists for the ID
	 */
	@Transactional
	public void updateProperty(UUID propertyId, PropertyUpdateRequest request) {
		Property property = propertyRepository.findByIdForUpdate(propertyId)
				.orElseThrow(PropertyNotFoundException::new);

		property.update(request.name(), request.address());
		propertyRepository.save(property);
	}

	private boolean hasUnsupportedStatus(String status, Optional<StatusFilter> statusFilter) {
		return !StatusFilter.isUnset(status) && statusFilter.isEmpty();
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

	private PropertyShowResponse toShowResponse(Property property) {
		return new PropertyShowResponse(
				property.getId(),
				property.getName(),
				property.getAddress(),
				property.isActive()
		);
	}
}
