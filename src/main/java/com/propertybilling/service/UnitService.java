package com.propertybilling.service;

import com.propertybilling.dto.common.StatusFilter;
import com.propertybilling.dto.unit.UnitCreateRequest;
import com.propertybilling.dto.unit.UnitIndexElement;
import com.propertybilling.dto.unit.UnitIndexResponse;
import com.propertybilling.dto.unit.queryresult.UnitIndexQueryResult;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.Unit;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.exception.UnitNumberConflictException;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.UnitRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for unit management.
 */
public class UnitService {

	private final PropertyRepository propertyRepository;
	private final UnitRepository unitRepository;

	/**
	 * Creates an active unit inside one property.
	 *
	 * @param propertyId owning property identifier
	 * @param request unit data to persist
	 * @throws PropertyNotFoundException when no property exists for the ID
	 * @throws UnitNumberConflictException when the unit number already exists inside the property
	 */
	public void createUnit(UUID propertyId, UnitCreateRequest request) {
		Property property = propertyRepository.findById(propertyId)
				.orElseThrow(PropertyNotFoundException::new);

		if (unitRepository.existsByPropertyIdAndUnitNumber(propertyId, request.unitNumber())) {
			throw new UnitNumberConflictException();
		}

		try {
			unitRepository.save(new Unit(
					UUID.randomUUID(),
					property,
					request.unitNumber(),
					toMonthlyFeeValue(request.monthlyFee()),
					request.dueDay(),
					true
			));
		} catch (DataIntegrityViolationException exception) {
			throw new UnitNumberConflictException();
		}
	}

	/**
	 * Lists units that belong to one property.
	 *
	 * @param propertyId owning property identifier
	 * @param offset number of matching units to skip
	 * @param limit maximum number of units to return
	 * @param status optional active-state filter
	 * @return unit index response
	 * @throws PropertyNotFoundException when no property exists for the ID
	 */
	public UnitIndexResponse listUnitsByProperty(UUID propertyId, int offset, int limit, String status) {
		if (!propertyRepository.existsById(propertyId)) {
			throw new PropertyNotFoundException();
		}

		Optional<StatusFilter> statusFilter = StatusFilter.fromQueryValue(status);

		if (hasUnsupportedStatus(status, statusFilter)) {
			return emptyIndexResponse();
		}

		Boolean active = statusFilter.map(StatusFilter::isActive).orElse(null);
		List<UnitIndexElement> units = unitRepository.findIndexByPropertyId(
				propertyId,
				active,
				PageRequest.of(offset / limit, limit)
		)
				.stream()
				.map(this::toIndexElement)
				.toList();

		return new UnitIndexResponse(units.size(), units);
	}

	private boolean hasUnsupportedStatus(String status, Optional<StatusFilter> statusFilter) {
		return !StatusFilter.isUnset(status) && statusFilter.isEmpty();
	}

	private UnitIndexResponse emptyIndexResponse() {
		return new UnitIndexResponse(0, List.of());
	}

	private String toMonthlyFeeValue(String monthlyFee) {
		return new BigDecimal(monthlyFee).toPlainString();
	}

	private UnitIndexElement toIndexElement(UnitIndexQueryResult unit) {
		return new UnitIndexElement(
				unit.id(),
				unit.propertyId(),
				unit.unitNumber(),
				new BigDecimal(unit.monthlyFee()),
				unit.dueDay(),
				unit.active()
		);
	}
}
