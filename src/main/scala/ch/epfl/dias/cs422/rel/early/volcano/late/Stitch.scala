package ch.epfl.dias.cs422.rel.early.volcano.late

import ch.epfl.dias.cs422.helpers.builder.skeleton
import ch.epfl.dias.cs422.helpers.rel.RelOperator
import ch.epfl.dias.cs422.helpers.rel.RelOperator.{LateTuple, NilLateTuple, Tuple}

import scala.Function1.UnliftOps
import scala.collection.View.Empty
import scala.collection.mutable.ArrayBuffer

/**
  * @inheritdoc
  *
  * @see [[ch.epfl.dias.cs422.helpers.builder.skeleton.Stitch]]
  * @see [[ch.epfl.dias.cs422.helpers.rel.late.volcano.naive.Operator]]
  */
class Stitch protected(
                              left: ch.epfl.dias.cs422.helpers.rel.late.volcano.naive.Operator,
                              right: ch.epfl.dias.cs422.helpers.rel.late.volcano.naive.Operator
                            ) extends skeleton.Stitch[
  ch.epfl.dias.cs422.helpers.rel.late.volcano.naive.Operator
](left, right)
  with ch.epfl.dias.cs422.helpers.rel.late.volcano.naive.Operator {



  protected var leftRow: List[LateTuple] = _
  protected var leftMap = Map.empty[Long, Tuple]


  override def open(): Unit = {
    left.open()
    right.open()

    leftRow = Iterator.continually(left.next()).takeWhile(_ != NilLateTuple).map(_.get).toList
    leftMap = leftRow.toIndexedSeq.map(t => (t.vid, t.value)).toMap

  }

  /**
    * @inheritdoc
    */
  override def next(): Option[LateTuple] = {
    var right_elem = right.next()

    right_elem match {
      case Some(lt) => {
        if(leftMap.contains(lt.vid))
          Option(LateTuple(lt.vid, (leftMap.get(lt.vid).get ++ lt.value)))
        else NilLateTuple
      }
      case _ => NilLateTuple
    }

  }

  /**
    * @inheritdoc
    */
  override def close(): Unit = {
    left.close()
    right.close()
  }
}
