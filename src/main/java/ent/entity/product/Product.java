package ent.entity.product;

import ent.entity.Auditable;
import ent.entity.Deliver;
import ent.entity.Template;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "products")
public class Product extends Auditable {
    private String name;
    private double count;
    private double newCount;
    private double totalCount;
    private boolean edited;
    private String day;
    @ManyToOne
    @JoinColumn(name = "template_id")
    private Template template;
    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Deliver> delivers = new ArrayList<>();
    @Column(name = "row_index")
    private int rowIndex;


}
