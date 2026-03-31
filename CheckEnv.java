public class CheckEnv {
    public static void main(String[] args) {
        System.out.println("PATH: " + System.getenv("PATH"));
        System.out.println("M2_HOME: " + System.getenv("M2_HOME"));
        System.out.println("JAVA_HOME: " + System.getenv("JAVA_HOME"));
    }
}