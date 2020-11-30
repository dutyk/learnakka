package io.kang.akka.stream.chapter01.StreamsQuickstartGuide

import akka.stream.{FlowShape, Inlet, Outlet, Shape}
import scala.annotation.unchecked.uncheckedVariance
import scala.collection.immutable

/**
 * A bidirectional flow of elements that consequently has two inputs and two
 * outputs, arranged like this:
 *
 * {{{
 *        +------+
 *  In1 ~>|      |~> Out1
 *        | bidi |
 * Out2 <~|      |<~ In2
 *        +------+
 * }}}
 */
final case class BidiShape[-In1, +Out1, -In2, +Out2](
                                                      in1: Inlet[In1 @uncheckedVariance],
                                                      out1: Outlet[Out1 @uncheckedVariance],
                                                      in2: Inlet[In2 @uncheckedVariance],
                                                      out2: Outlet[Out2 @uncheckedVariance])
  extends Shape {
  override val inlets: immutable.Seq[Inlet[_]] = in1 :: in2 :: Nil
  override val outlets: immutable.Seq[Outlet[_]] = out1 :: out2 :: Nil

  /**
   * Java API for creating from a pair of unidirectional flows.
   */
  def this(top: FlowShape[In1, Out1], bottom: FlowShape[In2, Out2]) = this(top.in, top.out, bottom.in, bottom.out)

  override def deepCopy(): BidiShape[In1, Out1, In2, Out2] =
    BidiShape(in1.carbonCopy(), out1.carbonCopy(), in2.carbonCopy(), out2.carbonCopy())

}
