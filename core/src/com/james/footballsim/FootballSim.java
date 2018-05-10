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
import com.ixeption.libgdx.transitions.FadingGame;
import com.ixeption.libgdx.transitions.ScreenTransition;
import com.ixeption.libgdx.transitions.impl.SlidingTransition;
import com.james.footballsim.Screens.PlayersList;
import com.james.footballsim.Screens.Screens;
import com.james.footballsim.Simulator.League;
import com.james.footballsim.Simulator.Team;
import uk.co.codeecho.fixture.generator.Fixture;
import uk.co.codeecho.fixture.generator.FixtureGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FootballSim extends FadingGame {
	public static Skin skin;

	static FixtureGenerator fixtureGenerator;
	public static League league;
	public static List<List<Fixture<Integer>>> rounds;

	public static List<Team> teams;

	public static int teamId = -1;
	public static int round;

	public static boolean seasonRunning = false;

	public static Screens SCREENS;

	public static FileSave fileSave;

	public static ScreenTransition IN =  new SlidingTransition(SlidingTransition.Direction.LEFT,Interpolation.exp10Out,false);
	public static ScreenTransition OUT =  new SlidingTransition(SlidingTransition.Direction.RIGHT,Interpolation.exp10Out,true);

	@Override
	public void create () {
		super.create();

		fileSave = new FileSave();

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

		Gdx.app.log(getClass().getCanonicalName(),"File was read!");
		readVars();

		if(fileSave.isCorruptFile()){
			Gdx.app.log(getClass().getCanonicalName(),"File was corrupt!");
			initVars();
		}

//		league = new League().init();
//
//		try {
//			FileOutputStream fileOut =
//					new FileOutputStream(Gdx.files.getLocalStoragePath()+"/saves/tests.txt");
//			ObjectOutputStream out = new ObjectOutputStream(fileOut);
//			out.writeObject(league);
//			out.close();
//			fileOut.close();
//			System.out.printf("Serialized data is saved");
//		} catch (IOException i) {
//			i.printStackTrace();
//		}


		teams = new ArrayList<>(league.getTeams().values());
		Collections.sort(teams,League.sortTeams);

		SCREENS = new Screens(this);

		this.setScreen(null, SCREENS.TITLE_SCREEN,IN,0);
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
		teamId = fileSave.saveClass(id,"teamId");
		league.getTeam(teamId).chosenTeam = true;
		SCREENS.PLAYER_SELECTION = new PlayersList(this);
	}

	public static Team getTeam(){
		if(teamId == -1) return new Team();
		return league.getTeam(teamId);
	}

	public void startSeason(){
		rounds = fixtureGenerator.getFixtures(league.getTeams(), true, teamId);
		seasonRunning = true;
	}

	public void initVars(){
		league = fileSave.saveClass(new League().init(),"League");
		teamId = fileSave.saveClass(-1,"teamId");
		//teams = fileSave.saveClass(new ArrayList<>(league.getTeams().values()));
	}

	public void readVars(){
		//teams = fileSave.readClass(ArrayList.class);
		league = fileSave.readClass(League.class, "League");
		teamId = fileSave.readClass(Integer.class,"teamId");
	}
}

