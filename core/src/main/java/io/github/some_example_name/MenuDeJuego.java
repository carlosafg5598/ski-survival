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

public class MenuDeJuego implements Screen {

    private final Main game;
    private Camera camera;
    private Viewport viewport;
    private Texture background;
    private int backgroundOffset;
    private BitmapFont font;
    private int selectedOption = -1;
    private final String[] menuOptions = {"V e r d e ", "A z u l ", "R o j o ", "N e g r o " ,"A t r a s"};
//    private final int WORLD_WIDTH = 72;
//    private final int WORLD_HEIGHT = 128;
    private Vector3 touchCoords = new Vector3();
    private Rectangle[] menuBounds;
    private boolean touchHandled = false;
    private boolean transitioning = false;
    private float touchCooldown = 0.3f;

    MenuDeJuego(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        background = new Texture("static_snow.png");
        backgroundOffset = 0;
        game.batch = new SpriteBatch();
        font = new BitmapFont();

        // 📌 **Reducimos el tamaño del texto**
        font.getData().setScale(2f); // Antes era 0.3f, ahora más pequeño

        // 📌 **Creamos los botones más pequeños**
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

        game.batch.begin();

        // 📌 **Dibujamos el fondo correctamente**
        backgroundOffset++;
        if (backgroundOffset % Gdx.graphics.getHeight() == 0) {
            backgroundOffset = 0;
        }
        game.batch.draw(background, 0, -backgroundOffset, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.draw(background, 0, -backgroundOffset + Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 🔹 **Dibujar el texto más pequeño y centrado dentro del botón**
        for (int i = 0; i < menuOptions.length; i++) {
            font.setColor(0, 0, 0, 1);

            // Centrar el texto dentro del botón
            float textWidth = font.getRegion().getTexture().getWidth();
            float textX = menuBounds[i].x + (menuBounds[i].width - textWidth) / 2;
            float textY = menuBounds[i].y + (menuBounds[i].height) / 2 + font.getCapHeight() / 2; // Centrar mejor verticalmente

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
            switch (selectedOption) {
                case 0:
                    game.setScreen(new GameScreen(game));
                    break;
                case 1:
                    game.setScreen(new BlueScreen(game));
                    break;
                case 2:
                    game.setScreen(new RedScreen(game));
                    break;
                case 3:
                    game.setScreen(new BlackScreen(game));
                    break;

                case 4:
                    game.setScreen(new MenuScreen(game));
                    break;
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        game.batch.setProjectionMatrix(camera.combined);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        game.batch.dispose();
        background.dispose();
        font.dispose();
    }
    @Override public void show() {
        touchCooldown = 0.3f;
    }
}
