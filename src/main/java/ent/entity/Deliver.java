package ent.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Deliver extends Auditable {
    private String username;
    @OneToMany(mappedBy = "deliver")
    private List<Product> products;
    private int allProductCount;
}
