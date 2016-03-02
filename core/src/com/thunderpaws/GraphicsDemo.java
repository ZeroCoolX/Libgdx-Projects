package com.thunderpaws;

/**
 * Created by dewit on 2/8/16.
 */

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.Input;


public class GraphicsDemo implements ApplicationListener{
    private SpriteBatch batch;
    private TextureAtlas textureAtlas;
    private Animation animation;
    private float elapsedTime = 0;
    private Texture texture;
    private Sprite sprite;

    @Override
    public void create() {
        batch = new SpriteBatch();
        //textureAtlas = new TextureAtlas(Gdx.files.internal("data/spritesheet.atlas"));
        //animation = new Animation(1/15f, textureAtlas.getRegions());
        texture = new Texture(Gdx.files.internal("data/0001.png"));
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        sprite = new Sprite(texture);
        sprite.setPosition(w/2 - sprite.getWidth()/2, h/2 - sprite.getHeight()/2);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1,1,1,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //keyboard movement

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)){
                sprite.translateX(-1f);
            }else{
                sprite.translateX(-10.0f);
            }
        }
        if(Gdx.input.isKeyPressed((Input.Keys.RIGHT))){
            if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)){
                sprite.translateX(1f);
            }else{
                sprite.translateX(10.0f);
            }
        }

        //mouse input
        /*if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            sprite.setPosition(Gdx.input.getX() - sprite.getWidth()/2,
                    Gdx.graphics.getHeight() - Gdx.input.getY() - sprite.getHeight()/2);
        }
        if(Gdx.input.isKeyPressed((Input.Buttons.RIGHT))){
            sprite.setPosition(Gdx.graphics.getWidth()/2 - sprite.getWidth()/2,
                    Gdx.graphics.getHeight()/2 - sprite.getHeight()/2);
        }*/

        batch.begin();
        sprite.draw(batch);
        batch.end();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        texture.dispose();
    }
}
