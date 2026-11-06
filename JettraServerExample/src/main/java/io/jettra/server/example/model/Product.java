package io.jettra.server.example.model;

import io.jettra.server.jakarta.persistence.PersistenceAnnotations.Entity;
import io.jettra.server.jakarta.persistence.PersistenceAnnotations.Id;
import io.jettra.server.jakarta.persistence.PersistenceAnnotations.Table;
import io.jettra.server.jakarta.validation.ValidationEngine.Min;
import io.jettra.server.jakarta.validation.ValidationEngine.NotNull;
import io.jettra.server.jakarta.validation.ValidationEngine.Size;
import java.io.Serializable;

/**
 * Entidad de Producto para la gestión del catálogo en el Dashboard.
 */
@Entity
@Table(name = "productos")
public class Product implements Serializable {

    @Id
    @NotNull(message = "El identificador del producto es requerido")
    private String id;

    @NotNull(message = "El nombre del producto no puede ser nulo")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    private String category;

    @Min(value = 0, message = "El precio debe ser positivo")
    private double price;

    @Min(value = 0, message = "El stock debe ser cero o positivo")
    private int stock;

    public Product() {}

    public Product(String id, String name, String category, double price, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
