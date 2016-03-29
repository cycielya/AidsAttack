package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;
import org.jbox2d.dynamics.Fixture;

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

//Level Two: Reverse Transcriptase
public class LevelTwo extends Level{
  ReverseTranscriptase theRT;
  DNAStrand viralRNA1;
  DNAStrand viralRNA2;
  DoubleHelix viralDH; //Initially only contains a single strand, which is the viralRNA.
  CellInterior theCI;

  static LevelTwo make(AidsAttack game){
    LevelTwo lt = new LevelTwo();
    lt.game = game;
    return lt;
  }

  @Override
  void initLevel(Camera camera){
    super.initLevel(camera);
    gameOver = false;
    success = false;

    // message to display when moving to level two.
    // this is added directly to graphics().rootLayer(), like the gameOver message,
    // so that it is not affected by scaling of worldLayer
    CanvasImage image = graphics().createImage(200,200);
    Canvas canvas = image.canvas();
    canvas.setFillColor(0xff050505);
    canvas.drawText("Level Two!",10,100);
    ImageLayer welcomeLayer = graphics().createImageLayer(image);
    welcomeLayer.setDepth(7f);
    graphics().rootLayer().add(welcomeLayer);
    System.out.println("welcomelayer added");
    System.out.println("RootLayer size is: "+graphics().rootLayer().size());


    // make the ReverseTranscriptase
    // This should create the image as well as the physics body
    this.theRT = ReverseTranscriptase.make(game, this, 15f, 10f, 0f);

    //Do I need to make them actual fields? Or maybe just variables for this method?
    this.viralRNA1 = DNAStrand.make(game, this, 10f, 20f, 5);
    this.viralRNA2 = DNAStrand.make(game, this, 10f, 22f, 0);
    this.viralDH = DoubleHelix.make(game, this, viralRNA1, viralRNA2);
    //this.viralDH.setStrandA(viralRNA);

    this.theCI = CellInterior.make(game, this, 20);

  }

  public void testStrandEnds(Nucleotide n){
    if(this.viralRNA1.compareFirst(n) || this.viralRNA1.compareLast(n)){
      System.out.println("Starting minigame!");
    }
  }
  @Override
  void successLevel(){
    super.successLevel();
    game.gameMessage(String.format("Level Complete! %d good matches, %d mismatches.",
                                  this.viralDH.getCorrectMatches(),
                                  this.viralDH.getMismatches()));
  }

  void updateLevel(int delta, int time){
    Nucleotide theUNA = viralDH.getUNA();
    if (theUNA != null && theUNA.inRT() && theUNA.notBoundToRT()){
      theRT.bindUNA(theUNA);
    }
    theRT.update(delta);
    //viralRNA1.update(delta);
    //viralRNA2.update(delta);
    //do not need to update strands individually because they are updated in viralDH
    viralDH.update(delta);
    theCI.update(delta);
    Contact contact = m_world.getContactList();
    while(contact != null){
      if(contact.isTouching()){
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        if(fixtureA.m_userData instanceof CollisionHandler){
          CollisionHandler ch = (CollisionHandler) fixtureA.m_userData;
          ch.handleCollision(fixtureA, fixtureB);
        }
        if(fixtureB.m_userData instanceof CollisionHandler){
          CollisionHandler ch = (CollisionHandler) fixtureB.m_userData;
          ch.handleCollision(fixtureB, fixtureA);
        }
      }
      contact = contact.getNext();
    }
  }

  void update(int delta, int time){
    if(!gameOver && !success){
      super.update(delta, time);
      updateLevel(delta, time);
    }
    // the step delta is fixed so box2d isn't affected by framerate
    m_world.step(0.033f, 10, 10);
  }
  void paint(float alpha){
    if(!gameOver && !success){
      theRT.paint(alpha);
      viralRNA1.paint(alpha);
      viralRNA2.paint(alpha);
      //no need to paint viralDH because no images associated with it
      theCI.paint(alpha);
    }
  }
  String levelName(){
    return "Level Two";
  }
}
