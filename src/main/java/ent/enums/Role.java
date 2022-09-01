package ent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    OWNER("owner"),
    ADMIN("admin"),
    USER("user"),
    DISTRIBUTOR("storekeeper");
    private final String code;
}
