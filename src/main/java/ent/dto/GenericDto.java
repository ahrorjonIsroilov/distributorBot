package ent.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GenericDto implements Dto{

    private Long id;

    public GenericDto(Long id) {
        this.id = id;
    }

}
