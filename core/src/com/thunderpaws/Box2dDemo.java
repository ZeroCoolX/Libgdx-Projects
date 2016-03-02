package com.thunderpaws;

import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dewit on 2/16/16.
 */
public class Box2dDemo extends InputAdapter implements ApplicationListener {
    final static float MAX_VELOCITY = 7f;
    boolean jump = false;
    boolean secondJump = false;
    boolean downAction = false;
    World world;
    Body player;
    Fixture playerPhysicsFixture;
    Fixture playerSensorFixture;
    OrthographicCamera cam;
    Box2DDebugRenderer renderer;
    Array<MovingPlatform> platforms = new Array<MovingPlatform>();
    MovingPlatform groundedPlatform = null;
    float stillTime = 0;
    long lastGroundTime = 0;
    SpriteBatch batch;
    BitmapFont font;

    @Override
    public void create() {
        world = new World(new Vector2(0, -20), true);

        //override contactListener - unnecessary at the moment
        world.setContactListener(new ContactListener() {

            @Override
            public void beginContact(Contact contact) {

            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void postSolve(Contact arg0, ContactImpulse arg1) {
                // TODO Auto-generated method stub
            }

            @Override
            public void preSolve(Contact arg0, Manifold arg1) {
                // TODO Auto-generated method stub
            }
        });

        renderer = new Box2DDebugRenderer();
        cam = new OrthographicCamera(28, 20);
        createWorld();
        Gdx.input.setInputProcessor(this);
        batch = new SpriteBatch();
        font = new BitmapFont();

    }

    private void createWorld() {
        float y1 = 1; //(float)Math.random() * 0.1f + 1;
        float y2 = y1;
        for(int i = 0; i < 50; i++) {
            Body ground = createEdge(BodyType.StaticBody, -50 + i * 2, y1, -50 + i * 2 + 2, y2, 0);
            y1 = y2;
            y2 = 1; //(float)Math.random() + 1;
        }

        Body box = createBox(BodyType.StaticBody, 1, 1, 0);
        box.setTransform(30, 3, 0);
        box = createBox(BodyType.StaticBody, 1.2f, 1.2f, 0);
        box.setTransform(5, 2.4f, 0);
        box = createBox(BodyType.StaticBody, 5f, 0.2f, 0);
        box.setTransform(40, 5f, 0);
        player = createPlayer();
        player.setTransform(10.0f, 4.0f, 0);
        player.setFixedRotation(true);

        for(int i = 0; i < 20; i++) {
            box = createBox(BodyType.DynamicBody, (float)Math.random(), (float)Math.random(), 3);
            box.setTransform((float)Math.random() * 10f - (float)Math.random() * 10f, (float)Math.random() * 10 + 6, (float)(Math.random() * 2 * Math.PI));
        }

        for(int i = 0; i < 20; i++) {
            Body circle = createCircle(BodyType.DynamicBody, (float)Math.random() * 0.5f, 3);
            circle.setTransform((float)Math.random() * 10f - (float)Math.random() * 10f, (float)Math.random() * 10 + 6, (float)(Math.random() * 2 * Math.PI));
        }

        platforms.add(new MovingPlatform(-2, 3, 2, 0.5f, 2, 0, 4));
        platforms.add(new MovingPlatform(17, 3, 5, 0.5f, 0, 2, 5));
        platforms.add(new MovingPlatform(-7, 5, 2, 0.5f, -2, 2, 8));
//		platforms.add(new MovingPlatform(40, 3, 20, 0.5f, 0, 2, 5));

    }


    private Body createBox(BodyType type, float width, float height, float density) {
        BodyDef def = new BodyDef();
        def.type = type;
        Body box = world.createBody(def);

        PolygonShape poly = new PolygonShape();
        poly.setAsBox(width, height);
        box.createFixture(poly, density);
        poly.dispose();

        return box;
    }

    private Body createEdge(BodyType type, float x1, float y1, float x2, float y2, float density) {
        BodyDef def = new BodyDef();
        def.type = type;
        Body box = world.createBody(def);

        PolygonShape poly = new PolygonShape();
        poly.setAsBox(x2-x1,y2-y1);
        box.createFixture(poly, density);
        box.setTransform(x1, y1, 0);
        poly.dispose();

        return box;
    }

    private Body createCircle(BodyType type, float radius, float density) {
        BodyDef def = new BodyDef();
        def.type = type;
        Body box = world.createBody(def);

        CircleShape poly = new CircleShape();
        poly.setRadius(radius);
        box.createFixture(poly, density);
        poly.dispose();

        return box;
    }

    private Body createPlayer() {
        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        Body box = world.createBody(def);

        PolygonShape poly = new PolygonShape();
        poly.setAsBox(0.45f, 1.4f);
        playerPhysicsFixture = box.createFixture(poly, 1);
        poly.dispose();

        CircleShape circle = new CircleShape();
        circle.setRadius(0.45f);
        circle.setPosition(new Vector2(0, -1.4f));
        playerSensorFixture = box.createFixture(circle, 0);
        circle.dispose();

        box.setBullet(true);

        return box;
    }

    @Override
    public void resume() {

    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        cam.position.set(player.getPosition().x, player.getPosition().y, 0);
        cam.update();
        //batch.setProjectionMatrix(cam.combined);
        //cam.apply(Gdx.gl20);
        renderer.render(world, cam.combined);
        //tiledMapRenderer.setView(cam);
        //tiledMapRenderer.render();

        Vector2 vel = player.getLinearVelocity();
        Vector2 pos = player.getPosition();
        boolean grounded = isPlayerGrounded(Gdx.graphics.getDeltaTime());
        if(grounded) {
            lastGroundTime = System.nanoTime();
        } else {
            if(System.nanoTime() - lastGroundTime < 100000000) {
                grounded = true;
            }
        }

        // cap max velocity on x
        if(Math.abs(vel.x) > MAX_VELOCITY) {
            vel.x = Math.signum(vel.x) * MAX_VELOCITY;
            player.setLinearVelocity(vel.x, vel.y);
        }

        // calculate stilltime & damp
        if(!Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D)) {
            stillTime += Gdx.graphics.getDeltaTime();
            player.setLinearVelocity(vel.x * 0.9f, vel.y);
        }
        else {
            stillTime = 0;
        }

        // disable friction while jumping
        if(!grounded) {
            playerPhysicsFixture.setFriction(0f);
            playerSensorFixture.setFriction(0f);
        } else {
            if(!Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D) && stillTime > 0.2) {
                playerPhysicsFixture.setFriction(100f);
                playerSensorFixture.setFriction(100f);
            }else {
                playerPhysicsFixture.setFriction(0.2f);
                playerSensorFixture.setFriction(0.2f);
            }
            //extra impulse for when the top of the platform is reached
            if(groundedPlatform != null && groundedPlatform.dist == 0) {
                player.applyLinearImpulse(0f, -24f, pos.x, pos.y, true);
            }
        }

        // apply left impulse, but only if max velocity is not reached yet
        if(Gdx.input.isKeyPressed(Keys.A) && vel.x > -MAX_VELOCITY) {
            player.applyLinearImpulse(-2f, 0f, pos.x, pos.y, true);
        }

        // apply right impulse, but only if max velocity is not reached yet
        if(Gdx.input.isKeyPressed(Keys.D) && vel.x < MAX_VELOCITY) {
            player.applyLinearImpulse(2f, 0f, pos.x, pos.y, true);
        }

        //eaither crouch or drown down a level
        if(downAction){
            downAction = false;
            if(grounded){
                player.setLinearVelocity(vel.x, 0);
                System.out.println("down before: " + player.getLinearVelocity());
                player.setTransform(pos.x, pos.y - 0.01f, 0);
                player.applyLinearImpulse(0f, -30f, pos.x, pos.y, true);
                System.out.println("down, " + player.getLinearVelocity());
            }
        }

        // jump
        if(jump) {
            jump = false;
            if(grounded) {
                secondJump = true;
                player.setLinearVelocity(vel.x, 0);
                System.out.println("jump before: " + player.getLinearVelocity());
                player.setTransform(pos.x, pos.y + 0.01f, 0);
                System.out.println("player mass =  " + player.getMass());
                player.applyLinearImpulse(0f, 30f, pos.x, pos.y, true);
                System.out.println("jump, " + player.getLinearVelocity());
            }else{//second jump
                if(!grounded && secondJump) {
                    secondJump = false;
                    player.setLinearVelocity(vel.x, 0);
                    System.out.println("second jump before: " + player.getLinearVelocity());
                    player.setTransform(pos.x, pos.y + 0.01f, 0);
                    player.applyLinearImpulse(0f, 30f, pos.x, pos.y, true);
                    System.out.println("second jump, " + player.getLinearVelocity());
                }
            }
        }

        // update platforms
        for(int i = 0; i < platforms.size; i++) {
            MovingPlatform platform = platforms.get(i);
            platform.update(Math.max(1/30.0f, Gdx.graphics.getDeltaTime()));
        }

        // le step...
        world.step(Gdx.graphics.getDeltaTime(), 4, 4);
        player.setAwake(true);

        //tiledMapRenderer.setView(cam);
        //tiledMapRenderer.render();

        //cam.project(point.set(pos.x, pos.y, 0));
        batch.begin();
        font.draw(batch, "friction: " + playerPhysicsFixture.getFriction() + "\ngrounded: " + grounded, point.x+20, point.y);
        batch.end();
    }

    private boolean isPlayerGrounded(float deltaTime) {
        groundedPlatform = null;
        Array<Contact> contactList = world.getContactList();
        for(int i = 0; i < contactList.size; i++) {
            Contact contact = contactList.get(i);
            if(contact.isTouching() && (contact.getFixtureA() == playerSensorFixture ||
                    contact.getFixtureB() == playerSensorFixture)) {
                Vector2 pos = player.getPosition();
                WorldManifold manifold = contact.getWorldManifold();
                boolean below = true;
                for(int j = 0; j < manifold.getNumberOfContactPoints(); j++) {
                    below &= (manifold.getPoints()[j].y < pos.y - 1.5f);
                }

                if(below) {
                    if(contact.getFixtureA().getUserData() != null && contact.getFixtureA().getUserData().equals("p")) {
                        groundedPlatform = (MovingPlatform)contact.getFixtureA().getBody().getUserData();
                    }

                    if(contact.getFixtureB().getUserData() != null && contact.getFixtureB().getUserData().equals("p")) {
                        groundedPlatform = (MovingPlatform)contact.getFixtureB().getBody().getUserData();
                    }
                    return true;
                }

                return false;
            }
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Keys.W) {
            jump = true;
        }else if(keycode == Keys.S){
            downAction = true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == Keys.W) {
            jump = false;
        }else if(keycode == Keys.S){
            downAction = false;
        }
        return false;
    }

    Vector2 last = null;
    Vector3 point = new Vector3();

    @Override
    public boolean touchDown(int x, int y, int pointerId, int button) {
        cam.unproject(point.set(x, y, 0));

        if(button == Input.Buttons.LEFT) {
            if(last == null) {
                last = new Vector2(point.x, point.y);
            } else {
                createEdge(BodyType.StaticBody, last.x, last.y, point.x, point.y, 0);
                last.set(point.x, point.y);
            }
        } else {
            last = null;
        }

        return false;
    }



    class MovingPlatform {
        Body platform;
        Vector2 pos = new Vector2();
        Vector2 dir = new Vector2();
        float dist = 0;
        float maxDist = 0;

        public MovingPlatform(float x, float y, float width, float height, float dx, float dy, float maxDist) {
            platform = createBox(BodyType.KinematicBody, width, height, 1);
            pos.x = x;
            pos.y = y;
            dir.x = dx;
            dir.y = dy;
            this.maxDist = maxDist;
            platform.setTransform(pos, 0);
            platform.getFixtureList().get(0).setUserData("p");
            platform.setUserData(this);
        }

        public void update(float deltaTime) {
            dist += dir.len() * deltaTime;
            if(dist > maxDist) {
                dir = new Vector2(dir.x * -1f, dir.y * -1f);//reset in the opposite direction
                dist = 0;
            }

            platform.setLinearVelocity(dir);
        }
    }
}
