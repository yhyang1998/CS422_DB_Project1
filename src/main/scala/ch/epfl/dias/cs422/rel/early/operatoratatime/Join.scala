package ch.epfl.dias.cs422.rel.early.operatoratatime

import ch.epfl.dias.cs422.helpers.builder.skeleton
import ch.epfl.dias.cs422.helpers.rel.RelOperator.{Column, Elem, Tuple}
import org.apache.calcite.rex.RexNode

/**
  * @inheritdoc
  * @see [[ch.epfl.dias.cs422.helpers.builder.skeleton.Join]]
  * @see [[ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator]]
  */
class Join(
    left: ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator,
    right: ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator,
    condition: RexNode
) extends skeleton.Join[
      ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator
    ](left, right, condition)
    with ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator {
  /**
    * Hint: you need to use methods getLeftKeys and getRightKeys
    * to implement joins
    */

  /**
   * @inheritdoc
   */
  override def execute(): IndexedSeq[Column] = {
    val rightKeys = getRightKeys
    val leftKeys = getLeftKeys

    val RightRow = right.execute().transpose.filter(_.last.asInstanceOf[Boolean])
    val LeftRow = left.execute().transpose.filter(_.last.asInstanceOf[Boolean]).map(_.dropRight(1))

    if(LeftRow.size > RightRow.size) {
      val mapRight: Map[Tuple, Iterable[Tuple]] = RightRow.groupBy(t => rightKeys.map(k => t(k)))

      LeftRow.flatMap(t => {mapRight.get(leftKeys.map(k => t(k))) match {
            case Some(tuples) => tuples.map(t :++ _)
            case _ => IndexedSeq.empty
          }
        }).transpose
    }else{
      val mapLeft: Map[Tuple, Iterable[Tuple]] = LeftRow.groupBy(t => leftKeys.map(k => t(k)))

      RightRow.flatMap(t => {mapLeft.get(rightKeys.map(k => t(k))) match {
            case Some(tuples) => tuples.map(_ :++ t)
            case _ => IndexedSeq.empty
          }
        }).transpose
    }

  }
}
