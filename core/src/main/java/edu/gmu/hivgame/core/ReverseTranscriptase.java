package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

import java.lang.Math;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;

import static playn.core.PlayN.assets;
import static playn.core.PlayN.graphics;
import static playn.core.PlayN.pointer;
import playn.core.Pointer;

import playn.core.CanvasImage;
import playn.core.Canvas;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.Image;
import playn.core.util.Callback;
import playn.core.TextFormat;
import playn.core.AbstractTextLayout;
import playn.core.TextLayout;
import playn.core.TextWrap;
import playn.core.Font;
import playn.core.util.TextBlock;

//TODO: add second zone/object to split DNAStrand after reverse transcription
public class ReverseTranscriptase implements CollisionHandler {
  AidsAttack game;
  LevelTwo level;
  private float prevX, prevY, prevA; // for calculating interpolation
  final float radius = 1f;
  final float diameter = radius*2f;
  private Body body;
  private Fixture myBodyFixture;
  private ImageLayer myLayer;
  private Body groundBody; // used for MouseJoint, does not have relevance to anything else
  private MouseJoint mouseJoint; // to make a nucleotide player-controllable

  private ReverseTranscriptase(){}
  public static ReverseTranscriptase make(AidsAttack game, Level level, float x, float y, float ang){
    ReverseTranscriptase rt = new ReverseTranscriptase();
    rt.game = game;
    rt.initPhysicsBody(level.physicsWorld(), x, y, ang);
    rt.drawRTImage();

    level.addLayer(rt.myLayer);

    rt.level = (LevelTwo) level;
    rt.prevX = rt.x(); rt.prevY = rt.y(); rt.prevA = rt.ang();
    BodyDef groundBodyDef = new BodyDef();
    rt.groundBody = level.physicsWorld().createBody(groundBodyDef); // groundBody only relevant for MouseJoint
    return rt;
  }

  void initPhysicsBody(World world, float x, float y, float angle) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position = new Vec2(x, y);
    bodyDef.angle = angle;
    bodyDef.linearDamping = 1.0f;

    this.body = world.createBody(bodyDef);
    //body.setSleepingAllowed(false);

    CircleShape shape = new CircleShape();
    shape.m_radius = radius;
    shape.m_p.set(0.0f, 0.0f);

    FixtureDef fd = new FixtureDef();
    fd.shape = shape;
    fd.isSensor = true;
    fd.friction = 0.1f;
    fd.restitution = 0.4f;
    fd.density = 1.0f;
    this.myBodyFixture = body.createFixture(fd);
    this.myBodyFixture.m_userData = this;

  }

  private void drawRTImage(){
    float imageSize = 100;
    CanvasImage image = graphics().createImage(imageSize, imageSize);
    Canvas canvas = image.canvas();
    //canvas.setFillColor(0xff050505);
    canvas.setFillColor(0xffff0000);
    canvas.fillCircle(image.width()/2f, image.height()/2f, imageSize/2f);
    this.myLayer = graphics().createImageLayer(image);
    myLayer.setOrigin(image.width()/2f, image.height()/2f);
    myLayer.setTranslation(x(), y());
    myLayer.setRotation(ang());
    myLayer.setScale(diameter/imageSize, diameter/imageSize);
    myLayer.setDepth(2);
    this.myLayer.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Pointer.Event event){
        Vec2 pointerLocation = new Vec2(event.x(), event.y());
        Vec2 physLocation = new Vec2(level.camera.screenXToPhysX(pointerLocation.x),
                                     level.camera.screenYToPhysY(pointerLocation.y));
        MouseJointDef def = new MouseJointDef();
        def.bodyA = level.physicsWorld().createBody(new BodyDef());
        def.bodyB = body;
        def.target.set(physLocation);
        def.maxForce = 1000f * body.getMass();
        mouseJoint = (MouseJoint) level.physicsWorld().createJoint(def);
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

  public void handleCollision(Fixture me, Fixture other){
    System.out.println("Ran into something!");
    if(me == this.myBodyFixture && other.m_userData instanceof Nucleotide){
      Nucleotide n = (Nucleotide) other.m_userData;
      if(n.inStrand()){
        DNAStrand strand = n.getStrand();
        if(strand.inDoubleHelix()){
          DoubleHelix dh = strand.getDoubleHelix();
          if(n.equals(dh.getUNA())){
            System.out.println("Hit UNA, binding!");
          }
        }
      }
    }
    else{
      System.out.println("Contacted: "+other.m_userData.toString());
    }
  }

  float x(){
    return body.getPosition().x;
  }
  float y(){
    return body.getPosition().y;
  }
  float ang(){
    return body.getAngle();
  }
  void update(int delta){
    prevX = x();
    prevY = y();
    prevA = ang();
  }
  void paint(float alpha){
    float x = (x() * alpha) + (prevX * (1f - alpha));
    float y = (y() * alpha) + (prevY * (1f - alpha));
    float a = (ang() * alpha) + (prevA * (1f - alpha));
    myLayer.setTranslation(x, y);
    myLayer.setRotation(a);
  }
}
