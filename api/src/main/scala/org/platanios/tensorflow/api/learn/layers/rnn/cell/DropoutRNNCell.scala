/* Copyright 2017, Emmanouil Antonios Platanios. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.platanios.tensorflow.api.learn.layers.rnn.cell

import org.platanios.tensorflow.api.learn.{Mode, TRAINING}
import org.platanios.tensorflow.api.ops

/** RNN cell that applies dropout to the provided RNN cell.
  *
  * Note that currently, a different dropout mask is used for each time step in an RNN (i.e., not using the variational
  * recurrent dropout method described in
  * ["A Theoretically Grounded Application of Dropout in Recurrent Neural Networks"](https://arxiv.org/abs/1512.05287).
  *
  * Note also that for LSTM cells, no dropout is applied to the memory tensor of the state. It is only applied to the
  * state tensor.
  *
  * @param  cell                  RNN cell on which to perform dropout.
  * @param  inputKeepProbability  Keep probability for the input of the RNN cell.
  * @param  outputKeepProbability Keep probability for the output of the RNN cell.
  * @param  stateKeepProbability  Keep probability for the output state of the RNN cell.
  * @param  seed                  Optional random seed, used to generate a random seed pair for the random number
  *                               generator, when combined with the graph-level seed.
  * @param  name                  Desired name for this layer (note that this name will be made unique by potentially
  *                               appending a number to it, if it has been used before for another layer).
  *
  * @author Emmanouil Antonios Platanios
  */
class DropoutRNNCell[O, OS, S, SS](
    val cell: RNNCell[O, OS, S, SS],
    val inputKeepProbability: Float = 1.0f,
    val outputKeepProbability: Float = 1.0f,
    val stateKeepProbability: Float = 1.0f,
    val seed: Option[Int] = None,
    override val name: String = "DropoutRNNCell"
)(implicit
    evO: ops.rnn.cell.DropoutRNNCell.Supported[O],
    evS: ops.rnn.cell.DropoutRNNCell.Supported[S]
) extends RNNCell[O, OS, S, SS](name) {
  require(inputKeepProbability > 0.0 && inputKeepProbability <= 1.0,
    s"'inputKeepProbability' ($inputKeepProbability) must be in (0, 1].")
  require(outputKeepProbability > 0.0 && outputKeepProbability <= 1.0,
    s"'outputKeepProbability' ($outputKeepProbability) must be in (0, 1].")
  require(stateKeepProbability > 0.0 && stateKeepProbability <= 1.0,
    s"'stateKeepProbability' ($stateKeepProbability) must be in (0, 1].")

  override val layerType: String = "DropoutRNNCell"

  override def createCell(mode: Mode): CellInstance[O, OS, S, SS] = {
    val cellInstance = cell.createCell(mode)
    mode match {
      case TRAINING =>
        val dropoutCell = ops.rnn.cell.DropoutRNNCell(
          cellInstance.cell, inputKeepProbability, outputKeepProbability, stateKeepProbability, seed,
          uniquifiedName)(evO, evS)
        CellInstance(dropoutCell, cellInstance.trainableVariables, cellInstance.nonTrainableVariables)
      case _ => cellInstance
    }
  }
}

object DropoutRNNCell {
  def apply[O, OS, S, SS](
      cell: RNNCell[O, OS, S, SS],
      inputKeepProbability: Float = 1.0f,
      outputKeepProbability: Float = 1.0f,
      stateKeepProbability: Float = 1.0f,
      seed: Option[Int] = None,
      name: String = "DropoutRNNCell"
  )(implicit
      evO: ops.rnn.cell.DropoutRNNCell.Supported[O],
      evS: ops.rnn.cell.DropoutRNNCell.Supported[S]
  ): DropoutRNNCell[O, OS, S, SS] = {
    new DropoutRNNCell(cell, inputKeepProbability, outputKeepProbability, stateKeepProbability, seed, name)(evO, evS)
  }
}
