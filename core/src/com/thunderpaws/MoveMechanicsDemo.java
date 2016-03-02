package com.thunderpaws;

/**
 * Created by dewit on 2/8/16.
 */
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class MoveMechanicsDemo extends ApplicationAdapter implements InputProcessor {
    SpriteBatch batch;
    Sprite sprite;
    Texture img;
    World world;
    Body body;
    Box2DDebugRenderer debugRenderer;
    Matrix4 debugMatrix;
    OrthographicCamera camera;
    Fixture bodyPhysicsFixture;
    BitmapFont font;
    Vector3 point = new Vector3();
    boolean jump = false;
    boolean secondJump = false;
    boolean downAction = false;


    boolean drawPhysicsBoundry = true;

    final float ptm = 100f;

    @Override
    public void create(){
        batch = new SpriteBatch();
        img = new Texture("data/KUNGFU_CAT_64.png");
        //img = new Texture("data/KUNGFU_CAT_128.png");
        //img = new Texture("data/KUNGFU_CAT_256.png");
        sprite = new Sprite(img);

        //set the position of the sprite in the top/middle of the screen
        sprite.setPosition(-sprite.getWidth()/2, -sprite.getHeight()/2);

        world = new World(new Vector2(0, -9.8f), true);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;//movable, and collision detection
        //position the physics body where the pixel sprite's left corner + width/2 and height/2 which is the middle of the sprite
        bodyDef.position.set((sprite.getX() + sprite.getWidth()/2)/ptm, (sprite.getY() + sprite.getHeight()/2)/ptm);

        //create the body in the world
        body = world.createBody(bodyDef);
        //build a box given half width and half height dimensions
        PolygonShape shape = new PolygonShape();
        //construct the shape as big as the sprite but in meters so pixels 256X256 px = 2.5X2.5 m
        shape.setAsBox((sprite.getWidth()/2)/ptm, (sprite.getHeight()/2)/ptm);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        bodyPhysicsFixture = body.createFixture(fixtureDef);
        shape.dispose();

        Gdx.input.setInputProcessor(this);

        //Create a box2ddebugrenderer, this allows us to see the physics simulation controlling the scene
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //create ground
        BodyDef groundDef = new BodyDef();
        groundDef.position.set(0.0f,0);//set at point 0x0
        Body groundBody = world.createBody(groundDef);
        System.out.println("ground body.pos = " + groundBody.getPosition());
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox((Gdx.graphics.getWidth())/ptm, 0);
        groundBody.createFixture(groundBox, 0);
        groundBox.dispose();

        font = new BitmapFont();

    }


    @Override
    public void render(){
        //SHOULD be making the camera follow the player...but ittis not...
        //Update**
            //So it WAS following the player, but forgot to scale
        camera.position.set(body.getPosition().x*ptm, body.getPosition().y*ptm, 0);
        camera.update();

        //step the physics simulation forward at a rate of 60hz
        world.step(1f/60f, 6, 2);

        //set the sprites position from the updated physics body locatio
        //                      meters       *  conversion    pixels                meters      * conversion        pixels
        sprite.setPosition((body.getPosition().x * ptm) - sprite.getWidth()/2, (body.getPosition().y * ptm) - sprite.getHeight()/2);


        //ditto for the rotation                    rads
        sprite.setRotation((float)Math.toDegrees(body.getAngle()));
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);//not sure but necessary

        batch.setProjectionMatrix(camera.combined);

        //scale down the sprite batches projection matrix to box2d size pixels -> meters (256 x 256 = 2.56 x 2.56)
        debugMatrix = batch.getProjectionMatrix().cpy().scale(ptm, ptm, 0);


        Vector2 vel = body.getLinearVelocity();
        Vector2 pos = body.getPosition();

        boolean grounded = isPlayerGrounded(Gdx.graphics.getDeltaTime());

        float stillTime = 0f;

        // calculate stilltime & damp
        if(!Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.D)) {
            stillTime += Gdx.graphics.getDeltaTime();
            body.setLinearVelocity(vel.x * 0.9f, vel.y);
        }
        else {
            stillTime = 0;
        }

        // disable friction while jumping
        if(!grounded) {
            bodyPhysicsFixture.setFriction(0f);
        } else {
            if(!Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.D) && stillTime > 0.2) {
                bodyPhysicsFixture.setFriction(100f);
            }else {
                bodyPhysicsFixture.setFriction(0.2f);
            }
            //extra impulse for when the top of the platform is reached
            //if(groundedPlatform != null && groundedPlatform.dist == 0) {
            //    player.applyLinearImpulse(0f, -24f, pos.x, pos.y, true);
            //}
        }

        // apply left impulse, but only if max velocity is not reached yet
        if(Gdx.input.isKeyPressed(Input.Keys.A) && vel.x > -3f) {
            body.applyLinearImpulse(-2f, 0f, pos.x, pos.y, true);
        }

        // apply right impulse, but only if max velocity is not reached yet
        if(Gdx.input.isKeyPressed(Input.Keys.D) && vel.x < 3f) {
            body.applyLinearImpulse(2f, 0f, pos.x, pos.y, true);
        }

        // jump
        if(jump) {
            jump = false;
            if(grounded) {
                secondJump = true;
                body.setLinearVelocity(vel.x, 0);
                System.out.println("jump before: " + body.getLinearVelocity());
                body.setTransform(pos.x, pos.y + 0.01f, 0);
                System.out.println("player mass =  " + body.getMass());
                body.applyLinearImpulse(0f, 5f, pos.x, pos.y, true);
                System.out.println("jump, " + body.getLinearVelocity());
            }else{//second jump
                if(!grounded && secondJump) {
                    secondJump = false;
                    body.setLinearVelocity(vel.x, 0);
                    System.out.println("second jump before: " + body.getLinearVelocity());
                    body.setTransform(pos.x, pos.y + 0.01f, 0);
                    body.applyLinearImpulse(0f, 5f, pos.x, pos.y, true);
                    System.out.println("second jump, " + body.getLinearVelocity());
                }
            }
        }

        batch.begin();

        //draw the pixel sprite to the screen
        batch.draw(sprite, sprite.getX(), sprite.getY(),
                sprite.getOriginX(), sprite.getOriginY(),
                sprite.getWidth(), sprite.getHeight(),
                sprite.getScaleX(), sprite.getScaleY(),
                sprite.getRotation());

        font.draw(batch, "friction: " + bodyPhysicsFixture.getFriction() + "\ngrounded: " + grounded, (sprite.getX()+sprite.getWidth()), sprite.getY() + sprite.getHeight()/2);

        batch.end();

        if(drawPhysicsBoundry){
            debugRenderer.render(world, debugMatrix);
        }

    }

    private boolean isPlayerGrounded(float deltaTime) {
        //groundedPlatform = null;
        Array<Contact> contactList = world.getContactList();
        for(int i = 0; i < contactList.size; i++) {
            Contact contact = contactList.get(i);
            if(contact.isTouching() && (contact.getFixtureA() == bodyPhysicsFixture ||
                    contact.getFixtureB() == bodyPhysicsFixture)) {
                Vector2 pos = body.getPosition();
                WorldManifold manifold = contact.getWorldManifold();
                boolean below = true;
                for(int j = 0; j < manifold.getNumberOfContactPoints(); j++) {
                    //System.out.println("manifold.getPoints()[j].y = " + manifold.getPoints()[j].y + "" +
                    //        "\npos.y = " + pos.y + "" +
                    //        "\nbody.position.y = " + body.getPosition().y + "" +
                    //        "\n(sprite.getHeight()/2)/ptm) = " + (sprite.getHeight()/2)/ptm);
                    below &= (manifold.getPoints()[j].y < pos.y - ((sprite.getHeight()/2)/ptm));
                }

                if(below) {
                    /*if(contact.getFixtureA().getUserData() != null && contact.getFixtureA().getUserData().equals("p")) {
                        groundedPlatform = (MovingPlatform)contact.getFixtureA().getBody().getUserData();
                    }

                    if(contact.getFixtureB().getUserData() != null && contact.getFixtureB().getUserData().equals("p")) {
                        groundedPlatform = (MovingPlatform)contact.getFixtureB().getBody().getUserData();
                    }*/
                    return true;
                }

                return false;
            }
        }
        return false;
    }

    @Override
    public void dispose(){
        img.dispose();
        world.dispose();
    }


    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.W) {
            jump = true;
        }else if(keycode == Input.Keys.S){
            downAction = true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        // The ESC key toggles the visibility of the sprite allow user to see
        //physics debug info
        if(keycode == Input.Keys.ESCAPE) {
            drawPhysicsBoundry = !drawPhysicsBoundry;
        }
        if(keycode == Input.Keys.W) {
            jump = false;
        }else if(keycode == Input.Keys.S){
            downAction = false;
        }

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
