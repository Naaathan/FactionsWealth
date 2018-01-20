package net.kyuzi.factionswealth.storage;

public enum StorageType {

    MYSQL("mysql"),
    YAML("file", "yaml", "yml");

    public static final StorageType DEFAULT = YAML;
    private String[] identifiers;

    StorageType(String... identifiers) {
        this.identifiers = identifiers;
    }

    public static StorageType getStorageType(String identifier) {
        if (identifier != null) {
            for (StorageType type : values()) {
                for (String typeIdentifier : type.identifiers) {
                    if (identifier.equalsIgnoreCase(typeIdentifier)) {
                        return type;
                    }
                }
            }
        }

        return DEFAULT;
    }

}
