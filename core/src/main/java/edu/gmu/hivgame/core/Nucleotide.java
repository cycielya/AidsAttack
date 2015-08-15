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
import org.jbox2d.dynamics.joints.RopeJoint;
import org.jbox2d.dynamics.joints.RopeJointDef;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import playn.core.ImageLayer;
import playn.core.Image;
import playn.core.CanvasImage;
import playn.core.Canvas;
import static playn.core.PlayN.pointer;
import playn.core.Pointer;


// The base unit of DNA strands. Each nucleotide contains one Nucleobase, and can
// pair with one other nucleotide, depending on the type of base each one has.
// It can also strand link, which is to say, become associated with up to two
// other nucleotides that would be on either side of it in a single-strand DNA or RNA.
public class Nucleotide implements CollisionHandler{
  AidsAttack game;
  private float width = 1f;
  private float height = 1f;
  private Nucleobase nBase; // see enum Nucleobase definition below
  private Body body;
  private Fixture myBodyFixture;
  private ImageLayer myLayer;
  private float prevX, prevY, prevA;
  LevelTwo level;
  private Nucleotide pair; // the Nucleotide it is base-paired with
  //private boolean movable;
  private Body groundBody; // used for MouseJoint, does not have relevance to anything else
  private MouseJoint mouseJoint; // to make a nucleotide player-controllable

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
    n.groundBody = level.physicsWorld().createBody(groundBodyDef); // groundBody only relevant for MouseJoint
    return n;
  }
  private void initPhysicsBody(World world, float x, float y, float ang){
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position = new Vec2(x, y);
    bodyDef.angle = ang;
    bodyDef.angularDamping = 1.0f;
    bodyDef.linearDamping = 1.0f;
    Body body = world.createBody(bodyDef);

    PolygonShape polygonShape = new PolygonShape();
    polygonShape.setAsBox(width/2f, height/2f);

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = polygonShape;
    fixtureDef.friction = 0.2f;
    //fixtureDef.restitution = 0.4f;
    fixtureDef.density = 10.0f;

    this.myBodyFixture = body.createFixture(fixtureDef);
    this.myBodyFixture.m_userData = this;
    this.body = body;
    this.body.m_userData = this;
  }

  private void drawNucleotideImage(){
    float imageSize = 100;
    CanvasImage image = graphics().createImage(imageSize, imageSize);
    Canvas canvas = image.canvas();
    //should be a darkish yellow, or gold color
    canvas.setFillColor(0xfffac22a);
    //coordinates are for upper-left corner placement
    canvas.fillRect(0f, 0f, imageSize, imageSize);
    myLayer = graphics().createImageLayer(image);
    myLayer.setOrigin(image.width() / 2f, image.height() / 2f);
    myLayer.setScale(getWidth()/imageSize,getHeight()/imageSize);
    myLayer.setTranslation(x(), y());
    myLayer.setRotation(ang());

    // intended for allowing click+drag control of DNA strands and
    // individual nucleotides
    this.myLayer.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Pointer.Event event){
        System.out.println("My Nucleobase is: ");
        System.out.println(nBase);
        Vec2 pointerLocation = new Vec2(event.x(), event.y());
        Vec2 physLocation = new Vec2(level.camera.screenXToPhysX(pointerLocation.x),
                                     level.camera.screenYToPhysY(pointerLocation.y));
        MouseJointDef def = new MouseJointDef();
        def.bodyA = level.physicsWorld().createBody(new BodyDef());
        def.bodyB = body;
        def.target.set(physLocation);
        def.maxForce = 1000f * body.getMass();
        mouseJoint = (MouseJoint) level.physicsWorld().createJoint(def);
        body.setAwake(true);
      }
      @Override
      public void onPointerDrag(Pointer.Event event){
        if(mouseJoint == null){
          return;
        }
        Vec2 pointerLocation = new Vec2(event.x(), event.y());
        Vec2 physLocation = new Vec2(level.camera.screenXToPhysX(pointerLocation.x),
                                     level.camera.screenYToPhysY(pointerLocation.y));
        mouseJoint.setTarget(physLocation);
      }
      @Override
      public void onPointerEnd(Pointer.Event event){
        if(mouseJoint == null){
          return;
        }
        level.physicsWorld().destroyJoint(mouseJoint);
        mouseJoint = null;
      }
    });
  }

  // other is the new Nucleotide being added to the strand.
  // visualize as this being last one in a DNA strand, and other being added to the end.
  public void strandLink(Nucleotide other){
    RopeJointDef def = new RopeJointDef();
    def.bodyA = this.body;
    def.bodyB = other.body;
    def.localAnchorA.set(0f+this.getWidth()/2f,0f-this.getHeight()/2f); //position at upper right corner
    def.localAnchorB.set(0f-other.getWidth()/2f,0f-other.getHeight()/2f); //position at upper left corner
    def.maxLength = .75f;
    def.collideConnected = true;
    RopeJoint rj = (RopeJoint) this.level.physicsWorld().createJoint(def);
    def.localAnchorA.set(0f+this.getWidth()/2f, 0f+this.getHeight()/2f); //position at lower right corner
    def.localAnchorB.set(0f-other.getWidth()/2f, 0f+other.getHeight()/2f); //position at lower left corner
    RopeJoint rj2 = (RopeJoint) this.level.physicsWorld().createJoint(def);
  }

  public boolean pairsWith(Nucleotide other){
    return this.nBase.pairsWith(other.nBase);
  }
  //attempts to pair two Nucleotides. Returns true on successful pairing, false on wrong pair.
  public boolean basePair(Nucleotide other){
    this.pair = other;
    other.pair = this;
    if(this.pairsWith(other)){
      return true;
    }
    else{
      return false;
    }
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
  public void handleCollision(Fixture me, Fixture other){
    if(me == this.myBodyFixture && other.m_userData instanceof ReverseTranscriptase){
      System.out.println("Hit the RT!");
    }
  }
}

/* Each nucleotide contains one nucleobase, which determines what other
 * nucleotides it can pair with when forming a double helix. 
 * Will be important during base-pairing puzzle portion of game.
 * NOTE: C,G,A and T occur in DNA, whereas C,G,A and U occur in RNA.
*/
enum Nucleobase{
  T ("thymine"),
  C ("cytosine"),
  G ("guanine"),
  A ("adenine"),
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
  public static String toString(Nucleobase n){
    return n.name;
  }
  // returns true if a given base, other, would naturally pair with this base.
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
