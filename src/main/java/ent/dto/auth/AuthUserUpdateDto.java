package ent.dto.auth;

import ent.dto.GenericDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthUserUpdateDto extends GenericDto {
    private String username;
    private String firstname;
    private String lastname;

    @Builder
    public AuthUserUpdateDto(Long id, String username, String firstname, String lastname) {
        super(id);
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
    }
}
