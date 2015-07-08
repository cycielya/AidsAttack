package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.DistanceJoint;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import playn.core.ImageLayer;
import playn.core.Image;
import playn.core.CanvasImage;
import playn.core.Canvas;
import static playn.core.PlayN.pointer;
import playn.core.Pointer;


public class Nucleotide{
  AidsAttack game;
  private float width = 1f;
  private float height = 1f;
  private Nucleobase nBase;
  private Body body;
  private Fixture myBodyFixture;
  private ImageLayer myLayer;
  private float prevX, prevY, prevA;
  LevelTwo level;
  private Nucleotide pair;
  private boolean movable;
  private Body groundBody;
  private MouseJoint mouseJoint;



  private Nucleotide(){}
  public static Nucleotide make(AidsAttack game, Level level, Nucleobase nBase, float x, float y, float ang){
    Nucleotide n = new Nucleotide();
    n.nBase = nBase;
    n.pair = null;
    n.game = game;
    n.level = (LevelTwo) level;
    n.initPhysicsBody(n.level.physicsWorld(), x, y, ang);
    n.drawNucleotideImage();
    n.level.addLayer(n.myLayer);
    BodyDef groundBodyDef = new BodyDef();
    n.groundBody = level.physicsWorld().createBody(groundBodyDef);
    return n;
  }
  private void initPhysicsBody(World world, float x, float y, float ang){
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position = new Vec2(x, y);
    bodyDef.angle = ang;
    Body body = world.createBody(bodyDef);
    //body.setSleepingAllowed(false);
    // NOTE: true sets body to asleep, false sets body to awake
    // Not very intuitive, so be aware that it's backwards!
    body.setAwake(true);
    movable = false;

    PolygonShape polygonShape = new PolygonShape();
    polygonShape.setAsBox(width/2f, height/2f);

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = polygonShape;
    fixtureDef.friction = 0.1f;
    fixtureDef.restitution = 0.4f;
    fixtureDef.density = 1.0f;

    this.myBodyFixture = body.createFixture(fixtureDef);
    this.myBodyFixture.m_userData = this;
    this.body = body;
    this.body.m_userData = this;
  }

  private void drawNucleotideImage(){
    float imageSize = 100;
    CanvasImage image = graphics().createImage(imageSize, imageSize);
    Canvas canvas = image.canvas();
    canvas.setFillColor(0xfffac22a);
    //coordinates are for upper-left corner placement
    canvas.fillRect(0f, 0f, imageSize, imageSize);
    myLayer = graphics().createImageLayer(image);
    myLayer.setOrigin(image.width() / 2f, image.height() / 2f);
    myLayer.setScale(getWidth()/imageSize,getHeight()/imageSize);
    myLayer.setTranslation(x(), y());
    myLayer.setRotation(ang());
    this.myLayer.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Pointer.Event event){
        System.out.println("Pointer hit me! Waah!");
        Vec2 pointerLocation = new Vec2(event.x(), event.y());
        MouseJointDef def = new MouseJointDef();
        def.bodyA = groundBody;
        def.bodyB = body;
        def.target.set(pointerLocation);
        mouseJoint = (MouseJoint) level.physicsWorld().createJoint(def);
        body.setAwake(true);
      }
      @Override
      public void onPointerDrag(Pointer.Event event){
        System.out.println("Mo-om! Pointer's being mean!");
        if(mouseJoint == null){
          return;
        }
        Vec2 pointerLocation = new Vec2(event.x(), event.y());
        mouseJoint.setTarget(pointerLocation);
      }
      @Override
      public void onPointerEnd(Pointer.Event event){
        System.out.println("Pointer, go to your room!");
        if(mouseJoint == null){
          return;
        }
        level.physicsWorld().destroyJoint(mouseJoint);
        mouseJoint = null;
      }
    });
  }

  public void strandLink(Nucleotide other){
    DistanceJointDef def = new DistanceJointDef();
    def.initialize(this.body, other.body, new Vec2(0f,0f), new Vec2(0f, 0f));
    DistanceJoint dj = (DistanceJoint) Joint.create(this.level.physicsWorld(), def);
  }

  public boolean pairsWith(Nucleotide other){
    return this.nBase.pairsWith(other.nBase);
  }
  public boolean basePair(Nucleotide other){
    if(this.pairsWith(other)){
      this.pair = other;
      other.pair = this;
      return true;
    }
    return false;
  }
  public boolean isBasePaired(){
    return (this.pair == null);
  }
  public float getWidth(){
    return this.width;
  }
  public float getHeight(){
    return this.height;
  }
  public float x(){
    return body.getPosition().x;
  }
  public float y(){
    return body.getPosition().y;
  }
  public float ang(){
    return body.getAngle();
  }
  public Vec2 position(){
    return body.getPosition();
  }
  public Body body(){
    return this.body;
  }

  public void update(int delta){
    prevX = x();
    prevY = y();
    prevA = ang();
  }
  public void paint(float alpha) {
    // interpolate based on previous state
    float x = (x() * alpha) + (prevX * (1f - alpha));
    float y = (y() * alpha) + (prevY * (1f - alpha));
    float a = (ang() * alpha) + (prevA * (1f - alpha));
    myLayer.setTranslation(x, y);
    myLayer.setRotation(a);
  }
}

/* Each nucleotide contains one nucleobase, which determines what other
 * nucleotides it can pair with when forming a double helix. 
 * Will be important during base-pairing puzzle portion of game.
 * NOTE: C,G,A and T occur in DNA, whereas C,G,A and U occur in RNA.
*/
enum Nucleobase{
  C ("cytosine"),
  G ("guanine"),
  A ("adenine"),
  T ("thymine"),
  U ("uracil");
  private final String name;
  private static final Nucleobase[][] pairsWith = {
    {C, G},
    {A, T},
    {A, U}
  };
  Nucleobase(String name){
    this.name = name;
  }
  public String nameOf(Nucleobase n){
    return n.name;
  }
  public boolean pairsWith(Nucleobase other){
    int i;
    for(i=0; i<pairsWith.length; i++){
      if( (this == pairsWith[i][0] && other == pairsWith[i][1]) ||
          (other == pairsWith[i][0] && this == pairsWith[i][1]) ){
        return true;
      }
    }
    return false;
  }
}
