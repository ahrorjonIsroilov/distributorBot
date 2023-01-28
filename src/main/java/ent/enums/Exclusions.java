package ent.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Exclusions {
    MAKRO("makro"),
    NAMANGAN("namangan");
    private final String val;
}
