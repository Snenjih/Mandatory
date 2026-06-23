package de.snenjih.mandatory.menu;

import java.nio.file.Path;

public record ScreenshotEntry(Path path, String filename, long lastModified) {}
