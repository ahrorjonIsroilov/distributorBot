package ent.entity.auth;

import ent.enums.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionUser {
    private Long chatId;
    private Role role;
    private String state;
    private Integer page;
    private String tempString;
    private Long tempLong;
}
