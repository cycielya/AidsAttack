package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;

import java.util.Random;
import java.math.*;
import pythagoras.f.Point;

import playn.core.Game;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.GroupLayer;
import playn.core.CanvasImage;
import playn.core.Canvas;
import playn.core.SurfaceImage;
import playn.core.Layer;
import static playn.core.PlayN.pointer;
import playn.core.Pointer;
import static playn.core.PlayN.keyboard;
import playn.core.Keyboard;
import playn.core.Key;
import playn.core.util.Callback;
import playn.core.*;

public class AidsAttack extends Game.Default {
  private static int width = 24;
  private static int height = 18;
  public static final int UPDATE_RATE = 33; // call update every 33ms (30 times per second)

  // world, worldLayer and entityLayer no longer in use by AidsAttack. See Layer class.
  GroupLayer buttonLayer; // contains buttons which do not scale with image
  public Camera camera;
  Level[] levels;
  Level currentLevel;
  private boolean gamePaused = false;

  World physicsWorld(){ return this.currentLevel.physicsWorld(); }

  public void addButton(Layer l){
    this.buttonLayer.add(l);
  }
  public void removeButton(Layer l){
    this.buttonLayer.remove(l);
  }

  Button resumeButton;
  public void pauseGame(){
    if(this.gamePaused == true){
      return;
    }
    this.gamePaused = true;
    this.resumeButton = Button.make(this, 10f, 130f, "resume");
    this.resumeButton.buttonImage.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Pointer.Event event){
        resumeGame();
      }
    });
  }
  public void resumeGame(){
    if(this.resumeButton != null){
      this.resumeButton.destroy();
      this.resumeButton = null;
    }
    this.gamePaused = false;
  }
  public boolean isGamePaused(){
    return this.gamePaused;
  }

  public AidsAttack() {
    super(UPDATE_RATE); 
  }

  public static float getCenterX(){
    return width/2f;
  }
  public static float getCenterY(){
    return height/2f;
  }

  public void populateLevelList(){
    levels = new Level[3];
    levels[0] = LevelOne.make(this);
    levels[1] = LevelTwo.make(this);
  }

  @Override
  public void init(){
    populateLevelList();
    currentLevel = levels[0];

    camera = new Camera(this);
    currentLevel.initLevel(camera);
    initKeyControls();
    // adds buttons
    initUI();
    //resumeGame();
  }
  public void initKeyControls(){
    //hook up key listener, for global scaling in-game
    keyboard().setListener(new Keyboard.Adapter() {
      @Override
      //Zoom keys: Up and Down arrows. Tried + and -, but did not work for +
      //I suspect this is because it required the shift key, but I'm not sure how to fix it.
      public void onKeyDown(Keyboard.Event event){
        if(event.key() == Key.valueOf("UP")){
          camera.zoomIn();
        }
        else if(event.key() == Key.valueOf("DOWN")){
          camera.zoomOut();
        }
        //Translation keys: a is left, s is down, w is up, d is right.
        else if(event.key() == Key.valueOf("A")){
          camera.translateRight();
        }
        else if(event.key() == Key.valueOf("S")){
          camera.translateUp();
        }
        else if(event.key() == Key.valueOf("W")){
          camera.translateDown();
        }
        else if(event.key() == Key.valueOf("D")){
          camera.translateLeft();
        }
        //for testing only.
        else if(event.key() == Key.valueOf("N")){
          successCurrentLevel();
        }
      }
      @Override
      public void onKeyUp(Keyboard.Event event){
        System.out.println("Key released!");
      }
    });
  }

  public void initUI(){
    // group layer to hold non-scaling layers
    // intended for manually-created buttons
    // each button has own layer and own pointer listener
    buttonLayer = graphics().createGroupLayer();
    buttonLayer.setDepth(4f);
    graphics().rootLayer().add(buttonLayer);

    Button zoomInButton = Button.make(this,10f,10f,"+");
    zoomInButton.buttonImage.addListener(new Pointer.Adapter() {
          @Override
          public void onPointerStart(Pointer.Event event) {
            camera.zoomingIn = true;
            camera.zoomingOut = false;
          }
          @Override
          public void onPointerDrag(Pointer.Event event){
            camera.zoomingIn = true;
            camera.zoomingOut = false;
          }
          @Override
          public void onPointerEnd(Pointer.Event event){
            camera.zoomingIn = false;
          }
    });
    Button zoomOutButton = Button.make(this,10f,40f,"-");
    zoomOutButton.buttonImage.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Pointer.Event event) {
        camera.zoomingOut = true;
        camera.zoomingIn = false;
      }
      @Override
      public void onPointerDrag(Pointer.Event event){
        camera.zoomingOut = true;
        camera.zoomingIn = false;
      }
      @Override
      public void onPointerEnd(Pointer.Event event){
        camera.zoomingOut = false;
      }
    });
    Button resetButton = Button.make(this,10f,70f,"reset");
    resetButton.buttonImage.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Pointer.Event event){
        currentLevel.endLevel();
        graphics().rootLayer().destroyAll();
        currentLevel = levels[0];
        currentLevel.initLevel(camera);
        camera.reset();
        initKeyControls();
        initUI();
      }
    });
    Button pauseButton = Button.make(this, 10f, 100f, "pause");
    pauseButton.buttonImage.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Pointer.Event event){
        System.out.println("Pausing game!");
        pauseGame();
      }
    });
    resumeButton = null;

    resumeGame();
  }

  boolean gameOver = false;
  // TODO: call this when Virus has 6 hits on it.
  public void gameOver(){
    // create surface layer with 'game over'
    CanvasImage image = graphics().createImage(200,200);
    Canvas canvas = image.canvas();
    canvas.setFillColor(0xff050505);
    canvas.drawText("Game Over!",100,100);
    ImageLayer gameOverLayer = graphics().createImageLayer(image);
    gameOverLayer.setDepth(6);
    graphics().rootLayer().add(gameOverLayer);
    // pointer listener should be null so mouse clicks don't continue to move virus.
    pointer().setListener(null);
    keyboard().setListener(null);
    gameOver = true;
    currentLevel.gameOver = true;
  }

  // method to transition from LevelOne to LevelTwo.
  // May abstract further, to a success() method in LevelOne that does most of the cleanup.
  public void successLevelOne(){
    pointer().setListener(null);
    keyboard().setListener(null);
    currentLevel.success = true;
    System.out.println("Current level is: "+currentLevel);
    currentLevel.endLevel();
    graphics().rootLayer().destroyAll();
    //levels[1] = LevelTwo.make(this);
    currentLevel = levels[1];
    System.out.println("Current level is: "+currentLevel);
    currentLevel.initLevel(camera);
    camera.reset();
    // adds the buttons back in
    initUI();
  }
  public void successCurrentLevel(){
    currentLevel.endLevel();
    currentLevel.successLevel();
    int i;
    for(i=0; i<levels.length && currentLevel != levels[i]; i++){}
    if(i >= levels.length-1){
    }
    else{
      currentLevel = levels[i+1];
    }
    currentLevel.initLevel(camera);
    camera.reset();
    initUI();
  }

  //Vec2 virusScreenTarget = new Vec2();
  //boolean attractingVirus = false;
  float minLength = 1f;
  float forceScale = 10f;

  int time = 0;
  public int time(){ return this.time; }
  Random gravity = new Random(54321);

  //float zoomStep = 0.1f;

  @Override
  public void update(int delta) {
    time += delta;
    time = time < 0 ? 0 : time;
    if(!isGamePaused()){
      camera.update();
      currentLevel.update(delta, time);
    }
    // world is no longer updated in AidsAttack
    //world.step(0.033f, 10, 10);
  }

  @Override
  public void paint(float alpha) {
    // the background automatically paints itself, so no need to do anything here!
    if(!isGamePaused()){
      currentLevel.paint(alpha);
    }
  }
}
