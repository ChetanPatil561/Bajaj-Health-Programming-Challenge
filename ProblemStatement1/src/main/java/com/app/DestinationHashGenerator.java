package com.app;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class DestinationHashGenerator {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN Number> <Path to JSON file>");
            return;
        }

        String prnNumber = args[0].toLowerCase();
        String jsonFilePath = args[1];

        String destinationValue = getDestinationValue(jsonFilePath);
        if (destinationValue == null) {
            System.out.println("Key 'destination' not found in the JSON file.");
            return;
        }

        String randomString = generateRandomString(8);
        String concatenatedString = prnNumber + destinationValue + randomString;

        String md5Hash = generateMD5Hash(concatenatedString);

        System.out.println(md5Hash + ";" + randomString);
    }

    private static String getDestinationValue(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JSONTokener tokener = new JSONTokener(reader);
            Object json = tokener.nextValue();
            return findDestinationValue(json);
        } catch (IOException e) {
            System.err.println("Error reading the JSON file: " + e.getMessage());
            return null;
        }
    }

    private static String findDestinationValue(Object json) {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            for (String key : jsonObject.keySet()) {
                if (key.equals("destination")) {
                    return jsonObject.getString(key);
                } else {
                    Object value = jsonObject.get(key);
                    String result = findDestinationValue(value);
                    if (result != null) {
                        return result;
                    }
                }
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (Object item : jsonArray) {
                String result = findDestinationValue(item);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error generating MD5 hash: " + e.getMessage());
            return null;
        }
    }
}
