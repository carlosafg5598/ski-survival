package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public class BlueScreen extends InputAdapter implements Screen {
    private final Main game; // Referencia al juego principal para acceder a métodos y manejar pantallas
    private Camera camera; // Cámara para visualizar el mundo del juego
    private Viewport viewport; // Vista que ajusta el tamaño de la pantalla
    private TmxMapLoader mapLoader; // Cargador de mapas Tiled (.tmx)
    private TiledMap map; // Mapa cargado desde un archivo Tiled
    private OrthogonalTiledMapRenderer renderer; // Renderizador para dibujar el mapa

    private BitmapFont font; // Fuente para mostrar texto en pantalla (como resultados)
    private final float WORLD_WIDTH = 500; // Ancho del mundo del juego
    private final float WORLD_HEIGHT = 800; // Alto del mundo del juego

    // Variables para Box2D (manejando la física del juego)
    private World world; // Mundo físico
    private Box2DDebugRenderer b2dr; // Renderizador para depuración de Box2D

    private boolean debug = false; // Bandera para activar el modo de depuración de Box2D
    // Esquiador y su body (objeto físico asociado al esquiador)
    private Esquiador esquiador;
    private float startX; // Coordenada X inicial del esquiador
    private float startY; // Coordenada Y inicial del esquiador

    BlueScreen(Main game) {
        this.game = game; // Asignamos la referencia del juego principal
        game.reproducirJuego(); // Inicia la música o efectos del juego al cargar esta pantalla

        // Inicialización de la cámara y viewport
        camera = new OrthographicCamera(); // Usamos una cámara ortográfica (2D)
        viewport = new ScalingViewport(Scaling.fillY, WORLD_WIDTH, WORLD_HEIGHT, camera); // Ajusta la vista a las dimensiones del mundo
        game.batch = new SpriteBatch(); // Crea el batch de dibujo para renderizar gráficos
        font = new BitmapFont(); // Crea la fuente para el texto en pantalla

        // Carga el mapa Tiled
        mapLoader = new TmxMapLoader();
        map = mapLoader.load("mapas/EsquiAzul.tmx"); // Mapa cargado desde un archivo Tiled

        // Calcula las dimensiones del mapa
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        // Calcula la posición inicial del esquiador en el mapa (centrado en X, en la parte superior en Y)
        startX = mapWidth / 2; // Centrado horizontalmente
        startY = mapHeight - 50; // Colocado cerca de la parte superior del mapa

        // Inicializa el renderizador del mapa
        renderer = new OrthogonalTiledMapRenderer(map);

        // Inicializa la cámara para que esté centrada en el medio del mundo
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        // Crea el mundo físico de Box2D con gravedad hacia abajo
        world = new World(new Vector2(0, -9.8f), true);
        b2dr = new Box2DDebugRenderer(); // Renderizador para mostrar las colisiones de Box2D en el modo debug

        // Inicializa el esquiador (jugador) con la física asociada
        esquiador = new Esquiador(world, startX, startY);

        // Crea los obstáculos definidos en el mapa (en el tercer y cuarto capa del mapa)
        createObstacles();

        // Configura el listener para detectar colisiones entre el esquiador y otros objetos
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Object dataA = contact.getFixtureA().getUserData(); // Datos del primer objeto en la colisión
                Object dataB = contact.getFixtureB().getUserData(); // Datos del segundo objeto en la colisión

                // Si el esquiador toca un obstáculo
                if ("obstaculo".equals(dataA) || "obstaculo".equals(dataB)) {
                    if(game.vibrationActive){ // Si la vibración está activa
                        Gdx.input.vibrate(250, 100, true); // Vibra el dispositivo
                    }
                    guardarResultadoPartida("EsquiAzul", false); // Guarda el resultado de la partida (derrota)
                    game.reproducirDerrota(); // Reproduce el sonido de derrota
                    game.setScreen(new GameOverScreen(game, "BlueScreen", "DERROTA")); // Cambia a la pantalla de Game Over
                }
                // Si el esquiador toca la meta
                else if ("meta".equals(dataA) || "meta".equals(dataB)) {
                    System.out.println("¡Has llegado a la meta! Has ganado.");
                    guardarResultadoPartida("EsquiAzul", true); // Guarda el resultado de la partida (victoria)
                    game.reproducirVictoria(); // Reproduce el sonido de victoria
                    game.setScreen(new GameOverScreen(game, "BlueScreen", "VICTORIA")); // Cambia a la pantalla de Game Over
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

    // Método para crear los obstáculos definidos en el mapa
    private void createObstacles() {
        BodyDef bodyDef = new BodyDef(); // Define las propiedades del cuerpo físico
        PolygonShape shape = new PolygonShape(); // Forma poligonal para los obstáculos
        FixtureDef fixtureDef = new FixtureDef(); // Define las propiedades del fixture (adjunto al cuerpo físico)
        Body body; // Cuerpo físico del objeto

        // Itera sobre los objetos en la capa 2 del mapa (obstáculos)
        for (MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle(); // Extrae la información de la rectángulo del objeto
            bodyDef.type = BodyDef.BodyType.StaticBody; // Define el cuerpo como estático (no se mueve)
            bodyDef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2); // Coloca el cuerpo en el centro del rectángulo
            body = world.createBody(bodyDef); // Crea el cuerpo en el mundo físico
            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2); // Define la forma del obstáculo como un rectángulo
            fixtureDef.shape = shape; // Asocia la forma con el fixture
            body.createFixture(fixtureDef).setUserData("obstaculo"); // Crea el fixture y asigna un dato de usuario para identificarlo como obstáculo
        }

        // Similar al proceso anterior, pero para la meta (capa 3 del mapa)
        for (MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
            body = world.createBody(bodyDef);
            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef).setUserData("meta"); // Aquí la meta es un tipo de objeto diferente
        }
    }

    // Método que maneja la entrada del jugador (movimiento del esquiador)
    public void handleInput(float dt) {
        float screenWidth = Gdx.graphics.getWidth(); // Obtiene el ancho de la pantalla

        if (Gdx.input.isTouched()) { // Si la pantalla es tocada
            int screenX = Gdx.input.getX(); // Obtiene la coordenada X del toque

            // Si el toque está en la mitad derecha de la pantalla
            if (screenX > screenWidth / 2) {
                esquiador.move(50, esquiador.body.getLinearVelocity().y); // Mueve el esquiador a la derecha
            } else {
                esquiador.move(-50, esquiador.body.getLinearVelocity().y); // Mueve el esquiador a la izquierda
            }
        }
    }

    // Método para actualizar la lógica del juego
    public void update(float dt) {
        handleInput(dt); // Maneja la entrada del usuario
        world.step(1 / 60f, 6, 2); // Actualiza la simulación de Box2D (físicas)

        esquiador.update(dt); // Actualiza al esquiador (mueve y anima)

        // Aplicar una fuerza hacia abajo (simula gravedad o caída)
        esquiador.body.applyForceToCenter(0, -100f, true);

        // Obtener las dimensiones del mapa para ajustar la cámara
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        // Calcular la posición de la cámara en función de la posición del esquiador
        float cameraX = esquiador.body.getPosition().x;
        float cameraY = esquiador.body.getPosition().y - 350;

        // Limitar la posición de la cámara para que no se salga de los límites del mapa
        float cameraHalfWidth = camera.viewportWidth / 2;
        float cameraHalfHeight = camera.viewportHeight / 2;

        if (cameraY - cameraHalfHeight < 0) {
            cameraY = cameraHalfHeight; // No permite que la cámara se mueva fuera del mapa por abajo
        } else if (cameraY + cameraHalfHeight > mapHeight) {
            cameraY = mapHeight - cameraHalfHeight; // No permite que la cámara se mueva fuera del mapa por arriba
        }

        // Actualizar la posición de la cámara
        camera.position.set(startX, cameraY, 0);
        camera.update(); // Actualiza la cámara con los nuevos valores

        renderer.setView((OrthographicCamera) camera); // Configura el renderizador con la nueva vista de la cámara
    }

    // Método para renderizar la pantalla
    @Override
    public void render(float delta) {
        update(delta); // Actualiza la lógica del juego
        Gdx.gl.glClearColor(0, 0, 0, 1); // Establece el color de fondo
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Limpia la pantalla

        renderer.render(); // Renderiza el mapa del juego

        if (debug) {
            b2dr.render(world, camera.combined); // Si está en modo debug, renderiza las colisiones físicas
        }

        // Renderiza los gráficos del juego (esquiador)
        game.batch.begin();
        game.batch.setProjectionMatrix(camera.combined); // Configura la matriz de proyección de la cámara
        esquiador.render(game.batch); // Renderiza al esquiador
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // Ajusta el tamaño del viewport al cambiar el tamaño de la pantalla
    }

    @Override
    public void dispose() {
        game.batch.dispose(); // Libera los recursos del batch de dibujo
        font.dispose(); // Libera los recursos de la fuente
        esquiador.dispose(); // Libera los recursos del esquiador
    }

    @Override
    public void show() {
        // Este método se usa cuando la pantalla se muestra
    }

    @Override
    public void hide() {
        // Este método se usa cuando la pantalla se oculta
    }

    @Override
    public void pause() {
        // Este método se usa cuando la pantalla se pausa
    }

    @Override
    public void resume() {
        // Este método se usa cuando la pantalla se reanuda
    }

    // Método para guardar el resultado de la partida (victoria o derrota)
    public void guardarResultadoPartida(String mapa, boolean gano) {
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()); // Fecha de la partida
        String resultado = gano ? "Ganó" : "Perdió"; // Resultado de la partida

        // Guardar en la base de datos (función personalizada en la clase Main)
        ((Main) Gdx.app.getApplicationListener()).getDatabase().insertarPartida(mapa, fecha, resultado);
        System.out.println("Partida guardada: " + mapa + " | " + fecha + " | " + resultado);
    }
}
