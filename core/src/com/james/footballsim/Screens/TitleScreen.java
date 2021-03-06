package com.james.footballsim.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.james.footballsim.FootballSim;

/**
 * Created by James on 04/04/2018.
 */
public class TitleScreen extends CustomGameScreen {

    //Labels
    private Label title;

    //Buttons
    private TextButton playButton;

    public TitleScreen(FootballSim aGame) {
        super(aGame);
        title = new Label("Football Sim", FootballSim.skin,"title");
        title.setAlignment(Align.center);

        stage.addActor(title);

        playButton = new TextButton("Play!",FootballSim.skin);
        playButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                aGame.setScreen(TitleScreen.this, FootballSim.SCREENS.TEAM_SELECTION,FootballSim.IN,transitionDuration);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        stage.addActor(playButton);



    }

    @Override
    public void show() {
        super.show();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        stage.act();
        stage.draw();
    }


    @Override
    public void updateUI(float width, float height) {
        title.setY(height*2/3);
        title.setWidth(width);

        playButton.pad(0,30,0,30);
        playButton.setPosition(width/2-playButton.getWidth()/2, height/2-playButton.getHeight()/2);
    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
