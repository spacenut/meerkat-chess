package com.alanjz.meerkat.moves

import com.alanjz.meerkat.moves.Move.{RookMove, RookCapture}
import com.alanjz.meerkat.position.mutable.MaskNode
import com.alanjz.meerkat.util.numerics.BitMask
import com.alanjz.meerkat.util.numerics.BitMask._

class RookMover(val node : MaskNode) extends IntermediateMover {

  /**
   * Returns all possible moves regardless of legality
   * (Or pseudo-legality).
   *
   * These are specifically attacking moves.
   * This does not include castling, or pawn advances.
   *
   * @param pieces the pieces mask.
   * @return the attacks possible by this piece.
   */
  override def getAttacks(pieces : BitMask): BitMask = {
    var rooks = pieces
    var moves = BitMask.empty

    // All pieces.
    val allPieces = node.allPieces

    while(rooks != BitMask.empty) {

      // Scan for the index of the LS bishop.
      val lsb = BitMask.bitScanForward(rooks)

      // Get the relevant rays.
      val northRay = BitMask.Ray.north(lsb)
      val eastRay = BitMask.Ray.east(lsb)
      val southRay = BitMask.Ray.south(lsb)
      val westRay = BitMask.Ray.west(lsb)

      // Get some intersections.
      val eastLSB = BitMask.bitScanForward(eastRay & allPieces)
      val southMSB = BitMask.bitScanReverse(southRay & allPieces)
      val westMSB = BitMask.bitScanReverse(westRay & allPieces)
      val northLSB = BitMask.bitScanForward(northRay & allPieces)

      // Get the combo rays.
      val northCombo =
        if(northLSB < 0) northRay
        else northRay ^ BitMask.Ray.north(northLSB)
      val eastCombo =
        if(eastLSB < 0) eastRay
        else eastRay ^ BitMask.Ray.east(eastLSB)
      val southCombo =
        if(southMSB < 0) southRay
        else southRay ^ BitMask.Ray.south(southMSB)
      val westCombo =
        if(westMSB < 0) westRay
        else westRay ^ BitMask.Ray.west(westMSB)

      // Get all moves for this bishop.
      moves = moves | northCombo | eastCombo | southCombo | westCombo

      // The twos-decrement removes this bishop.
      rooks &= (rooks-1)
    }

    // Return moves.
    moves
  }

  /**
   * Gets all pseudo-legal moves generated by this mover.
   * @return all pseudo-legal moves and captures.
   */
  override def getPseudos: BitMask = {
    val active = node.activePieces

    // Return moves.
    getAttacks(node.activeRooks) & ~active
  }

  /**
   * Serializes the pseudo-legal moves.
   * @return a list of pseudo-legal moves of the appropriate type.
   */
  override def mkList: List[Move] = {
    val builder = List.newBuilder[Move]
    var moves = getPseudos
    val activeRooks = node.activeRooks

    while(moves != BitMask.empty) {
      val lsb = BitMask.bitScanForward(moves)
      var sources = getAttacks(1l << lsb) & activeRooks

      while(sources != BitMask.empty) {
        val source = BitMask.bitScanForward(sources)
        if(node.empty(lsb)) {
          builder += RookMove(source, lsb)
        }
        else {
          builder += RookCapture(source, lsb, node.at(lsb).get)
        }
        sources &= (sources-1)
      }
      moves &= (moves-1)
    }

    builder.result()
  }
}
