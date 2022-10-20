package ent.entity;

import ent.entity.product.Product;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "delivers")
public class Deliver extends Auditable {
    private String username;
    private int productCount;
    private double percent;
    private boolean presentInProduct;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @Column(name = "col_index")
    private int colIndex;
}
