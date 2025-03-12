package Sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.Gdx;

public class Esquiador {
    public World world;
    public Body body;

    private Texture esquiadorTexture1;
    private Texture esquiadorTexture2;
    private Animation<Texture> esquiadorAnimation;

    private float stateTime; // Contador de tiempo

    public Esquiador(World world,float startX, float startY) {
        this.world = world;
        defineEsquiador(startX, startY);

        // Cargar texturas de la animación
        esquiadorTexture1 = new Texture("esquiador/esquiador1.png");
        esquiadorTexture2 = new Texture("esquiador/esquiador2.png");

        // Crear animación con 0.1 segundos entre frames
        esquiadorAnimation = new Animation<>(0.1f, esquiadorTexture1, esquiadorTexture2);
        esquiadorAnimation.setPlayMode(Animation.PlayMode.LOOP);
        stateTime = 0;
    }

    private void defineEsquiador(float startX, float startY) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(startX,startY);  // Posición inicial
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(5); // Tamaño del esquiador
        fdef.shape = shape;
        body.createFixture(fdef);
    }

    public void move(float x, float y) {
        body.setLinearVelocity(x, y); // Movimiento del esquiador
    }

    public void update(float deltaTime) {
        stateTime += deltaTime; // Actualizar tiempo para la animación
    }

    public void render(SpriteBatch batch) {
        Texture currentFrame = esquiadorAnimation.getKeyFrame(stateTime, true);

        batch.draw(currentFrame, body.getPosition().x - 8, body.getPosition().y - 8, 16, 16);
    }

    public void dispose() {
        esquiadorTexture1.dispose();
        esquiadorTexture2.dispose();
    }
}
