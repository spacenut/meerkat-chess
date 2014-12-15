package com.alanjz.meerkat.moves

import com.alanjz.meerkat.position.mutable.MaskNode
import com.alanjz.meerkat.util.numerics.BitMask
import com.alanjz.meerkat.util.numerics.BitMask._

class QueenMover(val node : MaskNode) extends IntermediateMover {
  /**
   * Returns all possible moves regardless of legality
   * (Or pseudo-legality).
   *
   * These are specifically attacking moves.
   * This does not include castling, or pawn advances.
   *
   * @param pieces the queens mask.
   * @return the attacks possible by this piece.
   */
  override def getAttacks(pieces : BitMask): BitMask =
    new BishopMover(node).getAttacks(pieces) |
      new RookMover(node).getAttacks(pieces)

  /**
   * Gets all pseudo-legal moves generated by this mover.
   * @return all pseudo-legal moves and captures.
   */
  override def getPseudos: BitMask = {
    val active = node.activePieces

    // Return moves.
    getAttacks(node.activeQueens) & ~active
  }

  /**
   * Serializes the pseudo-legal moves.
   * @return a list of pseudo-legal moves of the appropriate type.
   */
  override def mkList: List[Move] = ???
}
