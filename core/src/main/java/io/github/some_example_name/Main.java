package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;

import java.util.Locale;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends Game {

    private DatabaseInterface database;

    MenuScreen menuScreen;
    public SpriteBatch batch;
    private Music musicFondo;  // Variable para la música de fondo
    private Music musicGame;
    private Sound sonidoVictoria;  // Sonido de victoria
    private Sound sonidoDerrota;

    private boolean musicaActivada = true;  // Controlar si la música está activada


    private final float DURACION_VICTORIA = 3.7f;  // Ajusta esta duración según el sonido
    private final float DURACION_DERROTA = 0.7f;   // Ajusta esta duración según el sonido

    public float musicVolume;
    public float soundVolume;
    public boolean vibrationActive;
    public Preferences prefs; //guardar opciones


    public Main(DatabaseInterface database) {
        this.database = database;
    }

    public DatabaseInterface getDatabase() {
        return database;
    }

    @Override
    public void create() {
//        Preferences prefs = Gdx.app.getPreferences("GameSettings");
//        String savedLanguage = prefs.getString("language", "es");
//        LanguageManager.setLanguage(savedLanguage);
        prefs = Gdx.app.getPreferences("configuracion_juego");
        String savedLanguage = prefs.getString("language", "es");
        musicVolume = prefs.getFloat("volumen_musica", 1.0f);
        soundVolume = prefs.getFloat("volumen_sonido", 1.0f);
        vibrationActive = prefs.getBoolean("vibracion", true);

        LanguageManager.setLanguage(savedLanguage);
        batch = new SpriteBatch();
        menuScreen = new MenuScreen(this);
        setScreen(menuScreen);
        // Cargar la música de fondo
        musicFondo = Gdx.audio.newMusic(Gdx.files.internal("spritesEsqui/musica/Black Diamond.mp3"));
        musicGame = Gdx.audio.newMusic(Gdx.files.internal("spritesEsqui/musica/the_fun_run.mp3"));
        musicFondo.setLooping(true);  // Repetir la música
        musicGame.setLooping(true);
        if (musicaActivada) {
            musicFondo.play();
            musicGame.pause();
        } else {
            musicFondo.pause();
            musicGame.play();
        }

        sonidoVictoria = Gdx.audio.newSound(Gdx.files.internal("spritesEsqui/sonidos/Victory.mp3"));
        sonidoDerrota = Gdx.audio.newSound(Gdx.files.internal("spritesEsqui/sonidos/die1.mp3"));


//        String idioma = prefs.getString("idioma", Locale.getDefault().getLanguage());
//        cargarIdioma(idioma);

        //TODO ACABAR IDIOMAS

        // Aplicar volumen
        updateAudioSettings();


    }

//    public void cargarIdioma(String idioma) {
//        Locale locale;
//        switch (idioma) {
//            case "gl":
//                locale = new Locale("gl");
//                break;
//            case "en":
//                locale = new Locale("en");
//                break;
//            default:
//                locale = new Locale("es");
//                break;
//        }
//
//    }

    public void updateAudioSettings() {
        musicFondo.setVolume(musicVolume);
        musicGame.setVolume(musicVolume);
    }

    // Métodos para actualizar configuración desde el menú de opciones
    public void setMusicVolume(float volume) {
        this.musicVolume = volume;
        prefs.putFloat("volumen_musica", volume);
        prefs.flush();
        updateAudioSettings();
    }

    public void setSoundVolume(float volume) {
        this.soundVolume = volume;
        prefs.putFloat("volumen_sonido", volume);
        prefs.flush();
    }

    public void setVibrationEnabled(boolean enabled) {
        this.vibrationActive = enabled;
        prefs.putBoolean("vibracion", enabled);
        prefs.flush();
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public boolean isVibrationEnabled() {
        return vibrationActive;
    }

    @Override
    public void dispose() {
        super.dispose();
        menuScreen.dispose();
        // Liberar los recursos de la música cuando el juego termine
        if (musicFondo != null) {
            musicFondo.stop();
            musicFondo.dispose();
        }
        if (musicGame != null) {
            musicGame.stop();
            musicGame.dispose();
        }
        if (sonidoVictoria != null) {
            sonidoVictoria.dispose();
        }
        if (sonidoDerrota != null) {
            sonidoDerrota.dispose();
        }
    }

    // Método para reproducir la victoria
    public void reproducirVictoria() {
        if (musicGame.isPlaying()) {
            musicGame.stop();  // Detener la música de fondo
        }
        sonidoVictoria.play(soundVolume);  // Reproducir el sonido de victoria
        // Reproducir la música de fondo después de que termine el sonido de victoria
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                musicFondo.play();  // Volver a iniciar la música de fondo
            }
        }, DURACION_VICTORIA);
    }

    // Método para reproducir la derrota
    public void reproducirDerrota() {
        if (musicGame.isPlaying()) {
            musicGame.stop();  // Detener la música de fondo
        }
        sonidoDerrota.play(soundVolume);  // Reproducir el sonido de derrota
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                musicFondo.play();  // Volver a iniciar la música de fondo
            }
        }, DURACION_DERROTA);  // Usar la duración del sonido de derrota
    }

    public void reproducirJuego() {
        if (musicFondo.isPlaying()) {
            musicFondo.stop();
        }
        musicGame.play();


    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }
}
