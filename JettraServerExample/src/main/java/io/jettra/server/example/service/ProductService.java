package io.jettra.server.example.service;

import io.jettra.server.example.model.Product;
import io.jettra.server.jakarta.cdi.CdiAnnotations.ApplicationScoped;
import io.jettra.server.jakarta.cdi.CdiAnnotations.Inject;
import io.jettra.server.jakarta.persistence.EntityManager;
import io.jettra.server.jakarta.persistence.EntityManagerFactory;
import io.jettra.server.jakarta.transaction.TransactionAnnotations.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de productos que demuestra operaciones JPA y transaccionalidad JTA.
 */
@ApplicationScoped
public class ProductService {

    @Inject
    private EntityManagerFactory emf;

    private final List<String> registeredProductIds = new ArrayList<>();

    public ProductService() {}

    public ProductService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void initSampleCatalog() {
        EntityManager em = getEntityManager();
        addProduct(em, new Product("PRD-01", "Servidor JettraEdge 1U", "Hardware", 2499.00, 15));
        addProduct(em, new Product("PRD-02", "Licencia JettraServer Enterprise", "Software", 899.00, 100));
        addProduct(em, new Product("PRD-03", "Módulo de Aceleración Off-Heap", "Add-on", 450.00, 50));
        addProduct(em, new Product("PRD-04", "Switch Óptico 100Gbps QSFP28", "Redes", 1850.00, 8));
        addProduct(em, new Product("PRD-05", "Unidad NVMe PCIe 5.0 4TB", "Almacenamiento", 520.00, 30));
    }

    private void addProduct(EntityManager em, Product p) {
        em.persist(p);
        if (!registeredProductIds.contains(p.getId())) {
            registeredProductIds.add(p.getId());
        }
    }

    @Transactional
    public List<Product> listAll() {
        EntityManager em = getEntityManager();
        List<Product> list = new ArrayList<>();
        for (String id : registeredProductIds) {
            Product p = em.find(Product.class, id);
            if (p != null) {
                list.add(p);
            }
        }
        return list;
    }

    @Transactional
    public Product save(Product p) {
        EntityManager em = getEntityManager();
        em.persist(p);
        if (!registeredProductIds.contains(p.getId())) {
            registeredProductIds.add(p.getId());
        }
        return p;
    }

    @Transactional
    public boolean delete(String id) {
        EntityManager em = getEntityManager();
        Product p = em.find(Product.class, id);
        if (p != null) {
            em.remove(p);
            registeredProductIds.remove(id);
            return true;
        }
        return false;
    }

    public Product findById(String id) {
        return getEntityManager().find(Product.class, id);
    }

    private EntityManager getEntityManager() {
        if (emf != null) {
            return emf.createEntityManager();
        }
        throw new IllegalStateException("EntityManagerFactory no disponible");
    }
}

