package ent.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Emojis {
    CANCEL(""),
    CORRECT("✅"),
    EXCLAMATION("❗️"),
    REFRESH("\uD83D\uDD04"),
    SUN("\uD83D\uDD05"),
    INTERROBANG("⁉️"),
    BACK("\uD83D\uDD19");
    private final String emoji;

    public String emoji() {
        return emoji;
    }
}
