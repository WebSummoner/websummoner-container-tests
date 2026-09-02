package com.websummoner.websummoner.misc;

public enum Page {
    FIRST("first.html"),
    SECOND("second.html"),
    HOTKEYS("hotkeys.html"),
    ALERT("alert.html"),
    FRAMES("frames.html"),
    DRAG("drag.html"),
    UPLOAD("upload.html");

    private final String name;

    Page(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
