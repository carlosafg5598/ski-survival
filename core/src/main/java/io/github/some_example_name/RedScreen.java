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

public class RedScreen extends InputAdapter implements Screen {
    private final Main game; // Instancia del juego principal
    private Camera camera; // Cámara de juego
    private Viewport viewport; // Vista para el ajuste de la pantalla
    private TmxMapLoader mapLoader; // Cargador de mapas Tiled
    private TiledMap map; // Mapa cargado
    private OrthogonalTiledMapRenderer renderer; // Renderizador para el mapa

    private BitmapFont font; // Fuente para los textos en pantalla
    private final float WORLD_WIDTH = 500; // Ancho del mundo del juego
    private final float WORLD_HEIGHT = 800; // Altura del mundo del juego

    // Variables de Box2D para la física
    private World world; // Mundo de física
    private Box2DDebugRenderer b2dr; // Renderizador de Box2D para ver colisiones
    private boolean debug = false; // Activar/desactivar la visualización de Box2D

    // Esquiador y su body físico
    private Esquiador esquiador; // Personaje controlado por el jugador
    private float startX; // Coordenada X de inicio del esquiador
    private float startY; // Coordenada Y de inicio del esquiador

    // Constructor para la pantalla de juego
    RedScreen(Main game) {
        this.game = game;
        game.reproducirJuego(); // Iniciar música del juego

        camera = new OrthographicCamera(); // Crear cámara ortográfica
        viewport = new ScalingViewport(Scaling.fillY, WORLD_WIDTH, WORLD_HEIGHT, camera); // Establecer vista escalada
        game.batch = new SpriteBatch(); // Crear batch para dibujo
        font = new BitmapFont(); // Crear fuente para texto

        mapLoader = new TmxMapLoader(); // Inicializar cargador de mapa
        map = mapLoader.load("mapas/EsquiRojo.tmx"); // Cargar el mapa
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        // Centrar el esquiador en el mapa
        startX = mapWidth / 2;

        // Ajustar la coordenada Y para iniciar cerca de la parte superior del mapa
        startY = mapHeight - 50; // Ajusta el valor según lo necesites
        renderer = new OrthogonalTiledMapRenderer(map); // Renderizar mapa

        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0); // Posición inicial de la cámara
        world = new World(new Vector2(0, -9.8f), true); // Crear mundo con gravedad
        b2dr = new Box2DDebugRenderer(); // Inicializar render de Box2D

        // Crear el esquiador con las posiciones iniciales
        esquiador = new Esquiador(world, startX, startY);

        // Crear los obstáculos en el mapa
        createObstacles();

        // Configurar el listener de contacto para colisiones
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                // Detectar colisiones al comenzar
                Object dataA = contact.getFixtureA().getUserData();
                Object dataB = contact.getFixtureB().getUserData();

                // Si el esquiador choca con un obstáculo
                if ("obstaculo".equals(dataA) || "obstaculo".equals(dataB)) {
                    System.out.println("¡Colisión con un obstáculo! Has perdido.");
                    if (game.vibrationActive) {
                        Gdx.input.vibrate(250, 100, true); // Activar vibración
                    }
                    guardarResultadoPartida("EsquiRojo", false); // Guardar resultado de la partida
                    game.reproducirDerrota(); // Reproducir sonido de derrota
                    game.setScreen(new GameOverScreen(game, "RedScreen", "DERROTA")); // Cambiar pantalla a "Game Over"
                }
                // Si el esquiador llega a la meta
                else if ("meta".equals(dataA) || "meta".equals(dataB)) {
                    System.out.println("¡Has llegado a la meta! Has ganado.");
                    guardarResultadoPartida("EsquiRojo", true); // Guardar resultado de la victoria
                    game.reproducirVictoria(); // Reproducir sonido de victoria
                    game.setScreen(new GameOverScreen(game, "RedScreen", "VICTORIA")); // Cambiar pantalla a "Game Over"
                }
            }

            @Override
            public void endContact(Contact contact) {
                // Lógica cuando termina la colisión, si es necesario
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

    // Método para guardar los resultados de la partida
    public void guardarResultadoPartida(String mapa, boolean gano) {
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String resultado = gano ? "Ganó" : "Perdió";

        // Guardar en la base de datos
        ((Main) Gdx.app.getApplicationListener()).getDatabase().insertarPartida(mapa, fecha, resultado);
        System.out.println("Partida guardada: " + mapa + " | " + fecha + " | " + resultado);
    }

    // Método para crear los obstáculos en el mapa
    private void createObstacles() {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;
        // Crear obstáculos a partir de objetos en la capa 2 del mapa
        for (MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            bodyDef.type = BodyDef.BodyType.StaticBody; // Definir el cuerpo como estático
            bodyDef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
            body = world.createBody(bodyDef);
            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef).setUserData("obstaculo"); // Asignar datos de "obstáculo"
        }

        // Crear la meta (capa 3)
        for (MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
            body = world.createBody(bodyDef);
            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef).setUserData("meta"); // Asignar datos de "meta"
        }
    }

    // Método para manejar la entrada táctil
    public void handleInput(float dt) {
        float screenWidth = Gdx.graphics.getWidth(); // Obtener el ancho de la pantalla

        if (Gdx.input.isTouched()) {
            int screenX = Gdx.input.getX(); // Obtener la posición X del toque

            // Si el toque está en la mitad derecha de la pantalla
            if (screenX > screenWidth / 2) {
                esquiador.move(50, esquiador.body.getLinearVelocity().y);  // Movimiento a la derecha
            } else {
                esquiador.move(-50, esquiador.body.getLinearVelocity().y);  // Movimiento a la izquierda
            }
        }
    }

    // Actualizar la lógica del juego
    public void update(float dt) {
        handleInput(dt); // Manejar la entrada del jugador
        world.step(1 / 60f, 6, 2); // Actualizar la simulación física

        esquiador.update(dt); // Actualizar el esquiador

        esquiador.body.applyForceToCenter(0, -100f, true); // Aplicar fuerza hacia abajo (gravedad)

        // Calcular las dimensiones del mapa
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        // Calcular la posición de la cámara en función del esquiador
        float cameraX = esquiador.body.getPosition().x;
        float cameraY = esquiador.body.getPosition().y - 350;

        // Limitar la cámara en el eje Y para no salirse del mapa
        float cameraHalfWidth = camera.viewportWidth / 2;
        float cameraHalfHeight = camera.viewportHeight / 2;

        // Limitar la cámara en el eje Y
        if (cameraY - cameraHalfHeight < 0) {
            cameraY = cameraHalfHeight; // No permitir que la cámara se mueva fuera del mapa por abajo
        } else if (cameraY + cameraHalfHeight > mapHeight) {
            cameraY = mapHeight - cameraHalfHeight; // No permitir que la cámara se mueva fuera del mapa por arriba
        }

        // Actualizar la posición de la cámara
        camera.position.set(startX, cameraY, 0);
        camera.update();

        renderer.setView((OrthographicCamera) camera); // Actualizar la vista del renderizador
    }

    @Override
    public void show() {
        // Método llamado cuando se muestra la pantalla
    }

    @Override
    public void render(float delta) {
        update(delta); // Actualizar la lógica del juego
        Gdx.gl.glClearColor(0, 0, 0, 1); // Establecer el color de fondo
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Limpiar el buffer de pantalla
        renderer.render(); // Renderizar el mapa
        if (debug) {
            b2dr.render(world, camera.combined); // Renderizar Box2D si el debug está activado
        }

        // Dibujar el esquiador en la pantalla
        game.batch.begin();
        game.batch.setProjectionMatrix(camera.combined);
        esquiador.render(game.batch); // Dibujar el esquiador
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // Redimensionar la vista
    }

    @Override
    public void pause() {
        // Método llamado cuando la aplicación se pausa
    }

    @Override
    public void resume() {
        // Método llamado cuando la aplicación se reanuda
    }

    @Override
    public void hide() {
        // Método llamado cuando la pantalla se oculta
    }

    @Override
    public void dispose() {
        // Limpiar recursos cuando la pantalla es destruida
        game.batch.dispose();
        font.dispose();
        esquiador.dispose();
    }
}
