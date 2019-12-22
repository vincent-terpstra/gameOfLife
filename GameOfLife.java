package com.vdt.poolgame.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GameOfLife extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	
	Texture		texture;
	
	ShaderProgram shaderProgram, ignoreShader, colorShader;
	
	FrameBuffer main_buffer,
				second_buffer;
	
	
	static int BUFFER_WIDTH = 128;
	static int MULTI = 512 / BUFFER_WIDTH;
	
	int loadTexture = 0;
	
	public void loadTexture(int delta) {
		final int max = 11;
		loadTexture = (loadTexture + delta + max) % max;
		if(texture != null)
			texture.dispose();
		texture = new Texture("repeat" + loadTexture + ".png");
	}
	private boolean reset = true;
	@Override 
	public void create() {
		
		batch = new SpriteBatch();
		
		
		shaderProgram = new ShaderProgram(
			Gdx.files.internal("shader.vert").readString(),
			Gdx.files.internal("shader.frag").readString()
		);
		System.out.print(shaderProgram.getLog());
		
		ignoreShader = new ShaderProgram(
			Gdx.files.internal("shader.vert").readString(),
			Gdx.files.internal("ignore.frag").readString()
		);
		System.out.print(ignoreShader.getLog());
		
		colorShader = new ShaderProgram(
			Gdx.files.internal("shader.vert").readString(),
			Gdx.files.internal("color.frag").readString()
		);
		System.out.print(colorShader.getLog());
		
		main_buffer = new FrameBuffer(Pixmap.Format.RGB888, BUFFER_WIDTH, BUFFER_WIDTH, false);
		second_buffer = new FrameBuffer(Pixmap.Format.RGB888, BUFFER_WIDTH, BUFFER_WIDTH, false);
		Texture texture = main_buffer.getColorBufferTexture();
		texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		texture = second_buffer.getColorBufferTexture();
		texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		loadTexture(0);
		Gdx.input.setInputProcessor(this);
		
		
	}
	private void update() {
		batch.setShader(shaderProgram);
		
		second_buffer.begin();
		batch.begin();
		clear(0,0,0);
		batch.draw(main_buffer.getColorBufferTexture(), 0, 512, 512, -512);
		
		batch.end();
		second_buffer.end();
		
		FrameBuffer tmp = main_buffer;
		main_buffer = second_buffer;
		second_buffer = tmp;
	}
	
	
	@Override
	public void dispose() {
		texture.dispose();
		
		batch.dispose();
		
		shaderProgram.dispose();
		ignoreShader.dispose();
		colorShader.dispose();
		
		main_buffer.dispose();
		second_buffer.dispose();
	}
	
	
	private void clear(float red, float green, float blue) {
		Gdx.gl.glClearColor(red, green, blue, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	float deltaTime = 0;
	float step = 1/4f;
	@Override
	public void render() {
		if(reset) {
			reset = false;
			main_buffer.begin();
			//batch.begin();
			clear(0, 0, 0 );
			//batch.draw(texture, 0, 0);
			//batch.end();
			main_buffer.end();
		}
		if(draw) {
			draw = false;
			//draw onto the framebuffer
			batch.setShader(ignoreShader);
			main_buffer.begin();
			batch.begin();
			batch.draw(texture, screenX, screenY,
					texture.getWidth() * MULTI,  texture.getHeight() * MULTI);
			
			batch.end();
			main_buffer.end();
			
		}
		deltaTime += Gdx.graphics.getDeltaTime();
		if(deltaTime > step) {
			update();
			deltaTime = 0;
		}
		batch.begin();
		batch.setShader(colorShader);
		clear(0.05f, 0.05f, 0.1f);
		Texture tmp = main_buffer.getColorBufferTexture();
		tmp.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		batch.draw(tmp, 0, 0, 512, 512);
		
		batch.setShader(ignoreShader);
		batch.draw(texture, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), 
				texture.getWidth() * MULTI, - texture.getHeight() * MULTI);
		batch.end();
	}
	
	private int screenX, screenY;
	private boolean draw = false;
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		draw = true;
		this.screenX = screenX;
		this.screenY = screenY;
		return true;
	}
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		draw = true;
		this.screenX = screenX;
		this.screenY = screenY;
		return true;
	}
	
	@Override
	public boolean keyDown(int keycode) { 
		if(keycode == Input.Keys.PLUS) {
			step = Math.max(step / 2, 1/64f);
		} else if( keycode == Input.Keys.MINUS) {
			step = Math.min(step * 2, 1);
		} else if( keycode == Input.Keys.R && 
				Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || 
				Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
			reset = true;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		loadTexture(amount);
		return true;
	}
}
