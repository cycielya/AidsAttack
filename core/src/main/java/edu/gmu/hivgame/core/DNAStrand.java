package edu.gmu.hivgame.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.Joint;
import static playn.core.PlayN.*;
import java.util.*;
import java.util.Random;

//A single strand of DNA or RNA.
//Double-stranded DNA is implemented with two linked DNAStrands.
//contains a linked list of nucleotides, and distance joints between two adjacent nucleotides.
public class DNAStrand{
  private AidsAttack game;
  private LevelTwo level;
  private LinkedList<Nucleotide> strand;
  DoubleHelix dh;

  private DNAStrand(AidsAttack game, Level level){
    this.game = game;
    this.level = (LevelTwo) level;
    strand = new LinkedList<Nucleotide>();
  }
  public static DNAStrand make(AidsAttack game, Level level, float x, float y, int length){
    DNAStrand s = new DNAStrand(game, level);
    BodyDef bodyDef = new BodyDef();
    s.populateStrand(length, x, y);
    s.dh = null;
    return s;
  }
  public boolean inDoubleHelix(){
    return (this.dh != null)? true : false;
  }
  //returns null if dh is null.
  public DoubleHelix getDoubleHelix(){
    return this.dh;
  }
  public void setDoubleHelix(DoubleHelix dh){
    this.dh = dh;
  }

  private void populateStrand(int length, float x, float y){
    int i;
    float separationDist = 1.5f;
    for(i=0; i<length; i++){
      Nucleotide n = Nucleotide.make(this.game, this.level, Nucleobase.randomDNABase(), x+separationDist*i, y, 0f);
      this.addLast(n);
    }
  }
  public int size(){
    return this.strand.size();
  }

  private void addLast(Nucleotide n){
    if(strand.size() > 0){
      Nucleotide prev = strand.getLast();
      prev.strandLink(n);
    }
    n.setStrand(this);
    strand.addLast(n);
  }

  public void addNucleotide(Nucleotide n){
    this.addLast(n);
  }
  public ListIterator<Nucleotide> getIterator(){
    ListIterator<Nucleotide> itr = strand.listIterator(0);
    return itr;
  }

  // Returns true if n is the first element of the strand
  public boolean compareFirst(Nucleotide n){
    return n.equals(strand.getFirst());
  }
  // Returns true if n is the last element of the strand
  public boolean compareLast(Nucleotide n){
    return n.equals(strand.getLast());
  }

  // no physical body itself, so need to update each individual Nucleotide
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
