package ent.entity;

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
@Entity
public class Product extends Auditable {
    private String name;
    private int count;
    @ManyToOne
    @JoinColumn(name = "deliver_id")
    private Deliver deliver;
}
