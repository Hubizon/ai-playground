

## Core Packet

The Core packet contains the fundamental logic for building and training neural networks. It is composed of four main classes:

- **NeuralNet** – Provides functions to train neural networks and relay training callbacks to the view model.
- **Tensor** – A data structure for storing numerical data. Supports basic deep learning operations and records these operations for backpropagation using a computational graph.
- **ComputationalGraph** – Implements an autograd engine by managing a topologically sorted directed acyclic graph (DAG) of operations to compute gradients.
- **Dataset** – Handles loading data from files into tensors and provides functionality for train/test splitting.

Additionally, the packet defines several interfaces:

- **Layers**
- **Optimizers**
- **Loss_Fn**

The packet also includes raw example implementations of simple neural networks built with these components.
