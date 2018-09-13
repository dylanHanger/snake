public class Neuron {
    double[] value;
    final int layer; // i
    final int index; // j

    public Neuron(int i, int j) {
        value = new double[2]; // [1,0] or [0,1]
        layer = i;
        index = j;
    }
}
