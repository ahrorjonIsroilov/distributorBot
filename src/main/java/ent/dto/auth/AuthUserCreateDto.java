package ent.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AuthUserCreateDto {
    private Long chatId;
    private String username;
    private String firstname;
    private String lastname;

}
