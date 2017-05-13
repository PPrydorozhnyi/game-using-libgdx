package com.drakus.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class RainBucket extends ApplicationAdapter {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture dropImage;
	private Texture bucketImage;
	private Texture gameOverImage;
	private BitmapFont font, pausedFont;
	//private Texture gameWonImage;
	private Sound dropSound;
	private Music rainMusic;
	private Rectangle bucket;
	private Vector3 touchPos;
	private Array<Rectangle> rainDrops;
	private long lastDropTime;

	//private boolean isPaused;
	private boolean isResumed;
	private boolean isOver;
	private boolean isPaused;
	private int bottom;
	private int m_height;
	private int m_width;
	private int score;
	private float deltaScore;
	private String str;
	private float layoutWidth, layoutHeight;
	private FreeTypeFontGenerator generator;
	private FreeTypeFontGenerator.FreeTypeFontParameter parameter;
	private GlyphLayout pauseLayout;

	@Override
	public void create () {

		m_width = 800;
		m_height = 480;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, m_width, m_height);

		batch = new SpriteBatch();
		dropImage = new Texture("droplet.png");
		bucketImage = new Texture("bucket.png");
		gameOverImage = new Texture("game-over2.jpg");

		generator = new FreeTypeFontGenerator(Gdx.files.internal("courbd.ttf"));
		parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 23;
		//parameter.borderWidth = 2.0f;
		font = generator.generateFont(parameter);
		parameter.size = 30;

		pausedFont = generator.generateFont(parameter);
		pauseLayout = new GlyphLayout(pausedFont, "Game paused");
		layoutWidth = pauseLayout.width;
		layoutHeight = pauseLayout.height;
		//font.setColor(Color.WHITE);
		//font.getData().setScale(1.4f, 1.4f);



		dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));

		rainMusic.setLooping(true);
		rainMusic.play();

		bucket = new Rectangle();

		bucket.x = m_width / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		bottom = 15;
		isOver = false;
		isResumed = true;
		isPaused = false;
		score = 0;
		deltaScore = 1;
		str = "Score: " + score;
		//str = "Score: ";

		touchPos = new Vector3();

		rainDrops = new Array<Rectangle>();
		spawnDrops();

		/*Gdx.input.setInputProcessor(new InputAdapter () {
			@Override
			public boolean touchDragged (int x, int y, int pointer) {
				isResumed = false;
				isPaused = true;
				return false;
			}
		});*/
	}

	private void spawnDrops () {

		Rectangle rainDrop = new Rectangle();

		rainDrop.x = MathUtils.random(0, m_width - bucket.width);
		rainDrop.y = m_height;

		rainDrop.width = bucket.width;
		rainDrop.height = bucket.height;

		rainDrops.add(rainDrop);

		lastDropTime = TimeUtils.nanoTime();

	}


	@Override
	public void render () {

		if (isOver) {

			onFinish();
			drawScore(m_width/ 2 - layoutWidth / 2, m_height/ 2 - layoutHeight / 2, pausedFont);
			if (Gdx.input.isTouched()) {
				isResumed = true;
				//isPaused = false;
				isOver = false;
				onStart();
			}

		} else {

			Gdx.gl.glClearColor(0, 0, 0.2f, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			camera.update();

			batch.setProjectionMatrix(camera.combined);
			drawObjects();

			if (isPaused) {
				onPause();
				if (Gdx.input.isTouched()) {
					isResumed = true;
					isPaused = false;
				}
			}

			if (isResumed) {
				if (Gdx.input.isTouched()) {

					touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);

					camera.unproject(touchPos);

					bucket.x = touchPos.x - (bucket.width / 2);

				}

				if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
				bucket.x -= 600 * Gdx.graphics.getDeltaTime();
				if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
				bucket.x += 600 * Gdx.graphics.getDeltaTime();

				if (bucket.x < 0)
					bucket.x = 0;
				if (bucket.x > m_width - bucket.width)
					bucket.x = m_width - bucket.width;

				if (TimeUtils.nanoTime() - lastDropTime > 600000000)
					spawnDrops();
				if (rainDrops.size < 3 && score != 0)
					spawnDrops();

				Iterator<Rectangle> iter = rainDrops.iterator();

				while (iter.hasNext()) {

					Rectangle raindrop = iter.next();
					raindrop.y -= deltaScore * 200 * Gdx.graphics.getDeltaTime();

					if (raindrop.y + 64 < bottom) {

						gameOver();
						//iter.remove();

					}

					//if (raindrop.y + 64 < 0)
					//iter.remove();

					if (raindrop.overlaps(bucket)) {
						dropSound.play();
						iter.remove();
						score += deltaScore;
						str = "Score: " + score;
						//int am = rainDrops.size;
						//System.out.println(am);
					}
				}
			}
			drawScore(630, 460, font);
		}


		if (score <= 25)

			deltaScore = 1f;

		else if (score <= 500)

			deltaScore = 1.0f + 0.085f * (score / 25);
			//if (score > 25 && score <= 50)

			/*deltaScore = 1.1f;

		else if (score > 50 && score <= 100)

			deltaScore = 1.2f;

		else if (score > 100 && score <= 200)

			deltaScore = 1.4f;

		else

			deltaScore = 1.8f;*/


	}

	private void onStart() {

		Iterator<Rectangle> iter = rainDrops.iterator();

		while (iter.hasNext()) {

			iter.next();
			iter.remove();

		}

		bucket.x = m_width / 2 - 64 / 2;
		score = 0;
		str = "Score: " + score;
	}

	private void onPause() {

		batch.begin();
		pausedFont.draw(batch, "Game paused", m_width/ 2 - layoutWidth / 2, m_height/ 2 - layoutHeight / 2);
		batch.end();

	}


	private void onFinish() {


		//camera.update();

		//batch.setProjectionMatrix(camera.combined);

		//Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		batch.draw(gameOverImage, 0, 0);
		batch.end();

		//camera.update();
	}

	private void drawScore(float width, float height, BitmapFont currentFont) {

		batch.begin();
		//font.draw(batch, "Hello World", 500, 20);

		currentFont.draw(batch, str, width, height);
		batch.end();

	}

	@Override
	public void dispose () {
		super.dispose();
		batch.dispose();
		dropSound.dispose();
		dropImage.dispose();
		bucketImage.dispose();
		rainMusic.dispose();
		font.dispose();
		generator.dispose();
	}

	private void gameOver() {
		isOver = true;
		isResumed = false;
	}

	private  void drawObjects() {

		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for (Rectangle raindrop: rainDrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();

	}


	@Override
	public void pause() {

		rainMusic.pause();
		isPaused = true;
		isResumed = false;
	}

	@Override
	public void resume() {

		rainMusic.play();

	}


}