package com.github.eybv.jhttp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MultipartBody {

    private final List<Part> parts = new ArrayList<>();

    public void addPart(String type, String name, String filename, byte[] data) {
        parts.add(new Part(type, name, filename, data));
    }

    public List<Part> getParts() {
        return List.of(parts.toArray(Part[]::new));
    }

    public static class Part {

        private final String type;

        private final String name;

        private final String filename;

        private final byte[] data;

        private Part(String type, String name, String filename, byte[] data) {
            this.type = type;
            this.name = name;
            this.filename = filename;
            this.data = data;
        }

        public String getType() {
            return Optional.ofNullable(type).orElse("text/plain");
        }

        public String getName() {
            return name;
        }

        public Optional<String> getFilename() {
            return Optional.ofNullable(filename);
        }

        public byte[] getData() {
            return data;
        }

    }

}
