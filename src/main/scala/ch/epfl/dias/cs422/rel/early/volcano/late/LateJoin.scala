package ch.epfl.dias.cs422.rel.early.volcano.late

import ch.epfl.dias.cs422.helpers.builder.skeleton
import ch.epfl.dias.cs422.helpers.rel.RelOperator
import ch.epfl.dias.cs422.helpers.rel.RelOperator.{LateTuple, NilLateTuple, NilTuple, Tuple}
import org.apache.calcite.rex.RexNode

import scala.collection.mutable.ArrayBuffer

/**
  * @inheritdoc
  *
  * @see [[ch.epfl.dias.cs422.helpers.builder.skeleton.Join]]
  * @see [[ch.epfl.dias.cs422.helpers.rel.late.volcano.naive.Operator]]
  */
class LateJoin(
               left: ch.epfl.dias.cs422.helpers.rel.late.volcano.naive.Operator,
               right: ch.epfl.dias.cs422.helpers.rel.late.volcano.naive.Operator,
               condition: RexNode
             ) extends skeleton.Join[
  ch.epfl.dias.cs422.helpers.rel.late.volcano.naive.Operator
](left, right, condition)
  with ch.epfl.dias.cs422.helpers.rel.late.volcano.naive.Operator {
  /**
    * Hint: you need to use methods getLeftKeys and getRightKeys
    * to implement joins
    */

  private val leftKeys = getLeftKeys
  private val rightKeys = getRightKeys
  private var leftMap: Map[IndexedSeq[RelOperator.Elem], Iterable[LateTuple]] = _
  private var leftRow: Iterator[LateTuple] = _
  private var rightRow: List[LateTuple] = _
  private var buffer =  ArrayBuffer.empty[LateTuple]

  private var res_count: Int = 0
  /**
    * @inheritdoc
    */
  override def open(): Unit = {
    left.open()
    right.open()

    leftRow = Iterator.continually(left.next()).takeWhile(_ != NilLateTuple).map(_.get)
    rightRow = Iterator.continually(right.next()).takeWhile(_ != NilLateTuple).map(_.get).toList

    leftMap = leftRow.toIndexedSeq.groupBy(t => leftKeys.map(k => t.value(k)))  //(index, tuple value)

  }

  /**
    * @inheritdoc
    */
  override def next(): Option[LateTuple] = {

    var res : LateTuple = null

    if(buffer.size > 0){
      res = buffer(0)
      buffer.remove(0)
      return Option(res)
    } else {
      while(rightRow.size > 0){
        var right_elem = rightRow.head
        var key = rightKeys.map(k => right_elem.value(k))
        if(leftMap.contains(key)){
          var tmpTuples = leftMap.get(key).get
          for(l <- tmpTuples){
            buffer += LateTuple(res_count, l.value :++ right_elem.value)
            res_count += 1
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
    buffer = ArrayBuffer.empty[LateTuple]
    left.close()
    right.close()
  }
}
