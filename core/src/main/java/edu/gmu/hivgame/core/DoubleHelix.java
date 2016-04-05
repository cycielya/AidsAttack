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
  private boolean completed; //tracks whether every Nucleotide in strandA has been paired

  private DoubleHelix(AidsAttack game, Level level){
    this.game = game;
    this.level = (LevelTwo) level;
    this.strandA = null;
    this.strandB = null;
    this.uNA = null;
    this.newStrand = false;
    this.completed = false;
  }
  public static DoubleHelix make(AidsAttack game, Level level){
    DoubleHelix dh = new DoubleHelix(game, level);
    return dh;
  }
  public static DoubleHelix make(AidsAttack game, Level level, DNAStrand strandA, DNAStrand strandB){
    DoubleHelix dh = new DoubleHelix(game, level);
    dh.setStrandA(strandA);
    dh.setStrandB(strandB);
    return dh;
  }

  public void setStrandA(DNAStrand strandA){
    this.strandA = strandA;
    this.strandA.setDoubleHelix(this);
    this.strandA.addBackboneImage("tendril_red.png");
    this.newStrand = true;
  }
  public void setStrandB(DNAStrand strandB){
    this.strandB = strandB;
    this.strandB.setDoubleHelix(this);
    this.newStrand = true;
  }
  //returns true if uNA now holds a value, false if uNA is now null
  //does not take into account previous values of uNA.
  //uNA will always be considered to be in strandA.
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
      this.completed = true;
      return false;
    }
  }

  //TODO: When would I need this? Are these just arbitrary getters? Attempt to eliminate.
  public Nucleotide getUNA(){
    return this.uNA;
  }
  public DNAStrand getStrandA(){
    return this.strandA;
  }
  public DNAStrand getStrandB(){
    return this.strandB;
  }
  public boolean getCompleted(){
    return this.completed;
  }
  public int getCorrectMatches(){
    return this.correctMatches;
  }
  public int getMismatches(){
    return this.mismatches;
  }
  //Alert method to be used by DNAStrand when a Nucleotide in the strand hits a free nucleotide.
  public void alert(Nucleotide mine, Nucleotide other){
    //Assumption checking.
    //if( mine == null || other == null || other.inStrand() || this.getUNA() == null
        //|| !this.getUNA().equals(mine) ){
      //System.out.println("Strand, why you lyin' to me now?");
    if(mine == null || other == null){
      System.out.println("alert failed: nucleotide was null.");
      return;
    }
    if(other.inStrand()){
      System.out.println("alert failed: other was in strand.");
      return;
    }
    if(this.getUNA() == null){
      System.out.println("alert failed: UNA was null.");
      return;
    }
    if(!this.getUNA().equals(mine)){
      System.out.println("alert failed: mine was not UNA.");
      System.out.println("My base is: "+mine.getBase());
      System.out.println("UNA base is: "+this.getUNA().getBase());
      return;
    }

    System.out.println("Cool! uNA found someone!");
    //Now do the thing!
    //Must base-pair other with mine.
    boolean goodPair = mine.basePair(other);
    this.level.theRT.unBindUNA(mine);
    if(goodPair){
      correctMatches++;
    }
    else{
      mismatches++;
    }
    System.out.println("Good matches: "+correctMatches+"\nMismatches: "+mismatches);
    //then add other to end of strandB
    strandB.addNucleotide(other);
    evalUNA();
    /*if(yes){
      this.level.theRT.bindUNA(this.getUNA());
    }
    else{
      System.out.println("Oh no! I don't have a UNA!");
    }*/
  }

  //intended for if generating a double helix from two pre-populated DNAStrands.
  //Not fully implemented because not needed yet.
  private void joinStrands(){
    if(strandA == null || strandB == null){
      return;
    }
  }

  public void update(int delta){
    if(this.newStrand){
      evalUNA();
    }
    if(this.completed){
      //System.out.printf("Strand completed! %d good matches, %d bad matches.\n", correctMatches, mismatches);
      //game.successCurrentLevel();
      this.level.successLevel();
    }
    if(strandA != null){
      this.strandA.update(delta);
    }
    if(strandB != null){
      this.strandB.update(delta);
    }
  }
}
