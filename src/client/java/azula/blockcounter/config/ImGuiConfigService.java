package azula.blockcounter.config;

import java.io.*;
import java.nio.ByteBuffer;

public class ImGuiConfigService {

    private static String configDir = "config" + File.separator + "BlockCounter";
    private static String configFileName = "block-counter-imgui.ini";

    public static ImGuiConfig loadConfigFromDisk() throws IOException {

        try {
            File configDirectory = new File(configDir);

            // Make the directory if new
            if (!configDirectory.exists()) configDirectory.mkdirs();

            File configFile = new File(configDir + File.separator + configFileName);

            try (FileInputStream fileIn = new FileInputStream(configFile)) {
                ByteBuffer fileBuffer = ByteBuffer.wrap(fileIn.readAllBytes());

                float[] accent = new float[4];
                float[] text = new float[4];
                float[] background = new float[4];

                // Accent
                accent[0] = fileBuffer.getFloat();
                accent[1] = fileBuffer.getFloat();
                accent[2] = fileBuffer.getFloat();
                accent[3] = fileBuffer.getFloat();

                // Text
                text[0] = fileBuffer.getFloat();
                text[1] = fileBuffer.getFloat();
                text[2] = fileBuffer.getFloat();
                text[3] = fileBuffer.getFloat();

                // Background
                background[0] = fileBuffer.getFloat();
                background[1] = fileBuffer.getFloat();
                background[2] = fileBuffer.getFloat();
                background[3] = fileBuffer.getFloat();

                return new ImGuiConfig(accent, text, background);
            }


        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new IOException("Failed to load Block-Counter ImGui Config File: \n" + e.getMessage());
        }
    }

    public static void saveConfigToDisk(ImGuiConfig config) throws IOException {

        try {
            File configDirectory = new File(configDir);

            // Make the directory if new
            if (!configDirectory.exists()) configDirectory.mkdirs();

            File configFile = new File(configDir + File.separator + configFileName);

            // Make the file if new
            if (!configFile.exists()) configFile.createNewFile();

            float[] accent = config.getAccentColor();
            float[] text = config.getTextColor();
            float[] background = config.getBackgroundColor();

            // float = 4 bytes * 4 floats per color * 3 colors + x,y floats
            ByteBuffer fileBuffer = ByteBuffer.allocate(56);

            // Accent
            fileBuffer.putFloat(accent[0]);
            fileBuffer.putFloat(accent[1]);
            fileBuffer.putFloat(accent[2]);
            fileBuffer.putFloat(accent[3]);

            // Text
            fileBuffer.putFloat(text[0]);
            fileBuffer.putFloat(text[1]);
            fileBuffer.putFloat(text[2]);
            fileBuffer.putFloat(text[3]);

            // Accent
            fileBuffer.putFloat(background[0]);
            fileBuffer.putFloat(background[1]);
            fileBuffer.putFloat(background[2]);
            fileBuffer.putFloat(background[3]);

            try (FileOutputStream fileOut = new FileOutputStream(configFile)) {
                fileOut.write(fileBuffer.array());
            }

        } catch (IOException | SecurityException e) {
            throw new IOException("Unable to create Block-Counter ImGui Config File: \n" + e.getMessage());
        }

    }
}
