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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class SceneGroupingDemo implements  ApplicationListener{
    private Stage stage;
    private Group group;

    @Override
    public void create() {
        //below declaration didn't work with current version but the tutorial had it as such...
        //stage = new Stage(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        stage = new Stage();
        final TextureRegion jetTexture = new TextureRegion(new Texture("data/jet.png"));
        final TextureRegion exhaustTexture = new TextureRegion(new Texture("data/exhaust.png"));

        final Actor jet = new Actor(){
            public void draw(Batch batch, float alpha){
                batch.draw(jetTexture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(),
                        getScaleX(), getScaleY(), getRotation());
            }
        };
        jet.setBounds(jet.getX(), jet.getY(), jetTexture.getRegionWidth(), jetTexture.getRegionHeight());

        final Actor exhaust = new Actor(){
            public void draw(Batch batch, float alpha){
                batch.draw(exhaustTexture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(),
                        getScaleX(), getScaleY(), getRotation());
            }
        };
        exhaust.setBounds(0,0, exhaustTexture.getRegionWidth(), exhaustTexture.getRegionHeight());
        exhaust.setPosition(jet.getWidth()-25, 75);

        group = new Group();
        group.addActor(jet);
        group.addActor(exhaust);

        group.addAction(parallel(moveTo(200,0,5),rotateBy(90,5)));

        stage.addActor(group);
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
