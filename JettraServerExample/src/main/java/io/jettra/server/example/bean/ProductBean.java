package io.jettra.server.example.bean;

import io.jettra.server.example.model.Product;
import io.jettra.server.example.service.ProductService;
import io.jettra.server.jakarta.cdi.CdiAnnotations.Inject;
import io.jettra.server.jakarta.cdi.CdiAnnotations.Named;
import io.jettra.server.jakarta.cdi.CdiAnnotations.RequestScoped;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Managed Bean para la gestión de productos en PrimeFaces DataTable.
 */
@Named("productBean")
@RequestScoped
public class ProductBean implements Serializable {

    @Inject
    private ProductService productService;

    private Product newProduct = new Product();
    private Product selectedProduct;
    private String statusMessage;

    public ProductBean() {}

    public ProductBean(ProductService productService) {
        this.productService = productService;
    }

    public List<Product> getProducts() {
        if (productService != null) {
            return productService.listAll();
        }
        return Collections.emptyList();
    }

    public void save() {
        if (productService != null && newProduct != null && newProduct.getId() != null) {
            productService.save(newProduct);
            statusMessage = "Producto guardado exitosamente: " + newProduct.getName();
            newProduct = new Product();
        }
    }

    public void delete(String id) {
        if (productService != null && id != null) {
            boolean ok = productService.delete(id);
            if (ok) {
                statusMessage = "Producto eliminado: " + id;
            }
        }
    }

    public Product getNewProduct() { return newProduct; }
    public void setNewProduct(Product newProduct) { this.newProduct = newProduct; }

    public Product getSelectedProduct() { return selectedProduct; }
    public void setSelectedProduct(Product selectedProduct) { this.selectedProduct = selectedProduct; }

    public String getStatusMessage() { return statusMessage; }
}

