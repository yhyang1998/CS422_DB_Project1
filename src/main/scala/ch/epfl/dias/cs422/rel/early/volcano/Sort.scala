package ch.epfl.dias.cs422.rel.early.volcano

import java.util

import ch.epfl.dias.cs422.helpers.builder.skeleton
import ch.epfl.dias.cs422.helpers.rel.RelOperator.{Elem, NilTuple, Tuple}
import org.apache.calcite.rel.{RelCollation, RelFieldCollation}

import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.jdk.CollectionConverters._

/**
  * @inheritdoc
  * @see [[ch.epfl.dias.cs422.helpers.builder.skeleton.Sort]]
  * @see [[ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator]]
  */
class Sort protected (
    input: ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator,
    collation: RelCollation,
    offset: Option[Int],
    fetch: Option[Int]
) extends skeleton.Sort[
      ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator
    ](input, collation, offset, fetch)
    with ch.epfl.dias.cs422.helpers.rel.early.volcano.Operator {
  /**
    * Hint: See superclass documentation for info on collation i.e.
    * sort keys and direction
    */
  protected var iter:Iterator[Tuple] = _
  protected var scan_elem: IndexedSeq[Elem] = _
  protected var table_storage: IndexedSeq[IndexedSeq[Elem]] = _
  protected var table_sorted: IndexedSeq[IndexedSeq[Elem]] = _
  protected var table_size: Int = 0
  protected var scan_iter: Int = 0
  protected var order: Boolean = _
  protected var elem1_comp: Comparable[Elem] = _
  protected var elem2_comp: Comparable[Elem] = _
  protected var coll_iter:  util.List[RelFieldCollation] = _


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

  override def open(): Unit = {
    input.open()
    iter = input.iterator
    table_storage = IndexedSeq[IndexedSeq[Elem]]()    //store all the unsorted elements

    while(iter.hasNext){
      scan_elem = iter.next()
      table_storage = table_storage :+ scan_elem
      table_size += 1
    }

    table_sorted = table_storage.sortWith((elem1,elem2) => sortCollation(elem1,elem2,collation))

    if(offset != None) {
      if(table_size > scan_iter + offset.get) scan_iter += offset.get
    }
    if(fetch != None) {
      if(table_size > scan_iter + fetch.get) table_size = scan_iter + fetch.get
    }

  }

  /**
    * @inheritdoc
    */
  override def next(): Option[Tuple] = {
    var res : Tuple = null
    if(scan_iter < table_size)
    {
      res = table_sorted(scan_iter)
      scan_iter += 1
    }
    Option(res)
  }

  /**
    * @inheritdoc
    */
  override def close(): Unit = {
    table_sorted = null
    table_storage = null
    input.close()
  }
}
