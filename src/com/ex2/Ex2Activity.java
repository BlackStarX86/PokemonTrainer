package com.ex2;
///navigate a pokemontrainer through a map.Smooth battle mechanics not completed just yet
import java.util.ArrayList;

import com.e3roid.E3Activity;
import com.e3roid.E3Engine;
import com.e3roid.E3Scene;
import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.controls.DigitalController;
import com.e3roid.drawable.controls.StickController;
import com.e3roid.drawable.modifier.AlphaModifier;
import com.e3roid.drawable.modifier.SpanModifier;
import com.e3roid.drawable.sprite.AnimatedSprite;
import com.e3roid.drawable.sprite.TextSprite;
import com.e3roid.drawable.texture.AssetTexture;
import com.e3roid.drawable.texture.Texture;
import com.e3roid.drawable.texture.TiledTexture;
import com.e3roid.drawable.tmx.TMXException;
import com.e3roid.drawable.tmx.TMXLayer;
import com.e3roid.drawable.tmx.TMXTiledMap;
import com.e3roid.drawable.tmx.TMXTiledMapLoader;
import com.e3roid.event.ControllerEventListener;

import com.e3roid.util.Debug;

import android.app.Activity;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;

public class Ex2Activity extends E3Activity implements ControllerEventListener {

	private final static int WIDTH  = 480;
	private final static int HEIGHT = 320;
	
	private TiledTexture texture;
	private AnimatedSprite sprite;
	private AssetTexture controlBaseTexture;
	private AssetTexture controlKnobTexture;
	private DigitalController controller; 
	
	private ArrayList<AnimatedSprite.Frame> downFrames  = new ArrayList<AnimatedSprite.Frame>();
	private ArrayList<AnimatedSprite.Frame> upFrames    = new ArrayList<AnimatedSprite.Frame>();
	private ArrayList<AnimatedSprite.Frame> leftFrames  = new ArrayList<AnimatedSprite.Frame>();
	private ArrayList<AnimatedSprite.Frame> rightFrames = new ArrayList<AnimatedSprite.Frame>();
	
	private TMXTiledMap map;
	private ArrayList<TMXLayer> mapLayers;
	private TMXLayer collisionLayer;

	@Override
	public E3Engine onLoadEngine() {
		E3Engine engine = new E3Engine(this, WIDTH, HEIGHT);
		engine.requestFullScreen();
		engine.requestLandscape();
		return engine;
	}

	@Override
	public E3Scene onLoadScene() {
		final E3Scene scene = new E3Scene();
		
		// add sprite
		int centerX = (getWidth()  - texture.getTileWidth())  / 2;
		int centerY = (getHeight() - texture.getTileHeight()) / 2;
		
		sprite = new AnimatedSprite(texture, centerX, centerY) {
			@Override
			public Rect getCollisionRect() {
				// king's collision rectangle is just around his body.
				Rect rect = this.getRect();
				rect.left   = rect.left   + this.getWidth() / 3;
				rect.right  = rect.right  - this.getWidth() / 3;
				rect.top    = rect.top    + this.getHeight() / 3;
				rect.bottom = rect.bottom - this.getHeight() / 3;
				return rect;
			}
		};
		sprite.animate(500, downFrames);
		
		// add digital controller
		controller = new DigitalController(
				controlBaseTexture, controlKnobTexture,
				0, getHeight() - controlBaseTexture.getHeight(), scene, this);
		controller.setAlpha(0.7f);
		controller.setUpdateInterval(10);
		scene.addEventListener(controller);
		
		// add loading text with blink modifier
		final TextSprite loadingText = new TextSprite("Loading map...", 24, this);
		loadingText.move((getWidth()  - loadingText.getWidth())  / 2,
						 (getHeight() - loadingText.getHeight()) / 2);
		loadingText.addModifier(new SpanModifier(500L, new AlphaModifier(0, 0, 1)));
		scene.getTopLayer().add(loadingText);
		
		// limit refresh rate while loading for saving cpu power
		engine.setRefreshMode(E3Engine.REFRESH_LIMITED);
		engine.setPreferredFPS(10);
		
		// loading maps could take some time so work in background.
		new AsyncTask<Void,Integer,TMXTiledMap>() {
			@Override
			protected TMXTiledMap doInBackground(Void... params) {
				// get the map from TMX map file.
				try {
					TMXTiledMapLoader mapLoader = new TMXTiledMapLoader();
					TMXTiledMap map = mapLoader.loadFromAsset("desert.tmx", Ex2Activity.this);
					return map;
				} catch (TMXException e) {
					Debug.e(e.getMessage());
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(TMXTiledMap tmxTiledMap) {
			    map = tmxTiledMap;
				if (tmxTiledMap != null && (mapLayers = map.getLayers()) != null) {
					for (TMXLayer layer : mapLayers) {
						// Determine scene size of the layer.
						// This enables layer to skip drawing the tile which is out of the screen.
						layer.setSceneSize(getWidth(), getHeight());
						
						// if ground scrolls, child sprite follows it.
						if ("Ground".equals(layer.getName())) {
							layer.addChild(sprite);
						}
						
						// "Collision" layer is not to be added to the scene
						// because this layer is just defining collision of the tiles.
						if ("Collision".equals(layer.getName())) {
							collisionLayer = layer;
							continue;
						}
						
						scene.getTopLayer().remove(loadingText);
						
						// Add sprite to the scene.
						scene.getTopLayer().add(layer);
						scene.getTopLayer().add(sprite);
						scene.addHUD(controller);
						
						// restore refresh rate to default
						engine.setRefreshMode(E3Engine.REFRESH_DEFAULT);
					}
				} else {
					loadingText.setText("Failed to load!");
					loadingText.setAlpha(1);
					loadingText.clearModifier();
				}
			}
		}.execute();
		
		return scene;
	}

	@Override
	public void onLoadResources() {
		// 31x49 pixel sprite with 1px border and (0,0) tile.
		texture = new TiledTexture("pokemontrainer2.png", 40, 40, 0, 0, 2, 3, this);
		
		// Initialize animation frames from tile.
		downFrames = new ArrayList<AnimatedSprite.Frame>();
		//downFrames.add(new AnimatedSprite.Frame(0, 0));
		downFrames.add(new AnimatedSprite.Frame(2, 1));
		downFrames.add(new AnimatedSprite.Frame(2, 2));
		downFrames.add(new AnimatedSprite.Frame(2, 3));
		
		leftFrames = new ArrayList<AnimatedSprite.Frame>();
		leftFrames.add(new AnimatedSprite.Frame(0, 1));
		leftFrames.add(new AnimatedSprite.Frame(0, 2));
		leftFrames.add(new AnimatedSprite.Frame(0, 3));
		//leftFrames.add(new AnimatedSprite.Frame(3, 1));

		rightFrames = new ArrayList<AnimatedSprite.Frame>();
		rightFrames.add(new AnimatedSprite.Frame(1, 0));
		rightFrames.add(new AnimatedSprite.Frame(1, 1));
		rightFrames.add(new AnimatedSprite.Frame(1, 2));
		//rightFrames.add(new AnimatedSprite.Frame(3, 2));

		upFrames = new ArrayList<AnimatedSprite.Frame>();
		upFrames.add(new AnimatedSprite.Frame(0, 0));
		upFrames.add(new AnimatedSprite.Frame(2, 0));
		upFrames.add(new AnimatedSprite.Frame(1, 3));
		//upFrames.add(new AnimatedSprite.Frame(3, 3));
		
		controlBaseTexture = new AssetTexture("controller_base.png", this);
		controlKnobTexture = new AssetTexture("controller_knob.png", this);
	}
	
	@Override
	public void onControlUpdate(StickController controller,
			int relativeX, int relativeY, boolean hasChanged) {
		int dir = controller.getDirection();
		if (hasChanged) {
			if (dir == StickController.LEFT) {
				sprite.animate(500, leftFrames);
			} else if (dir == StickController.RIGHT) {
				sprite.animate(500, rightFrames);
			} else if (dir == StickController.UP) {
				sprite.animate(500, upFrames);
			} else if (dir == StickController.DOWN) {
				sprite.animate(500, downFrames);
			}
		}
		
		int xstep = (relativeX / 40);
		int ystep = (relativeY / 40);
		
		// move the ground
		if (!isInTheScene(sprite, xstep, ystep)) {
			for (TMXLayer layer : mapLayers) {
				layer.scroll(layer.getX() + xstep, layer.getY() + ystep);
			}
		}
		
		// move the sprite
		if (!collidesWithTile(sprite, xstep, ystep) && isInTheScene(sprite, xstep, ystep)) {
			sprite.moveRelative(xstep, ystep);
		}
	}
	
	private boolean isInTheScene(Sprite sprite, int xstep, int ystep) {
		int x = sprite.getRealX() + xstep;
		int y = sprite.getRealY() + ystep;
		return x > 0 && y > 0 && x < getWidth()  - sprite.getWidth() &&
				y < getHeight() - sprite.getHeight();
	}
	
	private boolean collidesWithTile(AnimatedSprite sprite, int xstep, int ystep) {
		if (collisionLayer == null) return false;
		return collisionLayer.getTileFromRect(sprite.getCollisionRect(), xstep, ystep).size() != 0;
	}
}
