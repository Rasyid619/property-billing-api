package com.propertybilling.service;

import com.propertybilling.dto.common.StatusFilter;
import com.propertybilling.dto.unit.UnitIndexElement;
import com.propertybilling.dto.unit.UnitIndexResponse;
import com.propertybilling.dto.unit.queryresult.UnitIndexQueryResult;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.UnitRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
