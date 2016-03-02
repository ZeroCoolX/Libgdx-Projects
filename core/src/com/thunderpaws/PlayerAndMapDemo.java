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
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class PlayerAndMapDemo extends ApplicationAdapter implements InputProcessor {
    /** The player character, has state and state time, */
    static class Player {
        static float WIDTH;
        static float HEIGHT;
        static float MAX_VELOCITY = 10f;
        static float JUMP_VELOCITY = 40f;
        static float DAMPING = 0.87f;

        enum State {
            Standing, Walking, Jumping
        }

        final Vector2 position = new Vector2();
        final Vector2 velocity = new Vector2();
        State state = State.Walking;
        float stateTime = 0;
        boolean facesRight = true;
        boolean grounded = false;
    }

    private TiledMapTileLayer collisionLayer;
    boolean collisionX = false;
    boolean collisionY = false;
    float tileWidth;
    float tileHeight;
    Batch batch;
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
    private OrthogonalTiledMapRenderer renderer;
    private TiledMap map;


    boolean jump = false;
    boolean secondJump = false;
    boolean downAction = false;
    //spritesheet dimensions: 360x42 (8 characters) so 45X42 for each
    private static final int        chars = 8;

    Texture                         walkSheet;
    TextureRegion[]                 animationFrames;
    private Animation stand;
    private Animation walk;
    TextureRegion                   currentFrame;

    boolean drawPhysicsBoundry = true;

    final float ptm = 100f;

    @Override
    public void create(){
        //batch = new SpriteBatch();
        img = new Texture("data/KUNGFU_CAT_64.png");
        //img = new Texture("data/KUNGFU_CAT_128.png");
        //img = new Texture("data/KUNGFU_CAT_256.png");
        sprite = new Sprite(img);

        //set the position of the sprite in the top/middle of the screen
        sprite.setPosition((-sprite.getWidth()/2)+Gdx.graphics.getWidth()/2, (-sprite.getHeight()/2)+Gdx.graphics.getHeight()/2);
        System.out.println("setting sprite positioning at ("+sprite.getX()+","+sprite.getY()+")" );
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

    //load the catapults!! noo. jk. map. load the map.
        map = new TmxMapLoader().load("fuCatLevelDemo.tmx");
        renderer = new OrthogonalTiledMapRenderer(map);
        Gdx.input.setInputProcessor(this);

        collisionLayer = (TiledMapTileLayer) map.getLayers().get(1);

        tileWidth = collisionLayer.getTileWidth();
        tileHeight = collisionLayer.getTileHeight();
        System.out.println("tile width and height = " + tileWidth + "," + tileHeight);
        //Create a box2ddebugrenderer, this allows us to see the physics simulation controlling the scene
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //create ground - 96 pixels up or 9.6 meters up
        BodyDef groundDef = new BodyDef();
        groundDef.position.set(0.0f,0.96f);//set at point 0x0
        Body groundBody = world.createBody(groundDef);
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox((Gdx.graphics.getWidth())/ptm, 0);
        groundBody.createFixture(groundBox, 0);
        groundBox.dispose();

        //createAnimations();

        // create the Koala we want to move around the world
        //Player player = new Player();
        //player.position.set(20, 20);

        font = new BitmapFont();


    }

    private void checkCollisions(){
        System.out.println("sprite position at ("+sprite.getX()+","+sprite.getY()+")");
        System.out.println(sprite.getY()+sprite.getHeight()/2);
        System.out.println("checking the following cells for collision:\n" +
                "("+(int)(sprite.getX()/tileWidth)+","+ (int)(tileHeight - ((sprite.getY()+sprite.getHeight())/tileHeight))+")\n" +
                "("+(int)(sprite.getX()/tileWidth)+","+ (int)(tileHeight - ((sprite.getY()+(sprite.getHeight()/2))/tileHeight))+")\n" +
                "("+(int)(sprite.getX()/tileWidth)+","+ (int)(tileHeight - ((sprite.getY())/tileHeight))+")\n" +
                "("+(int)((sprite.getX()+sprite.getWidth())/tileWidth)+","+ (int)(tileHeight - ((sprite.getY()+sprite.getHeight())/tileHeight))+")\n" +
                "("+(int)((sprite.getX()+sprite.getWidth())/tileWidth)+","+ (int)(tileHeight - ((sprite.getY()+(sprite.getHeight()/2))/tileHeight))+")\n" +
                "("+(int)((sprite.getX()+sprite.getWidth())/tileWidth)+","+ (int)(tileHeight - ((sprite.getY())/tileHeight))+")\n" +
                "("+(int)(sprite.getX()/tileWidth)+","+ (int)(tileHeight - ((sprite.getY()+sprite.getHeight())/tileHeight))+")\n" +
                "("+(int)((sprite.getX()+sprite.getWidth()/2)/tileWidth)+","+ (int)(tileHeight - ((sprite.getY()+sprite.getHeight())/tileHeight))+")\n" +
                "("+(int)((sprite.getX()+sprite.getWidth())/tileWidth)+","+ (int)(tileHeight - ((sprite.getY()+sprite.getHeight())/tileHeight))+")\n" +
                "("+(int)(sprite.getX()/tileWidth)+","+ (int)(tileHeight - (sprite.getY()/tileHeight))+")\n" +
                "("+(int)((sprite.getX()+(sprite.getWidth()/2))/tileWidth)+","+ (int)(tileHeight - (sprite.getY()/tileHeight))+")\n" +
                "("+(int)((sprite.getX()+sprite.getWidth())/tileWidth)+","+ (int)(tileHeight - ((sprite.getY())/tileHeight))+")\n");

        //moving left check
        if(body.getLinearVelocity().x < 0 ){
            //top left
            if(collisionLayer.getCell((int)(sprite.getX()/tileWidth), (int)(tileHeight - ((sprite.getY()+sprite.getHeight())/tileHeight))) != null) {
                collisionX = collisionLayer.getCell((int) (sprite.getX() / tileWidth), (int) (tileHeight - ((sprite.getY() + sprite.getHeight()) / tileHeight)))
                        .getTile().getProperties().containsKey("blocked");
            }
            //middle left
            if(collisionLayer.getCell((int)(sprite.getX()/tileWidth), (int)(tileHeight - ((sprite.getY()+(sprite.getHeight()/2))/tileHeight)))!= null) {
                collisionX = !collisionX ? collisionLayer.getCell((int) (sprite.getX() / tileWidth), (int) (tileHeight - ((sprite.getY() + (sprite.getHeight() / 2)) / tileHeight)))
                        .getTile().getProperties().containsKey("blocked") : true;
            }
            //bottom left
            if(collisionLayer.getCell((int)(sprite.getX()/tileWidth), (int)(tileHeight - ((sprite.getY())/tileHeight))) != null) {
                collisionX = !collisionX ? collisionLayer.getCell((int) (sprite.getX() / tileWidth), (int) (tileHeight - ((sprite.getY()) / tileHeight)))
                        .getTile().getProperties().containsKey("blocked") : true;
            }
        }
        //moving right check
        if(body.getLinearVelocity().x > 0 ){
            //top right
            if(collisionLayer.getCell((int)((sprite.getX()+sprite.getWidth())/tileWidth), (int)((sprite.getY()+sprite.getHeight())/tileHeight)) != null) {
                collisionX = collisionLayer.getCell((int) ((sprite.getX() + sprite.getWidth()) / tileWidth), (int) ((sprite.getY() + sprite.getHeight()) / tileHeight))
                        .getTile().getProperties().containsKey("blocked");
            }
            //middle right
            if(collisionLayer.getCell((int)((sprite.getX()+sprite.getWidth())/tileWidth), (int)(tileHeight - ((sprite.getY()+(sprite.getHeight()/2))/tileHeight))) != null) {
                collisionX = !collisionX ? collisionLayer.getCell((int) ((sprite.getX() + sprite.getWidth()) / tileWidth), (int) (tileHeight - ((sprite.getY() + (sprite.getHeight() / 2)) / tileHeight)))
                        .getTile().getProperties().containsKey("blocked") : true;
            }
            //bottom right
            if(collisionLayer.getCell((int)((sprite.getX()+sprite.getWidth())/tileWidth), (int)(tileHeight - ((sprite.getY())/tileHeight))) != null) {
                collisionX = !collisionX ? collisionLayer.getCell((int) ((sprite.getX() + sprite.getWidth()) / tileWidth), (int) (tileHeight - ((sprite.getY()) / tileHeight)))
                        .getTile().getProperties().containsKey("blocked") : true;
            }
        }

        if(collisionX){
            body.setLinearVelocity(0f, body.getLinearVelocity().y);
        }

        //moving up check
        if(body.getLinearVelocity().y > 0 ){
            //top left
            if(collisionLayer.getCell((int)(sprite.getX()/tileWidth), (int)(tileHeight - ((sprite.getY()+sprite.getHeight())/tileHeight))) != null) {
                collisionY = collisionLayer.getCell((int) (sprite.getX() / tileWidth), (int) (tileHeight - ((sprite.getY() + sprite.getHeight()) / tileHeight)))
                        .getTile().getProperties().containsKey("blocked");
            }
            //top middle
            if(collisionLayer.getCell((int)((sprite.getX()+sprite.getWidth()/2)/tileWidth), (int)(tileHeight - ((sprite.getY()+sprite.getHeight())/tileHeight))) != null) {
                collisionY = !collisionY ? collisionLayer.getCell((int) ((sprite.getX() + sprite.getWidth() / 2) / tileWidth), (int) (tileHeight - ((sprite.getY() + sprite.getHeight()) / tileHeight)))
                        .getTile().getProperties().containsKey("blocked") : true;
            }
            //top right
            if(collisionLayer.getCell((int)((sprite.getX()+sprite.getWidth())/tileWidth), (int)(tileHeight - ((sprite.getY()+sprite.getHeight())/tileHeight))) != null) {
                collisionY = !collisionY ? collisionLayer.getCell((int) ((sprite.getX() + sprite.getWidth()) / tileWidth), (int) (tileHeight - ((sprite.getY() + sprite.getHeight()) / tileHeight)))
                        .getTile().getProperties().containsKey("blocked") : true;
            }
        }
        //moving down check
        if(body.getLinearVelocity().y < 0 ){
            //bottom left
            if(collisionLayer.getCell((int)(sprite.getX()/tileWidth), (int)(tileHeight - (sprite.getY()/tileHeight))) != null) {
                collisionY = collisionLayer.getCell((int) (sprite.getX() / tileWidth), (int) (tileHeight - (sprite.getY() / tileHeight)))
                        .getTile().getProperties().containsKey("blocked");
            }
            //bottom middle
            if(collisionLayer.getCell((int)((sprite.getX()+(sprite.getWidth()/2))/tileWidth), (int)(tileHeight - (sprite.getY()/tileHeight))) != null) {
                collisionY = !collisionY ? collisionLayer.getCell((int) ((sprite.getX() + (sprite.getWidth() / 2)) / tileWidth), (int) (tileHeight - (sprite.getY() / tileHeight)))
                        .getTile().getProperties().containsKey("blocked") : true;
            }
            //bottom right
            if(collisionLayer.getCell((int)((sprite.getX()+sprite.getWidth())/tileWidth), (int)(tileHeight - ((sprite.getY())/tileHeight))) != null) {
                collisionY = !collisionY ? collisionLayer.getCell((int) ((sprite.getX() + sprite.getWidth()) / tileWidth), (int) (tileHeight - ((sprite.getY()) / tileHeight)))
                        .getTile().getProperties().containsKey("blocked") : true;
            }
        }

        if(collisionY){
            body.setLinearVelocity(body.getLinearVelocity().x, 0f);
        }

    }

    /*private void renderPlayer (float deltaTime) {
        // based on the koala state, get the animation frame
        TextureRegion frame = null;
        switch (player.state) {
            case Standing:
                frame = stand.getKeyFrame(player.stateTime);
                break;
            case Walking:
                frame = walk.getKeyFrame(player.stateTime);
                break;
            case Jumping:
                frame = jump.getKeyFrame(player.stateTime);
                break;
        }

        // draw the koala, depending on the current velocity
        // on the x-axis, draw the koala facing either right
        // or left
        Batch batch = renderer.getBatch();
        batch.begin();
        if (player.facesRight) {
            batch.draw(frame, player.position.x, player.position.y, Player.WIDTH, Player.HEIGHT);
        } else {
            batch.draw(frame, player.position.x + player.WIDTH, player.position.y, -Player.WIDTH, Player.HEIGHT);
        }
        batch.end();
    }*/

    private void createAnimations(){
        walkSheet = new Texture(Gdx.files.internal("data/cats_walking.png")); // #9
        TextureRegion[][] tmp = TextureRegion.split(walkSheet, 45, 41);              // #10


        animationFrames = new TextureRegion[8];
        int index = 0;
        for (int i = 0; i < 8; i++) {
            animationFrames[index++] = tmp[0][i];
        }
        walk = new Animation(0.05f, animationFrames);      // #11
        stand = new Animation(0f, animationFrames[0]);
        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        //stateTime = 0f;                         // #13
    }


    @Override
    public void render(){
        Gdx.gl.glClearColor(0, 0, 0, 0);
        //Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //SHOULD be making the camera follow the player...but ittis not...
        //Update**
        //So it WAS following the player, but forgot to scale ^_^
        camera.position.set(body.getPosition().x*ptm, body.getPosition().y*ptm, 0);
        camera.update();

        // set the TiledMapRenderer view based on what the
        // camera sees, and render the map
        renderer.setView(camera);
        renderer.render();

        //step the physics simulation forward at a rate of 60hz
        world.step(1f/60f, 6, 2);

        //set the sprites position from the updated physics body locatio
        //                      meters       *  conversion    pixels                meters      * conversion        pixels
        sprite.setPosition((body.getPosition().x * ptm) - sprite.getWidth()/2, (body.getPosition().y * ptm) - sprite.getHeight()/2);


        //ditto for the rotation                    rads
        sprite.setRotation((float)Math.toDegrees(body.getAngle()));
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);//not sure but necessary

        batch = renderer.getBatch();


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

        System.out.println("Player body Position:  " + body.getPosition() + "\nPlayer sprite position: (" + sprite.getX() + "," + sprite.getY()+")");
        System.out.println("Tiled Map Renderer Position: ");
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

        checkCollisions();


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
