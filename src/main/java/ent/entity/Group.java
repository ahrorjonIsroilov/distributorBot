package ent.entity;

import ent.entity.Auditable;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "groups")
public class Group extends Auditable {
    @Column(name = "group_id")
    private Long groupId;
    private String title;
    private Boolean accepted;
}
