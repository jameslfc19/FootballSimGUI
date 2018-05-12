package com.james.footballsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.ixeption.libgdx.transitions.FadingGame;
import com.ixeption.libgdx.transitions.ScreenTransition;
import com.ixeption.libgdx.transitions.impl.SlidingTransition;
import com.james.footballsim.Screens.PlayersList;
import com.james.footballsim.Screens.Screens;
import com.james.footballsim.Simulator.League;
import com.james.footballsim.Simulator.Team;
import uk.co.codeecho.fixture.generator.FixtureGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class FootballSim extends FadingGame {
	public static Skin skin;

	public static Info info;
    private static FixtureGenerator fixtureGenerator;

	public static Screens SCREENS;
	public static FileSave fileSave;
	public static ScreenTransition IN =  new SlidingTransition(SlidingTransition.Direction.LEFT,Interpolation.exp10Out,false);
	public static ScreenTransition OUT =  new SlidingTransition(SlidingTransition.Direction.RIGHT,Interpolation.exp10Out,true);

	@Override
	public void create () {
		super.create();

		fileSave = new FileSave();
        fileSave.kryo().register(Info.class, new CompatibleFieldSerializer(fileSave.kryo(),Info.class));
        fileSave.kryo().setReferences(false);

		skin = new Skin(Gdx.files.internal("skin/footballsim.json")) {
			//Override json loader to process FreeType fonts from skin JSON
			@Override
			protected Json getJsonLoader(final FileHandle skinFile) {
				Json json = super.getJsonLoader(skinFile);
				final Skin skin = this;

				json.setSerializer(FreeTypeFontGenerator.class, new Json.ReadOnlySerializer<FreeTypeFontGenerator>() {
					@Override
					public FreeTypeFontGenerator read(Json json,
													  JsonValue jsonData, Class type) {
						String path = json.readValue("font", String.class, jsonData);
						jsonData.remove("font");

						FreeTypeFontGenerator.Hinting hinting = FreeTypeFontGenerator.Hinting.valueOf(json.readValue("hinting",
								String.class, "AutoMedium", jsonData));
						jsonData.remove("hinting");

						Texture.TextureFilter minFilter = Texture.TextureFilter.valueOf(
								json.readValue("minFilter", String.class, "Nearest", jsonData));
						jsonData.remove("minFilter");

						Texture.TextureFilter magFilter = Texture.TextureFilter.valueOf(
								json.readValue("magFilter", String.class, "Nearest", jsonData));
						jsonData.remove("magFilter");

						FreeTypeFontGenerator.FreeTypeFontParameter parameter = json.readValue(FreeTypeFontGenerator.FreeTypeFontParameter.class, jsonData);
						parameter.hinting = hinting;
						parameter.minFilter = minFilter;
						parameter.magFilter = magFilter;
						FreeTypeFontGenerator generator = new FreeTypeFontGenerator(skinFile.parent().child(path));
						BitmapFont font = generator.generateFont(parameter);
						skin.add(jsonData.name, font);
						if (parameter.incremental) {
							generator.dispose();
							return null;
						} else {
							return generator;
						}
					}
				});

				return json;
			}
		};
		fixtureGenerator = new FixtureGenerator();

		readVars();

		if(fileSave.isCorruptFile()){
			Gdx.app.log(getClass().getCanonicalName(),"File was corrupt!");
			initVars();
		}

		//Info jsonInfo = JsonConverter.toJson(info, Info.class);

		Gdx.app.log("Sim", "Round: "+info.round);

		SCREENS = new Screens(this);
		if(info.teamId!=-1) this.setScreen(null,SCREENS.MAIN_MENU,IN,0);
		else this.setScreen(null, SCREENS.TITLE_SCREEN,IN,0);
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		skin.dispose();
	}

	public void setTeam(int id){
		info.teamId = id;
		info.leagues.get(info.division).getTeam(info.teamId).chosenTeam = true;
		fileSave.saveInfo();
		SCREENS.PLAYER_SELECTION = new PlayersList(this);
	}

	public static Team getTeam(){
		if(info.teamId == -1) return new Team();
		return info.leagues.get(info.division).getTeam(info.teamId);
	}

	public void startSeason(){
		info.leagues.get(info.division).rounds = fixtureGenerator.getFixtures(info.leagues.get(info.division).getTeams(), true, info.teamId);
		info.seasonRunning = true;
		FootballSim.fileSave.saveInfo();
	}

	private void initVars(){
		info = new Info();
		info.leagues = new HashMap<>();
		info.leagues.put(1, new League().init("Premier Division"));
		info.division = 1;
		info.teams = new ArrayList<>(info.leagues.get(info.division).getTeams().values());
		Collections.sort(info.teams,League.sortTeams);

		fileSave.saveClass(info,"data");
	}

	private void readVars(){
		info = fileSave.readClass(Info.class, "data");
	}
}

