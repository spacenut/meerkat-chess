package com.alanjz.meerkat.moves

import com.alanjz.meerkat.Square
import com.alanjz.meerkat.moves.Move.{KingCapture, KingCastleShort, KingCastleLong, KingMove}
import com.alanjz.meerkat.pieces.Color.{Black, White}
import com.alanjz.meerkat.position.mutable.MaskNode
import com.alanjz.meerkat.util.numerics.BitMask.{File, Rank, BitMask}
import com.alanjz.meerkat.util.numerics.{BitMask, CastleMask}


class KingMover(val node : MaskNode) extends IntermediateMover {
  /**
   * Returns all possible moves regardless of legality
   * (Or pseudo-legality).
   *
   * These are specifically attacking moves.
   * This does not include castling, or pawn advances.
   *
   * @param king the king mask.
   * @return the attacks possible by this piece.
   */
  override def getAttacks(king : BitMask): BitMask = {

    // Return attacks.
    (king & ~Rank._8) << 8 |
      (king & ~Rank._8 & ~File.H) << 9 |
      (king & ~File.H) << 1 |
      (king & ~Rank._1 & ~File.H) >>> 7 |
      (king & ~Rank._1) >>> 8 |
      (king & ~Rank._1 & ~File.A) >>> 9 |
      (king & ~File.A) >>> 1 |
      (king & ~Rank._8 & ~File.A) << 7
  }

  def getCastles : BitMask = {
    if(node.castleMask == CastleMask.empty) return BitMask.empty

    var moves = BitMask.empty

    def process_castle(kingMask : BitMask,
                       emptyMask : BitMask,
                       attackMask : BitMask,
                       destMask : BitMask): Unit = {
      val atk = new Attacker(node).getAttackers(attackMask)
      val empty = node.allPieces & emptyMask

      if(atk == BitMask.empty && empty == BitMask.empty) {
        moves |= destMask
      }
    }

    // Branch on color.
    if(node.active == White) {

      // Test short white castling.
      if((node.castleMask & CastleMask.shortWhite) != CastleMask.empty) {

        process_castle(BitMask.Square.E1,
          BitMask.Square.F1 | BitMask.Square.G1,
          BitMask.Square.E1 | BitMask.Square.F1 | BitMask.Square.G1,
          BitMask.Square.G1)
      }

      // Test long white castling.
      if((node.castleMask & CastleMask.longWhite) != CastleMask.empty) {

        process_castle(BitMask.Square.E1,
          BitMask.Square.B1 | BitMask.Square.C1 | BitMask.Square.D1,
          BitMask.Square.E1 | BitMask.Square.C1 | BitMask.Square.D1,
          BitMask.Square.C1)
      }
    }
    else {

      // Test short black castling.
      if((node.castleMask & CastleMask.shortBlack) != CastleMask.empty) {

        process_castle(BitMask.Square.E8,
          BitMask.Square.F8 | BitMask.Square.G8,
          BitMask.Square.E8 | BitMask.Square.F8 | BitMask.Square.G8,
          BitMask.Square.G8)
      }

      // Test long black castling.
      if((node.castleMask & CastleMask.longBlack) != CastleMask.empty) {

        process_castle(BitMask.Square.E8,
          BitMask.Square.B8 | BitMask.Square.C8 | BitMask.Square.D8,
          BitMask.Square.E8 | BitMask.Square.C8 | BitMask.Square.D8,
          BitMask.Square.C8)
      }
    }

    moves
  }

  /**
   * Gets all pseudo-legal moves generated by this mover.
   * @return all pseudo-legal moves and captures.
   */
  override def getPseudos: BitMask = {
    val active = node.activePieces
    val inactive = node.inactivePieces

    // Return moves.
    getAttacks(node.activeKing) & ~active | getCastles
  }

  /**
   * Serializes the pseudo-legal moves.
   * @return a list of pseudo-legal moves of the appropriate type.
   */
  override def mkList: List[Move] = {
    val builder = List.newBuilder[Move]
    val activeKing = node.activeKing
    var moves = getPseudos

    while(moves != BitMask.empty) {
      val lsb = BitMask.bitScanForward(moves)
      var sources = getAttacks(1l << lsb) & activeKing

      // This must be a castle move.
      if(sources == BitMask.empty) {
        Square(lsb) match {
          case Square.C1 => builder += KingCastleLong(White)
          case Square.G1 => builder += KingCastleShort(White)
          case Square.C8 => builder += KingCastleLong(Black)
          case Square.G8 => builder += KingCastleShort(Black)
          case _ => throw new IllegalStateException(s"Illegal move to $lsb in bit mask.")
        }
      }
      else {
        while(sources != BitMask.empty) {
          val source = BitMask.bitScanForward(sources)
          if(node.empty(lsb)) {
            builder += KingMove(source,lsb)
          }
          else {
            builder += KingCapture(source, lsb, node.at(lsb).get)
          }
          sources &= (sources-1)
        }
      }
      moves &= (moves-1)
    }

    builder.result()
  }
}