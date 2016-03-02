package com.thunderpaws;

/**
 * Created by dewit on 2/18/16.
 */

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class ScaledThunderPaws implements ApplicationListener, InputProcessor{
    Sprite Circle;
    SpriteBatch batch;
    World world;
    Body circleBody;
    Box2DDebugRenderer renderer;
    // Pixel per X and Y axis
    // The world is going to be 10 width 10 height
    private float PPuX;
    private float PPuY;
    private float WORLD_TO_BOX = .01f;
    private float BOX_TO_WORLD = 100f;
    Matrix4 debugMatrix;
    CircleShape circleShape;
    TiledMap tiledMap;
    OrthographicCamera camera;
    TiledMapRenderer tiledMapRenderer;
    Body player;
    Fixture playerPhysicsFixture;
    Fixture playerSensorFixture;


    @Override
    public void create() {
        Circle = new Sprite(new Texture(Gdx.files.internal("fu_cat.png")));
        Circle.setBounds(4.5f, 8f, 1f, 1f);
        batch = new SpriteBatch();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();



        tiledMap = new TmxMapLoader().load("fuCatLevelDemo.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        Gdx.input.setInputProcessor(this);

    }

    @Override
    public void resize(int width, int height) {
        PPuX = width / 10;
        System.out.println("width = " + width);
        System.out.println("height = " + height);
        PPuY = height / 10;
        world = new World(new Vector2(0, -9.8f), false);
        renderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera(width, height);
        debugMatrix=new Matrix4(camera.combined);


            Circle.setBounds(Circle.getX() * PPuX, Circle.getY() * PPuY, Circle.getWidth() * PPuX, Circle.getHeight() * PPuY);
            Circle.setOrigin(Circle.getWidth()/2, Circle.getHeight()/2);
            BodyDef circleDef = new BodyDef();
            circleDef.type = BodyType.DynamicBody;
            // To allign the Circle sprite with box 2d
            circleDef.position.set(convertToBox(Circle.getX() + Circle.getWidth()/2), convertToBox(Circle.getY() + Circle.getHeight()/2));
            circleBody = world.createBody(circleDef);
            //box2d builds around 0,0 you can see -X and -Y, this makes sure that you only see X,Y
            debugMatrix.translate(-camera.viewportWidth/2, -camera.viewportHeight/2, 0);
            //scale the debug matrix by the scaling so everything looks normal
            debugMatrix.scale(BOX_TO_WORLD, BOX_TO_WORLD, 0);
            circleShape = new CircleShape();
            circleShape.setRadius(convertToBox(Circle.getWidth()/2));

            FixtureDef circleFixture = new FixtureDef();
            circleFixture.shape = circleShape;
            circleFixture.density = 0.4f;
            circleFixture.friction = 0.2f;
            circleFixture.restitution = 1f;



            circleBody.createFixture(circleFixture);
            circleBody.setUserData(Circle);

            //create ground
            BodyDef groundDef = new BodyDef();
            groundDef.position.set(convertToBox(camera.viewportWidth/2),0);

            Body groundBody = world.createBody(groundDef);


            PolygonShape groundBox = new PolygonShape();

            groundBox.setAsBox(convertToBox(camera.viewportWidth/2), 0);
            groundBody.createFixture(groundBox, 0);

        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        def.position.set(0,0);
        Body box = world.createBody(def);

        PolygonShape poly = new PolygonShape();
        poly.setAsBox(0.1f, 0.2f);
        playerPhysicsFixture = box.createFixture(poly, 1);
        poly.dispose();

        CircleShape circle = new CircleShape();
        circle.setRadius(0.1f);
        circle.setPosition(new Vector2(0, -0.2f));
        playerSensorFixture = box.createFixture(circle, 0);
        circle.dispose();

        box.setBullet(true);

        player = box;
        player.setTransform(1.0f, 2.0f, 0);
        player.setFixedRotation(true);


    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.position.set(circleBody.getPosition().x, circleBody.getPosition().y, 0);
        camera.update();
        renderer.render(world, camera.combined);
        System.out.println("player.getPosition().x = " + player.getPosition().x + "\nplayer.getPosition().y = " + player.getPosition().y + "\ncamera.position = " + camera.position);
        Sprite sprite;
        sprite = (Sprite) circleBody.getUserData();
        // set position and width and height and makes sure it is in the center
        sprite.setBounds(convertToWorld(circleBody.getPosition().x)-sprite.getWidth()/2,  convertToWorld(circleBody.getPosition().y)-sprite.getHeight()/2, convertToWorld(circleShape.getRadius()*2), convertToWorld(circleShape.getRadius()*2));

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        System.out.println("Bouncing circle: " + circleBody.getMass() + "\n" +
                            "Player: " + player.getMass());


        //world.step(1/45f, 6, 2);
        world.step(Gdx.graphics.getDeltaTime(), 4, 4);
        player.setAwake(true);
        //camera.project(point.set(player.getPosition().x, player.getPosition().y, 0));



        //logger.log();
        batch.begin();
        //Circle.draw(batch);
        sprite.draw(batch);
        batch.end();
    }

    Vector3 point = new Vector3();


    @Override
    public void pause() {


    }

    @Override
    public void resume() {


    }


    @Override
    public void dispose() {
        //world.dispose();

    }
    public float convertToBox(float x){
        return x * WORLD_TO_BOX;
    }

    public float convertToWorld(float x){
        return x * BOX_TO_WORLD;
    }

    @Override
    public boolean keyDown(int keycode) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        System.out.println(screenX+" " +screenY);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        // TODO Auto-generated method stub
        return false;
    }
}
