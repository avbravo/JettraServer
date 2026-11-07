package io.jettra.server.backend.service;

import io.jettra.server.backend.model.Customer;
import io.jettra.server.core.storage.JettraStorageEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio para gestión y persistencia de Clientes respaldado por el motor de almacenamiento de JettraServer.
 */
@ApplicationScoped
public class CustomerService {

    private final Map<Long, Customer> customerMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(100);

    public CustomerService() {
        initSampleData();
    }

    private void initSampleData() {
        save(new Customer(1L, "Acme Corporation", "contact@acme.corp", "Acme Global", "ENTERPRISE", true));
        save(new Customer(2L, "Stark Industries", "tony@stark.tech", "Stark Tech", "PREMIUM", true));
        save(new Customer(3L, "Wayne Enterprises", "bruce@wayne.org", "Wayne Holdings", "ENTERPRISE", true));
        save(new Customer(4L, "Cyberdyne Systems", "miles@cyberdyne.ai", "Cyberdyne AI", "STANDARD", true));
    }

    public List<Customer> findAll() {
        return new ArrayList<>(customerMap.values());
    }

    public Optional<Customer> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(customerMap.get(id));
    }

    public Customer save(Customer customer) {
        if (customer.id() == null || customer.id() <= 0) {
            customer = new Customer(
                    idGenerator.incrementAndGet(),
                    customer.name(),
                    customer.email(),
                    customer.company(),
                    customer.tier() != null ? customer.tier() : "STANDARD",
                    customer.active()
            );
        }
        customerMap.put(customer.id(), customer);
        return customer;
    }

    public boolean delete(Long id) {
        if (id == null) return false;
        return customerMap.remove(id) != null;
    }

    public long count() {
        return customerMap.size();
    }
}

