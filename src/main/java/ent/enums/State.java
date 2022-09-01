package ent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum State {
    ADD_DIS("add_dis"),
    ADD_ADMIN("add_admin"),
    DEFAULT("default");
    private final String code;
}

