package com.propertybilling.repository;

import com.propertybilling.dto.invoice.queryresult.InvoiceIndexQueryResult;
import com.propertybilling.entity.Invoice;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.Unit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
/*
 * Query repository for invoice read models.
 */
public class InvoiceQueryRepository {

	private final EntityManager entityManager;

	/**
	 * Finds invoice index rows using optional query filters.
	 *
	 * @param propertyId optional owning property filter
	 * @param unitId optional unit filter
	 * @param tenantId optional tenant filter
	 * @param billingMonth optional billing month filter
	 * @param status optional invoice status filter
	 * @param pageable pagination settings
	 * @return matching rows ordered by newest billing month first
	 */
	public List<InvoiceIndexQueryResult> findIndex(
			UUID propertyId,
			UUID unitId,
			UUID tenantId,
			LocalDate billingMonth,
			String status,
			Pageable pageable
	) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<InvoiceIndexQueryResult> query = criteriaBuilder.createQuery(InvoiceIndexQueryResult.class);
		Root<Invoice> invoice = query.from(Invoice.class);
		Join<Invoice, Unit> unit = invoice.join("unit", JoinType.INNER);
		Join<Unit, Property> property = unit.join("property", JoinType.INNER);
		Join<Invoice, Tenant> tenant = invoice.join("tenant", JoinType.INNER);
		List<Predicate> predicates = buildPredicates(
				criteriaBuilder,
				invoice,
				unit,
				property,
				tenant,
				propertyId,
				unitId,
				tenantId,
				billingMonth,
				status
		);

		query.select(criteriaBuilder.construct(
				InvoiceIndexQueryResult.class,
				invoice.get("id"),
				unit.get("id"),
				tenant.get("id"),
				invoice.get("billingMonth"),
				invoice.get("invoiceNumber"),
				invoice.get("amount"),
				invoice.get("dueDate"),
				invoice.get("status")
		));
		query.where(predicates.toArray(Predicate[]::new));
		query.orderBy(
				criteriaBuilder.desc(invoice.get("billingMonth")),
				criteriaBuilder.asc(invoice.get("dueDate")),
				criteriaBuilder.asc(invoice.get("invoiceNumber")),
				criteriaBuilder.asc(invoice.get("id"))
		);

		TypedQuery<InvoiceIndexQueryResult> typedQuery = entityManager.createQuery(query);
		typedQuery.setFirstResult((int) pageable.getOffset());
		typedQuery.setMaxResults(pageable.getPageSize());

		return typedQuery.getResultList();
	}

	private List<Predicate> buildPredicates(
			CriteriaBuilder criteriaBuilder,
			Root<Invoice> invoice,
			Join<Invoice, Unit> unit,
			Join<Unit, Property> property,
			Join<Invoice, Tenant> tenant,
			UUID propertyId,
			UUID unitId,
			UUID tenantId,
			LocalDate billingMonth,
			String status
	) {
		List<Predicate> predicates = new ArrayList<>();

		if (propertyId != null) {
			predicates.add(criteriaBuilder.equal(property.get("id"), propertyId));
		}

		if (unitId != null) {
			predicates.add(criteriaBuilder.equal(unit.get("id"), unitId));
		}

		if (tenantId != null) {
			predicates.add(criteriaBuilder.equal(tenant.get("id"), tenantId));
		}

		if (billingMonth != null) {
			predicates.add(criteriaBuilder.equal(invoice.get("billingMonth"), billingMonth));
		}

		if (status != null) {
			predicates.add(criteriaBuilder.equal(invoice.get("status"), status));
		}

		return predicates;
	}
}
