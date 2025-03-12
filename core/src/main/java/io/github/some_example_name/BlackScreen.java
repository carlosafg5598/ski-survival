package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Sprites.Esquiador;

public class BlackScreen extends InputAdapter implements Screen {
    private final Main game;
    private Camera camera;
    private Viewport viewport;
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    private BitmapFont font;
    private final float WORLD_WIDTH = Gdx.graphics.getWidth();
    private final float WORLD_HEIGHT = Gdx.graphics.getHeight();

    // Box2D Variables
    private World world;
    private Box2DDebugRenderer b2dr;

    private boolean debug = false;
    // Esquiador y su body
    private Esquiador esquiador;
    private float startX;
    private float startY;

    BlackScreen(Main game) {
        this.game = game;
        game.reproducirJuego();
        startX = 150;
        startY = Gdx.graphics.getHeight()+ 14400;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        game.batch = new SpriteBatch();
        font = new BitmapFont();

        mapLoader = new TmxMapLoader();
        map = mapLoader.load("mapas/EsquiNegro.tmx");
        renderer = new OrthogonalTiledMapRenderer(map);

        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        world = new World(new Vector2(0, -9.8f), true); // Gravedad hacia abajo
        b2dr = new Box2DDebugRenderer();

        // Crear el esquiador
        esquiador = new Esquiador(world, startX, startY);

        // Crear los objetos de la capa de obstáculos
        createObstacles();

        // Configurar el listener de contacto para detectar colisiones
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Object dataA = contact.getFixtureA().getUserData();
                Object dataB = contact.getFixtureB().getUserData();

                if ("obstaculo".equals(dataA) || "obstaculo".equals(dataB)) {

                    if(game.vibrationActive){
                        Gdx.input.vibrate(250, 100, true);
                    }
                    guardarResultadoPartida("EsquiNegro", false);
                    game.reproducirDerrota();
                    game.setScreen(new GameOverScreen(game, "BlackScreen", "DERROTA"));
                } else if ("meta".equals(dataA) || "meta".equals(dataB)) {
                    System.out.println("¡Has llegado a la meta! Has ganado.");
                    guardarResultadoPartida("EsquiNegro", true);
                    game.reproducirVictoria();
                    game.setScreen(new GameOverScreen(game, "BlackScreen", "VICTORIA"));
                }
            }

            @Override
            public void endContact(Contact contact) {
                // Aquí se puede manejar el final de la colisión si es necesario
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                // Lógica previa a la colisión
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
                // Lógica posterior a la colisión
            }
        });
    }

    private void createObstacles() {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;
        for (MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
            body = world.createBody(bodyDef);
            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef).setUserData("obstaculo");
        }

        for (MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
            body = world.createBody(bodyDef);
            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef).setUserData("meta");
        }
    }
    public void guardarResultadoPartida(String mapa, boolean gano) {
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String resultado = gano ? "Ganó" : "Perdió";

        // Guardar en la base de datos
        ((Main) Gdx.app.getApplicationListener()).getDatabase().insertarPartida(mapa, fecha, resultado);
        System.out.println("Partida guardada: " + mapa + " | " + fecha + " | " + resultado);
    }

    public void handleInput(float dt) {
        float screenWidth = Gdx.graphics.getWidth(); // Ancho de la pantalla

        if (Gdx.input.isTouched()) {
            int screenX = Gdx.input.getX(); // Obtener la posición X del toque

            // Si el toque está en la mitad derecha de la pantalla
            if (screenX > screenWidth / 2) {
                esquiador.move(50, esquiador.body.getLinearVelocity().y);  // Movimiento hacia la derecha
            } else {
                esquiador.move(-50, esquiador.body.getLinearVelocity().y);  // Movimiento hacia la izquierda
            }
        }
    }
    // Actualizar la lógica del juego
    public void update(float dt) {
        handleInput(dt);
        world.step(1 / 60f, 6, 2);

        esquiador.update(dt);

        // Obtener las dimensiones del mapa
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        // Calcular la posición de la cámara en función de la posición del esquiador
        float cameraX = esquiador.body.getPosition().x;
        float cameraY = esquiador.body.getPosition().y - 350;

        // Limitar la posición de la cámara para que no se salga de los límites del mapa
        float cameraHalfWidth = camera.viewportWidth / 2;
        float cameraHalfHeight = camera.viewportHeight / 2;

//        // Limitar la cámara en el eje X
//        if (cameraX - cameraHalfWidth < 0) {
//            cameraX = cameraHalfWidth; // No permite que la cámara se mueva fuera del mapa por la izquierda
//        } else if (cameraX + cameraHalfWidth > mapWidth) {
//            cameraX = mapWidth - cameraHalfWidth; // No permite que la cámara se mueva fuera del mapa por la derecha
//        }

        // Limitar la cámara en el eje Y
        if (cameraY - cameraHalfHeight < 0) {
            cameraY = cameraHalfHeight; // No permite que la cámara se mueva fuera del mapa por abajo
        } else if (cameraY + cameraHalfHeight > mapHeight) {
            cameraY = mapHeight - cameraHalfHeight; // No permite que la cámara se mueva fuera del mapa por arriba
        }

        // Actualizar la posición de la cámara
        camera.position.set(startX, cameraY, 0);
        camera.update();

        renderer.setView((OrthographicCamera) camera);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderer.render();
        if (debug) {
            b2dr.render(world, camera.combined);
        }


        game.batch.begin();
        game.batch.setProjectionMatrix(camera.combined);
        esquiador.render(game.batch); // Dibujar esquiador animado
        game.batch.end();
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
        font.dispose();
        esquiador.dispose();
    }
}
