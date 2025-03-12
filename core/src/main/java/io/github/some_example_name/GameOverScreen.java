package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameOverScreen implements Screen {

    private final Main game;
    private Camera camera;
    private Viewport viewport;
    private Texture background;
    private int backgroundOffset;
    private BitmapFont font;
    private int selectedOption = -1;
    private final String[] menuOptions = {"V o l v e r  a  J u g a r ", "M e n u  D e  J u e g o", "S a l i r  D e l  J u e g o"};
    private Vector3 touchCoords = new Vector3();
    private Rectangle[] menuBounds;
    private boolean touchHandled = false;
    private boolean transitioning = false;
    private float touchCooldown = 0.3f;
    private String previousScreen;
    private String resultado;
    private float r, g, b; // Variables para cambiar el color del título


    GameOverScreen(Main game, String previousScreen, String resultado) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.resultado = resultado;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        background = new Texture("static_snow.png");
        backgroundOffset = 0;
        game.batch = new SpriteBatch();
        font = new BitmapFont();



        if ("VICTORIA".equals(resultado)) {
            resultado = "VICTORIA";
            r = 0;
            g = 1;
            b = 0; // Verde
        } else {
            resultado = "DERROTA";
            r = 1;
            g = 0;
            b = 0; // Rojo
        }

        menuBounds = new Rectangle[menuOptions.length];
        float buttonWidth = Gdx.graphics.getWidth() * 0.5f;  // 50% del ancho de la pantalla
        float buttonHeight = Gdx.graphics.getHeight() * 0.1f; // 10% de la altura
        float startY = Gdx.graphics.getHeight() / 2 + (menuOptions.length / 2) * buttonHeight; // Centrar los botones verticalmente

        for (int i = 0; i < menuOptions.length; i++) {
            float optionX = (Gdx.graphics.getWidth() - buttonWidth) / 2; // Centrar en X
            float optionY = startY - i * (buttonHeight + 10); // Separar bien los botones
            menuBounds[i] = new Rectangle(optionX, optionY, buttonWidth, buttonHeight);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (touchCooldown > 0) {
            touchCooldown -= delta;
        }
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

// 🔹 **Dibujar el fondo correctamente**
        backgroundOffset++;
        if (backgroundOffset % Gdx.graphics.getHeight() == 0) {
            backgroundOffset = 0;
        }
        game.batch.draw(background, 0, -backgroundOffset, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.draw(background, 0, -backgroundOffset + Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

// 🔹 **Dibujar el título (VICTORIA o DERROTA)**
        font.getData().setScale(2.5f);  // 🔹 **Aumentamos el tamaño de la cabecera**
        font.setColor(r, g, b, 1);  // Color dependiendo del resultado

// Centrar el texto en la parte superior
        float titleWidth = font.getRegion().getTexture().getWidth(); // Obtener ancho del texto
        float titleX = (Gdx.graphics.getWidth() - titleWidth) / 2; // Centrar en X
        float titleY = Gdx.graphics.getHeight() - 50; // Posicionar en la parte superior
        font.draw(game.batch, resultado, titleX, titleY);

// 🔹 **Dibujar el menú de opciones**
        font.getData().setScale(2f);  // 🔹 **Reducimos el tamaño del texto de los botones**
        for (int i = 0; i < menuOptions.length; i++) {
            font.setColor(0, 0, 0, 1);

            // Centrar el texto dentro del botón
            float textWidth = font.getRegion().getTexture().getWidth();
            float textX = menuBounds[i].x + (menuBounds[i].width - textWidth) / 2;
            float textY = menuBounds[i].y + (menuBounds[i].height) / 2 + font.getCapHeight() / 2;

            font.draw(game.batch, menuOptions[i], textX, textY);
        }

        game.batch.end();

        handleTouchInput();
    }

    private void handleTouchInput() {
        if (transitioning || touchCooldown > 0) return;

        if (Gdx.input.isTouched() && !touchHandled) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.input.getY();
            camera.unproject(touchCoords.set(touchX, touchY, 0));

            for (int i = 0; i < menuBounds.length; i++) {
                if (menuBounds[i].contains(touchCoords.x, touchCoords.y)) {
                    if (selectedOption != i) {
                        selectedOption = i;
                        handleSelection();
                    }
                    break;
                }
            }
            touchHandled = true;
        } else if (!Gdx.input.isTouched()) {
            touchHandled = false;
            selectedOption = -1;
        }
    }

    private void handleSelection() {
        transitioning = true;
        Gdx.app.postRunnable(() -> {
            if (selectedOption == 0) { // Volver a jugar
                switch (previousScreen) {
                    case "GameScreen":
                        game.setScreen(new GameScreen(game));
                        break;
                    case "BlueScreen":
                        game.setScreen(new BlueScreen(game));
                        break;
                    case "RedScreen":
                        game.setScreen(new RedScreen(game));
                        break;
                    case "BlackScreen":
                        game.setScreen(new BlackScreen(game));
                        break;

                }
            } else if (selectedOption == 1) { // Volver al menú
                game.setScreen(new MenuDeJuego(game));
            } else if (selectedOption == 2) { // Salir del juego
                Gdx.app.exit();
            }
        });
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
        game.batch.dispose();
        background.dispose();
        font.dispose();
    }

    @Override
    public void show() {
        touchCooldown = 0.3f;
    }

}
