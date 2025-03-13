package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
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
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Sprites.Esquiador;

public class GameScreen extends InputAdapter implements Screen {

    private final Main game;  // Referencia a la clase principal
    private Camera camera;  // Cámara para la visualización
    private Viewport viewport;  // Vista del juego
    private TmxMapLoader mapLoader;  // Cargador de mapa
    private TiledMap map;  // Mapa cargado
    private OrthogonalTiledMapRenderer renderer;  // Renderizador del mapa

    private BitmapFont font;  // Fuente para mostrar texto
    private final float WORLD_WIDTH = 500;  // Ancho del mundo
    private final float WORLD_HEIGHT = 800;  // Alto del mundo

    // Variables de Box2D
    private World world;  // Mundo de física de Box2D
    private Box2DDebugRenderer b2dr;  // Renderer para la depuración de Box2D

    private boolean debug = false;  // Habilitar o deshabilitar la depuración de física

    // Esquiador y su cuerpo en el mundo físico
    private Esquiador esquiador;  // Esquiador del juego
    private float startX;  // Posición inicial en X
    private float startY;  // Posición inicial en Y

    // Constructor del GameScreen
    GameScreen(Main game) {
        this.game = game;  // Referencia a la clase principal
        game.reproducirJuego();  // Inicia la música del juego

        camera = new OrthographicCamera();  // Crea una nueva cámara ortográfica
        viewport = new ScalingViewport(Scaling.fillY, WORLD_WIDTH, WORLD_HEIGHT, camera);  // Crea un viewport con escalado
        game.batch = new SpriteBatch();  // Crea un batch para renderizar
        font = new BitmapFont();  // Crea una fuente para mostrar texto

        mapLoader = new TmxMapLoader();  // Cargador de mapas
        map = mapLoader.load("mapas/EsquiVerde.tmx");  // Carga el mapa del archivo TMX

        // Obtener el tamaño del mapa en píxeles
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        // Centrar en el ancho del mapa
        startX = mapWidth / 2;

        // Colocar en la parte superior del mapa (o donde quieras que empiece)
        startY = mapHeight - 50;  // Ajuste para iniciar un poco abajo

        renderer = new OrthogonalTiledMapRenderer(map);  // Crea un renderizador para el mapa

        // Inicialización de la física con gravedad hacia abajo
        world = new World(new Vector2(0, -9.8f), true);
        b2dr = new Box2DDebugRenderer();  // Renderer para depuración de Box2D

        // Crear el esquiador en el mundo físico
        esquiador = new Esquiador(world, startX, startY);

        // Crear los obstáculos en el mapa
        createObstacles();

        // Configurar el listener de contacto para detectar colisiones
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Object dataA = contact.getFixtureA().getUserData();
                Object dataB = contact.getFixtureB().getUserData();

                // Detecta si el esquiador colide con un obstáculo o la meta
                if ("obstaculo".equals(dataA) || "obstaculo".equals(dataB)) {
                    if (game.vibrationActive) {
                        Gdx.input.vibrate(250, 100, true);  // Vibración si la colisión es con un obstáculo
                    }
                    guardarResultadoPartida("EsquiVerde", false);  // Guardar derrota

                    game.reproducirDerrota();  // Reproducir sonido de derrota
                    game.setScreen(new GameOverScreen(game, "GameScreen", "DERROTA"));  // Cambiar a pantalla de fin de juego
                } else if ("meta".equals(dataA) || "meta".equals(dataB)) {
                    guardarResultadoPartida("EsquiVerde", true);  // Guardar victoria
                    game.reproducirVictoria();  // Reproducir sonido de victoria
                    game.setScreen(new GameOverScreen(game, "GameScreen", "VICTORIA"));  // Cambiar a pantalla de fin de juego
                }
            }

            @Override
            public void endContact(Contact contact) {
                // Lógica para cuando finaliza la colisión
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

    // Crear obstáculos estáticos (si están definidos en el mapa)
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
            body.createFixture(fixtureDef).setUserData("obstaculo");  // Establecer que es un obstáculo
        }

        // Crear la meta en el mapa
        for (MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
            body = world.createBody(bodyDef);
            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef).setUserData("meta");  // Establecer que es la meta
        }
    }

    // Manejo de la entrada del usuario (mover el esquiador)
    public void handleInput(float dt) {
        float screenWidth = Gdx.graphics.getWidth();  // Ancho de la pantalla

        if (Gdx.input.isTouched()) {
            int screenX = Gdx.input.getX();  // Obtener la posición X del toque

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
        handleInput(dt);  // Manejar la entrada del usuario
        world.step(1 / 60f, 6, 2);  // Avanzar en la simulación de física

        esquiador.update(dt);  // Actualizar el esquiador
        esquiador.body.applyForceToCenter(0, -100f, true);  // Aplicar una fuerza para hacer que el esquiador se mueva

        // Obtener las dimensiones del mapa
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        float cameraY = esquiador.body.getPosition().y - 350;  // Ajustar la posición de la cámara en Y
        float cameraHalfHeight = camera.viewportHeight / 2;  // Mitad de la altura de la cámara

        // Limitar la cámara en el eje Y
        if (cameraY - cameraHalfHeight < 0) {
            cameraY = cameraHalfHeight;  // No permite que la cámara se mueva fuera del mapa por abajo
        } else if (cameraY + cameraHalfHeight > mapHeight) {
            cameraY = mapHeight - cameraHalfHeight;  // No permite que la cámara se mueva fuera del mapa por arriba
        }

        // Actualizar la posición de la cámara
        camera.position.set(startX, cameraY, 0);
        camera.update();
        renderer.setView((OrthographicCamera) camera);  // Actualizar el renderizador con la nueva vista
    }

    // Renderizar el juego
    @Override
    public void render(float delta) {
        update(delta);  // Actualizar el estado del juego
        Gdx.gl.glClearColor(0, 0, 0, 1);  // Limpiar la pantalla con color negro
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);  // Limpiar el buffer de pantalla

        renderer.render();  // Renderizar el mapa

        // Dibujar la depuración de la física si está habilitada
        if (debug) {
            b2dr.render(world, camera.combined);  // Dibujar las entidades físicas
        }

        // Dibujar el esquiador en la pantalla
        game.batch.begin();
        game.batch.setProjectionMatrix(camera.combined);  // Establecer la cámara como proyección
        esquiador.render(game.batch);  // Dibujar el esquiador animado
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);  // Ajustar el tamaño del viewport cuando cambia el tamaño de la pantalla
    }

    @Override
    public void dispose() {
        game.batch.dispose();  // Liberar recursos de SpriteBatch
        font.dispose();  // Liberar recursos de la fuente
        esquiador.dispose();  // Liberar recursos del esquiador
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    // Guardar el resultado de la partida (victoria o derrota)
    public void guardarResultadoPartida(String mapa, boolean gano) {
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String resultado = gano ? "Ganó" : "Perdió";

        // Guardar en la base de datos
        ((Main) Gdx.app.getApplicationListener()).getDatabase().insertarPartida(mapa, fecha, resultado);
        System.out.println("Partida guardada: " + mapa + " | " + fecha + " | " + resultado);
    }
}
