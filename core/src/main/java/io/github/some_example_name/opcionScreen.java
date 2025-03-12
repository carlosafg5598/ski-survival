package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class opcionScreen implements Screen {
    private final Main game;
    private Camera camera;
    private Viewport viewport;
    private Texture background;
    private Stage stage;
    private float musicVolume = 1.0f;
    private float soundVolume = 1.0f;
    private boolean vibrationEnabled = true;
    private int backgroundOffset;
    private CheckBox checkVibracion;

    public opcionScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        background = new Texture("static_snow.png");
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        // Crear UI
        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json")); // Puedes reemplazarlo si no tienes un Skin

        Label musicLabel = new Label(LanguageManager.get("Volumen Música"), skin);
        Slider musicSlider = new Slider(0, 1, 0.1f, false, skin);
        musicSlider.setValue(musicVolume);
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                musicVolume = musicSlider.getValue();
                game.setMusicVolume(musicVolume);
            }
        });

        Label soundLabel = new Label(LanguageManager.get("Volumen Efectos de Sonido"), skin);
        Slider soundSlider = new Slider(0, 1, 0.1f, false, skin);
        soundSlider.setValue(soundVolume);
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                soundVolume = soundSlider.getValue();
                game.setSoundVolume(soundVolume);
            }
        });

        TextButton vibrationButton = new TextButton(LanguageManager.get("Vibracion") + ": ON", skin);
        vibrationButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                vibrationEnabled = !vibrationEnabled;
                vibrationButton.setText("Vibración: " + (vibrationEnabled ? "ON" : "OFF"));
                game.setVibrationEnabled(vibrationEnabled);
            }
        });

        TextButton backButton = new TextButton(LanguageManager.get("Atras"), skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new MenuScreen(game));
            }
        });




        // Ajustar el tamaño de los elementos según la resolución de la pantalla
        float buttonWidth = Gdx.graphics.getWidth() * 0.4f;
        float sliderWidth = Gdx.graphics.getWidth() * 0.6f;

        // Organizar los elementos en la tabla
        table.add(musicLabel).pad(5);
        table.row();
        table.add(musicSlider).width(sliderWidth).pad(5);
        table.row();
        table.add(soundLabel).pad(5);
        table.row();
        table.add(soundSlider).width(sliderWidth).pad(5);
        table.row();
        table.add(vibrationButton).width(buttonWidth).pad(10);
        table.row();
        table.add(backButton).width(buttonWidth).pad(10);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        //game.batch.draw(background, 0, 0, 72, 128);
        // 🔹 **Dibujar el fondo correctamente**
        backgroundOffset++;
        if (backgroundOffset % Gdx.graphics.getHeight() == 0) {
            backgroundOffset = 0;
        }
        game.batch.draw(background, 0, -backgroundOffset, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.draw(background, 0, -backgroundOffset + Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //game.batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        game.batch.end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        background.dispose();
    }

    @Override
    public void show() {
    }
}
