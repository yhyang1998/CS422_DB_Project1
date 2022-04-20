package ch.epfl.dias.cs422.rel.early.operatoratatime

import ch.epfl.dias.cs422.helpers.builder.skeleton
import ch.epfl.dias.cs422.helpers.rel.RelOperator.{Column, Elem, Tuple}
import org.apache.calcite.rel.{RelCollation, RelFieldCollation}

import java.util
import scala.jdk.CollectionConverters._

/**
  * @inheritdoc
  * @see [[ch.epfl.dias.cs422.helpers.builder.skeleton.Sort]]
  * @see [[ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator]]
  */
class Sort protected (
    input: ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator,
    collation: RelCollation,
    offset: Option[Int],
    fetch: Option[Int]
) extends skeleton.Sort[
      ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator
    ](input, collation, offset, fetch)
    with ch.epfl.dias.cs422.helpers.rel.early.operatoratatime.Operator {
  /**
    * Hint: See superclass documentation for info on collation i.e.
    * sort keys and direction
    */

  protected var iter:Iterator[Column] = _
  protected var scan_elem: Column = _
  protected var table_sorted: IndexedSeq[IndexedSeq[Elem]] = _
  protected var table_result : IndexedSeq[IndexedSeq[Elem]] = _
  protected var table_size: Int = 0
  protected var scan_iter: Int = 0
  protected var order: Boolean = _
  protected var elem1_comp: Comparable[Elem] = _
  protected var elem2_comp: Comparable[Elem] = _
  protected var coll_iter:  util.List[RelFieldCollation] = _
  /**
   * @inheritdoc
   */
  def sortCollation(elem1 : Tuple, elem2 : Tuple, coll: RelCollation): Boolean = {
    coll_iter = coll.getFieldCollations()
    for(i <- 0 until coll_iter.size()){
      elem1_comp = elem1(coll_iter.get(i).getFieldIndex).asInstanceOf[Comparable[Elem]]
      elem2_comp = elem2(coll_iter.get(i).getFieldIndex).asInstanceOf[Comparable[Elem]]
      if(elem1_comp.compareTo(elem2_comp) != 0){    // not equal
        order = elem1_comp.compareTo(elem2_comp) > 0
        if(coll_iter.get(i).getDirection.isDescending)  return order
        else return !order
      }
    }
    false
  }

  override def execute(): IndexedSeq[Column] = {

    var table_storage = input.execute().transpose.filter(_.last.asInstanceOf[Boolean])
    table_size = table_storage.size
    table_sorted = table_storage.sortWith((elem1,elem2) => sortCollation(elem1,elem2,collation))

    if(offset != None) {
      if(table_size > scan_iter + offset.get) scan_iter += offset.get
    }
    if(fetch != None) {
      if(table_size > scan_iter + fetch.get) table_size = scan_iter + fetch.get
    }
    table_result = IndexedSeq[Column]()
    for(i <- scan_iter until table_size) {
      table_result = table_result :+ table_sorted(i)
    }
    table_result.transpose
  }
}
