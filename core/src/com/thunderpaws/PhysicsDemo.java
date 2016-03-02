package com.thunderpaws;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
/**
 * Created by dewit on 2/14/16.
 */
public class PhysicsDemo extends ApplicationAdapter{
    SpriteBatch batch;
    Sprite sprite;
    Texture img;
    World world;
    Body body;
    Box2DDebugRenderer debugrenderer;
    Matrix4 debugMatrix;
    OrthographicCamera camera;

    float torque = 0.0f;
    boolean drawSprite = true;

    final float PIXELS_TO_METERS = 100f;


    @Override
    public void create(){
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
        sprite = new Sprite(img);

        //Center the sprite in the top/middle of the screen
        sprite.setPosition(Gdx.graphics.getWidth() / 2 - sprite.getWidth() / 2, Gdx.graphics.getHeight() / 2);

        //Create a physics world, the heart of the simulation. The Vector passed in is gravity
        world = new World(new Vector2(0,-98f), true);

        //Now create a BodyDefinition. This defines the physics objects tyoe and position in the simulation
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        //We are going to use 1 to 1 dimensions. Meaning 1 in physics engine is 1 pixel
        //Set our body to the same posiiton as our sprite
        bodyDef.position.set(sprite.getX(), sprite.getY());

        //Crete a body in the world using our definition
        body = world.createBody(bodyDef);

        //Now define the dimensions of the physics shape
        PolygonShape shape = new PolygonShape();

        //We are a box, so this makes sense no?
        //Basically set the physics polygon to a box with the same dimensions as our sprite
        shape.setAsBox(sprite.getWidth()/2, sprite.getHeight()/2);

        //FixtureDef is a confusing expression for physical properties
        //Basically this is where you, in addition to defining the shape of the body
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;

        Fixture fixture = body.createFixture(fixtureDef);

        //Shape is the only disposable of the lot so get rid of it
        shape.dispose();

    }

    @Override
    public void render(){
        //Advance in the world by the amount og time that has elapsed since the last frame.
        //Generally in the real game don't do this in the render loop, as oyu are tying the physics
        //Instead update rate to the frame rate and via vera
        world.step(Gdx.graphics.getDeltaTime(), 6, 2);

        //Now update the sprite position accordingly to its now updated physics bodu
        sprite.setPosition(body.getPosition().x, body.getPosition().y);

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(sprite, sprite.getX(), sprite.getY());
        batch.end();
    }

    @Override
    public void dispose(){
        img.dispose();
        world.dispose();
    }

}
