package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;

import java.util.Locale;
import java.util.Properties;

public class LanguageManager {
    private static Properties properties = new Properties();
    private static String currentLanguage = "es"; // Idioma predeterminado: Español

    public static void setLanguage(String languageCode) {
        currentLanguage = languageCode;
        loadLanguageFile();//TODO acabar idiomas

        // 🔹 Guardar en preferencias
        Preferences prefs = Gdx.app.getPreferences("GameSettings");
        prefs.putString("language", languageCode);
        prefs.flush();
    }


    private static void loadLanguageFile() {
        String filePath = "languages/" + currentLanguage + ".properties";
        FileHandle file = Gdx.files.internal(filePath);

        if (file.exists()) {
            try {
                properties.load(file.read());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String get(String key) {
        return properties.getProperty(key, key);
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }
}

