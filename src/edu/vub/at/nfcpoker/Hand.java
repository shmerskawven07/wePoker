/*
 * wePoker: Play poker with your friends, wherever you are!
 * Copyright (C) 2012, The AmbientTalk team.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package edu.vub.at.nfcpoker;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

// From http://www.dreamincode.net/forums/topic/116864-how-to-make-a-poker-game-in-java/

public class Hand implements Comparable<Hand> {
	public Card[] cards;
	private int[] value;
	
	
	// for kryo
	public Hand() {}

	public Hand(Card[] cards) {
		value = new int[6];
		this.cards = cards;

		int[] ranks = new int[14];
		int[] orderedRanks = new int[5];	 //miscellaneous cards that are not otherwise significant
		boolean flush=true, straight=false;
		int sameCards=1,sameCards2=1;
		int largeGroupRank=0,smallGroupRank=0;
		int index=0;
		int topStraightValue=0;

		for (int x=0; x<=13; x++) {
			ranks[x]=0;
		}
		for (int x=0; x<=4; x++) {
			ranks[ cards[x].getRank() ]++;
		}
		for (int x=0; x<4; x++) {
			if ( cards[x].getSuit() != cards[x+1].getSuit() )
				flush=false;
		}
		
		for (int x=13; x>=1; x--) {
			if (ranks[x] > sameCards) {
				if (sameCards == 1) {
					largeGroupRank = x;
				} else {
					sameCards2 = sameCards;   // if sameCards was assigned to, write data from 
					smallGroupRank = x;		  // top group to low group			   
				}
				sameCards = ranks[x];		  // update sameCards to new greatest sameCards value in ranks
			} else if (ranks[x] > sameCards2) {
				sameCards2 = ranks[x];
				smallGroupRank = x;
			}
		}

		if (ranks[1]==1) {
			orderedRanks[index]=14;
			index++;
		}

		for (int x=13; x>=2; x--) {
			if (ranks[x]==1) {
				orderedRanks[index]=x;
				index++;
			}
		}
		
		for (int x=1; x<=9; x++) {
			if (ranks[x]==1 && ranks[x+1]==1 && ranks[x+2]==1 && ranks[x+3]==1 && ranks[x+4]==1) {
				straight=true;
				topStraightValue=x+4; //4 above bottom value
				break;
			}
		}

		if (ranks[10]==1 && ranks[11]==1 && ranks[12]==1 && ranks[13]==1 && ranks[1]==1) {
			straight=true;
			topStraightValue=14; //higher than king
		}
		
		for (int x=0; x<=5; x++) {
			value[x]=0;
		}


		 //start hand evaluation
		if (sameCards == 1) {
			value[0]=1;
			value[1]=orderedRanks[0];
			value[2]=orderedRanks[1];
			value[3]=orderedRanks[2];
			value[4]=orderedRanks[3];
			value[5]=orderedRanks[4];
		}

		if (sameCards==2 && sameCards2==1)
		{
			value[0]=2;
			value[1]=largeGroupRank; //rank of pair
			value[2]=orderedRanks[0];
			value[3]=orderedRanks[1];
			value[4]=orderedRanks[2];
		}

		if (sameCards==2 && sameCards2==2) //two pair
		{
			value[0]=3;
			value[1]= largeGroupRank>smallGroupRank ? largeGroupRank : smallGroupRank; //rank of greater pair
			value[2]= largeGroupRank<smallGroupRank ? largeGroupRank : smallGroupRank;
			value[3]=orderedRanks[0];  //extra card
		}

		if (sameCards==3 && sameCards2!=2)
		{
			value[0]=4;
			value[1]= largeGroupRank;
			value[2]=orderedRanks[0];
			value[3]=orderedRanks[1];
		}

		if (straight && !flush)
		{
			value[0]=5;
			value[1]=topStraightValue;
		}

		if (flush && !straight)
		{
			value[0]=6;
			value[1]=orderedRanks[0]; //tie determined by ranks of cards
			value[2]=orderedRanks[1];
			value[3]=orderedRanks[2];
			value[4]=orderedRanks[3];
			value[5]=orderedRanks[4];
		}

		if (sameCards==3 && sameCards2==2)
		{
			value[0]=7;
			value[1]=largeGroupRank;
			value[2]=smallGroupRank;
		}

		if (sameCards==4)
		{
			value[0]=8;
			value[1]=largeGroupRank;
			value[2]=orderedRanks[0];
		}

		if (straight && flush)
		{
			value[0]=9;
			value[1]=topStraightValue;
		}


	}
  

	void display()
	{
		String s;
		switch( value[0] )
		{

			case 1:
				s="high card";
				break;
			case 2:
				s="pair of " + Card.rankAsString(value[1]) + "\'s";
				break;
			case 3:
				s="two pair " + Card.rankAsString(value[1]) + " " + Card.rankAsString(value[2]);
				break;
			case 4:
				s="three of a kind " + Card.rankAsString(value[1]) + "\'s";
				break;
			case 5:
				s=Card.rankAsString(value[1]) + " high straight";
				break;
			case 6:
				s="flush";
				break;
			case 7:
				s="full house " + Card.rankAsString(value[1]) + " over " + Card.rankAsString(value[2]);
				break;
			case 8:
				s="four of a kind " + Card.rankAsString(value[1]);
				break;
			case 9:
				s="straight flush " + Card.rankAsString(value[1]) + " high";
				break;
			default:
				s="error in Hand.display: value[0] contains invalid value";
		}
		s = "				" + s;
		System.out.println(s);
	}

	void displayAll()
	{
		for (int x=0; x<5; x++)
			System.out.println(cards[x]);
	}

	public int compareTo(Hand that)
	{
		for (int x=0; x<6; x++)
		{
			if (this.value[x]>that.value[x])
				return 1;
			else if (this.value[x]<that.value[x])
				return -1;
		}
		return 0;
	}
	
	public static Hand makeBestHand(Set<Card> base, Collection<Card> holeCards) {
		Vector<Card> pool = new Vector<Card>();
		pool.addAll(base);
		pool.addAll(holeCards);
				
		Set<Hand> hands = makeHands(pool);
		
		Iterator<Hand> it = hands.iterator();
		Hand bestHand = it.next();
		
		while (it.hasNext()) {
			Hand next = it.next();
			if (bestHand.compareTo(next) < 0)
				bestHand = next;
		}
			
		return bestHand;
	}

	private static Set<Hand> makeHands(Vector<Card> pool) {
		Set<Card[]> s = new HashSet<Card[]>();
		s.add(pool.toArray(new Card[pool.size()]));
		return makeHands(s, pool.size());
	}

	private static Set<Hand> makeHands(Set<Card[]> hands, int size) {
		if (size == 5) {
			Set<Hand> ret = new HashSet<Hand>();
			for (Card[] cards : hands) 
				ret.add(new Hand(cards));
			return ret;
		} else {
			Set<Card[]> next = new HashSet<Card[]>();
			for (Card[] cards : hands) {
				Vector<Card> baseCards = new Vector<Card>();
				for (Card c : cards)
					baseCards.add(c);
				
				for (int skip = 0; skip < size; skip++) {
					@SuppressWarnings("unchecked")
					Vector<Card> newCards = (Vector<Card>) baseCards.clone();
					newCards.remove(skip);
					next.add(newCards.toArray(new Card[size - 1]));
				}
			}
			return makeHands(next, size - 1);
		}
	}

	public int getValue() {
		return value[0];
	}
}

