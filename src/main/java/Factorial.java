public class Factorial {
    public int fact(int x) {
        int f = 1;
        int i = 1;
        while (i <= x) {
            f = f * i;
            i++;
        }
        return f;
    }
}
