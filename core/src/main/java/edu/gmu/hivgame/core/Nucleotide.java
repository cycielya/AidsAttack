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


public class Nucleotide{
  AidsAttack game;
  private float width = 1f;
  private float height = 1f;
  private Nucleobase nBase;
  private Body body;
  private Fixture myBodyFixture;
  private float prevX, prevY, prevA;
  LevelTwo level;

  private Nucleotide(){}
  public Nucleotide make(AidsAttack game, Level level, Nucleobase nBase, float x, float y, float ang){
    Nucleotide n = new Nucleotide();
    n.nBase = nBase;
    n.game = game;
    return n;
  }
  //method for testing only
  public Nucleotide make(){
    return new Nucleotide();
  }
  private void initPhysicsBody(World world, float x, float y, float ang){
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position = new Vec2(x, y);
    bodyDef.angle = ang;
    Body body = world.createBody(bodyDef);
    body.setSleepingAllowed(false);

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
  public boolean pairsWith(Nucleotide other){
    return this.nBase.pairsWith(other.nBase);
  }
  public float getWidth(){
    return this.width;
  }
  public float getHeight(){
    return this.height;
  }
}

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
