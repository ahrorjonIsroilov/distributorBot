package ent.entity.auth;

import ent.entity.Auditable;
import ent.entity.BaseEntity;
import ent.enums.Role;
import ent.enums.State;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class AuthUser extends Auditable implements BaseEntity {
    @Column(name = "chat_id", unique = true)
    private Long chatId;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(columnDefinition = "boolean default false")
    private Boolean registered;
    @Column(columnDefinition = "boolean default false")
    private Boolean blocked;
    @Enumerated(EnumType.STRING)
    private State state;
    private Integer page;

    @Builder
    public AuthUser(Long chatId, String username, String fullName, String phoneNumber, Role role, Boolean registered, State state, Boolean blocked, Integer page) {
        this.chatId = chatId;
        this.username = username;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.registered = registered;
        this.state = state;
        this.role = role;
        this.blocked = blocked;
        this.page = page;
    }
}
