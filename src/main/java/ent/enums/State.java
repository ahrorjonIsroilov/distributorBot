package ent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum State {
    ADD_DIS("add_dis"),
    EDIT_PRODUCT("edit_product"),
    ADD_ADMIN("add_admin"),
    UPLOAD_FORM("upload_form"),
    DEFAULT("default"),
    LOAD_HISTORY("load_history");
    private final String code;
}

