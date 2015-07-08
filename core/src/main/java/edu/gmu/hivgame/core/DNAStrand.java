package edu.gmu.hivgame.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import static playn.core.PlayN.*;
import java.util.*;

//A single strand of DNA or RNA.
//Double-stranded DNA is implemented with two linked DNAStrands.
//contains a linked list of nucleotides, and distance joints between two adjacent nucleotides.
public class DNAStrand{
  private AidsAttack game;
  private LevelTwo level;
  private LinkedList<Nucleotide> strand;
  private Body groundBody;
  private MouseJoint mouseJoint;

  private DNAStrand(AidsAttack game, Level level){
    this.game = game;
    this.level = (LevelTwo) level;
    strand = new LinkedList<Nucleotide>();
  }
  public static DNAStrand make(AidsAttack game, Level level, float x, float y, int length){
    DNAStrand s = new DNAStrand(game, level);
    BodyDef bodyDef = new BodyDef();
    s.groundBody = level.physicsWorld().createBody(bodyDef);
    s.mouseJoint = null;
    Nucleotide n = Nucleotide.make(game,level,Nucleobase.valueOf("C"),x, y, 0f);
    Nucleotide n2 = Nucleotide.make(game,level,Nucleobase.valueOf("T"),x+1.1f, y, 0f);
    s.addLast(n);
    s.addLast(n2);
    return s;
  }

  public MouseJoint getMouseJoint(){
    return this.mouseJoint;
  }

  // Parameters governing motion of strand
  float minLength = 1f;
  float forceScale = 10f;

  public void attractToPointer(Vec2 target, Body body){
    MouseJointDef def = new MouseJointDef();
    def.bodyA = groundBody;
    def.bodyB = body;
    def.target.set(target);
    //def.maxForce = 1000f * body.getMass();
    mouseJoint = (MouseJoint) level.physicsWorld().createJoint(def);
  }
  
  public void attractHead(Vec2 target){
    if(strand.size() == 0){
      return;
    }
    Nucleotide n = strand.getFirst();
    Vec2 force = target.sub(n.position());
    float length = force.normalize(); // Alters force vector, length returned
    if(length > minLength){
      force.mulLocal(forceScale);
    }
    n.body().applyForceToCenter(force);
  }

  private void addLast(Nucleotide n){
    if(strand.size() > 0){
      Nucleotide prev = strand.getLast();
      prev.strandLink(n);
    }
    strand.addLast(n);
  }

  public void update(int delta){
    if(strand.size() == 0){
      return;
    }
    ListIterator<Nucleotide> iterator = strand.listIterator(0);
    while(iterator.hasNext()){
      iterator.next().update(delta);
    }
  }
  public void paint(float alpha){
    if(strand.size() == 0){
      return;
    }
    ListIterator<Nucleotide> iterator = strand.listIterator(0);
    while(iterator.hasNext()){
      iterator.next().paint(alpha);
    }
  }
}
