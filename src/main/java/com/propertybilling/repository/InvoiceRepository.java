package com.propertybilling.repository;

import com.propertybilling.entity.Invoice;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Data access boundary for invoice records.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
}
