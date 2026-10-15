package com.flightreservation.loyalty;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LoyaltyService {
    private final Path dataFile;
    private final ObjectMapper objectMapper;

    public LoyaltyService(Path dataDirectory) {
        this.objectMapper = new ObjectMapper();
        this.dataFile = dataDirectory.resolve("loyalty.json");
        try {
            Files.createDirectories(dataDirectory);
            if (Files.notExists(dataFile)) {
                Files.writeString(dataFile, "[]");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize loyalty storage", e);
        }
    }

    public LoyaltyProfile savePreference(String passengerName, String mealPreference) {
        List<LoyaltyProfile> profiles = loadProfiles();
        LoyaltyProfile profile = profiles.stream()
                .filter(p -> p.getPassengerName().equalsIgnoreCase(passengerName))
                .findFirst()
                .orElseGet(() -> new LoyaltyProfile(passengerName, null, 0, LoyaltyStatus.ACTIVE));

        profile.savePreference(mealPreference);

        if (!profiles.contains(profile)) {
            profiles.add(profile);
        }

        saveProfiles(profiles);
        return profile;
    }

    public List<LoyaltyProfile> getProfiles() {
        return loadProfiles();
    }

    public LoyaltyProfile findByPassengerName(String passengerName) {
        return loadProfiles().stream()
                .filter(p -> p.getPassengerName().equalsIgnoreCase(passengerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No loyalty profile for passenger: " + passengerName));
    }

    private List<LoyaltyProfile> loadProfiles() {
        try {
            return objectMapper.readValue(dataFile.toFile(), new TypeReference<List<LoyaltyProfile>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Could not read loyalty file", e);
        }
    }

    private void saveProfiles(List<LoyaltyProfile> profiles) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), profiles);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write loyalty file", e);
        }
    }
}
