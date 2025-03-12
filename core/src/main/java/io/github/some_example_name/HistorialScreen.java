package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class HistorialScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private Texture background;
    private float backgroundOffset;
    private SpriteBatch batch;
    private BitmapFont font;
    private String historial;

    private Stage stage;
    private Skin skin;
    private TextButton backButton;

    public HistorialScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        background = new Texture("static_snow.png");
        backgroundOffset = 0;
        batch = new SpriteBatch();
        font = new BitmapFont();

        // Obtener historial desde la base de datos
        historial = game.getDatabase().obtenerUltimasPartidas();
        if (historial == null || historial.isEmpty()) {
            historial = "No hay partidas registradas.";
        }

        // Configurar UI
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("skin/uiskin.json")); // Asegúrate de tener un skin

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Título
        Label titleLabel = new Label("Historial de Partidas", skin);
        table.add(titleLabel).padBottom(20);
        table.row();

        // Historial
        Label historialLabel = new Label(historial, skin);
        historialLabel.setWrap(true);
        table.add(historialLabel).width(400).padBottom(20);
        table.row();

        // Botón de Volver
        backButton = new TextButton("Volver", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });
        table.add(backButton).padTop(20);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Mover fondo
        backgroundOffset += 100 * delta;
        if (backgroundOffset >= Gdx.graphics.getHeight()) {
            backgroundOffset = 0;
        }

        batch.begin();
        batch.draw(background, 0, -backgroundOffset, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(background, 0, -backgroundOffset + Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        // Dibujar UI
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        background.dispose();
        stage.dispose();
        skin.dispose();
    }
}
