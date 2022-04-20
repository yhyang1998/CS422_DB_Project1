package ch.epfl.dias.cs422.rel.early.volcano

import ch.epfl.dias.cs422.helpers.builder.skeleton
import ch.epfl.dias.cs422.helpers.rel.RelOperator.{Elem, NilTuple, Tuple}
import ch.epfl.dias.cs422.helpers.rex.AggregateCall
import org.apache.calcite.util.ImmutableBitSet

import scala.jdk.CollectionConverters._

/**
  * @inheritdoc
  * @see [[ch.epfl.dias.cs422.helpers.builder.skeleton.Aggregate]]
  * @see [[ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator]]
  */
class Aggregate protected (
    input: ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator,
    groupSet: ImmutableBitSet,
    aggCalls: IndexedSeq[AggregateCall]
) extends skeleton.Aggregate[
      ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator
    ](input, groupSet, aggCalls)
    with ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator {
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
   protected var aggregated = List.empty[(Tuple, Vector[Tuple])]


  override def open(): Unit = {

    input.open()
    var next = input.next()
    if (next == NilTuple && groupSet.isEmpty) {

      aggregated = List(
        (IndexedSeq.empty[Elem] -> Vector(aggCalls.map(aggEmptyValue).foldLeft(IndexedSeq.empty[Elem])((a, b) => a :+ b)
        ))
      )

    } else {

      val keyId = groupSet.toArray
      var aggregatesMap = Map.empty[Tuple, Vector[Tuple]]
      while (next != NilTuple) {
        val tuple: Tuple = next.get
        val key: Tuple = keyId.map(i => tuple(i))
        aggregatesMap = aggregatesMap.get(key) match {
          case Some(arr: Vector[Tuple]) => aggregatesMap + (key -> (arr :+ tuple))
          case _                        => aggregatesMap + (key -> Vector(tuple))
        }
        next = input.next()
      }

      aggregated = aggregatesMap.toList
    }


  }

  /**
    * @inheritdoc
    */
  override def next(): Option[Tuple] = {

    aggregated match {
      case (key, tuples) :: tail =>
        aggregated = tail
        Some(key.++(aggCalls.map(agg => tuples.map(t => agg.getArgument(t)).reduce(aggReduce(_, _, agg)))))
      case _ => NilTuple
    }



  }

  /**
    * @inheritdoc
    */
  override def close(): Unit = {
    input.close()
  }
}
