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

public class BlackScreen extends InputAdapter implements Screen {
    private final Main game; // Referencia al objeto principal del juego
    private Camera camera; // Cámara de la escena
    private Viewport viewport; // Visor para ajustar la resolución de la pantalla
    private TmxMapLoader mapLoader; // Cargador de mapas Tiled
    private TiledMap map; // Mapa cargado
    private OrthogonalTiledMapRenderer renderer; // Renderizador de mapas en modo ortogonal

    private BitmapFont font; // Fuente para mostrar texto
    private final float WORLD_WIDTH = 500; // Ancho del mundo en píxeles
    private final float WORLD_HEIGHT = 800; // Alto del mundo en píxeles

    // Box2D Variables
    private World world; // Mundo de Box2D para física
    private Box2DDebugRenderer b2dr; // Renderizador de Box2D para depuración

    private boolean debug = false; // Habilitar o deshabilitar la depuración
    // Esquiador y su body
    private Esquiador esquiador; // Instancia del esquiador
    private float startX; // Posición inicial en X del esquiador
    private float startY; // Posición inicial en Y del esquiador

    // Constructor de la pantalla "BlackScreen"
    BlackScreen(Main game) {
        this.game = game; // Asigna la referencia al objeto principal del juego
        game.reproducirJuego(); // Reproduce música de fondo o sonido del juego

        // Configuración de la cámara y el visor
        camera = new OrthographicCamera();
        viewport = new ScalingViewport(Scaling.fillY, WORLD_WIDTH, WORLD_HEIGHT, camera);
        game.batch = new SpriteBatch(); // Crea un SpriteBatch para dibujar elementos gráficos
        font = new BitmapFont(); // Crea una fuente para mostrar texto

        // Cargar el mapa desde un archivo .tmx
        mapLoader = new TmxMapLoader();
        map = mapLoader.load("mapas/EsquiNegro.tmx");

        // Obtener las dimensiones del mapa
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        // Centrar el esquiador en el mapa
        startX = mapWidth / 2;
        startY = mapHeight - 50; // Ajusta la posición Y para colocar el esquiador en la parte superior

        renderer = new OrthogonalTiledMapRenderer(map); // Inicializa el renderizador de mapas

        // Inicializar el mundo Box2D con gravedad
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        world = new World(new Vector2(0, -9.8f), true); // Gravedad hacia abajo
        b2dr = new Box2DDebugRenderer(); // Inicializa el renderizador de depuración de Box2D

        // Crear el esquiador
        esquiador = new Esquiador(world, startX, startY);

        // Crear los objetos de la capa de obstáculos y metas
        createObstacles();

        // Configurar el listener de contacto para detectar colisiones
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Object dataA = contact.getFixtureA().getUserData();
                Object dataB = contact.getFixtureB().getUserData();

                // Si el esquiador toca un obstáculo, pierde
                if ("obstaculo".equals(dataA) || "obstaculo".equals(dataB)) {
                    if(game.vibrationActive) {
                        Gdx.input.vibrate(250, 100, true); // Vibration on contact
                    }
                    guardarResultadoPartida("EsquiNegro", false); // Guarda la derrota
                    game.reproducirDerrota(); // Reproduce el sonido de derrota
                    game.setScreen(new GameOverScreen(game, "BlackScreen", "DERROTA")); // Muestra la pantalla de "Game Over"
                }
                // Si el esquiador toca la meta, gana
                else if ("meta".equals(dataA) || "meta".equals(dataB)) {
                    System.out.println("¡Has llegado a la meta! Has ganado.");
                    guardarResultadoPartida("EsquiNegro", true); // Guarda la victoria
                    game.reproducirVictoria(); // Reproduce el sonido de victoria
                    game.setScreen(new GameOverScreen(game, "BlackScreen", "VICTORIA")); // Muestra la pantalla de "Game Over"
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

    // Crear obstáculos estáticos desde el mapa
    private void createObstacles() {
        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        // Crear obstáculos (capa 2 en el mapa)
        for (MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            bodyDef.type = BodyDef.BodyType.StaticBody; // Obstáculos son cuerpos estáticos
            bodyDef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
            body = world.createBody(bodyDef);
            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2); // Forma rectangular para cada obstáculo
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef).setUserData("obstaculo");
        }

        // Crear metas (capa 3 en el mapa)
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

    // Guardar el resultado de la partida en la base de datos
    public void guardarResultadoPartida(String mapa, boolean gano) {
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String resultado = gano ? "Ganó" : "Perdió";
        ((Main) Gdx.app.getApplicationListener()).getDatabase().insertarPartida(mapa, fecha, resultado); // Guardar en base de datos
        System.out.println("Partida guardada: " + mapa + " | " + fecha + " | " + resultado);
    }

    // Manejo de la entrada del usuario (movimiento del esquiador)
    public void handleInput(float dt) {
        float screenWidth = Gdx.graphics.getWidth(); // Ancho de la pantalla

        if (Gdx.input.isTouched()) {
            int screenX = Gdx.input.getX(); // Obtener la posición X del toque

            // Si el toque está en la mitad derecha de la pantalla, mover a la derecha
            if (screenX > screenWidth / 2) {
                esquiador.move(50, esquiador.body.getLinearVelocity().y);
            } else { // Si está en la mitad izquierda, mover a la izquierda
                esquiador.move(-50, esquiador.body.getLinearVelocity().y);
            }
        }
    }

    // Actualizar la lógica del juego
    public void update(float dt) {
        handleInput(dt); // Manejar la entrada del usuario
        world.step(1 / 60f, 6, 2); // Actualizar la simulación de la física en Box2D

        esquiador.update(dt); // Actualizar al esquiador

        // Aplicar fuerza al esquiador para que caiga
        esquiador.body.applyForceToCenter(0, -100f, true);

        // Obtener las dimensiones del mapa
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        // Calcular la posición de la cámara en función de la posición del esquiador
        float cameraX = esquiador.body.getPosition().x;
        float cameraY = esquiador.body.getPosition().y - 350;

        // Limitar la posición de la cámara en el eje Y
        float cameraHalfWidth = camera.viewportWidth / 2;
        float cameraHalfHeight = camera.viewportHeight / 2;

        if (cameraY - cameraHalfHeight < 0) {
            cameraY = cameraHalfHeight;
        } else if (cameraY + cameraHalfHeight > mapHeight) {
            cameraY = mapHeight - cameraHalfHeight;
        }

        // Actualizar la posición de la cámara
        camera.position.set(startX, cameraY, 0);
        camera.update();

        renderer.setView((OrthographicCamera) camera);
    }

    @Override
    public void show() {
        // Método vacío, implementado por interfaz Screen
    }

    @Override
    public void render(float delta) {
        update(delta); // Actualizar la lógica del juego
        Gdx.gl.glClearColor(0, 0, 0, 1); // Limpiar la pantalla con color negro
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Limpiar el buffer de pantalla

        renderer.render(); // Dibujar el mapa

        if (debug) {
            b2dr.render(world, camera.combined); // Mostrar la depuración de Box2D
        }

        game.batch.begin(); // Comienza el batch para dibujar
        game.batch.setProjectionMatrix(camera.combined); // Configurar la matriz de proyección
        esquiador.render(game.batch); // Dibujar el esquiador
        game.batch.end(); // Finaliza el batch
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // Actualizar el viewport cuando cambia el tamaño
    }

    @Override
    public void pause() {
        // Método vacío, implementado por interfaz Screen
    }

    @Override
    public void resume() {
        // Método vacío, implementado por interfaz Screen
    }

    @Override
    public void hide() {
        // Método vacío, implementado por interfaz Screen
    }

    @Override
    public void dispose() {
        game.batch.dispose(); // Liberar los recursos del SpriteBatch
        font.dispose(); // Liberar los recursos de la fuente
        esquiador.dispose(); // Liberar los recursos del esquiador
    }
}
