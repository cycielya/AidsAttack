package edu.gmu.hivgame.core;

import java.util.Random;

/* Each nucleotide contains one nucleobase, which determines what other
 * nucleotides it can pair with when forming a double helix. 
 * Will be important during base-pairing puzzle portion of game.
 * NOTE: C,G,A and T occur in DNA, whereas C,G,A and U occur in RNA.
*/
public enum Nucleobase{
  T ("thymine"),
  C ("cytosine"),
  G ("guanine"),
  A ("adenine"),
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
  public static String toString(Nucleobase n){
    return n.name;
  }
  public static Nucleobase randomRNABase(){
    Random r = new Random();
    //values().length -1 to include only four options instead of five.
    //+1 at end to shift option range from 0-3 to 1-4.
    int baseIndex = Math.abs(r.nextInt()) % (Nucleobase.values().length-1) + 1;
    return Nucleobase.values()[baseIndex];
  }
  public static Nucleobase randomDNABase(){
    Random r = new Random();
    int baseIndex = Math.abs(r.nextInt()) % (Nucleobase.values().length-1);
    return Nucleobase.values()[baseIndex];
  }
  public static Nucleobase randomBase(){
    Random r = new Random();
    int baseIndex = Math.abs(r.nextInt()) % Nucleobase.values().length;
    return Nucleobase.values()[baseIndex];
  }
  // returns true if a given base, other, would naturally pair with this base.
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
