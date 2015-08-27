package edu.gmu.hivgame.core;
import org.jbox2d.common.Vec2;
public class DoubleHelix{
  private AidsAttack game;
  private LevelTwo level;
  private DNAStrand strandA;
  private DNAStrand strandB;
  private int correctMatches;
  private int mismatches;
  private boolean newStrand; //Has a strand been added that has not been base-paired yet?
  private Nucleotide uNA; //unpaired Nucleotide A (on strandA)

  private DoubleHelix(AidsAttack game, Level level){
    this.game = game;
    this.level = (LevelTwo) level;
    this.strandA = null;
    this.strandB = null;
    this.uNA = null;
    this.newStrand = false;
  }

  public void setStrandA(DNAStrand strandA){
    this.strandA = strandA;
    this.strandA.setDoubleHelix(this);
    this.newStrand = true;
  }
  public void setStrandB(DNAStrand strandB){
    this.strandB = strandB;
    this.strandB.setDoubleHelix(this);
    this.newStrand = true;
  }
  //returns true if uNA now holds a value, false if uNA is now null
  //this is regardless of whether the previous uNA was correct or not.
  public boolean evalUNA(){
    if(this.strandA == null){
      this.uNA = null;
      return false;
    }
    else if(this.strandB == null){
      this.uNA = strandA.getFirst();
      if(this.uNA == null){
        return false;
      }
      return true;
    }
    else if(this.strandA.size() > this.strandB.size()){
      this.uNA = strandA.get(this.strandB.size());
      if(this.uNA == null){
        return false;
      }
      return true;
    }
    else{
      this.uNA = null;
      return false;
    }
  }
  public Nucleotide getUNA(){
    return this.uNA;
  }
  public DNAStrand getStrandA(){
    return this.strandA;
  }
  public DNAStrand getStrandB(){
    return this.strandB;
  }

  //intended for if generating a double helix from two pre-populated DNAStrands.
  private void joinStrands(){
    if(strandA == null || strandB == null){
      return;
    }
  }

  public void update(int delta){
    if(this.newStrand){
      evalUNA();
    }
    if(strandA != null){
      this.strandA.update(delta);
    }
    if(strandB != null){
      this.strandB.update(delta);
    }
  }
}
