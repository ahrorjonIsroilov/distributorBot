package ent.entity.product;

import ent.entity.Auditable;
import ent.entity.Deliver;
import ent.entity.Template;
import ent.enums.Exclusions;
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

    public List<Deliver> deliversWithoutExclusions() {
        List<Deliver> result = new ArrayList<>();
        for (Deliver deliver : this.delivers)
            if (!deliver.getUsername().equalsIgnoreCase(Exclusions.NAMANGAN.getVal()) && !deliver.getUsername().equalsIgnoreCase(Exclusions.MAKRO.getVal()))
                result.add(deliver);
        return result;
    }

    public List<Deliver> exclusionDelivers() {
        List<Deliver> result = new ArrayList<>();
        for (Deliver deliver : this.delivers)
            for (Exclusions value : Exclusions.values())
                if (deliver.getUsername().equalsIgnoreCase(value.getVal()))
                    result.add(deliver);
        return result;
    }


}
