package ch.epfl.dias.cs422.rel.early.columnatatime

import ch.epfl.dias.cs422.helpers.builder.skeleton
import ch.epfl.dias.cs422.helpers.rel.RelOperator._
import org.apache.calcite.rel.`type`.RelDataType
import org.apache.calcite.rex.RexNode

import scala.jdk.CollectionConverters._

/**
  * @inheritdoc
  * @see [[ch.epfl.dias.cs422.helpers.builder.skeleton.Project]]
  * @see [[ch.epfl.dias.cs422.helpers.rel.early.columnatatime.Operator]]
  */
class Project protected (
    input: ch.epfl.dias.cs422.helpers.rel.early.columnatatime.Operator,
    projects: java.util.List[_ <: RexNode],
    rowType: RelDataType
) extends skeleton.Project[
      ch.epfl.dias.cs422.helpers.rel.early.columnatatime.Operator
    ](input, projects, rowType)
    with ch.epfl.dias.cs422.helpers.rel.early.columnatatime.Operator {

  /**
    * Function that, when given a (non-NilTuple) tuple produced by the [[input]] operator,
    * it returns a new tuple composed of the evaluated projections [[projects]]
    */
  lazy val evals: IndexedSeq[IndexedSeq[HomogeneousColumn] => HomogeneousColumn] =
    projects.asScala.map(e => map(e, input.getRowType, isFilterCondition = false)).toIndexedSeq

  /**
   * @inheritdoc
   */
  def execute(): IndexedSeq[HomogeneousColumn] = {

    val executed_input = input.execute()
    val left = executed_input.dropRight(1)
    if (executed_input.isEmpty) {
      executed_input
    } else {
      evals.map(e => e(left)) :+ executed_input.last
    }
  }
}
