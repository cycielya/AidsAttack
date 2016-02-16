package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;
import java.util.Random;

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
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.collision.Manifold;

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
// Nucleotides only connect if both Nucleotides have boolean inRT set to true.
//    Rename image loading method
//    Test synchronous image loading w/ html,javascript, etc
public class Nucleotide implements CollisionHandler, ContactListener{
  AidsAttack game;
  LevelTwo level;
  DNAStrand strand;
  private float width = 1f;
  private float height = 1f;
  private float prevX, prevY, prevA;
  private Nucleobase nBase; // see enum Nucleobase definition in Nucleobase.java
  private int fillColor;
  private Body body;
  private Fixture myBodyFixture;
  private ImageLayer myLayer;
  private ImageLayer myNucleobaseLayer;
  private Nucleotide pair; // the Nucleotide it is base-paired with
  private Body groundBody; // used for MouseJoint, does not have relevance to anything else
  private MouseJoint mouseJoint; // to make a nucleotide player-controllable
  private boolean inRT; //designates if currently in contact with ReverseTranscriptase

  private Nucleotide(){}
  public static Nucleotide make(AidsAttack game, Level level, Nucleobase nBase, float x, float y, float ang){
    Nucleotide n = new Nucleotide();
    n.nBase = nBase;
    n.pair = null;
    n.game = game;
    n.level = (LevelTwo) level;
    n.initPhysicsBody(n.level.physicsWorld(), x, y, ang);
    n.inRT = false;
    n.drawNucleotideImage();
    n.level.addLayer(n.myLayer);
    n.level.addLayer(n.myNucleobaseLayer);
    BodyDef groundBodyDef = new BodyDef();
    n.groundBody = level.physicsWorld().createBody(groundBodyDef); // groundBody only relevant for MouseJoint
    n.strand = null;
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
  private void drawNucleobaseImage(){
    String imageFile = "A.png";
    if(this.nBase == Nucleobase.A){
      imageFile = "A.png";
      this.fillColor = 0xffd44844;
    }
    else if(this.nBase == Nucleobase.G){
      imageFile = "G.png";
      this.fillColor = 0xff3c8757;
    }
    else if(this.nBase == Nucleobase.U){
      imageFile = "U.png";
      this.fillColor = 0xfffac22a;
    }
    else if(this.nBase == Nucleobase.T){
      imageFile = "T.png";
      this.fillColor = 0xfffac22a;
    }
    else if(this.nBase == Nucleobase.C){
      imageFile = "C.png";
      this.fillColor = 0xff2469ae;
    }
    Image myNucleobaseImage = assets().getImageSync("images/"+imageFile);
    System.out.println("My base's image's width: "+myNucleobaseImage.width());
    myNucleobaseLayer = graphics().createImageLayer(myNucleobaseImage);
    myNucleobaseLayer.setOrigin(myNucleobaseImage.width() / 2f, myNucleobaseImage.height() / 2f);
    myNucleobaseLayer.setScale(getWidth()/myNucleobaseImage.width(),getHeight()/myNucleobaseImage.height());
    myNucleobaseLayer.setTranslation(x(), y());
    myNucleobaseLayer.setRotation(ang());
    myNucleobaseLayer.setDepth(2f);
  }

  private void drawNucleotideImage(){
    float imageSize = 100;
    drawNucleobaseImage();
    CanvasImage image = graphics().createImage(imageSize, imageSize);
    Canvas canvas = image.canvas();
    //should be a darkish yellow, or gold color
    canvas.setFillColor(this.fillColor);
    //coordinates are for upper-left corner placement
    canvas.fillRect(0f, 0f, imageSize, imageSize);
    myLayer = graphics().createImageLayer(image);
    myLayer.setOrigin(image.width() / 2f, image.height() / 2f);
    myLayer.setScale(getWidth()/imageSize,getHeight()/imageSize);
    myLayer.setTranslation(x(), y());
    myLayer.setRotation(ang());
    myLayer.setDepth(1.5f);

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
        //bodies are set awake when moving. is this necessary?
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
  // TODO: create a version of this to link two paired nucleotides! Or edit basePair()
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
  public boolean inStrand(){
    return (this.strand != null)? true : false;
  }
  //If nucleotide not in strand, returns null
  public DNAStrand getStrand(){
    return this.strand;
  }
  public void setStrand(DNAStrand strand){
    this.strand = strand;
  }
  public boolean pairsWith(Nucleotide other){
    return this.nBase.pairsWith(other.nBase);
  }
  public boolean inRT(){
    return this.inRT;
  }
  //attempts to pair two Nucleotides. Returns true on successful pairing, false on wrong pair.
  //does NOT create physics link between the nucleotides.
  public boolean basePair(Nucleotide other){
    this.pair = other;
    other.pair = this;
    //Need to establish physical link between Nucleotides. Should be closer together than stranded Nucleotides.
    RopeJointDef def = new RopeJointDef();
    def.bodyA = this.body;
    def.bodyB = other.body;
    //positioning of ropejoints depends on this being in a strand, other being the free nucleotide
    //places other below this, so may cause funky physics behavior immediately after connecting.
    //note: sets Anchors at offset from body's origin, which is center on Nucleotide.
    def.localAnchorA.set(0f-this.getWidth()/2f, 0f+this.getHeight()/2f); //position at lower left corner
    def.localAnchorB.set(0f-other.getWidth()/2f, 0f-other.getHeight()/2f); //position at upper left corner
    def.maxLength = .5f;
    def.collideConnected = true;
    RopeJoint rj = (RopeJoint) this.level.physicsWorld().createJoint(def);
    def.localAnchorA.set(0f+this.getWidth()/2f, 0f+this.getHeight()/2f); //position at lower right corner
    def.localAnchorB.set(0f+other.getWidth()/2f, 0f-other.getHeight()/2f); //position at upper right corner
    RopeJoint rj2 = (RopeJoint) this.level.physicsWorld().createJoint(def);
    if (this.pairsWith(other)){
      return true;
    }
    else{
      return false;
    }
  }
  public boolean isBasePaired(){
    return (this.pair != null);
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
    myNucleobaseLayer.setTranslation(x,y);
    myNucleobaseLayer.setRotation(a);
  }
  //beginContact, endContact, postSolve and preSolve are all required functions
  //for the ContactListener interface.
  //NOTE: No extra code should be added to these functions unless ABSOLUTELY NECESSARY.
  //The inRT field should not be set anywhere other than in beginContact
  //and in endContact, except for when it is initialized to false.
  public void beginContact(Contact contact){
    Object a = contact.getFixtureA().getUserData();
    Object b = contact.getFixtureB().getUserData();
    if(a != null && b != null){
      if(a instanceof ReverseTranscriptase || b instanceof ReverseTranscriptase){
        //allows Nucleotides to recognize when they are at least partially within the
        //area of the RT. Will only base-pair if both are.
        this.inRT = true;
      }
    }
  }
  public void endContact(Contact contact){
    Object a = contact.getFixtureA().getUserData();
    Object b = contact.getFixtureB().getUserData();
    if(a != null && b != null){
      if(a instanceof ReverseTranscriptase || b instanceof ReverseTranscriptase){
        //allows Nucleotides to recognize when they have entirely left the area of
        //the RT.
        this.inRT = false;
      }
    }
  }
  public void postSolve(Contact contact, ContactImpulse impulse){}
  public void preSolve(Contact contact, Manifold oldManifold){}

  public void handleCollision(Fixture me, Fixture other){
    if(me != this.myBodyFixture){
      return;
    }
    if(other.m_userData instanceof ReverseTranscriptase){
      System.out.println("Hit the RT!");
    }
    else if (other.m_userData instanceof Nucleotide){
      Nucleotide otherN = (Nucleotide) other.m_userData;
      //here, check if otherN is in a strand
      //if in a strand, do nothing.
      //if free, then alert my strand's DH, if it has one, with this and otherN, unless I'm already paired.
      //TODO: clean this up, if at all possible. May just have to be really long if-statement.
      //Elminated need for alert() method in strand, however. Yay! Less redundant code!
      if(!otherN.inStrand() && this.inStrand() && this.strand.inDoubleHelix() && !this.isBasePaired()){
        if(this.inRT() && otherN.inRT()){
          this.strand.getDoubleHelix().alert(this, otherN);
          System.out.println("Hey strand! Found a straggler!");
        }
      }
    }
  }
}
