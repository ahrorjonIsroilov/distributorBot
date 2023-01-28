package ent.entity.auth;

import ent.enums.Role;
import ent.enums.State;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionUser {
    private Long chatId;
    private Role role;
    private Boolean blocked;
    private State state;
    private Integer page;
    private String tempString;
    private String exclusion;
    private LocalDateTime date;
    private Long tempLong;
}
