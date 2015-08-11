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

  private DoubleHelix(AidsAttack game, Level level){
    this.game = game;
    this.level = (LevelTwo) level;
    this.strandA = null;
    this.strandB = null;
    this.newStrand = false;
  }

  public void setStrandA(DNAStrand strandA){
    this.strandA = strandA;
    this.newStrand = true;
  }
  public void setStrandB(DNAStrand strandB){
    this.strandB = strandB;
    this.newStrand = true;
  }
  public DNAStrand getStrandA(){
    return this.strandA;
  }
  public DNAStrand getStrandB(){
    return this.strandB;
  }

  private void joinStrands(){
    if(strandA == null || strandB == null){
      return;
    }
  }

  public void update(int delta){
    if(this.newStrand){
    }
  }
}
