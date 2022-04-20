package ch.epfl.dias.cs422.rel.early.volcano

import ch.epfl.dias.cs422.helpers.builder.skeleton
import ch.epfl.dias.cs422.helpers.rel.RelOperator
import ch.epfl.dias.cs422.helpers.rel.RelOperator.{LateTuple, NilTuple, Tuple}
import org.apache.calcite.rex.RexNode

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.break

/**
  * @inheritdoc
  * @see [[ch.epfl.dias.cs422.helpers.builder.skeleton.Join]]
  * @see [[ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator]]
  */
class Join(
    left: ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator,
    right: ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator,
    condition: RexNode
) extends skeleton.Join[
      ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator
    ](left, right, condition)
    with ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator {
  /**
    * Hint: you need to use methods getLeftKeys and getRightKeys
    * to implement joins
    */

  /**
    * @inheritdoc
    */

  private val leftKeys = getLeftKeys
  private val rightKeys = getRightKeys
  private var leftMap = Map.empty[Tuple, IndexedSeq[Tuple]]
  private var leftRow: Iterator[Tuple] = _
  private var rightRow: List[Tuple] = _
  private var buffer =  ArrayBuffer.empty[Tuple]


  override def open(): Unit = {
    left.open()
    right.open()

    leftRow = Iterator.continually(left.next()).takeWhile(_ != NilTuple).map(_.get)
    rightRow = Iterator.continually(right.next()).takeWhile(_ != NilTuple).map(_.get).toList

    leftMap = leftRow.toIndexedSeq.groupBy(t => leftKeys.map(k => t(k)))  //(index, tuple value)

  }





  override def next(): Option[Tuple] = {

    var res : Tuple = null
    if(buffer.size > 0){
      res = buffer(0)
      buffer.remove(0)
      return Option(res)
    } else {
      while(rightRow.size > 0){
        var right_elem = rightRow.head
        var key = rightKeys.map(k => right_elem(k))
        if(leftMap.contains(key)){
          var tmpTuples = leftMap.get(key).get
          for(l <- tmpTuples){
            buffer += (l :++ right_elem)
          }
          res = buffer(0)
          buffer.remove(0)
          rightRow = rightRow.tail
          return Option(res)
        }
        rightRow = rightRow.tail
      }
    }

    Option(res)


  }

  /**
    * @inheritdoc
    */
  override def close(): Unit = {
    buffer = ArrayBuffer.empty[Tuple]
    left.close()
    right.close()

  }
}
