package ch.epfl.dias.cs422.rel.early.operatoratatime

import ch.epfl.dias.cs422.helpers.builder.skeleton
import ch.epfl.dias.cs422.helpers.rel.RelOperator.{Column, Tuple, Elem}
import ch.epfl.dias.cs422.helpers.rex.AggregateCall
import org.apache.calcite.util.ImmutableBitSet

import scala.jdk.CollectionConverters._

/**
  * @inheritdoc
  * @see [[ch.epfl.dias.cs422.helpers.builder.skeleton.Aggregate]]
  * @see [[ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator]]
  */
class Aggregate protected (
    input: ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator,
    groupSet: ImmutableBitSet,
    aggCalls: IndexedSeq[AggregateCall]
) extends skeleton.Aggregate[
      ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator
    ](input, groupSet, aggCalls)
    with ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator {
  /**
    * Hint 1: See superclass documentation for semantics of groupSet and aggCalls
    * Hint 2: You do not need to implement each aggregate function yourself.
    * You can use reduce method of AggregateCall
    * Hint 3: In case you prefer a functional solution, you can use
    * groupMapReduce
    */

  /**
   * @inheritdoc
   */
  override def execute(): IndexedSeq[Column] = {
    val filtered_input = input.execute().transpose.filter(_.last.asInstanceOf[Boolean])   //don't care about the False Tuples
    if (filtered_input.isEmpty && groupSet.isEmpty) {
      IndexedSeq(aggCalls.map(aggEmptyValue).foldLeft(IndexedSeq.empty[Elem])((a, b) => a :+ b) :+ true
      ).transpose
    } else {
      val keys = groupSet.toArray

      filtered_input.map(_.toIndexedSeq).groupBy(tuple => keys.map(k => tuple(k)).toIndexedSeq)
        .map {case (key, tuples) => key.++(aggCalls.map(agg => tuples.map(t => agg.getArgument(t)).reduce(aggReduce(_, _, agg)))) :+ true
        }.toIndexedSeq.transpose
    }
  }
}
