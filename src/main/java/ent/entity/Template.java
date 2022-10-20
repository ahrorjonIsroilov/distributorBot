package ent.entity;

import ent.entity.product.Product;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "templates")
public class Template extends Auditable {
    private String date;
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "template", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Product> products;
    private boolean edited;
    private long allProductCount;
}
