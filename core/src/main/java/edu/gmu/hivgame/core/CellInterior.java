package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

import java.util.Random;
import java.util.LinkedList;
import java.util.ListIterator;

//Class designed to hold, update, and arrange all miscellaneous
//objects of TCell interior, to prevent excessive clutter in LevelTwo.
//May currently only hold loose TCell Nucleotides.
public class CellInterior{
  AidsAttack game;
  LevelTwo level;
  LinkedList<Nucleotide> m_Nucleotides;
  Random r;

  private CellInterior(){}
  public static CellInterior make(AidsAttack game, Level level, int numNucleotides){
    CellInterior ci = new CellInterior();
    ci.m_Nucleotides = new LinkedList<Nucleotide>();
    ci.game = game;
    ci.level = (LevelTwo) level;
    ci.r = new Random();
    ci.init(numNucleotides);
    return ci;
  }
  private void init(int numNucleotides){
    Nucleotide n;
    float x;
    float y;
    for(int i=0; i<numNucleotides; i++){
      x = Math.abs(r.nextFloat()*50);
      y = Math.abs(r.nextFloat()*50);
      n = Nucleotide.make(this.game, this.level, Nucleobase.randomBase(), x, y, 0f);
      m_Nucleotides.addLast(n);
    }
  }
  public void update(int delta){
    ListIterator<Nucleotide> itr = m_Nucleotides.listIterator(0);
    while(itr.hasNext()){
      itr.next().update(delta);
    }
  }
  public void paint(float alpha){
    ListIterator<Nucleotide> itr = m_Nucleotides.listIterator(0);
    while(itr.hasNext()){
      itr.next().paint(alpha);
    }
  }
}
