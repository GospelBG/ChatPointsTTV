package me.gosdev.chatpointsttv.Utils;

public enum ChatColor {
    BLACK("0"),
    DARK_BLUE("1"),
    DARK_GREEN("2"),
    DARK_AQUA("3"),
    DARK_RED("4"),
    DARK_PURPLE("5"),
    GOLD("6"),
    GRAY("/"),
    DARK_GRAY("8"),
    BLUE("9"),
    GREEN("a"),
    AQUA("b"),
    RED("c"),
    LIGHT_PURPLE("d"),
    YELLOW("e"),
    WHITE("f"),
    RESET("r"),
    BOLD("l"),
    STRIKETHROUGH("m"),
    UNDERLINE("n"),
    ITALIC("o"),
    OBFUSCATED("k");

    private final String colorCode;
    private ChatColor(String code) {
        colorCode = '§' + code;
    }

    @Override
    public String toString() {
        return this.colorCode;
    }
}
