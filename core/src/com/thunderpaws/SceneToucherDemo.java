package com.thunderpaws;

/**
 * Created by dewit on 2/9/16.
 */

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import java.util.Random;
public class SceneToucherDemo implements  ApplicationListener{
    class Jet extends Actor{
        private TextureRegion _texture;

        public Jet(TextureRegion texture){
            _texture = texture;
            setBounds(getX(), getY(), _texture.getRegionWidth(), _texture.getRegionHeight());

            this.addListener(new InputListener(){
               public boolean touchDown(InputEvent event, float x, float y, int pointer, int buttons){
                   System.out.println("Touched " + getName());
                   setVisible(false);
                   return true;
               }
            });
        }

        //Implement the full form of draw() so we can handle rotation and scaling
        public void draw(Batch batch, float alpha){
            batch.draw(_texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(),
                    getHeight(), getScaleX(), getScaleY(), getRotation());
        }

        //This hit() instead of checking against a bounding box checks a bouncing circle
        public Actor hit(float x, float y, boolean touchable){
            //if this actor is hidden or untouchable it cannot be hit
            if(!this.isVisible() || this.getTouchable() == Touchable.disabled){
                return null;
            }

            //Get centerpoint of bounding circle, also known as the center of the rect
            float centerX = getWidth()/2;
            float centerY = getHeight()/2;

            //square roots are bad m'kay. In "real" code, simply square both sides for much speedy fastness
            //This however is the proper, unoptimized and easiest to grasp equation for a hit within a circle
            //You could of course use LibGDX's Circle class instead..

            //Calculate radius of circle
            float radius = (float)Math.sqrt(centerX * centerX + centerY * centerY);//clever girl

            //and distance of point from the center of circle
            float distance = (float) Math.sqrt(((centerX - x) * (centerX - x)) +  ((centerY - y) * (centerY - y)));

            //if the distance is less than the circle radius its a hit
            if(distance <= radius){
                return this;
            }

            //failsafe return null
            return null;
        }
    }

    private Jet[] jets;
    private Stage stage;

    @Override
    public void create() {
        stage = new Stage();
        final TextureRegion jetTexture = new TextureRegion(new Texture("data/jet.png"));

        jets = new Jet[10];

        //Create/seed our random number for positioning jets randomly
        Random rand = new Random();

        //Create 10 jets at random screen locations
        for(int i = 0; i < 10; ++i){
            jets[i] = new Jet(jetTexture);

            //Assign the position of the jet to a random value within the screen boundries
            jets[i].setPosition(rand.nextInt(Gdx.graphics.getWidth() - (int)jets[i].getWidth()),
                    rand.nextInt(Gdx.graphics.getHeight() - (int)jets[i].getHeight()));

            //set the name of the jet to its index within the loop
            jets[i].setName(Integer.toString(i));

            //Add them to the stage
            stage.addActor(jets[i]);
        }
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
